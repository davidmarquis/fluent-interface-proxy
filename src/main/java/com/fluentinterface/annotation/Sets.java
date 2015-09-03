package com.fluentinterface.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation allows developers to specify the target property associated to the annotated builder method.
 *
 *
 * For example:
 *
 * <pre>
 * public class PersonBuilder {
 *     &#064;Sets(property = "name")
 *     PersonBuilder named(String name);
 * }
 * </pre>
 */
@Retention(value = RUNTIME)
@Target(value = {METHOD})
public @interface Sets {
    /**
     * @return the target property
     */
    String property();
}
