package com.example.a3uzik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CategoryAdapter categoryAdapter;
    private AlbumAdapter albumAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getCategories();
        setupSection();
        getAlbums();
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
                        binding.trendingSectionRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        binding.trendingSectionRecyclerView.setAdapter(new SectionSongListAdapter(section.getSongs()));
                        binding.trendingSectionMainLayout.setOnClickListener(v -> {
                            SongsListActivity.category = section;
                            startActivity(new Intent(MainActivity.this, SongsListActivity.class));
                        });
                    }
                });
    }
}
