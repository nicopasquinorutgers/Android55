package com.example.android55;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    public interface OnAlbumClickListener {
        void onAlbumClick(Album album, int position);
        void onAlbumLongClick(Album album, int position);
    }

    private List<Album> albums;
    private final OnAlbumClickListener listener;

    public AlbumAdapter(List<Album> albums, OnAlbumClickListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    public void updateData(List<Album> albums) {
        this.albums = albums;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.name.setText(album.getName());
        holder.itemView.setOnClickListener(v -> listener.onAlbumClick(album, position));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onAlbumLongClick(album, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvAlbumName);
        }
    }
}
