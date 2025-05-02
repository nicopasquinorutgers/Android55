// AlbumDetailActivity.java
package com.example.android55;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;

public class AlbumDetailActivity extends AppCompatActivity {
    private static final int SPAN_COUNT = 3;

    private User user;
    private Album album;
    private String albumName;
    private PhotoAdapter adapter;
    private RecyclerView rvPhotos;

    private final PhotoAdapter.OnPhotoClickListener photoClickListener =
            new PhotoAdapter.OnPhotoClickListener() {
                @Override
                public void onPhotoClick(int position) {
                    Intent i = new Intent(AlbumDetailActivity.this, PhotoViewActivity.class);
                    i.putExtra("albumName", albumName);
                    i.putExtra("position", position);
                    startActivity(i);
                }
                @Override
                public void onPhotoLongClick(int position) {
                    showDeletePhotoDialog(position);
                }
            };

    private final ActivityResultLauncher<Intent> pickPhotoLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) {
                                getContentResolver().takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                                Photo newPhoto = new Photo(uri.toString());
                                if (album.addPhoto(newPhoto)) {
                                    saveUser();
                                    adapter.notifyItemInserted(album.getPhotos().size() - 1);
                                }
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        user = DataManager.loadUser(this);
        albumName = getIntent().getStringExtra("albumName");
        album = findAlbum();
        if (album == null) { finish(); return; }
        toolbar.setTitle(albumName);

        rvPhotos = findViewById(R.id.rvPhotos);
        rvPhotos.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        adapter = new PhotoAdapter(album.getPhotos(), photoClickListener);
        rvPhotos.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddPhoto);
        fab.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.setType("image/*");
            pick.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            );
            pickPhotoLauncher.launch(pick);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        user  = DataManager.loadUser(this);
        album = findAlbum();
        if (album == null) { finish(); return; }

        Log.d("AlbumDetail", "Loaded album '" + albumName + "' with " + album.getPhotos().size() + " photos");

        adapter = new PhotoAdapter(album.getPhotos(), photoClickListener);
        rvPhotos.setAdapter(adapter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        saveUser();
    }

    private Album findAlbum() {
        for (Album a : user.getAlbums()) {
            if (a.getName().equals(albumName)) return a;
        }
        return null;
    }

    private void showDeletePhotoDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove this photo?")
                .setMessage("It will be permanently removed from this album.")
                .setPositiveButton("Remove", (dlg, which) -> {
                    Photo toRemove = album.getPhotos().get(position);
                    album.removePhoto(toRemove);
                    saveUser();
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveUser() {
        try {
            DataManager.saveUser(this, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}