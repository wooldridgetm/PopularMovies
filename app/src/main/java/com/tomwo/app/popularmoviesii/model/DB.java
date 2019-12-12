package com.tomwo.app.popularmoviesii.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.tomwo.app.popularmoviesii.model.supportClasses.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wooldridgetm on 5/23/17.
 */
public class DB
{
    public static final String AUTHORITY = "com.tomwo.app.popularmoviesii.model";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://"+AUTHORITY);

    public static final String PATH_MOVIES = Movies.TABLE_NAME;
    public static final String PATH_FAVORITES = Favorites.TABLE_NAME;
    public static final String PATH_MOVIE_FAVORITES_VIEW = MovieFavoritesView.VIEW_NAME;

    private DB()
    {
        // empty constructor - do not want this instantiated
    }

    public static class Movies implements BaseColumns
    {
        // tablename
        public static final String TABLE_NAME = "movies";
        public static final String TRIGGER_NAME = "insertMoviesCheckSyncType";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        // column names for the table
        public static final String COLUMN_REFID = "_refID";  // "id" property in the JSON response from moviedb.org
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VOTE_COUNT = "vote_count";

        /**
         * isFavorite
         *
         * @deprecated this column was extracted into the Table 'Favorites' s.t. when the data updates, 'isFavorite' user data won't be wiped from the db <br>
         *             7.11.2017 <br>
         *
         *
         */
        @Deprecated
        public static final String COLUMN_FAVORITE = "isFavorite";

        /*
         * App's protected columns
         *
         */
        public static final String COLUMN_SYNC_TYPE = "_syncType";
        public static final String COLUMN_PAGE ="_page";
        public static final String COLUMN_TIMESTAMP = "_timeStamp";
        public static final String COLUMN_USER = "_user";
        public static final String COLUMN_LAST_SYNC = "_lastSync";
        public static final String COLUMN_VERSION = "_version";

        // SQLStatements for Creating the table
        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+ " ("+
                _ID+" INTEGER PRIMARY KEY AUTOINCREMENT"
                +","+COLUMN_TITLE+" TEXT"
                +","+COLUMN_REFID+" INTEGER NOT NULL"
                +","+COLUMN_OVERVIEW+" TEXT"
                +","+COLUMN_POSTER_PATH+" TEXT"
                +","+COLUMN_BACKDROP_PATH+" TEXT"
                +","+COLUMN_RELEASE_DATE+" TEXT"
                +","+COLUMN_VOTE_COUNT+" INTEGER"
                +","+COLUMN_VOTE_AVERAGE+" REAL"
                +","+COLUMN_USER+" TEXT"
                +","+COLUMN_SYNC_TYPE+" TEXT DEFAULT 'popular'"
                +","+COLUMN_PAGE+" INTEGER"
                +","+COLUMN_TIMESTAMP+" TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                +","+COLUMN_LAST_SYNC+" INTEGER"  // convert to DATE
                +","+COLUMN_VERSION+" INTEGER DEFAULT "+1+""
                +");"
                ;

        public static final String SQL_CREATE_TRIGGER = "CREATE TRIGGER IF NOT EXISTS "+TRIGGER_NAME+" "
                +"BEFORE INSERT ON "+TABLE_NAME+" "
                +"FOR EACH ROW "  // not necessary
                +"BEGIN "
                +"DELETE FROM "+TABLE_NAME+" WHERE "+COLUMN_REFID+"=NEW."+COLUMN_REFID+" AND "+COLUMN_SYNC_TYPE+"=NEW."+COLUMN_SYNC_TYPE+"; "
                +"END;";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS "+TABLE_NAME+";";

    }


    public static class Favorites implements BaseColumns
    {
        public static final String TABLE_NAME = "favorites";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        // values for isFavorite
        public static final int VALUE_FAVORITE_FALSE = 0;
        public static final int VALUE_FAVORITE_TRUE  = 1;

        // columns
        public static final String COLUMN_REFID = "_refID";
        public static final String COLUMN_FAVORITE = "isFavorite";
        public static final String COLUMN_TIMESTAMP = "_timeStamp";

        public static final String SQL_CREATE_TBL = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME
                +" ("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT"
                +","+COLUMN_REFID+" INTEGER NOT NULL"
                +","+COLUMN_FAVORITE+" INTEGER DEFAULT 0"
                +","+COLUMN_TIMESTAMP+" DATETIME DEFAULT CURRENT_TIMESTAMP"
                +", UNIQUE ("+COLUMN_REFID+") ON CONFLICT REPLACE"
                +");";

        public static final String SQL_DROP_TBL = "DROP TABLE IF EXISTS "+TABLE_NAME+";";
    }

    public static class MovieFavoritesView
    {
        public static final String VIEW_NAME = "movie_favorites_view";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(VIEW_NAME).build();

        public static final String SQL_CREATE_VIEW = "CREATE VIEW IF NOT EXISTS "+VIEW_NAME+" AS "+
                "SELECT movies."+Movies._ID+", movies."+Movies.COLUMN_REFID
                +","+Movies.COLUMN_TITLE
                +","+Movies.COLUMN_OVERVIEW
                +",fav."+Favorites.COLUMN_FAVORITE
                +","+Movies.COLUMN_RELEASE_DATE
                +","+Movies.COLUMN_VOTE_COUNT
                +","+Movies.COLUMN_VOTE_AVERAGE
                +","+Movies.COLUMN_SYNC_TYPE
                +","+Movies.COLUMN_PAGE
                +","+Movies.COLUMN_POSTER_PATH
                +","+Movies.COLUMN_BACKDROP_PATH
                +" FROM "+Movies.TABLE_NAME+" LEFT OUTER JOIN "+Favorites.TABLE_NAME+" fav "
                +" ON fav."+Favorites.COLUMN_REFID+" = movies."+Movies.COLUMN_REFID

                +";";

        public static final String SQL_DROP_VIEW = "DROP VIEW IF EXISTS "+VIEW_NAME+";";
    }


    /**
     *
     *
     * Utils Class --> utility, debugging methods for working with SQLite
     *
     *
     *
     */
    public static class Utils
    {
        private static final String TAG = "DB.Utils";

        public static void insertNewMovie(@NonNull final Context context)
        {
            @SuppressLint("StaticFieldLeak")
            AsyncTask<Context, Void, Uri> task = new AsyncTask<Context, Void, Uri>()
            {
                @Override
                protected Uri doInBackground(Context... contexts)
                {
                    Movie movie = new Movie();
                    movie.setTitle("Chittie Chittie, Bang Bang, We love you");
                    movie.setOverview("family-friendly musical about a motor-car & lots of singing.");
                    movie.setReleaseDate("1960");

                    ContentValues value = new ContentValues();
                    value.put(Movies.COLUMN_TITLE, movie.getTitle());
                    value.put(Movies.COLUMN_OVERVIEW, movie.getOverview());
                    value.put(Movies.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
                    value.put(Movies.COLUMN_POSTER_PATH, movie.getPosterPath());
                    value.put(Movies.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                    value.put(Movies.COLUMN_USER, "twooldridge");

                    return context.getContentResolver().insert(Movies.CONTENT_URI, value);
                }

                @Override
                protected void onPostExecute(Uri uri)
                {
                    Toast.makeText(context, uri.toString(), Toast.LENGTH_SHORT).show();
                }
            };

            task.execute();
        }

        public static void deleteAllData(@NonNull final Context context)
        {
            @SuppressLint("StaticFieldLeak")
            AsyncTask<Context, Void, Integer> task = new AsyncTask<Context, Void, Integer>()
            {
                @Override
                protected Integer doInBackground(Context... params)
                {
                    Context context = params[0];
                    int rowsDeleted = context.getContentResolver().delete(Movies.CONTENT_URI, null, null);

                    Log.d(TAG, "Fx doInBackground(): num_of_rows_deleted='" + rowsDeleted + "'");
                    return rowsDeleted;
                }

                @Override
                protected void onPostExecute(Integer integer)
                {
                    Toast.makeText(context, integer + " rows were deleted!", Toast.LENGTH_SHORT).show();
                }
            };

            task.execute(context);
        }

        public static void resetDb(final @NonNull Context context)
        {
            @SuppressLint("StaticFieldLeak")
            AsyncTask<Context, Void, Void> task = new AsyncTask<Context, Void, Void>()
            {
                @Override
                protected Void doInBackground(Context... params)
                {
                    Context context = params[0];
                    context.getContentResolver().call(DB.Movies.CONTENT_URI, MovieContentProvider.RESET_METHOD, null, null);
                    return null;
                }
            };

            task.execute(context);
        }

        public static void insertFakData(@NonNull final Context context)
        {
            AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>()
            {
                @Override
                protected Integer doInBackground(Void... params)
                {
                    return doBulkInsert(context);
                }

                @Override
                protected void onPostExecute(Integer i)
                {
                    Toast.makeText(context, i + " number of rows were inserted into table Movies", Toast.LENGTH_SHORT).show();

                }
            };

            // execute the background task
            task.execute();
        }

        private static Integer doBulkInsert(Context context)
        {
            // get the JSON string & parse it
            List<Movie> items = parseJSON(getJSON(context));

            if (items == null)
            {
                return 0;
            }

            ContentValues[] contentValues = new ContentValues[items.size()];
            ContentValues value;
            for (int i = 0; i < items.size(); i++)
            {
                Movie movie = items.get(i);

                value = new ContentValues();
                value.put(Movies.COLUMN_TITLE, movie.getTitle());
                value.put(Movies.COLUMN_OVERVIEW, movie.getOverview());
                value.put(Movies.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
                value.put(Movies.COLUMN_POSTER_PATH, movie.getPosterPath());
                value.put(Movies.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                value.put(Movies.COLUMN_USER, "twooldridge");

                contentValues[i] = value;
            }

            int numOfRowsInserted = context.getContentResolver().bulkInsert(Movies.CONTENT_URI, contentValues);
            Log.d(TAG, "Fx doBulkInsert(): num_of_rows_inserted: " + numOfRowsInserted);

            return numOfRowsInserted;
        }

        private static String getJSON(Context context)
        {
            String jsonStr = null;
            try
            {
                InputStream inputStream = context.getAssets().open("json/exampleMovies.json");
                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();
                jsonStr = new String(buffer, "UTF-8");

            } catch (IOException e)
            {
                Log.e(TAG, "Fx insertFakData(): ", e);
            }

            return jsonStr;
        }

        private static List<Movie> parseJSON(String jsonStr)
        {
            try
            {
                JSONObject json = new JSONObject(jsonStr);
                JSONArray results = json.getJSONArray("results");

                final String NA = "n.a.";

                final String TITLE = "title";
                final String OVERVIEW = "overview";
                final String POSTER_PATH = "poster_path";
                final String BACKDROP_PATH = "backdrop_path";
                final String RELEASE_DATE = "release_date";
                final String VOTE_AVERAGE = "vote_average";
                final String VOTE_TOTAL = "vote_count";

                // cache the result
                int length = results.length();

                // String[] items = new String[length];
                List<Movie> items = new ArrayList<>();
                Movie item;
                JSONObject jsonObj;
                for (int i = 0; i < length; i++)
                {
                    // items[i] = results.getJSONObject(i).getString("title");
                    // Log.v(TAG,"movie title: "+items[i]);
                    jsonObj = results.getJSONObject(i);

                    if (jsonObj == null)
                    {
                        // shouldn't happen
                        continue;
                    }

                    // create a new MoviePM item
                    item = new Movie(jsonObj.optString(TITLE, NA));
                    // Log.v(TAG, "Movie Title: " + item.getTitle());

                    if (jsonObj.has("id"))
                    {
                        item.setId(jsonObj.getLong("id"));
                    }

                    // overview
                    if (jsonObj.has(OVERVIEW))
                    {
                        item.setOverview(jsonObj.getString(OVERVIEW));
                    }
                    // poster_path
                    if (jsonObj.has(POSTER_PATH))
                    {
                        item.setPosterPath(jsonObj.getString(POSTER_PATH));
                        // Log.d(TAG,"Fx parseJSON() - "+item.getPosterPath());
                    }

                    // backdrop_path
                    if (jsonObj.has(BACKDROP_PATH))
                    {
                        item.setBackdropPath(jsonObj.getString(BACKDROP_PATH));
                    }

                    // vote_count
                    if (jsonObj.has(VOTE_TOTAL))
                    {
                        item.setVoteCount(jsonObj.getLong(VOTE_TOTAL));
                    }

                    // vote_average
                    if (jsonObj.has(VOTE_AVERAGE))
                    {
                        item.setVoteAverage(jsonObj.getDouble(VOTE_AVERAGE));
                    }

                    // release_date
                    if (jsonObj.has(RELEASE_DATE))
                    {
                        item.setReleaseDate(jsonObj.getString(RELEASE_DATE));
                    }
                    items.add(item);
                }

                return items;

            } catch (JSONException e)
            {
                e.printStackTrace();
                Log.e(TAG, "Fx insertFakData(): error parsing jsonString", e);
                Log.w(TAG, jsonStr);
            }

            return null;
        }
    }
}
