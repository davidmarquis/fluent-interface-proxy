package com.fluentinterface.proxy.impl;

import com.fluentinterface.proxy.Instantiator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.fluentinterface.utils.TypeConversionUtils.translateFromPrimitive;

/**
 * Instantiates objects by finding a constructor on the target Class matching a given set of parameters.
 */
public class BestMatchingConstructor implements Instantiator {

    private Class builtClass;
    private Object[] params;

    public BestMatchingConstructor(Class builtClass, Object[] params) {
        this.builtClass = builtClass;
        this.params = params;
    }

    @Override
    public Object instantiate() throws InstantiationException {
        try {
            Constructor<?> constructor = findMatchingConstructor(params);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance(params);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Constructor findMatchingConstructor(Object[] params) throws NoSuchMethodException {
        if (params == null || params.length == 0) {
            // use default (empty) constructor
            return builtClass.getDeclaredConstructor();
        }

        Class<?>[] paramTypes = extractTypesFromValues(params);

        List<Constructor<?>> candidates = findCandidateConstructors(paramTypes);

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "No constructor found on class [%s] that matches signature (%s)",
                    builtClass, Arrays.toString(paramTypes)));
        } else if (candidates.size() > 1) {
            throw new IllegalArgumentException(String.format(
                    "Found %s constructors matching signature (%s) on class [%s], which is too ambiguous to proceed.",
                    candidates.size(), Arrays.toString(paramTypes), builtClass));
        } else {
            return candidates.get(0);
        }
    }

    private Class<?>[] extractTypesFromValues(Object[] params) {
        Class<?>[] paramTypes = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];

            paramTypes[i] = (param == null) ? null : param.getClass();
        }
        return paramTypes;
    }

    private List<Constructor<?>> findCandidateConstructors(Class<?>[] paramTypes) {
        Constructor<?>[] allConstructors = builtClass.getDeclaredConstructors();
        List<Constructor<?>> candidates = new ArrayList<>();

        for (Constructor<?> constructor : allConstructors) {

            Class<?>[] constructorParamTypes = constructor.getParameterTypes();
            if (constructorParamTypes.length != paramTypes.length) {
                continue;
            }

            if (typesAreCompatible(paramTypes, constructorParamTypes)) {
                candidates.add(constructor);
            }
        }

        return candidates;
    }

    /**
     * Checks if a set of types are compatible with the given set of constructor parameter types. If an input type is
     * null, then it is considered as a wildcard for matching purposes, and always matches.
     * @param paramTypes A set of input types that are to be matched against constructor parameters.
     * @param constructorParamTypes The constructor parameters to match against.
     * @return whether the input types are compatible or not.
     */
    private boolean typesAreCompatible(Class<?>[] paramTypes, Class<?>[] constructorParamTypes) {
        boolean matches = true;
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            if (paramType != null) {

                Class<?> inputParamType = translateFromPrimitive(paramType);
                Class<?> constructorParamType = translateFromPrimitive(constructorParamTypes[i]);

                if (!inputParamType.isAssignableFrom(constructorParamType)) {
                    matches = false;
                    break;
                }
            }
        }
        return matches;
    }
}
