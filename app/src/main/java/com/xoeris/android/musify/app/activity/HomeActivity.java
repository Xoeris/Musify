package com.xoeris.android.musify.app.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.xoeris.android.musify.R;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.manager.SoundFusionNotificationManager;
import com.xoeris.android.musify.app.fragment.HomeFragment;
import com.xoeris.android.musify.app.fragment.SettingsFragment;
import com.xoeris.android.xesc.system.core.module.media.ui.DialogSheet;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.service.SoundFusionService;
import com.xoeris.android.xesc.system.core.module.media.ui.VortexSlider;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SongByte;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SoundFusion;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class HomeActivity extends BaseActivity implements SoundFusion.OnMusicPlayerListener {
    private static final String KEY_FADE_DURATION = "fade_duration";
    private static final String KEY_REPEAT_MODE = "repeat_mode";
    private static final String KEY_SHUFFLE_STATE = "shuffle_state";
    public static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "music_player_prefs";
    private static final float SWIPE_THRESHOLD = 50.0f;
    public ImageView albumButton;
    private LinearLayout bottomFABLayout;
    private LinearLayout bottomLayout;
    private DialogSheet bottomSheetFragment;
    private ConstraintLayout constraintLayout1;
    private ConstraintLayout constraintLayout2;
    private ConstraintLayout constraintLayout3;
    private int currentPosition;
    private SongByte currentSongByte;
    private LinearLayout customFab;
    private int duration;
    private SeekBar fadeSeekBar;
    public ImageView homeIcon;
    private boolean isPlaying;
    public ImageView libraryIcon;
    private SoundFusionNotificationManager notificationManager;
    public ImageView playPauseButton;
    private VortexSlider seekBar;
    private Handler seekBarHandler;
    private Runnable seekBarRunnable;
    public ImageView settingIcon;
    private SharedPreferences sharedPreferences;
    public ImageView skipNextButton;
    public ImageView skipPreviousButton;
    private TextView songArtistTextView;
    private TextView songCurrentDuration;
    private TextView songDuration;
    private TextView songTitleTextView;
    private SoundFusion soundFusion;
    private LinearLayout topFABLayout;
    private FrameLayout topLayout;
    private float touchStartY;
    private boolean isExpanded = false;
    private Fragment currentFragment = null;
    private boolean isFirstPlay = true;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences("ThemePrefs", 0);
        int themeMode = preferences.getInt("theme_mode", -1);
        AppCompatDelegate.setDefaultNightMode(themeMode);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        boolean shuffleState = prefs.getBoolean(KEY_SHUFFLE_STATE, false);
        int repeatMode = prefs.getInt(KEY_REPEAT_MODE, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_home);
        this.soundFusion = SoundFusion.getInstance(this);
        this.soundFusion.setListener(this);
        this.notificationManager = new SoundFusionNotificationManager(this);
        setupSeekBarUpdater();
        this.songTitleTextView = (TextView) findViewById(R.id.song_title);
        this.songArtistTextView = (TextView) findViewById(R.id.song_artist);
        this.songTitleTextView.setSelected(true);
        this.songArtistTextView.setSelected(true);
        this.playPauseButton = (ImageView) findViewById(R.id.play_pause_icon);
        LinearLayout customFab = (LinearLayout) findViewById(R.id.custom_fab);
        ConstraintLayout container = (ConstraintLayout) findViewById(R.id.container_layout);
        this.bottomLayout = (LinearLayout) findViewById(R.id.bottom_fab_layout);
        this.skipPreviousButton = (ImageView) findViewById(R.id.skip_previous_icon);
        this.playPauseButton = (ImageView) findViewById(R.id.play_pause_icon);
        this.skipNextButton = (ImageView) findViewById(R.id.skip_next_icon);
        this.homeIcon = (ImageView) findViewById(R.id.home_icon);
        this.settingIcon = (ImageView) findViewById(R.id.setting_icon);
        this.albumButton = (ImageView) findViewById(R.id.album_icon);
        this.seekBar = (VortexSlider) findViewById(R.id.vortexSlider);
        this.songCurrentDuration = (TextView) findViewById(R.id.song_current_duration);
        this.songDuration = (TextView) findViewById(R.id.song_duration);
        this.bottomLayout.setVisibility(8);
        resetIconAndTint(this.homeIcon, R.drawable.ic_home_fill);
        resetIconAndTint(this.settingIcon, R.drawable.ic_settings);
        this.albumButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.activities.HomeActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                HomeActivity.this.showNowPlayingBottomSheet();
            }
        });
        customFab.setOnTouchListener(new View.OnTouchListener() { // from class: com.xoeris.app.musify.activities.HomeActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return HomeActivity.this.m206lambda$onCreate$0$comxoerisappmusifyactivitiesHomeActivity(view, motionEvent);
            }
        });
        customFab.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.activities.HomeActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (HomeActivity.this.isExpanded) {
                    HomeActivity.this.collapseView(HomeActivity.this.bottomLayout);
                } else {
                    HomeActivity.this.expandView(HomeActivity.this.bottomLayout);
                }
                HomeActivity.this.isExpanded = !HomeActivity.this.isExpanded;
            }
        });
        container.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.activities.HomeActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (HomeActivity.this.isExpanded) {
                    HomeActivity.this.collapseView(HomeActivity.this.bottomLayout);
                }
                HomeActivity.this.isExpanded = !HomeActivity.this.isExpanded;
            }
        });
        this.skipPreviousButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.activities.HomeActivity.6
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (HomeActivity.this.soundFusion != null) {
                    HomeActivity.this.soundFusion.playPrevious();
                    HomeActivity.this.updatePlayPauseButton(HomeActivity.this.soundFusion.isPlaying());
                    HomeActivity.this.updateMusicData();
                }
            }
        });
        this.skipNextButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.activities.HomeActivity.7
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (HomeActivity.this.soundFusion != null) {
                    HomeActivity.this.soundFusion.playNext();
                    HomeActivity.this.updatePlayPauseButton(HomeActivity.this.soundFusion.isPlaying());
                    HomeActivity.this.updateMusicData();
                }
            }
        });
        this.playPauseButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.activities.HomeActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                HomeActivity.this.m207lambda$onCreate$1$comxoerisappmusifyactivitiesHomeActivity(view);
            }
        });
        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
            updateIconAndTint(this.homeIcon, R.drawable.ic_home_fill);
        }
        this.homeIcon.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.activities.HomeActivity.8
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (!(HomeActivity.this.currentFragment instanceof HomeFragment)) {
                    HomeActivity.this.switchFragment(new HomeFragment());
                    HomeActivity.this.updateIconAndTint(HomeActivity.this.homeIcon, R.drawable.ic_home_fill);
                    HomeActivity.this.resetIconAndTint(HomeActivity.this.settingIcon, R.drawable.ic_settings);
                    HomeActivity.this.currentFragment = new HomeFragment();
                }
            }
        });
        this.settingIcon.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.activities.HomeActivity.9
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (!(HomeActivity.this.currentFragment instanceof SettingsFragment)) {
                    HomeActivity.this.switchFragment(new SettingsFragment());
                    HomeActivity.this.updateIconAndTint(HomeActivity.this.settingIcon, R.drawable.ic_settings_fill);
                    HomeActivity.this.resetIconAndTint(HomeActivity.this.homeIcon, R.drawable.ic_home);
                    HomeActivity.this.currentFragment = new SettingsFragment();
                }
            }
        });
        this.seekBar.setOnSeekBarChangeListener(new VortexSlider.OnSeekBarChangeListener() { // from class: com.xoeris.app.musify.activities.HomeActivity.10
            @Override // com.xoeris.system.core.module.media.ui.view.VortexSlider.OnSeekBarChangeListener
            public void onProgressChanged(VortexSlider vortexSlider, int progress, boolean fromUser) {
                if (fromUser) {
                    HomeActivity.this.soundFusion.seekTo(progress);
                }
            }

            @Override // com.xoeris.system.core.module.media.ui.view.VortexSlider.OnSeekBarChangeListener
            public void onStartTrackingTouch(VortexSlider vortexSlider) {
            }

            @Override // com.xoeris.system.core.module.media.ui.view.VortexSlider.OnSeekBarChangeListener
            public void onStopTrackingTouch(VortexSlider vortexSlider) {
            }
        });
        this.soundFusion = SoundFusion.getInstance(this);
        this.soundFusion.setListener(this);
        this.notificationManager = new SoundFusionNotificationManager(this);
        this.notificationManager.setActionListener(new AnonymousClass11());
        updateMusicData();
        updateAlbumArtwork();
        updateRepeatButton(repeatMode);
        checkAndRequestPermissions();
        startService(new Intent(this, (Class<?>) SoundFusionService.class));
        setDefaultAlbumArt();
    }

boolean m206lambda$onCreate$0$comxoerisappmusifyactivitiesHomeActivity(View v, MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                this.touchStartY = event.getY();
                break;
            case 1:
                float deltaY = this.touchStartY - event.getY();
                if (deltaY <= 50.0f) {
                    if (Math.abs(deltaY) < 10.0f) {
                        if (this.isExpanded) {
                            collapseView(this.bottomLayout);
                        } else {
                            expandView(this.bottomLayout);
                        }
                        this.isExpanded = !this.isExpanded;
                        break;
                    }
                } else {
                    showNowPlayingBottomSheet();
                    break;
                }
                break;
        }
        return true;
    }

    /* renamed from: lambda$onCreate$1$com-xoeris-app-musify-activities-HomeActivity, reason: not valid java name */
    /* synthetic */ void m207lambda$onCreate$1$comxoerisappmusifyactivitiesHomeActivity(View v) {
        this.soundFusion.playPause();
        updatePlayPauseButton(this.soundFusion.isPlaying());
        updateMusicData();
    }

    public SoundFusion getSoundFusion() {
        return soundFusion;
    }

    public VortexSlider getSeekBar() {
        return seekBar;
    }

    /* renamed from: com.xoeris.app.musify.activities.HomeActivity$11, reason: invalid class name */
    class AnonymousClass11 implements SoundFusionNotificationManager.NotificationActionListener {
        AnonymousClass11() {
        }

        @Override // com.xoeris.app.musify.classes.MusicNotificationManager.NotificationActionListener
        public void onPlayPauseClicked() {
            HomeActivity.this.runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.activities.HomeActivity$11$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    HomeActivity.AnonymousClass11.this.m212xd20157ea();
                }
            });
        }

        /* renamed from: lambda$onPlayPauseClicked$0$com-xoeris-app-musify-activities-HomeActivity$11, reason: not valid java name */
        /* synthetic */ void m212xd20157ea() {
            HomeActivity.this.soundFusion.playPause();
        }

        @Override // com.xoeris.app.musify.classes.MusicNotificationManager.NotificationActionListener
        public void onPreviousClicked() {
            HomeActivity.this.runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.activities.HomeActivity$11$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    HomeActivity.AnonymousClass11.this.m213x54990416();
                }
            });
        }

        /* renamed from: lambda$onPreviousClicked$1$com-xoeris-app-musify-activities-HomeActivity$11, reason: not valid java name */
        /* synthetic */ void m213x54990416() {
            HomeActivity.this.soundFusion.playPrevious();
        }

        @Override // com.xoeris.app.musify.classes.MusicNotificationManager.NotificationActionListener
        public void onNextClicked() {
            HomeActivity.this.runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.activities.HomeActivity$11$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    HomeActivity.AnonymousClass11.this.m211x1d7d2531();
                }
            });
        }

        /* renamed from: lambda$onNextClicked$2$com-xoeris-app-musify-activities-HomeActivity$11, reason: not valid java name */
        /* synthetic */ void m211x1d7d2531() {
            HomeActivity.this.soundFusion.playNext();
        }
    }

    public void updateAlbumArtwork() {
        if (this.soundFusion != null && this.soundFusion.getCurrentSong() != null) {
            try {
                String filePath = this.soundFusion.getCurrentSong().getPath();
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(filePath);
                byte[] albumArt = retriever.getEmbeddedPicture();
                if (albumArt != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                    this.albumButton.setImageBitmap(bitmap);
                    this.albumButton.setBackgroundResource(R.drawable.bg_rounded_album_ripple);
                    this.albumButton.setImageTintList(null);
                } else {
                    this.albumButton.setImageResource(R.drawable.ic_album);
                    this.albumButton.setBackgroundResource(R.drawable.bg_rounded_album_ripple);
                    this.albumButton.setImageTintList(ColorStateList.valueOf(-1));
                }
                retriever.release();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                setDefaultAlbumArt();
                return;
            }
        }
        setDefaultAlbumArt();
    }

    private void setDefaultAlbumArt() {
        this.albumButton.setImageResource(R.drawable.ic_album);
        this.albumButton.setImageTintList(ColorStateList.valueOf(-1));
    }

    public void syncUIState() {
        if (this.soundFusion != null && this.soundFusion.getCurrentSong() != null) {
            updateMusicData();
            if (this.bottomSheetFragment != null && this.bottomSheetFragment.isVisible()) {
                this.bottomSheetFragment.updateUI(this.soundFusion.getCurrentSong(), this.soundFusion.isPlaying(), this.soundFusion.getCurrentPosition(), this.soundFusion.getDuration());
            }
        }
    }

    public void updateMusicData() {
        if (this.soundFusion != null && this.soundFusion.getCurrentSong() != null) {
            if (this.isFirstPlay) {
                expandView(this.bottomLayout);
                this.isExpanded = true;
                this.isFirstPlay = false;
            }
            this.songTitleTextView.setText(this.soundFusion.getCurrentSong().getTitle());
            this.songArtistTextView.setText(this.soundFusion.getCurrentSong().getArtist());
            updatePlayPauseButton(this.soundFusion.isPlaying());
            updateRepeatButton(this.soundFusion.getRepeatMode());
            updateAlbumArtwork();
            int currentPosition = this.soundFusion.getCurrentPosition();
            int duration = this.soundFusion.getDuration();
            this.seekBar.setMax(duration);
            this.seekBar.setProgress(currentPosition);
            this.songCurrentDuration.setText(formatTime(currentPosition));
            this.songDuration.setText(formatTime(duration));
            this.notificationManager.createNotification(this.soundFusion.getCurrentSong().getTitle(), this.soundFusion.getCurrentSong().getArtist(), this.soundFusion.isPlaying(), this.soundFusion.getCurrentSong(), this.soundFusion.getCurrentPosition(), this.soundFusion.getDuration());
        }
    }

    @Override // com.xoeris.system.core.module.media.ux.audio.SoundFusion.OnMusicPlayerListener
    public void onSongChanged(String title, String artist) {
        runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.activities.HomeActivity$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                HomeActivity.this.updateSongInfo();
            }
        });
    }

    private void updateSongInfo() {
        if (this.bottomLayout.getVisibility() != View.VISIBLE) {
            expandView(this.bottomLayout);
            this.isExpanded = true;
        }
        if (this.soundFusion != null && this.soundFusion.getCurrentSong() != null) {
            // Update song title and artist immediately
            this.songTitleTextView.setText(this.soundFusion.getCurrentSong().getTitle());
            this.songArtistTextView.setText(this.soundFusion.getCurrentSong().getArtist());
            updatePlayPauseButton(this.soundFusion.isPlaying());
            updateAlbumArtwork();
            int currentPosition = this.soundFusion.getCurrentPosition();
            int duration = this.soundFusion.getDuration();
            this.seekBar.setMax(duration);
            this.seekBar.setProgress(currentPosition);
            this.songCurrentDuration.setText(formatTime(currentPosition));
            this.songDuration.setText(formatTime(duration));
            this.notificationManager.createNotification(
                this.soundFusion.getCurrentSong().getTitle(),
                this.soundFusion.getCurrentSong().getArtist(),
                this.soundFusion.isPlaying(),
                this.soundFusion.getCurrentSong(),
                currentPosition,
                duration
            );
        }
        if (this.bottomSheetFragment != null && this.bottomSheetFragment.isVisible()) {
            this.bottomSheetFragment.updateUI(
                this.soundFusion.getCurrentSong(),
                this.soundFusion.isPlaying(),
                this.soundFusion.getCurrentPosition(),
                this.soundFusion.getDuration()
            );
        }
    }

    @Override // com.xoeris.system.core.module.media.ux.audio.SoundFusion.OnMusicPlayerListener
    public void onPlaybackStateChanged(final boolean isPlaying) {
        runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.activities.HomeActivity$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                HomeActivity.this.m208x413a4dd6(isPlaying);
            }
        });
    }

    /* renamed from: lambda$onPlaybackStateChanged$3$com-xoeris-app-musify-activities-HomeActivity, reason: not valid java name */
    /* synthetic */ void m208x413a4dd6(boolean isPlaying) {
        updatePlayPauseButton(isPlaying);
        if (this.soundFusion != null && this.soundFusion.getCurrentSong() != null) {
            this.notificationManager.updateNotification(this.soundFusion.getCurrentSong().getTitle(), this.soundFusion.getCurrentSong().getArtist(), isPlaying, this.soundFusion.getCurrentSong(), this.soundFusion.getCurrentPosition(), this.soundFusion.getDuration());
        }
        if (isPlaying) {
            this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
            this.seekBarHandler.post(this.seekBarRunnable);
        } else {
            this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
        }
        if (this.bottomSheetFragment != null && this.bottomSheetFragment.isVisible()) {
            this.bottomSheetFragment.updateUI(this.soundFusion.getCurrentSong(), isPlaying, this.soundFusion.getCurrentPosition(), this.soundFusion.getDuration());
        }
    }

    @Override // com.xoeris.system.core.module.media.ux.audio.SoundFusion.OnMusicPlayerListener
    public void onProgressChanged(final int progress, final int duration) {
        runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.activities.HomeActivity$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                HomeActivity.this.m209xacc2f5c8(progress, duration);
            }
        });
    }

    /* renamed from: lambda$onProgressChanged$4$com-xoeris-app-musify-activities-HomeActivity, reason: not valid java name */
    /* synthetic */ void m209xacc2f5c8(int progress, int duration) {
        this.songCurrentDuration.setText(formatTime(progress));
        this.songDuration.setText(formatTime(duration));
        this.seekBar.setMax(duration);
        this.seekBar.setProgress(progress);
        updateMusicData();
        updateAlbumArtwork();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        if (this.soundFusion != null) {
            this.soundFusion.setListener(this);
            updateMusicData();
            if (this.soundFusion.isPlaying()) {
                this.seekBarHandler.post(this.seekBarRunnable);
            }
        }
    }

    public interface BottomSheetDismissListener {
        default void onBottomSheetDismissed() {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showNowPlayingBottomSheet() {
        if (this.bottomSheetFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(this.bottomSheetFragment).commitAllowingStateLoss();
        }
        this.bottomSheetFragment = new DialogSheet();
        this.bottomSheetFragment.setDismissListener(new AnonymousClass12());
        if (this.soundFusion != null && this.soundFusion.getCurrentSong() != null) {
            this.bottomSheetFragment.setInitialData(this.soundFusion.getCurrentSong(), this.soundFusion.isPlaying(), this.soundFusion.getCurrentPosition(), this.soundFusion.getDuration());
        }
        updateMusicData();
        this.bottomSheetFragment.show(getSupportFragmentManager(), this.bottomSheetFragment.getTag());
    }

    /* renamed from: com.xoeris.app.musify.activities.HomeActivity$12, reason: invalid class name */
    class AnonymousClass12 implements BottomSheetDismissListener {
        AnonymousClass12() {
        }

        @Override // com.xoeris.app.musify.activities.HomeActivity.BottomSheetDismissListener
        public void onBottomSheetDismissed() {
            HomeActivity.this.runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.activities.HomeActivity$12$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    HomeActivity.AnonymousClass12.this.m214x5349117b();
                }
            });
        }

        /* renamed from: lambda$onBottomSheetDismissed$0$com-xoeris-app-musify-activities-HomeActivity$12, reason: not valid java name */
        /* synthetic */ void m214x5349117b() {
            if (HomeActivity.this.soundFusion != null && HomeActivity.this.soundFusion.getCurrentSong() != null) {
                HomeActivity.this.songTitleTextView.setText(HomeActivity.this.soundFusion.getCurrentSong().getTitle());
                HomeActivity.this.songArtistTextView.setText(HomeActivity.this.soundFusion.getCurrentSong().getArtist());
                HomeActivity.this.updateMusicData();
            }
            HomeActivity.this.bottomSheetFragment = null;
        }
    }

    @Override // com.xoeris.app.musify.activities.BaseActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override // com.xoeris.system.core.module.media.ux.audio.SoundFusion.OnMusicPlayerListener
    public void onTaskRemoved(Intent rootIntent) {
        if (this.soundFusion != null) {
            this.soundFusion.release();
        }
        if (this.notificationManager != null) {
            this.notificationManager.cancelNotification();
        }
        if (this.seekBarHandler != null) {
            this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
        }
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putBoolean(KEY_SHUFFLE_STATE, this.soundFusion.isShuffleEnabled());
        editor.putInt(KEY_REPEAT_MODE, this.soundFusion.getRepeatMode());
        editor.apply();
        this.notificationManager.cancelNotification();
        stopService(new Intent(this, (Class<?>) SoundFusionService.class));
    }

    private void setupSeekBarUpdater() {
        this.seekBarHandler = new Handler();
        this.seekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (HomeActivity.this.soundFusion != null && HomeActivity.this.soundFusion.isPlaying()) {
                    int currentPosition = HomeActivity.this.soundFusion.getCurrentPosition();
                    int duration = HomeActivity.this.soundFusion.getDuration();
                    HomeActivity.this.seekBar.setMax(duration);
                    HomeActivity.this.seekBar.setProgress(currentPosition);
                    HomeActivity.this.songCurrentDuration.setText(HomeActivity.this.formatTime(currentPosition));
                    HomeActivity.this.songDuration.setText(HomeActivity.this.formatTime(duration));
                    
                    // Update MediaSession playback state
                    if (HomeActivity.this.notificationManager != null) {
                        HomeActivity.this.notificationManager.updateMediaSessionPlaybackState(
                            currentPosition,
                            HomeActivity.this.soundFusion.isPlaying()
                        );
                    }
                }
                HomeActivity.this.seekBarHandler.postDelayed(this, 10L);
            }
        };
        this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
        this.seekBarHandler.post(this.seekBarRunnable);
    }

    /* JADX INFO: Access modifiers changed from: private */

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRepeatButton(int repeatMode) {
        int iconRes;
        switch (repeatMode) {
            case 1:
                iconRes = R.drawable.ic_repeat;
                break;
            case 2:
                iconRes = R.drawable.ic_repeat_one;
                break;
            default:
                iconRes = R.drawable.ic_repeat_off;
                break;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePlayPauseButton(boolean isPlaying) {
        this.playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        if (this.seekBarHandler != null) {
            this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
        }
        if (this.soundFusion != null) {
            this.soundFusion.setListener(null);
            if (this.seekBarHandler != null) {
                this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
            }
        }
    }

    @Override // com.xoeris.app.musify.activities.BaseActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putBoolean(KEY_SHUFFLE_STATE, this.soundFusion.isShuffleEnabled());
        editor.putInt(KEY_REPEAT_MODE, this.soundFusion.getRepeatMode());
        editor.apply();
        if (isFinishing()) {
            if (this.seekBarHandler != null) {
                this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
            }
            if (this.soundFusion != null) {
                this.soundFusion.release();
            }
            if (this.notificationManager != null) {
                this.notificationManager.cancelNotification();
            }
            stopService(new Intent(this, (Class<?>) SoundFusionService.class));
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / 60000) % 60;
        int ms = (milliseconds % 1000) / 10;
        return String.format("%02d:%02d.%02d", Integer.valueOf(minutes), Integer.valueOf(seconds), Integer.valueOf(ms));
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") != 0) {
            permissionsNeeded.add("android.permission.POST_NOTIFICATIONS");
        }
        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            permissionsNeeded.add("android.permission.READ_EXTERNAL_STORAGE");
        }
        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_AUDIO") != 0) {
            permissionsNeeded.add("android.permission.READ_MEDIA_AUDIO");
        }
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, (String[]) permissionsNeeded.toArray(new String[0]), 100);
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int result : grantResults) {
                if (result != 0) {
                    return;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void expandView(final View view) {
        view.setVisibility(0);
        int targetHeight = measureViewHeight(view);
        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.xoeris.app.musify.activities.HomeActivity$$ExternalSyntheticLambda5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                HomeActivity.lambda$expandView$5(view, valueAnimator);
            }
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300L);
        animator.start();
    }

    static /* synthetic */ void lambda$expandView$5(View view, ValueAnimator animation) {
        view.getLayoutParams().height = ((Integer) animation.getAnimatedValue()).intValue();
        view.requestLayout();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void collapseView(final View view) {
        int initialHeight = view.getMeasuredHeight();
        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.xoeris.app.musify.activities.HomeActivity$$ExternalSyntheticLambda2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                HomeActivity.lambda$collapseView$6(view, valueAnimator);
            }
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300L);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() { // from class: com.xoeris.app.musify.activities.HomeActivity.14
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(8);
            }
        });
    }

    static /* synthetic */ void lambda$collapseView$6(View view, ValueAnimator animation) {
        view.getLayoutParams().height = ((Integer) animation.getAnimatedValue()).intValue();
        view.requestLayout();
    }

    private int measureViewHeight(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
        return view.getMeasuredHeight();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIconAndTint(ImageView icon, int iconRes) {
        icon.setImageResource(iconRes);
        icon.setColorFilter(getResources().getColor(R.color.blue));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetIconAndTint(ImageView icon, int iconRes) {
        icon.setImageResource(iconRes);
        icon.setColorFilter(getResources().getColor(R.color.text));
    }
}