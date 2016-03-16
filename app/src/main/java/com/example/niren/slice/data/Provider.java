package com.example.niren.slice.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.niren.slice.data.Contract.TaskEntry;

/**
 * Created by Ixanos on 10/03/2016.
 */
public class Provider extends ContentProvider {
    /* Provide a single task by id.
    * Proivde a list of tasks by day */

    public static final int TASK = 1;
    public static final int TASKS_BY_DAY = 2;
    public static final int TASK_BY_ID = 3;
    public static final int TASKS_WITH_QUERY = 4;

    private DbHelper mDbHelper;

    public UriMatcher getUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Contract.AUTHORITY, TaskEntry.PATH, TASK);
        matcher.addURI(Contract.AUTHORITY, TaskEntry.PATH + "/#", TASK_BY_ID);
        matcher.addURI(Contract.AUTHORITY, TaskEntry.PATH + "/day/#", TASKS_BY_DAY);
        matcher.addURI(Contract.AUTHORITY, TaskEntry.PATH + "?*", TASKS_WITH_QUERY);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        switch (getUriMatcher().match(uri)) {
            case TASK:
                cursor = db.query(TaskEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        UriMatcher matcher = getUriMatcher();
        int match = matcher.match(uri);
        switch (match) {
            case TASK_BY_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + Contract.AUTHORITY + "/" + TaskEntry.PATH;
            case TASKS_BY_DAY:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + Contract.AUTHORITY + "/" + TaskEntry.PATH;
            case TASKS_WITH_QUERY:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + Contract.AUTHORITY + "/" + TaskEntry.PATH;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        /* There's only one table now so no need to check for URI match */
        long id = db.insert(TaskEntry.TABLE_NAME, null, values);
        return TaskEntry.BASE_URI.buildUpon().appendPath(Long.toString(id)).build();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
