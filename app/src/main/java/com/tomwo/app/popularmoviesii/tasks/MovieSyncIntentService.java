package com.tomwo.app.popularmoviesii.tasks;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by wooldridgetm on 6/1/17.
 */
public class MovieSyncIntentService extends IntentService
{
    private static final String TAG = "MovieSyncIntentService";
    public MovieSyncIntentService()
    {
        // default constructor
        // @param name Used to name the worker thread, important only for debugging.
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();

        BackgroundTasks.execute(this, action, bundle);
    }
}
