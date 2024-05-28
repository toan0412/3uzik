package com.example.a3uzik;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.a3uzik.adapter.AlbumAdapter;
import com.example.a3uzik.adapter.CategoryAdapter;
import com.example.a3uzik.adapter.SectionSongListAdapter;
import com.example.a3uzik.databinding.ActivityMainBinding;
import com.example.a3uzik.models.CategoryModel;
import com.example.a3uzik.models.SongModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CategoryAdapter categoryAdapter;
    private AlbumAdapter albumAdapter;
    private ExoPlayer exoPlayer;

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_ENDED) {
                playNextSong();
                showPlayerView();
            }
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        exoPlayer = MyExoplayer.getInstance(this);
        exoPlayer.addListener(playerListener);
        getCategories();
        setupSection();
        getAlbums();
        binding.optionBtn.setOnClickListener(v -> showPopupMenu());
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, binding.optionBtn);
        popupMenu.getMenuInflater().inflate(R.menu.option_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.logout) {
                    logout();
                    return true;
                } else {
                    return false;
                }
            }
        });

        popupMenu.show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPlayerView();
    }


    private void showPlayerView() {
        binding.playerView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PlayerActivity.class)));
        SongModel currentSong = MyExoplayer.getCurrentSong();
        if (currentSong != null) {
            binding.playerView.setVisibility(View.VISIBLE);
            binding.songTitleTextView.setText(currentSong.getTitle());
            binding.songSubtitleTextView.setText(currentSong.getSubtitle());
            Glide.with(binding.songCoverImageView).load(currentSong.getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(32)))
                    .into(binding.songCoverImageView);
            binding.playBtn.setOnClickListener(v -> togglePlayPause());
        } else {
            binding.playerView.setVisibility(View.GONE);
        }
    }

    // Categories

    private void getCategories() {
        FirebaseFirestore.getInstance().collection("category")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CategoryModel> categoryList = queryDocumentSnapshots.toObjects(CategoryModel.class);
                    setupCategoryRecyclerView(categoryList);
                });
    }

    private void getAlbums() {
        FirebaseFirestore.getInstance().collection("albums")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CategoryModel> albumsList = queryDocumentSnapshots.toObjects(CategoryModel.class);
                    setupAlbumsRecyclerView(albumsList);
                });
    }

    private void setupCategoryRecyclerView(List<CategoryModel> categoryList) {
        categoryAdapter = new CategoryAdapter(categoryList);
        binding.categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupAlbumsRecyclerView(List<CategoryModel> albumList) {
        albumAdapter = new AlbumAdapter(albumList);
        binding.albumsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.albumsRecyclerView.setAdapter(albumAdapter);
    }

    // Sections

    private void setupSection() {
        FirebaseFirestore.getInstance().collection("sections")
                .document("trending_section")
                .get().addOnSuccessListener(documentSnapshot -> {
                    CategoryModel section = documentSnapshot.toObject(CategoryModel.class);
                    if (section != null) {
                        binding.trendingSectionTitle.setText(section.getName());
                        GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 2, LinearLayoutManager.VERTICAL, false);
                        binding.trendingSectionRecyclerView.setLayoutManager(layoutManager);
                        binding.trendingSectionRecyclerView.setAdapter(new SectionSongListAdapter(section.getSongs()));
                        binding.trendingSectionMainLayout.setOnClickListener(v -> {
                            SongsListActivity.category = section;
                            startActivity(new Intent(MainActivity.this, SongsListActivity.class));
                        });
                    }
                });
    }

    private void togglePlayPause() {
        if (exoPlayer != null) {
            if (exoPlayer.isPlaying()) {
                binding.playBtn.setImageResource(R.drawable.ic_play_white);
                exoPlayer.pause();
            } else {
                binding.playBtn.setImageResource(R.drawable.ic_pause_white);
                exoPlayer.play();
            }
        }
    }

    private void playNextSong() {
        SongModel nextSong = MyExoplayer.getNextSong();
        MyExoplayer.startPlaying(this, nextSong);
    }
}
