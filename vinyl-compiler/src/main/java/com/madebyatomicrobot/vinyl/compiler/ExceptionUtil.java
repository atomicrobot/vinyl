package com.madebyatomicrobot.vinyl.compiler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class ExceptionUtil {
    public static String printThrowable(Throwable throwable) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        throwable.printStackTrace(printStream);
        return new String(outputStream.toByteArray());
    }

    private ExceptionUtil() {

    }
}
