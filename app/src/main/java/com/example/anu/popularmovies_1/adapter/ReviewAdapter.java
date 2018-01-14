package com.example.anu.popularmovies_1.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.anu.popularmovies_1.R;
import com.example.anu.popularmovies_1.model.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewHolder> {

    private Context context;
    private List<Review> reviewList;

    public ReviewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_review, parent, false);
        return new ReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ReviewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.txtAuthor.setText(review.getAuthor());
        holder.txtReview.setText(review.getReview());
    }

    @Override
    public int getItemCount() {
        if (null == reviewList)
            return 0;
        return reviewList.size();
    }

    /**
     * method to set review list
     *
     * @param reviewList
     */
    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
        notifyDataSetChanged();
    }
}
