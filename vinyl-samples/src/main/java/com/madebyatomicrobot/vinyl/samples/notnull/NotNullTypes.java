package com.madebyatomicrobot.vinyl.samples.notnull;

import android.support.annotation.NonNull;

import com.madebyatomicrobot.vinyl.annotations.Record;

// methods that return objects by default expect Nullable types
@Record
public interface NotNullTypes {
    @NonNull
    byte[] blobType();

    @NonNull
    String stringType();

    @NonNull
    Boolean booleanObject();

    @NonNull
    Short shortObject();

    @NonNull
    Integer integerObject();

    @NonNull
    Long longObject();

    @NonNull
    Float floatObject();

    @NonNull
    Double doubleObject();
}
