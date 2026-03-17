package com.flowbeats.app.gesture;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.List;

public class HandGestureClassifier {
    private static final String TAG = "HandGestureClassifier";
    private static final String MODEL_FILE = "hand_landmarker.task";

    public enum GestureType {
        PALM_OPEN, // Play
        FIST, // Stop/Pause
        TWO_FINGERS, // Next song
        THREE_FINGERS, // Previous song
        POINT_UP, // Volume up
        PINKY_UP, // Volume down
        UNKNOWN
    }

    private HandLandmarker handLandmarker;
    private GestureListener gestureListener;
    private long lastActionTime = 0;

    public HandGestureClassifier(Context context, GestureListener listener) {
        this.gestureListener = listener;
        initializeHandLandmarker(context);
    }

    private void initializeHandLandmarker(Context context) {
        try {
            BaseOptions baseOptions = BaseOptions.builder()
                    .setDelegate(Delegate.CPU)
                    .setModelAssetPath(MODEL_FILE)
                    .build();

            HandLandmarker.HandLandmarkerOptions options = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.VIDEO)
                    .setMinHandDetectionConfidence(0.5f)
                    .setMinHandPresenceConfidence(0.5f)
                    .setMinTrackingConfidence(0.5f)
                    .setNumHands(1)
                    .build();

            handLandmarker = HandLandmarker.createFromOptions(context, options);
        } catch (IllegalStateException e) {
            Log.e(TAG, "TFLite Error: " + e.getMessage());
            // Potentially disable gesture features here or notify listener
        } catch (RuntimeException e) {
            Log.e(TAG, "MediaPipe Error: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing hand landmarker: " + e.getMessage());
        }
    }

    public void detectGesture(Bitmap bitmap, long frameTime) {
        if (handLandmarker == null || bitmap == null)
            return;

        try {
            long startTime = System.currentTimeMillis();
            MPImage mpImage = new BitmapImageBuilder(bitmap).build();
            HandLandmarkerResult result = handLandmarker.detectForVideo(mpImage, frameTime);

            if (result != null && !result.landmarks().isEmpty()) {
                Log.d(TAG, "Landmarks detected: " + result.landmarks().size() + " in "
                        + (System.currentTimeMillis() - startTime) + "ms");
                List<NormalizedLandmark> landmarks = result.landmarks().get(0);
                GestureType gesture = classifyGesture(landmarks);

                Log.d(TAG, "Classified gesture: " + gesture);

                Log.d(TAG, "Classified gesture: " + gesture);

                if (gesture != GestureType.UNKNOWN && gestureListener != null) {
                    boolean isVolumeGesture = (gesture == GestureType.POINT_UP || gesture == GestureType.PINKY_UP);
                    long currentTime = System.currentTimeMillis();

                    if (isVolumeGesture || currentTime - lastActionTime >= 2000) {
                        if (!isVolumeGesture) {
                            lastActionTime = currentTime;
                        }
                        gestureListener.onGestureDetected(gesture);
                    } else {
                        Log.d(TAG, "Gesture skipped due to cooldown: " + gesture);
                    }
                }
            } else {
                // Log.v(TAG, "No landmarks detected"); // Verbose to avoid spam
            }
        } catch (Exception e) {
            Log.e(TAG, "Error detecting gesture: " + e.getMessage());
        }
    }

    private GestureType classifyGesture(List<NormalizedLandmark> landmarks) {
        if (landmarks == null || landmarks.size() < 21) {
            return GestureType.UNKNOWN;
        }

        // Finger tip indices: Thumb=4, Index=8, Middle=12, Ring=16, Pinky=20
        // PIP/IP indices for strict check: Thumb=3, Index=6, Middle=10, Ring=14,
        // Pinky=18
        // Use PIP and Tip to check strict extension

        boolean thumbExtended = isFingerExtended(landmarks, 4, 3);
        boolean indexExtended = isFingerExtended(landmarks, 8, 6);
        boolean middleExtended = isFingerExtended(landmarks, 12, 10);
        boolean ringExtended = isFingerExtended(landmarks, 16, 14);
        boolean pinkyExtended = isFingerExtended(landmarks, 20, 18);

        int extendedCount = 0;
        if (thumbExtended)
            extendedCount++;
        if (indexExtended)
            extendedCount++;
        if (middleExtended)
            extendedCount++;
        if (ringExtended)
            extendedCount++;
        if (pinkyExtended)
            extendedCount++;

        // Fist - 0 fingers extended (Stop/Pause)
        // Also accept just thumb curled/extended as potential fist if others are curled
        if (extendedCount == 0 || (extendedCount == 1 && thumbExtended)) {
            return GestureType.FIST;
        }

        // Palm open - 5 fingers extended (Play)
        // Also accept 4 fingers extended without thumb as Palm open for leniency
        if (extendedCount == 5 || (extendedCount == 4 && !thumbExtended)) {
            return GestureType.PALM_OPEN;
        }

        // Three fingers - index, middle, ring EXTENDED, pinky and thumb CURLED
        // (Previous song)
        if (indexExtended && middleExtended && ringExtended && !pinkyExtended && !thumbExtended) {
            return GestureType.THREE_FINGERS;
        }

        // Two fingers - index and middle EXTENDED, others CURLED (Next song)
        if (indexExtended && middleExtended && !ringExtended && !pinkyExtended && !thumbExtended) {
            return GestureType.TWO_FINGERS;
        }

        // Point up - only index finger extended (Volume Up)
        if (indexExtended && !middleExtended && !ringExtended && !pinkyExtended && !thumbExtended) {
            return GestureType.POINT_UP;
        }

        // Pinky up - only pinky finger extended (Volume Down)
        if (!indexExtended && !middleExtended && !ringExtended && pinkyExtended && !thumbExtended) {
            return GestureType.PINKY_UP;
        }

        return GestureType.UNKNOWN;
    }

    private boolean isFingerExtended(List<NormalizedLandmark> landmarks, int tipIndex, int baseIndex) {
        NormalizedLandmark tip = landmarks.get(tipIndex);
        NormalizedLandmark base = landmarks.get(baseIndex);

        // Calculate distance - finger is extended if tip is further from wrist than
        // base
        float tipDistance = calculateDistance(tip, landmarks.get(0));
        float baseDistance = calculateDistance(base, landmarks.get(0));

        return tipDistance > baseDistance;
    }

    private float calculateDistance(NormalizedLandmark point1, NormalizedLandmark point2) {
        float dx = point1.x() - point2.x();
        float dy = point1.y() - point2.y();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public void close() {
        if (handLandmarker != null) {
            handLandmarker.close();
            handLandmarker = null;
        }
    }
}
