package com.tomwo.app.popularmoviesii.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.tomwo.app.popularmoviesii.Shared;
import com.tomwo.app.popularmoviesii.model.DB;
import com.tomwo.app.popularmoviesii.model.DB.Favorites;

import static com.tomwo.app.popularmoviesii.Shared.BUNDLE_PARAM_ISFAVORITE;
import static com.tomwo.app.popularmoviesii.Shared.BUNDLE_PARAM_REFID;

/**
 * Created by wooldridgetm on 7/11/17.
 */

public class DBUtils
{



    public static void saveFavoriteSelection(Context context, Bundle bundle)
    {
        if (bundle == null) throw new IllegalArgumentException("bundle can't be null");

        long refID     = bundle.getLong(Shared.BUNDLE_PARAM_REFID);
        int isFavorite = bundle.getInt(Shared.BUNDLE_PARAM_ISFAVORITE);


        String[] projection = { Favorites._ID };
        final String WHERE_CLAUSE = Favorites.COLUMN_REFID+"="+refID;

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(Favorites.CONTENT_URI, projection, WHERE_CLAUSE, null, null);

        ContentValues values = new ContentValues();
        values.put(Favorites.COLUMN_FAVORITE, isFavorite);

        if (cursor == null || cursor.getCount() == 0)
        {
            // need to insert a record in the
            values.put(Favorites.COLUMN_REFID, refID);
            contentResolver.insert(Favorites.CONTENT_URI, values);

        } else
        {
            // just update the data
            cursor.moveToFirst();
            long id = cursor.getLong(cursor.getColumnIndex(Favorites._ID));
            Uri uri = ContentUris.withAppendedId(Favorites.CONTENT_URI,id);

            contentResolver.update(uri, values, null, null);
        }

    }

}
