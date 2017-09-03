package com.fluentinterface.domain;

import com.fluentinterface.ReflectionBuilder;
import com.fluentinterface.annotation.Constructs;
import com.fluentinterface.annotation.Sets;
import com.fluentinterface.builder.Builder;

import java.util.function.Function;

import static java.lang.Integer.valueOf;

public interface PersonAnnotatedBuilder extends Builder<Person> {

    @Constructs
    PersonAnnotatedBuilder of(String name, int age);

    @Constructs
    PersonAnnotatedBuilder of(String name, int age, PersonAnnotatedBuilder partner);

    @Constructs
    PersonAnnotatedBuilder havingNameAndFriends(String name, PersonAnnotatedBuilder... friends);

    @Constructs
    PersonAnnotatedBuilder havingFriends(PersonAnnotatedBuilder... friends);

    @Sets(via = StringToInteger.class)
    PersonAnnotatedBuilder withAge(String age);

    static PersonAnnotatedBuilder aPerson() {
        return ReflectionBuilder.implementationFor(PersonAnnotatedBuilder.class).create();
    }

    class StringToInteger implements Function<String, Integer> {
        public Integer apply(String s) {
            return valueOf(s);
        }
    }
}
