package com.tomwo.app.popularmoviesii.tasks;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.tomwo.app.popularmoviesii.BuildConfig;
import com.tomwo.app.popularmoviesii.utils.PreferenceUtils;

/**
 * Created by wooldridgetm on 6/2/17.
 */
public class BackgroundTasks
{
    private static final String TAG = "BackgroundTasks";

    // actions in the intent
    public static final String ACTION_SYNC_MOVIES = "sync-movies";
    public static final String ACTION_REFRESH_DB = "refresh-database";
    public static final String ACTION_SYNC_DATA_NOW = "sync-movies-now";
    public static final String ACTION_SAVE_FAVORITE_SELECTION_DB = "save-favorite-selection-db";
    public static final String ACTION_UPDATE_PREFERENCES = "update-preferences";


    public static final String BUNDLE_PARAM_PAGINATION = "bundleParamPagination";

    public static void execute(Context context, String action)
    {
        execute(context, action, null);
    }


    /**
     * All Tasks that execute in teh background will go through here...
     * This class acts like a funnel
     *
     * @param context - context of the App
     * @param action - which background operation to take?
     * @param bundle - key-value pairs for extra info (i.e. orderBy & pagination)
     */
    public static void execute(Context context, String action, Bundle bundle)
    {
        switch (action)
        {
            case ACTION_SYNC_MOVIES:
            {
                int page = bundle.getInt(BUNDLE_PARAM_PAGINATION);
                if (page < 1) page = 1;

                SyncUtils.sync(context, page);
                break;
            }
            case ACTION_REFRESH_DB:
            case ACTION_SYNC_DATA_NOW:
            {
                // delete everything but ensure that the
                SyncUtils.refresh(context);
                break;
            }
            case ACTION_SAVE_FAVORITE_SELECTION_DB:
            {
                DBUtils.saveFavoriteSelection(context, bundle);
                break;
            }
            case ACTION_UPDATE_PREFERENCES:
            {
                int filterSelectedIndex = bundle.getInt(PreferenceUtils.KEY_FILTER_INDEX);
                PreferenceUtils.updateFilterIndex(context,filterSelectedIndex);
                break;
            }
            default:
            {
                Log.wtf(TAG, "error, unknown action='" + action + "'");

                // alert dev when debugging!
                if (BuildConfig.DEBUG)
                {
                    throw new UnsupportedOperationException("unknown action: " + action);
                }

                break;
            }
        }
    } // end Fx execute()

}
