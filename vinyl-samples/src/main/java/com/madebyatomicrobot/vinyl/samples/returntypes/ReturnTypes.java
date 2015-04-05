package com.madebyatomicrobot.vinyl.samples.returntypes;

import com.madebyatomicrobot.vinyl.annotations.Record;

@Record
public interface ReturnTypes {
    boolean booleanType();

    short shortType();

    int intType();

    long longType();

    float floatType();

    double doubleType();

    byte[] blobType();

    String stringType();

    Boolean booleanObject();

    Short shortObject();

    Integer integerObject();

    Long longObject();

    Float floatObject();

    Double doubleObject();
}
