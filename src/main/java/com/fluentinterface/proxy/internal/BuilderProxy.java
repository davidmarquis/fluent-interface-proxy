package com.fluentinterface.proxy.internal;

import com.fluentinterface.annotation.Constructs;
import com.fluentinterface.proxy.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A dynamic proxy that implements the Builder pattern, building a target bean and setting its properties according
 * to how the builder method were called.
 * <p>
 * The implemented interface should be designed to adhere to the builder pattern, that is:
 * <p>
 * 1) Present a series of single-property methods returning the builder instance for chaining calls. Example: {@code <pre>MyBuilder withName(String name)</pre>}
 * 2) Have a {@code <pre>build()</pre>} method returning the built object.
 * <p>
 * For convenience, a generic {@link com.fluentinterface.builder.Builder} interface is provided by the library.
 *
 * @param <T> The interface to dynamically implement.
 */
public class BuilderProxy<T> implements InvocationHandler {

    private Class proxied;
    private Class<T> builtClass;
    private BuilderDelegate builderDelegate;
    private PropertyAccessStrategy propertyAccessStrategy;

    private Map<PropertySetter, Object> settersWithValues;
    private Instantiator<T> instantiator;
    private PropertySetterFactory setterFactory;

    public BuilderProxy(Class builderInterface,
                        Class<T> builtClass,
                        BuilderDelegate builderDelegate,
                        PropertyAccessStrategy propertyAccessStrategy,
                        Instantiator instantiator) {

        this.proxied = builderInterface;
        this.builtClass = builtClass;
        this.builderDelegate = builderDelegate;
        this.propertyAccessStrategy = propertyAccessStrategy;

        this.settersWithValues = new LinkedHashMap<>();
        this.instantiator = instantiator != null ? instantiator : new EmptyConstructor<>(builtClass);
        this.setterFactory = new PropertySetterFactory(propertyAccessStrategy, builtClass, builderDelegate);
    }

    public Object invoke(Object target, Method method, Object[] params) throws Throwable {
        if (method.isDefault()) {
            return invokeDefaultMethod(target, method, params);
        }

        if (isConstructingMethod(method)) {
            instantiator = new BestMatchingConstructor<>(builtClass, builderDelegate, params);
            return target;
        }

        if (isBuildMethod(method)) {
            params = extractVarArgsIfNeeded(params);
            if (params.length > 0) {
                buildIfBuilderInstances(params);
                instantiator = new BestMatchingConstructor<>(builtClass, builderDelegate, params);
            }
            return createInstanceFromProperties();
        }

        if (isFluentSetter(method)) {
            PropertySetter setter = setterFactory.createPropertySetter(method);
            Object valueForProperty = (params == null || params.length == 0)
                    ? null : params[0];

            settersWithValues.put(setter, valueForProperty);

            return target;
        }

        throw new IllegalStateException("Unrecognized builder method invocation: " + method);
    }

    private Object createInstanceFromProperties() throws Exception {
        Object instance = instantiator.instantiate(new State());
        PropertyTarget target = this.propertyAccessStrategy.targetFor(instance);

        for (Map.Entry<PropertySetter, Object> entry : settersWithValues.entrySet()) {
            PropertySetter setter = entry.getKey();
            Object value = entry.getValue();

            setter.apply(target, value);
        }

        return instance;
    }

    private void buildIfBuilderInstances(Object[] params) {
        BuildWithBuilder builder = new BuildWithBuilder(builderDelegate);
        for (int i = 0; i < params.length; i++) {
            params[i] = builder.apply(params[i]);
        }
    }

    private boolean hasBuilderDelegate() {
        return (builderDelegate != null);
    }

    private boolean isFluentSetter(Method method) {
        return method.getReturnType().isAssignableFrom(this.proxied)
                && !this.isBuildMethod(method);
    }

    private boolean isConstructingMethod(Method method) {
        return method.getAnnotation(Constructs.class) != null
                && method.getReturnType().isAssignableFrom(this.proxied);
    }

    private boolean isBuildMethod(Method method) {
        if (hasBuilderDelegate()) {
            return builderDelegate.isBuildMethod(method);
        }
        return method.getReturnType() == Object.class;
    }

    private Object[] extractVarArgsIfNeeded(Object[] params) {
        if (params != null
                && params.length == 1
                && params[params.length - 1].getClass().isArray()) {
            return (Object[]) params[params.length - 1];
        }
        return params;
    }

    private Object invokeDefaultMethod(Object target, Method method, Object[] params) throws Throwable {
        try {
            return invokeDefaultMethodJava8(target, method, params);
        } catch (IllegalAccessException e) {
            return invokeDefaultMethodJava9(target, method, params);
        }
    }

    private Object invokeDefaultMethodJava8(Object target, Method method, Object[] params) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);

        constructor.setAccessible(true);

        return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
                          .unreflectSpecial(method, declaringClass)
                          .bindTo(target)
                          .invokeWithArguments(params);
    }

    private Object invokeDefaultMethodJava9(Object target, Method method, Object[] params) throws Throwable {
        return MethodHandles.lookup()
                            .findSpecial(
                                    proxied,
                                    method.getName(),
                                    MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                                    proxied)
                            .bindTo(target)
                            .invokeWithArguments(params);
    }

    private class State implements BuilderState {

        private BuildWithBuilder builderConverter;

        State() {
            builderConverter = new BuildWithBuilder(builderDelegate);
        }

        public boolean hasValueFor(String... properties) {
            return Arrays.stream(properties)
                         .allMatch(prop -> findSetterFor(prop).isPresent());
        }

        public <P> Optional<P> peek(String property, Class<P> type) {
            return findSetterFor(property).map(setter -> {
                Object value = settersWithValues.get(setter);
                return getValueForTargetProperty(setter, value);
            });
        }

        public <P> Optional<P> consume(String property, Class<P> type) {
            return findSetterFor(property).map(setter -> {
                Object value = settersWithValues.remove(setter);
                return getValueForTargetProperty(setter, value);
            });
        }

        public Object coerce(Object value, Class<?> targetType) {
            return new CoerceValueConverter(targetType, builderConverter).apply(value);
        }

        private Optional<PropertySetter> findSetterFor(String property) {
            for (Map.Entry<PropertySetter, Object> setterEntries : settersWithValues.entrySet()) {
                PropertySetter setter = setterEntries.getKey();
                if (property.equals(setter.getPropertyName())) {
                    return Optional.of(setter);
                }
            }
            return Optional.empty();
        }

        private <P> P getValueForTargetProperty(PropertySetter setter, Object value) {
            PropertyHolder<P> holder = new PropertyHolder<>();
            try {
                setter.apply(holder, value);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return holder.value;
        }

        private class PropertyHolder<P> implements PropertyTarget {
            public P value;

            @SuppressWarnings("unchecked")
            public void setProperty(String property, Object value) {
                this.value = (P) value;
            }
        }
    }
}
