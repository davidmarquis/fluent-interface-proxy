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

## Features

 * Supports any type of property by simply copying the value passed in the builder to the bean's property.
 * Supports varargs arguments in builders that can be directly copied to an array property on the target bean, or transformed as any Collection.
 * Supports setting bean values using both public setters and using private fields directly.
 * Supports using non-empty constructors on your beans
 * Whenever a Builder is encountered in your Builder interface's methods, this builder will be asked to build the object prior to setting the target bean's property value.

## Tips for designing your builder interfaces

 * **Any prefix is supported for property-setting methods**
    In the example above, `with` and `having` are used, but anything else lower case could be used.
 * **The property names must match between the builder method and the actual bean property**
    For every property-setting method in your builder, there must exist a property that is named exactly the same as what comes after the lower case prefix.
    Ex: `builder.withSomething` -> `bean.setSomething`
 * **Every property-setting method has to return the builder itself**.
    We're using the Builder pattern, eh?
 * **For multi-valued properties (arrays or collections), you can use varargs in your interface.**
    The framework will automatically convert to set the correct value on the target bean (even collections!).
 * **You may use a `Builder` in place of any bean in your builder methods.**
    The Builder's `build()` method will automatically be called and the resulting bean will be set on the target bean's property.
 * **By default, your builder interface should extend the `Builder<T>` interface provided in the framework.**
    This interface has a single method: `T build(Object...)`. If extending this interface is too invasive (I understand why it would be in some cases),
    you can use your own super interface, but you have to provide custom code to 'plug it in' (see below).

## Using your own `build` method

By default, `ReflectionBuilder` assumes that your builder interfaces extend the `Builder` interface provided by the library.

If you want to use your own builder interface (and thus your own `build()` methods), you need to tell the `ReflectionBuilder` when creating the
dynamic builder:

``` java
ReflectionBuilder.implementationFor(YourBean.class)
        .withDelegate(new YourBuilderDelegate())
        .create();
```

Have a look at the `BuilderDelegate` interface, as well as the default implementation of this interface in `ReflectionBuilder` for more
details on what to provide in your implementation.


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
AttributeAccessStrategy yourStrategy = new YourStrategy();  // implements AttributeAccessStrategy interface

ReflectionBuilder.implementationFor(YourBean.class)
        .usingAttributeAccessStrategy(yourStrategy)
        .create();
```

## Using non-empty constructors

Sometimes the beans you are building may have only non-empty constructors available, or you may require the use of a specific constructor
when using your dynamic builder. Since version 1.2.0, the library supports calling specific constructors using the build() method:

Your bean:

``` java
public class Person {

    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

Using the multi argument constructor using the dynamic builder:

``` java
PersonBuilder aPerson = ReflectionBuilder.implementationFor(PersonBuilder.class).create();

Person person = aPerson.build("Jeremy", 25);
```

The library will check all available constructors on your target bean's class and will find the right constructor to use from the types
 of the parameters you pass to the `build(Object...)` method.

There are limitations however:

 * Nulls have the effect of being considered as wildcards when the library tries to find a matching constructor. If many
 constructors match because of that, an error will be thrown because the library cannot know for sure which constructor you intended to use.
 * When no constructor match the signature you provided to the `build(Object...)` method, an error will also be thrown.

## Other documentation

Have a look at the tests defined in the `test` folder to see some sample usages of the dynamic builder.

## Using the code

### Maven dependency

The project is published to the Maven Central repository. To use it in your Maven project, add this dependency to your `pom.xml`:

``` xml
<dependency>
    <groupId>com.github.davidmarquis</groupId>
    <artifactId>fluent-interface-proxy</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Building from sources

You can build from sources using Maven by running:

```
mvn clean package
```

## Future features (wishlist)

Here are some features I'd like to eventually add to the project:

 * Support for conversions: `aPerson().withAge("45").build()`
    Allows for use cases where inputs as strings are used directly (I am mostly thinking of functional testing frameworks
    like Fitnesse (fixtures) or Cucumber)
 * Support for specifying custom handlers for builder methods. Could allow: `aPerson().named("Joe").aged(45).build()`
 * Configuration of the non-standard builder methods through annotations. Something like:

```java
public interface PersonBuilder extends Builder<Person> {
    @Sets(property = "name")
    PersonBuilder named(String name);
    @Sets(property = "age", convertTo = int.class)
    PersonBuilder aged(String age);
    ...
}
```

