package com.madebyatomicrobot.vinyl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides details for how to map between a field class and a desired class.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Converter {
    /**
     * Implementations must be instantiable, have a default constructor, and have these public methods defined.
     *
     * <ul>
     *     <li><code>public DesiredType convertFrom(FieldType value)</code></li>
     *     <li><code>public FieldType convertTo(DesiredType value)</code></li>
     * </ul>
     */
    Class<?> converter();

    /**
     * The type of field that would be accessed from a
     * <a href="http://developer.android.com/reference/android/database/Cursor.html">Cursor</a> or
     * put into a <a href="http://developer.android.com/reference/android/content/ContentValues.html">ContentValues</a>.
     *
     * This must be one of the supported non-primitive types.
     *
     * @return Backing field type.
     */
    Class<?> fieldClass();
}
