package com.madebyatomicrobot.vinyl.compiler;

import com.squareup.javapoet.TypeName;

public class RecordProjection {
    final String simpleProjection;
    final TypeName conditionalProjection;

    public RecordProjection(String simpleProjection, TypeName conditionalProjection) {
        this.simpleProjection = simpleProjection;
        this.conditionalProjection = conditionalProjection;
    }
}
