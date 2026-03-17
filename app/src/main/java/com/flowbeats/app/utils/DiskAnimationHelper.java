package com.flowbeats.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * DiskAnimationHelper
 *
 * Manages the vinyl disk rotation animation for:
 * 1. The mini player's small disk (layout_mini_player.xml → diskContainer)
 * 2. The full player's vinyl disk (activity_player.xml → vinylDisk)
 *
 * Usage in MainActivity / PlayerActivity:
 *
 *   DiskAnimationHelper diskHelper = new DiskAnimationHelper(vinylDiskView);
 *   diskHelper.startRotation();   // call when song starts playing
 *   diskHelper.pauseRotation();   // call when paused
 *   diskHelper.stopRotation();    // call when stopped/new song
 */
public class DiskAnimationHelper {

    private final View diskView;
    private ObjectAnimator rotationAnimator;
    private float currentRotation = 0f;
    private boolean isRunning = false;

    // Duration for one full 360° rotation (in ms). Lower = faster.
    private static final long ROTATION_DURATION_MS = 8000L;

    public DiskAnimationHelper(View diskView) {
        this.diskView = diskView;
        setupAnimator();
    }

    private void setupAnimator() {
        rotationAnimator = ObjectAnimator.ofFloat(diskView, View.ROTATION, 0f, 360f);
        rotationAnimator.setDuration(ROTATION_DURATION_MS);
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotationAnimator.setRepeatMode(ObjectAnimator.RESTART);
        rotationAnimator.setInterpolator(new LinearInterpolator());
    }

    /** Start / resume rotation from where it left off */
    public void startRotation() {
        if (isRunning) return;

        // Resume from last known rotation angle
        rotationAnimator.cancel();
        rotationAnimator = ObjectAnimator.ofFloat(diskView, View.ROTATION, currentRotation, currentRotation + 360f);
        rotationAnimator.setDuration(ROTATION_DURATION_MS);
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotationAnimator.setRepeatMode(ObjectAnimator.RESTART);
        rotationAnimator.setInterpolator(new LinearInterpolator());

        // Track current rotation so we can resume smoothly
        rotationAnimator.addUpdateListener(animation -> {
            currentRotation = (float) animation.getAnimatedValue();
        });

        rotationAnimator.start();
        isRunning = true;
    }

    /** Pause rotation and save position */
    public void pauseRotation() {
        if (!isRunning) return;
        currentRotation = diskView.getRotation();
        rotationAnimator.cancel();
        isRunning = false;
    }

    /** Stop and reset to 0 */
    public void stopRotation() {
        rotationAnimator.cancel();
        diskView.setRotation(0f);
        currentRotation = 0f;
        isRunning = false;
    }

    public boolean isRotating() {
        return isRunning;
    }
}
