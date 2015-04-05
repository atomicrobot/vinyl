package com.madebyatomicrobot.vinyl.compiler;

import com.google.common.base.Joiner;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

class RecordWriter {
    private final ClassName cursorClassName = ClassName.bestGuess("android.database.Cursor");
    private final ClassName contentValuesClassName = ClassName.bestGuess("android.content.ContentValues");
    private final ClassName notNullClassName = ClassName.bestGuess("android.support.annotation.NonNull");

    private final ClassName contentValuesBuilderClassName = ClassName.bestGuess("ContentValuesBuilder");

    private final RecordFields parsed;
    private final ClassName recordQualifiedClassName;
    private final String generatedClassName;
    private final ClassName generatedCursorProxyName;
    private final ClassName generatedCursorPojoName;

    private final TypeSpec projectionsClass;
    private final FieldSpec projectionField;
    private final MethodSpec projectionMethod;

    private final TypeSpec cursorProxyClass;
    private final TypeSpec cursorPojoClass;
    private final MethodSpec wrapCursor;
    private final MethodSpec buildFromCursor;

    private final TypeSpec contentValuesBuilder;
    private final MethodSpec contentValuesBuilderFactory;

    RecordWriter(RecordFields parsed) {
        this.parsed = parsed;

        recordQualifiedClassName = ClassName.bestGuess(parsed.getQualifiedAnnotatedClassName());
        generatedClassName = String.format("%sRecord", parsed.typeName);
        generatedCursorProxyName = ClassName.bestGuess("CursorProxy");
        generatedCursorPojoName = ClassName.bestGuess(String.format("%sPojo", parsed.typeName));

        projectionsClass = buildProjectionsClass();
        projectionField = buildProjectionField();
        projectionMethod = buildProjectionMethod();

        cursorProxyClass = buildCursorProxyClass();
        cursorPojoClass = buildCursorPojoClass();
        wrapCursor = buildWrapCursor();
        buildFromCursor = buildBuildFromCursor();

        contentValuesBuilder = buildContentValuesBuilder();
        contentValuesBuilderFactory = builderContentValuesBuilderFactory();
    }

    void writeJava(Filer filer) throws IOException {
        JavaFile.builder(parsed.packageName, buildTypeSpec()).build().writeTo(filer);
    }

    private TypeSpec buildTypeSpec() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(generatedClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(projectionField)
                .addMethod(projectionMethod)
                .addType(projectionsClass)
                .addType(cursorProxyClass)
                .addType(cursorPojoClass)
                .addMethod(wrapCursor)
                .addMethod(buildFromCursor)
                .addMethod(contentValuesBuilderFactory)
                .addType(contentValuesBuilder)
                .addJavadoc("Generated record class for {@link $N}", parsed.typeName);

        for (RecordField field : parsed.fields) {
            if (field.converter != null) {
                builder.addField(FieldSpec.builder(field.converter, String.format("%sConverter", field.name))
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T()", field.converter)
                        .build());
            }

            TypeName conditionalProjection = field.projection.conditionalProjection;
            if (conditionalProjection != null) {
                builder.addField(
                        FieldSpec.builder(conditionalProjection, String.format("%sConditionalProjection", field.name))
                                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                                .initializer("new $T()", field.projection.conditionalProjection)
                                .build());
            }
        }

        return builder.build();
    }

    private FieldSpec buildProjectionField() {
        CodeBlock.Builder initializer = CodeBlock.builder();
        initializer.add("{");
        boolean first = true;
        for (RecordField field : parsed.fields) {
            if (!first) {
                initializer.add(", ");
            }
            initializer.add("$N.$L()", projectionsClass, field.name);
            first = false;
        }
        initializer.add("}");

        TypeName arrayOfStrings = ArrayTypeName.of(ClassName.get(String.class));
        return FieldSpec.builder(arrayOfStrings, "PROJECTION", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(initializer.build().toString())
                .build();
    }

    private MethodSpec buildProjectionMethod() {
        return MethodSpec.methodBuilder("projection")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ArrayTypeName.of(ClassName.get(String.class)))
                .addStatement("return $T.copyOf($N, $N.length)", Arrays.class, projectionField, projectionField)
                .addJavadoc("Provides an ordered set of field projections.")
                .build();
    }

    private TypeSpec buildProjectionsClass() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Projections")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addJavadoc("{@link $N} field projections.", parsed.typeName);
        for (RecordField field : parsed.fields) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(field.name)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ClassName.get(String.class));

            if (field.projection.simpleProjection != null) {
                methodBuilder.addStatement("return $S", field.projection.simpleProjection);
            } else {
                methodBuilder.addStatement("return $L.projection()",
                        String.format("%sConditionalProjection", field.name));
            }

            builder.addMethod(methodBuilder.build());
        }

        return builder.build();
    }

    private TypeSpec buildCursorProxyClass() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(generatedCursorProxyName.simpleName())
                .addSuperinterface(recordQualifiedClassName)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addField(cursorClassName, "cursor", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PRIVATE)
                                .addParameter(cursorClassName, "cursor")
                                .addStatement("this.cursor = cursor")
                                .build()
                );

        List<RecordField> fields = parsed.fields;
        for (int i = 0; i < fields.size(); i++) {
            RecordField field = fields.get(i);
            builder.addMethod(buildCursorProxyClassMethod(i, field));
        }

        return builder.build();
    }

    private TypeSpec buildCursorPojoClass() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE);
        for (RecordField field : parsed.fields) {
            constructor.addParameter(field.typeName, field.name);
            constructor.addStatement("this.$L = $L", field.name, field.name);
        }

        TypeSpec.Builder builder = TypeSpec.classBuilder(generatedCursorPojoName.simpleName())
                .addSuperinterface(recordQualifiedClassName)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addMethod(constructor.build());

        for (RecordField field : parsed.fields) {
            builder.addField(FieldSpec.builder(field.typeName, field.name, Modifier.PRIVATE, Modifier.FINAL).build());

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(field.name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(field.typeName)
                    .addStatement("return $L", field.name);

            if (field.notNull) {
                methodBuilder.addAnnotation(notNullClassName);
            }

            builder.addMethod(methodBuilder.build());
        }

        return builder.build();
    }

    private MethodSpec buildCursorProxyClassMethod(int index, RecordField field) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(field.name)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(field.typeName);

        if (field.notNull) {
            builder.addAnnotation(notNullClassName);
        }

        if (parsed.ordered) {
            builder.addStatement("int columnIndex = $L", index);
        } else {
            builder.addStatement("int columnIndex = cursor.getColumnIndex($N.$L())", projectionsClass, field.name);
        }

        String nullCheckStatement = (field.primitive || field.notNull) ? "" : "cursor.isNull(columnIndex) ? null : ";
        String cursorGetStatement = nullCheckStatement + field.cursorAccessorFormat;

        if (field.converter == null) {
            builder.addStatement("return $L", cursorGetStatement);
        } else {
            builder.addStatement("return $LConverter.convertFrom($L)", field.name, cursorGetStatement);
        }

        return builder.build();
    }

    private MethodSpec buildWrapCursor() {
        return MethodSpec.methodBuilder("wrapCursor")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(recordQualifiedClassName)
                .addParameter(ParameterSpec.builder(cursorClassName, "cursor").build())
                .addStatement("return new $T(cursor)", generatedCursorProxyName)
                .addJavadoc("Returns a {@link $N} implementation that only decorates a {@link $T}.",
                        parsed.typeName,
                        cursorClassName)
                .build();
    }

    private MethodSpec buildBuildFromCursor() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("buildFromCursor")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(recordQualifiedClassName)
                .addParameter(ParameterSpec.builder(cursorClassName, "cursor").build())
                .addJavadoc("Returns an instance of {@link $N} reflecting the current row of a {@link $T}.",
                        parsed.typeName,
                        cursorClassName);

        builder.addStatement("$T proxy = new $T(cursor)", generatedCursorProxyName, generatedCursorProxyName);

        List<Object> args = new ArrayList<>();
        args.add(cursorPojoClass);
        for (RecordField field : parsed.fields) {
            args.add(field.name);
        }
        String params = Joiner.on(", ").join(Collections.nCopies(parsed.fields.size(), "proxy.$L()"));
        builder.addStatement(String.format("return new $N(%s)", params), args.toArray());

        return builder.build();
    }

    private TypeSpec buildContentValuesBuilder() {
        FieldSpec field = FieldSpec.builder(contentValuesClassName, "contentValues", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", contentValuesClassName)
                .build();

        TypeSpec.Builder builder = TypeSpec.classBuilder(contentValuesBuilderClassName.simpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addField(field);

        builder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());

        addContentValuesBuilderMethods(builder);

        builder.addMethod(MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(contentValuesClassName)
                .addStatement("return contentValues")
                .build());

        return builder.build();
    }

    private void addContentValuesBuilderMethods(Builder builder) {
        for (RecordField field : parsed.fields) {
            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(field.typeName, field.name);
            if (field.notNull) {
                parameterBuilder.addAnnotation(notNullClassName);
            }

            MethodSpec.Builder fieldMethodBuilder = MethodSpec.methodBuilder(field.name)
                    .returns(contentValuesBuilderClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(parameterBuilder.build());

            if (field.converter == null) {
                fieldMethodBuilder.addStatement(
                        "contentValues.put($N.$L(), $L)",
                        projectionsClass,
                        field.name,
                        field.name);
            } else {
                fieldMethodBuilder.addStatement(
                        "contentValues.put($N.$L(), $LConverter.convertTo($L))",
                        projectionsClass,
                        field.name,
                        field.name,
                        field.name);
            }
            fieldMethodBuilder.addStatement("return this");

            builder.addMethod(fieldMethodBuilder.build());
        }
    }

    private MethodSpec builderContentValuesBuilderFactory() {
        return MethodSpec.methodBuilder("contentValuesBuilder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.bestGuess("ContentValuesBuilder"))
                .addStatement("return new $N()", contentValuesBuilder)
                .addJavadoc("Returns a {@link $T} builder for {@link $N).", contentValuesClassName, parsed.typeName)
                .build();
    }
}
