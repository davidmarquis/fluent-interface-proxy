package com.fluentinterface.examples;

/**
 * TypedStringID is used to assure proper setting of values at compilation time.
 *
 * For example, consider the following class:
 *
 * public class ABoyAndHisDog {
 *
 *     private String boyId;
 *
 *     private String dogId;
 *
 *     ... setters and getters ...
 *
 * }
 *
 * In this case it is easy to mistake between the dogId and the boyId when setting them.
 * The error will only present itself at runtime.
 *
 * Now using TypedStringID instead:
 *
 * public class ABoyAndHisDog {
 *
 *     private TypedStringID<Boy> boyId;
 *
 *     private TypedStringID<Dog> dogId;
 *
 *     ... setters and getters ...
 *
 * }
 *
 * In this case the compiler will emit an error when trying to set the boyId to the dogId
 * and vice versa. In addition, this serves as a hint to the IDE's auto completion.
 */
public class TypedStringID<T> {

    private String id;

    public TypedStringID() {
    }

    public TypedStringID(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
