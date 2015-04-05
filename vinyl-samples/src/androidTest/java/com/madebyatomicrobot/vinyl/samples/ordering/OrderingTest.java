package com.madebyatomicrobot.vinyl.samples.ordering;

import android.database.MatrixCursor;

import org.junit.Assert;
import org.junit.Test;

public class OrderingTest {
    private static final String[] COLUMNS = { "one", "two", "three" };

    @Test
    public void testConvertToDate() {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        cursor.newRow().add(1).add(2).add(3);

        cursor.moveToFirst();

        Ordered ordered = OrderedRecord.wrapCursor(cursor);
        Unordered unordered = UnorderedRecord.wrapCursor(cursor);

        Assert.assertArrayEquals(new String[] { "one", "two", "three" }, OrderedRecord.projection());
        Assert.assertArrayEquals(new String[] { "three", "two", "one" }, UnorderedRecord.projection());

        Assert.assertEquals(1, ordered.one());
        Assert.assertEquals(1, unordered.one());

        Assert.assertEquals(2, ordered.two());
        Assert.assertEquals(2, unordered.two());

        Assert.assertEquals(3, ordered.three());
        Assert.assertEquals(3, unordered.three());
    }
}
