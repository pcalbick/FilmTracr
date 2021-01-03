package com.peternc.filmtracr;

public class Movie implements Comparable<Movie> {
    private String movieTitle;
    private String reviewTitle;
    private String review;
    private int rating;

    public Movie(String movieTitle, int rating, String reviewTitle, String review){
        this.movieTitle = movieTitle;
        this.rating = rating;
        this.reviewTitle = reviewTitle;
        this.review = review;
    }

    public void setMovieTitle(String movieTitle){
        this.movieTitle = movieTitle;
    }

    public void setRating(int rating){
        this.rating = rating;
    }

    public void setReviewTitle(String reviewTitle){
        this.reviewTitle = reviewTitle;
    }

    public void setReview(String review){
        this.review = review;
    }

    public String getMovieTitle(){
        return movieTitle;
    }

    public int getRating(){
        return rating;
    }

    public String getReviewTitle(){
        return reviewTitle;
    }

    public String getReview(){
        return review;
    }

    @Override
    public int compareTo(Movie o) {
        return movieTitle.compareTo(o.movieTitle);
    }
}
