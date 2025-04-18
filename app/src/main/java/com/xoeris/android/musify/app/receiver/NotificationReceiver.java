package com.xoeris.android.musify.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SoundFusion;

@SuppressWarnings("all")
public class NotificationReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        char c;
        SoundFusion soundFusion = SoundFusion.getInstance(context);
        String action = intent.getAction();
        switch (action.hashCode()) {
            case 560451710:
                if (action.equals("PREVIOUS_ACTION")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1461011714:
                if (action.equals("NEXT_ACTION")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 2111758314:
                if (action.equals("PLAY_PAUSE_ACTION")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                soundFusion.playPrevious();
                break;
            case 1:
                soundFusion.playPause();
                break;
            case 2:
                soundFusion.playNext();
                break;
        }
    }
}
