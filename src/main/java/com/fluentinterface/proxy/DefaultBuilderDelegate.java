package com.fluentinterface.proxy;

import com.fluentinterface.builder.Builder;

/**
 * Bridges the provided {@link Builder} interface as the builder interface.
 */
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
