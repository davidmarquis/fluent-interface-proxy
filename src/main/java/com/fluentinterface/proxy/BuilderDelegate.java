package com.fluentinterface.proxy;

import java.lang.reflect.Method;

/**
 * This interface allows for using a custom "Builder" base interface in your project. Please note that a default
 * BuilderDelegate implementation is already provided. You do not need to provide a custom delegate if you use
 * the provided Builder interface as your own builders' super interface (which is recommended).
 * <p>
 * Use this delegate when creating your dynamic builders.
 * </p>
 *
 * Ex:
 * <pre>
 * ReflectionBuilder.implementationFor(YourBuilder.class)
 *      .withDelegate(new YourDelegate())
 *      .create();
 * </pre>
 *
 * @param <B> Type for builders the delegate handles.
 */
public interface BuilderDelegate<B> {

    /**
     * Implementation should determine which type of object the provided Builder builds.
     * Typically this would be determined via the Reflection API.
     * @param builderInterface interface of the builder being created.
     * @return the type of object that the Builder builds.
     */
    Class<?> getClassBuiltBy(Class<?> builderInterface);

    /**
     * Implementation has to call the right method on the target builder in order to create an instance of
     * the object being built.
     * @param builder the builder which is being asked to build an object.
     * @return the built object.
     */
    Object build(B builder);

    /**
     * @param value when a builder sets values on the target object, it will ask the builder delegate to determine if
     * the value is a 'builder'. If it is, then it will be asked to be built and the result will be set on the target object
     * (the object being created by the dynamic builder).
     * @return whether the provided object is a Builder or not.
     */
    boolean isBuilderInstance(Object value);

    /**
     * Implementation has to determine if the provided Method is the actual 'build' method. That is, the method that
     * builds the final object.
     * @param method a method that is being called.
     * @return whether the provided method is the 'build' method or not.
     */
    boolean isBuildMethod(Method method);
}
