package com.fluentinterface.proxy;

public interface AttributeAccessStrategy {

    boolean hasProperty(Class<?> builtClass, String property);

    Class getPropertyType(Object target, String property) throws Exception;

    void setPropertyValue(Object target, String property, Object value) throws Exception;
}
