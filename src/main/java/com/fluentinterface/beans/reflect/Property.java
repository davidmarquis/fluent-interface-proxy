/*
 * Copyright 2015 Fabio Piro (minimalcode.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fluentinterface.beans.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fluentinterface.beans.reflect.Bean.*;

/**
 * A {@link Property} provides instrospected information about, and dynamic
 * access to, a single property of a {@link Bean}.
 *
 * <p> Accessors are matched based on the "get", "is" (for boolean)
 * and "set" {@link Method} prefix. The following patterns are
 * automatically discovered as valid property accessors:
 *
 * <p> - Read  Method:   T getField() || boolean isField()
 * <p> - Write Method:   void setField(T field)
 *
 * <p> Static and synthetic members are not considered valid accessors
 * of a property. Read-only and write-only properties are all valid options.
 *
 * <p> If a backing {@link Field} with the same name and type as the
 * property is present is the declaring class, it will be retrieved not
 * taking into account its modifier. If it is present in a superclass
 * instead, it will be retrieved only if it has a not-private modifier.
 *
 * <p> In any case, accessors don't have to specifically map the backing
 * field with the same name, and the field is only used as an annotations
 * provider, considering that annotating directly the backing field is
 * a well established pattern.
 *
 * <p> While public properties are guaranteed to have at least a public
 * accessor method (and consequently are always readable or writable),
 * for declared properties it may happen that all the accessors methods
 * are not accessible.
 *
 * <p> To preserve immutability, this class <em>not</em> provides a way
 * to forcefully alter the property's accessibility. It is responsability
 * of the developer to deal with it using directly the property's accessors.
 *
 * <p> Introspected information (like type or annotations) is collected from
 * all the property's {@link Member}s (and their ancestors), following a
 * "best-available" design pattern.
 *
 * @author Fabio Piro
 * @see Bean
 * @see Bean#getProperties()
 * @see Bean#getProperty(String)
 * @see Bean#getDeclaredProperties()
 * @see Bean#getDeclaredProperty(String)
 */
public final class Property implements AnnotatedElement {

    private final String name;
    private final Field field;
    private final Method readMethod;
    private final Method writeMethod;

    private final Class<?> type;
    private final Class<?> actualType;
    private final Bean<?> declaringBean;
    private final Type genericType;
    private final int hashCode;

    private final Map<Class<? extends Annotation>, Annotation> annotations;
    private final Map<Class<? extends Annotation>, Annotation> declaredAnnotations;

    /**
     * Internal: Creates a new property.
     *
     * <p> Package-private constructor, as the only way to get a property is
     * through the {@link Bean}'s methods.
     *
     * @param declaringBean bean which declared this property, cannot be {@code null}
     * @param name          name, cannot be {@code null}
     * @param readMethod    getter\isser method, can be {@code null} if writeMethod is not
     * @param writeMethod   setter method, can be {@code null} if readMethod is not
     */
    Property(Bean<?> declaringBean, String name, Method readMethod, Method writeMethod) {
        this.name = name;
        this.declaringBean = declaringBean;
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.hashCode = declaringBean.getType().getName().hashCode() ^ name.hashCode();

        // Type and GenericType
        if (writeMethod != null) {
            type = writeMethod.getParameterTypes()[0];
            genericType = writeMethod.getGenericParameterTypes()[0];
        } else /* if (readMethod != null) is always true */ {
            type = readMethod.getReturnType();
            genericType = readMethod.getGenericReturnType();
        }

        // ActualType
        Class<?> candidateActualType;

        try {
            if (type.isArray()) {
                candidateActualType = type.getComponentType();
            } else if (Iterable.class.isAssignableFrom(type)) {
                candidateActualType = (Class) ((ParameterizedType) genericType).getActualTypeArguments()[0];
            } else if (Map.class.isAssignableFrom(type)) {
                candidateActualType = (Class) ((ParameterizedType) genericType).getActualTypeArguments()[1];
            } else {
                candidateActualType = type;
            }
        } catch (Exception e) {
            // Unresolved T generic or raw
            candidateActualType = null;
        }

        actualType = candidateActualType;

        field = findAccessorField(declaringBean.getType(), name, type);

        // Annotations
        Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>(4);
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations = new HashMap<Class<? extends Annotation>, Annotation>(4);
        for (AnnotatedElement element : new AnnotatedElement[]{field, readMethod, writeMethod}) {
            if (element == null) {
                continue;
            }

            // General Annotations
            for (Annotation annotation : element.getAnnotations()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                annotations.put(annotationType, annotation);
            }

            // Declared Annotations
            if (((Member) element).getDeclaringClass() == declaringBean.getType()) {
                for (Annotation annotation : element.getDeclaredAnnotations()) {
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    declaredAnnotations.put(annotationType, annotation);
                }
            }
        }

        this.annotations = optimizeMap(annotations);
        this.declaredAnnotations = optimizeMap(declaredAnnotations);
    }

    /**
     * Returns all the annotations that are <em>collectively present</em> on this
     * property's members.
     *
     * <p> Annotation may be inherited from members declared in a superclass.
     *
     * <p> In the case of collision of an annotation with the same type in the leaf
     * class from a different {@link AnnotatedElement}, the overriding order is:
     * {@code field}, {@code readMethod} and finally {@code writeMethod}.
     *
     * <p> If there are no annotations found, the return value is an array of
     * length 0.
     *
     * <p> The caller of this method is free to modify the returned array;
     * it will have no effect on the arrays returned to other callers.
     *
     * <p> The elements in the returned array are not sorted and are not in any
     * particular order.
     *
     * @return annotations collectively present on this property's members
     */
    @Override
    public Annotation[] getAnnotations() {
        return annotations.values().toArray(new Annotation[0]);
    }

    /**
     * Returns all the annotations that are <em>collectively and directly present</em>
     * on this property's members. This method ignores inherited annotations.
     *
     * <p> If there are no annotations <em>collectively and directly present</em>
     * on this property's members, the return value is an array of length 0.
     *
     * <p> The caller of this method is free to modify the returned array; it will
     * have no effect on the arrays returned to other callers.
     *
     * <p> The elements in the returned array are not sorted and are not in any
     * particular order.
     *
     * @return annotations collectively and directly present on this property's members
     */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return declaredAnnotations.values().toArray(new Annotation[0]);
    }

    /**
     * Returns an annotation <em>collectively present</em> on this property's members, else {@code null}.
     *
     * @param <T>             the type of the annotation to query for and return, if collectively present
     * @param annotationClass the Class object corresponding to the annotation type
     * @return the specified annotation type if collectively present on this property, else {@code null}
     * @throws NullPointerException if the given annotation class is {@code null}
     */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotationClass == null) {
            throw new NullPointerException("Cannot get an annotation with a 'null' annotationClass.");
        }

        return annotationClass.cast(annotations.get(annotationClass));
    }

    /**
     * Returns this property's annotation for the specified type if
     * such an annotation is <em>collectively and directly present</em>,
     * else {@code null}.
     *
     * <p> This method ignores inherited annotations. and returns {@code null}
     * if no annotations are <em>collectively and directly present</em> on
     * this property's members.
     *
     * @param <T>             the type of the annotation to query for and return if
     *                        collectively and directly present
     * @param annotationClass the Class corresponding to the annotation type
     * @return this property's annotation for the specified annotation type if
     * collectively and directly present on this element, else {@code null}
     * @throws NullPointerException if the given annotation class is {@code null}
     */
    @SuppressWarnings("override")// must be disabled to support jdk 6+
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        if (annotationClass == null) {
            throw new NullPointerException("Cannot get a declared annotation with a 'null' annotationClass.");
        }

        return annotationClass.cast(declaredAnnotations.get(annotationClass));
    }

    /**
     * Returns true if an annotation for the specified type is
     * <em>collectively present</em> on this property's members, else false.
     *
     * @param annotationClass the Class object corresponding to the annotation type
     * @return true if an annotation for the specified type is collectively present on this property, else false
     * @throws NullPointerException if the given annotationClass is {@code null}
     */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        if (annotationClass == null) {
            throw new NullPointerException("Cannot check the presence of an annotation with a 'null' annotationClass.");
        }

        return annotations.containsKey(annotationClass);
    }

    /**
     * Returns the {@link Bean} object that declares this property.
     *
     * @return an object representing the declaring bean
     */
    public Bean<?> getDeclaringBean() {
        return declaringBean;
    }

    /**
     * Returns the simple name of this property.
     *
     * <p> The name is the lowercased method's name without
     * the relative prefix (get\is\set).
     *
     * @return the simple name of this property
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this property.
     *
     * @return the type class
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the actual type of this property, from simple type or generics.
     *
     * <p> The actual type is the plain type for simple property, the generic
     * element resolved type for {@link List}, {@link Iterable}, {@code array}
     * and {@link Map} property, or {@code null} for not resolved generic type.
     *
     * @return the actual type of this property, or {@code null} if unresolved
     */
    public Class<?> getActualType() {
        return actualType;
    }

    /**
     * Returns a {@link Type} object that represents the declared type
     * for the property represented by this {@link Property} object.
     *
     * @return the type object for this property
     */
    public Type getGenericType() {
        return genericType;
    }

    /**
     * Returns a copy of this property's read{@link Method}, if present, else {@code null}.
     *
     * @return A copy of the getter/isser method, or {@code null} if this property has no valid getter/isser
     */
    public Method getReadMethod() {
        if (readMethod == null) return null;

        try {
            // Note: sun.reflect.ReflectionFactory is not available in Android API
            return readMethod.getDeclaringClass().getDeclaredMethod(readMethod.getName());
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(e.getMessage(), e);// Not Reproducible
        }
    }

    /**
     * Returns a copy of this property's write{@link Method}, if present, else {@code null}.
     *
     * @return A copy of the setter method, or {@code null} if this proprety has no valid setter
     */
    public Method getWriteMethod() {
        if (writeMethod == null) return null;

        try {
            // Note: sun.reflect.ReflectionFactory is not available in Android API
            return writeMethod.getDeclaringClass().getDeclaredMethod(writeMethod.getName(), type);
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(e.getMessage(), e);// Not Reproducible
        }
    }

    /**
     * Returns a copy of this property's {@link Field}, if present, else {@code null}.
     * *
     *
     * @return A copy of the underlying field, or {@code null} if this property has no field
     */
    public Field getField() {
        if (field == null) return null;

        try {
            // Note: sun.reflect.ReflectionFactory is not available in Android API
            return field.getDeclaringClass().getDeclaredField(field.getName());
        } catch (NoSuchFieldException e) {
            throw new ReflectionException(e.getMessage(), e);// Not Reproducible
        }
    }

    /**
     * Returns true if this property has a valid getter\isser.
     *
     * @return true if this property is readable from an object, else false
     */
    public boolean isReadable() {
        return isPublic(readMethod);
    }

    /**
     * Returns true if this property has a valid setter.
     *
     * @return true if this property is writable to an object, else false
     */
    public boolean isWritable() {
        return isPublic(writeMethod);
    }

    /**
     * Returns the value of the representation of this property from the specified object.
     *
     * <p> The underlying property's value is obtained trying to invoke the {@code readMethod}.
     *
     * <p> If this {@link Property} object has no public {@code readMethod},
     * it is considered write-only, and the action will be prevented throwing
     * a {@link ReflectionException}.
     *
     * <p> The value is automatically wrapped in an object if it has a primitive type.
     *
     * @param obj object from which the property's value is to be extracted
     * @return the value of the represented property in object {@code obj}
     * @throws ReflectionException if access to the underlying method throws an exception
     * @throws ReflectionException if this property is write-only (no public readMethod)
     */
    public Object get(Object obj) throws ReflectionException {
        try {
            if (isPublic(readMethod)) {
                return readMethod.invoke(obj);
            } else {
                throw new ReflectionException("Cannot get the value of " + this + ", as it is write-only.");
            }
        } catch (Exception e) {
            throw new ReflectionException("Cannot get the value of " + this + " in object " + obj, e);
        }
    }

    /**
     * Sets a new value to the representation of this property on the specified object.
     *
     * <p> The underlying property's value will be updated trying
     * to invoke the {@code writeMethod}.
     *
     * <p> If this {@link Property} object has no public {@code writeMethod}, it is considered read-only,
     * and the action will be prevented throwing a {@link ReflectionException}.
     *
     * <p>The new value is automatically unwrapped if the property has a primitive type.
     *
     * @param obj   the object whose property should be modified
     * @param value the new value for the property of {@code obj}, can be {@code null}
     * @throws ReflectionException if accessing to the underlying method throws an exception
     * @throws ReflectionException if this property is read-only (no public writeMethod)
     */
    public void set(Object obj, Object value) throws ReflectionException {
        try {
            if (isPublic(writeMethod)) {
                writeMethod.invoke(obj, value);
            } else {
                throw new ReflectionException("Cannot set the value of " + this + ", as it is read-only.");
            }
        } catch (Exception e) {
            throw new ReflectionException("Cannot set the value of " + this + " to object " + obj, e);
        }
    }

    /**
     * Compares this property against the specified object.
     *
     * <p> Two properties are the same if they were declared in the same bean
     * and have the same name and the same accessors. Under the right circumstances
     * a property and a declared property can be "technically" equivalent.
     *
     * @param obj the object to compare
     * @return true if the objects are the same
     */
    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Property other = (Property) obj;
        if (!name.equals(other.name)) return false;
        if (readMethod != null ? !readMethod.equals(other.readMethod) : other.readMethod != null) return false;
        if (writeMethod != null ? !writeMethod.equals(other.writeMethod) : other.writeMethod != null) return false;

        return declaringBean.equals(other.declaringBean);
    }

    /**
     * Returns a hashcode for this {@code Property}.
     * <p>
     * This is computed as the exclusive-or of the hashcodes for the underlying
     * bean's type class name and its simple name.
     *
     * @return this property's hashcode
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Internal: Gets a {@link Field} with any valid modifier, evaluating its hierarchy.
     *
     * @param type         the class from where to search, cannot be null
     * @param name         the name of the field to search, cannot be null
     * @param requiredType the required type to match, cannot be null
     * @return the field, if found, else {@code null}
     */
    private static Field findAccessorField(Class<?> type, String name, Class<?> requiredType) {
        Class<?> current = type;

        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getName().equals(name)
                        && field.getType().equals(requiredType)
                        && !isStatic(field)
                        && !field.isSynthetic()
                        && (!isPrivate(field) || field.getDeclaringClass() == type)) {
                    return field;
                }
            }

            current = current.getSuperclass();
        }

        return null;
    }

    /**
     * Returns a textual rapresentation of this property.
     *
     * @return a String with this property's type followed by a dot and this property's name
     */
    @Override
    public String toString() {
        return "property " + declaringBean.getType().getName() + "." + name;
    }
}
