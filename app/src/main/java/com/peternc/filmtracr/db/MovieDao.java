package com.peternc.filmtracr.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MovieDao {
    @Query("SELECT * FROM MovieEntity")
    List<MovieEntity> getAll();

    @Query("SELECT * FROM MovieEntity WHERE movie_title LIKE :title LIMIT 1")
    MovieEntity findByTitle(String title);

    @Query("SELECT * FROM MovieEntity WHERE rating like :movieRating ORDER BY movie_title ASC")
    List<MovieEntity> findAllByRating(int movieRating);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MovieEntity movie);

    @Update
    void update(MovieEntity movie);

    @Delete
    void delete(MovieEntity movie);
}

