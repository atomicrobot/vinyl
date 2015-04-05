package com.madebyatomicrobot.vinyl.samples.singleparent;

import android.database.MatrixCursor;

import org.junit.Assert;
import org.junit.Test;

public class SingleParentTest {
    @Test
    public void testMethodsExist() {
        // It is worth noting that the order of the cursor columns are different and will vary
        // based on how the type hierarchy is parsed.  This is why using the generated projection
        // is a requirement.
        MatrixCursor childCursor = new MatrixCursor(new String[] {"child", "parent"});
        childCursor.newRow()
                .add("the child")
                .add("the parent");

        MatrixCursor parentCursor = new MatrixCursor(new String[] {"parent"});
        parentCursor.newRow()
                .add("the parent");

        childCursor.moveToFirst();
        Child child = ChildRecord.wrapCursor(childCursor);
        Assert.assertEquals("the parent", child.parent());
        Assert.assertEquals("the child", child.child());

        parentCursor.moveToFirst();
        Parent parent = ParentRecord.wrapCursor(parentCursor);
        Assert.assertEquals("the parent", parent.parent());
    }
}
