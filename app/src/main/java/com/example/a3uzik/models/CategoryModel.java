package com.example.a3uzik.models;

import java.util.ArrayList;
import java.util.List;

public class CategoryModel {
    private String name;
    private String coverUrl;
    private List<String> songs;

    public CategoryModel() {
        this.name = "";
        this.coverUrl = "";
        this.songs = new ArrayList<>();
    }

    public CategoryModel(String name, String coverUrl, List<String> songs) {
        this.name = name;
        this.coverUrl = coverUrl;
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public List<String> getSongs() {
        return songs;
    }

    public void setSongs(List<String> songs) {
        this.songs = songs;
    }
}
