package com.fluentinterface.proxy.internal;

import com.fluentinterface.proxy.BuilderDelegate;

import java.util.function.Function;

class BuildWithBuilder implements Function<Object, Object> {

    private BuilderDelegate builderDelegate;

    BuildWithBuilder(BuilderDelegate builderDelegate) {
        this.builderDelegate = builderDelegate;
    }

    @SuppressWarnings("unchecked")
    public Object apply(Object value) {
        if (builderDelegate.isBuilderInstance(value)) {
            return builderDelegate.build(value);
        }
        return value;
    }
}
