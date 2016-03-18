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
import android.widget.TextView;

import com.example.niren.slice.data.Contract.TaskEntry;
import com.example.niren.slice.dummy.DummyContent;


/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private final String[] PROJECTION = new String[]{TaskEntry._ID, TaskEntry.COL_DATE_START, TaskEntry.COL_DATE_END};
    private final int COL_IDX_ID = 0;
    private final int COL_IDX_DATE_START = 1;
    private final int COL_IDX_DATE_END = 2;

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;
    private Cursor cursor;

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

        if (cursor != null) {
            ((TextView) rootView.findViewById(R.id.item_detail)).setText(Long.toString(cursor.getLong(COL_IDX_DATE_END)));
        }

        return rootView;
    }
}
