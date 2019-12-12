package com.tomwo.app.popularmoviesii.ui.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomwo.app.popularmoviesii.R;
import com.tomwo.app.popularmoviesii.model.supportClasses.FilterItem;

import java.util.List;

/**
 * Created by wooldridgetm on 7/2/17.
 */
public class CustomSpinnerAdapter extends ArrayAdapter<FilterItem>
{
    private static final String TAG = "CustomSpinnerAdapter";

    private LayoutInflater layoutInflater;
    private int mSelectedIndex;

    public CustomSpinnerAdapter(@NonNull Context context, @LayoutRes int resource, int textViewId, List<FilterItem> data)
    {
        super(context, resource, textViewId, data);
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // default this to 0 b/c that's what the Spinner does when adding the dataProvider
        this.mSelectedIndex = 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View view = super.getView(position, convertView, parent);
        view.setVisibility(View.GONE);

        return view;
    }


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = this.layoutInflater.inflate(R.layout.item_spinner_list, parent, false);
        }

        FilterItem item = getItem(position);

        TextView textView = (TextView) convertView.findViewById(R.id.textViewSpinnerItem);
        textView.setText(item.getTitle());

        ImageView check = (ImageView) convertView.findViewById(R.id.imageViewSpinnerItem);

        if (item.isSelected())
        {
            this.mSelectedIndex = position;

            if (check.getVisibility() != View.VISIBLE)
            {
                check.setVisibility(View.VISIBLE);
            }
        } else if (check.getVisibility() != View.INVISIBLE)
        {
            check.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public int getSelectedIndex()
    {
        return this.mSelectedIndex;
    }


}
