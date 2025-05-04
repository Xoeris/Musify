package com.xoeris.android.xesc.system.core.module.media.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.xoeris.android.musify.R;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SongByte;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.SoundFusion;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.adapter.SongByteAdapter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class QueueDialogSheet extends BottomSheetDialogFragment {
    public static OnDismissListener OnDismissListener;
    private RecyclerView queueRecyclerView;
    private SongByteAdapter queueAdapter;
    private List<SongByte> queueList = new ArrayList<>();
    private SoundFusion soundFusion;
    private Context context;
    private OnDismissListener dismissListener;
    private ItemTouchHelper itemTouchHelper;

    public QueueDialogSheet(SoundFusion soundFusion, Context context) {
        this.soundFusion = soundFusion;
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_queue, container, false);
        queueRecyclerView = view.findViewById(R.id.queueRecyclerView);
        queueRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        queueAdapter = new SongByteAdapter(queueList, new SongByteAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(SongByte songByte, int position) {
                if (soundFusion != null) {
                    soundFusion.setPlaylist(new ArrayList<>(queueList), position, true);
                    soundFusion.playSongAtCurrentIndex(); // <-- Use the correct method here
                    updateQueueList();
                }
            }
        });
        queueRecyclerView.setAdapter(queueAdapter);
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                // Track the current song before reordering
                SongByte currentSong = null;
                int oldCurrentIndex = -1;
                if (soundFusion != null) {
                    oldCurrentIndex = soundFusion.getCurrentSongIndex();
                    if (oldCurrentIndex >= 0 && oldCurrentIndex < queueList.size()) {
                        currentSong = queueList.get(oldCurrentIndex);
                    }
                }
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        java.util.Collections.swap(queueList, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        java.util.Collections.swap(queueList, i, i - 1);
                    }
                }
                queueAdapter.notifyItemMoved(fromPosition, toPosition);
                // After reordering, update the playlist and currentSongIndex
                if (soundFusion != null) {
                    int newCurrentIndex = 0;
                    if (currentSong != null) {
                        for (int i = 0; i < queueList.size(); i++) {
                            if (queueList.get(i).getPath().equals(currentSong.getPath())) {
                                newCurrentIndex = i;
                                break;
                            }
                        }
                    }
                    soundFusion.setPlaylist(new ArrayList<>(queueList), newCurrentIndex, true);
                }
                return true;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}
            @Override
            public boolean isLongPressDragEnabled() { return true; }
        });
        itemTouchHelper.attachToRecyclerView(queueRecyclerView);
        updateQueueList();
        return view;
    }

    private void updateQueueList() {
        if (soundFusion != null) {
            queueList.clear();
            int repeatMode = soundFusion.getRepeatMode();
            SongByte currentSong = soundFusion.getCurrentSong();
            List<SongByte> queueOrder = soundFusion.getCurrentQueueOrder();
            boolean isShuffle = soundFusion.isShuffleEnabled();
            // Repeat once (repeat current song)
            if (repeatMode == 2 && currentSong != null) {
                // Only show the current song in the queue
                queueList.clear();
                queueList.add(currentSong);
            } else if (queueOrder != null && !queueOrder.isEmpty() && currentSong != null) {
                // Repeat all + shuffle ON: show full shuffled queue, current song at top, then rest in shuffled order
                if (repeatMode == 1 && isShuffle) { // Only show shuffled queue when repeat all + shuffle
                    queueList.add(currentSong);
                    for (SongByte song : queueOrder) {
                        if (!song.getPath().equals(currentSong.getPath())) {
                            queueList.add(song);
                        }
                    }
                } else if (repeatMode == 1 && !isShuffle) {
                    // Repeat all + shuffle OFF: show full queue in order, current song at top, then rest in order
                    queueList.add(currentSong);
                    for (SongByte song : queueOrder) {
                        if (!song.getPath().equals(currentSong.getPath())) {
                            queueList.add(song);
                        }
                    }
                }
                // Removed repeat off (ic_repeat_off) logic
            }
            // No extra shuffle needed here, just display the order from SoundFusion
            if (queueAdapter != null) queueAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (soundFusion != null) {
            soundFusion.setListener(new SoundFusion.OnMusicPlayerListener() {
                @Override
                public void onPlaybackStateChanged(boolean isPlaying) {
                    refreshQueue();
                }
                @Override
                public void onProgressChanged(int position, int duration) {}
                @Override
                public void onSongChanged(String title, String artist) {
                    refreshQueue();
                }
                @Override
                public void onTaskRemoved(android.content.Intent intent) {}
            });
        }
    }

    public void refreshQueue() {
        updateQueueList();
    }

    public void setDismissListener(OnDismissListener listener) {
        this.dismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onDismiss(dialog);
        }
    }

    public interface OnDismissListener {
        void onDismiss(DialogInterface dialog);
    }
}