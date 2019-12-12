package com.tomwo.app.popularmoviesii.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tomwo.app.popularmoviesii.R;
import com.tomwo.app.popularmoviesii.ui.adapters.BaseReviewAdapter;
import com.tomwo.app.popularmoviesii.ui.utils.ItemDivider;
import com.tomwo.app.popularmoviesii.databinding.FragmentDialogReviewListBinding;
import com.tomwo.app.popularmoviesii.model.supportClasses.Movie;
import com.tomwo.app.popularmoviesii.model.supportClasses.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewListDialogFragment extends DialogFragment implements BaseReviewAdapter.IClickListener
{
    private static final String ARG_MOVIE = "movieArg";
    private static final String ARG_ARRAY_LIST = "arrayListArg";
    private static final String ARG_SELECTED_INDEX = "selectedIndexArg";

    private List<Review> mReviews;
    private int mSelectedIndex = 0;
    private Movie mMovie;

    public ReviewListDialogFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (this.getArguments() != null)
        {
            this.mMovie = this.getArguments().getParcelable(ARG_MOVIE);
            this.mReviews = this.getArguments().getParcelableArrayList(ARG_ARRAY_LIST);
            this.mSelectedIndex = this.getArguments().getInt(ARG_SELECTED_INDEX);
        }

        // styling of DialogFragment
        int style = DialogFragment.STYLE_NORMAL;
        int theme = android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth;
        this.setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        FragmentDialogReviewListBinding mBinding = FragmentDialogReviewListBinding.inflate(inflater, container, false);

        this.getDialog().setTitle(this.mMovie.getTitle() + "'s Reviews");

        TextView textView = (TextView) this.getDialog().findViewById(android.R.id.title);
        if (textView != null)
        {
            textView.setGravity(Gravity.CENTER);
        }

        RecyclerView mRecyclerView = mBinding.reviewRecyclerViewLarge;

        BaseReviewAdapter mAdapter = new BaseReviewAdapter(this, this.mReviews, R.layout.item_review_list_large);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecyclerView.addItemDecoration(new ItemDivider(this.getActivity()));
        if (this.mSelectedIndex != 0)
        {
            mRecyclerView.scrollToPosition(this.mSelectedIndex);
        }

        return mBinding.getRoot();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    // when the user clicks on a list item…dismiss the FragmentDialog
    @Override
    public void listItem_triggeredHandler(int index)
    {
        this.dismiss();
    }

    /**
     *
     * ** Utility Methods **
     *
     *
     */


    public static ReviewListDialogFragment newInstance(Movie movie, ArrayList<Review> reviews, int selectedIndex)
    {
        ReviewListDialogFragment fragment = new ReviewListDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        args.putParcelableArrayList(ARG_ARRAY_LIST, reviews);
        args.putInt(ARG_SELECTED_INDEX, selectedIndex);
        fragment.setArguments(args);

        return fragment;
    }
}
