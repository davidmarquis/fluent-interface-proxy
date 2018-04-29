package com.fluentinterface.utils;

import org.junit.Test;

import static com.fluentinterface.utils.TypeConversionUtils.translateFromPrimitive;
import static org.hamcrest.MatcherAssert.assertThat;

public class TypeConversionUtilsTest {

    @Test
    public void testTranslateFromPrimitiveSupportedConversions() {
        assertThat("boolean", translateFromPrimitive(boolean.class).equals(Boolean.class));
        assertThat("byte", translateFromPrimitive(byte.class).equals(Byte.class));
        assertThat("short", translateFromPrimitive(short.class).equals(Short.class));
        assertThat("char", translateFromPrimitive(char.class).equals(Character.class));
        assertThat("int", translateFromPrimitive(int.class).equals(Integer.class));
        assertThat("long", translateFromPrimitive(long.class).equals(Long.class));
        assertThat("float", translateFromPrimitive(float.class).equals(Float.class));
        assertThat("double", translateFromPrimitive(double.class).equals(Double.class));
    }
}