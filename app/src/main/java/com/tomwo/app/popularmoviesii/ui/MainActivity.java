package com.tomwo.app.popularmoviesii.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.tomwo.app.popularmoviesii.R;
import com.tomwo.app.popularmoviesii.Shared;
import com.tomwo.app.popularmoviesii.ui.adapters.MainAdapter;
import com.tomwo.app.popularmoviesii.ui.fragments.MainActivityFragment;
import com.tomwo.app.popularmoviesii.model.supportClasses.Movie;

public class MainActivity extends AppCompatActivity implements MainAdapter.IMovieClickListener
{
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // request feature must be called before adding content
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            // invoked BEFORE adding content to the screen
            this.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

            this.getWindow().setEnterTransition(new Explode());
            this.getWindow().setExitTransition(new Slide(Gravity.START));
        }
        this.setContentView(R.layout.activity_main);

        // set the toolbar as the ActionBar
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        // This has no visible effect since we are using a NoActionBar theme
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setElevation(0f);
        }
        // set the default values for Preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                this.startActivityForResult(intent, Shared.SETTINGS_REQUEST_CODE, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            } else
            {
                this.startActivityForResult(intent, Shared.SETTINGS_REQUEST_CODE);
            }
        } else
        {
            Log.w(TAG, "Fx onOptionsItemSelected(): menuItem " + item.getTitle().toString() + " isn't supported in MainActivity");
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // switch statement is added for clarity;
        if (requestCode == Shared.SETTINGS_REQUEST_CODE)
        {
            if (resultCode == Shared.RESULT_SUCCESS_CODE)
            {
                String syncType = data.getStringExtra(Shared.SYNC_TYPE_VALUE);
                Log.d(TAG, "Fx onActivityResult(): syncType='" + syncType + "'");

                // was this changed?
                if (syncType == null) // || syncType.equals(""))
                {
                    return;
                }

                MainActivityFragment fragment = (MainActivityFragment) this.getSupportFragmentManager().findFragmentById(R.id.movieListsFragment);
                LoaderManager.getInstance(this).restartLoader(Shared.CURSOR_LOADER_ID, null, fragment);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(Movie movie, int index)
    {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Movie.KEY_MOVIE, movie);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            this.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            return;
        }
        this.startActivity(intent);
    }

}
