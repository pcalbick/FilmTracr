package com.peternc.filmtracr;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieList {
    private static final String TAG = "MovieList";
    
    private final List<Movie> fiveStarList;
    private final List<Movie> fourStarList;
    private final List<Movie> threeStarList;
    private final List<Movie> twoStarList;
    private final List<Movie> oneStarList;

    public MovieList(){
        fiveStarList = new ArrayList<>();
        fourStarList = new ArrayList<>();
        threeStarList = new ArrayList<>();
        twoStarList = new ArrayList<>();
        oneStarList = new ArrayList<>();
    }

    public void addToList(int stars, Movie movie){
        List<Movie> list = getList(stars);
        list.add(movie);
        //BROKEN; Something strange happens on second addition to list
        //Collections.sort(list);
    }

    public void removeMovieFromList(int stars, Movie movie){
        getList(stars).remove(movie);
    }

    public Movie getMovieFromList(int stars, int index) {
        List<Movie> movies = getList(stars);
        return movies.get(index);
    }

    public List<Movie> getList(int stars){
        switch (stars){
            case 1:
                return oneStarList;
            case 2:
                return twoStarList;
            case 3:
                return threeStarList;
            case 4:
                return fourStarList;
            default:
                return fiveStarList;
        }
    }

    public int getListSize(int stars){
        return getList(stars).size();
    }

    public int getTotalSize(){
        return fiveStarList.size()+fourStarList.size()+
                threeStarList.size()+twoStarList.size()+
                oneStarList.size();
    }
}
