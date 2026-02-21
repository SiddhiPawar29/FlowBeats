package com.flowbeats.app.player;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.flowbeats.app.database.AppDatabase;
import com.flowbeats.app.database.PlaylistDao;
import com.flowbeats.app.models.Playlist;
import com.flowbeats.app.models.PlaylistSongCrossRef;
import com.flowbeats.app.models.PlaylistWithSongs;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistRepository {
    private PlaylistDao playlistDao;
    private com.flowbeats.app.database.SongDao songDao;
    private LiveData<List<Playlist>> allPlaylists;
    private ExecutorService executorService;

    public PlaylistRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        playlistDao = database.playlistDao();
        songDao = database.songDao();
        allPlaylists = playlistDao.getAllPlaylists();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Playlist>> getAllPlaylists() {
        return allPlaylists;
    }

    public LiveData<List<com.flowbeats.app.models.PlaylistWithSongCount>> getPlaylistsWithCounts() {
        return playlistDao.getPlaylistsWithCounts();
    }

    public void insert(Playlist playlist) {
        executorService.execute(() -> playlistDao.insertPlaylist(playlist));
    }

    public void delete(Playlist playlist) {
        executorService.execute(() -> playlistDao.deletePlaylist(playlist));
    }

    public void addSongToPlaylist(int playlistId, com.flowbeats.app.models.Song song) {
        executorService.execute(() -> {
            int songId = song.getId();

            // Check if song is already in DB (using path as unique key)
            com.flowbeats.app.models.Song existingSong = songDao.getSongByPath(song.getPath());
            if (existingSong != null) {
                songId = existingSong.getId();
            } else {
                // Insert new song
                song.setId(0); // Ensure creation of new ID
                long newId = songDao.insertSong(song);
                songId = (int) newId;
            }

            PlaylistSongCrossRef crossRef = new PlaylistSongCrossRef(playlistId, songId);
            try {
                playlistDao.insertPlaylistSongCrossRef(crossRef);
            } catch (Exception e) {
                // Ignore duplicate
            }
        });
    }

    public LiveData<PlaylistWithSongs> getPlaylistWithSongs(int playlistId) {
        return playlistDao.getPlaylistWithSongs(playlistId);
    }
}
