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

    private static interface PersonBuilder extends Builder<Person> {}

    private static interface PersonWithAnotherInterfaceBuilder extends Serializable, Builder<Person> {}
}