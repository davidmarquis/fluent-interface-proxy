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

import java.lang.ref.SoftReference;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A bean provides introspected information about the properties
 * of a {@link Class} or an interface.
 *
 * @author Fabio Piro
 * @see Property
 * @see Class#getFields()
 * @see Class#getMethods()
 * @see Class#getDeclaredFields()
 * @see Class#getDeclaredMethods()
 */
public final class Bean<T> {
    private final static Map<Class<?>, SoftReference<Bean<?>>> beansCache = new ConcurrentHashMap<Class<?>, SoftReference<Bean<?>>>();

    private final Class<T> type;
    private final Map<String, Property> properties;
    private final Map<String, Property> declaredProperties;

    /**
     * Introspects a {@link Class} or an interface and learns about all
     * its {@link Property} elements.
     *
     * <p> If the target type has been previously analized then the {@link Bean}
     * instance is retrieved from a thread-safe {@link SoftReference} cache.
     *
     * @param beanClass the class or interface to analize
     * @param <T>       the bean's type.
     * @return a {@link Bean} object describing the target class or interface
     * @throws NullPointerException if the given beanClass parameter is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> Bean<T> forClass(Class<T> beanClass) {
        if (beanClass == null) {
            throw new NullPointerException("Cannot instrospect a bean with a 'null' beanClass.");
        }

        Bean bean;
        SoftReference<Bean<?>> softReference = beansCache.get(beanClass);

        if (softReference == null) {
            bean = new Bean(beanClass);
            beansCache.put(beanClass, new SoftReference<Bean<?>>(bean));
        } else {
            bean = softReference.get();

            if (bean == null) {
                bean = new Bean(beanClass);
                beansCache.put(beanClass, new SoftReference<Bean<?>>(bean));
            }
        }

        return bean;
    }

    /**
     * Internal: Introspects a {@link Class} or an interface and learns about all
     * its {@link Property} elements.
     *
     * <p> Private constructor, as the only way to get a {@link Bean} object is through
     * the {@link #forClass(Class)} static factory method.
     *
     * @param type the class or interface to analize
     */
    private Bean(Class<T> type) {
        this.type = type;

        // Properties
        Map<String, Property> properties = new HashMap<String, Property>();
        for (PropertyDescriptor descriptor : getPropertyDescriptors(type.getMethods())) {
            Property property = new Property(this, descriptor.name, descriptor.readMethod, descriptor.writeMethod);
            properties.put(property.getName(), property);
        }

        // Declared Properties
        Map<String, Property> declaredProperties = new HashMap<String, Property>();
        for (PropertyDescriptor descriptor : getPropertyDescriptors(type.getDeclaredMethods())) {
            Property declaredProperty = new Property(this, descriptor.name, descriptor.readMethod, descriptor.writeMethod);

            // Properties are immutable and Map::containsValue(V) uses Property::equals() to check equality.
            // Considering that a property with the same name, bean and accessors is "technically" equivalent
            // to its corrispective declared version, then it can be used instead, saving some heap memory space.
            if (properties.containsValue(declaredProperty)) {
                declaredProperties.put(declaredProperty.getName(), properties.get(declaredProperty.getName()));
            } else {
                declaredProperties.put(declaredProperty.getName(), declaredProperty);
            }
        }

        this.properties = optimizeMap(properties);
        this.declaredProperties = optimizeMap(declaredProperties);
    }

    /**
     * Internal: Introspects the given type and creates the appropriate {@link PropertyDescriptor}.
     *
     * <p> This logic is a completely rewritten version of {@link java.beans.Introspector},
     * providing fewer bounds on the matching types (less strict equivalence for the return
     * type of readMethod and the first parameter of writeMethod) and a supplementary support
     * for declared accessors. In addition to this, the new implementation was needed as the
     * package {@code java.beans} is absent from the Android's Java API implementation.
     *
     * @param methods the methods to parse
     * @return all the property descriptors found
     */
    private static Collection<PropertyDescriptor> getPropertyDescriptors(Method[] methods) {
        List<PropertyDescriptor> desciptorsHolder = new ArrayList<PropertyDescriptor>();

        // Collects writeMetod and readMethod
        for (Method method : methods) {
            if (isStatic(method) || method.isSynthetic()) {
                continue;
            }

            String name = method.getName();
            if (method.getParameterTypes().length == 0) {
                // Getter
                if (name.length() > 3 && name.startsWith("get") && method.getReturnType() != void.class) {
                    PropertyDescriptor info = new PropertyDescriptor();
                    info.name = uncapitalize(name.substring(3));
                    info.readMethod = method;
                    info.isGetter = true;
                    desciptorsHolder.add(info);
                }
                // Isser
                else if (name.length() > 2 && name.startsWith("is") && method.getReturnType() == boolean.class) {
                    PropertyDescriptor info = new PropertyDescriptor();
                    info.name = uncapitalize(name.substring(2));
                    info.readMethod = method;
                    info.isIsser = true;
                    desciptorsHolder.add(info);
                }
            } else if (method.getParameterTypes().length == 1) {
                // Setter
                if (name.length() > 3 && name.startsWith("set") && method.getReturnType() == void.class) {
                    PropertyDescriptor info = new PropertyDescriptor();
                    info.name = uncapitalize(name.substring(3));
                    info.writeMethod = method;
                    info.isSetter = true;
                    desciptorsHolder.add(info);
                }
            }
        }

        // Merges descriptors with the same name into a single entity
        Map<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();

        for (PropertyDescriptor descriptor : desciptorsHolder) {
            PropertyDescriptor instance = descriptors.get(descriptor.name);

            if (instance == null) {
                descriptors.put(descriptor.name, descriptor);
                instance = descriptor;
            }

            if (descriptor.isIsser) {
                instance.readMethod = descriptor.readMethod;
            } else if (descriptor.isGetter) {
                // if both getter and isser methods are present as descriptors,
                // the isser is chose as readMethod, and the getter discarded
                if (instance.readMethod == null) {
                    instance.readMethod = descriptor.readMethod;
                }
            } else if (descriptor.isSetter) {
                instance.writeMethod = descriptor.writeMethod;
            }
        }

        return descriptors.values();
    }

    /**
     * Internal: Converts a string to the normal Java variable
     * name capitalization.
     *
     * <p>This normally means converting the first character
     * from upper case to lower case, but in the (unusual) special
     * case when there is more than one character and both the first
     * and second characters are upper case, the string is returned
     * immutate.
     *
     * <p>Thus "SomeVariable" becomes "someVariable" and "X" becomes
     * "x", but "URL" stays as "URL".
     *
     * @param name The string to be uncapitolized
     * @return The uncapitolized version of the string
     * @see java.beans.Introspector#decapitalize(String)
     */
    private static String uncapitalize(String name) {
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1))
                && Character.isUpperCase(name.charAt(0))) {
            return name;
        }

        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);

        return new String(chars);
    }

    /**
     * Internal: Shortcut for {@link Modifier#isPrivate(int)} (int)}, {@code null}-safe.
     *
     * @param member the member to check
     * @return true if the member is private, else false
     */
    static boolean isPrivate(Member member) {
        return (member != null) && Modifier.isPrivate(member.getModifiers());
    }

    /**
     * Internal: Shortcut for {@link Modifier#isPublic(int)}, {@code null}-safe.
     *
     * @param member the member to check
     * @return true if the member is public, else false
     */
    static boolean isPublic(Member member) {
        return (member != null) && Modifier.isPublic(member.getModifiers());
    }

    /**
     * Internal: Shortcut for {@link Modifier#isStatic(int)}, {@code null}-safe.
     *
     * @param member the member to check
     * @return true if the member is static, else false
     */
    static boolean isStatic(Member member) {
        return (member != null) && Modifier.isStatic(member.getModifiers());
    }

    /**
     * Returns the type of the analized class or interface.
     *
     * @return the analized class or interface
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns a single public property of the analized class or interface,
     * if present, else {@code null}.
     *
     * @param propertyName the name of the property to retrieve
     * @return the {@link Property} searched, or {@code null} if not found
     * @throws NullPointerException if propertyName parameter is {@code null}
     */
    public Property getProperty(String propertyName) {
        if (propertyName == null) {
            throw new NullPointerException("Cannot get a property with a 'null' propertyName.");
        }

        return properties.get(propertyName);
    }

    /**
     * Returns a single declared property of the analized class or interface,
     * if present, else {@code null}.
     *
     * @param propertyName the name of the declared property to retrieve
     * @return the declared {@link Property} searched, or {@code null} if not found
     * @throws NullPointerException if propertyName parameter is {@code null}
     */
    public Property getDeclaredProperty(String propertyName) {
        if (propertyName == null) {
            throw new NullPointerException("Cannot get a declared property with a 'null' propertyName.");
        }

        return declaredProperties.get(propertyName);
    }

    /**
     * Returns an array containing {@link Property} objects reflecting all the
     * public properties of the analized class or interface.
     *
     * <p> The array includes those properties whose public {@link Method} accessors
     * are declared in the analized class or interface, or inherited from its
     * superclasses and superinterfaces.
     *
     * <p> The caller of this method is free to modify the returned array; it will
     * have no effect on the arrays returned to other callers.
     *
     * <p> The elements in the returned array are not sorted and are not in any
     * particular order.
     *
     * @return an array with a shallow copy of all the public properties of this bean
     */
    public Property[] getProperties() {
        return properties.values().toArray(new Property[properties.size()]);
    }

    /**
     * Returns an array containing {@link Property} objects reflecting all the
     * declared properties of the class or interface.
     *
     * <p> The array will include properties whose {@link Method} accessors are
     * public, protected, default (package) access, and private, but exclude
     * inherited accessors.
     *
     * <p> The caller of this method is free to modify the returned array; it will
     * have no effect on the arrays returned to other callers.
     *
     * <p> The elements in the returned array are not sorted and are not in any
     * particular order.
     *
     * @return an array with a shallow copy of all the declared properties of this bean
     */
    public Property[] getDeclaredProperties() {
        return declaredProperties.values().toArray(new Property[declaredProperties.size()]);
    }

    /**
     * Compares this {@code Bean} against the specified object.
     *
     * <p> Returns true if the objects are the same. Two {@code Bean} are the
     * same if they analized the same class or interface.
     *
     * @param obj the object to compare
     * @return true if the objects are the same
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Bean<?> other = (Bean<?>) obj;
        return type.equals(other.type);
    }

    /**
     * Returns a hashcode for this {@code Bean}.
     *
     * @return this bean's hashcode
     */
    @Override
    public int hashCode() {
        return type.getName().hashCode();
    }

    /**
     * Returns a textual rapresentation of this bean.
     *
     * <p> The string representation is the string "bean",
     * followed by a space, and then by the fully qualified name of
     * the type analized in the format returned by {@code getName}.
     *
     * @return a string representation of this bean object
     */
    @Override
    public String toString() {
        return "bean " + type.getName();
    }

    /**
     * Internal: Returns an optimized (memory-wise) version of a map.
     *
     * <p> {@link Collections.EmptyMap} has near-zero memory
     * allocation, while {@link Collections.SingletonMap} has
     * a faster {@link Map#get(Object)} lookup for maps with only a
     * single entry.
     *
     * @param map the map to check
     * @return the optimized {@link Map} (could be immutable)
     */
    static <K, V> Map<K, V> optimizeMap(Map<K, V> map) {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        } else if (map.size() == 1) {
            Map.Entry<K, V> entry = map.entrySet().iterator().next();
            return Collections.singletonMap(entry.getKey(), entry.getValue());
        } else {
            return map;
        }
    }

    /**
     * Internal: Custom dataholder.
     */
    private final static class PropertyDescriptor {
        public String name;
        public Method readMethod;
        public Method writeMethod;
        public boolean isIsser;
        public boolean isGetter;
        public boolean isSetter;
    }
}
