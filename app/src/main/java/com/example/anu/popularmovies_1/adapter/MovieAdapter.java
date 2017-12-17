package com.example.anu.popularmovies_1.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.anu.popularmovies_1.R;
import com.example.anu.popularmovies_1.data.MovieContract;
import com.example.anu.popularmovies_1.data.MovieDbHelper;
import com.example.anu.popularmovies_1.model.Movie;
import com.example.anu.popularmovies_1.utils.MovieDBUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Design on 27-11-2017.
 *
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieHolder> {

    private List<Movie> movieList;
    private Context context;
    private OnClickHandleListener clickHandleListener;
    private static final String TAG = MovieAdapter.class.getSimpleName();
    private static int lastAnimatedPosition = -1;
    private MovieDbHelper movieDbHelper;

    public MovieAdapter(Context context, List<Movie> movieList, OnClickHandleListener clickHandleListener) {
        this.context = context;
        this.movieList = movieList;
        this.clickHandleListener = clickHandleListener;
        movieDbHelper = new MovieDbHelper(context);
    }

    public interface OnClickHandleListener {
        void onThumbnailClick(int pos);
    }

    @Override
    public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_movie_item, parent, false);
        return new MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(final MovieHolder holder, final int position) {
        final Movie movie = movieList.get(position);
        holder.txtMovieName.setText(movie.getTitle());
        holder.txtRating.setText(String.valueOf(movie.getVoteAverage()));
        Log.d(TAG, "poster path : "+MovieDBUtils.URL_POSTER_PATH + movie.getPosterPath());
        Picasso.with(context)
                .load(movie.getPosterPath()).fit()
                .placeholder(R.drawable.ic_place_holder)
                .error(R.drawable.ic_place_holder)
                .into(holder.imgThumbnail);

        holder.imgThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickHandleListener.onThumbnailClick(position);
            }
        });
        
        setEnterAnimation(holder.cardView, position);

        /**
         * if database does not already contains a row for the particular movie, cursor count will be 0
         * insert new row for the movie if cursor count is 0
         */
        Cursor cursor = movieDbHelper.getMovieById(movie.getId());
        int count = cursor.getCount();  //return the cursor count
        int favorite;
        if (count == 0){
            favorite = 0;   //indicating movie is not favorite(initially)
            setFavorite(holder, favorite);
        }
        else {
            //get saved favorite value from cursor
            cursor.moveToFirst();
            favorite = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_FAVORITE));
            setFavorite(holder, favorite);
        }

        holder.btnFavorite.setEnabled(false);
    }

    /**
     * method to set favorite based either on the value retrieved rom local db
     * or when user click on the favorite
     * @param holder
     * @param isFavorite favorite or not
     */
    private void setFavorite(MovieHolder holder, int isFavorite) {
        if (isFavorite == 1) {
            holder.btnFavorite.setVisibility(View.VISIBLE);
            holder.btnFavorite.setBackgroundResource(R.drawable.ic_favorite);
        }
        else {
            holder.btnFavorite.setVisibility(View.GONE);
            holder.btnFavorite.setBackgroundResource(R.drawable.ic_not_favorite);
        }
    }

    /**
     * method to set enter animation to each recyclerview item
     * if that item is not already added
     * @param cardView item on which animation is to be set
     * @param position item position
     */
    private void setEnterAnimation(CardView cardView, int position) {

        if(position > lastAnimatedPosition){
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.enter_anim);
            cardView.setAnimation(animation);
            lastAnimatedPosition = position;
        }

    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }
}
