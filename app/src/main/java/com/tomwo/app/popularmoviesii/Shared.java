package com.tomwo.app.popularmoviesii;

import com.tomwo.app.popularmoviesii.model.supportClasses.FilterItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wooldridgetm on 6/5/17.
 */

public class Shared
{
    // reference to our CursorLoader in MovieListsFragment
    public static final int CURSOR_LOADER_ID = 6112;

    // used by MainActivity, this.startActivityForResult(intent, SETTINGS_REQUEST_CODE)
    public static final int SETTINGS_REQUEST_CODE = 57;

    // used by SettingsActivity, Fx onOptionsItemSelected()
    public static final String SYNC_TYPE_VALUE = "restartLoaderValue";

    //
    // used to indicate everything worked fine in SettingsActivity
    public static final int RESULT_SUCCESS_CODE = 12;


    // used when user presses the StarBtn in DetailActivityFragment
    public static final String BUNDLE_PARAM_REFID = "bundleParamRefID";
    public static final String BUNDLE_PARAM_ISFAVORITE = "bundleParamIsFavorite";

    // number of items returned from a Network Request
    public static final int PAGE_SIZE = 20;




    public static final List<FilterItem> FILTER_ITEMS;
    static
    {
        FILTER_ITEMS = new ArrayList<>();
        FILTER_ITEMS.add(new FilterItem("Popular", "popular", false));
        FILTER_ITEMS.add(new FilterItem("Top Rated", "top_rated", false));
        FILTER_ITEMS.add(new FilterItem("Favorites", "favorites", false));
    }



    // for debugging only
    public static final String LINESPACE = "																																.";
}
