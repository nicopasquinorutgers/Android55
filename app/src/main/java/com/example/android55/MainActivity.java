package com.example.android55;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private User user;
    private AlbumAdapter adapter;
    private RecyclerView rvAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        user = DataManager.loadUser(this);
        rvAlbums = findViewById(R.id.rvAlbums);
        rvAlbums.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlbumAdapter(user.getAlbums(), new AlbumAdapter.OnAlbumClickListener() {
            @Override
            public void onAlbumClick(Album album, int position) {
                Intent i = new Intent(MainActivity.this, AlbumDetailActivity.class);
                i.putExtra("albumName", album.getName());
                startActivity(i);
            }
            @Override
            public void onAlbumLongClick(Album album, int position) {
                showAlbumOptionsDialog(album);
            }
        });
        rvAlbums.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddAlbum);
        fab.setOnClickListener(v -> showAddAlbumDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddAlbumDialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("New Album")
                .setMessage("Enter album name")
                .setView(input)
                .setPositiveButton("Create", (dlg, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                    } else if (user.addAlbum(new Album(name))) {
                        saveUser();
                        adapter.updateData(user.getAlbums());
                        Toast.makeText(this, "Album created", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Album already exists", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAlbumOptionsDialog(Album album) {
        String[] options = {"Rename","Delete"};
        new AlertDialog.Builder(this)
                .setTitle(album.getName())
                .setItems(options, (dlg, which) -> {
                    if (which == 0) showRenameAlbumDialog(album);
                    else           showDeleteAlbumDialog(album);
                })
                .show();
    }

    private void showRenameAlbumDialog(Album album) {
        final EditText input = new EditText(this);
        input.setText(album.getName());
        new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(input)
                .setPositiveButton("Rename", (dlg, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                    } else if (user.renameAlbum(album, newName)) {
                        saveUser();
                        adapter.updateData(user.getAlbums());
                        Toast.makeText(this, "Renamed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Name already in use", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAlbumDialog(Album album) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setMessage("Delete \"" + album.getName() + "\"?")
                .setPositiveButton("Delete", (dlg, which) -> {
                    user.removeAlbum(album);
                    saveUser();
                    adapter.updateData(user.getAlbums());
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveUser() {
        try {
            DataManager.saveUser(this, user);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = DataManager.loadUser(this);
        adapter.updateData(user.getAlbums());
    }
}