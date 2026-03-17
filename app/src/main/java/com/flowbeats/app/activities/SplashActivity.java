package com.flowbeats.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.flowbeats.app.R;
import com.flowbeats.app.utils.SharedPreferenceManager;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2500; // 2.5 seconds to allow animation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Animate logo and text
        animateSplash();

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            com.flowbeats.app.utils.SharedPreferenceManager prefManager = new com.flowbeats.app.utils.SharedPreferenceManager(
                    this);

            Intent intent;
            if (prefManager.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }, SPLASH_DURATION);
    }

    private void animateSplash() {
        android.view.View logoContainer = findViewById(R.id.logoContainer);
        android.view.View progressBar = findViewById(R.id.progressBar);

        if (logoContainer != null) {
            logoContainer.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(500)
                    .start();
        }

        if (progressBar != null) {
            progressBar.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setStartDelay(1000)
                    .start();
        }
    }
}
