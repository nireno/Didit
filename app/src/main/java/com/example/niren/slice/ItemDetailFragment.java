package com.example.niren.slice;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.niren.slice.data.Contract.TaskEntry;
import com.example.niren.slice.dummy.DummyContent;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_START_TIME = "start_time";

    private final String[] PROJECTION = new String[]{
            TaskEntry._ID,
            TaskEntry.COL_DATE_START,
            TaskEntry.COL_DATE_END,
            TaskEntry.COL_DESCRIPTION,
            TaskEntry.COL_CATEGORY
    };
    private final int COL_IDX_ID = 0;
    private final int COL_IDX_DATE_START = 1;
    private final int COL_IDX_DATE_END = 2;
    private final int COL_IDX_DESCRIPTION = 3;
    private final int COL_IDX_CATEGORY = 4;
    private final String[] mCategories = new String[]{
            "Development",
            "Research",
            "SDLC",
            "Internal Comms",
            "External Comms",
            "Work Related"
    };
    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;
    private Cursor cursor;
    private TextView mTextStartTime;
    private TextView mTextEndTime;
    private TextView mTextDescription;
    private Spinner mSpinCategory;
    private TextView mTextClickedTime;
    private Calendar mCalStartTime;
    private Calendar mCalEndTime;
    private String mDescription;
    private MenuItem mSaveAction;
    private long mTaskEntryId;
    private int mSelectedCategoryId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        String title = "Edit Task";
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            /* User clicked on an existing task. Get a cursor on that task id and load start/stop time etc. */
            mTaskEntryId = getArguments().getLong(ARG_ITEM_ID);
            Uri uri = TaskEntry.buildTaskByIdUri(mTaskEntryId);
            cursor = getContext().getContentResolver().query(uri, PROJECTION, null, null, null);

            cursor.moveToFirst();
            long startTimeMillis = cursor.getLong(COL_IDX_DATE_START);
            mCalStartTime = GregorianCalendar.getInstance();
            mCalStartTime.setTimeInMillis(startTimeMillis);

            long endTimeMillis = cursor.getLong(COL_IDX_DATE_END);
            mCalEndTime = GregorianCalendar.getInstance();
            mCalEndTime.setTimeInMillis(endTimeMillis);

            mDescription = cursor.getString(COL_IDX_DESCRIPTION);
            mSelectedCategoryId = identifySelectedCategory(cursor.getString(COL_IDX_CATEGORY));
        } else {
            /* User clicked an empty cell. We expect to get a start time argument  */
            long startTimeMillis = getArguments().getLong(ARG_START_TIME);
            title = "New Task";
            mCalStartTime = GregorianCalendar.getInstance();
            mCalStartTime.setTimeInMillis(startTimeMillis);

            mCalEndTime = (GregorianCalendar) mCalStartTime.clone();
            mCalEndTime.add(Calendar.HOUR, 1);
        }

        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(title);
        }
    }

    private int identifySelectedCategory(String category) {
        int id = -1;
        for (int i = 0; i < mCategories.length; i++) {
            if (mCategories[i].equals(category)) {
                id = i;
            }
        }
        return id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);
//        TextView tv = ((TextView) rootView.findViewById(R.id.item_detail));
        mTextStartTime = ((TextView) rootView.findViewById(R.id.start_time));
        mTextStartTime.setOnClickListener(this);
        mTextEndTime = ((TextView) rootView.findViewById(R.id.end_time));
        mTextEndTime.setOnClickListener(this);
        mTextDescription = ((TextView) rootView.findViewById(R.id.description));
        mSpinCategory = (Spinner) rootView.findViewById(R.id.category);

        ArrayAdapter<String> a = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_item, mCategories);
        mSpinCategory.setAdapter(a);

        mSpinCategory.setSelection(mSelectedCategoryId);

        mTextStartTime.setText(String.format("%1$tH:%1$tM", mCalStartTime));
        mTextEndTime.setText(String.format("%1$tH:%1$tM", mCalEndTime));
        mTextDescription.setText(mDescription);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        mTextClickedTime = (TextView) v;
        Calendar cal;
        switch (v.getId()) {
            case R.id.start_time:
                cal = mCalStartTime;
                break;
            case R.id.end_time:
                cal = mCalEndTime;
                break;
            default:
                cal = Calendar.getInstance();
        }

        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        );
        tpd.show(getActivity().getFragmentManager(), "timepickerdialog");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        Calendar oldCal = GregorianCalendar.getInstance();
        oldCal.setTimeInMillis(mCalStartTime.getTimeInMillis());
        Calendar newCal = GregorianCalendar.getInstance();
        newCal.set(oldCal.get(Calendar.YEAR), oldCal.get(Calendar.MONTH), oldCal.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, second);

        mTextClickedTime.setText(String.format("%1$tH:%1$tM", newCal));

        if (mTextStartTime == mTextClickedTime) {
            mCalStartTime.setTimeInMillis(newCal.getTimeInMillis());
        } else {
            mCalEndTime.setTimeInMillis(newCal.getTimeInMillis());
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_task_menu, menu);
        mSaveAction = menu.findItem(R.id.action_save);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mSaveAction == item) {
            /*Is it a new task or are we editing an existing one */
            ContentValues cv = makeContentValues();
            if (cursor == null) {
                /* then its a new task */
                Uri uri = getActivity().getContentResolver().insert(TaskEntry.BASE_URI, cv);
            } else {
                String where = TaskEntry._ID + " = ?";
                String[] whereArgs = new String[]{Long.toString(mTaskEntryId)};
                getActivity().getContentResolver().update(TaskEntry.BASE_URI, cv, where, whereArgs);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private ContentValues makeContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(TaskEntry.COL_DATE_START, mCalStartTime.getTimeInMillis());
        cv.put(TaskEntry.COL_DATE_END, mCalEndTime.getTimeInMillis());
        cv.put(TaskEntry.COL_DESCRIPTION, mTextDescription.getText().toString());
        cv.put(TaskEntry.COL_CATEGORY, mSpinCategory.getSelectedItem().toString());
        return cv;
    }
}
