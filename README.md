## 1 minute primer

Given a Person bean:

```java
public class Person {
    private String name;
    private int age;
    private Person partner;
    private List<Person> friends;

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setPartner(Person partner) {
        this.partner = partner;
    }

    public void setFriends(List<Person> friends) {
        this.friends = friends;
    }

    ... getters omitted for brevity ...
}
```

And a builder:

```java
public interface PersonBuilder extends Builder<Person> {
    PersonBuilder withName(String name);
    PersonBuilder withAge(int age);
    PersonBuilder withPartner(PersonBuilder partner);
    PersonBuilder havingFriends(PersonBuilder... friends);
}
```

Enjoy an automatic implementation of your builder:

```java
public static PersonBuilder aPerson() {
    return ReflectionBuilder.implementationFor(PersonBuilder.class).create();
}

...

Person person = aPerson()
                    .withName("John Doe")
                    .withAge(44)
                    .withPartner( aPerson().withName("Diane Doe") )
                    .havingFriends(
                        aPerson().withName("Smitty Smith"),
                        aPerson().withName("Joe Anderson")
                    )
                    .build();
```

Yay! No code!

## The problem

Writing [Fluent Interfaces](http://en.wikipedia.org/wiki/Fluent_interface) for creating simple beans in Java is cumbersome.
It requires the developer to write a lot of boilerplate code to implement a simple "set properties" type builder. This small project
aims at facilitating the implementation of such patterns by providing an automatic implementation of these builder interfaces.

## The solution

This library provides a dynamic implementation of your builder interface that will be able to build the target object using the Reflection API.
The dynamic implementation is in fact a Proxy that intercepts all calls to your interface and stores property values to set when comes the time to build your final object.

All you need to make sure is that you follow a few conventions when designing your builder interface. Keep reading!

## Usage

## Compatibility

Starting from version 2, this library is **only compatible with Java 8+**.
Version 1.3.2 was the last version that was compatible with pre Java 8 environments.

### Maven dependency

The project is published to Maven Central. To use it in your Maven project, add this dependency to your `pom.xml`:

``` xml
<dependency>
    <groupId>com.github.davidmarquis</groupId>
    <artifactId>fluent-interface-proxy</artifactId>
    <version>LATEST</version>
</dependency>
```

### Building from sources

You can build from sources using Maven by running:

```
mvn clean package
```

## Features

 * Supports any type of property by simply copying the value passed in the builder to the bean's property.
 * Supports varargs arguments in builder methods, which will be be directly copied to an array property on the target bean, or transformed as any Collection.
 * Supports setting bean values using both public setters and using private fields directly.
 * Supports custom property-setting methods via the `@Sets` annotation.
 * Supports using non-empty constructors on your target beans.
 * Supports transpart conversion between your property-setting method arguments and target properties.
 * Whenever a Builder is encountered in your Builder interface's methods, this builder will be asked to build the object prior to setting the target bean's property value.

## Tips for designing your builder interfaces

 * **Any prefix is supported for property-setting methods**
    In the example above, `with` and `having` are used, but anything else lower case could be used. The name of the target property is always assumed to start from the first uppercase character in the method name.
 * **Property names should match between the builder method and the actual bean property**
    For every property-setting method in your builder, there must exist a property that is named exactly the same as what comes after the lower case prefix.
    Ex: `builder.withSomething -> bean.setSomething`
    For cases when you need to set a different property, use the `@Sets` annotation on the builder method (continue reading for details).
 * **Every property-setting method has to return the builder itself**.
    We're using the Builder pattern, right? It's all about chaining.
 * **For multi-valued properties (arrays or collections), you can use varargs in your interface.**
    The library will automatically convert to set the correct value on the target bean (even collections!).
 * **Arguments of property-setting methods do not need to match the target property's type**
    Best-effort conversion is attempted every time the type of your property-setting method argument does not match the target property's type. See the class `Conversions` for details of the conversions supported out-of-the-box.
 * **You may use a `Builder` in place of any bean in your builder methods.**
    The Builder's `build()` method will automatically be called and the resulting bean will be set on the target bean's property.
 * **Your builder interface should extend the `Builder<T>` interface provided in the library.**
    This interface has a single method: `T build(Object...)`. If extending this interface is too invasive (I understand why it would be in some cases), you can use your own super interface, but you have to provide a custom `BuilderDelegate` to help the library understand what it's building (see below).


## Using your own `build` method

By default, `ReflectionBuilder` assumes that your builder interfaces extend the `Builder` interface provided by the library.

If you want to use your own builder interface (and thus your own `build()` methods), you need to tell the `ReflectionBuilder` when creating the dynamic builder:

``` java
ReflectionBuilder.implementationFor(YourBean.class)
        .withDelegate(new YourBuilderDelegate())
        .create();
```

Have a look at the `BuilderDelegate` interface, as well as the default implementation of this interface `DefaultBuilderDelegate` for more details on what to provide in your own implementation. The abstract base class `AbstractBuilderDelegate` provides an quick and easy starting point to plug your own builder interfaces into the library.


## Choosing between setters or private fields

The library supports both setting the target bean's attributes using public setters or private fields (using the Reflection API).
By default, public setters are used. You may choose to use fields directly using this:

``` java
ReflectionBuilder.implementationFor(YourBuilder.class)
        .usingFieldsDirectly()
        .create();
```

You may also provide your own implementation of the `AttributeAccessStrategy` interface and use it this way:

``` java
ReflectionBuilder.implementationFor(YourBean.class)
        .usingAttributeAccessStrategy(new YourStrategy()) // implements AttributeAccessStrategy interface
        .create();
```


## Defining non-standard property-setting methods using the `@Sets(property=...)` annotation

The library defines a naming convention for property-setting methods. But for more flexibility, it is possible to define your own method names using the `Sets` annotation:

```java
public interface PersonBuilder extends Builder<Person> {
    @Sets(property = "name")
    PersonBuilder named(String name);
    @Sets(property = "age")
    PersonBuilder aged(String age);
    ...
}
```


## Custom value conversions on setters using `@Sets(via=...)`

If you need to do special processing of values passed to your setter methods before setting them on the target bean, you can provide your own conversion function using the `@Sets` annotation:

```java
public interface PersonBuilder extends Builder<Person> {
    @Sets(via = StringToStatus.class)
    PersonBuilder withStatus(String status);

    class StringToStatus implements Function<String, Status> {
        public Status apply(String value) {
            return new Status(value);
        }
    }
}
```

The `Class` passed to the `via` parameter must implement the Java `Function` interface. A new instance of that class will be created every time the property needs to be set and will be called *instead* of the library's default processing - which is to try to convert the source value into the destination type on a best effort basis.

## Using non-empty constructors

Sometimes the beans you are building may have only non-empty constructors available, or you may require the use of a specific constructor when using your dynamic builder.

Example bean with a non-default constructor:

``` java
public class Person {

    private String name;
    private Person spouse;
    private int age;

    public Person(String name, Person spouse) {
        this.name = name;
        this.spouse = spouse;
    }

    public void setAge(int age) { this.age = age; }
}
```

The library supports these non-default constructors using three different mechanisms:


### Option 1: `@Constructs` annotation on builder methods (since 1.2.0)

```java
public interface PersonBuilder extends Builder<Person> {
    @Constructs
    PersonBuilder of(String name, PersonBuilder spouse);
    ...
}
```

Any method annotated with `@Constructs` will end up calling the corresponding constructor (if found) on the built object.


### Option 2: Varargs in `build` method (since 1.2.0)

Using the varargs argument constructor in the provided `Builder` interface:

``` java
PersonBuilder aPerson() {
    return ReflectionBuilder.implementationFor(PersonBuilder.class).create();
}

Person person = aPerson().build("Jeremy", aPerson().withAge(16));
```


As showcased in the above 2 examples, it is very much possible to have builders as parameters and the library will build those builders for you before invoking the class constructors.

The library will check all available constructors on your target bean's class and will find the best matching constructor to use from the types of the parameters you pass to the `build(...)` method or the `@Constructs` annotated method.

There are certain limitations however:

 * Nulls have the effect of being considered as wildcards when the library tries to find a matching constructor. If many constructors match because of that, an error will be thrown because the library cannot know for sure which constructor you intended to use.
 * When no constructor match the signature you provided to either the `build(Object...)` method or the `@Constructs` annotated method, an error will also be thrown.

### Option 3: Using a custom `Instantiator` when configuring the builder (since 2.1.0)

If the above mechanisms do not fit your need, you can take full control of the instantiation process by providing a custom `Instantiator` implementation when creating your reflection builder:

``` java
PersonBuilder aPerson() {
    return ReflectionBuilder.implementationFor(PersonBuilder.class)
        .usingInstantiator((BuilderState state) -> {
            return new Person(
                state.consume("name", String.class).orElseThrow(() -> new IllegalStateException("Name is required")),
                state.consume("spouse", Person.class).orElse(null)
            );
        })
        .create();
}
```

The `Instantiator` interface has a single method `instantiate(BuilderState)` whose sole responsibility is to create the target object using the right constructor and using the passed `BuilderState` instance to obtain values for each constructor parameters (as set by previous invocations of property-setting methods).

If you don't want the properties used during the instantiation process to be used later on when setting properties on the object, it is important that the `consume()` method be used to remove the property from the state after usage.

If on the other hand, you want the builder to continue to consider these properties after instantiation, use the `peek()` method instead.


## Other documentation

Have a look at the tests defined in the `test` folder to see some sample usages of the dynamic builder.
