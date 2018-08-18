package com.fluentinterface;

import com.fluentinterface.examples.Person;
import org.junit.Test;

import static com.fluentinterface.examples.PersonAnnotatedBuilder.aPerson;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class BuilderWithAnnotationsTest {

    @Test
    public void testCanUseConstructorFromAnnotatedMethod() {

        Person person = aPerson().of("name", 16).build();

        assertThat(person.getName(), is("name"));
        assertThat(person.getAge(), is(16));
    }

    @Test
    public void testAlwaysUsesLastConstructsInvocationParameterToInstantiateObject() {

        Person person = aPerson()
                .of("name", 16)
                .of("last name", 99)
                .build();

        assertThat(person.getName(), is("last name"));
        assertThat(person.getAge(), is(99));
    }

    @Test
    public void testCanUseConverterFromSetsAnnotatedMethod() {

        Person person = aPerson().withAge("16").build();

        assertThat(person.getAge(), is(16));
    }

    @Test
    public void testCanUseBuilderParameterInConstructor() {
        Person person = aPerson().of(
                "Sylvia",
                27,
                aPerson().of("John", 24)
        ).build();

        assertThat(person.getName(), is("Sylvia"));
        assertThat(person.getAge(), is(27));
        assertThat(person.getPartner(), hasProperty("name", is("John")));
    }

    @Test
    public void testCanUseBuilderVarargsInConstructor() {

        Person person = aPerson().havingFriends(
                aPerson().of("John", 24),
                aPerson().of("Nancy", 32)
        ).build();

        assertThat(person.getFriends(), contains(
                hasProperty("name", is("John")),
                hasProperty("name", is("Nancy"))
        ));
    }

    @Test
    public void testCanUsePropertiesCombinedWithVarargsInConstructor() {

        Person person = aPerson().havingNameAndFriends("George",
                aPerson().of("John", 24),
                aPerson().of("Nancy", 32)
        ).build();

        assertThat(person.getName(), is("George"));
        assertThat(person.getFriends(), contains(
                hasProperty("name", is("John")),
                hasProperty("name", is("Nancy"))
        ));
    }
}
