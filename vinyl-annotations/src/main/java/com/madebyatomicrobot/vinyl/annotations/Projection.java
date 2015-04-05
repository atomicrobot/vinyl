package com.madebyatomicrobot.vinyl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies an explicit projection for a field. One of {@link #value()} or {@link #conditionalProjection()} must
 * be specified.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Projection {
    /**
     * Explicit field projection.
     * @return field projection value.
     */
    String value() default "";

    /**
     * The only time this should be used is if the projection to be used will not be a constant value but could vary
     * at runtime.
     *
     * The class reference provided must be instantiable, have a default constructor, and
     * implement {@link ConditionalProjection}.
     *
     * @return conditional runtime projection evaluator.
     */
    Class<? extends ConditionalProjection> conditionalProjection() default ConditionalProjection.class;
}
