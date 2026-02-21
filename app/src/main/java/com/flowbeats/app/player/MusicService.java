package com.flowbeats.app.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.flowbeats.app.R;
import com.flowbeats.app.activities.MainActivity;
import com.flowbeats.app.models.Song;

public class MusicService extends Service {
    private static final String CHANNEL_ID = "FlowBeatsChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "ACTION_PLAY".equals(intent.getAction())) {
            showNotification();
        } else if (intent != null && "ACTION_STOP".equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Song currentSong = MusicPlayer.getInstance(this).getCurrentSong();
        String title = currentSong != null ? currentSong.getTitle() : "FlowBeats";
        String artist = currentSong != null ? currentSong.getArtist() : "Enjoy the music";

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "FlowBeats Music Service",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Bound service not used, we communicate via Singleton
    }
}
