package com.xoeris.android.musify.app.classes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.xoeris.android.musify.R;
import com.xoeris.android.musify.app.activity.HomeActivity;
import com.xoeris.android.musify.app.receiver.NotificationReceiver;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SongByte;

@SuppressWarnings("all")
public class MusicNotificationManager {
    private static final String CHANNEL_ID = "music_player_channel";
    private static final int NOTIFICATION_ID = 1;
    private NotificationActionListener actionListener;
    private final Context context;
    private MediaStyle mediaStyle;
    private MediaSessionCompat mediaSession;
    private final NotificationManager notificationManager;

    public interface NotificationActionListener {
        void onNextClicked();

        void onPlayPauseClicked();

        void onPreviousClicked();
    }

    public MusicNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService("notification");
        createNotificationChannel();
        setupMediaSession();
    }

    private void setupMediaSession() {
        this.mediaSession = new MediaSessionCompat(this.context, "MusicSession");
        this.mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        this.mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                if (actionListener != null) actionListener.onPlayPauseClicked();
            }
            
            @Override
            public void onPause() {
                if (actionListener != null) actionListener.onPlayPauseClicked();
            }
            
            @Override
            public void onSkipToNext() {
                if (actionListener != null) actionListener.onNextClicked();
            }
            
            @Override
            public void onSkipToPrevious() {
                if (actionListener != null) actionListener.onPreviousClicked();
            }

            @Override
            public void onSeekTo(long pos) {
                if (context instanceof HomeActivity) {
                    ((HomeActivity) context).getSoundFusion().seekTo((int) pos);
                    ((HomeActivity) context).getSeekBar().setProgress((int) pos);
                }
            }
        });
        this.mediaSession.setActive(true);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music Player", 2);
            channel.setSound(null, null);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setShowBadge(true);
            this.notificationManager.createNotificationChannel(channel);
        }
    }

    public void cancelNotification() {
        this.notificationManager.cancel(1);
    }

    public void setActionListener(NotificationActionListener listener) {
        this.actionListener = listener;
    }

    private void updateMediaSession(String title, String artist, Bitmap albumArt, boolean isPlaying, long position, long duration) {
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_TITLE, title).putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist).putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt).putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration).build();
        this.mediaSession.setMetadata(metadata);
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(816L);
        int state = isPlaying ? 3 : 2;
        stateBuilder.setState(state, position, 1.0f);
        this.mediaSession.setPlaybackState(stateBuilder.build());
    }

    public void createNotification(String title, String artist, boolean isPlaying, SongByte songByte, long position, long duration) {
        Bitmap albumArtBitmap = getAlbumArtBitmap(songByte);
        updateMediaSession(title, artist, albumArtBitmap, isPlaying, position, duration);
        Intent intent = new Intent(this.context, (Class<?>) HomeActivity.class);
        intent.setFlags(603979776);
        PendingIntent contentIntent = PendingIntent.getActivity(this.context, 0, intent, 201326592);
        Intent previousIntent = new Intent("PREVIOUS_ACTION").setClass(this.context, NotificationReceiver.class);
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(this.context, 0, previousIntent, 201326592);
        Intent playPauseIntent = new Intent("PLAY_PAUSE_ACTION").setClass(this.context, NotificationReceiver.class);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this.context, 0, playPauseIntent, 201326592);
        Intent nextIntent = new Intent("NEXT_ACTION").setClass(this.context, NotificationReceiver.class);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this.context, 0, nextIntent, 201326592);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, CHANNEL_ID).setSmallIcon(albumArtBitmap != null ? R.drawable.ic_musify : R.drawable.ic_album).setLargeIcon(albumArtBitmap).setContentTitle(title).setContentText(artist).setOnlyAlertOnce(true).setShowWhen(false).setContentIntent(contentIntent).setOngoing(true).setAutoCancel(false).addAction(R.drawable.ic_skip_previous, "Previous", previousPendingIntent).addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow, isPlaying ? "Pause" : "Play", playPausePendingIntent).addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent).setPriority(2).setOngoing(true).setForegroundServiceBehavior(1);
        MediaStyle mediaStyle = new MediaStyle()
            .setMediaSession(this.mediaSession.getSessionToken())
            .setShowActionsInCompactView(0, 1, 2);
        builder.setStyle(mediaStyle);
        Notification notification = builder.build();
        notification.flags |= 34;
        this.notificationManager.notify(1, notification);
    }

    private boolean supportsSystemMediaPlayback() {
        return true;
    }

    private Bitmap getAlbumArtBitmap(SongByte songByte) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(songByte.getPath());
            byte[] albumArt = retriever.getEmbeddedPicture();
            retriever.release();
            if (albumArt != null) {
                return BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateNotification(String title, String artist, boolean isPlaying, SongByte songByte, long position, long duration) {
        createNotification(title, artist, isPlaying, songByte, position, duration);
    }

    public void updateMediaSessionPlaybackState(long position, boolean isPlaying) {
        if (mediaSession != null) {
            PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO |
                           PlaybackStateCompat.ACTION_PLAY |
                           PlaybackStateCompat.ACTION_PAUSE |
                           PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                           PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
            
            int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            stateBuilder.setState(state, position, 1.0f);
            mediaSession.setPlaybackState(stateBuilder.build());
        }
    }
}
