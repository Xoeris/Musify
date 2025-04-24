package com.xoeris.android.musify.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.xoeris.android.musify.R;
import com.xoeris.android.musify.app.activity.AboutSettingsActivity;
import com.xoeris.android.musify.app.activity.DisplaySettingsActivity;
import com.xoeris.android.musify.app.activity.SoundSettingsActivity;

@SuppressWarnings("all")
public class SettingsFragment extends Fragment {

    private Intent intent;
    private Intent intent2;
    private Intent intent3;
    private View displayCard;
    private View soundCard;
    private View aboutCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_settings, container, false);
        displayCard = view.findViewById(R.id.displayCard);
        displayCard.setOnClickListener(v -> {
            intent = new Intent(getActivity(), DisplaySettingsActivity.class);
            startActivity(intent);
        });
        soundCard = view.findViewById(R.id.soundCard);
        soundCard.setOnClickListener(v -> {
            intent2 = new Intent(getActivity(), SoundSettingsActivity.class);
            startActivity(intent2);
        });
        aboutCard = view.findViewById(R.id.aboutCard);
        aboutCard.setOnClickListener(v -> {
            intent3 = new Intent(getActivity(), AboutSettingsActivity.class);
            startActivity(intent3);
        });
        return view;
    }

}
