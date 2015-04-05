package com.madebyatomicrobot.vinyl.samples.returntypes;

import android.database.MatrixCursor;

import org.junit.Assert;
import org.junit.Test;

public class ReturnTypesTest {
    // Same array as ReturnTypesRecord.PROJECTION
    private static final String[] COLUMNS = {
            "booleanType",
            "shortType",
            "intType",
            "longType",
            "floatType",
            "doubleType",
            "blobType",
            "stringType",
            "booleanObject",
            "shortObject",
            "integerObject",
            "longObject",
            "floatObject",
            "doubleObject"};

    @Test
    public void testCursorReturnTypes() {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        cursor.newRow()
                .add(1) // Cursors don't support booleans
                .add(Short.MAX_VALUE)
                .add(Integer.MAX_VALUE)
                .add(Long.MAX_VALUE)
                .add(Float.MAX_VALUE)
                .add(Double.MAX_VALUE)
                .add("bytes".getBytes())
                .add("string")
                .add(1) // Cursors don't support booleans
                .add(41)
                .add(42)
                .add(43)
                .add(1.23)
                .add(9.87);

        cursor.moveToFirst();
        ReturnTypes returnTypes = ReturnTypesRecord.wrapCursor(cursor);

        Assert.assertEquals(true, returnTypes.booleanType());
        Assert.assertEquals(Short.MAX_VALUE, returnTypes.shortType());
        Assert.assertEquals(Integer.MAX_VALUE, returnTypes.intType());
        Assert.assertEquals(Long.MAX_VALUE, returnTypes.longType());
        Assert.assertEquals(Float.MAX_VALUE, returnTypes.floatType(), 0);
        Assert.assertEquals(Double.MAX_VALUE, returnTypes.doubleType(), 0);
        Assert.assertEquals("bytes", new String(returnTypes.blobType()));
        Assert.assertEquals("string", returnTypes.stringType());

        Assert.assertEquals(Boolean.TRUE, returnTypes.booleanObject());
        Assert.assertEquals(Short.valueOf((short) 41), returnTypes.shortObject());
        Assert.assertEquals(Integer.valueOf(42), returnTypes.integerObject());
        Assert.assertEquals(Long.valueOf(43), returnTypes.longObject());
        Assert.assertEquals(Float.valueOf(1.23f), returnTypes.floatObject());
        Assert.assertEquals(Double.valueOf(9.87), returnTypes.doubleObject());
    }

    @Test
    public void testNullValues() {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        cursor.newRow()
                .add(1) // Cursors don't support booleans
                .add(Short.MAX_VALUE)
                .add(Integer.MAX_VALUE)
                .add(Long.MAX_VALUE)
                .add(Float.MAX_VALUE)
                .add(Double.MAX_VALUE)
                .add(null)
                .add(null)
                .add(null) // Cursors don't support booleans
                .add(null)
                .add(null)
                .add(null)
                .add(null)
                .add(null)
        ;

        cursor.moveToFirst();
        ReturnTypes returnTypes = ReturnTypesRecord.wrapCursor(cursor);

        Assert.assertNull(returnTypes.blobType());
        Assert.assertNull(returnTypes.stringType());

        Assert.assertNull(returnTypes.booleanObject());
        Assert.assertNull(returnTypes.shortObject());
        Assert.assertNull(returnTypes.integerObject());
        Assert.assertNull(returnTypes.longObject());
        Assert.assertNull(returnTypes.floatObject());
        Assert.assertNull(returnTypes.doubleObject());
    }
}
