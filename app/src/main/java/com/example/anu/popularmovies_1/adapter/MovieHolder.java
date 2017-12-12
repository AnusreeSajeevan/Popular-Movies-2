package com.example.anu.popularmovies_1.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.anu.popularmovies_1.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Design on 27-11-2017.
 *
 */

public class MovieHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.img_thumbnail)
    ImageView imgThumbnail;
    @BindView(R.id.txt_movie_name)
    TextView txtMovieName;
    @BindView(R.id.txt_rating)
    TextView txtRating;
    @BindView(R.id.card_view)
    CardView cardView;
    @BindView(R.id.btn_favorites)
    ToggleButton btnFavorite;

    public MovieHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
