package com.peternc.filmtracr;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.peternc.filmtracr.db.MovieDatabase;
import com.peternc.filmtracr.db.MovieEntity;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ListHolder> {
    private static final String TAG = "RecyclerViewAdapter";

    public static final String TITLE_CHANGE = "title-changed";

    private List<Movie> mMovies;
    private ReviewActivity mActivity;

    class ListHolder extends RecyclerView.ViewHolder {
        SwipeRevealLayout layout;
        TextView title;
        ImageButton editButton, deleteButton;
        FrameLayout titleHolder;
        ListHolder(View v){
            super(v);
            layout = (SwipeRevealLayout) v;
            title = (TextView) v.findViewById(R.id.movie_title);
            titleHolder = (FrameLayout) v.findViewById(R.id.movie_title_holder);
            editButton = (ImageButton) v.findViewById(R.id.edit_button);
            deleteButton = (ImageButton) v.findViewById(R.id.delete_button);
        }
    }

    public RecyclerViewAdapter(ReviewActivity activity, List<Movie> movieList) {
        mActivity = activity;
        mMovies = movieList;
    }

    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SwipeRevealLayout v = (SwipeRevealLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.swipe_movie_item, parent, false);

        return new ListHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ListHolder holder, int position) {
        //Set title of card
        holder.title.setText(mMovies.get(position).getMovieTitle());

        //Set click listener on top layer
        holder.titleHolder.setOnClickListener((v) -> {
            Intent intent = new Intent(mActivity,ViewReviewActivity.class);
            intent.putExtra(ReviewActivity.MOVIE_TITLE,mMovies.get(position).getMovieTitle());
            intent.putExtra(ReviewActivity.REVIEW_TITLE,mMovies.get(position).getReviewTitle());
            intent.putExtra(ReviewActivity.REVIEW,mMovies.get(position).getReview());
            intent.putExtra(ReviewActivity.RATING,mMovies.get(position).getRating());
            intent.putExtra(ReviewActivity.INDEX,position);
            intent.putExtra(ReviewActivity.MOVIE_COUNT,mMovies.size());
            mActivity.startActivityForResult(intent,ReviewActivity.UPDATE_REVIEW);
        });

        //Set delete button click listener
        holder.deleteButton.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage(R.string.delete_question)
                    .setPositiveButton(R.string.delete_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Store rating for updating movie count
                            int rating = mMovies.get(position).getRating();
                            //Close the drawer
                            holder.layout.close(false);
                            //Remove from database
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    MovieDatabase database = Repository.getInstance(mActivity.getApplicationContext()).getDatabase();
                                    MovieEntity entity = database.movieDao().findByTitle(mMovies.get(position).getMovieTitle());
                                    database.movieDao().delete(entity);
                                }
                            };
                            thread.start();
                            try{
                                thread.join();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            //Remove the movie from the list
                            Repository.getInstance(mActivity.getApplicationContext()).getMovieList()
                                    .removeMovieFromList(mMovies.get(position).getRating(),mMovies.get(position));
                            notifyDataSetChanged();
                            //Update the count
                            updateMovieCount(rating);
                            //Collapse list if list is empty
                            if(mMovies.isEmpty()) mActivity.collapseIfEmpty(rating);
                            //Close the dialog
                            dialog.dismiss();
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
                            mActivity.getResources().getConfiguration().uiMode &
                                    Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            negativeButton.setTextColor(mActivity.getResources().getColor(R.color.my_light_orange));
                            positiveButton.setTextColor(mActivity.getResources().getColor(R.color.my_light_orange));
                            break;

                        case Configuration.UI_MODE_NIGHT_NO:
                            negativeButton.setTextColor(mActivity.getResources().getColor(R.color.my_orange));
                            positiveButton.setTextColor(mActivity.getResources().getColor(R.color.my_orange));
                            break;
                    }
                    negativeButton.invalidate();
                    positiveButton.invalidate();
                }
            });
            dialog.show();
        });

        //Set edit button listener
        holder.editButton.setOnClickListener((v) -> {
            Intent intent = new Intent(mActivity, CreateReviewActivity.class);
            intent.putExtra(ReviewActivity.MOVIE_TITLE,mMovies.get(position).getMovieTitle());
            intent.putExtra(ReviewActivity.REVIEW_TITLE,mMovies.get(position).getReviewTitle());
            intent.putExtra(ReviewActivity.REVIEW,mMovies.get(position).getReview());
            intent.putExtra(ReviewActivity.RATING,mMovies.get(position).getRating());
            intent.putExtra(ReviewActivity.UPDATE,true);
            intent.putExtra(ReviewActivity.INDEX,position);
            mActivity.startActivityForResult(intent,ReviewActivity.UPDATE_REVIEW);
        });

        //Set focus change listener (DOES NOT DO ANYTHING)
        holder.layout.setOnFocusChangeListener((v,f) -> {
            holder.layout.close(false);
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ListHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        if(payloads.contains(TITLE_CHANGE)){
            holder.title.setText(mMovies.get(position).getMovieTitle());
        }

        //Update movie count (Add movie added payload)
        updateMovieCount(mMovies.get(position).getRating());
    }

    @Override
    public int getItemCount() {
        if(mMovies != null)
            return mMovies.size();
        return 0;
    }

    private void updateMovieCount(int rating){
        switch (rating){
            case 5:
                mActivity.setMovieCount(5,mMovies.size());
                break;
            case 4:
                mActivity.setMovieCount(4,mMovies.size());
                break;
            case 3:
                mActivity.setMovieCount(3,mMovies.size());
                break;
            case 2:
                mActivity.setMovieCount(2,mMovies.size());
                break;
            case 1:
                mActivity.setMovieCount(1,mMovies.size());
        }
        //mActivity.sortMovieList(this,rating);
    }
}
