package com.flowbeats.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.flowbeats.app.models.Song;

import java.util.List;

@Dao
public interface SongDao {
    @Query("SELECT * FROM songs")
    List<Song> getAllSongs();

    @Query("SELECT * FROM songs WHERE path = :path LIMIT 1")
    Song getSongByPath(String path);

    @Query("SELECT * FROM songs WHERE id = :id")
    Song getSongById(int id);

    @Insert
    long insertSong(Song song);

    @Insert
    void insertAll(List<Song> songs);

    @Update
    void updateSong(Song song);

    @Delete
    void deleteSong(Song song);

    @Query("DELETE FROM songs")
    void deleteAllSongs();
}
