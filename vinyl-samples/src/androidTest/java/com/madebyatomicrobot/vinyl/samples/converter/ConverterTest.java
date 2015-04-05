package com.madebyatomicrobot.vinyl.samples.converter;

import android.database.MatrixCursor;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class ConverterTest {
    private static final String[] COLUMNS = { "time" };

    @Test
    public void testConvertToDate() {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        cursor.newRow().add(1438387200); // April 1, 2015

        cursor.moveToFirst();

        Epoch epoch = EpochRecord.wrapCursor(cursor);
        Date time = epoch.time();
        Assert.assertEquals(1438387200, time.getTime());
    }
}
