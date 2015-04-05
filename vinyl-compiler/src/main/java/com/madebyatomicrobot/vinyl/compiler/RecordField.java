package com.madebyatomicrobot.vinyl.compiler;

import com.squareup.javapoet.TypeName;

class RecordField {
    final boolean primitive;
    final TypeName typeName;
    final String cursorAccessorFormat;
    final boolean notNull;
    final String name;
    final RecordProjection projection;

    final TypeName converter;

    public RecordField(
            boolean primitive,
            TypeName typeName,
            String cursorAccessorFormat,
            boolean notNull,
            String name,
            RecordProjection projection,
            TypeName converter) {
        this.primitive = primitive;
        this.typeName = typeName;
        this.cursorAccessorFormat = cursorAccessorFormat;
        this.notNull = notNull;
        this.name = name;
        this.projection = projection;
        this.converter = converter;
    }
}
