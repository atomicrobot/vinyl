package com.madebyatomicrobot.vinyl.samples;

import android.database.MatrixCursor;

import org.junit.Assert;
import org.junit.Test;

public class PlanetTest {
    @Test
    public void testPojo() {
        MatrixCursor cursor = new MatrixCursor(new String[] {"_id", "name", "radius_in_meters", "inhabited_by_humans"});
        cursor.newRow().add(1).add("test").add(2).add(1);

        cursor.moveToFirst();

        Planet planet = PlanetRecord.buildFromCursor(cursor);
        Assert.assertEquals(1, planet.id());
        Assert.assertEquals("test", planet.name());
        Assert.assertEquals(2f, planet.radiusInMeters(), 0);
        Assert.assertEquals(true, planet.inhabitedByHumans());

        Planet anotherPlanet = PlanetRecord.buildFromCursor(cursor);
        Assert.assertNotSame(planet, anotherPlanet);
    }
}
