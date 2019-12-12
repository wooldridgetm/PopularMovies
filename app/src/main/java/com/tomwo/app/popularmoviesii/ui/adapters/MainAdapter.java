package com.tomwo.app.popularmoviesii.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.like.LikeButton;
import com.tomwo.app.popularmoviesii.R;
import com.tomwo.app.popularmoviesii.model.DB.Movies;
import com.tomwo.app.popularmoviesii.model.DB.Favorites;
import com.tomwo.app.popularmoviesii.model.supportClasses.Movie;


/**
 * Created by wooldridgetm on 3/16/17.
 */
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    // log tag can only be 23 characters long
    private static final String TAG = "MovieRecyclerViewAdapte";
    private static MatrixCursor extras;


    public interface IMovieClickListener
    {
        void onClick(Movie movie, int index);
    }

    // invoked when we've scroll the RecyclerView to the End
    public interface OnLoadMoreListener
    {
        void onLoadMore();
    }

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    // MovieRecyclerViewAdapter Instance Variables
    private Context mContext;
    private Cursor mCursor;
    private IMovieClickListener mClickListener;
    private int mMaxPage = -1;
    private String mSyncType = "n.a.";

    private OnLoadMoreListener mOnLoadMoreListener;

    public void setOnLoadMoreListener(OnLoadMoreListener listener)
    {
        this.mOnLoadMoreListener = listener;
    }


    // OnScrollListener
    private boolean isLoading = false;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    private int densityDpi;
    private int sixteenPixels;
    private int eightPixels;

    // default constructor
    public MainAdapter(Context context, Cursor cursor)
    {
        this.mContext = context;
        this.mCursor = cursor;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        this.densityDpi = (int) (metrics.density * 160f);
        this.sixteenPixels = 16 * (this.densityDpi / 160);
        this.eightPixels = 8 * (this.densityDpi / 160);

        if (context instanceof IMovieClickListener)
        {
            this.mClickListener = (IMovieClickListener) context;
            return;
        }
        throw new RuntimeException(context + " does not implement IMovieClickListener");
    }

    // constructor
    public MainAdapter(RecyclerView recyclerView, Context context, Cursor cursor)
    {
        this(context, cursor);
        final GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = layoutManager.getItemCount();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (isLoading)
                    return;

                if (totalItemCount <= (lastVisibleItem + visibleThreshold))
                {
                    if (mOnLoadMoreListener != null)
                    {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if (viewType == VIEW_TYPE_LOADING)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_list, parent, false);
        return new DefaultViewHolder(v);
    }

    // sets up the image (Picasso)
    // sets up the text in the list item
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof DefaultViewHolder)
        {
            // get the movie item
            if (this.mCursor.moveToPosition(position))
            {
                DefaultViewHolder movieViewHolder = (DefaultViewHolder) holder;
                Movie movie = new Movie();

                long id = this.mCursor.getLong(this.mCursor.getColumnIndex(Movies._ID));
                String title = this.mCursor.getString(this.mCursor.getColumnIndex(Movies.COLUMN_TITLE));
                String overview = this.mCursor.getString(this.mCursor.getColumnIndex(Movies.COLUMN_OVERVIEW));
                String posterPath = this.mCursor.getString(this.mCursor.getColumnIndex(Movies.COLUMN_POSTER_PATH));
                String backdropPath = this.mCursor.getString(this.mCursor.getColumnIndex(Movies.COLUMN_BACKDROP_PATH));
                String releaseDate = this.mCursor.getString(this.mCursor.getColumnIndex(Movies.COLUMN_RELEASE_DATE));

                double voteAverage = this.mCursor.getDouble(this.mCursor.getColumnIndex(Movies.COLUMN_VOTE_AVERAGE));
                int voteCount = this.mCursor.getInt(this.mCursor.getColumnIndex(Movies.COLUMN_VOTE_COUNT));
                long refID = this.mCursor.getLong(this.mCursor.getColumnIndex(Movies.COLUMN_REFID));

                movie.setTitle(title);
                movie.setOverview(overview);
                movie.setPosterPath(posterPath);
                movie.setBackdropPath(backdropPath);
                movie.setVoteAverage(voteAverage);
                movie.setVoteCount(voteCount);
                movie.setReleaseDate(releaseDate);
                movie.setId(id);
                movie.setRefID(refID);

                int currentPage = this.mCursor.getInt(this.mCursor.getColumnIndex(Movies.COLUMN_PAGE));
                if (this.mMaxPage < currentPage)
                {
                    this.mMaxPage = currentPage;
                }

                String syncType = this.mCursor.getString(this.mCursor.getColumnIndex(Movies.COLUMN_SYNC_TYPE));
                if (syncType == null)
                {
                    syncType = "";
                }

                if (this.mSyncType.compareToIgnoreCase(syncType) != 0)
                {
                    this.mSyncType = syncType;
                }

                int isFavorite = this.mCursor.getInt(this.mCursor.getColumnIndex(Favorites.COLUMN_FAVORITE));
                movie.setFavorite(isFavorite == Favorites.VALUE_FAVORITE_TRUE);

                movieViewHolder.setMovie(movie);    // set the movie
                movieViewHolder.setIndex(position); // set the current index

                // handle correct image sizes
                // w92, w154, w185, w342, w500, w780, original
                // e.g. http://image.tmdb.org/t/p/w185//5pAGnkFYSsFJ99ZxDIYnhQbQFXs.jpg
                String url = "http://image.tmdb.org/t/p/" + mContext.getString(R.string.image_size) + "/" + movie.getPosterPath();
                // Picasso.with(this.mContext).load(url).into(movieViewHolder.mImageView);
                GlideApp.with(this.mContext).load(url).placeholder(R.drawable.image_not_available).centerCrop().into(movieViewHolder.mImageView);
            }
        }
        else if (holder instanceof LoadingViewHolder)
        {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    // to change our dataSet
    public void swapCursor(Cursor cursor, boolean useMergeCursor)
    {
        if (this.mCursor != null)
        {
            this.mCursor.close();
        }

        // determine if we want to use LoadingItem Indicators for pagination
        if (useMergeCursor)
        {
            extras = new MatrixCursor(new String[]{"title"});
            extras.addRow(new String[]{"loading"});
            extras.addRow(new String[]{"loading"});
            this.mCursor = new MergeCursor(new Cursor[]{cursor, extras});
        }
        else
        {
            this.mCursor = cursor;
        }

        this.notifyDataSetChanged();
    }


    /**
     * Fx getter (mMaxPage)
     * <p>
     * INVOKED from MainActivityFragment.java to determine the next "page" of results we need to request from the server
     */
    public int getMaxPage()
    {
        return this.mMaxPage;
    }

    /**
     * Helper Methods for ITEM_LOADING_VIEW
     */
    @Override
    public int getItemViewType(int position)
    {
        try
        {
            if (this.mCursor.moveToPosition(position))
            {
                if (this.mCursor.getString(this.mCursor.getColumnIndex("title")).equals("loading"))
                {
                    return VIEW_TYPE_LOADING;
                }
            }
            return VIEW_TYPE_ITEM;
        } catch (Exception e)
        {
            Log.e(TAG, e.toString());
            return VIEW_TYPE_LOADING;
        }
    }

    // returns the number of items that the adapter binds
    @Override
    public int getItemCount()
    {
        return this.mCursor != null ? this.mCursor.getCount() : 0;
    }

    public void setLoaded()
    {
        this.isLoading = false;
    }

    public void dispose()
    {
        this.mCursor.close();
        this.mCursor = null;
    }


    /**
     * ** ViewHolders **
     */


    // "Default item" ViewHolder
    // nested subclass of RecyclerView.ViewHolder used to implement
    // the view-holder pattern in the context of a RecyclerView
    private class DefaultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public TextView mTextView;
        public ImageView mImageView;
        public LikeButton mStarButton;

        private CardView mCardView;

        private Movie mMovie;
        private int mIndex;

        public DefaultViewHolder(View itemView)
        {
            super(itemView);

            this.mTextView = (TextView) itemView.findViewById(R.id.titleTextView1);

            this.mCardView = (CardView) itemView.findViewById(R.id.card_view);
            View layout = this.mCardView.findViewById(R.id.constraintLayoutItemView);
            layout.setOnClickListener(this);

            this.mImageView = (ImageView) layout.findViewById(R.id.imageView);
            // this.mStarButton = (LikeButton) cl.findViewById(R.id.starBtn);
        }

        public void setMovie(Movie value)
        {
            this.mMovie = value;

            if (this.mTextView != null)
            {
                this.mTextView.setText(this.mMovie.getTitle());
            }
        }

        public void setIndex(int index)
        {
            this.mIndex = index;

            GridLayoutManager.LayoutParams layout = (GridLayoutManager.LayoutParams) this.mCardView.getLayoutParams();

            switch (index)
            {
                case 0:
                    layout.setMargins(sixteenPixels, sixteenPixels, eightPixels, 0);
                    break;
                case 1:
                    layout.setMargins(eightPixels, sixteenPixels, sixteenPixels, 0);
                    break;
                default:
                    if (this.mIndex % 2 == 0)
                    {
                        layout.setMargins(sixteenPixels, eightPixels, eightPixels, 0);
                    } else
                    {
                        layout.setMargins(eightPixels, eightPixels, sixteenPixels, 0);
                    }
            }
        }

        @Override
        public void onClick(View v)
        {
            mClickListener.onClick(mMovie, mIndex);
        }
    } // ViewHolder


    // "Loading item" ViewHolder
    private class LoadingViewHolder extends RecyclerView.ViewHolder
    {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view)
        {
            super(view);
            this.progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        }
    }
}
