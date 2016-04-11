package com.nirenorie.didit;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.nirenorie.didit.data.Contract.TaskEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                String errorMessage = GeofenceErrorMessages.getErrorString(this,
                        geofencingEvent.getErrorCode());
                Log.e(TAG, errorMessage);
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            switch (geofenceTransition){
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    // when i get to where i work i want the app to start a new task for me.
                    // but only if i didnt add any tasks for that day already.
                    Calendar c = new GregorianCalendar();
                    if( !dayHasTask(c)){
                        startNewTask();
                    }
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    // in case i forgot, stop the last task i was working on when i left work
                    stopLastTask();
                    break;
                default:
                    // Log the error.
                    Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                            geofenceTransition));
                    break;
            }
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            Log.i(TAG, geofenceTransitionDetails);
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context             The app context.
     * @param geofenceTransition  The ID of the geofence transition.
     * @param triggeringGeofences The geofence(s) triggered.
     * @return The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    /* */
    private boolean dayHasTask(Calendar cal){
        /* Truncate hour & minute from cal */
        Calendar dateStart = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)
                ,cal.get(Calendar.DAY_OF_MONTH));
        Calendar dateEnd = (GregorianCalendar)dateStart.clone();
        dateEnd.add(Calendar.DAY_OF_MONTH, 1);

        String where = TaskEntry.COL_DATE_START + " >= ? AND"
                + TaskEntry.COL_DATE_END + " < ?";
        String[] whereArgs = new String[]{ Long.toString(dateStart.getTimeInMillis())
                , Long.toString(dateEnd.getTimeInMillis())};

        Cursor c = getContentResolver().query(TaskEntry.BASE_URI, new String[]{
                TaskEntry._ID}, where, whereArgs, null);

        return c != null && c.getCount() > 0;
    }

    private void startNewTask(){
        Calendar cal = new GregorianCalendar();
        ContentValues cv = new ContentValues();
        cv.put(TaskEntry.COL_DATE_START, cal.getTimeInMillis());
        cv.put(TaskEntry.COL_DESCRIPTION, getString(R.string.geofence_enter_task_description));
        getContentResolver().insert(TaskEntry.BASE_URI, cv);
    }

    private void stopLastTask(){
        long timeNow = new GregorianCalendar().getTimeInMillis();
        String where = TaskEntry.COL_DATE_START + " < ?"; // (ignore tasks that are in the future for some reason)
        String[] whereArgs = new String[] {Long.toString(timeNow)};
        String sortOrder = TaskEntry.COL_DATE_START + " DESC";
        ContentResolver contentResolver = getContentResolver();
        Cursor cur = contentResolver.query(TaskEntry.BASE_URI
                , new String[]{TaskEntry._ID, TaskEntry.COL_DATE_END}
                , where, whereArgs, sortOrder);

        // the sort order makes the first row the last task entry
        if(cur != null && cur.moveToFirst()){
            long id = cur.getLong(0);
            String endTime = cur.getString(1);
            if(endTime == null || endTime.equals("")){
                ContentValues cv = new ContentValues();
                cv.put(TaskEntry.COL_DATE_END, timeNow);
                where = TaskEntry._ID + " = ?";
                whereArgs = new String[]{Long.toString(id)};
                contentResolver.update(TaskEntry.BASE_URI, cv, where, whereArgs);
            }
        }
    }
}
