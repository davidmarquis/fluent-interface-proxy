package com.fluentinterface.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericsUtils {

    private GenericsUtils() {}

    public static Class<?> getGenericTypeOf(Class<?> clazz) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        if (genericInterfaces != null && genericInterfaces.length == 1) {
            Type genericType = genericInterfaces[0];
            if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                return (Class<?>) paramType.getActualTypeArguments()[0];
            }
        }
        return null;
    }
}
