package com.tomwo.app.popularmoviesii.ui.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.tomwo.app.popularmoviesii.R;
import com.tomwo.app.popularmoviesii.Shared;
import com.tomwo.app.popularmoviesii.ui.adapters.CustomSpinnerAdapter;
import com.tomwo.app.popularmoviesii.ui.adapters.MainAdapter;
import com.tomwo.app.popularmoviesii.ui.adapters.MainAdapter.OnLoadMoreListener;
import com.tomwo.app.popularmoviesii.model.DB;
import com.tomwo.app.popularmoviesii.model.supportClasses.FilterItem;
import com.tomwo.app.popularmoviesii.tasks.BackgroundTasks;
import com.tomwo.app.popularmoviesii.tasks.MovieSyncIntentService;
import com.tomwo.app.popularmoviesii.utils.NetworkUtils;
import com.tomwo.app.popularmoviesii.utils.PreferenceUtils;

import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;

import static com.tomwo.app.popularmoviesii.Shared.CURSOR_LOADER_ID;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String TAG = "MovieListsFragment";

    // I.V.'s
    private RecyclerView mRecyclerView;
    private TextView mOfflineTextView;

    private MainAdapter mAdapter;

    private GridLayoutManager mGridLayoutManager;
    private CustomSpinnerAdapter mSpinnerAdapter;
    private Spinner mSpinner;
    private Handler mHandler;
    private ProgressBar mProgressBar;


    public MainActivityFragment()
    {
        // Required empty public constructor
        Log.d(TAG, "MainFragment - CONSTRUCTOR");
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

        // set the IVs
        this.mHandler = new Handler();

        // now, get saved Filter from SharedPreferences
        int filterSelectedIndex = PreferenceUtils.getFilterIndex(this.getActivity());
        FilterItem filterItem = Shared.FILTER_ITEMS.get(filterSelectedIndex);
        filterItem.setSelected(true);

        // set the Title of the Fragment based upon the currently selectedIndex in the Filter!
        this.getActivity().setTitle(filterItem.getTitle());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.main_fragment_menu, menu);

        // setup the dropdown for FilterBtn
        MenuItem item = menu.findItem(R.id.filterBtn);
        this.mSpinner = (Spinner) MenuItemCompat.getActionView(item);

        if (this.mSpinner == null)
            return;

        // create Adapter for FilterBtn
        this.mSpinnerAdapter = new CustomSpinnerAdapter(this.getActivity(), R.layout.item_spinner_list, R.id.textViewSpinnerItem, Shared.FILTER_ITEMS);
        this.mSpinner.setAdapter(this.mSpinnerAdapter);

        // set the selectedIndex in the Spinner.
        // NOTE: If this is omitted, then the Spinner always believes the SelectedIndex = 0, despite the CustomSpinnerAdapter having it correct & it displaying it correctly in the dropdown
        this.mSpinner.setSelection(PreferenceUtils.getFilterIndex(this.getActivity()));

        // add listener to detect Event.CHANGE in the selectedIndex for the Spinner
        this.mSpinner.setOnItemSelectedListener(this.filterBtnListener);

        // NOTE: this is just a UI thing
        // we set a LongPress listener b/c we want the layer_list_filter_btn_selected.xml background to displayed as long as the user is pressing the Filter Btn
        this.mSpinner.setOnItemLongClickListener(this.filterBtn_longPressListener);
    }

    private AdapterView.OnItemSelectedListener filterBtnListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            int previousIndex = mSpinnerAdapter.getSelectedIndex();

            // happens when Activity is 1st created
            if (previousIndex == position)
                return;

            // update the Activity's Title with currently selected filter
            getActivity().setTitle(Shared.FILTER_ITEMS.get(position).getTitle());

            mSpinnerAdapter.getItem(previousIndex).setSelected(false);
            mSpinnerAdapter.getItem(position).setSelected(true);

            // update the Selected Index
            mSpinnerAdapter.notifyDataSetChanged();

            // update our value for the Filter in SharedPreferences using a BackgroundTask
            Intent intent = new Intent(getActivity(), MovieSyncIntentService.class);
            intent.setAction(BackgroundTasks.ACTION_UPDATE_PREFERENCES);
            Bundle bundle = new Bundle();
            bundle.putInt(PreferenceUtils.KEY_FILTER_INDEX, position);
            intent.putExtras(bundle);
            // start a backgroundService to update Preferences in the background
            getActivity().startService(intent);

            LoaderManager.getInstance(MainActivityFragment.this.getActivity()).restartLoader(CURSOR_LOADER_ID, bundle, MainActivityFragment.this);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {
            mSpinnerAdapter.getItem(0).setSelected(true);

            int prevIndex = mSpinnerAdapter.getSelectedIndex();
            mSpinnerAdapter.getItem(prevIndex).setSelected(false);

            mSpinnerAdapter.notifyDataSetChanged();
        }
    };

    private AdapterView.OnItemLongClickListener filterBtn_longPressListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            return true;
        }
    };


    /**
     * Fx onCreateView(...args)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity_main, container, false);

        this.mProgressBar = view.findViewById(R.id.progressBar);
        this.mProgressBar.setVisibility(View.VISIBLE);

        this.mOfflineTextView = view.findViewById(R.id.offlineView2);

        this.mRecyclerView = view.findViewById(R.id.recyclerView);
        this.mGridLayoutManager = new GridLayoutManager(this.getActivity(), 2);
        this.mRecyclerView.setLayoutManager(this.mGridLayoutManager);
        this.mRecyclerView.setItemAnimator(new SlideInDownAnimator());
        this.mRecyclerView.setHasFixedSize(true);

        // create the Adapter, datasource will be a cursor
        this.mAdapter = new MainAdapter(this.mRecyclerView, this.getActivity(), null);

        // set the Adapter for the List
        this.mRecyclerView.setAdapter(this.mAdapter);

        // add the OnLoadMoreListener to the ADAPTER (not recyclerView) to get notified when we've reached the end of our local data & need to request more from the server
        this.mAdapter.setOnLoadMoreListener(new OnLoadMoreListener()
        {
            @Override
            public void onLoadMore()
            {
                // fetch more movies
                fetchMovies();
            }
        });
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        int filterSelectedIndex = PreferenceUtils.getFilterIndex(this.getActivity());
        Bundle bundle = new Bundle();
        bundle.putInt(PreferenceUtils.KEY_FILTER_INDEX, filterSelectedIndex);
        LoaderManager.getInstance(this.getActivity()).restartLoader(CURSOR_LOADER_ID, bundle, this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

    }

    /**
     * ** LOADERS **
     */

    @Override @NonNull
    public Loader<Cursor> onCreateLoader( int id, Bundle args)
    {
        if (id == CURSOR_LOADER_ID)
        {
            int filterSelectedIndex = args.getInt(PreferenceUtils.KEY_FILTER_INDEX);

            // we are grabbing ALL of the data…not ideal
            // only grab the 1st 20 records or so
            String filterBy = Shared.FILTER_ITEMS.get(filterSelectedIndex).getKey();
            String where = DB.Movies.COLUMN_SYNC_TYPE + "='" + filterBy + "'";

            if (filterBy.toLowerCase().equals("favorites"))
            {
                where = DB.Favorites.COLUMN_FAVORITE + "=" + DB.Favorites.VALUE_FAVORITE_TRUE;
            }
            return new CursorLoader(this.getActivity(), DB.MovieFavoritesView.CONTENT_URI, null, where, null, null);
        }
        throw new UnsupportedOperationException("Unknown loader id: " + id);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor)
    {
        // determine if we want the new dataset to use a progressBar layout (@layout/item_loading.xml) for pagination purposes
        // we must do this b/c our SQLStatements are setup to just grab ALL of the data from the local db…whether 20 records OR 20,000 records
        boolean useMergeCursor = true;

        // adapter is null when the app first runs
        if (this.mSpinner != null)
        {
            String filterBy = Shared.FILTER_ITEMS.get(this.mSpinner.getSelectedItemPosition()).getKey();
            Log.d(TAG, "Fx onLoadFinished(): filterBy='"+filterBy+"'");
            if (filterBy.toLowerCase().equals("favorites"))
            {
                useMergeCursor = false;
            }
        }

        // update our Adapter to the current cursor we just retrieved
        this.mAdapter.swapCursor(cursor, useMergeCursor);
        this.mAdapter.setLoaded();

        // we need to sync the data
        if (cursor == null || cursor.getCount() == 0)
        {
            this.mRecyclerView.setVisibility(View.GONE);
            this.mProgressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            this.mRecyclerView.setVisibility(View.VISIBLE);
            this.mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader)
    {
        this.mAdapter.swapCursor(null,false);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        this.mAdapter.dispose();
    }

    /**
     * Fx fetchMovies()
     * <p>
     * Invoked when from RecyclerView's Adapter when we've reached the end of our data && need to request moree
     */
    private void fetchMovies()
    {
        // check for Internet Connection
        if (NetworkUtils.isConnected(this.getActivity()))
        {
            // start the backgroundTask to go get more data.
            Intent intent = new Intent(this.getActivity(), MovieSyncIntentService.class);
            intent.setAction(BackgroundTasks.ACTION_SYNC_MOVIES);

            Bundle args = new Bundle();
            args.putInt(BackgroundTasks.BUNDLE_PARAM_PAGINATION, this.mAdapter.getMaxPage() + 1);

            intent.putExtras(args);

            this.getActivity().startService(intent);

            this.mOfflineTextView.setVisibility(View.INVISIBLE);

        }
        else
        {
            // todo: use a Broadcast Receiver to determine this…
            this.mOfflineTextView.setVisibility(View.VISIBLE);

            // make it go invisible after 5 secons
            this.mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mOfflineTextView.setVisibility(View.INVISIBLE);

                    // the data isn't loaded - it can't be b/c we're offline - so reset the flag s.t. when the user scrolls to the bottom (again)
                    // this will be displaed
                    mAdapter.setLoaded();
                }
            },5000);
        }
    }
}