package com.fluentinterface;

import com.fluentinterface.examples.Person;
import com.fluentinterface.examples.PersonBuilder;
import com.fluentinterface.proxy.PropertyAccessStrategy;
import com.fluentinterface.proxy.internal.FieldPropertyAccessStrategy;
import com.fluentinterface.proxy.internal.SetterPropertyAccessStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayDeque;

import static com.fluentinterface.ReflectionBuilder.implementationFor;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class BuilderProxyTest {

    @Parameterized.Parameters
    public static Iterable<Object[]> strategies() {
        return asList(
                new Object[] {new FieldPropertyAccessStrategy()},
                new Object[] {new SetterPropertyAccessStrategy()}
        );
    }

    private PropertyAccessStrategy propertyAccessStrategy;

    public BuilderProxyTest(PropertyAccessStrategy propertyAccessStrategy) {
        this.propertyAccessStrategy = propertyAccessStrategy;
    }

    private PersonBuilder aPerson() {
        return implementationFor(PersonBuilder.class)
                .usingAttributeAccessStrategy(propertyAccessStrategy)
                .create();
    }

    @Test
    public void shouldSupportAnyMethodNamedAfterTargetPropertyName() {
        Person built = aPerson()
                .forAge(10)
                .build();

        assertThat(built.getAge(), is(10));

        built = aPerson()
                .withAge(10)
                .build();

        assertThat(built.getAge(), is(10));
    }

    @Test
    public void shouldSupportSetterMethodsWithNoArguments() {
        Person built = aPerson()
                .unnamed()
                .build();

        assertThat(built.getName(), is(nullValue()));

        built = aPerson()
                .notYetBorn()
                .build();

        assertThat(built.getAge(), is(0));
    }

    @Test
    public void shouldSetPropertyValueString() {
        Person built = aPerson()
                .withName("John Smith")
                .build();

        assertThat(built.getName(), is("John Smith"));
    }

    @Test
    public void shouldSetPropertyValuePrimitive() {
        Person built = aPerson()
                .withAge(10)
                .build();

        assertThat(built.getAge(), is(10));
    }

    @Test
    public void shouldSetPropertyValueToLastOneWhenCalledMultipleTimes() {
        Person built = aPerson()
                .withAge(10)
                .withAge(20)
                .build();

        assertThat(built.getAge(), is(20));
    }

    @Test
    public void shouldSetPropertyValueArrayToList() {
        Person built = aPerson()
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
        Person built = aPerson()
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
        Person built = aPerson()
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
        Person built = aPerson()
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
        Person built = aPerson()
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
        Person built = aPerson()
                .withSurnames("Bill", "William", "Guillaume")
                .build();

        assertThat(built.getSurnames().size(), is(3));
        assertThat(built.getSurnames(), hasItem("Bill"));
        assertThat(built.getSurnames(), hasItem("William"));
        assertThat(built.getSurnames(), hasItem("Guillaume"));
    }

    @Test
    public void shouldSetPropertyValueArrayOfPrimitivesToArrayOfPrimitives() {
        Person built = aPerson()
                .withAgesOfMarriages(23, 45)
                .build();

        assertThat(built.getAgesOfMarriages(), is(new int[]{23, 45}));
    }

    @Test
    public void shouldSetPropertyValueBuilderToObject() {
        Person built = aPerson()
                .withPartner(
                        aPerson().withName("Diane")
                ).build();

        assertThat(built.getPartner(), notNullValue());
        assertThat(built.getPartner().getName(), is("Diane"));
    }

    @Test
    public void shouldCopyCollectionByReferenceWhenCollectionTypeNotSupported() {
        ArrayDeque queue = new ArrayDeque();
        Person built = aPerson().withQueue(queue).build();

        assert built.getQueue() == queue;
    }

    @Test
    public void shouldCallSpecificConstructorWhenBuildMethodCalledWithParameters() {
        Person person = aPerson().build("Jeremy", 3);

        assertThat(person.getName(), is("Jeremy"));
        assertThat(person.getAge(), is(3));
    }

    @Test
    public void shouldCallSpecificConstructorWhenBuildMethodCalledWithParametersWithNullValue() {
        Person person = aPerson().build("Jeremy", 3, null);

        assertThat(person.getName(), is("Jeremy"));
        assertThat(person.getAge(), is(3));
        assertThat(person.getPartner(), is(nullValue()));
    }

    @Test
    public void shouldUseBuilderWhenPassedInBuildMethodArguments() {
        Person person = aPerson().build("Jeremy", 3, aPerson().withName("Suzana"));

        assertThat(person.getName(), is("Jeremy"));
        assertThat(person.getAge(), is(3));
        assertThat(person.getPartner().getName(), is("Suzana"));
    }

    @Test
    public void shouldSetPropertyUserSetsAnnotation() {
        Person built = aPerson()
                .named("John Smith")
                .aged(20)
                .build();

        assertThat(built.getName(), is("John Smith"));
        assertThat(built.getAge(), is(20));
    }

    @Test
    public void shouldConvertValueToTargetTypeWhenPossible() {
        Person built = aPerson().withAge("16").build();

        assertThat(built.getAge(), is(16));
    }

    @Test
    public void shouldPassthroughDefaultMethodsDefinedOnBuilderInterface() {
        Person built = aPerson().withManyValues("John Doe", 18).build();

        assertThat(built.getName(), is("John Doe"));
        assertThat(built.getAge(), is(18));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldFailWhenConversionToTargetTypeFails() {
        aPerson().withAge("invalid int").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenMultipleConstructorsMatchBuildMethodArguments() {
        aPerson().build(null, 3);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenBuilderUsesAnUnknownProperty() {
        aPerson().withAnUnknownProperty("fails").build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenBuilderMethodDoesNotContainPropertyName() {
        aPerson().something("fails").build();
    }
}
