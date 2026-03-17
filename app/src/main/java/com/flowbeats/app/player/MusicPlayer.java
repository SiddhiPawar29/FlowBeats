package com.flowbeats.app.player;

import android.content.Context;
import android.content.Intent;
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

    private OnMusicPlayerListener listener;
    private android.content.SharedPreferences prefs;

    // Core player fields
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private List<Song> playlist;
    private int currentPosition = 0;
    private boolean isPlaying = false;

    public interface OnMusicPlayerListener {
        void onPlaybackStateChanged(boolean isPlaying);

        void onSongChanged(Song song);
    }

    public void setListener(OnMusicPlayerListener listener) {
        this.listener = listener;
        if (listener != null && !playlist.isEmpty()) {
            // Notify current state immediately
            listener.onSongChanged(getCurrentSong());
            listener.onPlaybackStateChanged(isPlaying);
        }
    }

    private Context context;

    private MusicPlayer(Context context) {
        this.context = context;
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        playlist = new ArrayList<>();
        prefs = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE);

        mediaPlayer.setOnCompletionListener(mp -> playNext());
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
            Log.e(TAG, "Invalid song position: " + position + ", playlist size: " + playlist.size());
            return;
        }

        currentPosition = position;
        Song song = playlist.get(position);

        try {
            mediaPlayer.reset();
            Log.d(TAG, "Playing song: " + song.getTitle() + ", Path: " + song.getPath());

            // Basic validation
            if (song.getPath() == null || song.getPath().isEmpty()) {
                Log.e(TAG, "Song path is empty");
                return;
            }

            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;

            startService();
            if (listener != null) {
                listener.onSongChanged(song);
                listener.onPlaybackStateChanged(true);
            }
            saveState();
        } catch (IOException e) {
            Log.e(TAG, "Error playing song (IOException): " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Error playing song: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
                if (listener != null)
                    listener.onPlaybackStateChanged(false);
            } else {
                if (playlist.isEmpty()) {
                    // Try to restore?
                    restoreState();
                    if (!playlist.isEmpty()) {
                        playSong(currentPosition);
                        return;
                    }
                }

                mediaPlayer.start();
                isPlaying = true;
                startService();
                if (listener != null)
                    listener.onPlaybackStateChanged(true);
            }
        }
    }

    private void startService() {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction("ACTION_PLAY");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
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

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void saveState() {
        if (playlist.isEmpty() || currentPosition < 0)
            return;
        Song song = playlist.get(currentPosition);
        prefs.edit()
                .putString("last_path", song.getPath())
                .putString("last_title", song.getTitle())
                .putString("last_artist", song.getArtist())
                .apply();
    }

    private void restoreState() {
        String path = prefs.getString("last_path", null);
        if (path != null) {
            String title = prefs.getString("last_title", "Unknown");
            String artist = prefs.getString("last_artist", "Unknown");
            Song song = new Song(title, artist, "", path, 0);

            // Restore as single item playlist for now
            List<Song> savedPlaylist = new ArrayList<>();
            savedPlaylist.add(song);
            this.playlist = savedPlaylist;
            this.currentPosition = 0;

            if (listener != null) {
                listener.onSongChanged(song);
                listener.onPlaybackStateChanged(false);
            }
        }
    }
}
