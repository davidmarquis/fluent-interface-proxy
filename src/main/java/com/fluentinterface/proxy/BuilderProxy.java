package com.fluentinterface.proxy;

import com.fluentinterface.annotation.Constructs;
import com.fluentinterface.proxy.impl.BestMatchingConstructor;
import com.fluentinterface.proxy.impl.BuildWithBuilderConverter;
import com.fluentinterface.proxy.impl.EmptyConstructorInstantiator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A dynamic proxy which will build a bean of the target type upon calls to the implemented builder interface.
 */
public class BuilderProxy implements InvocationHandler {

    private Class proxied;
    private Class  builtClass;
    private BuilderDelegate builderDelegate;
    private PropertyAccessStrategy propertyAccessStrategy;

    private Map<PropertySetter, Object> settersWithValues;
    private Instantiator instantiator;

    public BuilderProxy(Class builderInterface, Class builtClass, BuilderDelegate builderDelegate,
                        PropertyAccessStrategy propertyAccessStrategy) {

        this.proxied = builderInterface;
        this.builtClass = builtClass;
        this.builderDelegate = builderDelegate;
        this.propertyAccessStrategy = propertyAccessStrategy;

        this.settersWithValues = new LinkedHashMap<>();
        this.instantiator = new EmptyConstructorInstantiator(builtClass);
    }

    public Object invoke(Object target, Method method, Object[] params) throws Throwable {

        boolean isBuildMethod = isBuildMethod(method);
        boolean isConstructingMethod = isConstructingMethod(method);

        if (isConstructingMethod || isBuildMethod) {
            params = extractVarArgsIfNeeded(params);
            if (params.length > 0) {
                buildIfBuilderInstances(params);
                instantiator = new BestMatchingConstructor(builtClass, params);
            }
        }

        if (isConstructingMethod) {
            return target;
        }

        if (isBuildMethod) {
            return createInstanceFromProperties();
        }

        boolean isFluentSetterMethod = isFluentSetter(method);

        if (isFluentSetterMethod) {
            PropertySetterFactory factory = new PropertySetterFactory(propertyAccessStrategy, builtClass, builderDelegate);
            PropertySetter setter = factory.createSetterFor(method);
            Object valueForProperty = (params == null || params.length == 0)
                    ? null : params[0];

            if (!hasProperty(builtClass, setter.getPropertyName())) {
                throw new IllegalStateException(String.format(
                        "Method [%s] on [%s] corresponds to unknown property [%s] on built class [%s]",
                        method.getName(), proxied, setter.getPropertyName(), builtClass)
                );
            }

            settersWithValues.put(setter, valueForProperty);

            return target;
        }

        throw new IllegalStateException("Unrecognized builder method invocation: " + method);
    }

    private Object[] extractVarArgsIfNeeded(Object[] params) {
        if (params != null
                && params.length == 1
                && params[params.length - 1].getClass().isArray()) {
            return (Object[]) params[params.length - 1];
        }
        return params;
    }

    private boolean hasProperty(Class<?> builtClass, String propertyName) {
        return propertyAccessStrategy.hasProperty(builtClass, propertyName);
    }

    private Object createInstanceFromProperties() throws Exception {
        if (instantiator == null) {
            throw new IllegalStateException("No instantiator set for builder");
        }

        Object instance = instantiator.instantiate();
        PropertyTarget target = this.propertyAccessStrategy.targetFor(instance);

        for (Map.Entry<PropertySetter, Object> entry : settersWithValues.entrySet()) {
            PropertySetter setter = entry.getKey();
            Object value = entry.getValue();

            setter.apply(target, value);
        }

        return instance;
    }

    private void buildIfBuilderInstances(Object[] params) {
        BuildWithBuilderConverter builder = new BuildWithBuilderConverter(builderDelegate);
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
}
