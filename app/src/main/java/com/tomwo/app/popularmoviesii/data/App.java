package com.tomwo.app.popularmoviesii.data;

import android.app.Application;
import android.util.Log;

import com.tomwo.app.popularmoviesii.tasks.SyncUtils;

import static com.tomwo.app.popularmoviesii.Shared.LINESPACE;

/**
 * Created by wooldridgetm on 7/16/17.
 */
public final class App extends Application
{
    private static final String TAG = "App";

    @Override
    public void onCreate()
    {
        super.onCreate();

        // schedule FirebaseJob
        // check if db is empty; if so, then do a sync
        SyncUtils.initialize(this.getApplicationContext());
    }
}
