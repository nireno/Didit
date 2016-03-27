package com.example.niren.slice.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class Contract {
    public static final String AUTHORITY = "com.example.niren.slice";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public static class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "task";
        public static final String COL_DATE_START = "start_date";
        public static final String COL_DATE_END = "end_date";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_CATEGORY = "category";

        public static final String PATH = "task";
        public static final Uri BASE_URI = Contract.BASE_URI.buildUpon().appendPath("task").build();

        public static Uri buildTaskByIdUri(long id) {
            return ContentUris.withAppendedId(BASE_URI, id);
        }

        public static Uri buildTasksByDateUri(long date) {
            return BASE_URI.buildUpon().appendPath("/date/" + date).build();
        }

        public static String getTaskIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
