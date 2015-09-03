package com.fluentinterface.proxy.impl;

import java.lang.reflect.Field;

import com.fluentinterface.proxy.AttributeAccessStrategy;

/**
 * Strategy that sets the target bean's attributes directly using the Reflection API (without going through the setters).
 */
public class FieldAttributeAccessStrategy implements AttributeAccessStrategy {

    public boolean hasProperty(Class<?> builtClass, String property) {

        Field field = getFieldFromClass(builtClass, property);

        return field != null;
    }

    public Class getPropertyType(Object target, String property) throws Exception {
        if (target == null) {
            return null;
        }

        Field field = getFieldFromClass(target.getClass(), property);
        return field.getType();
    }

    public void setPropertyValue(Object target, String property, Object value) throws Exception {

        Field field = getFieldFromClass(target.getClass(), property);

        boolean wasAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            field.set(target, value);
        } finally {
            field.setAccessible(wasAccessible);
        }
    }

    private Field getFieldFromClass(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return findFieldFromAncestors(clazz, fieldName);
        }
    }

    private Field findFieldFromAncestors(Class<?> clazz, String fieldName) {
        Class<?> parent = clazz.getSuperclass();

        if (parent == Object.class) {
            return null;
        }
        return getFieldFromClass(parent, fieldName);
    }
}
