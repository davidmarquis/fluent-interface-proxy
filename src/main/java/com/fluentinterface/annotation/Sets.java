package com.fluentinterface.annotation;

import com.fluentinterface.proxy.CoerceValueConverter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that can be used to customize how a given setter method will be handled by the library.
 */
@Retention(value = RUNTIME)
@Target(value = {METHOD})
public @interface Sets {
    /**
     * @return the name of the target property to set.
     * If not provided, the library will attempt to guess the property from the annotated method's name instead.
     */
    String property() default "";

    /**
     * @return a custom value conversion function to use when setting the input value on the target bean.
     */
    Class<? extends Function> via() default CoerceValueConverter.class;
}
