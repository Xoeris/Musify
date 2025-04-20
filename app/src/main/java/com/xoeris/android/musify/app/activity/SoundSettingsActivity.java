package com.xoeris.android.musify.app.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import com.xoeris.android.xesc.system.core.module.media.ui.VortexSlider;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SoundFusion;
import androidx.appcompat.app.AppCompatActivity;
import com.xoeris.android.musify.R;
import android.content.SharedPreferences;

@SuppressWarnings("all")
public class SoundSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_sound_settings);
        ImageView back = findViewById(R.id.back);
        if (back != null) {
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        // Volume controls
        Switch volumeSwitch = findViewById(R.id.volumeSwitch);
        VortexSlider vortexSlider = findViewById(R.id.vortexSlider);
        SoundFusion soundFusion = SoundFusion.getInstance(this);
        SharedPreferences prefs = getSharedPreferences("SoundSettingsPrefs", MODE_PRIVATE);
        boolean volumeEnabled = prefs.getBoolean("volume_enabled", soundFusion.isVolumeEnabled());
        float savedVolume = prefs.getFloat("user_volume", soundFusion.getUserVolume());
        // Set initial state
        if (volumeSwitch != null) {
            volumeSwitch.setChecked(volumeEnabled);
            volumeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                soundFusion.setVolumeEnabled(isChecked);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("volume_enabled", isChecked);
                editor.apply();
                if (vortexSlider != null) {
                    vortexSlider.setEnabled(isChecked);
                }
            });
        }
        if (vortexSlider != null) {
            vortexSlider.setEnabled(volumeEnabled);
            vortexSlider.setProgress((int)(savedVolume * 100));
            vortexSlider.setOnSeekBarChangeListener(new VortexSlider.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(VortexSlider slider, int progress, boolean fromUser) {
                    if (volumeSwitch == null || !volumeSwitch.isChecked()) return;
                    float userVolume = progress / 100f;
                    soundFusion.setUserVolume(userVolume);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putFloat("user_volume", userVolume);
                    editor.apply();
                }
                @Override
                public void onStartTrackingTouch(VortexSlider slider) {}
                @Override
                public void onStopTrackingTouch(VortexSlider slider) {}
            });
        }
    }
}