package com.example.a3uzik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.a3uzik.adapter.SongsListAdapter;
import com.example.a3uzik.databinding.ActivitySongsListBinding;
import com.example.a3uzik.models.CategoryModel;
import com.example.a3uzik.models.SongModel;

public class SongsListActivity extends AppCompatActivity {

    public static CategoryModel category;

    private ActivitySongsListBinding binding;
    private SongsListAdapter songsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySongsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.nameTextView.setText(category.getName());
        Glide.with(binding.coverImageView.getContext()).load(category.getCoverUrl())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(32)))
                .into(binding.coverImageView);

        setupSongsListRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPlayerView();
    }

    private void setupSongsListRecyclerView() {
        songsListAdapter = new SongsListAdapter(category.getSongs());
        binding.songsListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.songsListRecyclerView.setAdapter(songsListAdapter);
    }

    private void showPlayerView() {
        binding.playerView.setOnClickListener(v -> startActivity(new Intent(this, PlayerActivity.class)));
        SongModel currentSong = MyExoplayer.getCurrentSong();
        if (currentSong != null) {
            binding.playerView.setVisibility(View.VISIBLE);
            binding.songTitleTextView.setText(currentSong.getTitle());
            binding.songSubtitleTextView.setText(currentSong.getSubtitle());
            Glide.with(binding.songCoverImageView.getContext()).load(currentSong.getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(32)))
                    .into(binding.songCoverImageView);
        } else {
            binding.playerView.setVisibility(View.GONE);
        }
    }

}
