package com.example.anu.popularmovies_1.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.anu.popularmovies_1.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Design on 09-01-2018.
 */

public class TrailerHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.img_thumbnail)
    ImageView imgThumbnail;
    @BindView(R.id.txt_trailer_name)
    TextView txtTrailerName;
    @BindView(R.id.layout_main)
    CardView layoutMain;

    public TrailerHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
