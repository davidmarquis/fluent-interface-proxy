package com.fluentinterface.domain;

import com.fluentinterface.builder.Builder;


public interface TypedStringIDBuilder<T> extends Builder<TypedStringID<T>> {

    TypedStringIDBuilder<T> withId(String id);

}
