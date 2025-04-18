package com.xoeris.android.musify.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import com.xoeris.android.musify.app.classes.MusicNotificationManager;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SoundFusion;

@SuppressWarnings("all")
public class MusicService extends Service {
    private MusicNotificationManager notificationManager;
    private SoundFusion soundFusion;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.soundFusion = SoundFusion.getInstance(this);
        this.notificationManager = new MusicNotificationManager(this);
    }

    @Override // android.app.Service
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (this.notificationManager != null) {
            this.notificationManager.cancelNotification();
        }
        if (this.soundFusion != null) {
            this.soundFusion.release();
        }
        stopSelf();
        Process.killProcess(Process.myPid());
    }
}
