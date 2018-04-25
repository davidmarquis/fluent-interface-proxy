package com.fluentinterface.convert;

import javax.lang.model.type.NullType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public interface TypeMatcher<T> {
    boolean matches(Type type);

    static TypeMatcher<?> any() {
        return t -> true;
    }

    static TypeMatcher<?> nulls() {
        return t -> t.equals(NullType.class);
    }

    static <T> TypeMatcher<T> is(Class<T> type) {
        return t -> t.equals(type);
    }

    static TypeMatcher<? super Object> is(Type... types) {
        Set<Type> typeSet = new HashSet<>(asList(types));
        return typeSet::contains;
    }

    static <T> TypeMatcher<T> isInstanceOf(Class<? extends T> numberClass) {
        return t -> numberClass.isAssignableFrom((Class<?>) t);
    }
}
