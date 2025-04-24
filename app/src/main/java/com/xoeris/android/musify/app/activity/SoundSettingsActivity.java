package com.xoeris.android.musify.app.activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.xoeris.android.musify.R;

public class SoundSettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_sound_settings);

        findViewById(R.id.back).setOnClickListener(v -> finish());


        getSavedThemeMode();
    }

    private int getSavedThemeMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}