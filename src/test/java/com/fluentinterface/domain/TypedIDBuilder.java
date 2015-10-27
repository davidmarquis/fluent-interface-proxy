package com.fluentinterface.domain;

import com.fluentinterface.builder.Builder;


public interface TypedIDBuilder<T> extends Builder<TypedID<T>> {

    TypedIDBuilder<T> withId(String id);

}
