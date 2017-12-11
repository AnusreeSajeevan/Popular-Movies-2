package com.example.anu.popularmovies_1.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Design on 11-12-2017.
 */

/**
 * {@link SQLiteOpenHelper} is responsible for creating the database for the first time,
 * and to upgrade the databse whenever the scheme changes
 */
public class MovieDbHelper extends SQLiteOpenHelper{

    /**
     * name of the database
     * it is the name of the local file which will store all the data on the android device
     * it should end with .db extension
     */
    private static final String DATABASE_NAME = "movie.db";

    /**
     * will store current database version
     * initial value will be 1, and should upgrade it whenever we change database schema
     */
    private static final int DATABASE_VERSION = 1;

    /**
     *
     * @param context
     * @param name {@value DATABASE_NAME}
     * @param factory will be null
     * @param version {@value DATABASE_VERSION}
     */
    public MovieDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * method responsible for creating the database for the first time
     * @param sqLiteDatabase database instance to be created
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /**
         * query to perform create movie table operation
         */
        String CREATE_MOVIE_TABLE_QUERY = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieContract.MovieEntry.KEY_COLUMN_FAVORITE + " INTEGER NOT NULL, " +
                "); ";
        sqLiteDatabase.execSQL(CREATE_MOVIE_TABLE_QUERY);
    }

    /**
     * method to upgrade the databse and to make sure that the database schema is upto date
     * @param sqLiteDatabase instance of the database to be upgraded
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}
