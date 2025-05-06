package com.xoeris.android.xesc.system.core.module.media.ux.audio.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.manager.HyperSoundNotificationManager;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.HyperSound;

@SuppressWarnings("all")
public class HyperSoundService extends Service {
    private HyperSoundNotificationManager notificationManager;
    private HyperSound hyperSound;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.hyperSound = HyperSound.getInstance(this);
        this.notificationManager = new HyperSoundNotificationManager(this);
    }

    @Override // android.app.Service
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (this.notificationManager != null) {
            this.notificationManager.cancelNotification();
        }
        if (this.hyperSound != null) {
            this.hyperSound.release();
        }
        stopSelf();
        Process.killProcess(Process.myPid());
    }
}
