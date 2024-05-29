package com.example.a3uzik;

import android.content.Context;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import com.example.a3uzik.models.SongModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
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
            updateCount();
            if (currentSong.getUrl() != null) {
                MediaItem mediaItem = MediaItem.fromUri(currentSong.getUrl());
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.play();
                addToRecentlyPlayed(currentSong);
            }
        }
    }

    public static void updateCount() {
        if (currentSong != null && currentSong.getId() != null) {
            String id = currentSong.getId();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("songs")
                    .document(id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot document) {
                            Long latestCount = document.getLong("count");
                            if (latestCount == null) {
                                latestCount = 1L;
                            } else {
                                latestCount += 1;
                            }
                            firestore.collection("songs")
                                    .document(id)
                                    .update("count", latestCount);
                        }
                    });
        }
    }

    public static void updateLike() {
        if (currentSong != null && currentSong.getId() != null) {
            String id = currentSong.getId();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("songs")
                    .document(id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot document) {
                            String isHeartString = document.getString("isHeart");
                            boolean isLiked = Boolean.parseBoolean(isHeartString);
                            isLiked = !isLiked;
                            firestore.collection("songs")
                                    .document(id)
                                    .update("isHeart", String.valueOf(isLiked));
                        }
                    });
        }
    }

    public static void addToRecentlyPlayed(SongModel song) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String songId = song.getId();

        firestore.collection("library").document("recently_played_song").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> recentlyPlayed = (List<String>) documentSnapshot.get("songs");
                if (recentlyPlayed == null) {
                    recentlyPlayed = new ArrayList<>();
                }
                recentlyPlayed.remove(songId); // Remove the song if it already exists
                recentlyPlayed.add(0, songId);

                if (recentlyPlayed.size() > 10) {
                    recentlyPlayed.remove(recentlyPlayed.size() - 1);
                }
                firestore.collection("library").document("recently_played_song").update("songs", recentlyPlayed);
            }
        });
    }


}
