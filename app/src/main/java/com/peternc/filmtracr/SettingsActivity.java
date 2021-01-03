package com.peternc.filmtracr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private SwitchCompat nightTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        nightTheme = findViewById(R.id.night_theme);

        Intent data = getIntent();
        if(data.getExtras() != null){
            nightTheme.setChecked(data.getExtras().getBoolean(ReviewActivity.NIGHT));
        }

        SharedPreferences sharedPrefs = getSharedPreferences(ReviewActivity.SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        nightTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(nightTheme.isChecked()){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean(ReviewActivity.NIGHT,true);
                editor.apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean(ReviewActivity.NIGHT,false);
                editor.apply();
            }
        });
    }

    public void backClick(View view){
        finish();
    }
}