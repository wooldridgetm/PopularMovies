package com.tomwo.app.popularmoviesii.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tomwo.app.popularmoviesii.model.DB.Movies;
import com.tomwo.app.popularmoviesii.model.DB.MovieFavoritesView;
import com.tomwo.app.popularmoviesii.model.DB.Favorites;

/**
 * Created by wooldridgetm on 5/23/17.
 */
public class SQLiteHelper extends SQLiteOpenHelper
{
    private static final String TAG = "SQLiteHelper";

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 10;

    public SQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // create the 'movies','favorites' & 'movie_favorites_view'
        db.execSQL(Movies.SQL_CREATE_TABLE);
        db.execSQL(Movies.SQL_CREATE_TRIGGER);
        db.execSQL(Favorites.SQL_CREATE_TBL);
        db.execSQL(MovieFavoritesView.SQL_CREATE_VIEW);
    }

    /**feel
     * Fx onUpgrade(db, oldVersion, newVersion)
     *
     * <b>EDITS:</b>
     * <i>6.2.2017</i>
     * &nbsp; - added condition where oldVersion == 1 && newVersion == 3 (user missed the last update)
     * &nbsp; - added condition where oldVersion == 2 && newVersion == 3 (user is keeping with the updates)
     *
     * <i>6.6.2017</i> - upgrade from v7 to v8
     * &nbsp; - added
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion == 1 && newVersion == 2)
        {
            db.execSQL("ALTER TABLE "+Movies.TABLE_NAME+" ADD COLUMN "+Movies.COLUMN_VOTE_COUNT+" INTEGER;");
            db.execSQL("ALTER TABLE "+Movies.TABLE_NAME+" ADD COLUMN "+Movies.COLUMN_VOTE_AVERAGE+" REAL;");
            return;
        }

        // user missed the
        if (oldVersion == 1 && newVersion == 3)
        {
            db.execSQL("ALTER TABLE "+Movies.TABLE_NAME+" ADD COLUMN "+Movies.COLUMN_VOTE_COUNT+" INTEGER;");
            db.execSQL("ALTER TABLE "+Movies.TABLE_NAME+" ADD COLUMN "+Movies.COLUMN_VOTE_AVERAGE+" REAL;");

            // added on version 3
            db.execSQL("ALTER TABLE "+Movies.TABLE_NAME+" ADD COLUMN "+Movies.COLUMN_FAVORITE+" BOOLEAN DEFAULT 0;");
            return;
        }

        // just need 1 column
        if (oldVersion == 2 && newVersion == 3)
        {
            // added on version 3
            db.execSQL("ALTER TABLE "+Movies.TABLE_NAME+" ADD COLUMN "+Movies.COLUMN_FAVORITE+" BOOLEAN DEFAULT 0;");
            return;
        }

        // 3 & 4
        if (oldVersion <= 3 && newVersion == 4)
        {
            db.execSQL(Movies.SQL_DROP_TABLE);
            db.execSQL(Movies.SQL_CREATE_TABLE);
            return;
        }

        // 4 & 5
        // removed  +", UNIQUE("+Movies.COLUMN_REFID+") ON CONFLICT REPLACE"
        if (oldVersion == 4 && newVersion == 5)
        {
            db.execSQL(Movies.SQL_DROP_TABLE);
            db.execSQL(Movies.SQL_CREATE_TABLE);
            return;
        }

        if (oldVersion == 6 && newVersion == 7)
        {
            db.execSQL("ALTER TABLE "+Movies.TABLE_NAME+" ADD COLUMN "+Movies.COLUMN_PAGE+" INTEGER;");
            return;
        }

        if (newVersion == 8 || (oldVersion <= 5 && newVersion == 7))
        {
            // v8, changed datatype of COLUMN_FAVORITE

            db.execSQL(Movies.SQL_DROP_TABLE);
            db.execSQL(Movies.SQL_CREATE_TABLE);
            db.execSQL(Movies.SQL_CREATE_TRIGGER);
            return;
        }

        if (newVersion >= 9)
        {
            // v9, added table 'Favorites'
            db.execSQL(Favorites.SQL_CREATE_TBL);
        }

        if (newVersion == 10)
        {
            // v10, removed column 'isFavorite' from 'movies' table
            // added in VIEW movie_favorites_view as a JOIN of 'movies' & 'favorites'
            db.execSQL(Movies.SQL_DROP_TABLE);
            db.execSQL(Movies.SQL_CREATE_TABLE);
            db.execSQL(Movies.SQL_CREATE_TRIGGER);

            db.execSQL(MovieFavoritesView.SQL_CREATE_VIEW);
        }
    }




}
