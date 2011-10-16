package com.fluentinterface.builder;

/**
 * This interface allows users of this API to use their own "builders" instead of using the provided super interface.
 *
 * @param <B> Type for builders the delegate handles.
 * @see Builder
 */
public interface BuilderDelegate<B> {
    /**
     * Implementation has to call the right method on the target builder in order to create an instance of
     * the object being built.
     * @param builder
     * @return the built object.
     */
    public Object build(B builder);
}
