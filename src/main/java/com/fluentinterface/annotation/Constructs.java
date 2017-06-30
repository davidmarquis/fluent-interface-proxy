package com.fluentinterface.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that can be used to mark a builder method to be used as a constructor invocation instead of a setter. This
 * is useful when your target bean only has non-empty constructors.
 */
@Retention(value = RUNTIME)
@Target(value = {METHOD})
public @interface Constructs {
}
