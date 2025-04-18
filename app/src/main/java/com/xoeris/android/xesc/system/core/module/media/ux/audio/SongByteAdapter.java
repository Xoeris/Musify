package com.xoeris.android.xesc.system.core.module.media.ux.audio;

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
public class SongByteAdapter extends RecyclerView.Adapter<SongByteAdapter.ViewHolder> {
    private OnSongClickListener listener;
    private List<SongByte> originalSongBytes;
    private List<SongByte> songBytes;

    public interface OnSongClickListener {
        void onSongClick(SongByte songByte, int i);
    }

    public SongByteAdapter(List<SongByte> songBytes, OnSongClickListener listener) {
        this.songBytes = songBytes;
        this.listener = listener;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
        return new ViewHolder(view);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SongByte songByte = this.songBytes.get(position);
        holder.titleText.setText(songByte.getTitle());
        holder.artistText.setText(songByte.getArtist());
        
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(songByte.getPath());
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
                SongByteAdapter.this.m238x5a0a69e6(songByte, holder, view);
            }
        });
    }

    /* renamed from: lambda$onBindViewHolder$0$com-xoeris-system-core-module-media-ux-audio-SongByteAdapter, reason: not valid java name */
    /* synthetic */ void m238x5a0a69e6(SongByte songByte, ViewHolder holder, View v) {
        if (this.listener != null) {
            this.listener.onSongClick(songByte, holder.getAdapterPosition());
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.songBytes.size();
    }

    public SongByteAdapter(List<SongByte> songBytes) {
        this.songBytes = songBytes;
        this.listener = null;
    }

    public void filterSongs(String query) {
        if (this.originalSongBytes == null) {
            this.originalSongBytes = new ArrayList(this.songBytes);
        }
        List<SongByte> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        if (query.isEmpty()) {
            filteredList.addAll(this.originalSongBytes);
        } else {
            for (SongByte songByte : this.originalSongBytes) {
                if (songByte.getTitle().toLowerCase().contains(lowerQuery) || songByte.getArtist().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(songByte);
                }
            }
        }
        this.songBytes.clear();
        this.songBytes.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void resetSongs(List<SongByte> newSongBytes) {
        this.originalSongBytes = new ArrayList(newSongBytes);
        this.songBytes.clear();
        this.songBytes.addAll(newSongBytes);
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
