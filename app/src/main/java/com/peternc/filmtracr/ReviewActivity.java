package com.peternc.filmtracr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.peternc.filmtracr.db.MovieDatabase;
import com.peternc.filmtracr.db.MovieEntity;

import java.util.List;
import java.util.Locale;

public class ReviewActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String SHARED_PREF = "filmtracr_sharedprefs";

    public static final int CREATE_REVIEW = 0;
    public static final int UPDATE_REVIEW = 1;
    public static final int DELETE_REVIEW = 2;

    public static final String UPDATE = "update";
    public static final String INDEX = "index";
    public static final String MOVIE_COUNT = "movie_count";
    public static final String MOVIE_TITLE = "movie_title";
    public static final String REVIEW_TITLE = "review_title";
    public static final String REVIEW = "review_text";
    public static final String RATING = "rating";
    public static final String OLD_RATING = "old_rating";
    public static final String NIGHT = "night";

    private Repository repository;

    public RecyclerView.Adapter<?> mAdapterFive;
    public RecyclerView.Adapter<?> mAdapterFour;
    public RecyclerView.Adapter<?> mAdapterThree;
    public RecyclerView.Adapter<?> mAdapterTwo;
    public RecyclerView.Adapter<?> mAdapterOne;

    private RecyclerView fiveStar;
    private RecyclerView fourStar;
    private RecyclerView threeStar;
    private RecyclerView twoStar;
    private RecyclerView oneStar;

    private TextView fiveStarNumber;
    private TextView fourStarNumber;
    private TextView threeStarNumber;
    private TextView twoStarNumber;
    private TextView oneStarNumber;

    private ImageView fiveStarArrow;
    private ImageView fourStarArrow;
    private ImageView threeStarArrow;
    private ImageView twoStarArrow;
    private ImageView oneStarArrow;

    private boolean fiveCollapsed;
    private boolean fourCollapsed;
    private boolean threeCollapsed;
    private boolean twoCollapsed;
    private boolean oneCollapsed;

    private boolean nightMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        //Check SharedPrefs to see if night mode is active
        checkIfNightMode();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createReview();
            }
        });

        //Init Repository and MovieList
        repository = Repository.getInstance(getApplicationContext());
        repository.setMovieList(new MovieList());

        //Init UI State
        fiveStar = findViewById(R.id.five_star);
        fourStar = findViewById(R.id.four_star);
        threeStar = findViewById(R.id.three_star);
        twoStar = findViewById(R.id.two_star);
        oneStar = findViewById(R.id.one_star);

        fiveStarNumber = findViewById(R.id.five_star_number);
        fourStarNumber = findViewById(R.id.four_star_number);
        threeStarNumber = findViewById(R.id.three_star_number);
        twoStarNumber = findViewById(R.id.two_star_number);
        oneStarNumber = findViewById(R.id.one_star_number);

        fiveStarArrow = findViewById(R.id.five_star_arrow);
        fourStarArrow = findViewById(R.id.four_star_arrow);
        threeStarArrow = findViewById(R.id.three_star_arrow);
        twoStarArrow = findViewById(R.id.two_star_arrow);
        oneStarArrow = findViewById(R.id.one_star_arrow);
        //Possible unneeded
        if(fiveStar.getVisibility() == View.GONE) fiveCollapsed = true;
        if(fourStar.getVisibility() == View.GONE) fourCollapsed = true;
        if(threeStar.getVisibility() == View.GONE) threeCollapsed = true;
        if(twoStar.getVisibility() == View.GONE) twoCollapsed = true;
        if(oneStar.getVisibility() == View.GONE) oneCollapsed = true;

        initRecyclerView();
    }

    //Configure Recycler Views
    private void initRecyclerView(){
        for(int i=5; i>0; i--) {
            RecyclerView recyclerView;
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            switch (i){
                case 5:
                    recyclerView = findViewById(R.id.five_star);
                    recyclerView.setLayoutManager(layoutManager);
                    mAdapterFive = new RecyclerViewAdapter(this,repository.getMovieList().getList(i));
                    recyclerView.setAdapter(mAdapterFive);
                    break;
                case 4:
                    recyclerView = findViewById(R.id.four_star);
                    recyclerView.setLayoutManager(layoutManager);
                    mAdapterFour = new RecyclerViewAdapter(this,repository.getMovieList().getList(i));
                    recyclerView.setAdapter(mAdapterFour);
                    break;
                case 3:
                    recyclerView = findViewById(R.id.three_star);
                    recyclerView.setLayoutManager(layoutManager);
                    mAdapterThree = new RecyclerViewAdapter(this,repository.getMovieList().getList(i));
                    recyclerView.setAdapter(mAdapterThree);
                    break;
                case 2:
                    recyclerView = findViewById(R.id.two_star);
                    recyclerView.setLayoutManager(layoutManager);
                    mAdapterTwo = new RecyclerViewAdapter(this,repository.getMovieList().getList(i));
                    recyclerView.setAdapter(mAdapterTwo);
                    break;
                case 1:
                    recyclerView = findViewById(R.id.one_star);
                    recyclerView.setLayoutManager(layoutManager);
                    mAdapterOne = new RecyclerViewAdapter(this,repository.getMovieList().getList(i));
                    recyclerView.setAdapter(mAdapterOne);
            }
        }

        initData();
    }

    //Populate lists with data from database
    private void initData(){
        //Show progress bar while loading data
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        MovieDatabase database = repository.getDatabase();
        Thread thread = new Thread() {
            @Override
            public void run() {
                for(int i = 5; i>0; i--) {
                    List<MovieEntity> movies = database.movieDao().findAllByRating(i);
                    for (MovieEntity movie : movies) {
                        Movie m = new Movie(movie.getMovieTitle(), movie.getRating(), movie.getReviewTitle(), movie.getReview());
                        repository.getMovieList().addToList(i, m);
                    }
                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Hide progress bar after loading
        progressBar.setVisibility(View.GONE);

        initMovieCount();
    }

    //Init the number of movies in each list
    private void initMovieCount(){
        TextView number;
        for(int i=5; i>0; i--){
            switch (i){
                case 5:
                    number = fiveStarNumber;
                    break;
                case 4:
                    number = fourStarNumber;
                    break;
                case 3:
                    number = threeStarNumber;
                    break;
                case 2:
                    number = twoStarNumber;
                    break;
                default:
                    number = oneStarNumber;
            }

            number.setText(String.format(
                    Locale.US,"%d",
                    repository.getMovieList().getListSize(i)));
        }
    }

    //Check sharded prefs for night mode flag
    private void checkIfNightMode(){
        SharedPreferences sharedPrefs = getSharedPreferences(ReviewActivity.SHARED_PREF, Context.MODE_PRIVATE);
        boolean night = sharedPrefs.getBoolean(NIGHT,false);
        if(night){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            nightMode = true;
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            nightMode = false;
        }
    }

    //Set the number of movies of a specific list
    public void setMovieCount(int rating, int count){
        switch (rating){
            case 5:
                fiveStarNumber.setText(String.format(Locale.US,"%d",count));
                break;
            case 4:
                fourStarNumber.setText(String.format(Locale.US,"%d",count));
                break;
            case 3:
                threeStarNumber.setText(String.format(Locale.US,"%d",count));
                break;
            case 2:
                twoStarNumber.setText(String.format(Locale.US,"%d",count));
                break;
            case 1:
                oneStarNumber.setText(String.format(Locale.US,"%d",count));
        }
    }

    //Sort list after binding of data (Can't call notify while computing layout exception)
    /*public void sortMovieList(RecyclerViewAdapter adapter, int rating){
        Collections.sort(repository.getMovieList().getList(rating));
        adapter.notifyDataSetChanged();
    }*/

    //Create New Review
    private void createReview(){
        Intent intent = new Intent(this, CreateReviewActivity.class);
        startActivityForResult(intent,CREATE_REVIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CREATE_REVIEW){
            if(data != null) {
                //Update movie count
                setMovieCount(data.getExtras().getInt(RATING),data.getExtras().getInt(MOVIE_COUNT));
            }
            //Refresh list if expanded
            refreshViews();
        }
        if(requestCode == UPDATE_REVIEW && data != null){
            //Move movie from one list to another if rating changed
            int oldRating = data.getExtras().getInt(OLD_RATING);
            if(data.getExtras().getInt(RATING) != 0){
                int rating = data.getExtras().getInt(RATING);
                //Swipe not closing
                //Add movie to new list
                repository.getMovieList().addToList(rating,
                        repository.getMovieList().getMovieFromList(oldRating,data.getExtras().getInt(INDEX)));
                //Remove movie from old list
                repository.getMovieList().removeMovieFromList(oldRating,
                        repository.getMovieList().getMovieFromList(oldRating,data.getExtras().getInt(INDEX)));
                //Notify Recycler view of change and change count
                updateRecyclerView(rating);
            }
            //Get recycler view of edited movie
            RecyclerView recyclerView;
            switch (data.getExtras().getInt(OLD_RATING)){
                case 5:
                    recyclerView = fiveStar;
                    break;
                case 4:
                    recyclerView = fourStar;
                    break;
                case 3:
                    recyclerView = threeStar;
                    break;
                case 2:
                    recyclerView = twoStar;
                    break;
                default:
                    recyclerView = oneStar;
            }
            //Close the swipe view after edit
            SwipeRevealLayout swipe = (SwipeRevealLayout) recyclerView.getChildAt(data.getExtras().getInt(INDEX));
            swipe.close(false);
            //only updates when clicked on recycler view
            updateRecyclerView(oldRating);
        }
        if(requestCode == DELETE_REVIEW && data != null){
            int rating = data.getExtras().getInt(OLD_RATING);
            switch (rating){
                case 5:
                    mAdapterFive.notifyItemRemoved(data.getExtras().getInt(INDEX));
                    break;
                case 4:
                    mAdapterFour.notifyItemRemoved(data.getExtras().getInt(INDEX));
                    break;
                case 3:
                    mAdapterThree.notifyItemRemoved(data.getExtras().getInt(INDEX));
                    break;
                case 2:
                    mAdapterTwo.notifyItemRemoved(data.getExtras().getInt(INDEX));
                    break;
                case 1:
                    mAdapterOne.notifyItemRemoved(data.getExtras().getInt(INDEX));
            }
            //Update the count
            setMovieCount(rating,data.getExtras().getInt(MOVIE_COUNT));
            //Collapse list if list is empty
            collapseIfEmpty(rating);
        }
    }

    private void updateRecyclerView(int rating){
        switch (rating){
            case 5:
                mAdapterFive.notifyDataSetChanged();
                setMovieCount(5,mAdapterFive.getItemCount());
                collapseIfEmpty(5);
                break;
            case 4:
                mAdapterFour.notifyDataSetChanged();
                setMovieCount(4,mAdapterFour.getItemCount());
                collapseIfEmpty(4);
                break;
            case 3:
                mAdapterThree.notifyDataSetChanged();
                setMovieCount(3,mAdapterThree.getItemCount());
                collapseIfEmpty(3);
                break;
            case 2:
                mAdapterTwo.notifyDataSetChanged();
                setMovieCount(2,mAdapterTwo.getItemCount());
                collapseIfEmpty(2);
                break;
            case 1:
                mAdapterOne.notifyDataSetChanged();
                setMovieCount(1,mAdapterOne.getItemCount());
                collapseIfEmpty(1);
        }
    }

    //Refreshes the view if added while expanded
    private void refreshViews(){
        if(!fiveCollapsed){
            if(repository.getMovieList().getList(5).size() == 1)
                collapse(findViewById(R.id.five_star_no_movie),fiveStarArrow);
            collapse(fiveStar,fiveStarArrow);
            expand(fiveStar,fiveStarArrow);
        }
        if(!fourCollapsed){
            if(repository.getMovieList().getList(4).size() == 1)
                collapse(findViewById(R.id.four_star_no_movie),fourStarArrow);
            collapse(fourStar,fourStarArrow);
            expand(fourStar,fourStarArrow);
        }
        if(!threeCollapsed){
            if(repository.getMovieList().getList(3).size() == 1)
                collapse(findViewById(R.id.three_star_no_movie),threeStarArrow);
            collapse(threeStar,threeStarArrow);
            expand(threeStar,threeStarArrow);
        }
        if(!twoCollapsed){
            if(repository.getMovieList().getList(2).size() == 1)
                collapse(findViewById(R.id.two_star_no_movie),twoStarArrow);
            collapse(twoStar,twoStarArrow);
            expand(twoStar,twoStarArrow);
        }
        if(!oneCollapsed){
            if(repository.getMovieList().getList(1).size() == 1)
                collapse(findViewById(R.id.one_star_no_movie),oneStarArrow);
            collapse(oneStar,oneStarArrow);
            expand(oneStar,oneStarArrow);
        }
    }

    //Implement Expansion Click Listeners
    public void expandFiveStar(View view){
        if(fiveCollapsed) {
            if(!repository.getMovieList().getList(5).isEmpty())
                expand(fiveStar,fiveStarArrow);
            else
                expand(findViewById(R.id.five_star_no_movie),fiveStarArrow);
            fiveCollapsed = false;
        } else {
            collapse(fiveStar,fiveStarArrow);
            collapse(findViewById(R.id.five_star_no_movie),fiveStarArrow);
            fiveCollapsed = true;
        }
    }

    public void expandFourStar(View view){
        if(fourCollapsed) {
            if(!repository.getMovieList().getList(4).isEmpty())
                expand(fourStar,fourStarArrow);
            else
                expand(findViewById(R.id.four_star_no_movie),fourStarArrow);
            fourCollapsed = false;
        } else {
            collapse(fourStar,fourStarArrow);
            collapse(findViewById(R.id.four_star_no_movie),fourStarArrow);
            fourCollapsed = true;
        }
    }

    public void expandThreeStar(View view){
        if(threeCollapsed) {
            if(!repository.getMovieList().getList(3).isEmpty())
                expand(threeStar,threeStarArrow);
            else
                expand(findViewById(R.id.three_star_no_movie),threeStarArrow);
            threeCollapsed = false;
        } else {
            collapse(threeStar,threeStarArrow);
            collapse(findViewById(R.id.three_star_no_movie),threeStarArrow);
            threeCollapsed = true;
        }
    }

    public void expandTwoStar(View view){
        if(twoCollapsed) {
            if(!repository.getMovieList().getList(2).isEmpty())
                expand(twoStar,twoStarArrow);
            else
                expand(findViewById(R.id.two_star_no_movie),twoStarArrow);
            twoCollapsed = false;
        } else {
            collapse(twoStar,twoStarArrow);
            collapse(findViewById(R.id.two_star_no_movie),twoStarArrow);
            twoCollapsed = true;
        }
    }

    public void expandOneStar(View view){
        if(oneCollapsed) {
            if(!repository.getMovieList().getList(1).isEmpty())
                expand(oneStar,oneStarArrow);
            else
                expand(findViewById(R.id.one_star_no_movie),oneStarArrow);
            oneCollapsed = false;
        } else {
            collapse(oneStar,oneStarArrow);
            collapse(findViewById(R.id.one_star_no_movie),oneStarArrow);
            oneCollapsed = true;
        }
    }

    //Collapse list if empty
    public void collapseIfEmpty(int stars){
        switch (stars){
            case 5:
                collapse(fiveStar,fiveStarArrow);
                fiveCollapsed = true;
                break;
            case 4:
                collapse(fourStar,fourStarArrow);
                fourCollapsed = true;
                break;
            case 3:
                collapse(threeStar,threeStarArrow);
                threeCollapsed = true;
                break;
            case 2:
                collapse(twoStar,twoStarArrow);
                twoCollapsed = true;
                break;
            case 1:
                collapse(oneStar,oneStarArrow);
                oneCollapsed = true;
        }
    }

    //Expand Animation
    private void expand(final View list, final View arrow) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) list.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        list.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = list.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        list.getLayoutParams().height = 1;
        list.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                list.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                list.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int)(targetHeight / list.getContext().getResources().getDisplayMetrics().density));
        list.startAnimation(a);

        RotateAnimation rotate = getRotateAnimation(0f,90f);
        arrow.startAnimation(rotate);
    }

    //Collapse Animation
    private void collapse(final View list, final View arrow) {
        final int initialHeight = list.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    list.setVisibility(View.GONE);
                }else{
                    list.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    list.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int)(initialHeight / list.getContext().getResources().getDisplayMetrics().density));
        list.startAnimation(a);

        RotateAnimation rotate = getRotateAnimation(90f,0f);
        arrow.startAnimation(rotate);
    }

    //Rotate Animation
    private RotateAnimation getRotateAnimation(float from, float to){
        RotateAnimation rotate = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(100);
        rotate.setFillAfter(true);
        rotate.setInterpolator(new DecelerateInterpolator());
        return rotate;
    }

    //Options Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            int nightModeFlags =
                    getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    nightMode = true;
                    break;

                case Configuration.UI_MODE_NIGHT_NO:
                    nightMode = false;
                    break;
            }
            intent.putExtra(NIGHT,nightMode);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}