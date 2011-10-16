package com.fluentinterface;

import com.fluentinterface.builder.Builder;
import com.fluentinterface.builder.BuilderDelegate;
import com.fluentinterface.utils.GenericsUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ReflectionBuilder<T> {

    private BuilderDelegate<?> builderDelegate;
    private Class<T> builderInterface;
    private Class<?> builtClass;

    private ReflectionBuilder(Class<T> builderInterface) {
        if (!builderInterface.isInterface()) {
            throw new IllegalArgumentException(String.format(
                    "Can only create dynamic builder for interfaces. [%s] is not an interface.", builderInterface));
        }

        this.builderInterface = builderInterface;
        this.builtClass = implyBuiltClassFromImplementedInterface(builderInterface);
        this.builderDelegate = new DefaultBuilderDelegate();
    }

    public static <T> ReflectionBuilder<T> implementationFor(Class<T> builderInterface) {
        return new ReflectionBuilder<T>(builderInterface);
    }

    public ReflectionBuilder builds(Class<?> objectsOfType) {
        this.builtClass = objectsOfType;
        return this;
    }

    public ReflectionBuilder with(BuilderDelegate builderDelegate) {
        this.builderDelegate = builderDelegate;
        return this;
    }

    public T create() {
        if (builtClass == null) {
            throw new IllegalStateException(String.format(
                    ""));
        }

        InvocationHandler invokeHandler = new BuilderProxy(builderInterface, builtClass, builderDelegate);
        return (T) Proxy.newProxyInstance(
                builderInterface.getClassLoader(),
                new Class[]{builderInterface},
                invokeHandler);
    }

    private Class<?> implyBuiltClassFromImplementedInterface(Class<?> proxied) {
        return GenericsUtils.getGenericTypeOf(proxied);
    }

    private static class DefaultBuilderDelegate implements BuilderDelegate<Builder> {
        public Object build(Builder builder) {
            return builder.build();
        }
    }
}
