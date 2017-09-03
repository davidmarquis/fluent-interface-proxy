package com.fluentinterface.proxy;

public interface Instantiator<T> {

    T instantiate(BuilderState state) throws IllegalAccessException, InstantiationException;
}
