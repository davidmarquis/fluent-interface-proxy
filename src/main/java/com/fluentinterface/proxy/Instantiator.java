package com.fluentinterface.proxy;

public interface Instantiator<T> {
    /**
     * Instantiates the target object, optionally using the provided {@link BuilderState} as a source of values for
     * constructor parameters.
     * <p>
     * Note that that implementation should limit its scope to create (i.e.: calling `new`) the target object. The
     * builder will automatically set remaining properties on the target object once instantiated.
     *
     * @param state the current state of the builder, as built with previous builder methods invocations.
     * @return the instantiated object
     * @throws Exception if anything wrong happens when instantiating the class.
     */
    T instantiate(BuilderState state) throws Exception;
}
