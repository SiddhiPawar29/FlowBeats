package com.flowbeats.app.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.camera.view.PreviewView;
import com.flowbeats.app.gesture.GestureDetector;
import com.flowbeats.app.gesture.GestureListener;
import com.flowbeats.app.gesture.HandGestureClassifier;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;

import com.flowbeats.app.R;
import com.flowbeats.app.adapters.SongAdapter;
import com.flowbeats.app.models.Song;
import com.flowbeats.app.player.MusicPlayer;
import com.flowbeats.app.viewmodels.MusicViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity implements GestureListener {
    private static final int CAMERA_PERMISSION_CODE = 100;

    private RecyclerView recyclerView;
    private TextView tvPlaylistTitle;
    private FloatingActionButton fabAddSong;
    private SongAdapter adapter;
    private MusicViewModel viewModel;
    private int playlistId;
    private String playlistName;

    // Components for Gestures
    private PreviewView cameraPreview;
    private TextView tvGestureIndicator;
    private GestureDetector gestureDetector;
    private MusicPlayer musicPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        playlistId = getIntent().getIntExtra("playlist_id", -1);
        playlistName = getIntent().getStringExtra("playlist_name");

        if (playlistId == -1) {
            finish();
            return;
        }

        musicPlayer = MusicPlayer.getInstance(this);

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupFab();
        checkPermissions();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rvPlaylistSongs);
        tvPlaylistTitle = findViewById(R.id.tvPlaylistName);
        fabAddSong = findViewById(R.id.fabAddSong);

        cameraPreview = findViewById(R.id.cameraPreview);
        tvGestureIndicator = findViewById(R.id.tvGestureIndicator);

        tvPlaylistTitle.setText(playlistName);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        viewModel.getPlaylistWithSongs(playlistId).observe(this, playlistWithSongs -> {
            if (playlistWithSongs != null && playlistWithSongs.songs != null) {
                currentPlaylistSongs = playlistWithSongs.songs;
                adapter.setSongs(playlistWithSongs.songs);
            } else {
                currentPlaylistSongs = new ArrayList<>();
                adapter.setSongs(Collections.emptyList());
            }
        });
    }

    private List<Song> currentPlaylistSongs = new ArrayList<>();

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(this, position -> {
            // Play song from playlist context
            if (currentPlaylistSongs != null && !currentPlaylistSongs.isEmpty()) {
                if (position >= 0 && position < currentPlaylistSongs.size()) {
                    musicPlayer.setPlaylist(currentPlaylistSongs);
                    musicPlayer.playSong(position);
                    Toast.makeText(this, "Playing: " + currentPlaylistSongs.get(position).getTitle(),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Playlist is empty or loading...", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddSong.setOnClickListener(v -> showAddSongDialog());
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.CAMERA },
                        CAMERA_PERMISSION_CODE);
            } else {
                startGestureDetection();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.CAMERA },
                        CAMERA_PERMISSION_CODE);
            } else {
                startGestureDetection();
            }
        }
    }

    private void startGestureDetection() {
        if (cameraPreview == null)
            return;
        try {
            gestureDetector = new GestureDetector(this, cameraPreview, this);
            gestureDetector.startCamera(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to init gestures: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGestureDetected(HandGestureClassifier.GestureType gesture) {
        runOnUiThread(() -> {
            tvGestureIndicator.setVisibility(View.VISIBLE);
            tvGestureIndicator.setText(gesture.name());
            handleGesture(gesture);
            tvGestureIndicator.postDelayed(() -> tvGestureIndicator.setVisibility(View.GONE), 1500);
        });
    }

    private void handleGesture(HandGestureClassifier.GestureType gesture) {
        String message = "";
        switch (gesture) {
            case PALM_OPEN:
                if (!musicPlayer.isPlaying()) {
                    musicPlayer.togglePlayPause();
                    message = "Play Music";
                }
                break;
            case FIST:
                if (musicPlayer.isPlaying()) {
                    musicPlayer.togglePlayPause();
                    message = "Stop Music";
                }
                break;
            case TWO_FINGERS:
                musicPlayer.playNext();
                message = "Next Song";
                break;
            case THREE_FINGERS:
                musicPlayer.playPrevious();
                message = "Previous Song";
                break;
            case POINT_UP:
                musicPlayer.volumeUp();
                message = "Volume Up";
                break;
            case THUMBS_UP:
                musicPlayer.volumeDown();
                message = "Volume Down";
                break;
        }
        if (!message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions,
            @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGestureDetection();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gestureDetector != null) {
            gestureDetector.stopCamera();
        }
    }

    private void showAddSongDialog() {
        // Fetch all songs to show in the list
        List<Song> allSongs = viewModel.getAllSongs().getValue();
        if (allSongs == null || allSongs.isEmpty()) {
            Toast.makeText(this, "No songs on device to add", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] songTitles = new String[allSongs.size()];
        for (int i = 0; i < allSongs.size(); i++) {
            songTitles[i] = allSongs.get(i).getTitle();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Song to Playlist");
        builder.setItems(songTitles, (dialog, which) -> {
            Song selectedSong = allSongs.get(which);
            viewModel.addSongToPlaylist(playlistId, selectedSong);
        });
        builder.show();
    }
}
