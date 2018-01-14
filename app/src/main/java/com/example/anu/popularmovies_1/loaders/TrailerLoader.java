package com.example.anu.popularmovies_1.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.anu.popularmovies_1.model.Trailer;
import com.example.anu.popularmovies_1.model.TrailerResponse;
import com.example.anu.popularmovies_1.retrofit.ApiClient;
import com.example.anu.popularmovies_1.retrofit.ApiInterface;
import com.example.anu.popularmovies_1.utils.MovieDBUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * loader class to load reviews
 */
public class TrailerLoader extends AsyncTaskLoader<List<Trailer>> {

    private int movieId;
    private List<Trailer> trailerList;

    public TrailerLoader(Context context, int movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        if (null == trailerList){
            forceLoad();
        }
    }

    @Override
    public List<Trailer> loadInBackground() {
        List list = new ArrayList();
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<TrailerResponse> call = apiInterface.getTrailers(movieId, MovieDBUtils.API_KEY);
        try {
            list = call.execute().body().getTrailerList();
        } catch (IOException e) {
            e.printStackTrace();
            return list;
        }
        return list;
    }

    @Override
    public void deliverResult(List<Trailer> data) {
        trailerList = data;
        super.deliverResult(data);
    }
}
