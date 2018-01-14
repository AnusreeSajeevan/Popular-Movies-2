package com.example.anu.popularmovies_1.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReviewResponse {

    private double id;
    private int pages;

    @SerializedName("results")
    private List<Review> reviewList;

    public List<Review> getReviewList() {
        return reviewList;
    }
}
