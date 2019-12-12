package com.tomwo.app.popularmoviesii.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;
import com.tomwo.app.popularmoviesii.R;
import com.tomwo.app.popularmoviesii.Shared;
import com.tomwo.app.popularmoviesii.databinding.FragmentActivityMainBinding;
import com.tomwo.app.popularmoviesii.ui.DetailActivity;
import com.tomwo.app.popularmoviesii.ui.adapters.BaseReviewAdapter;
import com.tomwo.app.popularmoviesii.ui.adapters.TrailerAdapter;
import com.tomwo.app.popularmoviesii.ui.utils.ItemDivider;
import com.tomwo.app.popularmoviesii.databinding.ActivityDetailBinding;
import com.tomwo.app.popularmoviesii.databinding.FragmentDetailAlternateBinding;
import com.tomwo.app.popularmoviesii.model.DB;
import com.tomwo.app.popularmoviesii.model.supportClasses.Movie;
import com.tomwo.app.popularmoviesii.model.supportClasses.Review;
import com.tomwo.app.popularmoviesii.model.supportClasses.Trailer;
import com.tomwo.app.popularmoviesii.tasks.BackgroundTasks;
import com.tomwo.app.popularmoviesii.tasks.MovieSyncIntentService;
import com.tomwo.app.popularmoviesii.tasks.SyncUtils;
import com.tomwo.app.popularmoviesii.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import static com.tomwo.app.popularmoviesii.Shared.CURSOR_LOADER_ID;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements View.OnClickListener,
                                                                BaseReviewAdapter.IClickListener,
                                                                LoaderManager.LoaderCallbacks<List<Object>>
{
    private static final String TAG = "DetailActivityFragment";

    private static final int LOADER_ID_REVIEWS = 2812;
    private static final int LOADER_ID_TRAILERS = 5963;

    // current Movie
    private Movie mMovie;

    // Views
    private TextView mTitleTextView;
    private TextView mOverviewTextView;
    private TextView mReleaseDateTextView;
    private TextView mRatingTextView;
    private ImageView mPosterImageView;
    private RecyclerView mReviewsRecyclerView;  // Review List

    private BaseReviewAdapter mAdapter;  // Adapter for ReviewRecyclerView
    private TextView mEmptyTextViewReview; // empty view for Reviews

    private List<Trailer> mTrailerList;

    private ImageView mBackdropImageView;
    private FrameLayout mFrameLayout;
    private Button mPlayBtn;
    private LikeButton mStarBtn;

    private RecyclerView mTrailerRecyclerView;  // trailer's list
    private TrailerAdapter mTrailerAdapter; // trailerAdapter

    FragmentDetailAlternateBinding mBinding;

    public DetailActivityFragment()
    {
        // blank constructor, req'd
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (this.getArguments() != null)
        {
            // put here in Fx newInstance(Movie movie);
            this.mMovie = this.getArguments().getParcelable(Movie.KEY_MOVIE);
        }

        if (this.mMovie == null)
        {
            DetailActivity activity = (DetailActivity) this.getActivity();
            this.mMovie = activity.getMovie();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // get the DataBinding Instance
        this.mBinding = FragmentDetailAlternateBinding.inflate(inflater, container, false);

        // assign references
        this.mTitleTextView = this.mBinding.titleTextView;
        this.mOverviewTextView = this.mBinding.overviewTextView2;
        this.mRatingTextView = this.mBinding.ratingTextView;
        this.mReleaseDateTextView = this.mBinding.releaseDateTextView;
        this.mPosterImageView = this.mBinding.posterImageView2;


        this.mTitleTextView.setText(this.mMovie.getTitle());
        this.mOverviewTextView.setText(this.mMovie.getOverview());
        this.mReleaseDateTextView.setText(this.mMovie.getReleaseDate());
        this.mRatingTextView.setText(Double.toString(this.mMovie.getVoteAverage()));

        // Reviews List
        this.mReviewsRecyclerView = this.mBinding.reviewRecyclerView;
        this.mReviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        // this.mAdapter = new SingleReviewLayoutAdapter(R.layout.item_review_list);
        this.mAdapter = new BaseReviewAdapter(this, R.layout.item_review_list);
        this.mReviewsRecyclerView.setAdapter(this.mAdapter);
        this.mReviewsRecyclerView.addItemDecoration(new ItemDivider(this.getActivity()));
        this.mReviewsRecyclerView.setHasFixedSize(true);

        this.mEmptyTextViewReview = this.mBinding.emptyTextViewForReviews;  // no Reviews Found!

        // set the color background for the Rating TextView
        int ratingColorResourceId = getRatingColor(this.mMovie.getVoteAverage());
        ((GradientDrawable) this.mRatingTextView.getBackground()).setColor(ContextCompat.getColor(this.getActivity(), ratingColorResourceId));

        // now, get the pictures - poster & backdrop path
        // w92, w154, w185, w342, w500, w780, original
        final String BASE_PATH = "http://image.tmdb.org/t/p";
        String url = BASE_PATH + "/w92/" + mMovie.getPosterPath();
        Picasso.with(this.getActivity()).load(url).into(this.mPosterImageView);

        this.mTrailerRecyclerView = this.mBinding.trailersRecyclerView;
        this.mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        this.mTrailerRecyclerView.addItemDecoration(new ItemDivider(this.getActivity()));
        this.mTrailerRecyclerView.setHasFixedSize(true);

        this.mTrailerAdapter = new TrailerAdapter(this.getActivity());

        this.mTrailerRecyclerView.setAdapter(this.mTrailerAdapter);
        return this.mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity act = this.getActivity();
        LoaderManager.getInstance(act).initLoader(LOADER_ID_REVIEWS, null, this);
        LoaderManager.getInstance(act).initLoader(LOADER_ID_TRAILERS, null, this);

        this.getActivity().setTitle(this.mMovie.getTitle());

        // get the ActivityDetailBinding
        DetailActivity detail = (DetailActivity) this.getActivity();
        ActivityDetailBinding actBinding = detail.getBinding();

        this.mStarBtn = actBinding.starBtn;
        this.mStarBtn.setOnLikeListener(this.starBtn_triggeredHandler);

        this.mFrameLayout = actBinding.frameLayout;
        this.mPlayBtn = actBinding.playTrailerBtn;

        this.mBackdropImageView = actBinding.backdropImageView2;

        final String BASE_PATH = "http://image.tmdb.org/t/p";
        final String url = BASE_PATH + "/" + this.getActivity().getString(R.string.image_size) + "/" + mMovie.getBackdropPath();
        Picasso.with(this.getActivity()).load(url).into(this.mBackdropImageView);
    }

    private OnLikeListener starBtn_triggeredHandler = new OnLikeListener()
    {
        @Override
        public void liked(LikeButton button)
        {
            mMovie.setFavorite(true);

            // start our BackgroundTask to update our db
            Intent intent = new Intent(getActivity(), MovieSyncIntentService.class);
            intent.setAction(BackgroundTasks.ACTION_SAVE_FAVORITE_SELECTION_DB);

            Bundle extras = new Bundle();
            extras.putInt(Shared.BUNDLE_PARAM_ISFAVORITE, DB.Favorites.VALUE_FAVORITE_TRUE);
            extras.putLong(Shared.BUNDLE_PARAM_REFID, mMovie.getRefID());

            intent.putExtras(extras);

            getActivity().startService(intent);

        }

        @Override
        public void unLiked(LikeButton button)
        {
            mMovie.setFavorite(false);

            // start our BackgroundTask to update our db
            Intent intent = new Intent(getActivity(), MovieSyncIntentService.class);
            intent.setAction(BackgroundTasks.ACTION_SAVE_FAVORITE_SELECTION_DB);

            Bundle extras = new Bundle();
            extras.putInt(Shared.BUNDLE_PARAM_ISFAVORITE, DB.Favorites.VALUE_FAVORITE_FALSE);
            extras.putLong(Shared.BUNDLE_PARAM_REFID, mMovie.getRefID());

            intent.putExtras(extras);

            getActivity().startService(intent);
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
    }

    /**
     * Backdrop image was clicked!
     *
     * @param v
     */
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.playTrailerBtn:
            case R.id.frameLayout:
            {
                // this.launchTrailer(0);
                if (getActivity() instanceof DetailActivity)
                {
                    DetailActivity detail = (DetailActivity) getActivity();
                    detail.trailer_triggeredHandler(mTrailerList.get(0));
                }

                break;
            }
            default:
            {
                Log.w(TAG, "Fx onClick(): Error! id='" + v.getId() + "' isn't supported here.", null);
            }
        }
    }

    /**
     * fires when a Review List Item is pressed
     *
     * @param selectedIndex - index in the list that was selected
     */
    @Override
    public void listItem_triggeredHandler(int selectedIndex)
    {
        ArrayList<Review> array = (ArrayList<Review>) this.mAdapter.getArrayList();

        ReviewListDialogFragment dialogFrag = ReviewListDialogFragment.newInstance(this.mMovie, array, selectedIndex);
        dialogFrag.show(this.getActivity().getSupportFragmentManager(), null);
    }

    /**
     * ** LOADERS **
     */


    @Override
    public Loader<List<Object>> onCreateLoader(int id, Bundle args)
    {
        switch (id)
        {
            case LOADER_ID_REVIEWS:
            {
                return new ReviewAsyncTaskLoader(this.getActivity(), this.mMovie.getRefID());
            }
            case LOADER_ID_TRAILERS:
            {
                return new TrailerAsyncTaskLoader(this.getActivity(), this.mMovie.getRefID());
            }
            default:
            {
                throw new IllegalArgumentException("loader id " + id + " isn't supported yet!");
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Object>> loader, List<Object> result)
    {
        switch (loader.getId())
        {
            case LOADER_ID_REVIEWS:
            {
                List<?> temp = result;
                List<Review> reviews = (List<Review>) temp;

                if (reviews == null || reviews.isEmpty())
                {
                    if (this.mEmptyTextViewReview.getVisibility() == View.GONE)
                    {
                        this.mReviewsRecyclerView.setVisibility(View.GONE);
                        this.mEmptyTextViewReview.setVisibility(View.VISIBLE);
                    }

                    if (NetworkUtils.isConnected(getActivity()))
                    {
                        this.mEmptyTextViewReview.setText(R.string.no_reviews_found);
                    } else
                    {
                        this.mEmptyTextViewReview.setText(R.string.check_internet_connection);
                    }

                    this.mAdapter.clear();

                } else
                {
                    if (this.mReviewsRecyclerView.getVisibility() == View.GONE)
                    {
                        this.mReviewsRecyclerView.setVisibility(View.VISIBLE);
                        this.mEmptyTextViewReview.setVisibility(View.GONE);
                    }

                    this.mAdapter.addAll(reviews);
                }
                break;
            }
            case LOADER_ID_TRAILERS:
            {
                List<?> temp = result;
                this.mTrailerList = (List<Trailer>) temp;

                this.mTrailerAdapter.addAll(this.mTrailerList);


                if (this.mTrailerList != null && this.mTrailerList.size() > 0)
                {
                    this.mFrameLayout.setOnClickListener(this);
                }
                Log.i(TAG, "onLoadFinished: stop! just got the trailers!!!");

                break;
            }
            default:
            {
                Log.wtf(TAG, "Fx onLoadFinished(): loader_id='" + loader.getId() + "' isn't implemented yet!!!", null);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Object>> loader)
    {
        switch (loader.getId())
        {
            case LOADER_ID_REVIEWS:
            {
                this.mAdapter.clear();
                break;
            }
            case LOADER_ID_TRAILERS:
            {
                break;
            }
        }
    }






    // Background thread to fetch the Reviews
    private static final class ReviewAsyncTaskLoader extends AsyncTaskLoader<List<Object>>
    {
        private long mRefID;

        public ReviewAsyncTaskLoader(Context context, long refId)
        {
            super(context);
            this.mRefID = refId;
        }

        @Override
        protected void onStartLoading()
        {
            this.forceLoad();
        }

        @Override
        public List<Object> loadInBackground()
        {
            return SyncUtils.getReviews(this.getContext(), this.mRefID);
        }
    }

    // background thread to fetch trailers
    private static final class TrailerAsyncTaskLoader extends AsyncTaskLoader<List<Object>>
    {
        private long mRefID;

        public TrailerAsyncTaskLoader(Context context, long refID)
        {
            super(context);
            this.mRefID = refID;
        }

        @Override
        protected void onStartLoading()
        {
            this.forceLoad();
        }

        @Override
        public List<Object> loadInBackground()
        {
            return SyncUtils.getTrailers(this.getContext(), this.mRefID);
        }
    }


    // get the color of the TextView's background based upon the value of the rating
    private static int getRatingColor(double value)
    {
        int ratingColorResourceId;
        switch ((int) Math.floor(value))
        {
            case 0:
            case 1:
                ratingColorResourceId = R.color.rating1;
                break;
            case 2:
                ratingColorResourceId = R.color.rating2;
                break;
            case 3:
                ratingColorResourceId = R.color.rating3;
                break;
            case 4:
                ratingColorResourceId = R.color.rating4;
                break;
            case 5:
                ratingColorResourceId = R.color.rating5;
                break;
            case 6:
                ratingColorResourceId = R.color.rating6;
                break;
            case 7:
                ratingColorResourceId = R.color.rating7;
                break;
            case 8:
                ratingColorResourceId = R.color.rating8;
                break;
            case 9:
                ratingColorResourceId = R.color.rating9;
                break;
            default:
                ratingColorResourceId = R.color.rating10plus;
                break;
        }
        return ratingColorResourceId;
    }
}
