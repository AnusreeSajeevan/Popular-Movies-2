package com.example.anu.popularmovies_1.utils;

import com.example.anu.popularmovies_1.BuildConfig;

/**
 * Created by Design on 08-12-2017.
 *
 */

public class MovieDBUtils {

    //api key to access TMDB apis
    public static final String API_KEY = BuildConfig.API_KEY;

    //base url to access movies api
    public static final String BASE_URL = "http://api.themoviedb.org/3/";

    public static final String KEY_MOVIE = "movie";
    public static final String PARAM_VALUE_TOP_RATED = "top_rated";
    public static final String PARAM_VALUE_POPULAR = "popular";

    //api token parameter key
    public static final String PARAM_API_KEY = "api_key";

    public static final String URL_POSTER_PATH = "http://image.tmdb.org/t/p/w185/";
    public static final String URL_BACKDROP_PATH = "http://image.tmdb.org/t/p/w780/";

    public static final String TRAILER_BASE_YOUTUBE_PATH = "https://www.youtube.com/watch?v=";

    //paths to fetch trainer thumbnail & it's image
    public static final String TRAILER_THUMBNAIL_IMAGE_PATH = "http://img.youtube.com/vi/";
    public static final String TRAILER_THUMBNAIL_IMAGE_0= "/0.jpg";
    public static final String TRAILER_THUMBNAIL_IMAGE_1= "/1.jpg";
    public static final String TRAILER_THUMBNAIL_IMAGE_2= "/2.jpg";
    public static final String TRAILER_THUMBNAIL_IMAGE_3= "/3.jpg";


}
