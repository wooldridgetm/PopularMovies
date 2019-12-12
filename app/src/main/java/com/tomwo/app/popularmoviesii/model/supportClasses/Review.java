package com.tomwo.app.popularmoviesii.model.supportClasses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wooldridgetm on 6/16/17.
 */
public class Review implements Parcelable
{
    private String mId;
    private String mAuthor;
    private String mContent;
    private String mUrl;

    public Review()
    {
        // default Constructor
        this.mId = "n.a.";
        this.mAuthor = this.mContent = this.mUrl = "";
    }

    public Review(String id)
    {
        this.mId = id;
        this.mAuthor = this.mContent = this.mUrl = "";
    }

    public Review(String author, String content)
    {
        this.mAuthor = author;
        this.mContent = content;
    }

    /**
     * @param id
     * @param author
     * @param content
     * @param url
     */
    public Review(String id, String author, String content, String url)
    {
        this.mId = id;
        this.mAuthor = author;
        this.mContent = content;
        this.mUrl = url;
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
     * Fx getter (mAuthor)
     */
    public String getAuthor()
    {
        return mAuthor;
    }

    public void setAuthor(String mAuthor)
    {
        this.mAuthor = mAuthor;
    }

    /**
     * Fx getter (mContent)
     */
    public String getContent()
    {
        return mContent;
    }

    public void setContent(String mContent)
    {
        this.mContent = mContent;
    }

    /**
     * Fx getter (Url)
     */
    public String getUrl()
    {
        return this.mUrl;
    }

    public void Url(String url)
    {
        this.mUrl = url;
    }


    // Parcelable management
    private Review(Parcel in)
    {
        this.mId = in.readString();
        this.mAuthor = in.readString();
        this.mContent = in.readString();
        this.mUrl = in.readString();
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.mId);
        dest.writeString(this.mAuthor);
        dest.writeString(this.mContent);
        dest.writeString(this.mUrl);
    }

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>()
    {
        public Review createFromParcel(Parcel in)
        {
            return new Review(in);
        }

        public Review[] newArray(int size)
        {
            return new Review[size];
        }
    };


}
