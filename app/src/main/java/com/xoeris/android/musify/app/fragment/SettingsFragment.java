package com.xoeris.android.musify.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.xoeris.android.musify.R;

@SuppressWarnings("all")
public class SettingsFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_settings, container, false);
        View displayCard = view.findViewById(R.id.displayCard);
        displayCard.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.xoeris.android.musify.app.activity.DisplaySettingsActivity.class);
            startActivity(intent);
        });
        return view;
    }

}
