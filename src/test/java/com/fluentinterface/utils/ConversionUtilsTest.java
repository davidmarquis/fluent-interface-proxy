package com.fluentinterface.utils;

import org.junit.Test;

import static com.fluentinterface.utils.ConversionUtils.translateFromPrimitive;
import static junit.framework.Assert.assertTrue;

public class ConversionUtilsTest {

    @Test
    public void testTranslateFromPrimitiveSupportedConversions() {

        // not using Hamcrest matchers due to generics explicit typing obscuring test
        assertTrue(translateFromPrimitive(boolean.class).equals(Boolean.class));
        assertTrue(translateFromPrimitive(byte.class).equals(Byte.class));
        assertTrue(translateFromPrimitive(short.class).equals(Short.class));
        assertTrue(translateFromPrimitive(char.class).equals(Character.class));
        assertTrue(translateFromPrimitive(int.class).equals(Integer.class));
        assertTrue(translateFromPrimitive(long.class).equals(Long.class));
        assertTrue(translateFromPrimitive(float.class).equals(Float.class));
        assertTrue(translateFromPrimitive(double.class).equals(Double.class));
    }
}