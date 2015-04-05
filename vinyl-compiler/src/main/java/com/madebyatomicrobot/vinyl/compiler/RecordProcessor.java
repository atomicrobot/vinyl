package com.madebyatomicrobot.vinyl.compiler;

import com.google.auto.service.AutoService;
import com.madebyatomicrobot.vinyl.annotations.Record;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

@AutoService(Processor.class)
public class RecordProcessor extends AbstractProcessor {
    private Elements elements;
    private Types types;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elements = env.getElementUtils();
        types = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new LinkedHashSet<>();
        supportedTypes.add(Record.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        processRecords(env);
        return true;
    }

    private void processRecords(RoundEnvironment env) {
        for (Element record : env.getElementsAnnotatedWith(Record.class)) {
            try {
                processRecord((TypeElement) record);
            } catch (IOException ex) {
                error("Unable to process " + record.asType().getKind().name());
            }
        }
    }

    private void processRecord(TypeElement record) throws IOException {
        try {
            RecordParser parser = new RecordParser(elements, types, record);
            RecordFields parsed = parser.parse();

            RecordWriter writer = new RecordWriter(parsed);
            writer.writeJava(filer);
        } catch (Exception ex) {
            printException(ex);
        }
    }

    private void printException(Exception ex) {
        error(ExceptionUtil.printThrowable(ex));
    }

    private void error(String error) {
        getMessager().printMessage(Kind.ERROR, error);
    }

    private Messager getMessager() {
        return processingEnv.getMessager();
    }
}
