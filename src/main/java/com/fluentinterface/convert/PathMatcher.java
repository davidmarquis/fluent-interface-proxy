package com.fluentinterface.convert;

import java.lang.reflect.Type;

public interface PathMatcher<S, T> {
    boolean matches(Type sourceType, Type targetType);

    static <S, T> PathMatcher<S, T> match(TypeMatcher<S> source, TypeMatcher<T> target) {
        return (sourceType, targetType) -> source.matches(sourceType) && target.matches(targetType);
    }
}
