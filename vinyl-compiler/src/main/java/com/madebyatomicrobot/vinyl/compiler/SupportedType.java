package com.madebyatomicrobot.vinyl.compiler;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public enum SupportedType {
    BOOLEAN_PRIMITIVE(true, TypeName.BOOLEAN, "(cursor.getShort(columnIndex) != 0)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, TypeKind.BOOLEAN);
        }
    },
    SHORT_PRIMITIVE(true, TypeName.SHORT, "cursor.getShort(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, TypeKind.SHORT);
        }
    },
    INT_PRIMITIVE(true, TypeName.INT, "cursor.getInt(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, TypeKind.INT);
        }
    },
    LONG_PRIMITIVE(true, TypeName.LONG, "cursor.getLong(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, TypeKind.LONG);
        }
    },
    FLOAT_PRIMITIVE(true, TypeName.FLOAT, "cursor.getFloat(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, TypeKind.FLOAT);
        }
    },
    DOUBLE_PRIMITIVE(true, TypeName.DOUBLE, "cursor.getDouble(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, TypeKind.DOUBLE);
        }
    },
    BOOLEAN_OBJECT(false, ClassName.get(Boolean.class), "(cursor.getShort(columnIndex) != 0)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, "java.lang.Boolean");
        }
    },
    SHORT_OBJECT(false, ClassName.get(Short.class), "cursor.getShort(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, "java.lang.Short");
        }
    },
    INTEGER_OBJECT(false, ClassName.get(Integer.class), "cursor.getInt(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, "java.lang.Integer");
        }
    },
    LONG_OBJECT(false, ClassName.get(Long.class), "cursor.getLong(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, "java.lang.Long");
        }
    },
    FLOAT_OBJECT(false, ClassName.get(Float.class), "cursor.getFloat(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, "java.lang.Float");
        }
    },
    DOUBLE_OBJECT(false, ClassName.get(Double.class), "cursor.getDouble(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, "java.lang.Double");
        }
    },
    STRING(false, ClassName.get(String.class), "cursor.getString(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, "java.lang.String");
        }
    },
    BYTE_ARRAY(false, ArrayTypeName.of(TypeName.BYTE), "cursor.getBlob(columnIndex)") {
        @Override
        public boolean isSupported(TypeMirror typeMirror) {
            return isSupported(typeMirror, "byte[]");
        }
    };

    public final boolean primitive;
    public final TypeName typeName;
    public final String cursorAccessorFormat;

    SupportedType(boolean primitive, TypeName typeName, String cursorAccessorFormat) {
        this.primitive = primitive;
        this.typeName = typeName;
        this.cursorAccessorFormat = cursorAccessorFormat;
    }

    public abstract boolean isSupported(TypeMirror typeMirror);

    boolean isSupported(TypeMirror typeMirror, TypeKind typeKind) {
        return typeMirror.getKind().equals(typeKind);
    }

    boolean isSupported(TypeMirror typeMirror, String className) {
        return typeMirror.toString().equals(className);
    }
}
