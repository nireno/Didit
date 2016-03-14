package com.example.niren.slice;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.niren.slice.data.Contract;
import com.example.niren.slice.data.Contract.TaskEntry;

import java.util.GregorianCalendar;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_LOADER = 0;
    private final String[] PROJECTION = new String[]{TaskEntry._ID, TaskEntry.COL_DATE_START, TaskEntry.COL_DATE_END};
    private final int COL_IDX_ID = 0;
    private final int COL_IDX_DATE_START = 1;
    private final int COL_IDX_DATE_END = 2;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private SimpleItemRecyclerViewAdapter mAdapter = new SimpleItemRecyclerViewAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.item_list);
        assert recyclerView != null;
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(mAdapter);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        ContentValues cvs = new ContentValues();
        GregorianCalendar cal = new GregorianCalendar(2016, 01, 01, 14, 30);
        cvs.put(TaskEntry.COL_DATE_START, cal.getTimeInMillis());
        cal.add(GregorianCalendar.MINUTE, 30);
        cvs.put(TaskEntry.COL_DATE_END, cal.getTimeInMillis());
        getContentResolver().insert(Contract.TaskEntry.BASE_URI, cvs);

        getLoaderManager().initLoader(TASK_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        GregorianCalendar cal = new GregorianCalendar(2016, 01, 01);
        Uri uri = TaskEntry.buildTasksByDateUri(cal.getTimeInMillis());
        return new CursorLoader(this, uri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private Cursor mCursor;

        public SimpleItemRecyclerViewAdapter() {
        }

        public void swapCursor(Cursor newCursor) {
            mCursor = newCursor;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.mIdView.setText(Long.toString(mCursor.getLong(COL_IDX_DATE_START)));
            holder.mContentView.setText(Long.toString(mCursor.getLong(COL_IDX_DATE_END)));

//            holder.mView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mTwoPane) {
//                        Bundle arguments = new Bundle();
//                        arguments.putString(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
//                        ItemDetailFragment fragment = new ItemDetailFragment();
//                        fragment.setArguments(arguments);
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.item_detail_container, fragment)
//                                .commit();
//                    } else {
//                        Context context = v.getContext();
//                        Intent intent = new Intent(context, ItemDetailActivity.class);
//                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
//
//                        context.startActivity(intent);
//                    }
//                }
//            });
        }

        @Override
        public int getItemCount() {
            if (null == mCursor) return 0;
            return mCursor.getCount();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
