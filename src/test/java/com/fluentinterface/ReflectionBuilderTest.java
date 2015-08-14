package com.fluentinterface;

import com.fluentinterface.builder.Builder;
import com.fluentinterface.domain.Person;
import org.junit.Test;

import java.io.Serializable;

import static com.fluentinterface.ReflectionBuilder.implementationFor;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.junit.Assert.*;

public class ReflectionBuilderTest {

    @Test
    public void shouldImplyBuiltClassFromSingleBuilderInterface() {

        ReflectionBuilder<PersonBuilder> reflectionBuilder = implementationFor(PersonBuilder.class);

        assertThat(reflectionBuilder.getBuiltClass(), typeCompatibleWith(Person.class));
    }

    @Test
    public void shouldImplyBuiltClassFromMultipleBuilderInterface() {

        ReflectionBuilder<PersonWithAnotherInterfaceBuilder> reflectionBuilder = implementationFor(PersonWithAnotherInterfaceBuilder.class);

        assertThat(reflectionBuilder.getBuiltClass(), typeCompatibleWith(Person.class));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotImplyBuiltClassFromNoInterfaceAtAll() {

        ReflectionBuilder<DefinitelyNotABuilder> reflectionBuilder = implementationFor(DefinitelyNotABuilder.class);

        reflectionBuilder.getBuiltClass();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotImplyBuiltClassFromMultipleNonBuilderInterfaces() {

        ReflectionBuilder<NotABuilder> reflectionBuilder = implementationFor(NotABuilder.class);

        reflectionBuilder.getBuiltClass();
    }

    private interface PersonBuilder extends Builder<Person> {}

    private interface PersonWithAnotherInterfaceBuilder extends Serializable, Builder<Person> {}

    private interface NotABuilder extends Serializable, Comparable {}

    private interface DefinitelyNotABuilder {}
}