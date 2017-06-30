package com.fluentinterface.domain;

import com.fluentinterface.ReflectionBuilder;
import com.fluentinterface.annotation.Constructs;
import com.fluentinterface.annotation.Sets;
import com.fluentinterface.builder.Builder;

import java.util.function.Function;

import static java.lang.Integer.valueOf;

public interface PersonAnnotatedBuilder extends Builder<Person> {

    @Constructs
    PersonAnnotatedBuilder with(String name, int age);

    @Sets(via = StringToInteger.class)
    PersonAnnotatedBuilder withAge(String age);

    class StringToInteger implements Function<String, Integer> {
        public Integer apply(String s) {
            return valueOf(s);
        }
    }

    static PersonAnnotatedBuilder aPerson() {
        return ReflectionBuilder.implementationFor(PersonAnnotatedBuilder.class).create();
    }
}
