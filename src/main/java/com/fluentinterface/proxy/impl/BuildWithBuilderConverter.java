package com.fluentinterface.proxy.impl;

import com.fluentinterface.proxy.BuilderDelegate;

import java.util.function.Function;

public class BuildWithBuilderConverter implements Function<Object, Object> {

    private BuilderDelegate builderDelegate;

    public BuildWithBuilderConverter(BuilderDelegate builderDelegate) {
        this.builderDelegate = builderDelegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object apply(Object value) {
        if (!hasBuilderDelegate()) {
            return value;
        }

        if (builderDelegate.isBuilderInstance(value)) {
            return builderDelegate.build(value);
        }

        return value;
    }

    private boolean hasBuilderDelegate() {
        return (builderDelegate != null);
    }
}
