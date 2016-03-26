package com.example.niren.slice;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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

    private final String[] PROJECTION = new String[]{TaskEntry._ID, TaskEntry.COL_DATE_START, TaskEntry.COL_DATE_END};
    private final int COL_IDX_ID = 0;
    private final int COL_IDX_DATE_START = 1;
    private final int COL_IDX_DATE_END = 2;

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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            long id = getArguments().getLong(ARG_ITEM_ID);
            Uri uri = TaskEntry.buildTaskByIdUri(id);
            cursor = getContext().getContentResolver().query(uri, PROJECTION, null, null, null);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null && cursor.moveToFirst()) {
                appBarLayout.setTitle(Long.toString(cursor.getLong(COL_IDX_ID)));
            }
        }
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

        ArrayAdapter<String> a = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, new String[]{"hello", "world"});
        mSpinCategory.setAdapter(a);

        if (cursor != null && cursor.moveToFirst()) {
            mTextStartTime.setText(Long.toString(cursor.getLong(COL_IDX_DATE_START)));
            mTextEndTime.setText(Long.toString(cursor.getLong(COL_IDX_DATE_END)));
            mTextDescription.setText("hello world");
        } else {
            long startTime = getActivity().getIntent().getLongExtra(ARG_START_TIME, 0);
            mCalStartTime = GregorianCalendar.getInstance();
            mCalStartTime.setTimeInMillis(startTime);
            mTextStartTime.setText(String.format("%1$tH:%1$tM", mCalStartTime));
            mCalEndTime = GregorianCalendar.getInstance();
            mTextEndTime.setText(String.format("%1$tH:%1$tM", mCalEndTime));
        }

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
        mTextClickedTime.setText("" + hourOfDay + ":" + minute);
    }
}
