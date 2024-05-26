package com.example.a3uzik.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.a3uzik.MyExoplayer;
import com.example.a3uzik.PlayerActivity;
import com.example.a3uzik.databinding.SectionSongListRecyclerRowBinding;
import com.example.a3uzik.models.SongModel;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class SectionSongListAdapter extends RecyclerView.Adapter<SectionSongListAdapter.MyViewHolder> {

    private final List<String> songIdList;

    public SectionSongListAdapter(List<String> songIdList) {
        this.songIdList = songIdList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final SectionSongListRecyclerRowBinding binding;

        public MyViewHolder(SectionSongListRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(String songId) {
            FirebaseFirestore.getInstance().collection("songs")
                    .document(songId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        SongModel song = documentSnapshot.toObject(SongModel.class);
                        if (song != null) {
                            binding.songTitleTextView.setText(song.getTitle().toUpperCase());
                            binding.songSubtitleTextView.setText(song.getSubtitle());
                            Glide.with(binding.songCoverImageView.getContext()).load(song.getCoverUrl())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(4)))
                                    .into(binding.songCoverImageView);
                            binding.getRoot().setOnClickListener(v -> {
                                MyExoplayer.startPlaying(v.getContext(), song);
                                v.getContext().startActivity(new Intent(v.getContext(), PlayerActivity.class));
                            });
                        }
                    });
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SectionSongListRecyclerRowBinding binding = SectionSongListRecyclerRowBinding.inflate(inflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bindData(songIdList.get(position));
    }

    @Override
    public int getItemCount() {
        return songIdList.size();
    }
}
