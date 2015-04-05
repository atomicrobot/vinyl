package com.madebyatomicrobot.vinyl.samples;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class PlanetDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "planets.db";

    private static final String SQL_CREATE_PLANETS =
            "CREATE TABLE " + PlanetContract.Planet.TABLE_NAME
                    + " ("
                    + String.format("%s %s,", PlanetContract.Planet._ID, "INTEGER PRIMARY KEY AUTOINCREMENT")
                    + String.format("%s %s,", PlanetContract.Planet.COLUMN_NAME_NAME, "TEXT")
                    + String.format("%s %s,", PlanetContract.Planet.COLUMN_NAME_RADIUS_IN_METERS, "REAL")
                    + String.format("%s %s", PlanetContract.Planet.COLUMN_NAME_INHABITED_BY_HUMANS, "INTEGER")
                    + ")";

    private static final String SQL_DELETE_PLANETS =
            "DROP TABLE IF EXISTS " + PlanetContract.Planet.TABLE_NAME;

    PlanetDatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PLANETS);

        insertPlanet(db, "Mercury", 2.43e6, false);
        insertPlanet(db, "Venus", 6.06e6, false);
        insertPlanet(db, "Earth", 6.37e6, true);
        insertPlanet(db, "Mars", 3.37e6, false);
        insertPlanet(db, "Jupiter", 6.99e7, false);
        insertPlanet(db, "Saturn", 5.85e7, false);
        insertPlanet(db, "Uranus", 2.33e7, false);
        insertPlanet(db, "Neptune", 2.21e7, false);
    }

    private void insertPlanet(SQLiteDatabase db, String name, double radiusInMeters, boolean inhabitedByHumans) {
        ContentValues contentValues = PlanetRecord.contentValuesBuilder()
                .name(name)
                .radiusInMeters(radiusInMeters)
                .inhabitedByHumans(inhabitedByHumans)
                .build();
        db.insert(PlanetContract.Planet.TABLE_NAME, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PLANETS);
        onCreate(db);
    }
}
