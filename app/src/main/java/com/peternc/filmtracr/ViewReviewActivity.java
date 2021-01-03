package com.peternc.filmtracr;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.peternc.filmtracr.db.MovieDatabase;
import com.peternc.filmtracr.db.MovieEntity;

public class ViewReviewActivity extends AppCompatActivity {
    private TextView movieTitle;
    private TextView reviewTitle;
    private TextView review;
    private int rating;
    private int index;
    private int movieCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_review);

        movieTitle = findViewById(R.id.view_movie_title);
        reviewTitle = findViewById(R.id.view_review_title);
        review = findViewById(R.id.view_review);
        RatingBar ratingBar = findViewById(R.id.view_rating);
        Intent intent = getIntent();

        movieTitle.setText(intent.getExtras().getString(ReviewActivity.MOVIE_TITLE));
        reviewTitle.setText(intent.getExtras().getString(ReviewActivity.REVIEW_TITLE));
        review.setText(intent.getExtras().getString(ReviewActivity.REVIEW));
        rating = intent.getExtras().getInt(ReviewActivity.RATING);
        ratingBar.setProgress(rating);
        index = intent.getExtras().getInt(ReviewActivity.INDEX);
        movieCount = intent.getExtras().getInt(ReviewActivity.MOVIE_COUNT);
    }

    public void viewEditClick(View view){
        Intent intent = new Intent(this, CreateReviewActivity.class);
        intent.putExtra(ReviewActivity.MOVIE_TITLE,movieTitle.getText().toString());
        intent.putExtra(ReviewActivity.REVIEW_TITLE,reviewTitle.getText().toString());
        intent.putExtra(ReviewActivity.REVIEW,review.getText().toString());
        intent.putExtra(ReviewActivity.RATING,rating);
        intent.putExtra(ReviewActivity.INDEX,index);
        intent.putExtra(ReviewActivity.UPDATE,true);
        startActivityForResult(intent,ReviewActivity.UPDATE_REVIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ReviewActivity.UPDATE_REVIEW && data != null){
            movieTitle.setText(data.getExtras().getString(ReviewActivity.MOVIE_TITLE));
            reviewTitle.setText(data.getExtras().getString(ReviewActivity.REVIEW_TITLE));
            review.setText(data.getExtras().getString(ReviewActivity.REVIEW));
            setResult(ReviewActivity.UPDATE_REVIEW,data);
        }
    }

    public void viewDeleteClick(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_question)
                .setPositiveButton(R.string.delete_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Remove from database
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                MovieDatabase database = Repository.getInstance(getApplicationContext()).getDatabase();
                                MovieEntity entity = database.movieDao().findByTitle(movieTitle.getText().toString());
                                database.movieDao().delete(entity);
                            }
                        };
                        thread.start();
                        //Remove the movie from the list
                        Repository.getInstance(getApplicationContext()).getMovieList().removeMovieFromList(rating,
                                Repository.getInstance(getApplicationContext()).getMovieList().getMovieFromList(rating,index));
                        dialog.dismiss();
                        Intent data = new Intent();
                        data.putExtra(ReviewActivity.MOVIE_COUNT,movieCount-1);
                        data.putExtra(ReviewActivity.OLD_RATING,rating);
                        data.putExtra(ReviewActivity.INDEX,index);
                        setResult(ReviewActivity.DELETE_REVIEW,data);
                        finish();
                    }
                })
                .setNegativeButton(R.string.delete_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button negativeButton = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                Button positiveButton = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                int nightModeFlags =
                        getResources().getConfiguration().uiMode &
                                Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        negativeButton.setTextColor(getResources().getColor(R.color.my_light_orange));
                        positiveButton.setTextColor(getResources().getColor(R.color.my_light_orange));
                        break;

                    case Configuration.UI_MODE_NIGHT_NO:
                        negativeButton.setTextColor(getResources().getColor(R.color.my_orange));
                        positiveButton.setTextColor(getResources().getColor(R.color.my_orange));
                        break;
                }
                negativeButton.invalidate();
                positiveButton.invalidate();
            }
        });
        dialog.show();
    }

    public void viewShareClick(View view){
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setType("text/plain");
        String stars = " stars! - ";
        if(rating == 1) stars = " star! - ";
        intent.putExtra(Intent.EXTRA_SUBJECT, movieTitle.getText().toString() + " - " + reviewTitle.getText().toString());
        intent.putExtra(Intent.EXTRA_TEXT, movieTitle.getText().toString() + " - " + rating + stars + review.getText().toString());

        if (intent.resolveActivity(getPackageManager()) != null) {
            Intent chooser = intent.createChooser(intent, "Share Review");
            startActivity(chooser);
        }
    }
}