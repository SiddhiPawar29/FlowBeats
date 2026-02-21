package com.flowbeats.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "playlists")
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public long createdAt;

    // For simplicity, let's keep it simple first.
    // If we want to store songs, we can use a CrossRef, but the plan mentioned
    // PlaylistSongCrossRef.
    // However, for a simple implementation, a JSON string of song paths or IDs
    // could work,
    // but CrossRef is cleaner. Let's stick to the plan: Playlist entity + CrossRef.

    public Playlist(String name) {
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Placeholder for song count
    public int getSongCount() {
        return 0;
    }
}
