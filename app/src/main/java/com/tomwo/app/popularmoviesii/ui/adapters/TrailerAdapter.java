package com.tomwo.app.popularmoviesii.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tomwo.app.popularmoviesii.R;
import com.tomwo.app.popularmoviesii.model.supportClasses.Trailer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wooldridgetm on 7/21/17.
 */

public class TrailerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public interface ITrailerListener
    {
        void trailer_triggeredHandler(Trailer item);
    }

    private List<Trailer> mTrailers;

    public ITrailerListener mParent;

    public TrailerAdapter(Context context)
    {
        this.mTrailers = new ArrayList<>();

        if (context instanceof ITrailerListener)
        {
            this.mParent = (ITrailerListener) context;
            return;
        }

        throw new RuntimeException("Context '"+context.getClass().getSimpleName()+" doesn't implement ITrailerListener!!");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trailer_list_simple, parent, false);
        return new DefaultViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i)
    {
        Trailer trailer = this.mTrailers.get(i);

        DefaultViewHolder defaultHolder = (DefaultViewHolder) holder;

        defaultHolder.mTextView.setText(trailer.getName());
        defaultHolder.position = i;
    }

    @Override
    public int getItemCount()
    {
        return this.mTrailers == null ? 0 : this.mTrailers.size();
    }


    public void addAll(List<Trailer> trailers)
    {
        if (trailers == null)
            return;

        if (this.mTrailers != null)
        {
            this.mTrailers.clear();
        }

        this.mTrailers.addAll(trailers);
        this.notifyDataSetChanged();
    }

    private class DefaultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public TextView mTextView;
        public int position;

        DefaultViewHolder(View itemView)
        {
            super(itemView);

            this.mTextView = (TextView) itemView.findViewById(R.id.trailerNameTextView);
            this.itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if (TrailerAdapter.this.mParent != null)
            {
                TrailerAdapter.this.mParent.trailer_triggeredHandler(mTrailers.get(position));
            }
        }
    }
}
