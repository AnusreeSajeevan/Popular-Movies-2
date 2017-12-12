package com.example.anu.popularmovies_1.data;

/**
 * Created by Design on 11-12-2017.
 */

import android.provider.BaseColumns;

/**
 * class which contains the contract for the tables and it's columns
 */
public class MovieContract {

    /**
     * to prevent someone accidentaly create an instance of the class
     * we will make the constructor private
     */
    private MovieContract(){}

    /**
     * should create inner class for each of the table in the database
     * each should implement {@link BaseColumns} interface
     * this class should define the table name and all it's columns as static final string fields
     */
    public static final class MovieEntry implements BaseColumns{

        /**
         * name of the table which will store the movie id and favorite status,
         * indicating if it is favirote or not
         */
        public static final String TABLE_NAME = "movie";

        /**
         * a coulumn named _ID will be created automatically whenever a new row is being inserted into the table
         */

        /**
         * name of the column to store movie id
         */
        public static final String KEY_COLUMN_MOVIE_ID = "movie_id";

        /**
         * column to indicate the movie favorite status
         * will store 1 if movie is favorite, 0 otherwise
         */
        public static final String KEY_COLUMN_FAVORITE = "favorite";

        /**
         * name of the column to store movie title
         */
        public static final String KEY_COLUMN_TITLE = "title";

        /**
         * name of the column to store movie original title
         */
        public static final String KEY_COLUMN_ORIGINAL_TITLE = "original_title";


        /**
         * name of the column to store vote average
         */
        public static final String KEY_COLUMN_VOTE_AVERAGE = "vote_average";


        /**
         * name of the column to store poster path
         */
        public static final String KEY_COLUMN_POSTER_PATH = "poster_path";

         /**
         * name of the column to store backdrop path
         */
        public static final String KEY_COLUMN_BACKDROP_PATH = "backdrop_path";



         /**
         * name of the column to store release date
         */
        public static final String KEY_COLUMN_RELEASE_DATE = "release_date";



         /**
         * name of the column to store overview
         */
        public static final String KEY_COLUMN_OVERVIEW = "overview";


    }
}
