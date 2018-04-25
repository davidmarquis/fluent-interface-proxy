package com.fluentinterface.convert;

import org.junit.Test;

import javax.lang.model.type.NullType;

import static com.fluentinterface.convert.TypeMatcher.is;
import static com.fluentinterface.convert.TypeMatcher.isInstanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TypeMatcherTest {

    @Test
    public void matchesNulls() {
        TypeMatcher matcher = TypeMatcher.nulls();

        assertTrue("null == null", matcher.matches(NullType.class));
        assertFalse("String != null", matcher.matches(String.class));
    }

    @Test
    public void matchesSingleType() {
        TypeMatcher matcher = is(Integer.class);

        assertTrue("Integer == Integer", matcher.matches(Integer.class));
        assertFalse("Number != Integer", matcher.matches(Number.class));
    }

    @Test
    public void matchesSpecificTypes() {
        TypeMatcher matcher = TypeMatcher.is(Integer.class, Long.class, int.class);

        assertTrue("Integer IN {Integer,Long,int}", matcher.matches(Integer.class));
        assertTrue("int IN {Integer,Long,int}", matcher.matches(Long.class));
        assertFalse("Double NOT IN {Integer,Long,int}", matcher.matches(Double.class));
    }

    @Test
    public void matchesTypeHierarchy() {
        TypeMatcher matcher = isInstanceOf(Number.class);

        assertTrue("Number instanceof Number", matcher.matches(Number.class));
        assertTrue("Integer instanceof Number", matcher.matches(Integer.class));
        assertFalse("String not instanceof Number", matcher.matches(String.class));
    }
}