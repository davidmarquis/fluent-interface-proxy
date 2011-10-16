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

By using the Java Reflection API, this library will provide
a dynamic implementation of you builder interface that will be able to build the target object.

All you need to make sure is that you follow a few conventions when designing your builder interface. Keep reading!

