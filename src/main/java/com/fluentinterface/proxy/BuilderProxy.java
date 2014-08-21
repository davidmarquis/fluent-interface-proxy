package com.fluentinterface.proxy;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fluentinterface.utils.ConversionUtils.translateFromPrimitive;

/**
 * A dynamic proxy which will build a bean of the target type upon calls to the implemented interface.
 */
public class BuilderProxy implements InvocationHandler {

    private static final Pattern BUILDER_METHOD_PROPERTY_PATTERN = Pattern.compile("[a-z]+([A-Z].*)");

    private Class proxied;
    private Class  builtClass;
    private BuilderDelegate builderDelegate;
    private AttributeAccessStrategy attributeAccessStrategy;
    private Map<String, Object> propertiesToSet;

    public BuilderProxy(Class builderInterface, Class builtClass, BuilderDelegate builderDelegate,
                        AttributeAccessStrategy attributeAccessStrategy) {

        this.proxied = builderInterface;
        this.builtClass = builtClass;
        this.builderDelegate = builderDelegate;
        this.attributeAccessStrategy = attributeAccessStrategy;
        this.propertiesToSet = new LinkedHashMap<String, Object>();
    }

    public Object invoke(Object target, Method method, Object[] params) throws Throwable {

        if (isFluentSetter(method)) {
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
        } else if (isBuildMethod(method)) {
            params = extractVarArgsIfNeeded(params);
            return createInstanceFromProperties(params);
        }

        throw new IllegalStateException("Unrecognized builder method: " + method);
    }

    private Object[] extractVarArgsIfNeeded(Object[] params) {
        if (params != null
                && params.length == 1
                && params[params.length - 1].getClass().isArray()) {
            return (Object[]) params[params.length - 1];
        }
        return params;
    }

    private boolean hasProperty(Class<?> builtClass, String propertyName) {
        return attributeAccessStrategy.hasProperty(builtClass, propertyName);
    }

    private Object createInstanceFromProperties(Object[] params) throws Exception {
        buildIfBuilderInstances(params);

        Constructor<?> constructor = findMatchingConstructor(params);
        Object instance = constructor.newInstance(params);

        for (Map.Entry<String, Object> entry : propertiesToSet.entrySet()) {
            String property = entry.getKey();
            Object value = entry.getValue();

            setTargetProperty(instance, property, value);
        }

        return instance;
    }

    private Constructor findMatchingConstructor(Object[] params) throws NoSuchMethodException {
        if (params == null || params.length == 0) {
            // use default (empty) constructor
            return builtClass.getConstructor();
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
        List<Constructor<?>> candidates = new ArrayList<Constructor<?>>();
        for (Constructor<?> constructor : allConstructors) {

            Class<?>[] constructorParamTypes = constructor.getParameterTypes();
            if (constructorParamTypes.length != paramTypes.length) {
                continue;
            }

            // if all param types match constructor argument types (null always matching), then consider as candidate
            if (typesAreCompatible(paramTypes, constructorParamTypes)) {
                candidates.add(constructor);
            }
        }
        return candidates;
    }

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

    private void setTargetProperty(Object target, String property, Object value) throws Exception {
        Class targetPropertyType = attributeAccessStrategy.getPropertyType(target, property);

        if (value != null) {
            Collection<Object> valueAsCollection = convertToCollectionIfMultiValued(value);

            if (valueAsCollection != null) {
                valueAsCollection = buildBuildersInCollection(valueAsCollection);
                value = transformCollectionToTargetTypeIfPossible(value, valueAsCollection, targetPropertyType);
            } else {
                value = buildIfBuilderInstance(value);
            }
        }

        attributeAccessStrategy.setPropertyValue(target, property, value);
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

    private boolean hasBuilderDelegate() {
        return (builderDelegate != null);
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

    private void buildIfBuilderInstances(Object[] params) {
        for (int i = 0; i < params.length; i++) {
            params[i] = buildIfBuilderInstance(params[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private Object buildIfBuilderInstance(Object value) {
        if (!hasBuilderDelegate()) {
            return value;
        }

        if (builderDelegate.isBuilderInstance(value)) {
            return builderDelegate.build(value);
        }

        return value;
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

    @SuppressWarnings("unchecked")
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

    private boolean isBuildMethod(Method method) {
        if (hasBuilderDelegate()) {
            return builderDelegate.isBuildMethod(method);
        }
        return method.getReturnType() == Object.class;
    }

    private boolean isFluentSetter(Method method) {
        return method.getParameterTypes().length == 1
                && method.getReturnType().isAssignableFrom(this.proxied)
                && !this.isBuildMethod(method);
    }

    private String uncapitalize(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        if (source.length() == 1) {
            return source.toLowerCase();
        }
        return source.substring(0, 1).toLowerCase() + source.substring(1);
    }
}
