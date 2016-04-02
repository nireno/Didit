package com.nirenorie.didit;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.nirenorie.didit.data.Contract.TaskEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    private final String[] PROJECTION = new String[]{TaskEntry._ID, TaskEntry.COL_DATE_START, TaskEntry.COL_DATE_END, TaskEntry.COL_DESCRIPTION};
    private final int COL_IDX_ID = 0;
    private final int COL_IDX_DATE_START = 1;
    private final int COL_IDX_DATE_END = 2;
    private final int COL_IDX_DESCRIPTION = 3;
    private final String DETAIL_FRAG = "detail_fragment";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private WeekView mWeekView;
    private ItemListActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());



        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        mWeekView = (WeekView) findViewById(R.id.weekView);

        MonthLoader.MonthChangeListener monthChangeListener = new MonthLoader.MonthChangeListener() {
            @Override
            public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                List<WeekViewEvent> events = new ArrayList<>();
                GregorianCalendar cal = new GregorianCalendar(newYear, newMonth, 1);
                GregorianCalendar cal2 = new GregorianCalendar(newYear, newMonth + 1, 1);
                String selection = TaskEntry.COL_DATE_START + ">= ? AND " + TaskEntry.COL_DATE_END + " < ?";
                String[] args = new String[]{Long.toString(cal.getTimeInMillis()), Long.toString(cal2.getTimeInMillis())};
                Cursor cur = getContentResolver().query(TaskEntry.BASE_URI, PROJECTION, selection, args, null);
                int count = 0;
                Calendar eventStart;
                Calendar eventEnd;
                long eventId;
                if (cur != null) {
                    while (cur.moveToNext()) {
                        eventStart = GregorianCalendar.getInstance();
                        eventEnd = GregorianCalendar.getInstance();
                        eventStart.setTimeInMillis(cur.getLong(COL_IDX_DATE_START));
                        eventEnd.setTimeInMillis(cur.getLong(COL_IDX_DATE_END));
                        eventId = cur.getLong(COL_IDX_ID);
                        String eventDescription = cur.getString(COL_IDX_DESCRIPTION);
                        WeekViewEvent e = new WeekViewEvent(eventId, eventDescription, eventStart, eventEnd);
                        events.add(e);
                    }
                    cur.close();
                }
                return events;
            }
        };

        // Set an action when any event is clicked.
        mWeekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putLong(ItemDetailFragment.ARG_ITEM_ID, event.getId());
                    Fragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment, DETAIL_FRAG)
                            .commit();
                } else {
                    Intent intent = new Intent(mContext, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, event.getId());

                    mContext.startActivity(intent);
                }
            }
        });

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(monthChangeListener);

        // Set long press listener for events.
//        mWeekView.setEventLongPressListener(mEventLongPressListener);

        mWeekView.setEmptyViewClickListener(new WeekView.EmptyViewClickListener() {
            @Override
            public void onEmptyViewClicked(Calendar time) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putLong(ItemDetailFragment.ARG_START_TIME, time.getTimeInMillis());
                    Fragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment, DETAIL_FRAG)
                            .commit();
                } else {
                    Intent intent = new Intent(mContext, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_START_TIME, time.getTimeInMillis());

                    mContext.startActivity(intent);
                }
            }
        });
        ContentValues cvs = new ContentValues();
        GregorianCalendar cal = new GregorianCalendar();
        cvs.put(TaskEntry.COL_DATE_START, cal.getTimeInMillis());
        cal.add(GregorianCalendar.HOUR_OF_DAY, 1);
        cvs.put(TaskEntry.COL_DATE_END, cal.getTimeInMillis());
//        getContentResolver().insert(Contract.TaskEntry.BASE_URI, cvs);
    }

    /* recreate() is called as a workaround to refresh android-week-view when in two-pane mode */
    public void removeDetailFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DETAIL_FRAG);
        getSupportFragmentManager().beginTransaction()
                .remove(fragment).commit();
        recreate();
    }

    public void selectItem(Long id) {
        recreate();
        Bundle arguments = new Bundle();
        arguments.putLong(ItemDetailFragment.ARG_ITEM_ID, id);
        Fragment fragment = new ItemDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, fragment, DETAIL_FRAG)
                .commit();
    }
}
