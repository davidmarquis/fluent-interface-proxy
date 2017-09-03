package com.fluentinterface.proxy.impl;

import com.fluentinterface.proxy.BuilderState;
import com.fluentinterface.proxy.Instantiator;

/**
 * Instantiates an object using its default empty constructor.
 */
public class EmptyConstructor<T> implements Instantiator<T> {
    private Class<T> targetClass;

    public EmptyConstructor(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public T instantiate(BuilderState state) throws InstantiationException {
        try {
            return targetClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
