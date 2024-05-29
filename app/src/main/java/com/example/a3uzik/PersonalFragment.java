package com.example.a3uzik;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.a3uzik.adapter.LibraryAdapter;
import com.example.a3uzik.adapter.SectionSongListAdapter;
import com.example.a3uzik.databinding.FragmentPersonalBinding;
import com.example.a3uzik.models.CategoryModel;
import com.example.a3uzik.models.SongModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalFragment extends Fragment {

    private FragmentPersonalBinding binding;
    private LibraryAdapter libraryAdapter;
    private ExoPlayer exoPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPersonalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        exoPlayer = MyExoplayer.getInstance(requireContext());
        exoPlayer.addListener(playerListener);

        updateMostListenedSongsInLibrary();
        updateLikedSong();
        setupSection();

        binding.optionBtn.setOnClickListener(v -> showPopupMenu());
    }

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_ENDED) {
                playNextSong();
                showPlayerView();
            }
        }
    };

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(requireContext(), binding.optionBtn);
        popupMenu.getMenuInflater().inflate(R.menu.option_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.logout) {
                logout();
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        showPlayerView();
    }

    private void showPlayerView() {
        binding.playerView.setOnClickListener(v -> startActivity(new Intent(getActivity(), PlayerActivity.class)));
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

    // Library

    private void getLibrary() {
        FirebaseFirestore.getInstance().collection("library")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CategoryModel> categoryList = queryDocumentSnapshots.toObjects(CategoryModel.class);
                    setupCategoryRecyclerView(categoryList);
                });
    }

    private void setupCategoryRecyclerView(List<CategoryModel> categoryList) {
        libraryAdapter = new LibraryAdapter(categoryList);
        binding.libraryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.libraryRecyclerView.setAdapter(libraryAdapter);
    }


    private void updateMostListenedSongsInLibrary() {
        FirebaseFirestore.getInstance().collection("songs")
                .orderBy("count", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SongModel> mostListenedSongs = queryDocumentSnapshots.toObjects(SongModel.class);
                    List<String> mostListenedSongIds = new ArrayList<>();
                    for (SongModel song : mostListenedSongs) {
                        mostListenedSongIds.add(song.getId());
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("songs", mostListenedSongIds);

                    FirebaseFirestore.getInstance().collection("library")
                            .document("most_listened_song")
                            .update(data)
                            .addOnSuccessListener(aVoid -> {
                                getLibrary();
                            });
                });

    }

    private void updateLikedSong() {
        FirebaseFirestore.getInstance().collection("songs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> likedSongIds = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String isHeartString = document.getString("isHeart");
                        if (isHeartString != null && Boolean.parseBoolean(isHeartString)) {
                            likedSongIds.add(document.getId());
                        }
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("songs", likedSongIds);

                    FirebaseFirestore.getInstance().collection("library")
                            .document("favorite_song")
                            .update(data)
                            .addOnSuccessListener(aVoid -> {
                                getLibrary();
                            });

                });

    }

    private void setupSection() {
        FirebaseFirestore.getInstance().collection("library")
                .document("recently_played_song")
                .get().addOnSuccessListener(documentSnapshot -> {
                    CategoryModel section = documentSnapshot.toObject(CategoryModel.class);
                    if (section != null) {
                        binding.recentlyPlayedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        binding.recentlyPlayedRecyclerView.setAdapter(new SectionSongListAdapter(section.getSongs()));
                        binding.recentlyPlayedMainLayout.setOnClickListener(v -> {
                            SongsListActivity.category = section;
                            startActivity(new Intent(getContext(), SongsListActivity.class));
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
        MyExoplayer.startPlaying(requireContext(), nextSong);
    }
}
