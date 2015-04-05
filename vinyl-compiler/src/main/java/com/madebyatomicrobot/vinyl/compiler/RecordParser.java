package com.madebyatomicrobot.vinyl.compiler;

import com.madebyatomicrobot.vinyl.annotations.Converter;
import com.madebyatomicrobot.vinyl.annotations.Projection;
import com.madebyatomicrobot.vinyl.annotations.Record;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

class RecordParser {
    private static final String CONVERTER_CLASS_NAME = Converter.class.getSimpleName();

    private static final String CONVERTER_FIELD_CLASS_VALUE = "fieldClass";
    private static final String CONVERTER_CLASS_VALUE = "converter";
    private static final String CONVERTER_CONVERT_TO_METHODNAME = "convertTo";
    private static final String CONVERTER_CONVERT_FROM_METHODNAME = "convertFrom";

    private static final String PROJECTION_CONDITIONAL_CLASS_VALUE = "conditionalProjection";

    private final Elements elements;
    private Types types;
    private final TypeElement record;

    RecordParser(Elements elements, Types types, TypeElement record) {
        this.elements = elements;
        this.types = types;
        this.record = record;
    }

    RecordFields parse() {
        String packageName = elements.getPackageOf(record).getQualifiedName().toString();
        String typeName = record.getSimpleName().toString();
        boolean ordered = record.getAnnotation(Record.class).ordered();
        RecordFields parsed = new RecordFields(packageName, typeName, ordered);
        parseTypeElement(parsed, record);
        return parsed;
    }

    private void parseTypeElement(RecordFields parsed, TypeElement typeElement) {
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind().equals(ElementKind.METHOD)) {
                ExecutableElement executableElement = (ExecutableElement) element;
                if (!executableElement.getParameters().isEmpty()) {
                    parseError(String.format("%s is not a no-arg method.", element.getSimpleName()));
                }

                parsed.fields.add(parseRecordField(parsed, executableElement));
            }
        }

        parseParentInterfaces(parsed, typeElement);
    }

    private RecordField parseRecordField(RecordFields parsed, ExecutableElement executableElement) {
        TypeMirror typeMirror = executableElement.getReturnType();
        SupportedType supportedType = getSupportedType(typeMirror);

        String methodName = executableElement.getSimpleName().toString();
        RecordProjection projection = getProjection(parsed, executableElement);
        boolean annotatedWithNotNull = isAnnotatedWithNotNull(executableElement);
        if (supportedType == null) {
            return parseRecordConvertableField(executableElement, methodName, projection, annotatedWithNotNull);
        } else {
            return new RecordField(
                    supportedType.primitive,
                    supportedType.typeName,
                    supportedType.cursorAccessorFormat,
                    annotatedWithNotNull,
                    methodName,
                    projection,
                    null);
        }
    }

    private RecordField parseRecordConvertableField(
            ExecutableElement executableElement,
            String methodName,
            RecordProjection projection,
            boolean notNull) {
        List<? extends AnnotationMirror> annotationMirrors = executableElement.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            if (annotationMirror.getAnnotationType().toString().equals(Converter.class.getName())) {
                return parseRecordConvertibleField(
                        executableElement,
                        methodName,
                        projection,
                        notNull,
                        annotationMirror);
            }
        }

        return null;
    }

    private RecordField parseRecordConvertibleField(
            ExecutableElement executableElement,
            String methodName,
            RecordProjection projection,
            boolean notNull,
            AnnotationMirror annotationMirror) {
        TypeMirror returnTypeMirror = executableElement.getReturnType();
        TypeName typeName = ClassName.get(returnTypeMirror);

        DeclaredType converterClass = extractDeclaredType(annotationMirror, CONVERTER_CLASS_VALUE);
        if (converterClass == null) {
            parseError(String.format(
                    "%s: Unable to read the %s attribute on the %s annotation",
                    methodName,
                    CONVERTER_CLASS_VALUE,
                    CONVERTER_CLASS_NAME));
        }

        DeclaredType cursorClass = extractDeclaredType(annotationMirror, CONVERTER_FIELD_CLASS_VALUE);
        if (cursorClass == null) {
            parseError(String.format(
                    "%s: Unable to read the %s attribute on the %s annotation",
                    methodName,
                    CONVERTER_FIELD_CLASS_VALUE,
                    CONVERTER_CLASS_NAME));
        }

        SupportedType cursorType = getSupportedType(cursorClass.asElement().asType());
        if (cursorType == null) {
            parseError(String.format(
                    "%s: %s is not a supported cursor type",
                    methodName,
                    CONVERTER_FIELD_CLASS_VALUE));
        }

        assertConverterMeetsExpectations(converterClass, returnTypeMirror, cursorClass);
        return new RecordField(
                false,
                typeName,
                cursorType.cursorAccessorFormat,
                notNull,
                methodName,
                projection,
                ClassName.get(converterClass));
    }

    private void assertConverterMeetsExpectations(
            DeclaredType converterClass,
            TypeMirror returnTypeMirror,
            DeclaredType cursorClass) {
        TypeElement typeElement = (TypeElement) converterClass.asElement();
        boolean defaultConstructorAvailable = false;
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind().equals(ElementKind.CONSTRUCTOR)) {
                ExecutableElement constructor = (ExecutableElement) element;
                if (constructor.getParameters().size() == 0) {
                    defaultConstructorAvailable = true;
                }
            }
        }

        if (!defaultConstructorAvailable) {
            parseError(String.format(
                    "%s must have a visible default constructor",
                    converterClass.toString()));
        }

        boolean convertToFound = false;
        boolean convertFromFound = false;
        for (Element converterElement : converterClass.asElement().getEnclosedElements()) {
            if (converterElement.getKind().equals(ElementKind.METHOD)) {
                ExecutableElement converterMethod = (ExecutableElement) converterElement;
                String converterMethodName = converterMethod.getSimpleName().toString();

                if (CONVERTER_CONVERT_TO_METHODNAME.equals(converterMethodName)) {
                    List<? extends VariableElement> parameters = converterMethod.getParameters();
                    if (parameters.size() == 1) {
                        VariableElement parameter = parameters.get(0);
                        if (parameter.asType().equals(returnTypeMirror)) {
                            if (converterMethod.getReturnType().equals(cursorClass.asElement().asType())) {
                                convertToFound = true;
                            }
                        }
                    }
                }

                if (CONVERTER_CONVERT_FROM_METHODNAME.equals(converterMethodName)) {
                    List<? extends VariableElement> parameters = converterMethod.getParameters();
                    if (parameters.size() == 1) {
                        VariableElement parameter = parameters.get(0);
                        if (parameter.asType().equals(cursorClass.asElement().asType())) {
                            if (converterMethod.getReturnType().equals(returnTypeMirror)) {
                                convertFromFound = true;
                            }
                        }
                    }
                }
            }
        }

        if (!convertToFound) {
            String convertToSignature = String.format(
                    "public %s %s(%s value)",
                    returnTypeMirror.toString(),
                    CONVERTER_CONVERT_TO_METHODNAME,
                    cursorClass.asElement().asType().toString());
            parseError(String.format(
                    "%s being used without this method: %s",
                    converterClass.toString(),
                    convertToSignature));
        }

        if (!convertFromFound) {
            String convertFromSignature = String.format(
                    "public %s %s(%s value)",
                    cursorClass.asElement().asType().toString(),
                    CONVERTER_CONVERT_FROM_METHODNAME,
                    returnTypeMirror.toString());
            parseError(String.format(
                    "%s being used without this method: %s",
                    converterClass.toString(),
                    convertFromSignature));
        }
    }

    private void parseParentInterfaces(RecordFields parsed, TypeElement typeElement) {
        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        for (TypeMirror typeMirror : interfaces) {
            TypeElement parentElement = (TypeElement) types.asElement(typeMirror);
            parseTypeElement(parsed, parentElement);
        }
    }

    private SupportedType getSupportedType(final TypeMirror typeMirror) {
        for (SupportedType supportedType : SupportedType.values()) {
            if (supportedType.isSupported(typeMirror)) {
                return supportedType;
            }
        }

        return null;
    }

    private RecordProjection getProjection(RecordFields parsed, ExecutableElement executableElement) {
        String methodName = executableElement.getSimpleName().toString();
        Projection projectionAnnotation = executableElement.getAnnotation(Projection.class);
        String explicitProjection = (projectionAnnotation != null) ? projectionAnnotation.value() : null;
        if ("".equals(explicitProjection)) {
            explicitProjection = null;
        }

        TypeName conditionalProjection = null;
        List<? extends AnnotationMirror> annotationMirrors = executableElement.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            if (annotationMirror.getAnnotationType().toString().equals(Projection.class.getName())) {
                DeclaredType declaredType = extractDeclaredType(annotationMirror, PROJECTION_CONDITIONAL_CLASS_VALUE);
                if (declaredType != null) {
                    if (!declaredType.asElement().getKind().equals(ElementKind.CLASS)) {
                        parseError("must be a class");
                    }

                    conditionalProjection = ClassName.get((TypeElement) declaredType.asElement());
                }
            }
        }

        final RecordProjection projection;
        if (projectionAnnotation != null) {
            if (explicitProjection == null && conditionalProjection == null) {
                projection = null;
                parseError(String.format(
                        "%s is annotated with @%s but is missing either a projection literal or a %s class reference",
                        methodName,
                        Projection.class.getSimpleName(),
                        PROJECTION_CONDITIONAL_CLASS_VALUE
                ));
            } else if (explicitProjection != null && conditionalProjection != null) {
                projection = null;
                parseError(String.format(
                        "%s is annotated with @%s and should only have a projection literal or a %s class reference",
                        methodName,
                        Projection.class.getSimpleName(),
                        PROJECTION_CONDITIONAL_CLASS_VALUE
                ));
            } else {
                if (explicitProjection != null) {
                    projection = new RecordProjection(explicitProjection, null);
                } else {
                    projection = new RecordProjection(null, conditionalProjection);
                }
            }
        } else {
            projection = new RecordProjection(methodName, null);
        }

        if (isDuplicateProjection(parsed, projection)) {
            parseError(String.format(
                    "%s would use a projection (%s) that has already been defined",
                    methodName,
                    projection));
        }

        return projection;
    }

    private boolean isDuplicateProjection(RecordFields parsed, RecordProjection projection) {
        for (RecordField field : parsed.fields) {
            // We can only realistically scan against simple projections
            if (projection.simpleProjection != null && field.projection.simpleProjection != null) {
                if (projection.simpleProjection.equalsIgnoreCase(field.projection.simpleProjection)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isAnnotatedWithNotNull(ExecutableElement executableElement) {
        List<? extends AnnotationMirror> annotationMirrors = executableElement.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            if ("@android.support.annotation.NonNull".equals(annotationMirror.toString())) {
                return true;
            }
        }
        return false;
    }


    private static DeclaredType extractDeclaredType(AnnotationMirror annotationMirror, String valueName) {
        return extractValue(annotationMirror, valueName, DeclaredType.class);
    }

    private static <T> T extractValue(AnnotationMirror annotationMirror, String valueName, Class<T> expectedType) {
        Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<>(annotationMirror.getElementValues());
        for (Entry<ExecutableElement, AnnotationValue> entry : elementValues.entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(valueName)) {
                Object value = entry.getValue().getValue();
                return expectedType.cast(value);
            }
        }
        return null;
    }

    private void parseError(String message) {
        throw new IllegalArgumentException(String.format(
                "%s: %s",
                record.getSimpleName().toString(),
                message));
    }
}
