package com.xoeris.android.musify.app.fragment;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.xoeris.android.xesc.system.core.module.media.ux.audio.adapter.UltraSongAdapter;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.HyperSound;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("all")
public class HomeFragment extends Fragment {
    private UltraSongAdapter adapter;
    private List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> filteredList;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> ultraSongList;
    private HyperSound hyperSound;
    private static List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> staticSongCache = null;
    private static boolean isCacheLoaded = false;
    private static final String SONG_CACHE_KEY = "song_cache";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_home, container, false);
        this.searchEditText = (EditText) view.findViewById(R.id.searchEditText);
        this.recyclerView = (RecyclerView) view.findViewById(R.id.searchListView);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.hyperSound = HyperSound.getInstance(requireContext());
        this.ultraSongList = new ArrayList<>();
        this.filteredList = new ArrayList<>();
        this.adapter = new UltraSongAdapter(this.filteredList, new UltraSongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong, int i) {
                HomeFragment.this.m217xc25240b1(ultraSong, i);
            }
        });
        this.recyclerView.setAdapter(this.adapter);
        this.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HomeFragment.this.filterSongs(s.toString().toLowerCase().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        loadSongsAsync();
        return view;
    }

    /* renamed from: lambda$onCreateView$0$com-xoeris-app-musify-fragments-HomeFragment, reason: not valid java name */
    /* synthetic */ void m217xc25240b1(com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong song, int position) {
        int originalIndex;
        if (!this.ultraSongList.isEmpty() && (originalIndex = findSongIndex(song)) != -1) {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).syncUIState();
            }
            this.hyperSound.setPlaylist(this.ultraSongList, originalIndex, false);
            this.hyperSound.playSong(requireContext(), Uri.parse(song.getPath()), song.getTitle(), song.getArtist());
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

    private void loadSongsAsync() {
        final WeakReference<HomeFragment> fragmentRef = new WeakReference<>(this);
        mainHandler.post(() -> showLoadingIndicator(true));
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> loadedSongs = new ArrayList<>();
                HomeFragment fragment = fragmentRef.get();
                if (fragment == null || fragment.getActivity() == null) return;
                ContentResolver contentResolver = fragment.getActivity().getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Cursor cursor = null;
                try {
                    cursor = contentResolver.query(uri, null, "is_music!= 0", null, "title ASC");
                    if (cursor != null && cursor.getCount() > 0) {
                        int batchSize = 30;
                        int count = 0;
                        while (cursor.moveToNext()) {
                            String data = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                            String artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"));
                            String duration = cursor.getString(cursor.getColumnIndexOrThrow(TypedValues.TransitionType.S_DURATION));
                            com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong = new com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong(title, artist, data, duration, duration);
                            loadedSongs.add(ultraSong);
                            count++;
                            if (count % batchSize == 0) {
                                final List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> batch = new ArrayList<>(loadedSongs);
                                mainHandler.post(() -> {
                                    HomeFragment frag = fragmentRef.get();
                                    if (frag == null) return;
                                    frag.ultraSongList.clear();
                                    frag.filteredList.clear();
                                    frag.ultraSongList.addAll(batch);
                                    frag.filteredList.addAll(batch);
                                    staticSongCache = new ArrayList<>(batch);
                                    isCacheLoaded = true;
                                    if (frag.adapter != null) frag.adapter.notifyDataSetChanged();
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    // Log or handle error
                } finally {
                    if (cursor != null) cursor.close();
                }
                mainHandler.post(() -> {
                    HomeFragment frag = fragmentRef.get();
                    if (frag == null) return;
                    frag.ultraSongList.clear();
                    frag.filteredList.clear();
                    frag.ultraSongList.addAll(loadedSongs);
                    frag.filteredList.addAll(loadedSongs);
                    staticSongCache = new ArrayList<>(loadedSongs);
                    isCacheLoaded = true;
                    if (frag.adapter != null) frag.adapter.notifyDataSetChanged();
                    showLoadingIndicator(false);
                });
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void filterSongs(String query) {
        this.filteredList.clear();
        if (query.isEmpty()) {
            this.filteredList.addAll(this.ultraSongList);
        } else {
            for (com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong : this.ultraSongList) {
                if (ultraSong.getTitle().toLowerCase().contains(query) || ultraSong.getArtist().toLowerCase().contains(query)) {
                    this.filteredList.add(ultraSong);
                }
            }
        }
        this.adapter.notifyDataSetChanged();
    }

    private int findSongIndex(com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong) {
        for (int i = 0; i < this.ultraSongList.size(); i++) {
            if (this.ultraSongList.get(i).getPath().equals(ultraSong.getPath())) {
                return i;
            }
        }
        return -1;
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        refreshSongsInBackground();
    }

    private void refreshSongsInBackground() {
        final WeakReference<HomeFragment> fragmentRef = new WeakReference<>(this);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> loadedSongs = new ArrayList<>();
                HomeFragment fragment = fragmentRef.get();
                if (fragment == null || fragment.getActivity() == null) return;
                ContentResolver contentResolver = fragment.getActivity().getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Cursor cursor = null;
                try {
                    cursor = contentResolver.query(uri, null, "is_music!= 0", null, "title ASC");
                    if (cursor != null && cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            String data = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                            String artist = cursor.getString(cursor.getColumnIndexOrThrow("artist"));
                            String duration = cursor.getString(cursor.getColumnIndexOrThrow(TypedValues.TransitionType.S_DURATION));
                            com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong = new com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong(title, artist, data, duration, duration);
                            loadedSongs.add(ultraSong);
                        }
                    }
                } catch (Exception e) {
                    // Log or handle error
                } finally {
                    if (cursor != null) cursor.close();
                }
                mainHandler.post(() -> {
                    HomeFragment frag = fragmentRef.get();
                    if (frag == null) return;
                    boolean changed = false;
                    if (staticSongCache == null || loadedSongs.size() != staticSongCache.size()) {
                        changed = true;
                    } else {
                        for (int i = 0; i < loadedSongs.size(); i++) {
                            if (!loadedSongs.get(i).getPath().equals(staticSongCache.get(i).getPath())) {
                                changed = true;
                                break;
                            }
                        }
                    }
                    if (changed) {
                        frag.ultraSongList.clear();
                        frag.filteredList.clear();
                        frag.ultraSongList.addAll(loadedSongs);
                        frag.filteredList.addAll(loadedSongs);
                        staticSongCache = new ArrayList<>(loadedSongs);
                        isCacheLoaded = true;
                        if (frag.adapter != null) frag.adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void showLoadingIndicator(boolean show) {
        View root = getView();
        if (root == null) return;
        View loading = root.findViewById(R.id.loadingIndicator);
        if (loading != null) loading.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
