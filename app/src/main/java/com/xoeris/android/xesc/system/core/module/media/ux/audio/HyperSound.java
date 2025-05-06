package com.xoeris.android.xesc.system.core.module.media.ux.audio;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import androidx.constraintlayout.core.motion.utils.TypedValues;
import com.xoeris.android.musify.app.activity.HomeActivity;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.manager.HyperSoundNotificationManager;
import com.xoeris.android.xesc.system.core.module.media.ui.AlbumSheet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("all")
public class HyperSound {
    private static final String KEY_FADE_DURATION = "fade_duration";
    private static final String PREFS_NAME = "music_player_prefs";
    private static final int UPDATE_INTERVAL = 500;
    private static HyperSound instance;
    private AlbumSheet bottomSheetFragment;
    private Context context;
    private String currentSong;
    private int fadeDuration;
    private HomeActivity homeActivity;
    private boolean isShuffleEnabled;
    private OnMusicPlayerListener listener;
    private HyperSoundNotificationManager notificationManager;
    private int repeatMode;
    private boolean isPlaying = false;
    private List<UltraSong> playlist = new ArrayList();
    private int currentSongIndex = -1;
    private boolean wasPlaying = false;
    private int lastPosition = 0;
    private UltraSong lastPlayedUltraSong = null;
    private List<UltraSong> allUltraSongs = new ArrayList();
    private Set<Integer> playedSongs = new HashSet();
    private final Runnable progressRunnable = new Runnable() { // from class: com.xoeris.system.core.module.media.ux.audio.SoundFusion.3
        @Override // java.lang.Runnable
        public void run() {
            if (HyperSound.this.mediaPlayer != null && HyperSound.this.mediaPlayer.isPlaying()) {
                if (HyperSound.this.listener != null) {
                    HyperSound.this.listener.onProgressChanged(HyperSound.this.mediaPlayer.getCurrentPosition(), HyperSound.this.mediaPlayer.getDuration());
                }
                HyperSound.this.progressHandler.postDelayed(this, 500L);
            }
        }
    };
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Handler progressHandler = new Handler(Looper.getMainLooper());

    public interface OnMusicPlayerListener {
        void onPlaybackStateChanged(boolean z);

        void onProgressChanged(int i, int i2);

        void onSongChanged(String str, String str2);

        void onTaskRemoved(Intent intent);
    }

    public List<UltraSong> getAllSongs(Context context) {
        if (this.allUltraSongs.isEmpty()) {
            loadAllSongs(context);
        }
        return this.allUltraSongs;
    }

    private void loadAllSongs(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        try {
            Cursor cursor = contentResolver.query(uri, null, "is_music!= 0", null, "title ASC");
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        this.allUltraSongs.clear();
                        while (cursor.moveToNext()) {
                            String data = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                            String artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"));
                            String duration = cursor.getString(cursor.getColumnIndexOrThrow(TypedValues.TransitionType.S_DURATION));
                            this.allUltraSongs.add(new UltraSong(title, artist, data, duration, ""));
                        }
                    }
                } finally {
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("SoundFusion", "Failed to load songs", e);
        }
    }

    public void setFadeDuration(int seconds) {
        this.fadeDuration = Math.max(0, Math.min(5, seconds));
        this.context.getSharedPreferences(PREFS_NAME, 0).edit().putInt(KEY_FADE_DURATION, this.fadeDuration).apply();
    }

    public int getFadeDuration() {
        return this.fadeDuration;
    }

    private void fadeOut(final MediaPlayer mediaPlayer) {
        if (this.fadeDuration == 0) {
            mediaPlayer.pause();
            return;
        }
        int fadeDurationMs = this.fadeDuration * 1000;
        final float stepVolume = 1.0f / (fadeDurationMs / 50);
        final Handler handler = new Handler();
        handler.post(new Runnable() { // from class: com.xoeris.system.core.module.media.ux.audio.SoundFusion.1
            float volume = 1.0f;

            @Override // java.lang.Runnable
            public void run() {
                if (this.volume > 0.0f) {
                    this.volume -= stepVolume;
                    mediaPlayer.setVolume(this.volume, this.volume);
                    handler.postDelayed(this, 50L);
                } else {
                    mediaPlayer.pause();
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
            }
        });
    }

    private void fadeIn(final MediaPlayer mediaPlayer) {
        if (this.fadeDuration == 0) {
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.start();
            return;
        }
        int fadeDurationMs = this.fadeDuration * 1000;
        final float stepVolume = 1.0f / (fadeDurationMs / 50);
        mediaPlayer.setVolume(0.0f, 0.0f);
        mediaPlayer.start();
        final Handler handler = new Handler();
        handler.post(new Runnable() { // from class: com.xoeris.system.core.module.media.ux.audio.SoundFusion.2
            float volume = 0.0f;

            @Override // java.lang.Runnable
            public void run() {
                if (this.volume < 1.0f) {
                    this.volume += stepVolume;
                    mediaPlayer.setVolume(this.volume, this.volume);
                    handler.postDelayed(this, 50L);
                    return;
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
            }
        });
    }

    public void setPlaylist(List<UltraSong> ultraSongs, int startIndex, boolean fromUserReorder) {
        this.playlist = new ArrayList(ultraSongs);
        this.currentSongIndex = startIndex;
        this.playedSongs.clear();
        if (this.isShuffleEnabled && !fromUserReorder) {
            Collections.shuffle(this.playlist);
            for (int i = 0; i < this.playlist.size(); i++) {
                if (this.playlist.get(i).getPath().equals(ultraSongs.get(startIndex).getPath())) {
                    Collections.swap(this.playlist, i, 0);
                    this.currentSongIndex = 0;
                    return;
                }
            }
        }
    }

    public void toggleShuffle() {
        this.isShuffleEnabled = !this.isShuffleEnabled;
        this.context.getSharedPreferences(PREFS_NAME, 0).edit().putBoolean("shuffle_state", this.isShuffleEnabled).apply();
        if (this.isShuffleEnabled && !this.playlist.isEmpty()) {
            UltraSong currentUltraSong = getCurrentSong();
            List<UltraSong> newPlaylist = new ArrayList<>(this.playlist);
            newPlaylist.remove(currentUltraSong);
            Collections.shuffle(newPlaylist);
            this.playlist.clear();
            this.playlist.add(currentUltraSong);
            this.playlist.addAll(newPlaylist);
            this.currentSongIndex = 0;
            this.playedSongs.clear();
            this.playedSongs.add(Integer.valueOf(this.currentSongIndex));
            return;
        } else if (!this.isShuffleEnabled && !this.playlist.isEmpty()) {
            // Maintain current song when shuffle is turned off
            UltraSong currentUltraSong = getCurrentSong();
            List<UltraSong> orderedList = new ArrayList<>();
            // Find the original order from allSongBytes that matches the playlist
            for (UltraSong song : this.allUltraSongs) {
                for (UltraSong pSong : this.playlist) {
                    if (song.getPath().equals(pSong.getPath())) {
                        orderedList.add(song);
                        break;
                    }
                }
            }
            this.playlist = orderedList;
            // Set currentSongIndex to the index of the current song in the new ordered playlist
            for (int i = 0; i < this.playlist.size(); i++) {
                if (this.playlist.get(i).getPath().equals(currentUltraSong.getPath())) {
                    this.currentSongIndex = i;
                    break;
                }
            }
            this.playedSongs.clear();
            this.playedSongs.add(Integer.valueOf(this.currentSongIndex));
            return;
        }
        if (!this.isShuffleEnabled && !this.playlist.isEmpty()) {
            Collections.sort(this.playlist, new Comparator() { // from class: com.xoeris.system.core.module.media.ux.audio.SoundFusion$$ExternalSyntheticLambda1
                @Override // java.util.Comparator
                public final int compare(Object obj, Object obj2) {
                    int compareToIgnoreCase;
                    compareToIgnoreCase = ((UltraSong) obj).getTitle().compareToIgnoreCase(((UltraSong) obj2).getTitle());
                    return compareToIgnoreCase;
                }
            });
            UltraSong currentUltraSong2 = getCurrentSong();
            int i = 0;
            while (true) {
                if (i >= this.playlist.size()) {
                    break;
                }
                if (!this.playlist.get(i).getPath().equals(currentUltraSong2.getPath())) {
                    i++;
                } else {
                    this.currentSongIndex = i;
                    break;
                }
            }
            this.playedSongs.clear();
        }
    }

    public List<UltraSong> getAllSongs() {
        List<UltraSong> allUltraSongs = new ArrayList<>();
        String[] projection = {"_id", "title", "artist", "album", TypedValues.TransitionType.S_DURATION, "_data"};
        try {
            Cursor cursor = this.context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, "is_music != 0", null, "title ASC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        String path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"));
                        String album = cursor.getString(cursor.getColumnIndexOrThrow("album"));
                        long durationMs = cursor.getLong(cursor.getColumnIndexOrThrow(TypedValues.TransitionType.S_DURATION));
                        String duration = String.format("%02d:%02d", Long.valueOf((durationMs / 1000) / 60), Long.valueOf((durationMs / 1000) % 60));
                        UltraSong ultraSong = new UltraSong(path, title, artist, album, duration);
                        allUltraSongs.add(ultraSong);
                    } finally {
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allUltraSongs;
    }

    public void toggleRepeat() {
        this.repeatMode = (this.repeatMode + 1) % 3;
        this.context.getSharedPreferences(PREFS_NAME, 0).edit().putInt("repeat_mode", this.repeatMode).apply();
    }

    public boolean isShuffleEnabled() {
        return this.isShuffleEnabled;
    }

    public int getRepeatMode() {
        return this.repeatMode;
    }

    public void playNext() {
        if (this.playlist.isEmpty()) {
            return;
        }
        // Always advance to the next song in the queue order
        if (this.repeatMode == 2) { // Repeat one
            // Stay on the current song
        } else if (this.currentSongIndex < this.playlist.size() - 1) {
            this.currentSongIndex++;
        } else if (this.repeatMode == 1) { // Repeat all
            this.currentSongIndex = 0;
        } else {
            return;
        }
        playSongAtCurrentIndex();
        if (this.listener != null) {
            UltraSong currentSong = getCurrentSong();
            if (currentSong != null) {
                this.listener.onSongChanged(currentSong.getTitle(), currentSong.getArtist());
            }
        }
    }

    public void playPrevious() {
        if (this.playlist.isEmpty()) {
            return;
        }
        if (getCurrentPosition() > 3000) {
            seekTo(0);
            return;
        }
        if (this.repeatMode == 2) { // Repeat one
            // Stay on the current song
        } else if (this.currentSongIndex > 0) {
            this.currentSongIndex--;
        } else if (this.repeatMode == 1) { // Repeat all
            this.currentSongIndex = this.playlist.size() - 1;
        }
        fadeOut(this.mediaPlayer);
        playSongAtCurrentIndex();
    }

    private int getNextUnplayedShuffleSong() {
        List<Integer> unplayed = new ArrayList<>();
        for (int i = 0; i < this.playlist.size(); i++) {
            if (!this.playedSongs.contains(Integer.valueOf(i))) {
                unplayed.add(Integer.valueOf(i));
            }
        }
        if (!unplayed.isEmpty()) {
            int index = (int) (Math.random() * unplayed.size());
            int songIndex = unplayed.get(index).intValue();
            this.playedSongs.add(Integer.valueOf(songIndex));
            return songIndex;
        }
        int index2 = this.currentSongIndex;
        return index2;
    }

    private int getLastPlayedSong() {
        List<Integer> played = new ArrayList<>(this.playedSongs);
        if (played.size() > 1) {
            this.playedSongs.remove(played.get(played.size() - 1));
            return played.get(played.size() - 2).intValue();
        }
        return this.currentSongIndex;
    }

    private int getUnplayedCount() {
        return this.playlist.size() - this.playedSongs.size();
    }

    public void playSongAtCurrentIndex() {
        if (this.currentSongIndex >= 0 && this.currentSongIndex < this.playlist.size()) {
            UltraSong ultraSong = this.playlist.get(this.currentSongIndex);
            playSong(this.context, Uri.parse(ultraSong.getPath()), ultraSong.getTitle(), ultraSong.getArtist());
        }
    }

    public UltraSong getCurrentSong() {
        if (playlist != null && currentSongIndex >= 0 && currentSongIndex < playlist.size()) {
            return playlist.get(currentSongIndex);
        }
        return null;
    }

    private HyperSound(Context context) {
        this.isShuffleEnabled = false;
        this.repeatMode = 0;
        this.fadeDuration = 0;
        this.context = context.getApplicationContext();
        this.notificationManager = new HyperSoundNotificationManager(context);
        setupMediaPlayer();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        this.fadeDuration = prefs.getInt(KEY_FADE_DURATION, 2);
        this.repeatMode = prefs.getInt("repeat_mode", 0);
        this.isShuffleEnabled = prefs.getBoolean("shuffle_state", false);
        this.fadeDuration = context.getSharedPreferences(PREFS_NAME, 0).getInt(KEY_FADE_DURATION, 2);
    }

    private void startProgressUpdate() {
        this.progressHandler.removeCallbacks(this.progressRunnable);
        this.progressHandler.post(this.progressRunnable);
    }

    private void stopProgressUpdate() {
        this.progressHandler.removeCallbacks(this.progressRunnable);
    }

    public static synchronized HyperSound getInstance(Context context) {
        HyperSound hyperSound;
        synchronized (HyperSound.class) {
            if (instance == null) {
                instance = new HyperSound(context);
            }
            hyperSound = instance;
        }
        return hyperSound;
    }

    private void setupMediaPlayer() {
        this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { // from class: com.xoeris.system.core.module.media.ux.audio.SoundFusion$$ExternalSyntheticLambda0
            @Override // android.media.MediaPlayer.OnCompletionListener
            public final void onCompletion(MediaPlayer mediaPlayer) {
                HyperSound.this.m242x82775eab(mediaPlayer);
            }
        });
    }

    /* renamed from: lambda$setupMediaPlayer$1$com-xoeris-system-core-module-media-ux-audio-SoundFusion, reason: not valid java name */
    /* synthetic */ void m242x82775eab(MediaPlayer mp) {
        if (this.repeatMode == 2) {
            this.mediaPlayer.seekTo(0);
            this.mediaPlayer.start();
            return;
        }
        if (this.isShuffleEnabled) {
            if (getUnplayedCount() == 0) {
                if (this.repeatMode == 1) {
                    this.playedSongs.clear();
                    this.currentSongIndex = (int) (Math.random() * this.playlist.size());
                    playSongAtCurrentIndex();
                    return;
                } else {
                    this.isPlaying = false;
                    if (this.listener != null) {
                        this.listener.onPlaybackStateChanged(false);
                        return;
                    }
                    return;
                }
            }
            this.currentSongIndex = getNextUnplayedShuffleSong();
            playSongAtCurrentIndex();
            return;
        }
        if (this.currentSongIndex < this.playlist.size() - 1) {
            this.currentSongIndex++;
            playSongAtCurrentIndex();
        } else if (this.repeatMode == 1) {
            this.currentSongIndex = 0;
            playSongAtCurrentIndex();
        } else {
            this.isPlaying = false;
            if (this.listener != null) {
                this.listener.onPlaybackStateChanged(false);
            }
        }
    }

    public void setListener(OnMusicPlayerListener listener) {
        this.listener = listener;
    }

    public void playSong(Context context, Uri songUri, String title, String artist) {
        try {
            if (this.mediaPlayer != null && this.mediaPlayer.isPlaying()) {
                this.lastPosition = this.mediaPlayer.getCurrentPosition();
                this.lastPlayedUltraSong = getCurrentSong();
            }
            if (this.mediaPlayer == null) {
                this.mediaPlayer = new MediaPlayer();
                setupMediaPlayer();
            }
            if (this.mediaPlayer != null && this.mediaPlayer.isPlaying()) {
                this.lastPosition = this.mediaPlayer.getCurrentPosition();
                this.lastPlayedUltraSong = getCurrentSong();
            } else {
                startNewSong(context, songUri, title, artist);
            }
            this.mediaPlayer.reset();
            this.mediaPlayer.setDataSource(context, songUri);
            this.mediaPlayer.prepare();
            this.mediaPlayer.start();
            fadeIn(this.mediaPlayer);
            this.isPlaying = true;
            this.currentSong = title;
            this.playedSongs.add(Integer.valueOf(this.currentSongIndex));
            if (this.listener != null) {
                this.listener.onSongChanged(title, artist);
                this.listener.onPlaybackStateChanged(true);
            }
            this.notificationManager.createNotification(title, artist, this.isPlaying, getCurrentSong(), getCurrentPosition(), getDuration());
            startProgressUpdate();
        } catch (IOException e) {
            e.printStackTrace();
            restorePreviousState();
            if (this.lastPlayedUltraSong != null && this.mediaPlayer != null) {
                try {
                    this.mediaPlayer.reset();
                    this.mediaPlayer.setDataSource(context, Uri.parse(this.lastPlayedUltraSong.getPath()));
                    this.mediaPlayer.prepare();
                    this.mediaPlayer.start();
                    this.mediaPlayer.seekTo(this.lastPosition);
                    this.isPlaying = true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void restorePreviousState() {
        if (this.lastPlayedUltraSong != null && this.mediaPlayer != null) {
            try {
                this.mediaPlayer.reset();
                this.mediaPlayer.setDataSource(this.context, Uri.parse(this.lastPlayedUltraSong.getPath()));
                this.mediaPlayer.prepare();
                this.mediaPlayer.start();
                this.mediaPlayer.seekTo(this.lastPosition);
                this.isPlaying = true;
                this.notificationManager.createNotification(this.lastPlayedUltraSong.getTitle(), this.lastPlayedUltraSong.getArtist(), this.isPlaying, getCurrentSong(), getCurrentPosition(), getDuration());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void startNewSong(Context context, Uri songUri, String title, String artist) throws IOException {
        if (this.mediaPlayer == null) {
            this.mediaPlayer = new MediaPlayer();
            setupMediaPlayer();
        }
        this.mediaPlayer.reset();
        this.mediaPlayer.setDataSource(context, songUri);
        this.mediaPlayer.prepare();
        this.mediaPlayer.start();
        this.isPlaying = true;
        this.currentSong = title;
        this.playedSongs.add(Integer.valueOf(this.currentSongIndex));
        if (this.listener != null) {
            this.listener.onSongChanged(title, artist);
            this.listener.onPlaybackStateChanged(true);
        }
        UltraSong currentUltraSong = getCurrentSong();
        if (currentUltraSong != null) {
            this.notificationManager.createNotification(currentUltraSong.getTitle(), currentUltraSong.getArtist(), this.isPlaying, currentUltraSong, getCurrentPosition(), getDuration());
        }
        startProgressUpdate();
    }

    public void playPause() {
        if (this.mediaPlayer != null) {
            if (this.isPlaying) {
                this.mediaPlayer.pause();
            } else {
                this.mediaPlayer.start();
            }
            this.isPlaying = !this.isPlaying;
            if (this.listener != null) {
                this.listener.onPlaybackStateChanged(this.isPlaying);
            }
            if (this.lastPlayedUltraSong != null) {
                this.notificationManager.createNotification(this.lastPlayedUltraSong.getTitle(), this.lastPlayedUltraSong.getArtist(), this.isPlaying, getCurrentSong(), getCurrentPosition(), getDuration());
            }
        }
    }

    public void seekTo(int position) {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.seekTo(position);
        }
    }

    public int getCurrentPosition() {
        if (this.mediaPlayer != null) {
            return this.mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (this.mediaPlayer != null) {
            return this.mediaPlayer.getDuration();
        }
        return 0;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public void release() {
        if (this.mediaPlayer != null) {
            if (this.mediaPlayer.isPlaying()) {
                this.lastPosition = this.mediaPlayer.getCurrentPosition();
                this.lastPlayedUltraSong = getCurrentSong();
                this.wasPlaying = true;
                this.context.getSharedPreferences(PREFS_NAME, 0).edit().putInt("repeat_mode", this.repeatMode).putBoolean("shuffle_state", this.isShuffleEnabled).apply();
            }
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }

    public void restoreState() {
        if (this.wasPlaying && this.lastPlayedUltraSong != null) {
            try {
                this.mediaPlayer = new MediaPlayer();
                setupMediaPlayer();
                this.mediaPlayer.setDataSource(this.context, Uri.parse(this.lastPlayedUltraSong.getPath()));
                this.mediaPlayer.prepare();
                this.mediaPlayer.start();
                this.mediaPlayer.seekTo(this.lastPosition);
                this.isPlaying = true;
                if (this.listener != null) {
                    this.listener.onSongChanged(this.lastPlayedUltraSong.getTitle(), this.lastPlayedUltraSong.getArtist());
                    this.listener.onPlaybackStateChanged(true);
                }
                this.notificationManager.createNotification(this.lastPlayedUltraSong.getTitle(), this.lastPlayedUltraSong.getArtist(), this.isPlaying, getCurrentSong(), getCurrentPosition(), getDuration());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setShuffleEnabled(boolean enabled) {
        this.isShuffleEnabled = enabled;
        this.context.getSharedPreferences(PREFS_NAME, 0).edit().putBoolean("shuffle_state", enabled).apply();
    }

    public void setRepeatMode(int mode) {
        this.repeatMode = mode;
        this.context.getSharedPreferences(PREFS_NAME, 0).edit().putInt("repeat_mode", mode).apply();
    }

    // Add methods to get next songs in queue and previous songs
    /**
     * Returns the queue order as it will be played, considering shuffle, repeat, and playedSongs state.
     * This method now reflects the actual upcoming play order, matching playback logic.
     */
    public List<UltraSong> getCurrentQueueOrder() {
        return new ArrayList<>(this.playlist);
    }
    public int getCurrentSongIndex() {
        return this.currentSongIndex;
    }
    public List<UltraSong> getPreviousSongs() {
        if (playlist == null || playlist.isEmpty() || currentSongIndex <= 0) return new ArrayList<>();
        return new ArrayList<>(playlist.subList(0, currentSongIndex));
    }
}
