package com.flowbeats.app.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.flowbeats.app.models.Song;

import java.util.ArrayList;
import java.util.List;

public class MediaScanner {
    
    public static List<Song> scanMusicFiles(Context context) {
        List<Song> songs = new ArrayList<>();
        
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        
        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE + " ASC")) {
            
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                
                do {
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String album = cursor.getString(albumColumn);
                    String path = cursor.getString(pathColumn);
                    long duration = cursor.getLong(durationColumn);
                    
                    Song song = new Song(title, artist, album, path, duration);
                    songs.add(song);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return songs;
    }
}
