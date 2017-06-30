package com.fluentinterface.proxy.impl;

import com.fluentinterface.proxy.PropertySetter;
import com.fluentinterface.proxy.PropertyTarget;

import java.util.function.Function;

public class TransformPropertySetter implements PropertySetter {

    private String property;
    private Function transformFunction;

    public TransformPropertySetter(String property, Function transformFunction) {
        this.property = property;
        this.transformFunction = transformFunction;
    }

    @Override
    public String getPropertyName() {
        return property;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void apply(PropertyTarget target, Object value) throws Exception {
        Object valueToSet = transformFunction.apply(value);
        target.setProperty(property, valueToSet);
    }
}
