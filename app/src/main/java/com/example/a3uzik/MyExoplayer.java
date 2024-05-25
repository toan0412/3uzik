package com.example.a3uzik;

import android.content.Context;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import com.example.a3uzik.models.SongModel;

public class MyExoplayer {
    private static ExoPlayer exoPlayer = null;
    private static SongModel currentSong = null;

    public static SongModel getCurrentSong() {
        return currentSong;
    }

    public static ExoPlayer getInstance() {
        return exoPlayer;
    }

    public static void startPlaying(Context context, SongModel song) {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(context).build();
        }
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
