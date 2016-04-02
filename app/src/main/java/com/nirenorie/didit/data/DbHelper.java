package com.nirenorie.didit.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ixanos on 10/03/2016.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "task.db";
    private static final int VERSION = 1;
    private static final String SQL_CREATE_TABLE_TASK = "CREATE TABLE " + Contract.TaskEntry.TABLE_NAME + "("
            + Contract.TaskEntry._ID + " integer primary key autoincrement, "
            + Contract.TaskEntry.COL_DATE_START + " integer not null, "
            + Contract.TaskEntry.COL_DATE_END + " integer, "
            + Contract.TaskEntry.COL_DESCRIPTION + " text, "
            + Contract.TaskEntry.COL_CATEGORY + " text);";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_TASK);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists" + Contract.TaskEntry.TABLE_NAME + ";");
    }
}
