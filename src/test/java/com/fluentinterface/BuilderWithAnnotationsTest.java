package com.fluentinterface;

import com.fluentinterface.domain.Person;
import org.junit.Test;

import static com.fluentinterface.domain.PersonAnnotatedBuilder.aPerson;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BuilderWithAnnotationsTest {

    @Test
    public void testCanUseConstructorFromAnnotatedMethod() {

        Person built = aPerson().with("name", 16).build();

        assertThat(built.getName(), is("name"));
        assertThat(built.getAge(), is(16));
    }

    @Test
    public void testCanUseConverterFromSetsAnnotatedMethod() {

        Person built = aPerson().withAge("16").build();

        assertThat(built.getAge(), is(16));
    }
}
