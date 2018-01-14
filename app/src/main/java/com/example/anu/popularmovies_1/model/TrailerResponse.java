package com.example.anu.popularmovies_1.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Design on 09-01-2018.
 */

public class TrailerResponse {

    private double id;
    private int pages;

    @SerializedName("results")
    private List<Trailer> trailerList;

    public List<Trailer> getTrailerList() {
        return trailerList;
    }
}
