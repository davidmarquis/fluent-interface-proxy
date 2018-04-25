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
package com.fluentinterface.beans;

import com.fluentinterface.beans.reflect.Bean;
import com.fluentinterface.beans.reflect.Property;
import com.fluentinterface.beans.reflect.ReflectionException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for using Java Reflection APIs to facilitate generic property
 * getter and setter operations on Java objects.
 *
 * <p> This class is highly optimized for preventing internal objects
 * instantiation, and at the same time guarantees near-zero garbage collection.
 * In addition to this, in a not-concurrent context, is perfectly safe to change
 * the wrapped object and the internal options at any time, without side effects.
 *
 * <p> Various types of public properties are supported:
 *
 * <p> - <b>Simple properties</b> are any property of any type (both {@link Object}
 * than primitive). Methods which supports a string pattern allows to traverse
 * the properties' tree by separating the propety name with a '.' dot notation.
 *
 * <p> For example "foo.bar.zoo" is parsed to object.getFoo().getBar().getZoo();
 *
 * <p> - <b>Indexed properties</b> are {@link List}, finite {@link Iterable}
 * (any {@link java.util.Collection}), and any type of {@code array}. They usually
 * preserve their addition order (although it is not guaranteed for {@link Iterable}),
 * and are accessible through an integer index. Methods which supports a string
 * pattern allows to specify the requested integer index by a '[i]' notation.
 *
 * <p> - For example "myList[1]" is parsed to object.getMyList().get(1);
 * <p> - For example "myArray[5]" is parsed to object.getMyArray()[5];
 *
 * <p> - <b>Mapped properties</b> are {@link Map} type and are accessible through an
 * object key. Methods which supports the a string pattern allows to specify the
 * requested key by a '[key]' notation. The key string can contains dots.
 *
 * <p> For example "myMap[my.key]" is parsed to object.getMyMap().get("my.key");
 *
 * <p> Any pattern combination is allowed, for example "foo.myList[1].bar.myMap[test].myArray[5]"
 * is parsed to object.getFoo().getMyList().get(1).getBar().getMyMap().get("test").getMyArray()[5].
 *
 * @author Fabio Piro
 * @see Bean
 * @see Property
 */
public class ObjectWrapper {

    private Bean<?> bean;
    private Object object;
    private boolean isAutoGrowing = true;
    private boolean isAutoInstancing = true;
    private boolean isOutOfBoundsSafety = true;

    /**
     * Wraps an object.
     *
     * @param obj the object to wrap
     * @throws IllegalArgumentException if the obj parameter is {@code null}
     */
    public ObjectWrapper(Object obj) {
        setWrappedObject(obj);
    }

    /**
     * Changes, at any time, the wrapped object with a new object.
     *
     * @param obj the new object to wrap
     * @throws IllegalArgumentException if the obj parameter is {@code null}
     */
    public void setWrappedObject(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Cannot warp a 'null' object.");
        }

        this.object = obj;
        this.bean = Bean.forClass(obj.getClass());
    }

    /**
     * Returns the {@link Bean} cached instance of the wrapped object.
     *
     * @return the bean instance of the wrapped object
     */
    public Bean<?> getBean() {
        return bean;
    }

    /**
     * Changes the status of the Auto-Growing option.
     *
     * <p>Default: enabled. If enabled, automatically grows-up any unbound
     * {@link List} or {@code array} to accomodate an element in an index
     * position larger than their capacity.
     *
     * <p>Affected:
     * <p>- {@link ObjectWrapper#setValue(String, Object)}
     * <p>- {@link ObjectWrapper#setIndexedValue(String, int, Object)}
     * <p>- {@link ObjectWrapper#setIndexedValue(Property, int, Object)}
     *
     * @param isAutoGrowing the new isAutoGrowing flag option value
     */
    public void setAutoGrowing(boolean isAutoGrowing) {
        this.isAutoGrowing = isAutoGrowing;
    }

    /**
     * Changes the status of the Auto-Instancing option.
     *
     * <p>Default: enabled. If enabled, automatically instances a new object
     * (a no-args constructor is required) or a new {@link List}, {@link Map}
     * or {@code array}, if the nested {@link Property} has value {@code null}
     * in the wrapped object.
     *
     * <p>Affected:
     * <p>- {@link ObjectWrapper#setValue(String, Object)}
     * <p>- {@link ObjectWrapper#setIndexedValue(String, int, Object)}
     * <p>- {@link ObjectWrapper#setIndexedValue(Property, int, Object)}
     * <p>- {@link ObjectWrapper#setMappedValue(String, Object, Object)}
     * <p>- {@link ObjectWrapper#setMappedValue(Property, Object, Object)}
     *
     * @param isAutoInstancing the new isAutoInstancing option flag value
     */
    public void setAutoInstancing(boolean isAutoInstancing) {
        this.isAutoInstancing = isAutoInstancing;
    }

    /**
     * Changes the status of the OutOfBounds-Safety option.
     *
     * <p>Default: enabled. If enabled, always returns a {@code null} value
     * instead of throwing an {@link IndexOutOfBoundsException} when trying
     * to access to an item in an unbound position of a {@link List},
     * {@code array} or any type of finite {@link Iterable}, behaving like a
     * {@link Map#get(Object)} implementation.
     *
     * <p>Affected:
     * <p>- {@link ObjectWrapper#getValue(String)}
     * <p>- {@link ObjectWrapper#getIndexedValue(Property, int)}
     *
     * @param isOutOfBoundsSafety the new isOutOfBoundsSafety flag option value
     */
    public void setOutOfBoundsSafety(boolean isOutOfBoundsSafety) {
        this.isOutOfBoundsSafety = isOutOfBoundsSafety;
    }

    /**
     * Returns a single public {@link Property} of the wrapped object, if present, else {@code null}.
     *
     * <p> Differently from {@link Bean#getProperty(String)}, this method supports the nested
     * pattern notation, allowing to statically traverse the properties' tree (ex: "foo.bar.property").
     *
     * @param propertyPattern the nested pattern to traverse, cannot be {@code null}
     * @return the requested property, or {@code null} if not found
     * @throws IllegalArgumentException if the propertyPattern parameter is {@code null}
     * @throws IllegalArgumentException if the propertyPattern parameter contains an indexed\mapped '[]' notation
     */
    public Property getProperty(String propertyPattern) {
        return getProperty(bean, propertyPattern);
    }

    /**
     * Returns the value of the specified simple, indexed or mapped property locate by the given pattern.
     *
     * @param propertyPattern the pattern to traverse, cannot be {@code null}
     * @return the simple, indexed or mapped property value
     * @throws ReflectionException       if a reflection error occurs
     * @throws IllegalArgumentException  if the propertyPattern parameter is {@code null}
     * @throws IllegalArgumentException  if the propertyPattern parameter contains an unclosed indexed\mapped '[]' notation
     * @throws IllegalArgumentException  if the propertyPattern parameter contains an invalid, not parsable, index integer
     * @throws NullPointerException      if the propertyPattern parameter contains a nested property with a {@code null} value
     * @throws NullPointerException      if the propertyPattern parameter contains a nested not-existent property
     * @throws IndexOutOfBoundsException if the specified index is outside the valid range for the underlying property
     */
    public Object getValue(String propertyPattern) {
        return getValue(object, propertyPattern, this);
    }

    /**
     * Returns the value of the specified simple property from the wrapped object.
     *
     * @param propertyName the name of the property whose value is to be extracted, cannot be {@code null}
     * @return the simple property value
     * @throws ReflectionException      if a reflection error occurs
     * @throws IllegalArgumentException if the propertyName parameter is {@code null}
     * @throws NullPointerException     if the wrapped object does not have a property with the given name
     */
    public Object getSimpleValue(String propertyName) {
        return getSimpleValue(object, getPropertyOrThrow(bean, propertyName));
    }

    /**
     * Returns the value of the specified simple property from the wrapped object.
     *
     * @param property the property whose value is to be extracted, cannot be {@code null}
     * @return the simple property value
     * @throws ReflectionException      if a reflection error occurs
     * @throws IllegalArgumentException if the property parameter is {@code null}
     */
    public Object getSimpleValue(Property property) {
        return getSimpleValue(object, property);
    }

    /**
     * Returns the value of the specified indexed property from the wrapped object.
     *
     * @param propertyName the name of the indexed property whose value is to be extracted, cannot be {@code null}
     * @param index        the index of the property value to be extracted
     * @return the indexed property value
     * @throws ReflectionException       if a reflection error occurs
     * @throws IllegalArgumentException  if the propertyName parameter is {@code null}
     * @throws IllegalArgumentException  if the indexed object in the wrapped object is not a {@link List}, {@link Iterable} or {@code array}
     * @throws NullPointerException      if the indexed {@link List}, {@link Iterable} or {@code array} is {@code null} in the given object
     * @throws NullPointerException      if the wrapped object does not have a property with the given name
     * @throws IndexOutOfBoundsException if the specified index is outside the valid range for the underlying indexed property
     */
    public Object getIndexedValue(String propertyName, int index) {
        return getIndexedValue(object, getPropertyOrThrow(bean, propertyName), index, this);
    }

    /**
     * Returns the value of the specified indexed property from the wrapped object.
     *
     * @param property the indexed property whose value is to be extracted, cannot be {@code null}
     * @param index    the index of the property value to be extracted
     * @return the indexed property value
     * @throws ReflectionException       if a reflection error occurs
     * @throws IllegalArgumentException  if the property parameter is {@code null}
     * @throws IllegalArgumentException  if the indexed object in the wrapped object is not a {@link List}, {@link Iterable} or {@code array}
     * @throws NullPointerException      if the indexed {@link List}, {@link Iterable} or {@code array} is {@code null} in the wrapped object
     * @throws IndexOutOfBoundsException if the specified index is outside the valid range for the underlying indexed property
     */
    public Object getIndexedValue(Property property, int index) {
        return getIndexedValue(object, property, index, this);
    }

    /**
     * Returns the value of the specified mapped property from the wrapped object.
     *
     * @param propertyName the name of the mapped property whose value is to be extracted, cannot be {@code null}
     * @param key          the key of the property value to be extracted, can be {@code null}
     * @return the mapped property value
     * @throws ReflectionException      if a reflection error occurs
     * @throws IllegalArgumentException if the propertyName parameter is {@code null}
     * @throws IllegalArgumentException if the mapped object in the wrapped object is not a {@link Map} type
     * @throws NullPointerException     if the mapped object in the wrapped object is {@code null}
     * @throws NullPointerException     if the wrapped object does not have a property with the given name
     */
    public Object getMappedValue(String propertyName, Object key) {
        return getMappedValue(object, getPropertyOrThrow(bean, propertyName), key);
    }

    /**
     * Returns the value of the specified mapped property from the wrapped object.
     *
     * @param property the mapped property whose value is to be extracted, cannot be {@code null}
     * @param key      the key of the property value to be extracted, can be {@code null}
     * @return the mapped property value
     * @throws ReflectionException      if a reflection error occurs
     * @throws IllegalArgumentException if the property parameter is {@code null}
     * @throws IllegalArgumentException if the mapped object in the wrapped object is not a {@link Map} type
     * @throws NullPointerException     if the mapped object in the wrapped object is {@code null}
     */
    public Object getMappedValue(Property property, Object key) {
        return getMappedValue(object, property, key);
    }

    /**
     * Sets the value of the specified simple, indexed or mapped property locate
     * by the given pattern, in the wrapped object.
     *
     * @param propertyPattern the pattern to traverse, cannot be {@code null}
     * @param value           the value to set, can be {@code null}
     * @throws ReflectionException       if a reflection error occurs
     * @throws IllegalArgumentException  if the propertyPattern parameter is {@code null}
     * @throws IllegalArgumentException  if the propertyPattern parameter contains an unclosed indexed\mapped '[]' notation
     * @throws IllegalArgumentException  if the propertyPattern parameter contains an invalid, not parsable, index integer
     * @throws NullPointerException      if the propertyPattern parameter contains a nested property with a {@code null} value
     * @throws NullPointerException      if the propertyPattern parameter contains a nested not-existent property
     * @throws IndexOutOfBoundsException if the specified index is outside the valid range for the underlying property
     */
    public void setValue(String propertyPattern, Object value) {
        setValue(object, propertyPattern, value, this);
    }

    /**
     * Sets the value of the specified property in the wrapped object.
     *
     * @param propertyName the name of the simple property whose value is to be updated, cannot be {@code null}
     * @param value        the value to set, can be {@code null}
     * @throws IllegalArgumentException if the property parameter is {@code null}
     * @throws NullPointerException     if the wrapped object does not have a property with the given name
     */
    public void setSimpleValue(String propertyName, Object value) {
        setSimpleValue(object, getPropertyOrThrow(bean, propertyName), value);
    }

    /**
     * Sets the value of the specified property in the wrapped object.
     *
     * @param property the property whose value is to be updated, cannot be {@code null}
     * @param value    the value to set, can be {@code null}
     * @throws IllegalArgumentException if the property parameter is {@code null}
     */
    public void setSimpleValue(Property property, Object value) {
        setSimpleValue(object, property, value);
    }

    /**
     * Sets the value of the specified indexed property in the wrapped object.
     *
     * @param propertyName the name of the indexed property whose value is to be updated, cannot be {@code null}
     * @param index        the index position of the property value to be set
     * @param value        the indexed value to set, can be {@code null}
     * @throws ReflectionException       if a reflection error occurs
     * @throws IllegalArgumentException  if the propertyName parameter is {@code null}
     * @throws IllegalArgumentException  if the indexed object in the wrapped object is not a {@link List} or {@code array} type
     * @throws IndexOutOfBoundsException if the indexed object in the wrapped object is out of bounds with the given index and autogrowing is disabled
     * @throws NullPointerException      if the indexed object in the wrapped object is {@code null}
     * @throws NullPointerException      if the indexed object in the wrapped object is out of bounds with the given index, but autogrowing (if enabled) is unable to fill the blank positions with {@code null}
     * @throws NullPointerException      if the wrapped object does not have a property with the given name
     */
    public void setIndexedValue(String propertyName, int index, Object value) {
        setIndexedValue(object, getPropertyOrThrow(bean, propertyName), index, value, this);
    }

    /**
     * Sets the value of the specified indexed property in the wrapped object.
     *
     * @param property the indexed property whose value is to be updated, cannot be {@code null}
     * @param index    the index position of the property value to be set
     * @param value    the indexed value to set, can be {@code null}
     * @throws ReflectionException       if a reflection error occurs
     * @throws IllegalArgumentException  if the property parameter is {@code null}
     * @throws IllegalArgumentException  if the indexed object in the wrapped object is not a {@link List} or {@code array} type
     * @throws IndexOutOfBoundsException if the indexed object in the wrapped object is out of bounds with the given index and autogrowing is disabled
     * @throws NullPointerException      if the indexed object in the wrapped object is {@code null}
     * @throws NullPointerException      if the indexed object in the wrapped object is out of bounds with the given index, but autogrowing (if enabled) is unable to fill the blank positions with {@code null}
     */
    public void setIndexedValue(Property property, int index, Object value) {
        setIndexedValue(object, property, index, value, this);
    }

    /**
     * Sets the value of the specified mapped property in the wrapped object.
     *
     * @param propertyName the name of the mapped property whose value is to be extracted, cannot be {@code null}
     * @param key          the mapped key of the property value to be set
     * @param value        the property value to set, can be {@code null}
     * @throws ReflectionException      if a reflection error occurs
     * @throws IllegalArgumentException if the propertyName parameter is {@code null}
     * @throws IllegalArgumentException if the mapped object in the wrapped object is not a {@link Map} type
     * @throws NullPointerException     if the mapped object in the wrapped object is {@code null}
     * @throws NullPointerException     if the wrapped object does not have a property with the given name
     */
    public void setMappedValue(String propertyName, Object key, Object value) {
        setMappedValue(object, getPropertyOrThrow(bean, propertyName), key, value, this);
    }

    /**
     * Sets the value of the specified mapped property in the wrapped object.
     *
     * @param property the mapped property whose value is to be updated, cannot be {@code null}
     * @param key      the mapped key of the property value to be set
     * @param value    the property value to set
     * @throws ReflectionException      if a reflection error occurs
     * @throws IllegalArgumentException if the property parameter is {@code null}
     * @throws IllegalArgumentException if the mapped object in the wrapped object is not a {@link Map}
     * @throws NullPointerException     if the mapped object in the wrapped object is {@code null}
     */
    public void setMappedValue(Property property, Object key, Object value) {
        setMappedValue(object, property, key, value, this);
    }

    /**
     * Returns a textual rapresentation of this wrapper.
     *
     * @return a string decribing the wrapped object
     */
    @Override
    public String toString() {
        return "ObjectWrapper{object=" + object + '}';
    }

    // STATIC SECTION
    //
    // The follow static section contains a corrispective static method for each wrapper method.
    // This hybrid architecture allows to avoid the instantiation of a new wrapper object when
    // traversing each property graph with a nested notation, hence optimizing the heap allocation.
    // -----------------------------------------------------------------------------------------------

    /**
     * Internal: Static version of {@link #getProperty(String)}, throws an exception instead of return null.
     *
     * @param bean         the bean object whose property is to be searched
     * @param propertyName the name of the property to search
     * @return the property with the given name, if found
     * @throws NullPointerException if the bean object does not have a property with the given name
     */
    private static Property getPropertyOrThrow(Bean bean, String propertyName) {
        Property property = bean.getProperty(propertyName);

        if (property == null) {
            throw new PropertyNotFoundException("Cannot find property with name '" + propertyName + "' in " + bean + ".");
        }

        return property;
    }

    /**
     * Optimized version of indexOf that searches both '.' and '[' positions
     * using just a sigle loop, while taking in consideration (and ignoring)
     * possible dots inside the square notation (like "map[foo.bar].zoo").
     *
     * <p> The searching process will stop immediately at the first '.' found,
     * hence the result order matters: '.' has precedence over '[' notation.
     *
     * <p> To avoid the need of a new object for containing both results (gc),
     * the alghoritm will return as a positive int value the '.' first position,
     * and as a negative int value the negated '[' first position.
     *
     * <p> A result == 0 means "not found", as a propertyPattern with the first
     * char equal to '.' or '[', in [0] position, is an illegal pattern anyway.
     *
     * @param propertyPattern the pattern to parse, cannot be {@code null}
     * @return the first '.' position (if any), or
     * the negated first '[' position (if any and no '.' found before), or
     * 0 if neither '.' nor ']' was found, or no valid pattern present
     * @throws IllegalArgumentException if the whole string was parsed but '[x]'
     *                                  notation is still "open" without an enclosing ']'
     */
    private static int indexOfDotOrSquare(String propertyPattern) {
        int lenght = propertyPattern.length();
        int result = 0;// nothing found

        boolean dotFound = false;
        boolean insideSquares = false;

        int i = 0;
        while (!dotFound && i < lenght) {
            char current = propertyPattern.charAt(i);

            if (!insideSquares) {
                if (current == '.') {
                    dotFound = true;
                    result = i;// found: set as positive and stop the loop
                } else if (current == '[') {
                    insideSquares = true;
                    result = -i;// found: set as negative, but still continuing...
                }
            } else {
                if (current == ']') {
                    insideSquares = false;
                }
            }

            i++;
        }

        if (insideSquares) {
            throw new IllegalArgumentException("Cannot found the closing ']' suffix in '" + propertyPattern + "' pattern.");
        }

        return result;
    }

    /*
     * Internal static version of {@link #getProperty(String)}.
     */
    public static Property getProperty(Bean bean, String propertyPattern) {
        if (propertyPattern == null) {
            throw new IllegalArgumentException("Cannot get a property with a 'null' propertyPattern.");
        }

        // '.' position if positive, '[' inverse position if negative
        int position = indexOfDotOrSquare(propertyPattern);

        // (propertyPattern.contains("."))
        if (position > 0) {
            String leftPattern = propertyPattern.substring(0, position);
            String rightPattern = propertyPattern.substring(position + 1, propertyPattern.length());
            Property leftProperty = bean.getProperty(leftPattern);

            return (leftProperty != null) ? getProperty(Bean.forClass(leftProperty.getType()), rightPattern) : null;
            // if (propertyPattern.contains("["))
        } else if (position < 0) {
            throw new IllegalArgumentException("The indexed or mapped '[]' notation is not allowed while searching" +
                                                       " a property, but '" + propertyPattern + "' pattern found.");
        }

        return bean.getProperty(propertyPattern);
    }

    /*
     * Internal static version of {@link ObjectWrapper#getValue(String)}.
     */
    private static Object getValue(Object obj, String propertyPattern, ObjectWrapper options) {
        if (propertyPattern == null) {
            throw new IllegalArgumentException("Cannot get the value from a property with a 'null' propertyPattern.");
        }

        if (obj == null) {
            // NullPointerException as it is throwed only when traversing a null object (ex: "foo.null.bar")
            throw new NullPointerException("Cannot get the value of '" + propertyPattern + "' from a 'null' object.");
        }

        // '.' position if positive, '[' inverse position if negative
        int position = indexOfDotOrSquare(propertyPattern);

        // (propertyPattern.contains("."))
        if (position > 0) {
            String leftPattern = propertyPattern.substring(0, position);
            String rightPattern = propertyPattern.substring(position + 1, propertyPattern.length());
            Object leftValue = getValue(obj, leftPattern, options);

            return getValue(leftValue, rightPattern, options);
            // if (propertyPattern.contains("["))
        } else if (position < 0) {
            int squarePosition = -position;

            String leftPattern = propertyPattern.substring(0, squarePosition);
            String indexOrKey = propertyPattern.substring(squarePosition + 1, propertyPattern.length() - 1);// removes ']'
            Property leftProperty = getPropertyOrThrow(Bean.forClass(obj.getClass()), leftPattern);

            if (Map.class.isAssignableFrom(leftProperty.getType())) {
                return getMappedValue(obj, leftProperty, indexOrKey);
            } else {
                try {
                    return getIndexedValue(obj, leftProperty, Integer.parseInt(indexOrKey), options);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("The pattern '" + indexOrKey + "' for the indexed "
                                                               + leftProperty + " is invalid. Cannot parse the string to a valid integer index.");
                }
            }
        }

        return getSimpleValue(obj, getPropertyOrThrow(Bean.forClass(obj.getClass()), propertyPattern));
    }

    /*
     * Internal: Static version of {@link ObjectWrapper#getSimpleValue(Property)}.
     */
    private static Object getSimpleValue(Object obj, Property property) {
        if (property == null) {
            throw new IllegalArgumentException("Cannot get the value from a 'null' property.");
        }

        return property.get(obj);
    }

    /*
     * Internal: Static version of {@link ObjectWrapper#getIndexedValue(Property, int)}.
     */
    @SuppressWarnings("unchecked")
    private static Object getIndexedValue(Object obj, Property property, int index, ObjectWrapper options) {
        if (property == null) {
            throw new IllegalArgumentException("Cannot get the indexed value from 'null' property.");
        }

        Object propertyValue = property.get(obj);

        if (propertyValue == null) {
            throw new NullPointerException("Invalid 'null' value found for indexed '" + property + "' in "
                                                   + obj.getClass().getName() + ".");

        }

        if (propertyValue instanceof List) {
            List list = (List) propertyValue;
            int size = list.size();

            if (size < index) {
                if (options.isOutOfBoundsSafety) {
                    return null;
                } else {
                    throw new IndexOutOfBoundsException("The indexed " + property + " in "
                                                                + obj.getClass().getSimpleName() + " object has only '" + size + "' elements," +
                                                                " but index '" + index + "' requested.");
                }
            }

            return list.get(index);
        } else if (propertyValue.getClass().isArray()) {
            int lenght = Array.getLength(propertyValue);

            if (lenght < index && options.isOutOfBoundsSafety) {
                return null;
            }

            try {
                return Array.get(propertyValue, index);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("The indexed " + property + " in "
                                                            + obj.getClass().getSimpleName() + " object has only '" + lenght + "' elements," +
                                                            " but index '" + index + "' requested.");

            }
        } else if (propertyValue instanceof Iterable) {
            Iterable iterable = (Iterable) propertyValue;

            int k = 0;
            for (Object object : iterable) {
                if (k == index) {
                    return object;
                }

                k++;// Hopefully not an infinite iteration
            }

            // Nothing found
            if (options.isOutOfBoundsSafety) {
                return null;
            } else {
                throw new IndexOutOfBoundsException("The indexed " + property + " in "
                                                            + obj.getClass().getSimpleName() + " object has less than '" + index + "'" +
                                                            " elements.");
            }
        } else {
            throw new IllegalArgumentException("Cannot get an indexed value from the not indexed " + property
                                                       + ". Only List, array and Iterable types are supported, but " + property.getType().getSimpleName()
                                                       + " found.");
        }
    }

    /*
     * Internal: Static version of {@link ObjectWrapper#getMappedValue(Property, Object)}.
     */
    @SuppressWarnings("unchecked")
    private static Object getMappedValue(Object obj, Property property, Object key) {
        if (property == null) {
            throw new IllegalArgumentException("Cannot get the mapped value from a 'null' property.");
        }

        if (property.getType().isAssignableFrom(Map.class)) {
            Map<Object, Object> map = (Map<Object, Object>) property.get(obj);

            if (map == null) {
                throw new NullPointerException("Invalid 'null' value found for mapped " + property + " in "
                                                       + obj.getClass().getName() + ".");

            }

            return map.get(key);
        } else {
            throw new IllegalArgumentException("Cannot get a mapped value from the not mapped " + property
                                                       + ". Only Map type is supported, but " + property.getType().getSimpleName() + " found.");
        }
    }

    /*
     * Internal: Static version of {@link ObjectWrapper#setValue(String, Object)}
     */
    private static void setValue(Object obj, String propertyPattern, Object value, ObjectWrapper options) {
        if (propertyPattern == null) {
            throw new IllegalArgumentException("Cannot set a new value to a property with a 'null' propertyPattern.");
        }

        if (obj == null) {
            // NullPointerException as it is throwed only when traversing a null object (ex: "foo.null.bar")
            throw new NullPointerException("Cannot set the value of '" + propertyPattern + "' to a 'null' object.");
        }

        // '.' position if positive, '[' inverse position if negative
        int position = indexOfDotOrSquare(propertyPattern);

        // (propertyPattern.contains("."))
        if (position > 0) {
            String leftPattern = propertyPattern.substring(0, position);
            String rightPattern = propertyPattern.substring(position + 1, propertyPattern.length());
            Object leftValue = getValue(obj, leftPattern, options);

            if (leftValue == null) {
                Property leftProperty = getPropertyOrThrow(Bean.forClass(obj.getClass()), leftPattern);

                if (options.isAutoInstancing) {
                    try {
                        leftValue = leftProperty.getType().newInstance();
                    } catch (Exception e) {
                        throw new ReflectionException("The value of " + leftProperty + " was 'null' in the object "
                                                              + obj.getClass().getName() + ". An attempt to invoke its 'no-args constructor'" +
                                                              " was made, but an error occurs.", e);
                    }

                    leftProperty.set(obj, leftValue);
                }
            }

            setValue(leftValue, rightPattern, value, options);
            // if (propertyPattern.contains("["))
        } else if (position < 0) {
            int squarePosition = -position;

            String leftPattern = propertyPattern.substring(0, squarePosition);
            String indexOrKey = propertyPattern.substring(squarePosition + 1, propertyPattern.length() - 1);// removes ']'
            Property leftProperty = getPropertyOrThrow(Bean.forClass(obj.getClass()), leftPattern);

            if (leftProperty.getType().isAssignableFrom(Map.class)) {
                setMappedValue(obj, leftProperty, indexOrKey, value, options);
            } else {
                try {
                    setIndexedValue(obj, leftProperty, Integer.parseInt(indexOrKey), value, options);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("The pattern '" + indexOrKey + "' for the indexed "
                                                               + leftProperty + " is invalid. Cannot parse the string to a valid integer index.");
                }
            }
        } else {
            setSimpleValue(obj, getPropertyOrThrow(Bean.forClass(obj.getClass()), propertyPattern), value);
        }
    }

    /*
     * Internal: Static version of {@link ObjectWrapper#setSimpleValue(Property, Object)}.
     */
    private static void setSimpleValue(Object obj, Property property, Object value) {
        if (property == null) {
            throw new IllegalArgumentException("Cannot set a new value to a property with a 'null' propertyName.");
        }

        property.set(obj, value);
    }

    /*
     * Internal: Static version of {@link ObjectWrapper#setIndexedValue(Property, int, Object)}.
     */
    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    private static void setIndexedValue(Object obj, Property property, int index, Object value, ObjectWrapper options) {
        if (property == null) {
            throw new IllegalArgumentException("Cannot set a new indexed value to a 'null' property.");
        }

        Object propertyValue = property.get(obj);

        if (propertyValue == null) {
            if (options.isAutoInstancing) {
                Class<?> propertyType = property.getType();

                if (propertyType.isAssignableFrom(List.class)) {
                    propertyValue = new ArrayList();
                    property.set(obj, propertyValue);
                } else if (propertyType.isArray()) {
                    propertyValue = Array.newInstance(property.getActualType(), index + 1);
                    property.set(obj, propertyValue);
                } else {
                    throw new IllegalArgumentException("The indexed " + property + " in object "
                                                               + obj.getClass().getSimpleName() + " has value 'null'. Only List and array types can be " +
                                                               "auto instantiated, but " + propertyType.getSimpleName() + " found.");
                }
            } else {
                throw new NullPointerException("Invalid 'null' value found for the indexed '" + property + "' in "
                                                       + obj.getClass().getName() + " object.");
            }
        }

        if (propertyValue instanceof List) {
            List list = (List) propertyValue;
            int size = list.size();

            if (index >= size && options.isAutoGrowing) {
                for (int i = size; i < index; i++) {
                    try {
                        list.add(null);
                    } catch (NullPointerException ex) {
                        throw new NullPointerException("The indexed " + property + " has size lower than the requested '"
                                                               + index + "' index. An attempt to autogrowing it, filling with 'null' values, was made, " +
                                                               " but the List implementation seems to do not accept 'null' values. " + ex.getMessage());
                    }
                }

                list.add(index, value);
            } else {
                try {
                    list.set(index, value);
                } catch (IndexOutOfBoundsException e) {
                    throw new IndexOutOfBoundsException("Cannot set a new value to the indexed list " + property + " in "
                                                                + obj.getClass().getSimpleName() + " as the requested '" + index + "' index is unbound.");
                }
            }
        } else if (propertyValue.getClass().isArray()) {
            int length = Array.getLength(propertyValue);

            if (index >= length && options.isAutoGrowing) {
                Object biggerArray = Array.newInstance(property.getActualType(), index + 1);
                System.arraycopy(propertyValue, 0, biggerArray, 0, length);
                property.set(obj, biggerArray);
                propertyValue = biggerArray;
            }

            try {
                Array.set(propertyValue, index, value);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Cannot set a new value to the indexed array " + property
                                                            + " in " + obj.getClass().getSimpleName() + " as the requested '" + index + "' index is unbound.");
            }
        } else {
            throw new IllegalArgumentException("Cannot set a new value to a not indexed " + property + ". Only List and array " +
                                                       "types are supported, but " + propertyValue.getClass().getName() + " found.");
        }
    }

    /*
     * Internal: Static version of {@link ObjectWrapper#setMappedValue(Property, Object, Object)}.
     */
    @SuppressWarnings("unchecked")
    private static void setMappedValue(Object obj, Property property, Object key, Object value, ObjectWrapper options) {
        if (property == null) {
            throw new IllegalArgumentException("Cannot set a new mapped value to a 'null' property.");
        }

        if (Map.class.isAssignableFrom(property.getType())) {
            Map map = (Map) property.get(obj);

            if (map == null) {
                if (options.isAutoInstancing) {
                    map = new LinkedHashMap();
                    property.set(obj, map);
                } else {
                    throw new NullPointerException("Invalid 'null' value found for the mapped '" + property + "' in "
                                                           + obj.getClass().getName() + " object.");
                }
            }

            map.put(key, value);
        } else {
            throw new IllegalArgumentException("Cannot set a new mapped value to " + property
                                                       + ". Only Map type is supported for mapped properties, but " + property.getType().getName()
                                                       + " found.");
        }
    }
}
