package com.madebyatomicrobot.vinyl.samples;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class PlanetContentProvider extends ContentProvider {
    private static final String AUTHORITY = PlanetContract.CONTENT_AUTHORITY;

    private static final int ROUTE_PLANETS = 1;
    private static final int ROUTE_PLANET = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, "planets", ROUTE_PLANETS);
        uriMatcher.addURI(AUTHORITY, "planets/#", ROUTE_PLANET);
    }

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        PlanetDatabaseHelper databaseHelper = new PlanetDatabaseHelper(getContext());
        db = databaseHelper.getWritableDatabase();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case ROUTE_PLANETS:
                return PlanetContract.Planet.CONTENT_TYPE;
            case ROUTE_PLANET:
                return PlanetContract.Planet.CONTENT_TYPE_ITEM;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_PLANETS:
                return db.query(PlanetContract.Planet.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            case ROUTE_PLANET:
                long id = PlanetContract.Planet.idFromUri(uri);
                selection = DatabaseUtils.concatenateWhere(selection, String.format("%s=?", PlanetContract.Planet._ID));
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[]{Long.toString(id)});
                return db.query(PlanetContract.Planet.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_PLANETS:
                long id = db.insert(PlanetContract.Planet.TABLE_NAME, null, values);
                return PlanetContract.Planet.contentUriItem(id);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
}
