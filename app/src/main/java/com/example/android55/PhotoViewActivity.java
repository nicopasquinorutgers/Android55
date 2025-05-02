package com.example.android55;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoViewActivity extends AppCompatActivity {
    private User user;
    private Album album;
    private List<Photo> photos;
    private int currentIndex;

    private ImageView imgFull;
    private Button btnPrev, btnNext;
    private ChipGroup chipGroupTags;
    private FloatingActionButton fabAddTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        user = DataManager.loadUser(this);
        String albumName = getIntent().getStringExtra("albumName");
        album = user.getAlbums().stream()
                .filter(a -> a.getName().equals(albumName))
                .findFirst()
                .orElse(null);
        if (album == null) { finish(); return; }
        photos = album.getPhotos();
        currentIndex = getIntent().getIntExtra("position", 0);
        if (currentIndex < 0 || currentIndex >= photos.size()) currentIndex = 0;

        Toolbar toolbar = findViewById(R.id.toolbarPhoto);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imgFull        = findViewById(R.id.imgFull);
        btnPrev        = findViewById(R.id.btnPrev);
        btnNext        = findViewById(R.id.btnNext);
        chipGroupTags  = findViewById(R.id.chipGroupTags);
        fabAddTag      = findViewById(R.id.fabAddTag);

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                refresh();
            }
        });
        btnNext.setOnClickListener(v -> {
            if (currentIndex < photos.size() - 1) {
                currentIndex++;
                refresh();
            }
        });

        fabAddTag.setOnClickListener(v -> showAddTagDialog());

        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_move_photo) {
            showMoveDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        displayPhoto(photos.get(currentIndex));
        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < photos.size() - 1);

        chipGroupTags.removeAllViews();
        for (Tag tag : photos.get(currentIndex).getTags()) {
            Chip chip = new Chip(this);
            chip.setText(tag.getKey() + ": " + tag.getValue());
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(c -> {
                photos.get(currentIndex).removeTag(tag);
                saveUser();
                refresh();
            });
            chipGroupTags.addView(chip);
        }
    }

    private void displayPhoto(Photo photo) {
        try {
            Uri uri = Uri.parse(photo.getFilePath());
            Bitmap bmp;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source src = ImageDecoder.createSource(getContentResolver(), uri);
                bmp = ImageDecoder.decodeBitmap(src);
            } else {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
            imgFull.setImageBitmap(bmp);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddTagDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Add Tag");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        Spinner spinnerKey = new Spinner(this);
        ArrayAdapter<CharSequence> keyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.tag_types,
                android.R.layout.simple_spinner_item
        );
        keyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKey.setAdapter(keyAdapter);
        layout.addView(spinnerKey);

        EditText inputValue = new EditText(this);
        inputValue.setHint("Value");
        layout.addView(inputValue);

        b.setView(layout);
        b.setPositiveButton("Add", (dlg, which) -> {
            String key = spinnerKey.getSelectedItem().toString();
            String value = inputValue.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(this, "Value is required", Toast.LENGTH_SHORT).show();
                return;
            }
            Photo p = photos.get(currentIndex);
            p.addTag(new Tag(key, value));
            saveUser();
            refresh();
        });
        b.setNegativeButton("Cancel", null);
        b.show();
    }

    private void saveUser() {
        try {
            DataManager.saveUser(this, user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMoveDialog() {
        List<Album> all = user.getAlbums();
        List<String> names = new ArrayList<>();
        for (Album a : all) {
            if (!a.getName().equals(album.getName())) {
                names.add(a.getName());
            }
        }
        if (names.isEmpty()) {
            Toast.makeText(this, "No other albums available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = names.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Move to…")
                .setItems(items, (dlg, which) -> {
                    String targetName = items[which];
                    Album target = all.stream()
                            .filter(a -> a.getName().equals(targetName))
                            .findFirst()
                            .orElse(null);
                    if (target == null) return;

                    Photo p = photos.get(currentIndex);
                    if (target.addPhoto(p)) {
                        album.removePhoto(p);
                        saveUser();

                        if (photos.isEmpty()) {
                            finish();
                            return;
                        }
                        if (currentIndex >= photos.size())
                            currentIndex = photos.size() - 1;

                        Toast.makeText(this, "Moved to " + targetName,
                                Toast.LENGTH_SHORT).show();
                        refresh();
                    } else {
                        Toast.makeText(this, "Photo already in " + targetName,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}