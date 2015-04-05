package com.madebyatomicrobot.vinyl.samples;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import org.junit.Assert;

public class PlanetContentProviderTest extends ProviderTestCase2<PlanetContentProvider> {
    public PlanetContentProviderTest() {
        super(PlanetContentProvider.class, PlanetContract.CONTENT_AUTHORITY);
    }

    public void testQueryPlanets() {
        MockContentResolver contentResolver = getMockContentResolver();
        Cursor cursor = contentResolver.query(PlanetContract.Planet.contentUri(), PlanetRecord.projection(), null, null, null);
        DatabaseUtils.dumpCursor(cursor);

        cursor.moveToFirst();
        Assert.assertEquals(8, cursor.getCount());

        Planet planet = PlanetRecord.wrapCursor(cursor);
        assertEquals("Mercury", planet.name());
        assertEquals(2.43e6, planet.radiusInMeters());
        assertEquals(false, planet.inhabitedByHumans());
        cursor.close();
    }

    public void testInsertPlanet() {
        MockContentResolver contentResolver = getMockContentResolver();
        Cursor cursor = contentResolver.query(PlanetContract.Planet.contentUri(), PlanetRecord.projection(), null, null, null);
        Assert.assertEquals(8, cursor.getCount());
        cursor.close();

        // Sorry, Neil deGrasse Tyson...
        ContentValues contentValues = PlanetRecord.contentValuesBuilder()
                .inhabitedByHumans(false)
                .radiusInMeters(1.5e6)
                .name("Pluto")
                .build();
        contentResolver.insert(PlanetContract.Planet.contentUri(), contentValues);

        cursor = contentResolver.query(PlanetContract.Planet.contentUri(), PlanetRecord.projection(), null, null, null);
        Assert.assertEquals(9, cursor.getCount());
        cursor.moveToLast();
        Planet planet = PlanetRecord.wrapCursor(cursor);
        assertEquals("Pluto", planet.name());
        assertEquals(1.5e6, planet.radiusInMeters());
        assertEquals(false, planet.inhabitedByHumans());
        cursor.close();
    }
}
