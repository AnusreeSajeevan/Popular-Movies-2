package com.example.anu.popularmovies_1.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.example.anu.popularmovies_1.R;
import com.example.anu.popularmovies_1.adapter.ReviewAdapter;
import com.example.anu.popularmovies_1.model.Review;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewsActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view_reviews)
    RecyclerView recyclerViewReviews;

    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        ButterKnife.bind(this);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setTitle(getResources().getString(R.string.reviews));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setupReviews();
    }

    /**
     * method to set up review recycler views
     */
    private void setupReviews() {
        reviewList = getIntent().getParcelableArrayListExtra("reviews");
        reviewAdapter = new ReviewAdapter(this);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setAdapter(reviewAdapter);
        recyclerViewReviews.setNestedScrollingEnabled(false);
        reviewAdapter.setReviewList(reviewList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
