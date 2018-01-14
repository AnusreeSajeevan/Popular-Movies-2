package com.example.anu.popularmovies_1.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.anu.popularmovies_1.R;
import com.example.anu.popularmovies_1.model.Trailer;
import com.example.anu.popularmovies_1.utils.MovieDBUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerHolder> {

    private Context context;
    private List<Trailer> trailerList;
    private TrailerClickHandlerListener trailerClickHandlerListener;


    public interface TrailerClickHandlerListener{
        void onTrailerClick(String key);
    }
    public TrailerAdapter(Context context, TrailerClickHandlerListener listener) {
        this.context = context;
        this.trailerClickHandlerListener = listener;
    }

    @Override
    public TrailerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_trailer_item, parent, false);
        return new TrailerHolder(view);
    }

    @Override
    public void onBindViewHolder(final TrailerHolder holder, int position) {
        final Trailer trailer = trailerList.get(position);

        Picasso.with(context)
                .load(MovieDBUtils.TRAILER_THUMBNAIL_IMAGE_PATH + trailer.getKey() + MovieDBUtils.TRAILER_THUMBNAIL_IMAGE_0)
                .fit()
                .placeholder(R.drawable.ic_place_holder)
                .error(R.drawable.ic_place_holder)
                .into(holder.imgThumbnail);

        holder.layoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trailerClickHandlerListener.onTrailerClick(trailer.getKey());
            }
        });

    }

    @Override
    public int getItemCount() {
        if (null == trailerList)
            return 0;
        return trailerList.size();
    }

    /**
     * method to set trailer list
     *
     * @param trailerList
     */
    public void setTrailerList(List<Trailer> trailerList) {
        this.trailerList = trailerList;
        notifyDataSetChanged();
    }
}
