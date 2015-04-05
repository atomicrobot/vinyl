package com.madebyatomicrobot.vinyl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker to indicate that this interface describes a record that exists in a
 * <a href="http://developer.android.com/reference/android/database/Cursor.html">Cursor</a> or
 * <a href="http://developer.android.com/reference/android/content/ContentValues.html">ContentValues</a>.
 *
 * This can only be applied to interfaces!
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Record {

    /**
     * Indicates that fields in a cursor will always match the order provided by the generated record
     * <code>projection</code> method. Accessing ordered fields is slightly more performant.
     *
     * Default is <code>false</code>.
     * @return <code>true</code> if order matches the specified projection, <code>false</code> otherwise.
     */
    boolean ordered() default false;
}
