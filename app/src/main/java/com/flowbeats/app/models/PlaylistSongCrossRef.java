package com.flowbeats.app.models;

import androidx.room.Entity;

@Entity(tableName = "playlist_song_cross_ref", primaryKeys = { "playlistId", "songId" }, indices = {
        @androidx.room.Index(value = "songId") })
public class PlaylistSongCrossRef {
    public int playlistId;
    public int songId; // Matches Song.id (which is int)

    public PlaylistSongCrossRef(int playlistId, int songId) {
        this.playlistId = playlistId;
        this.songId = songId;
    }
}
