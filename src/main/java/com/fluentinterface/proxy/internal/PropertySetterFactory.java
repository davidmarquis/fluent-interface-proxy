package com.fluentinterface.proxy.internal;

import com.fluentinterface.annotation.Sets;
import com.fluentinterface.proxy.BuilderDelegate;
import com.fluentinterface.proxy.PropertyAccessStrategy;
import com.fluentinterface.proxy.PropertySetter;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PropertySetterFactory {
    private static final Pattern BUILDER_METHOD_PROPERTY_PATTERN = Pattern.compile("[a-z]+([A-Z].*)");

    private PropertyAccessStrategy propertyAccessStrategy;
    private Class<?> builtClass;
    private BuilderDelegate builderDelegate;

    PropertySetterFactory(PropertyAccessStrategy propertyAccessStrategy,
                          Class<?> builtClass,
                          BuilderDelegate builderDelegate) {

        this.propertyAccessStrategy = propertyAccessStrategy;
        this.builtClass = builtClass;
        this.builderDelegate = builderDelegate;
    }

    PropertySetter createPropertySetter(Method setterMethod) {
        String propertyName = getPropertyName(setterMethod);

        if (propertyName != null && !propertyName.isEmpty()) {
            Function valueConverter = getValueConverter(setterMethod, propertyName);
            return new TransformPropertySetter(propertyName, valueConverter);
        }

        throw new IllegalStateException(String.format(
                "Method [%s] does not seem to represent a setter for a property", setterMethod.getName()));
    }

    private Function getValueConverter(Method method, String targetProperty) {
        Sets setsAnnotation = method.getAnnotation(Sets.class);
        if (setsAnnotation != null) {
            return createConverterFromAnnotation(setsAnnotation, targetProperty);
        } else {
            return createDefaultConverter(targetProperty);
        }
    }

    private Function createDefaultConverter(String targetProperty) {
        Class<?> targetClass = propertyAccessStrategy.getPropertyType(builtClass, targetProperty);
        return new CoerceValueConverter(targetClass, new BuildWithBuilder(builderDelegate));
    }

    private Function createConverterFromAnnotation(Sets setsAnnotation, String targetProperty) {
        Class<? extends Function> valueConverterClass = setsAnnotation.via();
        if (valueConverterClass.equals(Sets.NotSet.class)) {
            return createDefaultConverter(targetProperty);
        }

        try {
            return valueConverterClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(String.format("Could not instantiate function class %s", valueConverterClass), e);
        }
    }

    private String getPropertyName(Method method) {
        String propertyName = null;

        Sets setsAnnotation = method.getAnnotation(Sets.class);
        if (setsAnnotation != null) {
            propertyName = setsAnnotation.property();
        }

        if (propertyName == null || propertyName.isEmpty()) {
            String methodName = method.getName();
            Matcher propertyNameMatcher = BUILDER_METHOD_PROPERTY_PATTERN.matcher(methodName);

            if (propertyNameMatcher.matches()) {
                propertyName = propertyNameMatcher.group(1);
                if (propertyName != null) {
                    propertyName = uncapitalize(propertyName);
                }
            }
        }
        return propertyName;
    }

    private static String uncapitalize(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        char c[] = source.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}
