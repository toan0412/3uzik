package com.example.a3uzik;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import com.bumptech.glide.Glide;
import com.example.a3uzik.databinding.ActivityPlayerBinding;
import com.example.a3uzik.models.CategoryModel;
import com.example.a3uzik.models.SongModel;

public class PlayerActivity extends AppCompatActivity {

    private ActivityPlayerBinding binding;
    public static CategoryModel category;

    private ExoPlayer exoPlayer;
    private int currentSongIndex = 0;

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            PlayerActivity.this.showGif(isPlaying);
        }

        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_ENDED) {
                playNextSong();
            }
        }

    };

    @OptIn(markerClass = UnstableApi.class)
    private void playNextSong() {
            //Chuyeenr bai hat
    }


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (MyExoplayer.getCurrentSong() != null) {
            SongModel currentSong = MyExoplayer.getCurrentSong();
            binding.songTitleTextView.setText(currentSong.getTitle());
            binding.songSubtitleTextView.setText(currentSong.getSubtitle());
            Glide.with(binding.songCoverImageView.getContext()).load(currentSong.getCoverUrl())
                    .circleCrop()
                    .into(binding.songCoverImageView);
            Glide.with(binding.songGifImageView.getContext()).load(R.drawable.media_playing)
                    .circleCrop()
                    .into(binding.songGifImageView);

            exoPlayer = MyExoplayer.getInstance(this);
            binding.playerView.setPlayer(exoPlayer);
            binding.playerView.showController();
            exoPlayer.addListener(playerListener);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.removeListener(playerListener);
        }
    }

    private void showGif(boolean show) {
        if (show) {
            binding.songGifImageView.setVisibility(View.VISIBLE);
        } else {
            binding.songGifImageView.setVisibility(View.INVISIBLE);
        }
    }
}