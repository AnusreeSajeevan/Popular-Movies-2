package com.example.anu.popularmovies_1.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.anu.popularmovies_1.MoviesPreferences;
import com.example.anu.popularmovies_1.R;
import com.example.anu.popularmovies_1.adapter.MovieAdapter;
import com.example.anu.popularmovies_1.data.MovieContract;
import com.example.anu.popularmovies_1.data.MovieDbHelper;
import com.example.anu.popularmovies_1.model.Movie;
import com.example.anu.popularmovies_1.model.MovieResponse;
import com.example.anu.popularmovies_1.ui.settings.SettingsActivity;
import com.example.anu.popularmovies_1.utils.MoviesJsonUtils;
import com.example.anu.popularmovies_1.utils.NetworkUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnClickHandleListener,
        LoaderManager.LoaderCallbacks<MovieResponse>, SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.recyclerview_movies)
    RecyclerView recyclerviewMovies;
    @BindView(R.id.tv_error)
    TextView tvError;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tv_no_favorites)
    TextView tvNoFavorites;
    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;

    private MovieAdapter movieAdapter;
    private static List<Movie> movieList = new ArrayList<>();
    private String sortBy;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MOVIES_LOADER_ID = 10;
    private static final int FAVORITE_MOVIES_LOADER_ID = 20;
    private static final Bundle bundle = null;

    static String KEY_MOVIE_RESPONSE = "movie_response";
    static String KEY_CLICKED_POSITION = "clicked_position";
    static String KEY_SORT_BY = "sort_by";

    LoaderManager.LoaderCallbacks callBacks = MainActivity.this;

    LoaderManager.LoaderCallbacks callBacksFavorites = MainActivity.this;

    //flag to indicate if preference value has been updated or not
    private static boolean PREFERENCE_UPDATED = false;

    private MovieDbHelper movieDbHelper;

    private static final int REQUEST_CODE_DETAILS = 10;

    private static final int mPosition = RecyclerView.NO_POSITION;

    /**
     * Loader callbacks to load user's favorite movies from local database
     */
    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
            switch (loaderId) {
                case FAVORITE_MOVIES_LOADER_ID:
                    return new CursorLoader(MainActivity.this, MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            movieList.clear();
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);

            if (null != data) {
                data.moveToFirst();
                for (int i = 0; i < data.getCount(); i++) {
                    movieList.add(new Movie(data.getInt(data.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID)),
                            data.getDouble(data.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_VOTE_AVERAGE)),
                            data.getString(data.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_TITLE)),
                            data.getString(data.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_POSTER_PATH)),
                            data.getInt(data.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_FAVORITE)),
                            data.getString(data.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_ORIGINAL_TITLE)),
                            data.getString(data.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_BACKDROP_PATH)),
                            data.getString(data.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_OVERVIEW)),
                            data.getString(data.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_RELEASE_DATE))));
                    data.moveToNext();
                }
                movieAdapter.notifyDataSetChanged();
                if (data.getCount() != 0) {
                    tvNoFavorites.setVisibility(View.GONE);
                    tvError.setVisibility(View.GONE);
                } else {
                    showNoFavorites();
                }
            } else {
                showNoFavorites();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    /**
     * method to show appropriate message when there is no favorite movies
     */
    private void showNoFavorites() {
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);

        movieList.clear();
        movieAdapter.notifyDataSetChanged();
        tvError.setVisibility(View.GONE);
        tvNoFavorites.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        /*
         restore the saved data
         */
        if (null != savedInstanceState)
            sortBy = savedInstanceState.getString(KEY_SORT_BY);

        movieDbHelper = new MovieDbHelper(this);

        /*
          register MainActivity as a OnPreferenceChangedListener in onCreate
          inorder to receive callbacks when preference have been changed.
          We must unregister OnPreferenceChangedListener in onDestroy inorder to avoid any memory leaks
         */
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        /*
          set listener for {@link #swipeRefreshLayout},
          to fetch movies again on refresh
         */
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initializeOrRestartLoader();
            }
        });
        setupMoviesList();
    }

    /**
     * method to create {@link MovieAdapter} object
     * setup {@link #recyclerviewMovies} and set adapter
     * finally call {@link #onCreateLoader(int, Bundle)} )}
     */
    public void setupMoviesList() {
        movieAdapter = new MovieAdapter(MainActivity.this, movieList, this);
        int columnCount = setColumnCount();

        recyclerviewMovies.setLayoutManager(new GridLayoutManager(MainActivity.this, columnCount));
        recyclerviewMovies.setAdapter(movieAdapter);
        initializeOrRestartLoader();
    }

    /*
      get the user preferred sort order and set title
       */
    private void getSortOrderAndSetup() {
        showRefreshing();
        //get preference value
        sortBy = MoviesPreferences.getUserPreferredSortByValue(MainActivity.this);
        setTitle(sortBy);
    }


    /*
     *method to show refreshing
     */
    private void showRefreshing() {
        if (!swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(true);
    }


    /**
     * set action bar title to the sort order
     */
    private void setTitle(String sortBy) {
        if (null != getSupportActionBar()) {
            if (sortBy.equalsIgnoreCase(getResources().getString(R.string.pref_sortby_popular_value)))
                getSupportActionBar().setTitle(getResources().getString(R.string.pref_sortby_popular_label));
            else if (sortBy.equalsIgnoreCase(getResources().getString(R.string.pref_sortby_top_rated_value)))
                getSupportActionBar().setTitle(getResources().getString(R.string.pref_sortby_top_rated_label));
            else if (sortBy.equalsIgnoreCase(getResources().getString(R.string.pref_sortby_favorites_value)))
                getSupportActionBar().setTitle(getResources().getString(R.string.pref_sortby_favorites_label));
        }
    }

    /**
     * method overriden to redirect to {@link MovieDetailsActivity}
     * on clicking movie thumbnail
     *
     * @param pos clicked thumbnail position
     */
    @Override
    public void onThumbnailClick(int pos) {
        Intent iDetail = new Intent(MainActivity.this, MovieDetailsActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_MOVIE_RESPONSE, movieList.get(pos));
        bundle.putInt(KEY_CLICKED_POSITION, pos);

        iDetail.putExtras(bundle);
        startActivityForResult(iDetail, REQUEST_CODE_DETAILS);
    }

    @Override
    public Loader<MovieResponse> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MOVIES_LOADER_ID:
                return new AsyncTaskLoader<MovieResponse>(this) {

                    MovieResponse movieResponse = null;

                    @Override
                    protected void onStartLoading() {
                        if (movieResponse != null) {
                            deliverResult(movieResponse);
                        } else
                            forceLoad();
                    }

                    @Override
                    public void deliverResult(MovieResponse data) {
                        movieResponse = data;
                        super.deliverResult(data);
                    }

                    /**
                     * method to load and fetch movie
                     * @return fetched MovieResponse
                     */
                    @Override
                    public MovieResponse loadInBackground() {
                        try {

                            URL url = NetworkUtils.buildUrl(sortBy);
                            MovieResponse movieResponse;
                            try {
                                String response = NetworkUtils.getResponseFromHttpUrl(url);
                                JSONObject jsonObject = MoviesJsonUtils.getJSONObjectFromResponse(response);
                                movieResponse = new Gson().fromJson(jsonObject.toString(), MovieResponse.class);
                                return movieResponse;
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
            default:
                throw new RuntimeException("Loader not implemented");
        }
    }

    @Override
    public void onLoadFinished(Loader<MovieResponse> loader, MovieResponse data) {
        movieList.clear();

        recyclerviewMovies.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        tvNoFavorites.setVisibility(View.GONE);

        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);

        if (null != data) {
            List<Movie> movies = data.getResults();
            if (movies.size() != 0) {

                for (int i = 0; i < movies.size(); i++) {
                    Cursor cursor = movieDbHelper.getMovieById(movies.get(i).getId());
                    int favorite;
                    if (cursor.getCount() != 0) {
                        cursor.moveToFirst();
                        favorite = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_FAVORITE));
                    } else {
                        favorite = 0;
                    }
                    movies.get(i).setIsFavorite(favorite);
                }
                movieList.addAll(movies);
                movieAdapter.notifyDataSetChanged();
            } else {
                showError(getResources().getString(R.string.err_msg));
            }
        } else {
            showError(getResources().getString(R.string.err_msg));
        }
    }

    /**
     * method to show appropriate error message
     *
     * @param error the error message to be shown
     */
    private void showError(String error) {
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        recyclerviewMovies.setVisibility(View.GONE);
        tvError.setText(error);
        tvError.setVisibility(View.VISIBLE);
        tvNoFavorites.setVisibility(View.GONE);
    }

    /**
     * method to get determine the column count in movies list
     * based on screen orientation
     * column count = 2, for portrait, 3 for landscape
     *
     * @return grid column count depending on the orientation
     */
    private int setColumnCount() {
        int count;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            count = 2;
        } else {
            count = 3;
        }
        return count;
    }

    @Override
    public void onLoaderReset(Loader<MovieResponse> loader) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PREFERENCE_UPDATED = true;
    }

    /**
     * unregister MainActivity as OnPreferenceChangedListener to avoid any memory leakage
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * this method is called when user coming to the activity from another
     */
    @Override
    protected void onStart() {
        super.onStart();

        /*
          fetch movies again
          if the user preference for the sort order have been changed
          and set the flag false
         */
        if (PREFERENCE_UPDATED) {
            initializeOrRestartLoader();
            PREFERENCE_UPDATED = false;
        }
    }

    /**
     * method responsible for either initializing or restarting loaders based on user's sort preference
     */
    private void initializeOrRestartLoader() {
        getSortOrderAndSetup();
        if (sortBy.equalsIgnoreCase(getResources().getString(R.string.pref_sortby_favorites_value))) {
            initializeOrRestartFavoritesLoader();
        } else {
            getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER_ID);
            if (NetworkUtils.isNetworkAvailable(this)) {
                Loader loader = getSupportLoaderManager().getLoader(MOVIES_LOADER_ID);
                if (null == loader)
                    getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, callBacks);
                else
                    getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, callBacks);
            } else {
                initializeOrRestartFavoritesLoader();
                showSnackBar();
            }
        }
    }

    /**
     * method to either initialize or restart loader for favorite movies
     */
    private void initializeOrRestartFavoritesLoader() {
        getSupportLoaderManager().destroyLoader(MOVIES_LOADER_ID);
        Loader loaderFavorites = getSupportLoaderManager().getLoader(FAVORITE_MOVIES_LOADER_ID);
        if (null == loaderFavorites)
            getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER_ID, null, cursorLoaderCallbacks);
        else
            getSupportLoaderManager().restartLoader(FAVORITE_MOVIES_LOADER_ID, null, cursorLoaderCallbacks);

        // MoviesPreferences.setUserPreferredSortByValue(this, getResources().getString(R.string.pref_sortby_favorites_value));
    }

    /**
     * method to show snackbar while showing favorite movies when there is no internet connection
     */
    private void showSnackBar() {
        Snackbar.make(frameLayout, getResources().getString(R.string.snackbar_message), Snackbar.LENGTH_LONG).show();
        setTitle(getResources().getString(R.string.pref_sortby_favorites_value));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedId = item.getItemId();
        switch (selectedId) {
            case R.id.action_settings:
                Intent iSettings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(iSettings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * method to get favorite movies from local database
     */
    private void getFavorites() {
        Cursor cursorFavorites = movieDbHelper.getFavoriteMovies();
        movieList.clear();
        movieAdapter.notifyDataSetChanged();
        tvError.setVisibility(View.GONE);
        if (cursorFavorites.getCount() == 0) {
            tvNoFavorites.setVisibility(View.VISIBLE);
        } else {

            recyclerviewMovies.setVisibility(View.VISIBLE);
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);


            for (int i = 0; i < cursorFavorites.getCount(); i++) {

                if (i == 0)
                    cursorFavorites.moveToFirst();
                else
                    cursorFavorites.moveToNext();

                movieList.add(new Movie(
                        cursorFavorites.getInt(cursorFavorites.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID)),
                        cursorFavorites.getDouble(cursorFavorites.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_VOTE_AVERAGE)),
                        cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_TITLE)),
                        cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_POSTER_PATH)),
                        cursorFavorites.getInt(cursorFavorites.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_FAVORITE)),
                        cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_ORIGINAL_TITLE)),
                        cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_BACKDROP_PATH)),
                        cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_OVERVIEW)),
                        cursorFavorites.getString(cursorFavorites.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_RELEASE_DATE))
                ));
            }
            movieAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * method override to show the favorite status when returning to MainActivity from (@link {@link MovieDetailsActivity}
     * @param requestCode code associated with request
     * @param resultCode code associated with result
     * @param data data contained in the intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DETAILS) {
            if (resultCode == RESULT_OK) {
                String fav = getResources().getString(R.string.pref_sortby_favorites_value);
                if (sortBy.equalsIgnoreCase(fav)) {
                    getFavorites();
                } else {
                    movieAdapter.notifyDataSetChanged();

                    Bundle bundle = data.getExtras();
                    assert bundle != null;
                    Movie movie = bundle.getParcelable(MainActivity.KEY_MOVIE_RESPONSE);
                    int pos = bundle.getInt(KEY_CLICKED_POSITION, -1);
                    if (pos != -1) {
                        assert movie != null;
                        movieList.get(pos).setIsFavorite(movie.isFavorite());
                        movieAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    /**
     * save the value of {@literal loadFavorite} here,
     * so that we can retrieve it in {@literal onRestoreInstanceState} when configuration changes
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SORT_BY, sortBy);
    }

    /**
     * get the value of {@literal loadFavorite} we saved from {@literal onSaveInstanceState}
     * so that we can load favorites/all movies correctly even after configuration change
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (null != savedInstanceState)
            sortBy = savedInstanceState.getString(KEY_SORT_BY);
    }

}
