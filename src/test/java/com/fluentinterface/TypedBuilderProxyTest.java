package com.fluentinterface;

import com.fluentinterface.examples.Person;
import com.fluentinterface.examples.TypedStringID;
import com.fluentinterface.examples.TypedStringIDBuilder;
import org.junit.Test;

import static com.fluentinterface.ReflectionBuilder.implementationFor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypedBuilderProxyTest {

    private TypedStringIDBuilder<Person> personTypedStringIDBuilder = implementationFor(TypedStringIDBuilder.class)
            .usingFieldsDirectly()
            .create();

    @Test
    public void shouldSetPropertyValueString() {

        TypedStringID<Person> built = personTypedStringIDBuilder
                .withId("John Smith")
                .build();

        assertThat(built.getId(), is("John Smith"));
    }
}
