package com.fluentinterface.proxy;

public interface Instantiator<T> {
    /**
     * Instantiate the target object, optionally using the provided BuilderState as a source of values for constructor
     * parameters.
     * Note that that implementation should limit its scope to create (i.e.: calling `new`) the target object. The
     * builder will automatically set remaining properties on the target object once instantiated.
     * @param state the current state of the builder, as built with builder method invocations.
     * @return the instantiated object
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    T instantiate(BuilderState state) throws IllegalAccessException, InstantiationException;
}
