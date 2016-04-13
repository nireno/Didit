package com.nirenorie.didit.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.nirenorie.didit.ItemListActivity;
import com.nirenorie.didit.R;
import com.nirenorie.didit.data.Contract;

import java.util.GregorianCalendar;

/**
 * IntentService which handles updating all Task widgets
 */

public class TaskWidgetIntentService extends IntentService {
    private static final String[] PROJECTION = {
              Contract.TaskEntry._ID
            , Contract.TaskEntry.COL_DESCRIPTION
    };
    // these indices must match the projection
    private static final int INDEX_ID = 0;
    private static final int INDEX_DESC = 1;

    public TaskWidgetIntentService() {
        super("TaskWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all task widget ids to be updated
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TaskWidgetProvider.class));

        // Get latest task data from the ContentProvider
        String where = Contract.TaskEntry.COL_DATE_START + " <= ?";
        String[] whereArgs = new String[]{Long.toString((new GregorianCalendar().getTimeInMillis()))};
        Cursor data = getContentResolver().query(Contract.TaskEntry.BASE_URI, PROJECTION, where,
                whereArgs, Contract.TaskEntry.COL_DATE_START + " DESC");
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the task data from the Cursor
        int taskId = data.getInt(INDEX_ID);
        String description = data.getString(INDEX_DESC);
        data.close();

        // update each TaskWidget instance
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_task);

            // Add the data to the RemoteViews
            views.setTextViewText(R.id.widget_task_description, description);

            // Create an Intent to launch the app
            Intent launchIntent = new Intent(this, ItemListActivity.class);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
