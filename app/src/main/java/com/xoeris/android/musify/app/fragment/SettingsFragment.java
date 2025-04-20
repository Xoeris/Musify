package com.xoeris.android.musify.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.xoeris.android.musify.R;
import com.xoeris.android.musify.app.activity.DisplaySettingsActivity;
import com.xoeris.android.musify.app.activity.SoundSettingsActivity;

@SuppressWarnings("all")
public class SettingsFragment extends Fragment {

    private Intent intent;
    private Intent intent2;
    private LinearLayout cardThemeItem;
    private LinearLayout cardSoundItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_settings, container, false);
        cardThemeItem = view.findViewById(R.id.layout_card_item_theme);
        cardSoundItem = view.findViewById(R.id.layout_card_item_sound);

        cardThemeItem.setOnClickListener(v -> {
            intent = new android.content.Intent(getActivity(), DisplaySettingsActivity.class);
            startActivity(intent);
        });

        cardSoundItem.setOnClickListener(v -> {
            intent2 = new android.content.Intent(getActivity(), SoundSettingsActivity.class);
            startActivity(intent2);
        });
        return view;
    }

}
