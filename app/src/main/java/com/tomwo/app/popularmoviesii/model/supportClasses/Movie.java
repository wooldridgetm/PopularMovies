package com.tomwo.app.popularmoviesii.model.supportClasses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wooldridgetm on 5/23/17.
 */
public class Movie implements Parcelable
{
    public static final String KEY_MOVIE = "movieClassSimpleName";

    private long id;
    private long mRefID;

    private String mTitle;
    private String mOverview;
    private String mPosterPath;   // poster_path
    private String mBackdropPath; // backdrop_path
    private String mReleaseDate;  // release date

    private boolean mFavorite;  // is the movie their favorite??

    private long voteCount;     // vote_count
    private double voteAverage;  // vote_average

    public Movie()
    {
        // default constructor
        this.mTitle = this.mOverview = "";
        this.id = -1;
        this.mRefID = -1;
        this.mPosterPath = "";
        this.mBackdropPath = "";
        this.mFavorite = false;
    }

    public Movie(String title)
    {
        this.mTitle = title;
    }

    /**
     * Fx getter (Id)
     */
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    /**
     * Fx getter (mRefID)
     */
    public long getRefID()
    {
        return this.mRefID;
    }

    public void setRefID(long mRefID)
    {
        this.mRefID = mRefID;
    }

    /**
     * Fx getter (title)
     */
    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle(String mTitle)
    {
        this.mTitle = mTitle;
    }

    /**
     * Fx getter (mOverview)
     */
    public String getOverview()
    {
        return mOverview;
    }

    public void setOverview(String mOverview)
    {
        this.mOverview = mOverview;
    }

    /**
     * Fx getter (poster_path)
     */
    public String getPosterPath()
    {
        return mPosterPath;
    }

    public void setPosterPath(String mPosterPath)
    {
        this.mPosterPath = mPosterPath;
    }

    /**
     * Fx getter (mReleaseDate)
     */
    public String getReleaseDate()
    {
        return mReleaseDate;
    }

    public void setReleaseDate(String mReleaseDate)
    {
        this.mReleaseDate = mReleaseDate;
    }

    /**
     * Fx getter (mBackdropPath)
     */
    public String getBackdropPath()
    {
        return mBackdropPath;
    }

    public void setBackdropPath(String mBackdropPath)
    {
        this.mBackdropPath = mBackdropPath;
    }

    /**
     * Fx getter (mFavorite)
     */
    public boolean isFavorite()
    {
        return mFavorite;
    }

    /**
     * Fx setFavorite (setter)
     */
    public void setFavorite(boolean mFavorite)
    {
        this.mFavorite = mFavorite;
    }

    /**
     * vote_average
     */
    public void setVoteAverage(double value)
    {
        this.voteAverage = value;
    }

    public double getVoteAverage()
    {
        return this.voteAverage;
    }

    /**
     * vote_count
     */
    public void setVoteCount(long total)
    {
        this.voteCount = total;
    }


    // Parcelable management
    // Parcelable management
    private Movie(Parcel in)
    {
        id = in.readLong();
        mRefID = in.readLong();
        mTitle = in.readString();
        mOverview = in.readString();
        mPosterPath = in.readString();
        mBackdropPath = in.readString();
        mReleaseDate = in.readString();

        mFavorite = in.readInt() == 1;

        voteCount = in.readLong();
        voteAverage = in.readDouble();
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags)
    {

        dest.writeLong(id);
        dest.writeLong(mRefID);
        dest.writeString(mTitle);
        dest.writeString(mOverview);
        dest.writeString(mPosterPath);
        dest.writeString(mBackdropPath);
        dest.writeString(mReleaseDate);

        dest.writeInt(mFavorite ? 1 : 0);

        dest.writeLong(voteCount);
        dest.writeDouble(voteAverage);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>()
    {
        public Movie createFromParcel(Parcel in)
        {
            return new Movie(in);
        }

        public Movie[] newArray(int size)
        {
            return new Movie[size];
        }
    };

}
