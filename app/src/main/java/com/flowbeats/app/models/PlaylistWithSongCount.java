package com.flowbeats.app.models;

import androidx.room.Embedded;
import androidx.room.ColumnInfo;

public class PlaylistWithSongCount {
    @Embedded
    public Playlist playlist;

    @ColumnInfo(name = "song_count")
    public int songCount;
}
