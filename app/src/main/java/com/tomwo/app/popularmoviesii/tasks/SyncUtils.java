package com.tomwo.app.popularmoviesii.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.tomwo.app.popularmoviesii.BuildConfig;
import com.tomwo.app.popularmoviesii.Shared;
import com.tomwo.app.popularmoviesii.model.DB.Movies;
import com.tomwo.app.popularmoviesii.model.supportClasses.Review;
import com.tomwo.app.popularmoviesii.model.supportClasses.Trailer;
import com.tomwo.app.popularmoviesii.utils.NetworkUtils;
import com.tomwo.app.popularmoviesii.utils.NotificationUtils;
import com.tomwo.app.popularmoviesii.utils.PreferenceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.tomwo.app.popularmoviesii.Shared.LINESPACE;

/**
 * Created by wooldridgetm on 6/1/17.
 */
public class SyncUtils
{
    private static final String TAG = "SyncUtils";

    // flag to know if we've already run this code...
    private static boolean sIsInitialized = false;

    // used in scheduling our background sync
    private static final int SYNC_INTERVAL_HOURS = 48;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 6;

    // debugging only
    // have it sync every 9 mins!
    private static final int SYNC_INTERVAL_MINS = 9;
    private static final int ALT_SYNC_INTERVAL_SECONDS = (int) TimeUnit.MINUTES.toSeconds(SYNC_INTERVAL_MINS);
    private static final int ALT_SYNC_FLEXTIME_SECONDS = ALT_SYNC_INTERVAL_SECONDS / 3;

    public static void initialize(final Context context)
    {
        if (sIsInitialized)
        {
            return;
        }

        sIsInitialized = true;

        scheduleFirebaseJobService(context);

        // check if we have an empty db
        Thread emptyDatabase = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.i(TAG, LINESPACE);
                Log.d(TAG, "Fx initializee(): checking for empty database");
                Cursor cursor = context.getContentResolver().query(Movies.CONTENT_URI, new String[] { Movies._ID }, null,null,null);

                if (cursor == null || cursor.getCount() == 0)
                {
                    Log.i(TAG, "no records in database; do a sync immediately!!");

                    Intent intent = new Intent(context, MovieSyncIntentService.class);
                    intent.setAction(BackgroundTasks.ACTION_SYNC_DATA_NOW);
                    context.startService(intent);
                }
            }
        });

        // kick off the background thread to determine if we need to sync
        emptyDatabase.start();
    }

    private static void scheduleFirebaseJobService(@NonNull Context context)
    {
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);
        Job.Builder builder = jobDispatcher.newJobBuilder()
                .setService(MovieFirebaseJobService.class)
                .setTag(TAG)
                .setConstraints(Constraint.ON_UNMETERED_NETWORK)  // don't eat up their data
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                // .setTrigger(Trigger.executionWindow(ALT_SYNC_INTERVAL_SECONDS, ALT_SYNC_INTERVAL_SECONDS + ALT_SYNC_FLEXTIME_SECONDS))
                .setTrigger(Trigger.executionWindow(SYNC_INTERVAL_HOURS, SYNC_INTERVAL_HOURS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true);

        Job job = builder.build();

        // schedule the Job with the dispatcher!
        jobDispatcher.schedule(job);
    }

    /**
     * Fx sync(Context, page) &nbsp;
     * - this method is invoked when recyclerView requests more data & it's not stored locally
     *
     * @param context - Current App Context
     * @param page    - for pagination
     */
    synchronized public static void sync(@NonNull Context context, int page)
    {
        Log.d(TAG, "Fx sync()");
        String orderBy = Shared.FILTER_ITEMS.get(PreferenceUtils.getFilterIndex(context)).getKey();
        Log.d(TAG, "Fx sync(): filter='" + orderBy + "'.\nThis method is invoked when RecyclerView requests more data & it's not stored locally");

        URL url = createUrl(orderBy, "", page);
        String jsonStr = null;

        try
        {
            jsonStr = makeHttpRequest(url);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Fx request() - problem making HTTP request! " + orderBy, e);
        }

        if (TextUtils.isEmpty(jsonStr))
        {
            return;
        }
        if (BuildConfig.DEBUG)
        {
            writeJsonToFile(context, jsonStr);
        }

        // now, parse the JSON Str && insert the data
        ContentValues[] contentValuesArr;
        try
        {
            contentValuesArr = parseMovieJSON(jsonStr, orderBy);
        } catch (JSONException jsonException)
        {
            Log.e(TAG, "Fx refresh(): error parsing json='" + jsonStr + "'", jsonException);
            contentValuesArr = null;
        }

        if (contentValuesArr == null || contentValuesArr.length == 0 || contentValuesArr[0] == null)
        {
            // there was a parse error
            return;
        }

        // if page < 2, then we're not doing pagination
        // delete everything
        if (page < 2)
        {
            // delete all records in the database
            context.getContentResolver().delete(Movies.CONTENT_URI, null, null);
        }

        // bulkInsert
        context.getContentResolver().bulkInsert(Movies.CONTENT_URI, contentValuesArr);
    }

    /**
     * @param context - currentAppContext (Service)
     */
    synchronized public static void refresh(@NonNull Context context)
    {
        // Read from SharedPreferences to get the 'orderBy' value!
        ContentValues[] contentValuesArr = new ContentValues[]{};

        final int PAGINATION_START_NUM = 1;
        final int PAGINATION_END_NUM   = 3;

        URL url;
        final String[] ORDER_BY = {"popular", "top_rated"};
        int i = 0, paginationIndex = PAGINATION_START_NUM;


        do
        {
            do
            {
                url = createUrl(ORDER_BY[i], "", paginationIndex);
                String jsonStr = null;

                try
                {
                    jsonStr = makeHttpRequest(url);
                } catch (IOException e)
                {
                    Log.e(TAG, "Fx request() - problem making HTTP request! " + ORDER_BY[i], e);
                }

                if (TextUtils.isEmpty(jsonStr))
                {
                    return;
                }

                // used for debugging
                if (BuildConfig.DEBUG)
                {
                    writeJsonToFile(context, jsonStr);
                }

                // now, parse the JSON Str && insert the data
                ContentValues[] arr;
                try
                {
                    arr = parseMovieJSON(jsonStr, ORDER_BY[i]);
                } catch (JSONException jsonException)
                {
                    Log.e(TAG, "Fx refresh(): error parsing json='" + jsonStr + "'", jsonException);
                    arr = null;
                }

                if (arr != null && arr.length > 0)
                {
                    // concatenate the 2 arrays
                    contentValuesArr = concat(contentValuesArr, arr);
                }

                paginationIndex++;
            } while (paginationIndex < PAGINATION_END_NUM);

            paginationIndex = 1;
            i++;
        } while (i < ORDER_BY.length);

        // do we have do to insert?
        if (contentValuesArr == null || contentValuesArr.length == 0 || contentValuesArr[0] == null)
        {
            return;
        }

        // delete all records in the database
        context.getContentResolver().delete(Movies.CONTENT_URI, null, null);

        // now, insert the data
        context.getContentResolver().bulkInsert(Movies.CONTENT_URI, contentValuesArr);

        // check to see if user wants notifications. if so, then display 1
        if (PreferenceUtils.isEnabledNotifications(context))
        {
            Date lastNotifyDate = PreferenceUtils.getLastNofificationDate(context);
            Date currentDate = new Date();

            long lastNotifyDateInMS = lastNotifyDate.getTime();
            long currentDateInMS = currentDate.getTime();

            final long TWO_DAYS_IN_MS = 2 * 24 * 60 * 60 * 1000;  // 3 * 24 hrs * 60 min/hr * 60 sec/min * 1000 ms/sec
            final long TWO_MIN_IN_MS = 2 * 60 * 1000;
            if ((currentDateInMS - lastNotifyDateInMS) > TWO_DAYS_IN_MS)
            {
                NotificationUtils.notify(context);
                PreferenceUtils.updateLastNotificationDate(context);
            }
        }
    }

    /**
     * added on 7.6.2017
     * <p>
     * Concatenates 2 arrays
     *
     * @param a
     * @param b
     * @return a.concatenate(b);
     */
    synchronized private static ContentValues[] concat(ContentValues[] a, ContentValues[] b)
    {
        int aLen = a.length;
        int bLen = b.length;
        ContentValues[] c = new ContentValues[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    /**
     * @param context - current Context of the App
     * @param refID   - movieId
     */
    synchronized public static List<Object> getReviews(@NonNull Context context, long refID)
    {
        URL url = createUrl(Long.toString(refID), "reviews", 1);

        String jsonStr = null;
        try
        {
            jsonStr = makeHttpRequest(url);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Fx request() - problem making HTTP request for reviews!", e);
        }

        if (TextUtils.isEmpty(jsonStr))
        {
            return null;
        }

        // used for debugging
        if (BuildConfig.DEBUG)
        {
            writeJsonToFile(context, jsonStr);
        }

        // now, parse the response && return response to UI
        List<Review> reviews;
        try
        {
            reviews = parseReviewJSON(jsonStr);
        }
        catch (JSONException e)
        {
            Log.e(TAG, "Fx getReviews(): error parsing JSON. jsonStr='" + jsonStr + "'", e);
            reviews = null;
        }

        List<?> temp = reviews;
        List<Object> myReturn = (List<Object>) temp;

        return myReturn;
    }


    /**
     * Fx getTrailers(Context, refID)
     *
     * @param context - current calling context
     * @param refID   - refID of the movie
     * @return ArrayList of Trailerss
     */
    synchronized public static List<Object> getTrailers(Context context, long refID)
    {
        URL url = createUrl(Long.toString(refID), "videos", 1);
        Log.d(TAG, "Fx getTrailers(): url='" + url.toString() + "'");
        String jsonStr = null;
        try
        {
            jsonStr = makeHttpRequest(url);
        } catch (IOException e)
        {
            Log.e(TAG, "Fx request() - problem making HTTP request for trailers!!", e);
        }

        if (TextUtils.isEmpty(jsonStr))
        {
            return null;
        }

        // used for debugging
        if (BuildConfig.DEBUG)
        {
            writeJsonToFile(context, jsonStr);
        }

        // now, parse the response && return response to UI
        List<Trailer> trailers;
        try
        {
            trailers = parseTrailerJSON(jsonStr);
        } catch (JSONException e)
        {
            Log.e(TAG, "Fx getTrailersror parsing JSON. jsonStr='" + jsonStr + "'", e);
            trailers = null;
        }

        List<?> temp = trailers;
        List<Object> myObjects = (List<Object>) temp;

        return myObjects;
    }


    // Construct the URL for theMovieDb query
    // https://developers.themoviedb.org/3/movies/get-popular-movies
    // 3/movie/top_rated
    // 3/movie/popular
    // 3/movie/{refID}/videos
    // 3/movie/{refID}/reviews

    /**
     * @param orderByParam - String with the value of "popular" OR "top_rated" OR a specific "{movie_id}"; if a specific "{movie_id}" is used, then the details value cannot be empty!!!
     * @param details      - String w/ value of "videos" or "reviews". only used when the orderByParam="{movie_id}"
     * @param page         - for pagination purposes
     * @return URL
     */
    synchronized private static URL createUrl(String orderByParam, String details, int page)
    {
        if (page == -1)
        {
            page = 1;
        }

        URL url = null;
        try
        {
            final String LANGUAGE_PARAM = "language";
            final String PAGE_PARAM = "page";
            final String API_KEY = "api_key";

            // developers.themoviedb.org
            // popular/top_rated - http://api.themoviedb.org/3/movie/popular?language=en-US&api_key=6a2cca75922267640bc7afed282f893e
            // reviews - https://api.themoviedb.org/3/movie/1865/reviews?api_key=6a2cca75922267640bc7afed282f893e&language=en-US&page=1
            // trailer - https://api.themoviedb.org/3/movie/1865/videos?api_key=6a2cca75922267640bc7afed282f893e&language=en-US&page=1

            // https://www.youtube.com/watch?v=Ax6md6HoZ2o
            // Uri uri = Uri.parse("http://api.themoviedb.org/3/movie");
            Uri.Builder builder = new Uri.Builder();

            builder.scheme("http").authority("api.themoviedb.org").appendPath("3").appendPath("movie").appendPath(orderByParam);

            if (!orderByParam.equals("popular") && !orderByParam.equals("top_rated"))
            {
                builder.appendPath(details);
            }

            builder.appendQueryParameter(LANGUAGE_PARAM, "en-US")
                    .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
                    .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_V3_KEY)
                    .build();

            String myUrl = builder.toString();

            url = new URL(myUrl);
            Log.v(TAG, "Fx createUrl() - url: " + url.toString());

        } catch (MalformedURLException e)
        {
            Log.e(TAG, "Fx createUrl() - Problem building the URL ", e);
        }

        return url;
    }

    synchronized private static String makeHttpRequest(URL url) throws IOException
    {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        InputStream inputStream = null;
        String jsonResponse = "";

        try
        {
            // Create the request to moviedb.org, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(NetworkUtils.TIMEOUT_READ);
            urlConnection.setConnectTimeout(NetworkUtils.TIMEOUT_CONNECT);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200)
            {
                inputStream = urlConnection.getInputStream();

                jsonResponse = readFromStream(inputStream);
            } else
            {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }

        } catch (IOException e)
        {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally
        {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (final IOException e)
                {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    synchronized private static String readFromStream(InputStream inputStream) throws IOException
    {
        StringBuilder output = new StringBuilder();
        if (inputStream != null)
        {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null)
            {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * writes the network response to JSON files (for debugging purposes only)
     *
     * @param context - current App Context
     * @param jsonStr - network response from server
     */
    synchronized private static void writeJsonToFile(Context context, String jsonStr)
    {
        Date date = new Date();
        Locale locale = new Locale("en_US");
        SimpleDateFormat year = new SimpleDateFormat("yyyy", locale);
        SimpleDateFormat month = new SimpleDateFormat("mm", locale);
        SimpleDateFormat day = new SimpleDateFormat("dd", locale);
        SimpleDateFormat hour = new SimpleDateFormat("hh", locale);
        SimpleDateFormat min = new SimpleDateFormat("mm", locale);
        SimpleDateFormat sec = new SimpleDateFormat("ss", locale);

        String dateStr = year.format(date) + "_" + month.format(date) + "_" + day.format(date) + " " + hour.format(date) + "-" + min.format(date) + "-" + sec.format(date);
        String fn = dateStr + ".txt";

        try
        {
            FileOutputStream out = context.openFileOutput(fn, Context.MODE_PRIVATE);
            out.write(jsonStr.getBytes());
            out.close();
        } catch (Exception e)
        {
            Log.e(TAG, "Fx parseMovieJSON(): file '" + fn + "' not found OR error writing to the file '" + fn + "'", e);
        }
    }

    synchronized private static ContentValues[] parseMovieJSON(String jsonStr, String syncType) throws JSONException
    {
        final String NA = "n.a.";
        final String PAGE = "page";
        final String ID = "id";  // corresponds to refID in the database!
        final String TITLE = "title";
        final String OVERVIEW = "overview";
        final String POSTER_PATH = "poster_path";
        final String BACKDROP_PATH = "backdrop_path";
        final String RELEASE_DATE = "release_date";
        final String VOTE_AVERAGE = "vote_average";
        final String VOTE_TOTAL = "vote_count";

        JSONObject jsonObj = new JSONObject(jsonStr);
        int currentPage = jsonObj.getInt(PAGE);
        JSONArray results = jsonObj.getJSONArray("results");

        // cache the result
        int length = results.length();

        // create an array of contentValues
        ContentValues[] contentValuesArr = new ContentValues[length];

        ContentValues contentValues;
        JSONObject json;
        for (int i = 0; i < length; i++)
        {
            json = results.getJSONObject(i);

            // shouldn't happen
            if (json == null)
            {
                continue;
            }

            contentValues = new ContentValues();
            contentValues.put(Movies.COLUMN_REFID, json.optLong(ID, -1));
            contentValues.put(Movies.COLUMN_TITLE, json.optString(TITLE, NA));
            contentValues.put(Movies.COLUMN_OVERVIEW, json.optString(OVERVIEW, ""));
            contentValues.put(Movies.COLUMN_POSTER_PATH, json.optString(POSTER_PATH, ""));
            contentValues.put(Movies.COLUMN_BACKDROP_PATH, json.optString(BACKDROP_PATH, ""));
            contentValues.put(Movies.COLUMN_VOTE_AVERAGE, json.optDouble(VOTE_AVERAGE));
            contentValues.put(Movies.COLUMN_VOTE_COUNT, json.optInt(VOTE_TOTAL));
            contentValues.put(Movies.COLUMN_RELEASE_DATE, json.optString(RELEASE_DATE));
            contentValues.put(Movies.COLUMN_PAGE, currentPage);
            contentValues.put(Movies.COLUMN_SYNC_TYPE, syncType);

            // TODO: handle pagination here!
            // get the total_results && total_pages
            // need this to handle pagination
            // data.totalPages = json.optInt("total_pages",1);
            // data.totalResults = json.optInt("total_results",20);

            contentValuesArr[i] = contentValues;
        }

        return contentValuesArr;
    }

    synchronized private static List<Review> parseReviewJSON(String jsonStr) throws JSONException
    {
        final String ID = "id";  // corresponds to refID in the database!
        final String AUTHOR = "author";
        final String CONTENT = "content";
        final String URL = "url";
        final String NA = "n.a.";

        JSONObject jsonObj = new JSONObject(jsonStr);
        JSONArray results = jsonObj.getJSONArray("results");

        int length = results.length();

        List<Review> reviews = new ArrayList<>();
        Review review;
        JSONObject json;
        for (int i = 0; i < length; i++)
        {
            json = results.getJSONObject(i);

            if (json == null)
            {
                continue;
            }

            review = new Review();
            review.setId(json.optString(ID));
            review.setAuthor(json.optString(AUTHOR));
            review.setContent(json.optString(CONTENT));
            review.Url(json.optString(URL));

            reviews.add(review);
        }

        return reviews;
    }

    synchronized private static List<Trailer> parseTrailerJSON(String jsonStr) throws JSONException
    {
        final String ID = "id";
        final String KEY = "key";
        final String NAME = "name";
        final String SITE = "site";
        final String NA = "n.a.";
        final String TYPE = "type";

        final String TRAILER = "Trailer";

        JSONObject jsonObj = new JSONObject(jsonStr);
        JSONArray results = jsonObj.getJSONArray("results");

        int length = results.length();

        List<Trailer> trailers = new ArrayList<>();
        Trailer trailer;
        JSONObject json;
        for (int i = 0; i < length; i++)
        {
            json = results.getJSONObject(i);

            if (json == null)
                continue;

            if (json.optString(TYPE,TRAILER).compareToIgnoreCase(TRAILER) != 0)
                continue;

            trailer = new Trailer();
            trailer.setId(json.optString(ID));
            trailer.setKey(json.optString(KEY));
            trailer.setName(json.optString(NAME));
            trailer.setSite(json.optString(SITE));

            trailers.add(trailer);
        }

        return trailers;
    }
}
