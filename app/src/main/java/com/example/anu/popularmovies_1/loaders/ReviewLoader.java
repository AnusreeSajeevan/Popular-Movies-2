package com.example.anu.popularmovies_1.loaders;

/**
 * Created by Design on 09-01-2018.
 */


import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;

import com.example.anu.popularmovies_1.model.Review;
import com.example.anu.popularmovies_1.model.ReviewResponse;
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
public class ReviewLoader extends AsyncTaskLoader<List<Review>> {

    private int movieId;
    private List<Review> reviewList;

    public ReviewLoader(Context context, int movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        if (null == reviewList){
            forceLoad();
        }
    }

    @Override
    public List<Review> loadInBackground() {
        List list = new ArrayList();
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ReviewResponse> call = apiInterface.getReviews(movieId, MovieDBUtils.API_KEY);
        try {
            list = call.execute().body().getReviewList();
        } catch (IOException e) {
            e.printStackTrace();
            return list;
        }
        return list;
    }

    @Override
    public void deliverResult(List<Review> data) {
        reviewList = data;
        super.deliverResult(data);
    }
}
