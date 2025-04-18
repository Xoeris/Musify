package com.xoeris.android.xesc.system.core.module.media.ux.audio;

import android.media.MediaMetadataRetriever;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class SongByte {
    private byte[] albumArt;
    private String artist;
    private String duration;
    private String path;
    private String title;

    public SongByte(String title, String artist, String path, String duration, String l) throws IOException {
        this.path = path;
        this.duration = duration;
        if (title == null || title.isEmpty() || title.equals(path)) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                try {
                    try {
                        retriever.setDataSource(path);
                        String metadataTitle = retriever.extractMetadata(7);
                        String metadataArtist = retriever.extractMetadata(2);
                        this.title = (metadataTitle == null || metadataTitle.isEmpty()) ? cleanupTitle(path) : metadataTitle;
                        this.artist = (metadataArtist == null || metadataArtist.isEmpty()) ? extractArtistFromFilename(path) : metadataArtist;
                        this.albumArt = retriever.getEmbeddedPicture();
                        retriever.release();
                        return;
                    } catch (Throwable th) {
                        try {
                            retriever.release();
                        } catch (Exception e) {
                            Log.e("SongByte", "Failed to release MediaMetadataRetriever", e);
                        }
                        throw th;
                    }
                } catch (Exception e2) {
                    Log.e("SongByte", "Failed to release MediaMetadataRetriever", e2);
                    return;
                }
            } catch (Exception e3) {
                Log.e("SongByte", "Failed to retrieve metadata for path: " + path, e3);
                this.title = cleanupTitle(path);
                this.artist = extractArtistFromFilename(path);
                retriever.release();
                return;
            }
        }
        this.title = title;
        this.artist = (artist == null || artist.isEmpty() || artist.equals("<unknown>")) ? "Unknown Artist" : artist;
        this.albumArt = extractAlbumArt(path);
    }

    private String cleanupTitle(String path) {
        String filename = path.substring(path.lastIndexOf("/") + 1).replace(".mp3", "");
        if (filename.contains(" - ")) {
            String[] parts = filename.split(" - ", 2);
            return cleanSuffix(parts[1].trim());
        }
        return cleanSuffix(filename);
    }

    private String extractArtistFromFilename(String path) {
        String filename = path.substring(path.lastIndexOf("/") + 1).replace(".mp3", "");
        if (filename.contains(" - ")) {
            String[] parts = filename.split(" - ", 2);
            return parts[0].trim();
        }
        return "Unknown Artist";
    }

    private String cleanSuffix(String title) {
        return title.replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "").replaceAll("_", " ").replaceAll("(?i)official", "").replaceAll("(?i)lyrics", "").replaceAll("(?i)soundtrack", "").replaceAll("\\s+", " ").trim();
    }

    private byte[] extractAlbumArt(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            Log.e("SongByte", "File does not exist or is not a valid file: " + path);
            return null;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            try {
                retriever.setDataSource(path);
                return retriever.getEmbeddedPicture();
            } finally {
                try {
                    retriever.release();
                } catch (Exception e) {
                    Log.e("SongByte", "Failed to release MediaMetadataRetriever", e);
                }
            }
        } catch (Exception e2) {
            Log.e("SongByte", "Failed to extract album art for path: " + path, e2);
            try {
                retriever.release();
            } catch (Exception e3) {
                Log.e("SongByte", "Failed to release MediaMetadataRetriever", e3);
            }
            return null;
        }
    }

    public byte[] getAlbumArt() {
        return this.albumArt;
    }

    public void setAlbumArt(byte[] albumArt) {
        this.albumArt = albumArt;
    }

    public boolean matchesSearch(String query) {
        String lowerQuery = query.toLowerCase();
        return getTitle().toLowerCase().contains(lowerQuery) || getArtist().toLowerCase().contains(lowerQuery);
    }

    public static List<SongByte> filterSongs(List<SongByte> songBytes, String query) {
        List<SongByte> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        for (SongByte songByte : songBytes) {
            if (songByte.matchesSearch(lowerQuery)) {
                filtered.add(songByte);
            }
        }
        return filtered;
    }

    public String getTitle() {
        return this.title;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getPath() {
        return this.path;
    }

    public String getDuration() {
        return this.duration;
    }
}
