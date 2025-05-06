package com.xoeris.android.xesc.system.core.module.media.ux.audio.adapter;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.xoeris.android.musify.R;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class UltraSongAdapter extends RecyclerView.Adapter<UltraSongAdapter.ViewHolder> {
    private OnSongClickListener listener;
    private List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> originalUltraSongs;
    private List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> ultraSongs;
    private int layoutResId = R.layout.layout_item_music;

    public interface OnSongClickListener {
        void onSongClick(com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong, int i);
    }

    public UltraSongAdapter(List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> ultraSongs, OnSongClickListener listener) {
        this(ultraSongs, listener, R.layout.layout_item_music);
    }
    public UltraSongAdapter(List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> ultraSongs, OnSongClickListener listener, int layoutResId) {
        this.ultraSongs = ultraSongs;
        this.listener = listener;
        this.layoutResId = layoutResId;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        return new ViewHolder(view);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong = this.ultraSongs.get(position);
        holder.titleText.setText(ultraSong.getTitle());
        holder.artistText.setText(ultraSong.getArtist());
        
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(ultraSong.getPath());
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                holder.albumIcon.setImageBitmap(bitmap);
                holder.albumIcon.setImageTintList(null);
            } else {
                holder.albumIcon.setImageResource(R.drawable.ic_album);
                holder.albumIcon.setImageTintList(ColorStateList.valueOf(-1));
            }
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
            holder.albumIcon.setImageResource(R.drawable.ic_album);
            holder.albumIcon.setImageTintList(ColorStateList.valueOf(-1));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.xoeris.system.core.module.media.ux.audio.SongByteAdapter$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                UltraSongAdapter.this.m238x5a0a69e6(ultraSong, holder, view);
            }
        });
    }

    /* renamed from: lambda$onBindViewHolder$0$com-xoeris-system-core-module-media-ux-audio-SongByteAdapter, reason: not valid java name */
    /* synthetic */ void m238x5a0a69e6(com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong, ViewHolder holder, View v) {
        if (this.listener != null) {
            this.listener.onSongClick(ultraSong, holder.getAdapterPosition());
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.ultraSongs.size();
    }

    public UltraSongAdapter(List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> ultraSongs) {
        this.ultraSongs = ultraSongs;
        this.listener = null;
    }

    public void filterSongs(String query) {
        if (this.originalUltraSongs == null) {
            this.originalUltraSongs = new ArrayList(this.ultraSongs);
        }
        List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        if (query.isEmpty()) {
            filteredList.addAll(this.originalUltraSongs);
        } else {
            for (com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong ultraSong : this.originalUltraSongs) {
                if (ultraSong.getTitle().toLowerCase().contains(lowerQuery) || ultraSong.getArtist().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(ultraSong);
                }
            }
        }
        this.ultraSongs.clear();
        this.ultraSongs.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void resetSongs(List<com.xoeris.android.xesc.system.core.module.media.ux.audio.UltraSong> newUltraSongs) {
        this.originalUltraSongs = new ArrayList(newUltraSongs);
        this.ultraSongs.clear();
        this.ultraSongs.addAll(newUltraSongs);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView artistText;
        TextView titleText;
        ImageView albumIcon;

        ViewHolder(View itemView) {
            super(itemView);
            this.titleText = (TextView) itemView.findViewById(R.id.song_title);
            this.artistText = (TextView) itemView.findViewById(R.id.song_artist);
            this.albumIcon = (ImageView) itemView.findViewById(R.id.music_icon);
        }
    }
}
