package com.example.anu.popularmovies_1.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.anu.popularmovies_1.R;
import com.example.anu.popularmovies_1.adapter.ReviewAdapter;
import com.example.anu.popularmovies_1.adapter.TrailerAdapter;
import com.example.anu.popularmovies_1.data.MovieContract;
import com.example.anu.popularmovies_1.data.MovieDbHelper;
import com.example.anu.popularmovies_1.loaders.ReviewLoader;
import com.example.anu.popularmovies_1.loaders.TrailerLoader;
import com.example.anu.popularmovies_1.model.Movie;
import com.example.anu.popularmovies_1.model.Review;
import com.example.anu.popularmovies_1.model.Trailer;
import com.example.anu.popularmovies_1.utils.CommonUtils;
import com.example.anu.popularmovies_1.utils.MovieDBUtils;
import com.example.anu.popularmovies_1.utils.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks, TrailerAdapter.TrailerClickHandlerListener {

    @BindView(R.id.img_backdrop)
    ImageView imgBackdrop;
    @BindView(R.id.img_poster)
    ImageView imgPoster;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.txt_rating)
    TextView txtRating;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.txt_release_date)
    TextView txtReleaseDate;
    @BindView(R.id.txt_overview)
    TextView txtOverview;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar_layout)
    AppBarLayout appbarLayout;
    @BindView(R.id.btn_favorites)
    ToggleButton btnFavorite;
    @BindView(R.id.txt_rating_label)
    TextView txtRatingLabel;
    @BindView(R.id.txt_language)
    TextView txtLanguage;
    @BindView(R.id.layout)
    RelativeLayout layout;
    @BindView(R.id.btn_review_count)
    Button btnReviewCount;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.recycler_view_trailers)
    RecyclerView recyclerViewTrailers;
    @BindView(R.id.fab_share)
    FloatingActionButton fabShare;

    private Movie movie;
    private MovieDbHelper movieDbHelper;
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();
    private ReviewAdapter reviewAdapter;
    private static final int REVIEW_LOADER_ID = 10;
    private TrailerAdapter trailerAdapter;
    private static final int TRAILER_LOADER_ID = 20;
    private int reviewCount = 0;
    private List<Review> reviewList;
    private List<Trailer> trailerList;
    private static final String SHARE_INTENT_HASHTAG = "\n#PopularMoviesApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        movieDbHelper = new MovieDbHelper(this);

        setupBackNavigation();

        /*
          get clicked Movie object passed from MainActivity
         */
        movie = getIntent().getParcelableExtra(MainActivity.KEY_MOVIE_RESPONSE);

        /**
         * set addOnOffsetChangedListener to appbar to indicate if it is expanded or collapsed
         * show back button if it is collapsed, hide otherwise
         */
        appbarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int offsetAlpha = (int) (appBarLayout.getY() / appBarLayout.getTotalScrollRange());
                toolbar.getNavigationIcon().setAlpha(offsetAlpha);
            }
        });

        /**
         * listener registered to detect when user changes favorite
         */
        btnFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int checked;
                if (isChecked) {
                    Cursor cursor = movieDbHelper.getMovieById(movie.getId());
                    //int fav = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.KEY_COLUMN_FAVORITE));
                    if (null != cursor) {
                        String movieId = String.valueOf(movie.getId());
                        getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(movieId).build(),
                                null, null);
                    }
                    /**
                     * insert favorites into uer's favorite movies collection
                     */
                    int favorite = 1;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_MOVIE_ID, movie.getId());
                    contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_FAVORITE, favorite);
                    contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_TITLE, movie.getTitle());
                    contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
                    contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
                    contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_POSTER_PATH, movie.getPosterPath());
                    contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_BACKDROP_PATH, movie.getBackdropPath());
                    contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_RELEASE_DATE, movie.getReleaseDate());
                    contentValues.put(MovieContract.MovieEntry.KEY_COLUMN_OVERVIEW, movie.getOverview());
                    Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);
                    if (null != uri) {
                        checked = 1;
                    } else {
                        checked = 0;
                    }
                } else {
                    String movieId = String.valueOf(movie.getId());
                    int deleted = getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(movieId).build(),
                            null, null);
                    if (deleted > 0) {
                        checked = 0;
                    } else {
                        checked = 1;
                    }
                }
                movie.setIsFavorite(checked);
                setFavorite(checked);
            }
        });

        populateMovieDetails();

        if (NetworkUtils.isNetworkAvailable(this)) {
            loadReviews();
            setupTrailers();
        }
    }

    /**
     * method to set up trailer recyclerview
     */
    private void setupTrailers() {
        trailerAdapter = new TrailerAdapter(this, this);
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTrailers.setAdapter(trailerAdapter);
        recyclerViewTrailers.setNestedScrollingEnabled(false);
        getSupportLoaderManager().initLoader(TRAILER_LOADER_ID, null, this);
    }

    /**
     * method to set up review recyclerviews
     */
    private void loadReviews() {
        getSupportLoaderManager().initLoader(REVIEW_LOADER_ID, null, this);
    }

    /**
     * method to set back navigation to {@link MovieDetailsActivity}
     */
    private void setupBackNavigation() {
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_left_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * method to populate movie details on the views
     * populate original title, movie poster, image thumbnail, A plot synopsis,
     * user rating and release date
     */
    private void populateMovieDetails() {

        txtRating.setText(String.valueOf(movie.getVoteAverage()));
        txtTitle.setText(movie.getTitle());
        txtReleaseDate.setText(CommonUtils.formatDate("yyyy-mm-dd", "MMM dd, yyyy", movie.getReleaseDate()));
        txtOverview.setText(movie.getOverview());

        /*
          set collapsible toolbar and title expanded text color
         */
        collapsingToolbarLayout.setTitle(movie.getOriginalTitle());
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.colorTransparent));

        Log.d("checkPaths","movie.getBackdropPath() : " + movie.getBackdropPath());

        /*
          set backdrop image using picasso
          and set callback inorder to get access to the primary colors in an image,
          as well as the corresponding colors for overlaid text
         */
        Picasso.with(this)
                .load(MovieDBUtils.URL_BACKDROP_PATH +movie.getBackdropPath())
                .into(imgBackdrop, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = getBitmapFromImage(imgBackdrop);
                        if (null != bitmap) {
                            Palette.Swatch[] pSwatches = new Palette.Swatch[CommonUtils.SWATCH_ARRAY_LENGTH];
                            createPaletteAsync(bitmap);


                        }
                    }

                    @Override
                    public void onError() {

                    }
                });

        /*
          set movie poster image
         */
        Picasso.with(this)
                .load(MovieDBUtils.URL_POSTER_PATH + movie.getPosterPath())
                .fit()
                .placeholder(R.drawable.ic_place_holder)
                .error(R.drawable.ic_place_holder)
                .into(imgPoster);
        setFavorite(movie.isFavorite());
    }

    /**
     * method to set favorite based either on the value retrieved rom local db
     * or when user click on the favorite
     *
     * @param isFavorite favorite or not
     */
    private void setFavorite(int isFavorite) {
        if (isFavorite == 1) {
            btnFavorite.setChecked(true);
            btnFavorite.setBackgroundResource(R.drawable.ic_favorite);
        } else {
            btnFavorite.setChecked(false);
            btnFavorite.setBackgroundResource(R.drawable.ic_not_favorite);
        }

    }

    /**
     * method to generate bitmap from image
     *
     * @param imgBackdrop image from which bitmap is to be generated
     * @return generated bitmap
     */
    public static Bitmap getBitmapFromImage(ImageView imgBackdrop) {
        return ((BitmapDrawable) imgBackdrop.getDrawable()).getBitmap();
    }

    /**
     * method to generate palette asynchronously
     *
     * @param bitmap the bitmap from which palette is to be created
     */
    public void createPaletteAsync(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            /**
             *Use palette on a different thread using onGenerated
             * @param palette generated palette
             */
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch pSwatches1 = palette.getDarkVibrantSwatch();
                Palette.Swatch pSwatches2 = palette.getLightVibrantSwatch();


                if (null != pSwatches1 && null != pSwatches2) {
                    collapsingToolbarLayout.setContentScrimColor(pSwatches1.getRgb());
                    collapsingToolbarLayout.setCollapsedTitleTextColor(pSwatches2.getRgb());
                } else {
                    collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary));
                    collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.colorWhite));
                }

            }
        });
    }

    /**
     * metod ovveriden to send the favorite sttus back to MainActivity
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();

        Bundle bundle = new Bundle();
        bundle.putParcelable(MainActivity.KEY_MOVIE_RESPONSE, movie);
        bundle.putInt(MainActivity.KEY_CLICKED_POSITION, getIntent().getIntExtra(MainActivity.KEY_CLICKED_POSITION, -1));

        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case REVIEW_LOADER_ID:
                return new ReviewLoader(this, movie.getId());
            case TRAILER_LOADER_ID:
                return new TrailerLoader(this, movie.getId());
            default:
                throw new RuntimeException("Loader not implemented");
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        int loaderId = loader.getId();
        switch (loaderId) {
            case REVIEW_LOADER_ID:
                Log.d(TAG, "data : " + data);
                reviewList = (List<Review>) data;
                setReviewCount(reviewList.size());
                break;
            case TRAILER_LOADER_ID:
                Log.d(TAG, "data : " + data);
                trailerList = (List<Trailer>) data;
                trailerAdapter.setTrailerList((List<Trailer>) data);
                break;
            default:
                throw new RuntimeException("Loader not implemented");
        }
    }

    /**
     * method to set nyumber of reviews
     */
    private void setReviewCount(int count) {
        reviewCount = count;
        btnReviewCount.setVisibility(View.VISIBLE);
        btnReviewCount.setText(getResources().getQuantityString(R.plurals.review_count, count, count));
        if (count == 0)
            btnReviewCount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        else
            btnReviewCount.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_right_arrow), null);
    }


    @Override
    public void onLoaderReset(Loader loader) {
        int loaderId = loader.getId();
        switch (loaderId) {
            case REVIEW_LOADER_ID:
                reviewList = null;
                setReviewCount(0);
                break;
            case TRAILER_LOADER_ID:
                trailerList = null;
                trailerAdapter.setTrailerList(null);
                break;
            default:
                throw new RuntimeException("Loader not implemented");
        }
    }

    @Override
    public void onTrailerClick(String key) {
        Uri uriVideo = Uri.parse(MovieDBUtils.TRAILER_BASE_YOUTUBE_PATH + key);

        Intent intentYoutube = new Intent(Intent.ACTION_VIEW);
        intentYoutube.setData(uriVideo);

        /**
         * start the intent only if sutable app exists
         */
        if (intentYoutube.resolveActivity(getPackageManager()) != null)
            startActivity(intentYoutube);
        else
            Toast.makeText(this, "Cannot play video", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_review_count)
    public void clickOnReview() {
        if (reviewCount != 0) {
            Intent iReview = new Intent(MovieDetailsActivity.this, ReviewsActivity.class);
            iReview.putParcelableArrayListExtra("reviews", (ArrayList<? extends Parcelable>) reviewList);
            startActivity(iReview);
        }
    }
    /**
     * method to share the first trailer
     * it will open all the available applications to share the data
     */
    @OnClick(R.id.fab_share)
    public void shareTrailerLink() {
        Intent iShare = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(MovieDBUtils.TRAILER_BASE_YOUTUBE_PATH + trailerList.get(0).getKey() + SHARE_INTENT_HASHTAG)
                .getIntent();
        startActivity(iShare);
    }
}
