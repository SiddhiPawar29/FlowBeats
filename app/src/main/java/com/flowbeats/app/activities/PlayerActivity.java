package com.flowbeats.app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.flowbeats.app.R;
import com.flowbeats.app.models.Song;
import com.flowbeats.app.player.MusicPlayer;
import com.flowbeats.utils.DiskAnimationHelper;
import com.flowbeats.utils.GestureIndicatorHelper;
import com.flowbeats.app.gesture.GestureDetector;
import com.flowbeats.app.gesture.GestureListener;
import com.flowbeats.app.gesture.HandGestureClassifier;

public class PlayerActivity extends AppCompatActivity implements MusicPlayer.OnMusicPlayerListener, GestureListener {

    private static final int CAMERA_PERMISSION_CODE = 100;

    private DiskAnimationHelper diskHelper;
    private FrameLayout vinylDisk;
    private MusicPlayer musicPlayer;
    private ImageButton btnPlayPause;
    private TextView tvSongName;
    private TextView tvArtistName;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;

    private PreviewView cameraPreview;
    private TextView tvGestureIndicator;
    private GestureDetector gestureDetector;
    private GestureIndicatorHelper gestureHelper;

    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;
    private boolean isUserSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        vinylDisk = findViewById(R.id.vinylDisk);
        diskHelper = new DiskAnimationHelper(vinylDisk);
        musicPlayer = MusicPlayer.getInstance(this);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        tvSongName = findViewById(R.id.tvSongName);
        tvArtistName = findViewById(R.id.tvArtistName);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);

        cameraPreview = findViewById(R.id.cameraPreview);
        tvGestureIndicator = findViewById(R.id.tvGestureIndicator);

        if (tvGestureIndicator != null) {
            gestureHelper = new GestureIndicatorHelper(tvGestureIndicator);
        }

        ImageButton btnNext = findViewById(R.id.btnNext);
        ImageButton btnPrevious = findViewById(R.id.btnPrevious);

        // Setup Runnable
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (musicPlayer != null && musicPlayer.isPlaying() && !isUserSeeking) {
                    int currentPos = musicPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPos);
                    tvCurrentTime.setText(formatTime(currentPos));
                }
                progressHandler.postDelayed(this, 1000); // update every second
            }
        };

        // UI Listeners
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                musicPlayer.seekTo(seekBar.getProgress());
            }
        });

        // Wire play/pause button
        btnPlayPause.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_bounce));
            musicPlayer.togglePlayPause();
        });

        // Wire Next/Previous
        btnNext.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_bounce));
            musicPlayer.playNext();
        });

        btnPrevious.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_bounce));
            musicPlayer.playPrevious();
        });

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, R.anim.slide_down);
        });

        // Set listener to receive initial state and updates
        musicPlayer.setListener(this);

        checkPermissions();
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_CODE);
            } else {
                startGestureDetection();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
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
                    if (gestureHelper != null) gestureHelper.showGesture("✋  Play");
                }
                break;
            case FIST:
                if (musicPlayer.isPlaying()) {
                    musicPlayer.togglePlayPause();
                    message = "Stop Music";
                    if (gestureHelper != null) gestureHelper.showGesture("✊  Stop");
                }
                break;
            case TWO_FINGERS:
                musicPlayer.playNext();
                message = "Next Song";
                if (gestureHelper != null) gestureHelper.showGesture("✌️  Next Song");
                break;
            case THREE_FINGERS:
                musicPlayer.playPrevious();
                message = "Previous Song";
                if (gestureHelper != null) gestureHelper.showGesture("3️⃣  Previous Song");
                break;
            case POINT_UP:
                musicPlayer.volumeUp();
                message = "Volume Up";
                if (gestureHelper != null) gestureHelper.showGesture("☝️  Volume Up");
                break;
            case PINKY_UP:
                musicPlayer.volumeDown();
                message = "Volume Down";
                if (gestureHelper != null) gestureHelper.showGesture("🤙  Volume Down");
                break;
        }

        if (!message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            boolean cameraGranted = false;
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
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

    @Override
    protected void onResume() {
        super.onResume();
        musicPlayer.setListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressHandler.removeCallbacks(progressRunnable);
    }

    private String formatTime(int ms) {
        int seconds = ms / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        runOnUiThread(() -> {
            if (btnPlayPause != null) {
                btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
            }
            if (diskHelper != null) {
                if (isPlaying) {
                    diskHelper.startRotation();
                    progressHandler.post(progressRunnable);
                } else {
                    diskHelper.pauseRotation();
                    progressHandler.removeCallbacks(progressRunnable);
                }
            }
        });
    }

    @Override
    public void onSongChanged(Song song) {
        runOnUiThread(() -> {
            if (song != null) {
                if (tvSongName != null) tvSongName.setText(song.getTitle());
                if (tvArtistName != null) tvArtistName.setText(song.getArtist());
                if (diskHelper != null) {
                    diskHelper.stopRotation();
                }

                int duration = musicPlayer.getDuration();
                seekBar.setMax(duration);
                tvTotalTime.setText(formatTime(duration));
                int currentPos = musicPlayer.getCurrentPosition();
                seekBar.setProgress(currentPos);
                tvCurrentTime.setText(formatTime(currentPos));
            }
        });
    }
}
