package com.fluentinterface;

import com.fluentinterface.domain.Person;
import com.fluentinterface.domain.TypedID;
import com.fluentinterface.domain.TypedIDBuilder;
import org.junit.Test;

import static com.fluentinterface.ReflectionBuilder.implementationFor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypedBuilderProxyTest {

    private TypedIDBuilder<Person> personTypedIDBuilder = implementationFor(TypedIDBuilder.class)
            .usingFieldsDirectly()
            .create();

    @Test
    public void shouldSetPropertyValueString() {

        TypedID<Person> built = personTypedIDBuilder
                .withId("John Smith")
                .build();

        assertThat(built.getId(), is("John Smith"));
    }
}
