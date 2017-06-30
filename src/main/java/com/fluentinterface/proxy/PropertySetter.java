package com.fluentinterface.proxy;

public interface PropertySetter {

    String getPropertyName();

    void apply(PropertyTarget target, Object value) throws Exception;
}
