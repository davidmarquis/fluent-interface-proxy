package com.fluentinterface.proxy;

/**
 * This interface allows to use a different strategy for delegating builders.
 *
 * @param <B> Type for builders the delegate handles.
 * @see com.fluentinterface.builder.Builder
 */
public interface BuilderDelegate<B> {
    /**
     * Implementation has to call the right method on the target builder in order to create an instance of
     * the object being built.
     * @param builder
     * @return the built object.
     */
    public Object build(B builder);

    /**
     * @param value when a builder set values on the target object, it will ask the builder delegate to determine if
     * the value is a 'builder'. If it is, then it will be asked to be built and the result will be set on the target object
     * (the object being created by the dynamic builder).
     * @return whether the provided object is a Builder or not.
     */
    public boolean isBuilderInstance(Object value);
}
