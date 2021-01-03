package com.peternc.filmtracr;

import android.content.Context;

import androidx.room.Room;

import com.peternc.filmtracr.db.MovieDao;
import com.peternc.filmtracr.db.MovieDatabase;
import com.peternc.filmtracr.db.MovieEntity;

public class Repository {
    private static Repository sInstance;
    private MovieList mMovieList;
    private final MovieDatabase database;
    private MovieDao mMovieDao;

    public Repository(Context context){
        database = MovieDatabase.getDatabase(context);
        mMovieDao = database.movieDao();
    }

    public static Repository getInstance(Context context){
        if(sInstance == null){
            synchronized (Repository.class){
                if(sInstance == null){
                    sInstance = new Repository(context);
                }
            }
        }
        return sInstance;
    }

    public MovieDatabase getDatabase() {
        return database;
    }

    public void insert(MovieEntity movieEntity) {
        MovieDatabase.databaseWriteExecutor.execute(() -> {
            mMovieDao.insert(movieEntity);
        });
    }

    public void delete(MovieEntity movieEntity){
        MovieDatabase.databaseWriteExecutor.execute(() -> {
            mMovieDao.delete(movieEntity);
        });
    }

    public void setMovieList(MovieList movieList){
        mMovieList = movieList;
    }

    public MovieList getMovieList(){
        return mMovieList;
    }
}
