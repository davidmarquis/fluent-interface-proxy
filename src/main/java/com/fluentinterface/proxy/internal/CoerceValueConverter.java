package com.fluentinterface.proxy.internal;

import com.fluentinterface.convert.Converter;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

/**
 * This is the default conversion function that is applied to setter arguments in builders when not specified with
 * `@Sets(using=Class)` annotation. It attempts to coerce the input value to sensitive values on a best effort basis.
 */
class CoerceValueConverter implements Function {

    private final Converter converter;
    private Class targetType;
    private final Function<Object, Object> next;

    CoerceValueConverter(Class targetType, Function<Object, Object> next) {
        this.targetType = targetType;
        this.next = next;
        this.converter = new Converter();
    }

    @SuppressWarnings("unchecked")
    public Object apply(Object value) {
        if (targetType == null) {
            return value;
        }

        try {
            if (value != null) {
                Collection<Object> valueAsCollection = convertToCollectionIfMultiValued(value);

                if (valueAsCollection != null) {
                    valueAsCollection = applyNextFunctionToElements(valueAsCollection);
                    value = transformCollectionToTargetTypeIfPossible(value, valueAsCollection, targetType);

                    return value;
                } else {
                    value = next.apply(value);
                    value = convert(value);
                }
            } else {
                value = convert(null);
            }
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }

        return value;
    }

    private Object convert(Object value) {
        return converter.convert(value, targetType);
    }

    private Object transformCollectionToTargetTypeIfPossible(Object originalValue, Collection<Object> valueAsCollection,
                                                             Class targetPropertyType) throws InstantiationException, IllegalAccessException {

        if (targetPropertyType.isArray()) {
            return collectionToArray(valueAsCollection, targetPropertyType);
        }

        Collection<Object> targetValue = createCollectionOfType(targetPropertyType);
        if (targetValue != null) {
            targetValue.addAll(valueAsCollection);
            return targetValue;
        }

        return originalValue;
    }

    private Collection<Object> applyNextFunctionToElements(Collection<Object> collectionWithBuilders) {
        Collection<Object> transformed = new ArrayList<>(collectionWithBuilders.size());

        for (Object element : collectionWithBuilders) {
            element = next.apply(element);
            transformed.add(element);
        }

        return transformed;
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> convertToCollectionIfMultiValued(Object value) {
        Class valueClass = value.getClass();
        Collection<Object> valueAsCollection = null;

        if (valueClass.isArray()) {
            valueAsCollection = arrayToCollection(value);
        } else if (isCollection(valueClass)) {
            valueAsCollection = (Collection) value;
        }

        return valueAsCollection;
    }

    private Object collectionToArray(Collection<Object> valueAsCollection, Class targetPropertyType) {
        Class arrayElementsType = targetPropertyType.getComponentType();
        int arraySize = valueAsCollection.size();

        Object createdArray = Array.newInstance(arrayElementsType, arraySize);
        int idx = 0;
        for (Object arrayElement : valueAsCollection) {
            Array.set(createdArray, idx++, arrayElement);
        }

        return createdArray;
    }

    private Collection<Object> arrayToCollection(Object array) {
        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException(String.format("[%s] is not an array.", array));
        }

        int arrayLength = Array.getLength(array);
        List<Object> converted = new ArrayList<>(arrayLength);
        for (int i = 0; i < arrayLength; i++) {
            Object currentElement = Array.get(array, i);
            converted.add(currentElement);
        }

        return converted;
    }

    private boolean isCollection(Class clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> createCollectionOfType(Class clazz) throws IllegalAccessException, InstantiationException {
        if (!isCollection(clazz)) {
            throw new IllegalArgumentException(String.format("Class [%s] is not a collection.", clazz));
        }

        if (clazz.isInterface()) {
            if (SortedSet.class.isAssignableFrom(clazz)) {
                return new TreeSet<>();
            } else if (Set.class.isAssignableFrom(clazz)) {
                return new HashSet<>();
            } else if (List.class.isAssignableFrom(clazz)) {
                return new ArrayList<>();
            }

            return null;
        }

        return (Collection<Object>) clazz.newInstance();
    }
}
