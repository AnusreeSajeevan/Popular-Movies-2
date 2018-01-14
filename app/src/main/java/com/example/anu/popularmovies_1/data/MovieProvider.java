package com.example.anu.popularmovies_1.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MovieProvider extends ContentProvider {

    public static final int CODE_FAVORITES = 100;
    public static final int CODE_MOVIE_WITH_ID = 101;

    public MovieDbHelper movieDbHelper;

    public static UriMatcher sUriMatcher = buildUriMatcher();

    private static final String TAG = MovieProvider.class.getSimpleName();

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MovieContract.MovieEntry.CONTENT_AUTHORITY, MovieContract.MovieEntry.PATH_MOVIE, CODE_FAVORITES);
        uriMatcher.addURI(MovieContract.MovieEntry.CONTENT_AUTHORITY, MovieContract.MovieEntry.PATH_MOVIE + "/#", CODE_MOVIE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Cursor cursor;
        SQLiteDatabase sqLiteDatabase = movieDbHelper.getReadableDatabase();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case CODE_FAVORITES:
                cursor = sqLiteDatabase.query(MovieContract.MovieEntry.TABLE_NAME, projection, selection, selectionArgs, 
                        null, null, sortOrder);
                break;

            case CODE_MOVIE_WITH_ID:
                String[] movieID = new String[]{uri.getLastPathSegment()};
                cursor = sqLiteDatabase.query(MovieContract.MovieEntry.TABLE_NAME, projection, MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID
                 + " = ? ", movieID, null, null, sortOrder);
                break;

                default:
                    throw new  UnsupportedOperationException("Unknown uri " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase sqLiteDatabase = movieDbHelper.getWritableDatabase();
        Uri returnUri;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case CODE_FAVORITES:
                long id = sqLiteDatabase.insert(MovieContract.MovieEntry.TABLE_NAME, null, contentValues);
                if (id > 0){
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id);
                }
                else {
                    throw  new SQLException("Cannot insert movie");
                }
                break;
                default:
                    throw new UnsupportedOperationException("Unknown uri : " + uri);

        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        int deleted;
        SQLiteDatabase sqLiteDatabase = movieDbHelper.getWritableDatabase();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case CODE_MOVIE_WITH_ID:
                String selection = MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID + " = ? ";
                String[] selectionArgs = new String[]{uri.getLastPathSegment()};
                deleted = sqLiteDatabase.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
                default:
                    throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        int favoriteUpdated;
        SQLiteDatabase sqLiteDatabase = movieDbHelper.getWritableDatabase();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case CODE_MOVIE_WITH_ID:
                String selection = MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID + " = ? ";
                String[] selectionArgs = new String[]{uri.getLastPathSegment()};
                favoriteUpdated = sqLiteDatabase.update(MovieContract.MovieEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }

                if (favoriteUpdated != 0) {
                    //set notifications if a task was updated
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return favoriteUpdated;
    }
}
