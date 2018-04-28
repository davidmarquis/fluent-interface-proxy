package com.fluentinterface.proxy.internal;

import com.fluentinterface.proxy.PropertyAccessStrategy;

import java.lang.reflect.Field;

/**
 * Strategy that sets the target bean's attributes directly using the Reflection API (without going through the setters).
 * It automatically goes up the class hierarchy to find fields in inherited classes as well.
 */
public class FieldPropertyAccessStrategy implements PropertyAccessStrategy {

    public boolean hasProperty(Class<?> builtClass, String property) {

        Field field = getFieldFromClass(builtClass, property);

        return field != null;
    }

    public Class getPropertyType(Class targetClass, String property) {
        if (targetClass == null) {
            return null;
        }

        Field field = getFieldFromClass(targetClass, property);
        if (field == null) {
            throw new IllegalStateException(String.format("No property named '%s' was found on class %s", property, targetClass));
        }
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

    protected Field getFieldFromClass(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return findFieldFromAncestors(clazz, fieldName);
        }
    }

    protected Field findFieldFromAncestors(Class<?> clazz, String fieldName) {
        Class<?> parent = clazz.getSuperclass();

        if (parent == Object.class) {
            return null;
        }
        return getFieldFromClass(parent, fieldName);
    }
}
