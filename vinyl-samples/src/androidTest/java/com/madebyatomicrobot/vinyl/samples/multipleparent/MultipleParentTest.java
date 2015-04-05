package com.madebyatomicrobot.vinyl.samples.multipleparent;

import android.database.MatrixCursor;

import org.junit.Assert;
import org.junit.Test;

public class MultipleParentTest {
    @Test
    public void testMethodsExist() {
        // It is worth noting that the order of the cursor columns are different and will vary
        // based on how the type hierarchy is parsed.  This is why using the generated projection
        // is a requirement.
        MatrixCursor childCursor = new MatrixCursor(new String[] {"child", "dad", "mom"});
        childCursor.newRow()
                .add("the child")
                .add("the dad")
                .add("the mom");

        childCursor.moveToFirst();
        Child child = ChildRecord.wrapCursor(childCursor);
        Assert.assertEquals("the mom", child.mom());
        Assert.assertEquals("the dad", child.dad());
        Assert.assertEquals("the child", child.child());
    }
}
