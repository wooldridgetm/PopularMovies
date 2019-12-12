package com.tomwo.app.popularmoviesii.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;

import com.like.LikeButton;
import com.tomwo.app.popularmoviesii.R;
import com.tomwo.app.popularmoviesii.ui.adapters.TrailerAdapter;
import com.tomwo.app.popularmoviesii.databinding.ActivityDetailBinding;
import com.tomwo.app.popularmoviesii.model.supportClasses.Movie;
import com.tomwo.app.popularmoviesii.model.supportClasses.Trailer;

public class DetailActivity extends AppCompatActivity implements TrailerAdapter.ITrailerListener
{
    private static final String TAG = "DetailActivity";

    // current movie
    private Movie mMovie;

    // don't like this - this requires a tight coupling btw DetailActivityFragment && DetailActivity!
    public Movie getMovie()
    {
        return this.mMovie;
    }

    public ActivityDetailBinding mBinding;
    public ActivityDetailBinding getBinding()
    {
        return this.mBinding;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        trace("Fx onCreate - DetailActivity");
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            // invoked BEFORE adding content to the screen
            this.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

            this.getWindow().setEnterTransition(new Explode());
            this.getWindow().setExitTransition(new Slide(Gravity.START));
        }

        if (savedInstanceState != null)
        {
            this.mMovie = savedInstanceState.getParcelable(Movie.KEY_MOVIE);
        } else
        {
            // activity was started as an Activity
            this.mMovie = this.getIntent().getExtras().getParcelable(Movie.KEY_MOVIE);
        }

        // this.setContentView(R.layout.activity_detail);
        this.mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // toolbar
        Toolbar toolbar = this.mBinding.toolbarDetail;
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setElevation(0f);

        final LikeButton mStarButton = this.mBinding.starBtn;
        mStarButton.setLiked(this.mMovie.isFavorite());

        final CollapsingToolbarLayout collapsingToolbar = this.mBinding.collapsingToolbar;

        AppBarLayout appBarLayout = this.mBinding.appbar;
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener()
        {
            @Override
            public void onOffsetChanged(AppBarLayout layout, int verticalSlop)
            {
                if (collapsingToolbar.getHeight() + verticalSlop < 2 * ViewCompat.getMinimumHeight(collapsingToolbar))
                {
                    mStarButton.animate().alpha(0).setDuration(600);
                } else
                {
                    mStarButton.animate().alpha(1).setDuration(600);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            this.supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Trailers is triggered!
    @Override
    public void trailer_triggeredHandler(Trailer item)
    {
        // https://www.youtube.com/watch?v=Ax6md6HoZ2o
        Uri uri = Uri.parse("https://www.youtube.com/watch?v=" + item.getKey());
        Log.d(TAG, "uri='" + uri.toString() + "'");

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);

        if (intent.resolveActivity(this.getPackageManager()) != null)
        {
            this.startActivity(intent);
        }
    }

    // need to save the current movie when device is rotated
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        trace("Fx onSavedInstanceState() - DetailActivity");
        super.onSaveInstanceState(outState);

        outState.putParcelable(Movie.KEY_MOVIE, this.mMovie);
    }

    private static void trace(String msg)
    {
        Log.i(TAG, msg);
    }
}
