package com.peternc.filmtracr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.peternc.filmtracr.db.MovieDatabase;
import com.peternc.filmtracr.db.MovieEntity;

public class CreateReviewActivity extends AppCompatActivity {
    private static final String TAG = "CreateReviewActivity";
    
    private EditText movieTitle;
    private EditText reviewTitle;
    private EditText review;
    private RatingBar rating;

    private boolean updating = false;
    private int index;
    private int oldRating;
    private String oldTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        movieTitle = findViewById(R.id.create_movie_title);
        reviewTitle = findViewById(R.id.create_review_title);
        review = findViewById(R.id.create_review);
        rating = findViewById(R.id.create_rating);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            Button button = findViewById(R.id.create_button);
            button.setText(R.string.btn_update);
            initValues(intent);
        }
    }

    private void initValues(Intent intent){
        oldTitle = intent.getExtras().getString(ReviewActivity.MOVIE_TITLE);
        oldRating = intent.getExtras().getInt(ReviewActivity.RATING);

        movieTitle.setText(intent.getExtras().getString(ReviewActivity.MOVIE_TITLE));
        reviewTitle.setText(intent.getExtras().getString(ReviewActivity.REVIEW_TITLE));
        review.setText(intent.getExtras().getString(ReviewActivity.REVIEW));
        rating.setProgress(oldRating);
        updating = intent.getExtras().getBoolean(ReviewActivity.UPDATE);
        index = intent.getExtras().getInt(ReviewActivity.INDEX);
    }

    //Create the review
    public void createReview(View view){
        if(!movieTitle.getText().toString().isEmpty() && !reviewTitle.getText().toString().isEmpty()
            && !review.getText().toString().isEmpty()){
            MovieDatabase database = Repository.getInstance(getApplicationContext()).getDatabase();
            if(updating){
                //Update movie in database
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        MovieEntity entity = database.movieDao().findByTitle(oldTitle);
                        entity.setMovieTitle(movieTitle.getText().toString());
                        entity.setReviewTitle(reviewTitle.getText().toString());
                        entity.setReview(review.getText().toString());
                        entity.setRating(rating.getProgress());
                        database.movieDao().update(entity);
                    }
                };
                thread.start();
                //Update movie list
                Movie movie = Repository.getInstance(getApplicationContext()).getMovieList().getMovieFromList(oldRating,index);
                movie.setMovieTitle(movieTitle.getText().toString());
                movie.setReviewTitle(reviewTitle.getText().toString());
                movie.setReview(review.getText().toString());
                int newRating = rating.getProgress();
                movie.setRating(newRating);
                //Create data for result
                Intent data = new Intent();
                data.putExtra(ReviewActivity.MOVIE_TITLE,movieTitle.getText().toString());
                data.putExtra(ReviewActivity.REVIEW_TITLE,reviewTitle.getText().toString());
                data.putExtra(ReviewActivity.REVIEW,review.getText().toString());
                data.putExtra(ReviewActivity.INDEX,index);
                data.putExtra(ReviewActivity.OLD_RATING, oldRating);
                if(oldRating != newRating) {
                    data.putExtra(ReviewActivity.RATING, newRating);
                }
                setResult(ReviewActivity.UPDATE_REVIEW,data);
            } else {
                //Create movie for database
                MovieEntity entity = new MovieEntity(movieTitle.getText().toString(),
                        reviewTitle.getText().toString(),
                        review.getText().toString(),
                        rating.getProgress());
                Repository.getInstance(getApplicationContext()).insert(entity);
                //Create movie for list
                Movie movie = new Movie(movieTitle.getText().toString(), rating.getProgress(),
                        reviewTitle.getText().toString(), review.getText().toString());
                MovieList movieList = Repository.getInstance(getApplicationContext()).getMovieList();
                movieList.addToList(rating.getProgress(), movie);
                //Create data for return result
                Intent data = new Intent();
                data.putExtra(ReviewActivity.MOVIE_COUNT,
                        Repository.getInstance(getApplicationContext()).getMovieList().getListSize(rating.getProgress()));
                data.putExtra(ReviewActivity.RATING,rating.getProgress());
                setResult(ReviewActivity.CREATE_REVIEW,data);
            }
            finish();
        } else {
            Toast.makeText(this, "Make Sure Everything is Filled Out!", Toast.LENGTH_LONG).show();
        }
    }
}