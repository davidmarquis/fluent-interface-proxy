## 1 minute primer

Given a Javabean:

```java
public class Person {
    private String name;
    private int age;
    private List<Person> friends;

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setFriends(List<Friend> friends) {
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
    PersonBuilder withFriends(PersonBuilder... friends);
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
                    .withFriends(
                        aPerson().withName("Smitty Smith"),
                        aPerson().withName("Joe Anderson")
                    )
                    .build();
```

Yay! No code!

## The problem

Writing (Fluent Interfaces)[http://en.wikipedia.org/wiki/Fluent_interface] for creating beans in Java is cumbersome.
It requires the programmer to write a lot of boilerplate code to implement a builder for simple javabeans. This project
aims at facilitating the implementation of such patterns by providing an automatic implementation of builder interfaces.

## The solution

This library provides a dynamic implementation of your builder interface that will be able to build the target object using the Reflection API.
The dynamic implementation is in fact a Proxy that intercepts all calls to your interface and stores property values to set when building your final object.

All you need to make sure is that you follow a few conventions when designing your builder interface. Keep reading!

## Features

 # Supports any type of property by simply copying the value passed in the builder to the bean's property.
 # Supports varargs arguments in builders that can be directly copied to an array property on the target bean, or transformed as any Collection.
 # Whenever a Builder is encountered in your Builder interface's methods, this builder will be asked to build the object prior to setting the target bean's property.

## Designing your builder interfaces

 # Any prefix is supported for property-setting methods
 In the example above, `with` is used for all methods, but anything could be used.
 # The property names must match between the builder method and the actual bean property
 For every property-setting method in your builder, there must exist a property that is named exactly the same as what comes after the lower case prefix.
 Ex: `builder.withSomething` -> `bean.setSomething`
 # For multi-valued properties (arrays or collections), you can use varargs in your interface.
 The framework will automatically convert to set the correct value on the target bean.
 # You may use a `Builder` in place of any bean in your builder.
 The Builder's build() method will automatically be called and the resulting bean will be set on the target bean's property.
 # By default, your builder interface should extend the `Builder` interface provided in the framework.
 This builder has a single method: `T build()` If extending this super interface is too invasive (I understand why it would be in some cases),
 you can use your own super interface, but you have to provide custom code to 'plug it in' (an explanation will be added soon).

## Maven dependency

This project is not currently published on the Maven Central repository, so you have to checkout the project locally, build and install the
artifact in your own repository. I do plan to publish the artifact on Maven Central as soon as possible however.



