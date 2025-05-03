package com.xoeris.android.musify.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.xoeris.android.musify.R;
import com.xoeris.android.xesc.system.core.module.media.ui.UltraVideo;

@SuppressWarnings("all")
public class MainActivity extends AppCompatActivity {
    public static int currentMode;
    private boolean hasStartedNext = false; // Prevent double calls
    private UltraVideo ultraVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate
        SharedPreferences preferences = getSharedPreferences("ThemePrefs", 0);
        int themeMode = preferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        // Determine current theme mode
        currentMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        String videoPath = "android.resource://" + getPackageName() + "/" +
                (currentMode == android.content.res.Configuration.UI_MODE_NIGHT_YES ?
                        R.raw.brand_logo_b : R.raw.brand_logo_w);

        ultraVideo = findViewById(R.id.videoView1);

        // Add null check to prevent crash
        if (ultraVideo == null) {
            // Handle the error - maybe start the next activity directly or show an error message
            startNextActivity();
            return;
        }

        Uri uri = Uri.parse(videoPath);

        // Configure video view
        ultraVideo.getVideoView().setVideoURI(uri);
        ultraVideo.getVideoView().setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setVolume(0, 0); // Mute the video
            mediaPlayer.setLooping(false);
            ultraVideo.getVideoView().start();
        });

        ultraVideo.getVideoView().setOnCompletionListener(mediaPlayer -> startNextActivity());
    }

    private void startNextActivity() {
        if (hasStartedNext || isFinishing()) return;

        hasStartedNext = true;
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Disabled to prevent back navigation
    }
}
