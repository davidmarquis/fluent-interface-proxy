package com.fluentinterface;

import com.fluentinterface.builder.Builder;
import com.fluentinterface.proxy.BuilderDelegate;
import com.fluentinterface.proxy.BuilderProxy;
import com.fluentinterface.proxy.PropertyAccessStrategy;
import com.fluentinterface.proxy.impl.FieldPropertyAccessStrategy;
import com.fluentinterface.proxy.impl.SetterPropertyAccessStrategy;
import com.fluentinterface.utils.GenericsUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReflectionBuilder<T> {

    private static BuilderDelegate defaultBuilderDelegate = new InternalBuilderAsSuperClassDelegate();

    private BuilderDelegate<? super T> builderDelegate;
    private Class<T> builderInterface;
    private Class<?> builtClass = null;
    private PropertyAccessStrategy propertyAccessStrategy;

    @SuppressWarnings("unchecked")
    private ReflectionBuilder(Class<T> builderInterface) {
        if (!builderInterface.isInterface()) {
            throw new IllegalArgumentException(String.format(
                    "Can only create dynamic builder for interfaces. [%s] is not an interface.", builderInterface));
        }

        this.builderInterface = builderInterface;
        this.builderDelegate = defaultBuilderDelegate;
        this.propertyAccessStrategy = new SetterPropertyAccessStrategy();
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

    public ReflectionBuilder<T> usingAttributeAccessStrategy(PropertyAccessStrategy strategy) {
        this.propertyAccessStrategy = strategy;
        return this;
    }

    public ReflectionBuilder<T> usingFieldsDirectly() {
        this.propertyAccessStrategy = new FieldPropertyAccessStrategy();
        return this;
    }

    public Class<?> getBuiltClass() {
        if (builtClass != null) {
            return builtClass;
        }

        builtClass = builderDelegate.getClassBuiltBy(builderInterface);

        if (builtClass == null) {
            throw new IllegalStateException(String.format(
                    "Could not imply class being built by builder [%s]. " +
                            "If the interface does not extend [%s], you must explicitly set the type of object being built using the 'builds(class)' method.",
                    builderInterface, Builder.class
            ));
        }

        return builtClass;
    }

    @SuppressWarnings("unchecked")
    public T create() {

        InvocationHandler handler = new BuilderProxy(builderInterface, getBuiltClass(), builderDelegate, propertyAccessStrategy);

        return (T) Proxy.newProxyInstance(
                builderInterface.getClassLoader(),
                new Class[]{builderInterface},
                handler);
    }

    private static class InternalBuilderAsSuperClassDelegate implements BuilderDelegate<Builder> {

        private static final String BUILD_METHOD_NAME = "build";
        private Method buildMethod;

        public InternalBuilderAsSuperClassDelegate() {
            try {
                this.buildMethod = Builder.class.getDeclaredMethod(BUILD_METHOD_NAME, Object[].class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        String.format("Could not find [%s] method on [%s] class.", BUILD_METHOD_NAME, Builder.class),
                        e
                );
            }
        }

        @Override
        public Class<?> getClassBuiltBy(Class<?> builderInterface) {
            return GenericsUtils.getDeclaredGenericType(builderInterface, Builder.class);
        }

        @Override
        public Object build(Builder builder) {
            return builder.build();
        }

        @Override
        public boolean isBuilderInstance(Object value) {
            return value instanceof Builder;
        }

        @Override
        public boolean isBuildMethod(Method method) {
            return method.equals(buildMethod);
        }
    }
}
