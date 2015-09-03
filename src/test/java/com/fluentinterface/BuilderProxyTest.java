package com.fluentinterface;

import com.fluentinterface.domain.Person;
import com.fluentinterface.domain.PersonBuilder;
import com.fluentinterface.proxy.AttributeAccessStrategy;
import com.fluentinterface.proxy.impl.FieldAttributeAccessStrategy;
import com.fluentinterface.proxy.impl.SetterAttributeAccessStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayDeque;

import static com.fluentinterface.ReflectionBuilder.implementationFor;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

@RunWith(Parameterized.class)
public class BuilderProxyTest {

    @Parameterized.Parameters
    public static Iterable<Object[]> strategies() {
        return asList(
                new Object[] {new FieldAttributeAccessStrategy()},
                new Object[] {new SetterAttributeAccessStrategy()}
        );
    }

    private PersonBuilder personBuilder;

    private AttributeAccessStrategy attributeAccessStrategy;

    public BuilderProxyTest(AttributeAccessStrategy attributeAccessStrategy) {
        this.attributeAccessStrategy = attributeAccessStrategy;
    }

    @Before
    public void setup() throws InstantiationException, IllegalAccessException {
        personBuilder = aPerson();
    }

    private PersonBuilder aPerson() {
        return implementationFor(PersonBuilder.class)
                .usingAttributeAccessStrategy(attributeAccessStrategy)
                .create();
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
    public void shouldSetPropertyValueToLastOneWhenCalledMultipleTimes() {

        Person built = personBuilder
                .withAge(10)
                .withAge(20)
                .build();

        assertThat(built.getAge(), is(20));
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
                .withFriends(asList(
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
                .withParents(asList(
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

        assertThat(built.getAgesOfMarriages(), is(new int[]{23, 45}));
    }

    @Test
    public void shouldSetPropertyValueBuilderToObject() {

        Person built = personBuilder
                .withPartner(
                        aPerson().withName("Diane")
                ).build();

        assertThat(built.getPartner(), notNullValue());
        assertThat(built.getPartner().getName(), is("Diane"));
    }

    @Test
    public void shouldCopyCollectionByReferenceWhenCollectionTypeNotSupported() {

        ArrayDeque queue = new ArrayDeque();
        Person built = personBuilder.withQueue(queue).build();

        assert built.getQueue() == queue;
    }

    @Test
    public void shouldCallSpecificConstructorWhenBuildMethodCalledWithParameters() {

        Person person = personBuilder.build("Jeremy", 3);

        assertThat(person.getName(), is("Jeremy"));
        assertThat(person.getAge(), is(3));
    }

    @Test
    public void shouldCallSpecificConstructorWhenBuildMethodCalledWithParametersWithNullValue() {

        Person person = personBuilder.build("Jeremy", 3, null);

        assertThat(person.getName(), is("Jeremy"));
        assertThat(person.getAge(), is(3));
        assertThat(person.getPartner(), is(nullValue()));
    }

    @Test
    public void shouldUseBuilderWhenPassedInBuildMethodArguments() {

        Person person = personBuilder.build("Jeremy", 3, aPerson().withName("Suzana"));

        assertThat(person.getName(), is("Jeremy"));
        assertThat(person.getAge(), is(3));
        assertThat(person.getPartner().getName(), is("Suzana"));
    }

    @Test
    public void shouldSetPropertyUserSetsAnnotation() {

        Person built = personBuilder
                .named("John Smith")
                .aged(20)
                .build();

        assertThat(built.getName(), is("John Smith"));
        assertThat(built.getAge(), is(20));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenMultipleConstructorsMatchBuildMethodArguments() {

        personBuilder.build(null, 3);
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
}
