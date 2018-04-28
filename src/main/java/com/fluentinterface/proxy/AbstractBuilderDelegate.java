package com.fluentinterface.proxy;

import com.fluentinterface.utils.GenericsUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

public abstract class AbstractBuilderDelegate<B> implements BuilderDelegate<B> {
    private Method buildMethod;

    public AbstractBuilderDelegate() {
        try {
            this.buildMethod = getBuilderClass().getDeclaredMethod(getBuildMethodName(), Object[].class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    String.format("Could not find [%s] method on [%s] class.", getBuildMethodName(), getBuilderClass()),
                    e
            );
        }
    }

    protected abstract String getBuildMethodName();

    protected abstract Class<B> getBuilderClass();

    public Class<?> getClassBuiltBy(Class<?> builderInterface) {
        return GenericsUtils.getDeclaredGenericType(builderInterface, getBuilderClass());
    }

    public Class<?> getClassBuiltBy(Object builder) {
        return Arrays.stream(builder.getClass().getInterfaces())
                     .filter(clazz -> getBuilderClass().isAssignableFrom(clazz))
                     .map(this::getClassBuiltBy)
                     .findFirst()
                     .orElseThrow(() -> new IllegalStateException(String.format(
                             "Could not determine which class builder [%s] builds", builder.getClass())));
    }

    public boolean isBuilderInstance(Object value) {
        return getBuilderClass().isInstance(value);
    }

    public boolean isBuildMethod(Method method) {
        return method.equals(buildMethod);
    }
}
