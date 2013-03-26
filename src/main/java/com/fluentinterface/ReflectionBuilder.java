package com.fluentinterface;

import com.fluentinterface.builder.Builder;
import com.fluentinterface.proxy.AttributeAccessStrategy;
import com.fluentinterface.proxy.BuilderDelegate;
import com.fluentinterface.proxy.BuilderProxy;
import com.fluentinterface.proxy.impl.FieldAttributeAccessStrategy;
import com.fluentinterface.proxy.impl.SetterAttributeAccessStrategy;
import com.fluentinterface.utils.GenericsUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReflectionBuilder<T> {

    private static BuilderDelegate defaultBuilderDelegate = new InternalBuilderAsSuperClassDelegate();

    private BuilderDelegate<? super T> builderDelegate;
    private Class<T> builderInterface;
    private Class<?> builtClass;
    private AttributeAccessStrategy attributeAccessStrategy;

    @SuppressWarnings("unchecked")
    private ReflectionBuilder(Class<T> builderInterface) {
        if (!builderInterface.isInterface()) {
            throw new IllegalArgumentException(String.format(
                    "Can only create dynamic builder for interfaces. [%s] is not an interface.", builderInterface));
        }

        this.builderInterface = builderInterface;
        this.builtClass = implyBuiltClassFromImplementedInterface(builderInterface);
        this.builderDelegate = defaultBuilderDelegate;
        this.attributeAccessStrategy = new SetterAttributeAccessStrategy();
    }

    public static void setDefaultBuilderDelegate(BuilderDelegate delegate) {
        defaultBuilderDelegate = delegate;
    }

    public static <T> ReflectionBuilder<T> implementationFor(Class<T> builderInterface) {
        return new ReflectionBuilder<T>(builderInterface);
    }

    public ReflectionBuilder<T> builds(Class<?> objectsOfType) {
        this.builtClass = objectsOfType;
        return this;
    }

    public ReflectionBuilder<T> withDelegate(BuilderDelegate<? super T> builderDelegate) {
        this.builderDelegate = builderDelegate;
        return this;
    }

    public ReflectionBuilder<T> usingAttributeAccessStrategy(AttributeAccessStrategy strategy) {
        this.attributeAccessStrategy = strategy;
        return this;
    }

    public ReflectionBuilder<T> usingFields() {
        this.attributeAccessStrategy = new FieldAttributeAccessStrategy();
        return this;
    }

    @SuppressWarnings("unchecked")
    public T create() {
        if (builtClass == null) {
            throw new IllegalStateException(String.format(
                    "Could not imply class being built by builder [%s]. " +
                            "If the interface does not extend [%s], you must explicitly set the type of object being built using the 'builds(class)' method.",
                    builderInterface, Builder.class
            ));
        }

        InvocationHandler handler = new BuilderProxy(builderInterface, builtClass, builderDelegate, attributeAccessStrategy);

        return (T) Proxy.newProxyInstance(
                builderInterface.getClassLoader(),
                new Class[]{builderInterface},
                handler);
    }

    private Class<?> implyBuiltClassFromImplementedInterface(Class<?> proxied) {
        return GenericsUtils.getGenericTypeOf(proxied);
    }

    private static class InternalBuilderAsSuperClassDelegate implements BuilderDelegate<Builder> {

        private static final String BUILD_METHOD_NAME = "build";
        private Method buildMethod;

        public InternalBuilderAsSuperClassDelegate() {
            try {
                this.buildMethod = Builder.class.getMethod(BUILD_METHOD_NAME);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        String.format("Could not find [%s] method on [%s] class.", BUILD_METHOD_NAME, Builder.class),
                        e
                );
            }
        }

        public Object build(Builder builder) {
            return builder.build();
        }

        public boolean isBuilderInstance(Object value) {
            return value instanceof Builder;
        }

        public boolean isBuildMethod(Method method) {
            return method.equals(buildMethod);
        }
    }
}
