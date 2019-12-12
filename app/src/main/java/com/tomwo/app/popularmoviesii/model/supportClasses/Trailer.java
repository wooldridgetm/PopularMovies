package com.tomwo.app.popularmoviesii.model.supportClasses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wooldridgetm on 6/25/17.
 */

public class Trailer
{
    private String mId;
    private String mKey;
    private String mName;
    private String mSite;

    public Trailer()
    {
        // default constructor
    }


    /**
     * Fx getter (mId)
     */
    public String getId()
    {
        return mId;
    }

    public void setId(String mId)
    {
        this.mId = mId;
    }

    /**
     * Fx getter (mKey)
     */
    public String getKey()
    {
        return mKey;
    }

    public void setKey(String mKey)
    {
        this.mKey = mKey;
    }

    /**
     * Fx getter (mName)
     */
    public String getName()
    {
        return mName;
    }

    public void setName(String mName)
    {
        this.mName = mName;
    }

    /**
     * Fx getter (mSite)
     */
    public String getSite()
    {
        return mSite;
    }

    public void setSite(String mSite)
    {
        this.mSite = mSite;
    }

    // Parcelable management
    private Trailer(Parcel in)
    {

        mId = in.readString();

        mKey = in.readString();

        mName = in.readString();
        mSite = in.readString();
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags)
    {

        dest.writeString(mId);

        dest.writeString(mKey);

        dest.writeString(mName);
        dest.writeString(mSite);
    }

    public static final Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>()
    {
        public Trailer createFromParcel(Parcel in)
        {
            return new Trailer(in);
        }

        public Trailer[] newArray(int size)
        {
            return new Trailer[size];
        }
    };
}
