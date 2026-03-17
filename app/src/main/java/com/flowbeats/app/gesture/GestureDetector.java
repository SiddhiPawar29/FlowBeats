package com.flowbeats.app.gesture;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GestureDetector {
    private static final String TAG = "GestureDetector";

    private Context context;
    private PreviewView previewView;
    private HandGestureClassifier gestureClassifier;
    private ExecutorService cameraExecutor;
    private Camera camera;
    private long lastGestureTime = 0;
    private static final long GESTURE_COOLDOWN_MS = 1000; // 1 second between gestures

    public GestureDetector(Context context, PreviewView previewView, GestureListener listener) {
        this.context = context;
        this.previewView = previewView;
        this.gestureClassifier = new HandGestureClassifier(context, new GestureListener() {
            @Override
            public void onGestureDetected(HandGestureClassifier.GestureType gesture) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastGestureTime >= GESTURE_COOLDOWN_MS) {
                    lastGestureTime = currentTime;
                    listener.onGestureDetected(gesture);
                }
            }
        });
        this.cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void startCamera(LifecycleOwner lifecycleOwner) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider, lifecycleOwner);
            } catch (Exception e) {
                Log.e(TAG, "Camera initialization failed: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider,
            LifecycleOwner lifecycleOwner) {
        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image analysis for gesture detection
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640, 480))
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888) // Use RGBA directly
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                try {
                    Bitmap bitmap = imageProxyToBitmap(image);
                    if (bitmap != null) {
                        gestureClassifier.detectGesture(bitmap, System.currentTimeMillis());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Analysis failed: " + e.getMessage());
                } finally {
                    image.close();
                }
            }
        });

        // Select front camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed: " + e.getMessage());
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        try {
            ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
            ByteBuffer buffer = planeProxy.getBuffer();
            int width = image.getWidth();
            int height = image.getHeight();
            int rowStride = planeProxy.getRowStride();
            int pixelStride = planeProxy.getPixelStride(); // Typically 4 for RGBA_8888

            // Create bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            // Handle padding if rowStride > width * 4
            if (rowStride > width * 4) {
                // Create a temporary buffer for the bitmap data
                byte[] data = new byte[width * height * 4];
                byte[] rowData = new byte[rowStride];

                for (int y = 0; y < height; y++) {
                    buffer.position(y * rowStride);
                    buffer.get(rowData, 0, Math.min(rowStride, buffer.remaining()));
                    System.arraycopy(rowData, 0, data, y * width * 4, width * 4);
                }
                buffer = ByteBuffer.wrap(data);
                bitmap.copyPixelsFromBuffer(buffer);
            } else {
                bitmap.copyPixelsFromBuffer(buffer);
            }

            int rotation = image.getImageInfo().getRotationDegrees();
            if (rotation != 0) {
                android.graphics.Matrix matrix = new android.graphics.Matrix();
                // Front camera images are mirrored
                matrix.postRotate(rotation);
                matrix.postScale(-1, 1, width / 2f, height / 2f); // Mirror horizontally for selfie view natural feel
                return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            } else {
                // Still mirror for front camera usually
                android.graphics.Matrix matrix = new android.graphics.Matrix();
                matrix.postScale(-1, 1, width / 2f, height / 2f);
                return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error converting image to bitmap: " + e.getMessage());
            return null;
        }
    }

    public void stopCamera() {
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (gestureClassifier != null) {
            gestureClassifier.close();
        }
        try {
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            cameraProvider.unbindAll();
        } catch (Exception e) {
            Log.e(TAG, "Failed to unbind camera: " + e.getMessage());
        }
    }
}
