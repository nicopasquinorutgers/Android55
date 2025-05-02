package com.example.android55;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {
    private AutoCompleteTextView actvTagType, actvTagValue;
    private RadioGroup       rgAndOr;
    private Button           btnSearch;
    private RecyclerView     rvResults;

    private User                                user;
    private PhotoAdapter                        adapter;
    private PhotoAdapter.OnPhotoClickListener   photoListener;
    private final List<SearchResult>            results = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbarSearch);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        actvTagType  = findViewById(R.id.actvTagType);
        actvTagValue = findViewById(R.id.actvTagValue);
        rgAndOr      = findViewById(R.id.rgAndOr);
        btnSearch    = findViewById(R.id.btnSearch);
        rvResults    = findViewById(R.id.rvResults);

        user = DataManager.loadUser(this);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.tag_types,
                android.R.layout.simple_dropdown_item_1line
        );
        actvTagType.setAdapter(typeAdapter);
        actvTagType.setThreshold(1);

        actvTagType.setOnClickListener(v -> actvTagType.showDropDown());
        actvTagType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actvTagType.showDropDown();
        });

        actvTagType.setOnItemClickListener((parent, view, pos, id) -> populateValueSuggestions());

        photoListener = new PhotoAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoClick(int position) {
                SearchResult sr = results.get(position);
                Intent i = new Intent(SearchActivity.this, PhotoViewActivity.class);
                i.putExtra("albumName", sr.album.getName());
                i.putExtra("position", sr.indexInAlbum);
                startActivity(i);
            }
            @Override public void onPhotoLongClick(int position) {
            }
        };

        rvResults.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new PhotoAdapter(new ArrayList<>(), photoListener);
        rvResults.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> doSearch());
    }

    private void populateValueSuggestions() {
        String selectedType = actvTagType.getText().toString();
        Set<String> values = new HashSet<>();
        for (Album a : user.getAlbums()) {
            for (Photo p : a.getPhotos()) {
                for (Tag t : p.getTags()) {
                    if (t.getKey().equalsIgnoreCase(selectedType)) {
                        values.add(t.getValue());
                    }
                }
            }
        }
        ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(values)
        );
        actvTagValue.setAdapter(valueAdapter);
        actvTagValue.setThreshold(1);
    }

    private void doSearch() {
        String type = actvTagType.getText().toString().trim();
        if (type.isEmpty()) {
            actvTagType.setError("Select a tag type");
            return;
        }
        String raw = actvTagValue.getText().toString().trim();
        if (raw.isEmpty()) {
            actvTagValue.setError("Enter at least one tag value");
            return;
        }
        String[] terms = raw.split("\\s*,\\s*");
        boolean useAnd = ((RadioButton)findViewById(R.id.rbAnd)).isChecked();

        results.clear();
        for (Album a : user.getAlbums()) {
            List<Photo> photos = a.getPhotos();
            for (int idx = 0; idx < photos.size(); idx++) {
                Photo p = photos.get(idx);
                List<String> matchedValues = new ArrayList<>();
                for (Tag t : p.getTags()) {
                    if (t.getKey().equalsIgnoreCase(type)) {
                        for (String term : terms) {
                            if (t.getValue().toLowerCase().startsWith(term.toLowerCase())) {
                                matchedValues.add(term);
                            }
                        }
                    }
                }
                boolean qualifies = useAnd
                        ? matchedValues.containsAll(List.of(terms))
                        : !matchedValues.isEmpty();
                if (qualifies) {
                    results.add(new SearchResult(a, p, idx));
                }
            }
        }

        List<Photo> found = new ArrayList<>();
        for (SearchResult sr : results) found.add(sr.photo);
        adapter = new PhotoAdapter(found, photoListener);
        rvResults.setAdapter(adapter);
    }

    private static class SearchResult {
        final Album album;
        final Photo photo;
        final int   indexInAlbum;
        SearchResult(Album a, Photo p, int idx) {
            album = a; photo = p; indexInAlbum = idx;
        }
    }
}