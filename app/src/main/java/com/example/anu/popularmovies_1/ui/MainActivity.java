package com.example.anu.popularmovies_1.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.anu.popularmovies_1.MoviesPreferences;
import com.example.anu.popularmovies_1.R;
import com.example.anu.popularmovies_1.adapter.MovieAdapter;
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

    private MovieAdapter movieAdapter;
    private static List<Movie> movieList = new ArrayList<>();
    private String sortBy;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MOVIES_LOADER_ID = 0;
    private static final Bundle bundle = null;
    static String KEY_MOVIE_RESPONSE = "movie_response";
    LoaderManager.LoaderCallbacks callBacks = MainActivity.this;

    //flag to indicate if preference value has been updated or not
    private static boolean PREFERENCE_UPDATED = false;

    private MovieDbHelper movieDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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
                if (NetworkUtils.isNetworkAvailable(MainActivity.this)){
                    getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, callBacks);
                }
                else {
                    showError(getResources().getString(R.string.no_connectivity));
                }
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
        getSortOrderAndSetup();
        if (NetworkUtils.isNetworkAvailable(MainActivity.this)){
            getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, bundle, callBacks);
        }
        else {
            showError(getResources().getString(R.string.no_connectivity));
        }
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
        if (null != getSupportActionBar()){
            if (sortBy.equalsIgnoreCase(getResources().getString(R.string.pref_sortby_popular_value)))
                getSupportActionBar().setTitle(getResources().getString(R.string.pref_sortby_popular_label));
            else if (sortBy.equalsIgnoreCase(getResources().getString(R.string.pref_sortby_top_rated_value)))
                getSupportActionBar().setTitle(getResources().getString(R.string.pref_sortby_top_rated_label));
        }
    }

    /**
     * method over riden to redirect to {@link MovieDetailsActivity}
     * on clicking movie thumbnail
     * @param pos clicked thumbnail position
     */
    @Override
    public void onThumbnailClick(int pos) {
        Intent iDetail = new Intent(MainActivity.this, MovieDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_MOVIE_RESPONSE, movieList.get(pos));
        iDetail.putExtras(bundle);
        startActivity(iDetail);
    }

    @Override
    public Loader<MovieResponse> onCreateLoader(int id, Bundle args) {
        return new android.support.v4.content.AsyncTaskLoader<MovieResponse>(this) {

            MovieResponse movieResponse = null;

            @Override
            protected void onStartLoading() {
                if (movieResponse!=null){
                    deliverResult(movieResponse);
                }
                else
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

                        //parse and save movie id and favories to local database
                        if (null != movieResponse){
                            List<Movie> movieList = movieResponse.getResults();
                            for (int i=0;i<movieList.size();i++){
                                Movie movie = movieList.get(i);

                                /**
                                 * if database does not already contains a row for the particular movie, cursor count will be 0
                                 * insert new row for the movie if cursor count is 0
                                 */
                                Cursor cursor = movieDbHelper.getMovieById(movie.getId());

                                int count = cursor.getCount();  //return the cursor count
                                if (count == 0){
                                    int favorite = 0;
                                    movieDbHelper.addNewMovie(movie.getId(), favorite);
                                }
                            }
                        }

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
    }

    @Override
    public void onLoadFinished(Loader<MovieResponse> loader, MovieResponse data) {
        movieList.clear();

        recyclerviewMovies.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);

        if (null!=data){
            List<Movie> movies = data.getResults();
            if (movies.size()!=0){
                movieList.addAll(movies);
                movieAdapter.notifyDataSetChanged();
            }
            else {
                showError(getResources().getString(R.string.err_msg));
            }
        }else {
            showError(getResources().getString(R.string.err_msg));
        }
    }

    /**
     * method to show appropriate error message
     * @param error the error message to be shown
     */
    private void showError(String error) {
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        recyclerviewMovies.setVisibility(View.GONE);
        tvError.setText(error);
        tvError.setVisibility(View.VISIBLE);
    }

    /**
     * method to get determine the column count in movies list
     * based on screen orientation
     * column count = 2, for portrait, 3 for landscape
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
            if (NetworkUtils.isNetworkAvailable(MainActivity.this)){
                getSortOrderAndSetup();
                getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, callBacks);
                PREFERENCE_UPDATED = false;
            }
            else
            {
                showError(getResources().getString(R.string.no_connectivity));
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();
    }
}
