package com.fluentinterface.proxy.impl;

import com.fluentinterface.builder.Builder;

public class DefaultBuilderDelegate extends AbstractBuilderDelegate<Builder> {
    protected String getBuildMethodName() {
        return "build";
    }

    protected Class<Builder> getBuilderClass() {
        return Builder.class;
    }

    public Object build(Builder builder) {
        return builder.build();
    }
}
