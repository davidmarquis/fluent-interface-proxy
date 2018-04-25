package com.fluentinterface.proxy;

public interface PropertyAccessStrategy {

    Class getPropertyType(Class<?> targetClass, String property);

    void setPropertyValue(Object target, String property, Object value) throws Exception;

    default PropertyTarget targetFor(final Object instance) {
        return (property, value) -> PropertyAccessStrategy.this.setPropertyValue(instance, property, value);
    }
}
