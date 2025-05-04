package com.xoeris.android.xesc.system.core.module.media.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.xoeris.android.musify.R;
import com.xoeris.android.musify.app.activity.HomeActivity;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SongByte;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SoundFusion;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.adapter.SongByteAdapter;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("all")
public class DialogSheet extends BottomSheetDialogFragment implements SoundFusion.OnMusicPlayerListener {
    private ImageView albumArtContainer;
    private BottomSheetCallback callback;
    private int currentPosition;
    private SongByte currentSongByte;
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
    private SoundFusion soundFusion;
    private RecyclerView queueRecyclerView;
    private SongByteAdapter queueAdapter;
    private List<SongByte> queueList = new ArrayList<>();

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

    public void setInitialData(SongByte songByte, boolean isPlaying, int currentPosition, int duration) {
        this.currentSongByte = songByte;
        this.isPlaying = isPlaying;
        this.currentPosition = currentPosition;
        this.duration = duration;
    }

    public void updateUI(final SongByte songByte, final boolean isPlaying, final int currentPosition, final int duration) {
        if (getView() != null) {
            requireActivity().runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    DialogSheet.this.m236x1a75c9fe(songByte, currentPosition, duration, isPlaying);
                }
            });
        }
    }

    /* renamed from: lambda$updateUI$0$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m236x1a75c9fe(SongByte songByte, int currentPosition, int duration, boolean isPlaying) {
        this.songTitleTextView.setText(songByte.getTitle());
        this.songArtistTextView.setText(songByte.getArtist());
        this.songCurrentDuration.setText(formatTime(currentPosition));
        this.songDuration.setText(formatTime(duration));
        this.seekBar.setMax(duration);
        this.seekBar.setProgress(currentPosition);
        updatePlayPauseButton();
        updateShuffleButton();
        updateRepeatButton();
        this.currentSongByte = songByte;
        this.isPlaying = isPlaying;
        this.currentPosition = currentPosition;
        this.duration = duration;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(songByte.getPath());
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
                    DialogSheet.this.m226xba018fc8(title, artist);
                }
            });
        }
    }

    /* renamed from: lambda$onSongChanged$1$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m226xba018fc8(String title, String artist) {
        this.songTitleTextView.setText(title);
        this.songArtistTextView.setText(artist);
        updatePlayPauseButton();
        if (this.soundFusion != null && this.soundFusion.getCurrentSong() != null) {
            updateAlbumArtwork(this.soundFusion.getCurrentSong());
        }
        updateUI();
        updateAlbumArtwork(this.soundFusion.getCurrentSong());
        this.homeActivity.updateMusicData();
        this.homeActivity.updateAlbumArtwork();
    }

    public void updateAlbumArtwork(SongByte songByte) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(songByte.getPath());
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                this.albumArtContainer.setImageBitmap(bitmap);
                this.albumArtContainer.setImageTintList(null);
                this.albumArtContainer.post(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        DialogSheet.this.m232xd135a6d1();
                    }
                });
            } else {
                this.albumArtContainer.setImageResource(R.drawable.ic_album);
                this.albumArtContainer.setImageTintList(ColorStateList.valueOf(-7829368));
                this.albumArtContainer.post(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        DialogSheet.this.m233x5e705852();
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
                    DialogSheet.this.m234xebab09d3();
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
        if (this.soundFusion != null && this.soundFusion.getCurrentSong() != null && isAdded()) {
            requireActivity().runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    DialogSheet.this.m237xdc9b4183();
                }
            });
        }
    }

    /* renamed from: lambda$updateUI$5$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m237xdc9b4183() {
        int pos = this.currentPosition > 0 ? this.currentPosition : this.soundFusion.getCurrentPosition();
        int dur = this.duration > 0 ? this.duration : this.soundFusion.getDuration();
        this.songTitleTextView.setText(this.soundFusion.getCurrentSong().getTitle());
        this.songArtistTextView.setText(this.soundFusion.getCurrentSong().getArtist());
        this.songCurrentDuration.setText(formatTime(pos));
        this.songDuration.setText(formatTime(dur));
        this.songArtistTextView.setSelected(true);
        this.songTitleTextView.setSelected(true);
        updatePlayPauseButton();
        updateShuffleButton();
        updateRepeatButton();
        updateAlbumArtwork(this.soundFusion.getCurrentSong());
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
        this.soundFusion = SoundFusion.getInstance(getContext());
        this.soundFusion.setListener(this);
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
                DialogSheet.this.m224xe58e6027(view2);
            }
        });
        return view;
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
        if (this.soundFusion != null) {
            this.soundFusion.setListener(null);
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
                if (DialogSheet.this.soundFusion != null && DialogSheet.this.soundFusion.isPlaying()) {
                    int currentPosition = DialogSheet.this.soundFusion.getCurrentPosition();
                    int duration = DialogSheet.this.soundFusion.getDuration();
                    DialogSheet.this.updateProgress(currentPosition, duration);
                }
                DialogSheet.this.seekBarHandler.postDelayed(this, 10L);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateProgress(final int progress, final int duration) {
        if (isAdded()) {
            requireActivity().runOnUiThread(new Runnable() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    DialogSheet.this.m235x4575cefe(progress, duration);
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
                    DialogSheet.this.m225xc56d1946(isPlaying);
                    DialogSheet.this.updatePlayPauseButton();
                }
            });
        }
    }

    /* renamed from: lambda$onPlaybackStateChanged$8$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m225xc56d1946(boolean isPlaying) {
        this.isPlaying = isPlaying;
        updatePlayPauseButton();
        updateUI();
        updateAlbumArtwork(this.soundFusion.getCurrentSong());
        this.homeActivity.updateMusicData();
        this.homeActivity.updateAlbumArtwork();
        if (isPlaying) {
            this.seekBarHandler.post(this.seekBarRunnable);
            return;
        }
        this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
        if (this.soundFusion != null) {
            updateProgress(this.soundFusion.getCurrentPosition(), this.soundFusion.getDuration());
        }
    }

    @Override // com.xoeris.system.core.module.media.ux.audio.SoundFusion.OnMusicPlayerListener
    public void onProgressChanged(int progress, int duration) {
        updateProgress(progress, duration);
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        this.soundFusion.setListener(this);
        if (this.soundFusion.isPlaying()) {
            this.seekBarHandler.post(this.seekBarRunnable);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        this.seekBarHandler.removeCallbacks(this.seekBarRunnable);
        this.soundFusion.setListener(null);
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
                DialogSheet.this.m231xa570072c(view);
            }
        });
        this.skipPreviousButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                DialogSheet.this.m227x9f71d9bc(view);
            }
        });
        this.skipNextButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda11
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                DialogSheet.this.m228x2cac8b3d(view);
            }
        });
        this.shuffleButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda12
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                DialogSheet.this.m229xb9e73cbe(view);
            }
        });
        this.repeatButton.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda13
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                DialogSheet.this.m230x4721ee3f(view);
            }
        });
        this.seekBar.setOnSeekBarChangeListener(new VortexSlider.OnSeekBarChangeListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet.2
            @Override
            // com.xoeris.system.core.module.media.ui.view.VortexSlider.OnSeekBarChangeListener
            public void onProgressChanged(VortexSlider vortexSlider, int progress, boolean fromUser) {
                if (fromUser) {
                    DialogSheet.this.soundFusion.seekTo(progress);
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
        this.soundFusion.playPause();
        updatePlayPauseButton();
        if (this.musicStateListener != null) {
            this.musicStateListener.onUIRequiresUpdate();
        }
    }

    /* renamed from: lambda$setupListeners$10$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m227x9f71d9bc(View v) {
        this.soundFusion.playPrevious();
        updatePlayPauseButton();
        if (this.musicStateListener != null) {
            this.musicStateListener.onUIRequiresUpdate();
        }
    }

    /* renamed from: lambda$setupListeners$11$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m228x2cac8b3d(View v) {
        this.soundFusion.playNext();
        updatePlayPauseButton();
        if (this.musicStateListener != null) {
            this.musicStateListener.onUIRequiresUpdate();
        }
    }

    /* renamed from: lambda$setupListeners$12$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m229xb9e73cbe(View v) {
        this.soundFusion.toggleShuffle();
        updateShuffleButton();
        saveShuffleRepeatState();
        if (this.musicStateListener != null) {
            this.musicStateListener.onUIRequiresUpdate();
        }
    }

    /* renamed from: lambda$setupListeners$13$com-xoeris-app-musify-module-media-view-DialogSheet, reason: not valid java name */
    /* synthetic */ void m230x4721ee3f(View v) {
        this.soundFusion.toggleRepeat();
        updateRepeatButton();
        saveShuffleRepeatState();
        if (this.musicStateListener != null) {
            this.musicStateListener.onUIRequiresUpdate();
        }
    }

    private void updatePlayPauseButton() {
        this.playPauseButton.setImageResource(this.soundFusion.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
    }

    private void updateShuffleButton() {
        this.shuffleButton.setImageResource(this.soundFusion.isShuffleEnabled() ? R.drawable.ic_shuffle : R.drawable.ic_shuffle_disabled);
    }

    private void updateRepeatButton() {
        int iconRes;
        switch (this.soundFusion.getRepeatMode()) {
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
    // com.google.android.material.bottomsheet.BottomSheetDialogFragment, androidx.appcompat.app.AppCompatDialogFragment, androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.xoeris.app.musify.module.media.view.DialogSheet$$ExternalSyntheticLambda14
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                DialogSheet.this.m223x46d209ff(dialogInterface);
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
        editor.putBoolean("shuffle_state", this.soundFusion.isShuffleEnabled());
        editor.putInt("repeat_mode", this.soundFusion.getRepeatMode());
        editor.apply();
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("music_player_prefs", 0);
            boolean shuffleState = prefs.getBoolean("shuffle_state", false);
            int repeatMode = prefs.getInt("repeat_mode", 0);
            if (this.soundFusion != null) {
                this.soundFusion.setShuffleEnabled(shuffleState);
                this.soundFusion.setRepeatMode(repeatMode);
            }
        }
    }
}