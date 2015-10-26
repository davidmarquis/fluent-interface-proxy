package com.fluentinterface.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericsUtils {

    private GenericsUtils() {}

    /**
     * Finds the generic type declared on a single interface implemented by the provided class.
     * @param clazz the class on which we want to find the generic type.
     * @return the actual type declared on the provided generic interface.
     */
    public static Class<?> getDeclaredGenericType(Class<?> clazz, Class<?> genericInterface) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericType : genericInterfaces) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                if (paramType.getRawType().equals(genericInterface)) {

                    Type type = paramType.getActualTypeArguments()[0];

                    Class<?> inferredClazz;
                    if (type instanceof ParameterizedType) {
                        inferredClazz = (Class<?>) ((ParameterizedType) type).getRawType();
                    } else {
                        inferredClazz = (Class<?>) paramType.getActualTypeArguments()[0];
                    }

                    return inferredClazz;
                }
            }
        }
        return null;
    }
}
