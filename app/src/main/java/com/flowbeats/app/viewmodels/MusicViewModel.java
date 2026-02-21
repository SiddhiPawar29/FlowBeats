package com.flowbeats.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.flowbeats.app.models.Song;
import com.flowbeats.app.player.MusicRepository;

import com.flowbeats.app.player.PlaylistRepository;
import com.flowbeats.app.models.Playlist;
import com.flowbeats.app.models.PlaylistWithSongs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicViewModel extends AndroidViewModel {
    private final MusicRepository repository;
    private final PlaylistRepository playlistRepository;
    private final MutableLiveData<List<Song>> allSongs = new MutableLiveData<>();
    private final MutableLiveData<List<Song>> searchResults = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MusicViewModel(@NonNull Application application) {
        super(application);
        repository = new MusicRepository(application);
        playlistRepository = new PlaylistRepository(application);
        loadSongs();
    }

    public LiveData<List<Playlist>> getAllPlaylists() {
        return playlistRepository.getAllPlaylists();
    }

    public LiveData<List<com.flowbeats.app.models.PlaylistWithSongCount>> getPlaylistsWithCounts() {
        return playlistRepository.getPlaylistsWithCounts();
    }

    public void createPlaylist(String name) {
        playlistRepository.insert(new Playlist(name));
    }

    public void addSongToPlaylist(int playlistId, Song song) {
        playlistRepository.addSongToPlaylist(playlistId, song);
    }

    public LiveData<PlaylistWithSongs> getPlaylistWithSongs(int playlistId) {
        return playlistRepository.getPlaylistWithSongs(playlistId);
    }

    public void loadSongs() {
        executorService.execute(() -> {
            List<Song> songs = repository.getAllSongs();
            allSongs.postValue(songs);
        });
    }

    public LiveData<List<Song>> getAllSongs() {
        return allSongs;
    }

    public LiveData<List<Song>> getSearchResults() {
        return searchResults;
    }

    public void search(String query) {
        if (query == null || query.isEmpty()) {
            searchResults.setValue(new ArrayList<>());
            return;
        }

        List<Song> currentSongs = allSongs.getValue();
        if (currentSongs == null)
            return;

        List<Song> filtered = new ArrayList<>();
        for (Song song : currentSongs) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    song.getArtist().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(song);
            }
        }
        searchResults.setValue(filtered);
    }
}
