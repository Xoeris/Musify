package com.xoeris.android.xesc.system.core.module.media.ux.audio.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.manager.SoundFusionNotificationManager;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SoundFusion;

@SuppressWarnings("all")
public class SoundFusionService extends Service {
    private SoundFusionNotificationManager notificationManager;
    private SoundFusion soundFusion;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.soundFusion = SoundFusion.getInstance(this);
        this.notificationManager = new SoundFusionNotificationManager(this);
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
