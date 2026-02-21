package com.flowbeats.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.flowbeats.app.models.Playlist;
import com.flowbeats.app.models.Song;

import com.flowbeats.app.models.PlaylistSongCrossRef;

import com.flowbeats.app.models.User;

@Database(entities = { Song.class, Playlist.class, PlaylistSongCrossRef.class,
        User.class }, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract SongDao songDao();

    public abstract PlaylistDao playlistDao();

    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "flowbeats_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
