package com.example.anu.popularmovies_1.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.anu.popularmovies_1.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Design on 09-01-2018.
 */

public class ReviewHolder extends RecyclerView.ViewHolder {


    @BindView(R.id.txt_author)
    TextView txtAuthor;
    @BindView(R.id.txt_review)
    TextView txtReview;
    @BindView(R.id.layout_main)
    LinearLayout layoutMain;
    @BindView(R.id.btn_view_more)
    Button btnViewMore;

    public ReviewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
