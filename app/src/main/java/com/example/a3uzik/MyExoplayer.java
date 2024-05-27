package com.example.a3uzik;

import android.content.Context;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import com.example.a3uzik.models.SongModel;
import java.util.List;

public class MyExoplayer {
    private static ExoPlayer exoPlayer = null;
    private static SongModel currentSong = null;
    private static List<SongModel> playlist = null;
    private static int currentIndex = -1;

    public static SongModel getCurrentSong() {
        return currentSong;
    }

    public static ExoPlayer getInstance(Context context) {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(context).build();
        }
        return exoPlayer;
    }

    public static void setPlaylist(List<SongModel> songs) {
        playlist = songs;
    }

    public static SongModel getNextSong() {
        if (playlist != null && !playlist.isEmpty()) {
            currentIndex = (currentIndex + 1) % playlist.size();
            return playlist.get(currentIndex);
        }
        return null;
    }

    public static SongModel getPreviousSong() {
        if (playlist != null && !playlist.isEmpty()) {
            currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
            return playlist.get(currentIndex);
        }
        return null;
    }

    public static void startPlaying(Context context, SongModel song) {
        getInstance(context);
        if (currentSong != song) {
            // New song
            currentSong = song;
            if (currentSong.getUrl() != null) {
                MediaItem mediaItem = MediaItem.fromUri(currentSong.getUrl());
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.play();
            }
        }
    }
}
