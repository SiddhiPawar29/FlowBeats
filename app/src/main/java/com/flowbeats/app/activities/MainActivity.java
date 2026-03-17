package com.flowbeats.app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.flowbeats.app.R;
import com.flowbeats.app.fragments.HomeFragment;
import com.flowbeats.app.fragments.LibraryFragment;
import com.flowbeats.app.fragments.SearchFragment;
import com.flowbeats.app.gesture.GestureDetector;
import com.flowbeats.app.gesture.GestureListener;
import com.flowbeats.app.gesture.HandGestureClassifier;
import com.flowbeats.app.player.MusicPlayer;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import android.widget.FrameLayout;
import com.flowbeats.utils.DiskAnimationHelper;
import com.flowbeats.utils.GestureIndicatorHelper;

public class MainActivity extends AppCompatActivity implements GestureListener {
    private static final int CAMERA_PERMISSION_CODE = 100;

    private PreviewView cameraPreview;
    private TextView tvGestureIndicator;
    private GestureDetector gestureDetector;
    private MusicPlayer musicPlayer;
    private BottomNavigationView bottomNavigation;

    // Mini Player Views
    private View miniPlayer;
    private TextView tvMiniTitle, tvMiniArtist;
    private View btnMiniPlayPause;
    private DiskAnimationHelper miniDiskHelper;
    private GestureIndicatorHelper gestureHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        // Set status bar transparent
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        initViews();
        checkPermissions();
        setupBottomNavigation();

        musicPlayer = MusicPlayer.getInstance(this);

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void initViews() {
        cameraPreview = findViewById(R.id.cameraPreview);
        tvGestureIndicator = findViewById(R.id.tvGestureIndicator);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (tvGestureIndicator != null) {
            gestureHelper = new GestureIndicatorHelper(tvGestureIndicator);
        }

        miniPlayer = findViewById(R.id.miniPlayer);
        tvMiniTitle = findViewById(R.id.tvSongTitle);
        tvMiniArtist = findViewById(R.id.tvArtistName);
        btnMiniPlayPause = findViewById(R.id.btnPlayPause);

        if (miniPlayer != null) {
            FrameLayout miniDisk = miniPlayer.findViewById(R.id.diskContainer);
            if (miniDisk != null) {
                miniDiskHelper = new DiskAnimationHelper(miniDisk);
            }
            miniPlayer.setOnClickListener(v -> {
                // Open full player
                Intent intent = new Intent(this, PlayerActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, 0);
            });
        }

        if (btnMiniPlayPause != null) {
            btnMiniPlayPause.setOnClickListener(v -> musicPlayer.togglePlayPause());
        }

        setupMusicPlayerListener();
    }

    // Legacy methods removed (moved to Fragment)

    private void setupMusicPlayerListener() {
        musicPlayer = MusicPlayer.getInstance(this);
        musicPlayer.setListener(new MusicPlayer.OnMusicPlayerListener() {
            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
                runOnUiThread(() -> {
                    if (btnMiniPlayPause instanceof android.widget.ImageButton) {
                        ((android.widget.ImageButton) btnMiniPlayPause).setImageResource(
                                isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
                    }
                    if (miniDiskHelper != null) {
                        if (isPlaying)
                            miniDiskHelper.startRotation();
                        else
                            miniDiskHelper.pauseRotation();
                    }
                });
            }

            @Override
            public void onSongChanged(com.flowbeats.app.models.Song song) {
                runOnUiThread(() -> {
                    if (miniPlayer == null)
                        return;

                    if (song != null) {
                        miniPlayer.setVisibility(View.VISIBLE);
                        if (tvMiniTitle != null)
                            tvMiniTitle.setText(song.getTitle());
                        if (tvMiniArtist != null)
                            tvMiniArtist.setText(song.getArtist());
                    } else {
                        miniPlayer.setVisibility(View.GONE);
                        if (miniDiskHelper != null)
                            miniDiskHelper.stopRotation();
                    }
                });
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.navigation_search) {
                fragment = new SearchFragment();
            } else if (id == R.id.navigation_library) {
                fragment = new LibraryFragment();
            }

            return loadFragment(fragment);
        });
    }

    public void switchToLibraryTab() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_library);
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.CAMERA },
                        CAMERA_PERMISSION_CODE);
            } else {
                startGestureDetection();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA },
                        CAMERA_PERMISSION_CODE);
            } else {
                startGestureDetection();
            }
        }
    }

    private void startGestureDetection() {
        if (cameraPreview == null) {
            return;
        }

        try {
            gestureDetector = new GestureDetector(this, cameraPreview, this);
            gestureDetector.startCamera(this);
            // Toast.makeText(this, "Gesture Camera Started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to initialize gestures: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGestureDetected(HandGestureClassifier.GestureType gesture) {
        runOnUiThread(() -> handleGesture(gesture));
    }

    private void handleGesture(HandGestureClassifier.GestureType gesture) {
        String message = "";
        switch (gesture) {
            case PALM_OPEN:
                if (!musicPlayer.isPlaying()) {
                    musicPlayer.togglePlayPause();
                    message = "Play Music";
                    if (gestureHelper != null)
                        gestureHelper.showGesture("✋  Play");
                }
                break;
            case FIST:
                if (musicPlayer.isPlaying()) {
                    musicPlayer.togglePlayPause();
                    message = "Stop Music";
                    if (gestureHelper != null)
                        gestureHelper.showGesture("✊  Stop");
                }
                break;
            case TWO_FINGERS:
                musicPlayer.playNext();
                message = "Next Song";
                if (gestureHelper != null)
                    gestureHelper.showGesture("✌️  Next Song");
                break;
            case THREE_FINGERS:
                musicPlayer.playPrevious();
                message = "Previous Song";
                if (gestureHelper != null)
                    gestureHelper.showGesture("3️⃣  Previous Song");
                break;
            case POINT_UP:
                musicPlayer.volumeUp();
                message = "Volume Up";
                if (gestureHelper != null)
                    gestureHelper.showGesture("☝️  Volume Up");
                break;
            case PINKY_UP:
                musicPlayer.volumeDown();
                message = "Volume Down";
                if (gestureHelper != null)
                    gestureHelper.showGesture("🤙  Volume Down");
                break;
        }

        if (!message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            boolean cameraGranted = false;
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA)
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    cameraGranted = true;
                }
            }

            if (cameraGranted) {
                startGestureDetection();
            } else {
                Toast.makeText(this, "Camera permission required for gestures", Toast.LENGTH_LONG).show();
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
}
