package com.example.niren.slice.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ixanos on 10/03/2016.
 */
public class Contract {
    public static final String AUTHORITY = "com.example.niren.slice";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public static class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "task";
        public static final String COL_DATE_START = "start";
        public static final String COL_DATE_END = "task";
        public static final String COL_DESCRIPTION = "task";

        public static final String PATH = "task";
        public static final Uri BASE_URI = Contract.BASE_URI.buildUpon().appendPath("task").build();

        public static Uri buildTaskByIdUri(long id) {
            return BASE_URI.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static Uri buildTasksByDateUri(long date) {
            return BASE_URI.buildUpon().appendPath("/date/" + date).build();
        }
    }
}
