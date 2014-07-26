package com.fluentinterface.utils;

public class ConversionUtils {

    private ConversionUtils() {}

    /**
     * From a primitive type class, tries to return the corresponding wrapper class.
     * @param paramType a primitive type (ex: int, short, float, etc.) to convert
     * @return the corresponding wrapper class for the type, or the type itself if not a known primitive.
     */
    public static Class<?> translateFromPrimitive(Class<?> paramType) {

        if (paramType == int.class) {
            return Integer.class;
        } else if (paramType == char.class) {
            return Character.class;
        } else if (paramType == byte.class) {
            return Byte.class;
        } else if (paramType == long.class) {
            return Long.class;
        } else if (paramType == short.class) {
            return Short.class;
        } else if (paramType == boolean.class) {
            return Boolean.class;
        } else if (paramType == double.class) {
            return Double.class;
        } else if (paramType == float.class) {
            return Float.class;
        } else {
            return paramType;
        }
    }
}
