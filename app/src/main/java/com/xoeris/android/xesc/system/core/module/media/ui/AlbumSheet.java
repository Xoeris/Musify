package com.xoeris.android.xesc.system.core.module.media.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.xoeris.android.musify.R;
import com.xoeris.android.musify.app.activity.HomeActivity;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.HyperSound;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.adapter.UltraSongAdapter;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("all")
public class AlbumSheet extends BottomSheetDialogFragment implements HyperSound.OnMusicPlayerListener {
    private ImageView albumArtContainer;
    private BottomSheetCallback callback;
    private int currentPosition;
    private com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong currentUltraSong;
    private HomeActivity.BottomSheetDismissListener dismissListener;
    private int duration;
    private HomeActivity homeActivity;
    private boolean isPlaying;
    private MusicStateListener musicStateListener;
    private DialogInterface.OnShowListener onShowListener;
    private ImageView playPauseButton;
    private ImageView repeatButton;
    private VortexSlider seekBar;
    private Handler seekBarHandler;
    private Runnable seekBarRunnable;
    private ImageView shuffleButton;
    private ImageView skipNextButton;
    private ImageView skipPreviousButton;
    private TextView songArtistTextView;
    private TextView songCurrentDuration;
    private TextView songDuration;
    private TextView songTitleTextView;
    private HyperSound hyperSound;
    private RecyclerView queueRecyclerView;
    private UltraSongAdapter queueAdapter;
    private List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> queueList = new ArrayList<>();

    public interface BottomSheetCallback {
        void onNextClicked();

        void onPlayPauseClicked(boolean z);

        void onPlaybackStateChanged(boolean z);

        void onPreviousClicked();

        void onRepeatClicked(int i);

        void onSeekBarChanged(int i);

        void onShuffleClicked(boolean z);

        void onSongChanged(String str, String str2);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        // 👇 MUST be before super.onCreate()
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheet); // Set your custom style here

        super.onCreate(savedInstanceState);

        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("music_player_prefs", 0);
            boolean shuffleState = prefs.getBoolean("shuffle_state", false);
            int repeatMode = prefs.getInt("repeat_mode", 0);
            if (this.hyperSound != null) {
                this.hyperSound.setShuffleEnabled(shuffleState);
                this.hyperSound.setRepeatMode(repeatMode);
            }
        }
    }

    public interface MusicStateListener {
        void onUIRequiresUpdate();
    }

    public void setMusicStateListener(MusicStateListener listener) {
        this.musicStateListener = listener;
    }

    public void setDismissListener(HomeActivity.BottomSheetDismissListener listener) {
        this.dismissListener = listener;
    }

    @Override
    // androidx.fragment.app.DialogFragment, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (this.dismissListener != null) {
            this.dismissListener.onBottomSheetDismissed();
        }
    }

    public void setInitialData(com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong, boolean isPlaying, int currentPosition, int duration) {
        this.currentUltraSong = ultraSong;
        this.isPlaying = isPlaying;
        this.currentPosition = currentPosition;
        this.duration = duration;
    }

    public void updateUI(final com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong, final boolean isPlaying, final int currentPosition, final int duration) {
        if (getView() != null) {
            requireActivity().runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    AlbumSheet.this.m236x1a75c9fe(ultraSong, currentPosition, duration, isPlaying);
                }
            });
        }
    }

    /* renamed from: lambda$updateUI$0$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m236x1a75c9fe(com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong, int currentPosition, int duration, boolean isPlaying) {
        this.songTitleTextView.setText(ultraSong.getTitle());
        this.songArtistTextView.setText(ultraSong.getArtist());
        this.songCurrentDuration.setText(formatTime(currentPosition));
        this.songDuration.setText(formatTime(duration));
        this.seekBar.setMax(duration);
        this.seekBar.setProgress(currentPosition);
        updatePlayPauseButton();
        updateShuffleButton();
        updateRepeatButton();
        this.currentUltraSong = ultraSong;
        this.isPlaying = isPlaying;
        this.currentPosition = currentPosition;
        this.duration = duration;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(ultraSong.getPath());
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                this.albumArtContainer.setImageBitmap(bitmap);
                this.albumArtContainer.setImageTintList(null);
            } else {
                this.albumArtContainer.setImageResource(R.drawable.ic_album);
                this.albumArtContainer.setImageTintList(ColorStateList.valueOf(-7829368));
            }
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
            this.albumArtContainer.setImageResource(R.drawable.ic_album);
            this.albumArtContainer.setImageTintList(ColorStateList.valueOf(-7829368));
        }
    }

    public void setCallback(BottomSheetCallback callback) {
        this.callback = callback;
    }

    @Override // com.xoeris.system.core.module.media.ux.audio.SoundFusion.OnMusicPlayerListener
    public void onSongChanged(final String title, final String artist) {
        if (isAdded()) {
            requireActivity().runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda7
                @Override // java.lang.Runnable
                public final void run() {
                    AlbumSheet.this.m226xba018fc8(title, artist);
                }
            });
        }
    }

    /* renamed from: lambda$onSongChanged$1$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m226xba018fc8(String title, String artist) {
        this.songTitleTextView.setText(title);
        this.songArtistTextView.setText(artist);
        updatePlayPauseButton();
        if (this.hyperSound != null && this.hyperSound.getCurrentSong() != null) {
            updateAlbumArtwork(this.hyperSound.getCurrentSong());
        }
        updateUI();
        updateAlbumArtwork(this.hyperSound.getCurrentSong());
        this.homeActivity.updateMusicData();
        this.homeActivity.updateAlbumArtwork();
    }

    public void updateAlbumArtwork(com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(ultraSong.getPath());
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                this.albumArtContainer.setImageBitmap(bitmap);
                this.albumArtContainer.setImageTintList(null);
                this.albumArtContainer.post(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        AlbumSheet.this.m232xd135a6d1();
                    }
                });
            } else {
                this.albumArtContainer.setImageResource(R.drawable.ic_album);
                this.albumArtContainer.setImageTintList(ColorStateList.valueOf(-7829368));
                this.albumArtContainer.post(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        AlbumSheet.this.m233x5e705852();
                    }
                });
            }
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
            this.albumArtContainer.setImageResource(R.drawable.ic_album);
            this.albumArtContainer.setImageTintList(ColorStateList.valueOf(-7829368));
            this.albumArtContainer.post(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    AlbumSheet.this.m234xebab09d3();
                }
            });
        }
    }

    /* renamed from: lambda$updateAlbumArtwork$2$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m232xd135a6d1() {
        ViewGroup.LayoutParams params = this.albumArtContainer.getLayoutParams();
        params.height = this.albumArtContainer.getWidth();
        this.albumArtContainer.setLayoutParams(params);
    }

    /* renamed from: lambda$updateAlbumArtwork$3$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m233x5e705852() {
        ViewGroup.LayoutParams params = this.albumArtContainer.getLayoutParams();
        params.height = this.albumArtContainer.getWidth();
        this.albumArtContainer.setLayoutParams(params);
    }

    /* renamed from: lambda$updateAlbumArtwork$4$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m234xebab09d3() {
        ViewGroup.LayoutParams params = this.albumArtContainer.getLayoutParams();
        params.height = this.albumArtContainer.getWidth();
        this.albumArtContainer.setLayoutParams(params);
    }

    public void updateUI() {
        if (this.hyperSound != null && this.hyperSound.getCurrentSong() != null && isAdded()) {
            requireActivity().runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    AlbumSheet.this.m237xdc9b4183();
                }
            });
        }
    }

    /* renamed from: lambda$updateUI$5$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m237xdc9b4183() {
        int pos = this.currentPosition > 0 ? this.currentPosition : this.hyperSound.getCurrentPosition();
        int dur = this.duration > 0 ? this.duration : this.hyperSound.getDuration();
        this.songTitleTextView.setText(this.hyperSound.getCurrentSong().getTitle());
        this.songArtistTextView.setText(this.hyperSound.getCurrentSong().getArtist());
        this.songCurrentDuration.setText(formatTime(pos));
        this.songDuration.setText(formatTime(dur));
        this.songArtistTextView.setSelected(true);
        this.songTitleTextView.setSelected(true);
        updatePlayPauseButton();
        updateShuffleButton();
        updateRepeatButton();
        updateAlbumArtwork(this.hyperSound.getCurrentSong());
        this.homeActivity.updateMusicData();
        this.homeActivity.updateAlbumArtwork();
        this.seekBar.setMax(dur);
        this.seekBar.setProgress(pos);
    }

    public void setOnShowListener(DialogInterface.OnShowListener listener) {
        this.onShowListener = listener;
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_album, container, false);
        this.albumArtContainer = (ImageView) view.findViewById(R.id.album_art_container);
        this.homeActivity = (HomeActivity) getActivity();
        this.hyperSound = HyperSound.getInstance(getContext());
        this.hyperSound.setListener(this);
        setupSeekBarUpdater();
        View playerControls = view.findViewById(R.id.player_controls);
        initializeViews(playerControls);
        setupListeners();
        updateUI();
        this.homeActivity.updateMusicData();
        this.homeActivity.updateAlbumArtwork();
        ImageView collapseButton = (ImageView) view.findViewById(R.id.collapse_button);
        collapseButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                AlbumSheet.this.m224xe58e6027(view2);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();

            // Make the status bar transparent
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);

            // Make the navigation bar transparent
            window.setNavigationBarColor(Color.TRANSPARENT); // Transparent navigation bar

            // Allow layout to draw behind status bar and navigation bar
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            // Make the bottom sheet full height
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }

    /* renamed from: lambda$onCreateView$6$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m224xe58e6027(View v) {
        dismiss();
    }

    @Override // com.xoeris.system.core.module.media.ux.audio.SoundFusion.OnMusicPlayerListener
    public void onTaskRemoved(Intent rootIntent) {
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        if (this.hyperSound != null) {
            this.hyperSound.setListener(null);
        }
        if (this.seekBarHandler != null) {
            this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
            this.seekBarHandler = null;
        }
    }

    private void setupSeekBarUpdater() {
        this.seekBarHandler = new Handler();
        this.seekBarRunnable = new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet.1
            @Override // java.lang.Runnable
            public void run() {
                if (AlbumSheet.this.hyperSound != null && AlbumSheet.this.hyperSound.isPlaying()) {
                    int currentPosition = AlbumSheet.this.hyperSound.getCurrentPosition();
                    int duration = AlbumSheet.this.hyperSound.getDuration();
                    AlbumSheet.this.updateProgress(currentPosition, duration);
                }
                AlbumSheet.this.seekBarHandler.postDelayed(this, 10L);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateProgress(final int progress, final int duration) {
        if (isAdded()) {
            requireActivity().runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    AlbumSheet.this.m235x4575cefe(progress, duration);
                }
            });
        }
    }

    /* renamed from: lambda$updateProgress$7$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m235x4575cefe(int progress, int duration) {
        this.songCurrentDuration.setText(formatTime(progress));
        this.songDuration.setText(formatTime(duration));
        this.seekBar.setMax(duration);
        this.seekBar.setProgress(progress);
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / 60000) % 60;
        int ms = (milliseconds % 1000) / 10;
        return String.format("%02d:%02d.%02d", Integer.valueOf(minutes), Integer.valueOf(seconds), Integer.valueOf(ms));
    }

    @Override // com.xoeris.system.core.module.media.ux.audio.SoundFusion.OnMusicPlayerListener
    public void onPlaybackStateChanged(final boolean isPlaying) {
        if (isAdded()) {
            requireActivity().runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    AlbumSheet.this.m225xc56d1946(isPlaying);
                    AlbumSheet.this.updatePlayPauseButton();
                }
            });
        }
    }

    /* renamed from: lambda$onPlaybackStateChanged$8$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m225xc56d1946(boolean isPlaying) {
        this.isPlaying = isPlaying;
        updatePlayPauseButton();
        updateUI();
        updateAlbumArtwork(this.hyperSound.getCurrentSong());
        this.homeActivity.updateMusicData();
        this.homeActivity.updateAlbumArtwork();
        if (isPlaying) {
            this.seekBarHandler.post(this.seekBarRunnable);
            return;
        }
        this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
        if (this.hyperSound != null) {
            updateProgress(this.hyperSound.getCurrentPosition(), this.hyperSound.getDuration());
        }
    }

    @Override // com.xoeris.system.core.module.media.ux.audio.SoundFusion.OnMusicPlayerListener
    public void onProgressChanged(int progress, int duration) {
        updateProgress(progress, duration);
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        this.hyperSound.setListener(this);
        if (this.hyperSound.isPlaying()) {
            this.seekBarHandler.post(this.seekBarRunnable);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
        this.hyperSound.setListener(null);
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        if (this.seekBarHandler != null) {
            this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
        }
    }

    private void initializeViews(View playerControls) {
        this.playPauseButton = (ImageView) playerControls.findViewById(R.id.play_pause_icon);
        this.skipPreviousButton = (ImageView) playerControls.findViewById(R.id.skip_previous_icon);
        this.skipNextButton = (ImageView) playerControls.findViewById(R.id.skip_next_icon);
        this.shuffleButton = (ImageView) playerControls.findViewById(R.id.shuffle_icon);
        this.repeatButton = (ImageView) playerControls.findViewById(R.id.repeater_icon);
        this.songTitleTextView = (TextView) playerControls.findViewById(R.id.song_title);
        this.songArtistTextView = (TextView) playerControls.findViewById(R.id.song_artist);
        this.seekBar = (VortexSlider) playerControls.findViewById(R.id.vortexSlider);
        this.songCurrentDuration = (TextView) playerControls.findViewById(R.id.song_current_duration);
        this.songDuration = (TextView) playerControls.findViewById(R.id.song_duration);
    }

    private void setupListeners() {
        this.playPauseButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AlbumSheet.this.m231xa570072c(view);
            }
        });
        this.skipPreviousButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AlbumSheet.this.m227x9f71d9bc(view);
            }
        });
        this.skipNextButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda11
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AlbumSheet.this.m228x2cac8b3d(view);
            }
        });
        this.shuffleButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda12
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AlbumSheet.this.m229xb9e73cbe(view);
            }
        });
        this.repeatButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda13
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AlbumSheet.this.m230x4721ee3f(view);
            }
        });
        this.seekBar.setOnSeekBarChangeListener(new VortexSlider.OnSeekBarChangeListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet.2
            @Override
            // com.xoeris.system.core.module.media.ui.view.VortexSlider.OnSeekBarChangeListener
            public void onProgressChanged(VortexSlider vortexSlider, int progress, boolean fromUser) {
                if (fromUser) {
                    AlbumSheet.this.hyperSound.seekTo(progress);
                }
            }

            @Override
            // com.xoeris.system.core.module.media.ui.view.VortexSlider.OnSeekBarChangeListener
            public void onStartTrackingTouch(VortexSlider vortexSlider) {
            }

            @Override
            // com.xoeris.system.core.module.media.ui.view.VortexSlider.OnSeekBarChangeListener
            public void onStopTrackingTouch(VortexSlider vortexSlider) {
            }
        });
    }

    /* renamed from: lambda$setupListeners$9$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m231xa570072c(View v) {
        this.hyperSound.playPause();
        updatePlayPauseButton();
        if (this.musicStateListener != null) {
            this.musicStateListener.onUIRequiresUpdate();
        }
    }

    /* renamed from: lambda$setupListeners$10$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m227x9f71d9bc(View v) {
        this.hyperSound.playPrevious();
        updatePlayPauseButton();
        if (this.musicStateListener != null) {
            this.musicStateListener.onUIRequiresUpdate();
        }
    }

    /* renamed from: lambda$setupListeners$11$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m228x2cac8b3d(View v) {
        this.hyperSound.playNext();
        updatePlayPauseButton();
        if (this.musicStateListener != null) {
            this.musicStateListener.onUIRequiresUpdate();
        }
    }

    /* renamed from: lambda$setupListeners$12$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m229xb9e73cbe(View v) {
        this.hyperSound.toggleShuffle();
        updateShuffleButton();
        saveShuffleRepeatState();
        if (this.musicStateListener != null) {
            this.musicStateListener.onUIRequiresUpdate();
        }
    }

    /* renamed from: lambda$setupListeners$13$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m230x4721ee3f(View v) {
        this.hyperSound.toggleRepeat();
        updateRepeatButton();
        saveShuffleRepeatState();
        if (this.musicStateListener != null) {
            this.musicStateListener.onUIRequiresUpdate();
        }
    }

    private void updatePlayPauseButton() {
        this.playPauseButton.setImageResource(this.hyperSound.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
    }

    private void updateShuffleButton() {
        this.shuffleButton.setImageResource(this.hyperSound.isShuffleEnabled() ? R.drawable.ic_shuffle : R.drawable.ic_shuffle_disabled);
    }

    private void updateRepeatButton() {
        int iconRes;
        switch (this.hyperSound.getRepeatMode()) {
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
        this.repeatButton.setImageResource(iconRes);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);

                // Expand it to full screen
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        return dialog;
    }


    /* renamed from: lambda$onCreateDialog$14$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m223x46d209ff(DialogInterface dialogInterface) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
        setupFullHeight(bottomSheetDialog);
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
            layoutParams.height = -1;
            bottomSheet.setLayoutParams(layoutParams);
            behavior.setState(3);
            behavior.setSkipCollapsed(true);
        }
    }

    private void saveShuffleRepeatState() {
        if (getContext() == null) return;
        android.content.SharedPreferences prefs = getContext().getSharedPreferences("music_player_prefs", 0);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("shuffle_state", this.hyperSound.isShuffleEnabled());
        editor.putInt("repeat_mode", this.hyperSound.getRepeatMode());
        editor.apply();
    }
}