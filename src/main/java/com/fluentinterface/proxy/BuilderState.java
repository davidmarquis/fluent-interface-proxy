package com.fluentinterface.proxy;

public interface BuilderState {
    Object coerce(Object value, Class<?> targetType);
}
