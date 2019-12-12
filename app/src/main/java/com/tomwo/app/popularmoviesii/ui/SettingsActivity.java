package com.tomwo.app.popularmoviesii.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;

import com.tomwo.app.popularmoviesii.R;
import com.tomwo.app.popularmoviesii.Shared;
import com.tomwo.app.popularmoviesii.ui.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.ISettingsFragmentInteraction
{
    public static final String LIFECYCLE_INSTANCE_FIELD_RESTART_LOADER = "lifecycleInstanceFieldRestartLoader";

    private String mOrderBy;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // must be invoked BEFORE adding content to the screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            // request feature must be called before adding content
            this.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            this.getWindow().setEnterTransition(new Slide(Gravity.END));
            this.getWindow().setExitTransition(new Slide(Gravity.START));
        }

        // set the contentLayout
        this.setContentView(R.layout.activity_settings);

        // set the toolbar
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        this.mOrderBy = savedInstanceState.getString(LIFECYCLE_INSTANCE_FIELD_RESTART_LOADER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            // fired when the user presses the backBtn in header
            case android.R.id.home:
                this.transitionBack();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // fired when the user hits the back button
    @Override
    public void onBackPressed()
    {
        this.transitionBack();
    }

    private void transitionBack()
    {
        Intent intent = new Intent();
        intent.putExtra(Shared.SYNC_TYPE_VALUE, this.mOrderBy);

        this.setResult(Shared.RESULT_SUCCESS_CODE, intent);

        this.supportFinishAfterTransition();
    }

    @Override
    public void notifyParentOnPreferenceChange(String orderBy)
    {
        this.mOrderBy = orderBy;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putString(LIFECYCLE_INSTANCE_FIELD_RESTART_LOADER, this.mOrderBy);
    }
}
