package com.tomwo.app.popularmoviesii.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by wooldridgetm on 6/1/17.
 */
public class NetworkUtils
{
    public static final int TIMEOUT_READ = 10000;    /* milliseconds */
    public static final int TIMEOUT_CONNECT = 15000; /* milliseconds */

    private NetworkUtils()
    {
        // private constructor - we don't want to create any instances
    }

    /**
     * @return true or false, depending on whether we have a network connection
     */
    public static boolean isConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }
}
