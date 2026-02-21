package com.flowbeats.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.flowbeats.app.models.Playlist;
import com.flowbeats.app.models.PlaylistSongCrossRef;
import com.flowbeats.app.models.PlaylistWithSongs;
import com.flowbeats.app.models.PlaylistWithSongCount;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Insert
    long insertPlaylist(Playlist playlist);

    @Delete
    void deletePlaylist(Playlist playlist);

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    LiveData<List<Playlist>> getAllPlaylists();

    @Query("SELECT playlists.*, COUNT(playlist_song_cross_ref.songId) as song_count FROM playlists LEFT JOIN playlist_song_cross_ref ON playlists.id = playlist_song_cross_ref.playlistId GROUP BY playlists.id ORDER BY playlists.createdAt DESC")
    LiveData<List<PlaylistWithSongCount>> getPlaylistsWithCounts();

    @Insert
    void insertPlaylistSongCrossRef(PlaylistSongCrossRef crossRef);

    // For getting songs in a playlist, we need a Relation POJO.
    // Let's assume we can also just get the cross refs and join manually if needed,
    // but the proper way is Transaction + Relation.

    // Simplification for now: Just get all playlists.
    // Songs will be fetched by joining manually or using a POJO if we have time.
    // Given the complexity constraints, let's create the POJO 'PlaylistWithSongs'
    // next.

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    LiveData<PlaylistWithSongs> getPlaylistWithSongs(int playlistId);
}
