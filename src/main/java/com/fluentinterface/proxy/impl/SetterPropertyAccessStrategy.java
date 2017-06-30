package com.fluentinterface.proxy.impl;

import com.fluentinterface.proxy.PropertyAccessStrategy;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

/**
 * A strategy that uses public setters to set target bean properties (using Apache BeanUtils PropertyUtils).
 */
public class SetterPropertyAccessStrategy implements PropertyAccessStrategy {

    private PropertyDescriptor getPropertyDescriptor(Class<?> builtClass, String propertyName) {
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(builtClass);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getName().equals(propertyName)) {
                return propertyDescriptor;
            }
        }
        return null;
    }

    public boolean hasProperty(Class<?> builtClass, String propertyName) {
        return getPropertyDescriptor(builtClass, propertyName) != null;
    }

    public Class getPropertyType(Class<?> targetClass, String property) {
        PropertyDescriptor propertyDescriptor = getPropertyDescriptor(targetClass, property);
        return propertyDescriptor != null ? propertyDescriptor.getPropertyType() : null;
    }

    public void setPropertyValue(Object target, String property, Object value) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PropertyUtils.setProperty(target, property, value);
    }
}
