package com.flowbeats.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "songs")
public class Song {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String artist;
    private String album;
    private String path;
    private long duration;
    private String albumArt;

    public Song() {
    }

    @androidx.room.Ignore
    public Song(String title, String artist, String album, String path, long duration) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.duration = duration;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public String getFormattedDuration() {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
