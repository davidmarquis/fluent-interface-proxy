package com.fluentinterface;

import com.fluentinterface.domain.Person;
import com.fluentinterface.domain.TypedStringID;
import com.fluentinterface.domain.TypedStringIDBuilder;
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
