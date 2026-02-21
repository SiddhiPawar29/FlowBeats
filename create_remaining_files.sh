#!/bin/bash

# Create MainActivity
cat > app/src/main/java/com/flowbeats/app/activities/MainActivity.java << 'EOF'
package com.flowbeats.app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

public class MainActivity extends AppCompatActivity implements GestureListener {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    
    private PreviewView cameraPreview;
    private TextView tvGestureIndicator;
    private GestureDetector gestureDetector;
    private MusicPlayer musicPlayer;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermissions();
        setupBottomNavigation();
        
        musicPlayer = MusicPlayer.getInstance(this);
        
        // Load default fragment
        loadFragment(new HomeFragment());
    }

    private void initViews() {
        cameraPreview = findViewById(R.id.cameraPreview);
        tvGestureIndicator = findViewById(R.id.tvGestureIndicator);
        bottomNavigation = findViewById(R.id.bottomNavigation);
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

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startGestureDetection();
        }
    }

    private void startGestureDetection() {
        gestureDetector = new GestureDetector(this, cameraPreview, this);
        gestureDetector.startCamera(this);
    }

    @Override
    public void onGestureDetected(HandGestureClassifier.GestureType gesture) {
        runOnUiThread(() -> {
            tvGestureIndicator.setVisibility(View.VISIBLE);
            tvGestureIndicator.setText(gesture.name());
            
            handleGesture(gesture);
            
            tvGestureIndicator.postDelayed(() -> 
                tvGestureIndicator.setVisibility(View.GONE), 1500);
        });
    }

    private void handleGesture(HandGestureClassifier.GestureType gesture) {
        switch (gesture) {
            case PALM_OPEN:
                musicPlayer.togglePlayPause();
                break;
            case TWO_FINGERS:
                musicPlayer.playNext();
                break;
            case THREE_FINGERS:
                musicPlayer.playPrevious();
                break;
            case POINT_UP:
                musicPlayer.volumeUp();
                break;
            case POINT_DOWN:
                musicPlayer.volumeDown();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGestureDetection();
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
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
EOF

# Create MusicPlayer
cat > app/src/main/java/com/flowbeats/app/player/MusicPlayer.java << 'EOF'
package com.flowbeats.app.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.flowbeats.app.models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private static final String TAG = "MusicPlayer";
    private static MusicPlayer instance;
    
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private List<Song> playlist;
    private int currentPosition = 0;
    private boolean isPlaying = false;

    private MusicPlayer(Context context) {
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        playlist = new ArrayList<>();
    }

    public static synchronized MusicPlayer getInstance(Context context) {
        if (instance == null) {
            instance = new MusicPlayer(context.getApplicationContext());
        }
        return instance;
    }

    public void setPlaylist(List<Song> songs) {
        this.playlist = songs;
    }

    public void playSong(int position) {
        if (playlist.isEmpty() || position < 0 || position >= playlist.size()) {
            return;
        }

        currentPosition = position;
        Song song = playlist.get(position);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
        } catch (IOException e) {
            Log.e(TAG, "Error playing song: " + e.getMessage());
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
            } else {
                mediaPlayer.start();
                isPlaying = true;
            }
        }
    }

    public void playNext() {
        if (!playlist.isEmpty()) {
            currentPosition = (currentPosition + 1) % playlist.size();
            playSong(currentPosition);
        }
    }

    public void playPrevious() {
        if (!playlist.isEmpty()) {
            currentPosition = currentPosition - 1;
            if (currentPosition < 0) {
                currentPosition = playlist.size() - 1;
            }
            playSong(currentPosition);
        }
    }

    public void volumeUp() {
        if (audioManager != null) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        }
    }

    public void volumeDown() {
        if (audioManager != null) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public Song getCurrentSong() {
        if (!playlist.isEmpty() && currentPosition >= 0 && currentPosition < playlist.size()) {
            return playlist.get(currentPosition);
        }
        return null;
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
EOF

# Create Fragments
cat > app/src/main/java/com/flowbeats/app/fragments/HomeFragment.java << 'EOF'
package com.flowbeats.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.flowbeats.app.R;

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}
EOF

cat > app/src/main/java/com/flowbeats/app/fragments/SearchFragment.java << 'EOF'
package com.flowbeats.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.flowbeats.app.R;

public class SearchFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
}
EOF

cat > app/src/main/java/com/flowbeats/app/fragments/LibraryFragment.java << 'EOF'
package com.flowbeats.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.flowbeats.app.R;

public class LibraryFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }
}
EOF

# Create MusicService (stub)
cat > app/src/main/java/com/flowbeats/app/player/MusicService.java << 'EOF'
package com.flowbeats.app.player;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MusicService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
EOF

# Create SharedPreferenceManager
cat > app/src/main/java/com/flowbeats/app/utils/SharedPreferenceManager.java << 'EOF'
package com.flowbeats.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {
    private static final String PREF_NAME = "FlowBeatsPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PASSWORD = "user_password";
    
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    
    public SharedPreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    
    public void saveUserData(String name, String email, String password) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PASSWORD, password);
        editor.apply();
    }
    
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }
    
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }
    
    public String getUserPassword() {
        return preferences.getString(KEY_USER_PASSWORD, "");
    }
    
    public void clearUserData() {
        editor.clear();
        editor.apply();
    }
}
EOF

echo "All essential Java files created successfully"
