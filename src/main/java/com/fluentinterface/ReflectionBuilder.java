package com.fluentinterface;

import com.fluentinterface.builder.Builder;
import com.fluentinterface.proxy.BuilderDelegate;
import com.fluentinterface.proxy.DefaultBuilderDelegate;
import com.fluentinterface.proxy.Instantiator;
import com.fluentinterface.proxy.PropertyAccessStrategy;
import com.fluentinterface.proxy.internal.BuilderProxy;
import com.fluentinterface.proxy.internal.FieldPropertyAccessStrategy;
import com.fluentinterface.proxy.internal.SetterPropertyAccessStrategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ReflectionBuilder<B> {

    private static BuilderDelegate defaultBuilderDelegate = new DefaultBuilderDelegate();

    private BuilderDelegate<? super B> builderDelegate;
    private Class<B> builderInterface;
    private Class<?> builtClass = null;
    private PropertyAccessStrategy propertyAccessStrategy;
    private Instantiator instantiator;

    @SuppressWarnings("unchecked")
    private ReflectionBuilder(Class<B> builderInterface) {
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
        return new ReflectionBuilder<>(builderInterface);
    }

    public ReflectionBuilder<B> builds(Class<?> objectsOfType) {
        this.builtClass = objectsOfType;
        return this;
    }

    public ReflectionBuilder<B> withDelegate(BuilderDelegate<? super B> builderDelegate) {
        this.builderDelegate = builderDelegate;
        return this;
    }

    public ReflectionBuilder<B> usingAttributeAccessStrategy(PropertyAccessStrategy strategy) {
        this.propertyAccessStrategy = strategy;
        return this;
    }

    public ReflectionBuilder<B> usingFieldsDirectly() {
        this.propertyAccessStrategy = new FieldPropertyAccessStrategy();
        return this;
    }

    public ReflectionBuilder<B> usingInstantiator(Instantiator instantiator) {
        this.instantiator = instantiator;
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
    public B create() {
        InvocationHandler handler = new BuilderProxy(builderInterface, getBuiltClass(),
                                                     builderDelegate, propertyAccessStrategy, instantiator);

        return (B) Proxy.newProxyInstance(
                builderInterface.getClassLoader(),
                new Class[]{builderInterface},
                handler);
    }

}
