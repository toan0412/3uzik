package com.example.a3uzik.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.a3uzik.SongsListActivity;
import com.example.a3uzik.databinding.CategoryItemRecyclerRowBinding;
import com.example.a3uzik.databinding.LibraryItemRecyclerRowBinding;
import com.example.a3uzik.models.CategoryModel;
import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.MyViewHolder> {

    private final List<CategoryModel> libraryList;

    public LibraryAdapter(List<CategoryModel> libraryList) {
        this.libraryList = libraryList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final LibraryItemRecyclerRowBinding binding;

        public MyViewHolder(LibraryItemRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(CategoryModel album) {
            Glide.with(binding.libraryItemCoverImageView.getContext()).load(album.getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(32)))
                    .into(binding.libraryItemCoverImageView);
            binding.libraryItemTitleTextView.setText(album.getName());
            binding.libraryItemSubtitleTextView.setText("Danh sách phát");
            Context context = binding.getRoot().getContext();
            binding.getRoot().setOnClickListener(v -> {
                SongsListActivity.category = album;
                context.startActivity(new Intent(context, SongsListActivity.class));
            });
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        LibraryItemRecyclerRowBinding binding = LibraryItemRecyclerRowBinding.inflate(layoutInflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bindData(libraryList.get(position));
    }

    @Override
    public int getItemCount() {
        return libraryList.size();
    }
}
