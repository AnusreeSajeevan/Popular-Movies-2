package com.example.anu.popularmovies_1.adapter;

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.anu.popularmovies_1.R;
import com.example.anu.popularmovies_1.model.Review;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Design on 09-01-2018.
 */

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

        Layout l = holder.txtReview.getLayout();
        if (l != null) {
            int lines = l.getLineCount();
            if (lines > 0)
                if (l.getEllipsisCount(lines - 1) > 0) {
                holder.btnViewMore.setVisibility(View.VISIBLE);
                }
                else
                    holder.btnViewMore.setVisibility(View.GONE);
        }
        holder.txtReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Layout l = holder.txtReview.getLayout();
                if (l != null) {
                    int lines = l.getLineCount();
                    if (lines > 0)
                        if (l.getEllipsisCount(lines - 1) > 0) {
                            TransitionManager.beginDelayedTransition(holder.layoutMain);
                            holder.txtReview.setMaxLines(100);
                        } else {
                            TransitionManager.beginDelayedTransition(holder.layoutMain);
                            holder.txtReview.setMaxLines(3);
                        }
                }
            }
        });
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
