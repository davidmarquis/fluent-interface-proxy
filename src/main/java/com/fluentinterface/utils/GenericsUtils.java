package com.fluentinterface.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericsUtils {

    /**
     * Finds the generic type declared on a single interface implemented by the provided class.
     * If the generic type is itself a generic type (ex: <pre>{@code SomeType<SomeOtherType<T>>}</pre>,
     * the actual raw type will be returned (in the previous example, `SomeOtherType` would be returned).
     *
     * @param clazz            the class on which we want to find the generic type.
     * @param genericInterface the interface we're looking for.
     * @return the actual type declared on the provided generic interface.
     */
    public static Class<?> getDeclaredGenericType(Class<?> clazz, Class<?> genericInterface) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericType : genericInterfaces) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                if (paramType.getRawType().equals(genericInterface)) {
                    Type type = paramType.getActualTypeArguments()[0];
                    return getActualRawType(type);
                }
            }
        }
        return null;
    }

    /**
     * Java's introspection API differentiates between a raw type and a generic (parameterized) type.
     * To determine the actual raw type from a type, we need to take both cases into consideration.
     */
    private static Class<?> getActualRawType(Type type) {
        Class<?> rawType;
        if (type instanceof ParameterizedType) {
            rawType = (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            rawType = (Class<?>) type;
        }
        return rawType;
    }
}
