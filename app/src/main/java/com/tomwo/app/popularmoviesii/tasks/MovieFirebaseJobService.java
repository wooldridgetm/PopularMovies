package com.tomwo.app.popularmoviesii.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;

/**
 * Created by wooldridgetm on 7/6/17.
 */

public class MovieFirebaseJobService extends JobService
{
    private AsyncTask<Void, Void, Void> mFetchMovieTask;

    @Override
    public boolean onStartJob(final JobParameters job)
    {
        this.mFetchMovieTask = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                Context context = getApplicationContext();
                BackgroundTasks.execute(context, BackgroundTasks.ACTION_REFRESH_DB);
                jobFinished(job, false);
                return null;
            }
        };

        this.mFetchMovieTask.execute();
        return true;
    }

    /**
     * Invoked when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely b/c the runtime constraints associated with the hob are no longer satisfied...
     *
     * @return whether there is more work remaining
     *
     * @see Job.Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
    @Override
    public boolean onStopJob(JobParameters job)
    {
        if (this.mFetchMovieTask != null)
        {
            this.mFetchMovieTask.cancel(true);
        }
        return true;
    }
}
