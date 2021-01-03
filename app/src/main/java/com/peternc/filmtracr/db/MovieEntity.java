package com.peternc.filmtracr.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MovieEntity {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "movie_title")
    private String movieTitle;

    @ColumnInfo(name = "review_title")
    private String reviewTitle;

    @ColumnInfo(name = "review")
    private String review;

    @ColumnInfo(name = "rating")
    private int rating;

    public MovieEntity(@NonNull String movieTitle, @NonNull String reviewTitle, @NonNull String review,
                       int rating){
        this.movieTitle = movieTitle;
        this.reviewTitle = reviewTitle;
        this.review = review;
        this.rating = rating;
    }

    public String getMovieTitle(){
        return movieTitle;
    }

    public String getReviewTitle(){
        return reviewTitle;
    }

    public String getReview(){
        return review;
    }

    public int getRating(){
        return rating;
    }

    public void setMovieTitle(String movieTitle){
        this.movieTitle = movieTitle;
    }

    public void setReviewTitle(String reviewTitle){
        this.reviewTitle = reviewTitle;
    }

    public void setReview(String review){
        this.review = review;
    }

    public void setRating(int rating){
        this.rating = rating;
    }
}

