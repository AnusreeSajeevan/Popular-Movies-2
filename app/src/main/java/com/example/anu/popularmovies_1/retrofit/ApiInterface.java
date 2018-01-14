package com.example.anu.popularmovies_1.retrofit;

import com.example.anu.popularmovies_1.model.ReviewResponse;
import com.example.anu.popularmovies_1.model.TrailerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Design on 09-01-2018.
 */

public interface ApiInterface {

    @GET("movie/{id}/reviews")
    Call<ReviewResponse> getReviews(@Path("id") int id, @Query("api_key") String api_key);

    @GET("movie/{id}/videos")
    Call<TrailerResponse> getTrailers(@Path("id") int id, @Query("api_key") String api_key);

}
