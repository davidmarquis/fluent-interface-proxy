package com.fluentinterface.builder;

public interface Builder<T> {
    /**
     * Creates an object instance of the target class being built from the state that was set using the builder's methods.
     * @param constructorArgs (optional) when specified, a matching constructor will be looked up on the target class and
     *                        this constructor (if found) will be used to instantiate your object.
     * @return an object instance initialized as the builder was used.
     */
    T build(Object... constructorArgs);
}
