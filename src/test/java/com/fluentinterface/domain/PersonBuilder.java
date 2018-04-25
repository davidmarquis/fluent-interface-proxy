package com.fluentinterface.domain;

import com.fluentinterface.annotation.Sets;
import com.fluentinterface.builder.Builder;

import java.util.Collection;
import java.util.Queue;

public interface PersonBuilder extends Builder<Person> {

    @Sets(property = "name")
    PersonBuilder named(String name);

    @Sets(property = "name")
    PersonBuilder unnamed();

    PersonBuilder withName(String name);

    @Sets(property = "age")
    PersonBuilder aged(int age);

    PersonBuilder withAge(int age);

    PersonBuilder forAge(int age);

    PersonBuilder withAge(String age);

    @Sets(property = "age")
    PersonBuilder notYetBorn();

    PersonBuilder withPartner(PersonBuilder diane);

    PersonBuilder withSurnames(String... surnames);

    PersonBuilder withFriends(Person... friends);

    PersonBuilder withFriends(PersonBuilder... friendsBuilders);

    PersonBuilder withFriends(Collection<PersonBuilder> builderCollection);

    PersonBuilder withParents(Collection<Person> builderCollection);

    PersonBuilder withParents(PersonBuilder... parents);

    PersonBuilder withAgesOfMarriages(int... agesOfMarriages);

    /** Setting unknown properties will fail. */
    PersonBuilder withAnUnknownProperty(String value);

    /** Method names must represent property name starting from first uppercase character. */
    PersonBuilder something(String name);

    /** Queues are supported only for direct assignment (target class property must also be a Queue). */
    PersonBuilder withQueue(Queue queue);

    default PersonBuilder withManyValues(String name, int age) {
        return withName(name).withAge(age);
    }
}
