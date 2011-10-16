package com.fluentinterface;

import com.fluentinterface.domain.Person;
import com.fluentinterface.domain.PersonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayDeque;
import java.util.Arrays;

import static com.fluentinterface.ReflectionBuilder.implementationFor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BuilderProxyTest {

    private PersonBuilder personBuilder;

    @Before
    public void setup() throws InstantiationException, IllegalAccessException {
        personBuilder = aPerson();
    }

    private PersonBuilder aPerson() {
        return implementationFor(PersonBuilder.class).create();
    }

    @Test
    public void shouldSupportAnyMethodNamedAfterTargetPropertyName() {
        Person built = personBuilder
                .forAge(10)
                .build();

        assertThat(built.getAge(), is(10));

        built = personBuilder
                .withAge(10)
                .build();

        assertThat(built.getAge(), is(10));
    }

    @Test
    public void shouldSetPropertyValueString() {

        Person built = personBuilder
                .withName("John Smith")
                .build();

        assertThat(built.getName(), is("John Smith"));
    }

    @Test
    public void shouldSetPropertyValuePrimitive() {

        Person built = personBuilder
                .withAge(10)
                .build();

        assertThat(built.getAge(), is(10));
    }

    @Test
    public void shouldSetPropertyValueArrayToList() {


        Person built = personBuilder
                .withFriends(
                        aPerson().withName("John").build(),
                        aPerson().withName("Diane").build())
                .build();

        assertThat(built.getFriends().size(), is(2));
        assertThat(built.getFriends().get(0).getName(), is("John"));
        assertThat(built.getFriends().get(1).getName(), is("Diane"));
    }

    @Test
    public void shouldSetPropertyValueArrayOfBuildersToCollection() {

        Person built = personBuilder
                .withFriends(
                        aPerson().withName("Joe"),
                        aPerson().withName("Blow"))
                .build();

        assertThat(built.getFriends().size(), is(2));
        assertThat(built.getFriends().get(0).getName(), is("Joe"));
        assertThat(built.getFriends().get(1).getName(), is("Blow"));
    }

    @Test
    public void shouldSetPropertyValueArrayOfBuildersToArray() {

        Person built = personBuilder
                .withParents(
                        aPerson().withName("Mommy"),
                        aPerson().withName("Daddy"))
                .build();

        assertThat(built.getParents().length, is(2));
        assertThat(built.getParents()[0].getName(), is("Mommy"));
        assertThat(built.getParents()[1].getName(), is("Daddy"));
    }

    @Test
    public void shouldSetPropertyValueCollectionOfBuildersToCollection() {
        Person built = personBuilder
                .withFriends(Arrays.asList(
                        aPerson().withName("Joe"),
                        aPerson().withName("Blow")
                ))
                .build();

        assertThat(built.getFriends().size(), is(2));
        assertThat(built.getFriends().get(0).getName(), is("Joe"));
        assertThat(built.getFriends().get(1).getName(), is("Blow"));
    }

    @Test
    public void shouldSetPropertyValueCollectionToCollection() {

        Person built = personBuilder
                .withParents(Arrays.asList(
                        aPerson().withName("Mommy").build(),
                        aPerson().withName("Daddy").build()))
                .build();

        assertThat(built.getParents().length, is(2));
        assertThat(built.getParents()[0].getName(), is("Mommy"));
        assertThat(built.getParents()[1].getName(), is("Daddy"));
    }

    @Test
    public void shouldSetPropertyValueArrayToSet() {

        Person built = personBuilder
                .withSurnames("Bill", "William", "Guillaume")
                .build();

        assertThat(built.getSurnames().size(), is(3));
        assertThat(built.getSurnames(), hasItem("Bill"));
        assertThat(built.getSurnames(), hasItem("William"));
        assertThat(built.getSurnames(), hasItem("Guillaume"));
    }

    @Test
    public void shouldSetPropertyValueArrayOfPrimitivesToArrayOfPrimitives() {

        Person built = personBuilder
                .withAgesOfMarriages(23, 45)
                .build();

        assertThat(built.getAgesOfMarriages(), is(new int[] {23, 45}));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenBuilderUsesAnUnknownProperty() {

        personBuilder.withAnUnknownProperty("fails").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenBuilderUsesAnIncompatiblePropertyType() {

        personBuilder.withAge("fails").build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenBuilderMethodDoesNotContainPropertyName() {

        personBuilder.something("fails").build();
    }

    @Test
    public void shouldFailForUnsupportedCollectionTypes() {

        personBuilder.withQueue(new ArrayDeque()).build();
    }
}
