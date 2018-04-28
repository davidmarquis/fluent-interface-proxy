package com.fluentinterface.proxy.internal;

import com.fluentinterface.beans.ObjectWrapper;
import com.fluentinterface.beans.PropertyNotFoundException;
import com.fluentinterface.beans.reflect.Bean;
import com.fluentinterface.beans.reflect.Property;
import com.fluentinterface.proxy.PropertyAccessStrategy;

import java.util.Optional;

/**
 * A strategy that uses public setters to set target bean properties (using Apache BeanUtils PropertyUtils).
 */
public class SetterPropertyAccessStrategy implements PropertyAccessStrategy {

    public Class getPropertyType(Class<?> targetClass, String property) {
        return Optional.ofNullable(ObjectWrapper.getProperty(Bean.forClass(targetClass), property)).map(Property::getType).orElse(null);
    }

    public void setPropertyValue(Object target, String property, Object value) {
        try {
            new ObjectWrapper(target).setSimpleValue(property, value);
        } catch (PropertyNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

}