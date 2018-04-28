package com.fluentinterface.proxy;

import java.util.Optional;

public interface BuilderState {
    /**
     * Converts an input value to a target type, using the builder's configured conversions.
     * If the input value is a builder, a collection of builders or an array of builders, they are all built before
     * conversion.
     *
     * @param value      input value to coerce from.
     * @param targetType the target type to coerce the input value to.
     * @return the coerced value.
     */
    Object coerce(Object value, Class<?> targetType);

    /**
     * Checks if a set of properties were set on the builder.
     *
     * @param properties one or more properties to check.
     * @return whether all of those properties were set or not.
     */
    boolean hasValueFor(String... properties);

    /**
     * Returns the value of a property as set with any of the builder methods invocation.
     * If a conversion is required (either implicit conversion or a conversion configured with `@Sets` annotation), that
     * conversion will be performed prior to returning the value.
     *
     * @param property name of the property to fetch value of.
     * @param type     the property type.
     * @param <P>      the property type.
     * @return the property value, as provided using a previous builder method invocation.
     */
    <P> Optional<P> peek(String property, Class<P> type);

    /**
     * Returns the value of a property as set with any of the builder methods invocation, and removes the property
     * from the state as to not further use it for building the object. This is intended to be used for passing specific
     * properties values to constructors for initial instantiation.
     * If a conversion is required (either implicit conversion or a conversion configured with `@Sets` annotation), that
     * conversion will be performed prior to returning the value.
     *
     * @param property name of the property to fetch value of.
     * @param type     the property type.
     * @param <P>      the property type.
     * @return the property value, as provided using a previous builder method invocation.
     */
    <P> Optional<P> consume(String property, Class<P> type);
}
