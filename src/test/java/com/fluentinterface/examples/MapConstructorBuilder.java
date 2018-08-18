package com.fluentinterface.examples;

import com.fluentinterface.ReflectionBuilder;
import com.fluentinterface.annotation.Constructs;
import com.fluentinterface.builder.Builder;

import java.util.Map;

public interface MapConstructorBuilder extends Builder<MapConstructor> {
    @Constructs
    MapConstructorBuilder of(Map<String, Object> map);

    static MapConstructorBuilder aMapConstructor() {
        return ReflectionBuilder.implementationFor(MapConstructorBuilder.class).create();
    }
}
