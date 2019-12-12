package com.tomwo.app.popularmoviesii.ui.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tomwo.app.popularmoviesii.BR;
import com.tomwo.app.popularmoviesii.model.supportClasses.Review;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wooldridgetm on 6/16/17.
 */
public class BaseReviewAdapter extends RecyclerView.Adapter<BaseReviewAdapter.BaseViewHolder>
{
    public interface IClickListener
    {
        void listItem_triggeredHandler(int index);
    }

    private List<Review> items;
    private IClickListener mListener;

    public BaseReviewAdapter()
    {
        super();
        // default constructor
    }

    private int mLayoutId = 0;
    public int getLayoutIdForPosition(int position)
    {
        return this.mLayoutId;
    }

    public void setLayoutId(int layoutId)
    {
        this.mLayoutId = layoutId;
    }

    /**
     * used in DetailActivityFragment
     *
     * @param listener
     * @param layoutId
     */
    public BaseReviewAdapter(IClickListener listener, int layoutId)
    {
        this.mLayoutId = layoutId;
        this.mListener = listener;
        this.items = new ArrayList<>();
    }

    public BaseReviewAdapter(IClickListener listener, List<Review> items, int value)
    {
        this.mListener = listener;
        this.mLayoutId = value;
        this.items = (items == null) ? new ArrayList<Review>() : items;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_list, parent, false);
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, this.mLayoutId, parent, false);
        return new BaseViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(BaseViewHolder holder, int index)
    {
        Review review = this.items.get(index);
        holder.bind(review);

        holder.mIndex = index;
        holder.mClickListener = this.mListener;

        // holder.setReview(review);
        // holder.setIndex(index);
    }

    @Override
    public int getItemCount()
    {
        return this.items != null ? this.items.size() : 0;
    }

    // invoked from Fx onLoadFinished()
    public void addAll(List<Review> reviews)
    {
        this.items.addAll(reviews);
        this.notifyDataSetChanged();
    }

    // invoked from Fx onLoaderReset(Loader loader)
    public void clear()
    {
        this.items.clear();
        this.notifyDataSetChanged();
    }

    public List<Review> getArrayList()
    {
        return this.items;
    }



    static class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private int mIndex;
        private IClickListener mClickListener;

        private final ViewDataBinding mBinding;

        BaseViewHolder(ViewDataBinding binding)
        {
            super(binding.getRoot());

            this.mBinding = binding;
            this.mIndex = 0;

            this.itemView.setOnClickListener(this);
        }

        public void bind(Review review)
        {
            // this.mBinding.setVariable()
            this.mBinding.setVariable(BR.review, review);
            this.mBinding.executePendingBindings();
        }

        @Override
        public void onClick(View v)
        {
            if (this.mClickListener == null)
                return;

            this.mClickListener.listItem_triggeredHandler(this.mIndex);
        }
    }

    /*static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final ReviewListItemBinding mBinding;

        public IClickListener mClickListener;
        private Review mReview;
        private int mIndex;


        public ViewHolder(ReviewListItemBinding binding)
        {
            super(binding.getRoot());

            this.mBinding = binding;
            ConstraintLayout layout = this.mBinding.reviewListItemConstraintLayout;
            layout.setOnClickListener(this);
        }

        public void bind(Review review)
        {
            // this.mBinding.setVariable()
            this.mBinding.setReview(review);
            this.mBinding.executePendingBindings();
        }

        public void setReview(Review review)
        {
            this.mReview = review;
        }

        public void setIndex(int index)
        {
            this.mIndex = index;
        }

        @Override
        public void listItem_triggeredHandler(View v)
        {
            if (this.mClickListener != null)
            {
                this.mClickListener.listItem_triggeredHandler(this.mReview, this.mIndex);
            }
        }
    }*/




}
