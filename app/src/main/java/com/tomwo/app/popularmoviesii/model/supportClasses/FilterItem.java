package com.tomwo.app.popularmoviesii.model.supportClasses;

/**
 * Created by wooldridgetm on 7/2/17.
 */
public class FilterItem
{
    private String mTitle;
    private boolean mSelected;
    private String mKey;

    public FilterItem()
    {
        // default constructor
    }

    public FilterItem(String title, boolean selected)
    {
        this.mTitle = title;
        this.mSelected = selected;
    }

    public FilterItem(String title, String key, boolean selected)
    {
        this.mTitle = title;
        this.mKey = key;
        this.mSelected = selected;
    }

    /**
     * Fx getter (mTitle)
     */
    public String getTitle()
    {
        return this.mTitle;
    }

    public void setTitle(String value)
    {
        this.mTitle = value;
    }

    /**
     * Fx getter (mSelected)
     */
    public boolean isSelected()
    {
        return this.mSelected;
    }

    public void setSelected(boolean value)
    {
        this.mSelected = value;
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
}
