package com.fluentinterface;

import com.fluentinterface.builder.BuilderDelegate;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuilderProxy implements InvocationHandler {

    private static final Pattern BUILDER_METHOD_PROPERTY_PATTERN = Pattern.compile("[a-z]+([A-Z].*)");

    private Class proxied;
    private Class  builtClass;
    private BuilderDelegate builderDelegate;
    private Map<String, Object> propertiesToSet;

    BuilderProxy(Class builderInterface, Class builtClass, BuilderDelegate builderDelegate) {
        this.proxied = builderInterface;
        this.builtClass = builtClass;
        this.builderDelegate = builderDelegate;
        this.propertiesToSet = new LinkedHashMap<String, Object>();
    }

    public Object invoke(Object target, Method method, Object[] params) throws Throwable {

        if (isSetter(method)) {
            String propertyBeingSet = extractPropertyNameFrom(method);
            Object valueForProperty = params[0];

            if (!hasProperty(builtClass, propertyBeingSet)) {
                throw new IllegalStateException(String.format(
                        "Method [%s] on [%s] corresponds to unknown property [%s] on built class [%s]",
                        method.getName(), proxied, propertyBeingSet, builtClass)
                );
            }

            propertiesToSet.put(propertyBeingSet, valueForProperty);

            return target;
        } else if (isBuild(method)) {
            return createInstanceFromProperties();
        }

        throw new IllegalStateException("Unrecognized builder method: " + method);
    }

    private boolean hasProperty(Class<?> builtClass, String propertyName) {
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(builtClass);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    private Object createInstanceFromProperties() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Object instance = builtClass.newInstance();

        for (Map.Entry<String, Object> entry : propertiesToSet.entrySet()) {
            String property = entry.getKey();
            Object value = entry.getValue();

            setTargetProperty(instance, property, value);
        }

        return instance;
    }

    private void setTargetProperty(Object target, String property, Object value) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Class targetPropertyType = PropertyUtils.getPropertyType(target, property);

        if (value != null) {
            Collection<Object> valueAsCollection = convertToCollectionIfMultiValued(value);

            if (valueAsCollection != null) {
                valueAsCollection = buildBuildersInCollection(valueAsCollection);
                value = transformCollectionToTargetTypeIfPossible(value, valueAsCollection, targetPropertyType);
            } else {
                value = buildIfBuilderInstance(value);
            }
        }

        PropertyUtils.setProperty(target, property, value);
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

    private Collection<Object> buildBuildersInCollection(Collection<Object> collectionWithBuilders) {
        if (!hasBuilderDelegate()) {
            return collectionWithBuilders;
        }

        Collection<Object> transformed = new ArrayList<Object>(collectionWithBuilders.size());

        for (Object element : collectionWithBuilders) {
            element = buildIfBuilderInstance(element);
            transformed.add(element);
        }

        return transformed;
    }

    private boolean hasBuilderDelegate() {
        return (builderDelegate != null);
    }

    private Object buildIfBuilderInstance(Object value) {
        if (!hasBuilderDelegate()) {
            return value;
        }

        if (builderDelegate.isBuilderInstance(value)) {
            return builderDelegate.build(value);
        }

        return value;
    }

    private Collection<Object> convertToCollectionIfMultiValued(Object value) {
        Class valueClass = value.getClass();
        Collection<Object> valueAsCollection = null;

        if (valueClass.isArray()) {
            valueAsCollection = arrayToCollection(value);
        }

        if (isCollection(valueClass)) {
            valueAsCollection = (Collection) value;
        }

        return valueAsCollection;
    }

    private Collection<Object> arrayToCollection(Object array) {
        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException(String.format("[%s] is not an array.", array));
        }

        int arrayLength = Array.getLength(array);
        List<Object> converted = new ArrayList<Object>(arrayLength);
        for (int i = 0; i < arrayLength; i++) {
            Object currentElement = Array.get(array, i);
            converted.add(currentElement);
        }

        return converted;
    }

    private boolean isCollection(Class clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    private Collection<Object> createCollectionOfType(Class clazz) throws IllegalAccessException, InstantiationException {
        if (!isCollection(clazz)) {
            throw new IllegalArgumentException(String.format("Class [%s] is not a collection.", clazz));
        }

        if (clazz.isInterface()) {
            if (SortedSet.class.isAssignableFrom(clazz)) {
                return new TreeSet<Object>();
            } else if (Set.class.isAssignableFrom(clazz)) {
                return new HashSet<Object>();
            } else if (List.class.isAssignableFrom(clazz)) {
                return new ArrayList<Object>();
            }

            return null;
        }

        return (Collection<Object>) clazz.newInstance();
    }

    private String extractPropertyNameFrom(Method method) {
        String methodName = method.getName();
        Matcher propertyNameMatcher = BUILDER_METHOD_PROPERTY_PATTERN.matcher(methodName);

        if (propertyNameMatcher.matches()) {
            String propertyName = propertyNameMatcher.group(1);
            if (propertyName != null) {
                return uncapitalize(propertyName);
            }
        }

        throw new IllegalStateException(String.format(
                "Method [%s] does not seem to represent a setter for a property", methodName));
    }

    private boolean isBuild(Method method) {
        return method.getReturnType() == Object.class;
    }

    private boolean isSetter(Method method) {
        return method.getParameterTypes().length == 1
                && method.getReturnType() == proxied;
    }

    private String uncapitalize(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        if (source.length() == 1) {
            return source.toLowerCase();
        }
        return new StringBuilder()
                .append(source.substring(0, 1).toLowerCase())
                .append(source.substring(1))
                .toString();
    }
}
