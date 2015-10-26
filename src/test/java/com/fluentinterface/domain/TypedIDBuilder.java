package com.fluentinterface.domain;

import com.fluentinterface.builder.Builder;

/**
 * YouTab Media 2011 Ltd.
 * Created by eladlaufer on 26/10/2015.
 */
public interface TypedIDBuilder<T> extends Builder<TypedID<T>> {

    TypedIDBuilder<T> withId(String id);

}
