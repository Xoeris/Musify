package com.xoeris.android.musify.app.fragment;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.constraintlayout.core.motion.utils.TypedValues;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.xoeris.android.musify.R;
import com.xoeris.android.musify.app.activity.HomeActivity;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SongByte;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SongByteAdapter;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SoundFusion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class HomeFragment extends Fragment {
    private SongByteAdapter adapter;
    private List<SongByte> filteredList;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private List<SongByte> songByteList;
    private SoundFusion soundFusion;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_home, container, false);
        this.searchEditText = (EditText) view.findViewById(R.id.searchEditText);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.searchListView);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.soundFusion = SoundFusion.getInstance(requireContext());
        this.songByteList = new ArrayList();
        this.filteredList = new ArrayList();
        this.adapter = new SongByteAdapter(this.filteredList, new SongByteAdapter.OnSongClickListener() { // from class: com.xoeris.app.musify.fragments.HomeFragment$$ExternalSyntheticLambda0
            @Override // com.xoeris.system.core.module.media.ux.audio.SongByteAdapter.OnSongClickListener
            public final void onSongClick(SongByte songByte, int i) {
                HomeFragment.this.m217xc25240b1(songByte, i);
            }
        });
        this.recyclerView.setAdapter(this.adapter);
        this.searchEditText.addTextChangedListener(new TextWatcher() { // from class: com.xoeris.app.musify.fragments.HomeFragment.2
            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HomeFragment.this.filterSongs(s.toString().toLowerCase().trim());
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable s) {
            }
        });
        try {
            loadSongs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return view;
    }

    /* renamed from: lambda$onCreateView$0$com-xoeris-app-musify-fragments-HomeFragment, reason: not valid java name */
    /* synthetic */ void m217xc25240b1(SongByte song, int position) {
        int originalIndex;
        if (!this.songByteList.isEmpty() && (originalIndex = findSongIndex(song)) != -1) {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).syncUIState();
            }
            this.soundFusion.setPlaylist(this.songByteList, originalIndex);
            this.soundFusion.playSong(requireContext(), Uri.parse(song.getPath()), song.getTitle(), song.getArtist());
            final Handler handler = new Handler();
            Runnable updateRunnable = new Runnable() { // from class: com.xoeris.app.musify.fragments.HomeFragment.1
                int count = 0;

                @Override // java.lang.Runnable
                public void run() {
                    if (this.count < 5 && (HomeFragment.this.getActivity() instanceof HomeActivity)) {
                        ((HomeActivity) HomeFragment.this.getActivity()).syncUIState();
                        this.count++;
                        handler.postDelayed(this, 100L);
                    }
                }
            };
            handler.post(updateRunnable);
        }
    }

    private void loadSongs() throws IOException {
        ContentResolver contentResolver = requireActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, "is_music!= 0", null, "title ASC");
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    this.songByteList.clear();
                    this.filteredList.clear();
                    while (cursor.moveToNext()) {
                        String data = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"));
                        String duration = cursor.getString(cursor.getColumnIndexOrThrow(TypedValues.TransitionType.S_DURATION));
                        SongByte songByte = new SongByte(title, artist, data, duration, duration);
                        this.songByteList.add(songByte);
                    }
                    this.filteredList.addAll(this.songByteList);
                    this.adapter.notifyDataSetChanged();
                }
            } finally {
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void filterSongs(String query) {
        this.filteredList.clear();
        if (query.isEmpty()) {
            this.filteredList.addAll(this.songByteList);
        } else {
            for (SongByte songByte : this.songByteList) {
                if (songByte.getTitle().toLowerCase().contains(query) || songByte.getArtist().toLowerCase().contains(query)) {
                    this.filteredList.add(songByte);
                }
            }
        }
        this.adapter.notifyDataSetChanged();
    }

    private int findSongIndex(SongByte songByte) {
        for (int i = 0; i < this.songByteList.size(); i++) {
            if (this.songByteList.get(i).getPath().equals(songByte.getPath())) {
                return i;
            }
        }
        return -1;
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        try {
            loadSongs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
