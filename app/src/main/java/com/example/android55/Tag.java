package com.example.android55;

import java.io.Serializable;
import java.util.Objects;

public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    private String key;
    private String value;

    public Tag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return key.equalsIgnoreCase(tag.key) &&
                value.equalsIgnoreCase(tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key.toLowerCase(), value.toLowerCase());
    }
}