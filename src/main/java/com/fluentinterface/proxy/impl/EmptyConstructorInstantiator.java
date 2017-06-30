package com.fluentinterface.proxy.impl;

import com.fluentinterface.proxy.Instantiator;

/**
 * Instantiates an object using its default empty constructor.
 */
public class EmptyConstructorInstantiator implements Instantiator {
    private Class targetClass;

    public EmptyConstructorInstantiator(Class targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public Object instantiate() throws InstantiationException {
        try {
            return targetClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
