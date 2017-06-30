package com.fluentinterface.proxy.impl;

import com.fluentinterface.proxy.Instantiator;

/**
 * Instantiates an object using its default empty constructor.
 */
public class EmptyConstructor implements Instantiator {
    private Class targetClass;

    public EmptyConstructor(Class targetClass) {
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
