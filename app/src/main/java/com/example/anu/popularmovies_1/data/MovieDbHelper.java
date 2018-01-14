package com.example.anu.popularmovies_1.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * {@link SQLiteOpenHelper} is responsible for creating the database for the first time,
 * and to upgrade the databsae whenever the scheme changes
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
    private static final int DATABASE_VERSION = 2;

    private SQLiteDatabase sqLiteDatabase;
    private Context context;
    private static final String TAG = MovieDbHelper.class.getSimpleName();

    /**
     *
     * @param context called context
     */
    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * method responsible for creating the database for the first time
     * @param sqLiteDatabase database instance to be created
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /*
         * query to perform create movie table operation
         */
        String CREATE_MOVIE_TABLE_QUERY = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieContract.MovieEntry.KEY_COLUMN_FAVORITE + " INTEGER NOT NULL, " +
                MovieContract.MovieEntry.KEY_COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.KEY_COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.KEY_COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieContract.MovieEntry.KEY_COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.KEY_COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.KEY_COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.KEY_COLUMN_OVERVIEW + " TEXT NOT NULL" +
                "); ";
        sqLiteDatabase.execSQL(CREATE_MOVIE_TABLE_QUERY);
    }

    /**
     * method to upgrade the database and to make sure that the database schema is up to date
     * @param sqLiteDatabase instance of the database to be upgraded
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * method to get movie details by movie id
     * @param movieId id of the movie to retrieve the details
     * @return cursor containing row corresponding to the id
     */
    public Cursor getMovieById(long movieId){
        sqLiteDatabase = getReadableDatabase();
        String query = "SELECT * FROM " + MovieContract.MovieEntry.TABLE_NAME +
                " WHERE " + MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID + " = " + movieId;
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        return cursor;
    }

    /**
     * method to insert new movie into the table
     * @param movieId id of the movie to be inserted
     * @param favorite will be 0, for the first time, indicating movie is not favorite
     */
    public long addNewMovie(long movieId, int favorite, String title, String originalTitle, double voteAverage,
                            String posterPath, String backdropPath, String releaseDate, String overview){
        sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID, movieId);
        contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_FAVORITE, favorite);
        contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_TITLE, title);
        contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_ORIGINAL_TITLE, originalTitle);
        contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_VOTE_AVERAGE, voteAverage);
        contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_POSTER_PATH, posterPath);
        contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_BACKDROP_PATH, backdropPath);
        contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_RELEASE_DATE, releaseDate);
        contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_OVERVIEW, overview);
        return sqLiteDatabase.insert(MovieContract.MovieEntry.TABLE_NAME, null, contentValues);
    }

    /**
     * method to update favorite
     * @param movieId
     * @param isFavorite 0 for favorite, 1 for not favorite
     */
    public int updateFavorite(long movieId, int isFavorite){
        sqLiteDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.KEY_COLUMN_FAVORITE, isFavorite);

        return sqLiteDatabase.update(MovieContract.MovieEntry.TABLE_NAME, values,
                MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID + " = ?", new String[]{String.valueOf(movieId)});
    }

    /**
     * method to get all user's favorite movies
     * @return cursor containing all favorite movies
     */
    public Cursor getFavoriteMovies(){
        sqLiteDatabase = getReadableDatabase();
        String query = "SELECT * FROM " + MovieContract.MovieEntry.TABLE_NAME +
                " WHERE " + MovieContract.MovieEntry.KEY_COLUMN_FAVORITE+ " = " + 1;
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        return cursor;
    }
}
