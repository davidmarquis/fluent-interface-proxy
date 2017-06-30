package com.fluentinterface.proxy;

public interface Instantiator {

    Object instantiate() throws IllegalAccessException, InstantiationException;
}
