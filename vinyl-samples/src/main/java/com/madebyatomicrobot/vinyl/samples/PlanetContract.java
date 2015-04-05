package com.madebyatomicrobot.vinyl.samples;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class PlanetContract {
    public static final String CONTENT_AUTHORITY = "com.madebyatomicrobot.vinyl.samples.planets";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH = "planets";

    public static class Planet implements BaseColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.planets";
        public static final String CONTENT_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.planets";

        public static Uri contentUri() {
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();
        }

        public static Uri contentUriItem(long id) {
            return contentUri().buildUpon().appendPath(Long.toString(id)).build();
        }

        public static long idFromUri(Uri uri) {
            return Long.parseLong(uri.getLastPathSegment());
        }

        public static final String TABLE_NAME = "planets";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_RADIUS_IN_METERS = "radius_in_meters";
        public static final String COLUMN_NAME_INHABITED_BY_HUMANS = "inhabited_by_humans";
    }
}
