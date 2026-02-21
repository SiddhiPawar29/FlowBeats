package com.flowbeats.app.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.flowbeats.app.models.Song;
import com.flowbeats.app.utils.MusicRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Song>> songs = new MutableLiveData<>();
    private final MusicRepository repository;
    private final ExecutorService executorService;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new MusicRepository();
        executorService = Executors.newSingleThreadExecutor();
        loadSongs();
    }

    public void loadSongs() {
        executorService.execute(() -> {
            List<Song> songList = repository.getAllSongs(getApplication());
            songs.postValue(songList);
        });
    }

    public LiveData<List<Song>> getSongs() {
        return songs;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
