package com.flowbeats.utils;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

/**
 * GestureIndicatorHelper
 *
 * Shows a gesture name in the pill TextView briefly, then auto-hides.
 *
 * Usage (in MainActivity where you detect gestures):
 *
 *   GestureIndicatorHelper gestureHelper = new GestureIndicatorHelper(tvGestureIndicator);
 *   gestureHelper.showGesture("✋ PALM_OPEN");    // pause
 *   gestureHelper.showGesture("👆 POINT_UP");     // volume up
 *   gestureHelper.showGesture("👉 SWIPE_RIGHT");  // next song
 */
public class GestureIndicatorHelper {

    private final TextView gestureView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long DISPLAY_DURATION_MS = 1800L;

    public GestureIndicatorHelper(TextView gestureView) {
        this.gestureView = gestureView;
    }

    public void showGesture(String gestureName) {
        handler.removeCallbacksAndMessages(null); // cancel any pending hide

        gestureView.setText(gestureName);
        gestureView.setVisibility(View.VISIBLE);

        // Fade in
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(200);
        gestureView.startAnimation(fadeIn);

        // Auto-hide after delay
        handler.postDelayed(() -> {
            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(300);
            gestureView.startAnimation(fadeOut);
            gestureView.setVisibility(View.GONE);
        }, DISPLAY_DURATION_MS);
    }

    public void hide() {
        handler.removeCallbacksAndMessages(null);
        gestureView.setVisibility(View.GONE);
    }
}
