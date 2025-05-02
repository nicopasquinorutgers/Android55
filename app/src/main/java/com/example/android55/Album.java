package com.example.android55;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Album implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Photo> photos;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public boolean addPhoto(Photo photo) {
        for (Photo p : photos) {
            if (p.getFilePath().equalsIgnoreCase(photo.getFilePath())) {
                return false;
            }
        }
        photos.add(photo);
        return true;
    }

    public void removePhoto(Photo photo) {
        photos.remove(photo);
    }
}