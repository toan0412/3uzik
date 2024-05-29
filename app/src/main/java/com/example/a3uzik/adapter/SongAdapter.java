package com.example.a3uzik.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.a3uzik.MyExoplayer;
import com.example.a3uzik.PlayerActivity;
import com.example.a3uzik.databinding.SongListItemItemRecyclerRowBinding;
import com.example.a3uzik.models.SongModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    private List<SongModel> mSongList;

    public SongAdapter(List<SongModel> mSongList) {
        this.mSongList = mSongList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        //Su dung lai song_list_item_recycler_row.xml de bind du lieu
        private final SongListItemItemRecyclerRowBinding binding;

        public MyViewHolder(SongListItemItemRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(SongModel song) {
                            binding.songTitleTextView.setText(song.getTitle());
                            binding.songSubtitleTextView.setText(song.getSubtitle());
                            Glide.with(binding.songCoverImageView.getContext()).load(song.getCoverUrl())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(32)))
                                    .into(binding.songCoverImageView);
            binding.getRoot().setOnClickListener(v -> {
                MyExoplayer.startPlaying(binding.getRoot().getContext(), song);
                v.getContext().startActivity(new Intent(v.getContext(), PlayerActivity.class));
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SongListItemItemRecyclerRowBinding binding = SongListItemItemRecyclerRowBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData(mSongList.get(position));
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    public void updateList(List<SongModel> filteredList) {
        mSongList = filteredList;
        notifyDataSetChanged();
    }
}
