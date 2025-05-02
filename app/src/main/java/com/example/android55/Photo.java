package com.example.android55;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String filePath;
    private LocalDateTime dateTaken;
    private String caption;
    private List<Tag> tags;

    public Photo(String filePath) {
        this.filePath = filePath;
        File file = new File(filePath);
        this.dateTaken = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(file.lastModified()),
                ZoneId.systemDefault()
        );
        this.tags = new ArrayList<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDateTime getDateTaken() {
        return dateTaken;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        for (Tag t : tags) {
            if (t.getKey().equalsIgnoreCase(tag.getKey()) &&
                    t.getValue().equalsIgnoreCase(tag.getValue())) {
                return;
            }
        }
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }
}