package com.example.android55;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Album> albums;

    public User() {
        this.albums = new ArrayList<>();
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public boolean addAlbum(Album album) {
        for (Album a : albums) {
            if (a.getName().equalsIgnoreCase(album.getName())) {
                return false;
            }
        }
        albums.add(album);
        return true;
    }

    public void removeAlbum(Album album) {
        albums.remove(album);
    }

    public boolean renameAlbum(Album album, String newName) {
        String current = album.getName();
        if (current.equalsIgnoreCase(newName)) {
            return false;
        }
        for (Album a : albums) {
            if (a.getName().equalsIgnoreCase(newName)) {
                return false;
            }
        }
        album.setName(newName);
        return true;
    }
}