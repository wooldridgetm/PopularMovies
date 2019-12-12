package com.tomwo.app.popularmoviesii.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tomwo.app.popularmoviesii.R;

import java.util.Date;

/**
 * Created by wooldridgetm on 7/2/17.
 */

public class PreferenceUtils
{
    private static final String TAG = "PreferenceUtils";

    public static final String KEY_FILTER_INDEX = "filter-index";
    public static final String KEY_LAST_NOTIFICATION_DATE = "last-notification-date";

    private static final int DEFAULT_FILTER_INDEX = 0;

    public static void updateFilterIndex(Context context, int index)
    {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(KEY_FILTER_INDEX, index);
        editor.apply();
    }

    public static int getFilterIndex(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(KEY_FILTER_INDEX, DEFAULT_FILTER_INDEX);
    }

    public static boolean isEnabledNotifications(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(context.getString(R.string.pref_key_notifications), context.getResources().getBoolean(R.bool.notifications));
    }

    public static Date getLastNofificationDate(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        long time = pref.getLong(KEY_LAST_NOTIFICATION_DATE, 0);
        Date d = new Date(time);
        Log.d(TAG, "Fx getLastNofificationDate(): date='"+d.toString()+"'");
        return d;
    }

    public static void updateLastNotificationDate(Context context)
    {
        // preferences.edit().putLong(...args).apply();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Date currentDate = new Date();
        editor.putLong(KEY_LAST_NOTIFICATION_DATE, currentDate.getTime());
        editor.apply();
    }
}
