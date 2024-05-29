package com.example.a3uzik;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.example.a3uzik.databinding.ActivityPlayerBinding;
import com.example.a3uzik.models.SongModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    private ExoPlayer exoPlayer;
    private FirebaseFirestore db;
    private boolean isLooping;
    private boolean isShuffling;
    private Handler handler;
    private Runnable runnable;

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            PlayerActivity.this.showGif(isPlaying);
        }

        @Override
        public void onPlaybackStateChanged(int state) {
            switch (state) {
                case Player.STATE_ENDED:
                    playNextSong();
                    break;
                case Player.STATE_READY:
                    long durationMillis = exoPlayer.getDuration();
                    binding.durationTextView.setText(formatDuration(durationMillis));
                    break;
            }
        }


    };


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        exoPlayer = MyExoplayer.getInstance(this);
        SharedPreferences prefs = getSharedPreferences("PlayerPrefs", MODE_PRIVATE);
        isShuffling = prefs.getBoolean("isShuffling", false);
        isLooping = exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE;

        checkStateShuffleAndLoop();
        getAllSongs();
        playCurrentSong();
        setupButtonListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStateShuffleAndLoop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePreferences();
    }


    @OptIn(markerClass = UnstableApi.class)
    private void getAllSongs() {
        db.collection("songs").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<SongModel> playlist = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            SongModel song = document.toObject(SongModel.class);
                            playlist.add(song);
                        }
                        MyExoplayer.setPlaylist(playlist);

                    }
                });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void getRandomSongs() {
        db.collection("songs").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<SongModel> allSongs = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            SongModel song = document.toObject(SongModel.class);
                            allSongs.add(song);
                        }

                        int numberOfSongs = 20;
                        List<SongModel> randomSongs = getRandomSubset(allSongs, numberOfSongs);
                        MyExoplayer.setPlaylist(randomSongs);
                    }
                });
    }
    private List<SongModel> getRandomSubset(List<SongModel> allSongs, int numberOfSongs) {
        Collections.shuffle(allSongs);
        if (numberOfSongs > allSongs.size()) {
            numberOfSongs = allSongs.size();
        }
        return new ArrayList<>(allSongs.subList(0, numberOfSongs));
    }


    private void setupButtonListeners() {
        binding.loopBtn.setOnClickListener(v -> toggleLoop());
        binding.previousBtn.setOnClickListener(v -> playPreviousSong());
        binding.pauseBtn.setOnClickListener(v -> togglePlayPause());
        binding.nextBtn.setOnClickListener(v -> playNextSong());
        binding.shuffleBtn.setOnClickListener(v -> toggleShuffle());
        binding.heartBtn.setOnClickListener(v -> toggleHeart());
        binding.backBtn.setOnClickListener(v -> finish());
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && exoPlayer != null) {
                    long newPosition = (long) progress * exoPlayer.getDuration() / seekBar.getMax();
                    exoPlayer.seekTo(newPosition);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }


    private void toggleShuffle() {
        if (exoPlayer != null) {
            isShuffling = !isShuffling;
            if (isShuffling) {
                isLooping = false;
                getRandomSongs();
            } else {
                getAllSongs();
            }
            checkStateShuffleAndLoop();
            savePreferences();
        }
    }

    private void toggleLoop() {
        if (exoPlayer != null) {
            isLooping = !isLooping;
            if (isLooping) {
                isShuffling = false;
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            } else {
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
            }
            checkStateShuffleAndLoop();
            savePreferences();
        }
    }

    private void toggleHeart(){
        SongModel currentSong = MyExoplayer.getCurrentSong();
        if(currentSong.isHeart()){
            currentSong.setHeart(false);
            binding.heartBtn.setImageResource(R.drawable.ic_heart);
        }else {
            currentSong.setHeart(true);
            binding.heartBtn.setImageResource(R.drawable.ic_hearted);
        }
        MyExoplayer.updateLike();
    }



    private void togglePlayPause() {
        if (exoPlayer != null) {
            if (exoPlayer.isPlaying()) {
                binding.pauseBtn.setImageResource(R.drawable.ic_resume);
                exoPlayer.pause();
            } else {
                binding.pauseBtn.setImageResource(R.drawable.ic_pause);
                exoPlayer.play();
            }
            checkStateShuffleAndLoop();
            savePreferences();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void playSong(SongModel song) {
        if (song != null) {
            MyExoplayer.startPlaying(this, song);
            binding.songTitleTextView.setText(song.getTitle());
            binding.songSubtitleTextView.setText(song.getSubtitle());
            binding.heartBtn.setImageResource(song.isHeart() ? R.drawable.ic_hearted : R.drawable.ic_heart);
            Toast.makeText(this,"check hearted" + song.isHeart(), Toast.LENGTH_SHORT).show();
            Glide.with(binding.songCoverImageView.getContext()).load(song.getCoverUrl())
                    .circleCrop()
                    .into(binding.songCoverImageView);
            Glide.with(binding.songGifImageView.getContext()).load(R.drawable.media_playing)
                    .circleCrop()
                    .into(binding.songGifImageView);
        }
    }

    private void playCurrentSong() {
        SongModel currentSong = MyExoplayer.getCurrentSong();
        playSong(currentSong);
        if (currentSong != null) {
            exoPlayer = MyExoplayer.getInstance(this);
            exoPlayer.addListener(playerListener);

            if (exoPlayer.getPlaybackState() == Player.STATE_READY) {
                binding.durationTextView.setText(formatDuration(exoPlayer.getDuration()));
            }

            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    if(exoPlayer != null){
                        long currentPosition = exoPlayer.getCurrentPosition();
                        long duration = exoPlayer.getDuration();
                        int progress = (int) (currentPosition * 100 / duration);
                        binding.seekBar.setProgress(progress);
                        binding.progressTextView.setText(formatDuration(currentPosition));
                    }
                    handler.postDelayed(this, 1000);
                }
            };
            handler.postDelayed(runnable, 0);
        }
    }



    private void playNextSong() {
        SongModel nextSong;
        if(isLooping){
            nextSong = MyExoplayer.getCurrentSong();
        }else {
            nextSong = MyExoplayer.getNextSong();
        }
        playSong(nextSong);
    }

    private void playPreviousSong() {
        SongModel previousSong;
        if(isLooping){
            previousSong = MyExoplayer.getCurrentSong();
        }else {
            previousSong = MyExoplayer.getPreviousSong();
        }
        playSong(previousSong);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.removeListener(playerListener);
        }
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void showGif(boolean show) {
        if (show) {
            binding.songGifImageView.setVisibility(View.VISIBLE);
        } else {
            binding.songGifImageView.setVisibility(View.INVISIBLE);
        }
    }

    private String formatDuration(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void checkStateShuffleAndLoop() {
        if (isShuffling) {
            binding.shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
        } else {
            binding.shuffleBtn.setImageResource(R.drawable.ic_shuffle);
        }
        if (isLooping) {
            binding.loopBtn.setImageResource(R.drawable.ic_loop_on);
        } else {
            binding.loopBtn.setImageResource(R.drawable.ic_loop);
        }
    }

    private void savePreferences() {
        SharedPreferences prefs = getSharedPreferences("PlayerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isShuffling", isShuffling);
        editor.putBoolean("isLooping", isLooping);
        editor.apply();
    }


}
