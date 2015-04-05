package com.madebyatomicrobot.vinyl.compiler;

import java.util.ArrayList;
import java.util.List;

class RecordFields {
    final String packageName;
    final String typeName;
    final boolean ordered;
    final List<RecordField> fields = new ArrayList<>();

    public RecordFields(String packageName, String typeName, boolean ordered) {
        this.packageName = packageName;
        this.typeName = typeName;
        this.ordered = ordered;
    }

    String getQualifiedAnnotatedClassName() {
        return String.format("%s.%s", packageName, typeName);
    }
}
