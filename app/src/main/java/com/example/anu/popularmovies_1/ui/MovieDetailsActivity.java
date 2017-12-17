package com.example.anu.popularmovies_1.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.anu.popularmovies_1.R;
import com.example.anu.popularmovies_1.adapter.MovieHolder;
import com.example.anu.popularmovies_1.data.MovieContract;
import com.example.anu.popularmovies_1.data.MovieDbHelper;
import com.example.anu.popularmovies_1.model.Movie;
import com.example.anu.popularmovies_1.utils.CommonUtils;
import com.example.anu.popularmovies_1.utils.MovieDBUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailsActivity extends AppCompatActivity {

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


    private Movie movie;
    private MovieDbHelper movieDbHelper;
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

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
                    if(null != uri){
                         checked = 1;
                    }
                    else {
                        checked = 0;
                    }
                }
                else {
                    String movieId = String.valueOf(movie.getId());
                    int deleted = getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(movieId).build(),
                            null, null);
                    if (deleted>0){
                         checked = 0;
                    }
                    else {
                        checked = 1;
                    }
                }
                movie.setIsFavorite(checked);
                setFavorite(checked);
            }
        });

        populateMovieDetails();


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

        /*
          set backdrop image using picasso
          and set callback inorder to get access to the primary colors in an image,
          as well as the corresponding colors for overlaid text
         */
        Picasso.with(this)
                .load(movie.getBackdropPath())
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
     * @param isFavorite favorite or not
     */
    private void setFavorite(int isFavorite) {
        if (isFavorite == 1) {
            btnFavorite.setChecked(true);
            btnFavorite.setBackgroundResource(R.drawable.ic_favorite);
        }
        else {
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
}
