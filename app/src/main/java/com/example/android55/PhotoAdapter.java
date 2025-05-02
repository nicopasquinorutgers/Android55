package com.example.android55;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PhotoAdapter
        extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    public interface OnPhotoClickListener {
        void onPhotoClick(int position);
        void onPhotoLongClick(int position);
    }

    private final List<Photo> photos;
    private final OnPhotoClickListener listener;

    public PhotoAdapter(List<Photo> photos, OnPhotoClickListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        Uri uri = Uri.parse(photos.get(position).getFilePath());
        try {
            Bitmap bmp;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source src = ImageDecoder.createSource(
                        holder.itemView.getContext().getContentResolver(), uri);
                bmp = ImageDecoder.decodeBitmap(src);
            } else {
                try (InputStream in = holder.itemView.getContext()
                        .getContentResolver()
                        .openInputStream(uri)) {
                    bmp = BitmapFactory.decodeStream(in);
                }
            }

            if (bmp != null) {
                holder.img.setImageBitmap(bmp);
            } else {
                holder.img.setImageResource(android.R.drawable.ic_menu_report_image);
            }

        } catch (IOException e) {
            e.printStackTrace();
            holder.img.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        holder.itemView.setOnClickListener(v ->
                listener.onPhotoClick(position)
        );

        holder.itemView.setOnLongClickListener(v -> {
            listener.onPhotoLongClick(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgThumbnail);
        }
    }
}