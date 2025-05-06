package com.xoeris.android.musify.app.activity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Process;
import androidx.appcompat.app.AppCompatActivity;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.service.HyperSoundService;

@SuppressWarnings("all")
public class BaseActivity extends AppCompatActivity {
    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && isTaskRoot()) {
            cleanupAndExit();
        }
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getBooleanExtra("EXIT", false)) {
            cleanupAndExit();
            finish();
        }
    }

    @SuppressLint("WrongConstant")
    protected void exitApp() {
        Intent intent = new Intent(this, getClass());
        intent.setFlags(335544320);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    private void cleanupAndExit() {
        stopService(new Intent(this, (Class<?>) HyperSoundService.class));
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        notificationManager.cancelAll();
        Process.killProcess(Process.myPid());
    }
}
