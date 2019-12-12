package com.tomwo.app.popularmoviesii.model;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tomwo.app.popularmoviesii.BuildConfig;
import com.tomwo.app.popularmoviesii.model.DB.Movies;

import static com.tomwo.app.popularmoviesii.Shared.LINESPACE;

/**
 * Created by wooldridgetm on 5/23/17.
 */
public class MovieContentProvider extends ContentProvider
{
    private static final String TAG = MovieContentProvider.class.getSimpleName();

    // Define final integer constants for the directory of tasks and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int MOVIES = 100;
    public static final int MOVIES_WITH_ID = 101;

    public static final int FAVORITES = 200;
    public static final int FAVORITES_WITH_ID = 201;

    public static final int MOVIE_FAVORITES_VIEW = 300;
    public static final int MOVIE_FAVORITES_VIEW_WITH_ID = 301;


    public static final String RESET_METHOD = "restMethod";

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private SQLiteHelper dbHelper;

    @Override
    public boolean onCreate()
    {
        this.dbHelper = new SQLiteHelper(this.getContext());
        return true; // ContentProvider successfully created
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri))
        {
            case MOVIES:
            {
                cursor = db.query(Movies.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case MOVIES_WITH_ID:
            {
                String id = uri.getLastPathSegment();
                Log.d(TAG, "querying for id: " + id);

                cursor = db.query(Movies.TABLE_NAME,
                                    projection,
                                    Movies._ID,
                                    new String[]{id},
                                    null,
                                    null,
                                    sortOrder);
                break;
            }
            case FAVORITES:
            {
                cursor = db.query(DB.Favorites.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case MOVIE_FAVORITES_VIEW:
            {
                cursor = db.query(DB.MovieFavoritesView.VIEW_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case MOVIE_FAVORITES_VIEW_WITH_ID:
            {
                // query.appendWhere(Movies._ID+" = "+uri.getLastPathSegment());
                String id = uri.getLastPathSegment();
                Log.d(TAG, "querying for id: " + id);

                cursor = db.query(DB.MovieFavoritesView.VIEW_NAME,
                        projection,
                        Movies._ID,
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("unknown Uri: " + uri);
            }
        }

        // configure to watch for content changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri)
    {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values)
    {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final ContentResolver cr = this.getContext().getContentResolver();

        Uri newUri;
        long rowId;

        switch (sUriMatcher.match(uri))
        {
            case MOVIES:
            {
                rowId = db.insert(Movies.TABLE_NAME, null, values);

                if (rowId > 0)
                {
                    newUri = ContentUris.withAppendedId(Movies.CONTENT_URI, rowId);

                    cr.notifyChange(uri, null);
                } else
                {
                    throw new SQLException("failed to insert a new record!");
                }
                break;
            }
            case FAVORITES:
            {
                rowId = db.insert(DB.Favorites.TABLE_NAME, null, values);

                if (rowId > 0)
                {
                    newUri = ContentUris.withAppendedId(DB.Favorites.CONTENT_URI, rowId);

                    cr.notifyChange(uri, null);
                    cr.notifyChange(DB.MovieFavoritesView.CONTENT_URI, null);
                } else
                {
                    throw new SQLException("failed to insert a new record!");
                }
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("unknown uri: " + uri);
            }
        }
        return newUri;
    }

    // invoked after we've downloaded the data.
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values)
    {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        int rowsInserted = 0;
        switch (sUriMatcher.match(uri))
        {
            case MOVIES:
            {
                db.beginTransaction();

                try
                {
                    // for-each loop, not the most efficient
                    for (ContentValues value : values)
                    {
                        db.insert(Movies.TABLE_NAME, null, value);
                        rowsInserted++;
                        Log.d(TAG, "Fx bulkInsert(): row inserted into 'movies' table, title='"+value.getAsString(Movies.COLUMN_TITLE)+"'");
                    }
                    db.setTransactionSuccessful(); // need this or the entire thing goes ca-flooey

                } catch (SQLException e)
                {
                    e.printStackTrace();
                    if (BuildConfig.DEBUG) throw new SQLiteException("error inserting data, Fx bulkInsert(). Error='"+e.toString()+"'");
                }

                // aborts, if 'db.setTransactionSuccessful()' isn't invoked
                db.endTransaction();
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("unknown Uri:" + uri);
            }
        }

        if (rowsInserted > 0)
        {
            ContentResolver cr = this.getContext().getContentResolver();

            cr.notifyChange(uri, null);
            cr.notifyChange(DB.MovieFavoritesView.CONTENT_URI, null);
        }
        Log.i(TAG, LINESPACE);
        Log.i(TAG, rowsInserted+" were inserted into the database!");
        return rowsInserted;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (sUriMatcher.match(uri))
        {
            case MOVIES:
            {
                rowsDeleted = db.delete(Movies.TABLE_NAME, null, null);
                break;
            }
            case MOVIES_WITH_ID:
            {
                rowsDeleted = db.delete(Movies.TABLE_NAME, Movies._ID + " = " + uri.getLastPathSegment(), selectionArgs);
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("unknown Uri: " + uri);
            }
        }

        if (rowsDeleted > 0)
        {
            this.getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        int rowsUpdated = 0;

        switch (sUriMatcher.match(uri))
        {
            case MOVIES_WITH_ID:
            {
                rowsUpdated = db.update(Movies.TABLE_NAME, values, Movies._ID + "=" + uri.getLastPathSegment(), selectionArgs);
                break;
            }
            case FAVORITES_WITH_ID:
            {
                rowsUpdated = db.update(DB.Favorites.TABLE_NAME, values, DB.Favorites._ID + "=" + uri.getLastPathSegment(), selectionArgs);
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("unknown URI: " + uri);
            }
        }

        if (rowsUpdated > 0)
        {
            this.getContext().getContentResolver().notifyChange(uri, null);
            this.getContext().getContentResolver().notifyChange(DB.MovieFavoritesView.CONTENT_URI, null);

            Uri viewUri = Uri.withAppendedPath(DB.MovieFavoritesView.CONTENT_URI, uri.getLastPathSegment());

            this.getContext().getContentResolver().notifyChange(viewUri, null);
        }

        return rowsUpdated;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras)
    {
        switch (method)
        {
            case RESET_METHOD:
            {
                this.reset();
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("unknown method '"+method+"'");
            }
        }

        return null;
    }

    private void reset()
    {
        Log.d(TAG, "Fx reset(): invoked!!!");

        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS "+ Movies.TABLE_NAME+";");
        db.execSQL(Movies.SQL_CREATE_TABLE);

        this.getContext().getContentResolver().notifyChange(Movies.CONTENT_URI, null);
    }

    /**
     * creates our UriMatcher based upon all the tables/queries we'll be running against our local db
     *
     * @return UriMatcher
     */
    private static UriMatcher buildUriMatcher()
    {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the movies directory and a single item by ID.
         */
        uriMatcher.addURI(DB.AUTHORITY, DB.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(DB.AUTHORITY, DB.PATH_MOVIES + "/#", MOVIES_WITH_ID);

        uriMatcher.addURI(DB.AUTHORITY, DB.PATH_FAVORITES, FAVORITES);
        uriMatcher.addURI(DB.AUTHORITY, DB.PATH_FAVORITES + "/#", FAVORITES_WITH_ID);

        uriMatcher.addURI(DB.AUTHORITY, DB.PATH_MOVIE_FAVORITES_VIEW, MOVIE_FAVORITES_VIEW);
        uriMatcher.addURI(DB.AUTHORITY, DB.PATH_MOVIE_FAVORITES_VIEW + "/#", MOVIE_FAVORITES_VIEW_WITH_ID);

        return uriMatcher;
    }


}
