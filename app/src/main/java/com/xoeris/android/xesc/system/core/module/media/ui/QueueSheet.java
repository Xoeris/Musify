package com.xoeris.android.xesc.system.core.module.media.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.xoeris.android.musify.R;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.HyperSound;
import com.xoeris.android.xesc.system.core.module.media.ux.audio.adapter.UltraSongAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("all")
public class QueueSheet extends BottomSheetDialogFragment {
    public static OnDismissListener OnDismissListener;
    private RecyclerView queueRecyclerView;
    private UltraSongAdapter queueAdapter;
    private List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> queueList = new ArrayList<>();
    private HyperSound hyperSound;
    private Context context;
    private OnDismissListener dismissListener;
    private ItemTouchHelper itemTouchHelper;

    public QueueSheet(HyperSound hyperSound, Context context) {
        this.hyperSound = hyperSound;
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_queue, container, false);

        // Make sure root view is transparent (recommended for consistency)
        view.setBackgroundColor(Color.TRANSPARENT);

        // Initialize RecyclerView
        queueRecyclerView = view.findViewById(R.id.queueRecyclerView);
        queueRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with click listener
        queueAdapter = new UltraSongAdapter(queueList, new UltraSongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong, int position) {
                if (hyperSound != null) {
                    hyperSound.setPlaylist(new ArrayList<>(queueList), position, true);
                    hyperSound.playSongAtCurrentIndex();
                    updateQueueList();
                }
            }
        }, R.layout.layout_item_music_queue);
        queueRecyclerView.setAdapter(queueAdapter);

        // Enable drag-and-drop reordering
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Track the current song before reordering
                com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong currentSong = null;
                int oldCurrentIndex = -1;

                if (hyperSound != null) {
                    oldCurrentIndex = hyperSound.getCurrentSongIndex();
                    if (oldCurrentIndex >= 0 && oldCurrentIndex < queueList.size()) {
                        currentSong = queueList.get(oldCurrentIndex);
                    }
                }

                // Swap items in list
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(queueList, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(queueList, i, i - 1);
                    }
                }

                queueAdapter.notifyItemMoved(fromPosition, toPosition);

                // Update SoundFusion playlist with new order
                if (hyperSound != null) {
                    int newCurrentIndex = 0;
                    if (currentSong != null) {
                        for (int i = 0; i < queueList.size(); i++) {
                            if (queueList.get(i).getPath().equals(currentSong.getPath())) {
                                newCurrentIndex = i;
                                break;
                            }
                        }
                    }
                    hyperSound.setPlaylist(new ArrayList<>(queueList), newCurrentIndex, true);
                }

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe action
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        });

        itemTouchHelper.attachToRecyclerView(queueRecyclerView);

        // Load or refresh queue
        updateQueueList();

        return view;
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        // 👇 MUST be before super.onCreate()
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheet); // Set your custom style here

        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);

                // Expand it to full screen
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        return dialog;
    }


    private void updateQueueList() {
        if (hyperSound != null) {
            queueList.clear();
            int repeatMode = hyperSound.getRepeatMode();
            com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong currentSong = hyperSound.getCurrentSong();
            List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> queueOrder = hyperSound.getCurrentQueueOrder();
            boolean isShuffle = hyperSound.isShuffleEnabled();
            // Repeat once (repeat current song)
            if (repeatMode == 2 && currentSong != null) {
                // Only show the current song in the queue
                queueList.clear();
                queueList.add(currentSong);
            } else if (queueOrder != null && !queueOrder.isEmpty() && currentSong != null) {
                // Repeat all + shuffle ON: show full shuffled queue, current song at top, then rest in shuffled order
                if (repeatMode == 1 && isShuffle) { // Only show shuffled queue when repeat all + shuffle
                    queueList.add(currentSong);
                    for (com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong song : queueOrder) {
                        if (!song.getPath().equals(currentSong.getPath())) {
                            queueList.add(song);
                        }
                    }
                } else if (repeatMode == 1 && !isShuffle) {
                    // Repeat all + shuffle OFF: show full queue in order, current song at top, then rest in order
                    queueList.add(currentSong);
                    for (com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong song : queueOrder) {
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
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();

            // Make the status bar transparent
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);

            // Make the navigation bar transparent
            window.setNavigationBarColor(Color.TRANSPARENT); // Transparent navigation bar

            // Allow layout to draw behind status bar and navigation bar
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            // Make the bottom sheet full height
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hyperSound != null) {
            hyperSound.setListener(new HyperSound.OnMusicPlayerListener() {
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