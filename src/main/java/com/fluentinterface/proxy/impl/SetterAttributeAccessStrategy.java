package com.fluentinterface.proxy.impl;

import com.fluentinterface.proxy.AttributeAccessStrategy;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

/**
 * A strategy that uses public setters to set target bean properties (using Apache BeanUtils PropertyUtils).
 */
public class SetterAttributeAccessStrategy implements AttributeAccessStrategy {

    public boolean hasProperty(Class<?> builtClass, String propertyName) {
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(builtClass);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    public Class getPropertyType(Object target, String property) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return PropertyUtils.getPropertyType(target, property);
    }

    public void setPropertyValue(Object target, String property, Object value) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PropertyUtils.setProperty(target, property, value);
    }
}
