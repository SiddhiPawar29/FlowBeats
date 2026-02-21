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
        THUMBS_UP, // Volume down
        UNKNOWN
    }

    private HandLandmarker handLandmarker;
    private GestureListener gestureListener;

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

                if (gesture != GestureType.UNKNOWN && gestureListener != null) {
                    gestureListener.onGestureDetected(gesture);
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
        // Finger base indices: Thumb=2, Index=5, Middle=9, Ring=13, Pinky=17
        // Use MCP (base) and Tip to check extension

        boolean thumbExtended = isFingerExtended(landmarks, 4, 2);
        boolean indexExtended = isFingerExtended(landmarks, 8, 5);
        boolean middleExtended = isFingerExtended(landmarks, 12, 9);
        boolean ringExtended = isFingerExtended(landmarks, 16, 13);
        boolean pinkyExtended = isFingerExtended(landmarks, 20, 17);

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
        if (extendedCount == 0) {
            return GestureType.FIST;
        }

        // Also accept just thumb curled as potential fist if others are curled
        if (extendedCount == 1 && thumbExtended) {
            return GestureType.FIST;
        }

        // Palm open - 5 fingers extended (Play)
        if (extendedCount == 5) {
            return GestureType.PALM_OPEN;
        }

        // Also accept 4 fingers (no thumb) as Palm Open if user struggles with thumb
        if (indexExtended && middleExtended && ringExtended && pinkyExtended && !thumbExtended) {
            return GestureType.PALM_OPEN;
        }

        // Two fingers - index and middle EXTENDED, others CURLED (Next song)
        if (indexExtended && middleExtended && !ringExtended && !pinkyExtended) {
            return GestureType.TWO_FINGERS;
        }

        // Three fingers - index, middle, ring EXTENDED, pinky CURLED (Previous song)
        if (indexExtended && middleExtended && ringExtended && !pinkyExtended) {
            return GestureType.THREE_FINGERS;
        }

        // Point up - only index finger extended (Volume Up)
        if (indexExtended && !middleExtended && !ringExtended && !pinkyExtended && !thumbExtended) {
            // Optional: Check orientation
            return GestureType.POINT_UP;
        }

        // Thumbs Up - only thumb extended (Volume Down)
        // We need to be careful with "isFingerExtended" for thumb as it works
        // differently.
        // But assuming generic check works:
        if (thumbExtended && !indexExtended && !middleExtended && !ringExtended && !pinkyExtended) {
            return GestureType.THUMBS_UP;
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
