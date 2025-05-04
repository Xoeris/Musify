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
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SongByte;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.adapter.SongByteAdapter;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SoundFusion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("all")
public class HomeFragment extends Fragment {
    private SongByteAdapter adapter;
    private List<SongByte> filteredList;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private List<SongByte> songByteList;
    private SoundFusion soundFusion;
    private static List<SongByte> staticSongCache = null;
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
        this.soundFusion = SoundFusion.getInstance(requireContext());
        this.songByteList = new ArrayList<>();
        this.filteredList = new ArrayList<>();
        this.adapter = new SongByteAdapter(this.filteredList, new SongByteAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(SongByte songByte, int i) {
                HomeFragment.this.m217xc25240b1(songByte, i);
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
    /* synthetic */ void m217xc25240b1(SongByte song, int position) {
        int originalIndex;
        if (!this.songByteList.isEmpty() && (originalIndex = findSongIndex(song)) != -1) {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).syncUIState();
            }
            this.soundFusion.setPlaylist(this.songByteList, originalIndex, false);
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

    private void loadSongsAsync() {
        final WeakReference<HomeFragment> fragmentRef = new WeakReference<>(this);
        mainHandler.post(() -> showLoadingIndicator(true));
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<SongByte> loadedSongs = new ArrayList<>();
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
                            SongByte songByte = new SongByte(title, artist, data, duration, duration);
                            loadedSongs.add(songByte);
                            count++;
                            if (count % batchSize == 0) {
                                final List<SongByte> batch = new ArrayList<>(loadedSongs);
                                mainHandler.post(() -> {
                                    HomeFragment frag = fragmentRef.get();
                                    if (frag == null) return;
                                    frag.songByteList.clear();
                                    frag.filteredList.clear();
                                    frag.songByteList.addAll(batch);
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
                    frag.songByteList.clear();
                    frag.filteredList.clear();
                    frag.songByteList.addAll(loadedSongs);
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
        refreshSongsInBackground();
    }

    private void refreshSongsInBackground() {
        final WeakReference<HomeFragment> fragmentRef = new WeakReference<>(this);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<SongByte> loadedSongs = new ArrayList<>();
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
                            SongByte songByte = new SongByte(title, artist, data, duration, duration);
                            loadedSongs.add(songByte);
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
                        frag.songByteList.clear();
                        frag.filteredList.clear();
                        frag.songByteList.addAll(loadedSongs);
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
