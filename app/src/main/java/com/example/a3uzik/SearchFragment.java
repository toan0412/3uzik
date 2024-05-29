package com.example.a3uzik;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.a3uzik.adapter.SongAdapter;
import com.example.a3uzik.adapter.SongsListAdapter;
import com.example.a3uzik.databinding.FragmentSearchBinding;
import com.example.a3uzik.models.CategoryModel;
import com.example.a3uzik.models.SongModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private final List<SongModel> mListSong = new ArrayList<>();
    private ExoPlayer exoPlayer;
    private SongAdapter songAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        exoPlayer = MyExoplayer.getInstance(requireContext());
        exoPlayer.addListener(playerListener);

        songAdapter = new SongAdapter(new ArrayList<>());
        fetchSongs();

        binding.searchView.clearFocus();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fileList(newText);
                return false;
            }

            private void fileList(String newText) {
                List<SongModel> filteredList = new ArrayList<>();
                for (SongModel song : mListSong) {
                    if (song.getTitle().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(song);
                    }
                }

                songAdapter.updateList(filteredList);
            }
        });
        binding.optionBtn.setOnClickListener(v -> showPopupMenu());

        binding.songsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        songAdapter = new SongAdapter(mListSong);
        binding.songsListRecyclerView.setAdapter(songAdapter);
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

    private void fetchSongs() {
        FirebaseFirestore.getInstance().collection("songs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        SongModel song = document.toObject(SongModel.class);
                        if (song != null) {
                            mListSong.add(song);
                            songAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
