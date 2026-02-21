# FlowBeats - Complete Setup Guide

## 🎵 Project Overview
FlowBeats is a gesture-controlled music player for Android that uses MediaPipe Hand Landmarker and CameraX to control music playback with hand gestures.

## 📋 Prerequisites
1. **Android Studio** (Arctic Fox or newer) - [Download here](https://developer.android.com/studio)
2. **JDK 8 or higher**
3. **Android SDK** (API Level 24 or higher)
4. **Physical Android device** (API 24+) - Gesture detection works best on real devices

## 🚀 Step-by-Step Setup Instructions

### Step 1: Download and Extract
1. Download this FlowBeats.zip file
2. Extract to your desired location
3. Open Android Studio

### Step 2: Open Project in Android Studio
1. Click "Open an Existing Project"
2. Navigate to the extracted FlowBeats folder
3. Click "OK"
4. Wait for Gradle sync to complete (this may take several minutes)

### Step 3: Download MediaPipe Hand Landmarker Model
**CRITICAL**: You must download the MediaPipe model file manually.

1. Go to: https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
2. Download the `hand_landmarker.task` file
3. In Android Studio, switch to "Project" view (top-left dropdown)
4. Navigate to: `app/src/main/assets/`
5. If `assets` folder doesn't exist, create it:
   - Right-click on `main` → New → Directory → name it `assets`
6. Copy the downloaded `hand_landmarker.task` file into the `assets` folder

### Step 4: Sync Gradle Dependencies
1. Click "File" → "Sync Project with Gradle Files"
2. Wait for all dependencies to download (MediaPipe, CameraX, Room, etc.)
3. If you see any errors, click "Try Again"

### Step 5: Configure gradle.properties (if needed)
If you encounter build issues, add these to `gradle.properties`:
```
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

### Step 6: Build the Project
1. Click "Build" → "Clean Project"
2. Click "Build" → "Rebuild Project"
3. Wait for build to complete

### Step 7: Run on Device
1. Enable USB Debugging on your Android phone:
   - Settings → About Phone → Tap "Build Number" 7 times
   - Settings → Developer Options → Enable "USB Debugging"
2. Connect phone via USB
3. Click "Run" button (green play icon) or press Shift+F10
4. Select your device from the list
5. Click "OK"

## 📱 First Run Setup

### Grant Permissions
On first launch, the app will request:
1. **Camera Permission** - Required for gesture detection
2. **Storage Permission** - Required to read music files
3. Grant both permissions for full functionality

### Create Account
1. Click "Sign Up"
2. Enter your details:
   - Full Name
   - Email
   - Password
3. Click "Sign Up" button
4. You're now logged in!

## 🤚 Gesture Controls

Once the app is running with camera permission:

| Gesture | Action |
|---------|--------|
| 🖐️ **Open Palm** (all 5 fingers) | Play / Pause |
| ✌️ **Two Fingers** (index + middle) | Next Song |
| 🤟 **Three Fingers** (index + middle + ring) | Previous Song |
| 👆 **Point Up** (index finger pointing up) | Volume Up |
| 👇 **Point Down** (index finger pointing down) | Volume Down |

**Tips for Best Gesture Detection:**
- Hold your hand 12-18 inches from camera
- Ensure good lighting
- Make clear, distinct gestures
- Wait 1 second between gestures (cooldown period)
- Camera preview shows in top-right corner

## 📂 Project Structure

```
FlowBeats/
├── app/
│   ├── src/main/
│   │   ├── java/com/flowbeats/app/
│   │   │   ├── activities/        # All app screens
│   │   │   ├── adapters/          # RecyclerView adapters
│   │   │   ├── fragments/         # Home, Search, Library
│   │   │   ├── models/            # Data models
│   │   │   ├── gesture/           # Gesture detection (MediaPipe + CameraX)
│   │   │   ├── player/            # Music player & service
│   │   │   ├── database/          # Room database
│   │   │   └── utils/             # Helper classes
│   │   ├── res/                   # Resources (layouts, drawables, etc.)
│   │   ├── assets/                # ⚠️ PUT hand_landmarker.task HERE
│   │   └── AndroidManifest.xml
│   └── build.gradle               # App dependencies
├── build.gradle                   # Project gradle
└── settings.gradle
```

## 🔧 Key Dependencies

### Already Included in build.gradle:
- **CameraX** (1.3.1) - Camera preview and image analysis
- **MediaPipe Hand Landmarker** (0.10.9) - Hand gesture detection
- **Room Database** (2.6.1) - Local data persistence
- **Glide** (4.16.0) - Image loading
- **Lottie** (6.3.0) - Animations
- **Material Components** (1.11.0) - UI components

### External Downloads Required:
1. **MediaPipe Model File**: 
   - URL: https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
   - Location: `app/src/main/assets/hand_landmarker.task`

## 🎨 UI Features
- **Dark Theme** - Spotify-inspired premium design
- **Bottom Navigation** - Home, Search, Library
- **Mini Player** - Always visible at bottom
- **Camera Preview** - Top-right corner for gesture monitoring
- **Gesture Indicator** - Shows detected gesture name
- **Smooth Animations** - Material Design transitions

## 🎵 Music Features
- **Auto-scan** - Automatically detects music on device
- **Playlists** - Create and manage custom playlists
- **Persistent Playback** - Background music service
- **Media Controls** - Play, pause, next, previous
- **Volume Control** - Gesture-based volume adjustment

## 🐛 Troubleshooting

### Build Errors
**Problem**: Gradle sync failed
**Solution**: 
- File → Invalidate Caches / Restart
- Increase heap size in gradle.properties
- Check internet connection for dependency downloads

### MediaPipe Error: "Model file not found"
**Problem**: App crashes on gesture detection
**Solution**:
- Ensure `hand_landmarker.task` is in `app/src/main/assets/`
- Check file name is exactly: `hand_landmarker.task`
- Rebuild project after adding file

### Camera Not Working
**Problem**: Black camera preview
**Solution**:
- Grant camera permission in Settings → Apps → FlowBeats
- Test on physical device (emulator cameras often fail)
- Check camera is not used by another app

### No Music Found
**Problem**: "No songs found" message
**Solution**:
- Grant storage permission
- Ensure music files (.mp3, .wav, .m4a) are on device
- Wait for media scan to complete
- Click "Scan for music" button

### Gestures Not Detected
**Problem**: Hand shown but no gesture response
**Solution**:
- Improve lighting conditions
- Hold hand 12-18 inches from camera
- Make clearer, more distinct gestures
- Ensure palm faces camera
- Wait 1 second between gestures

## 📱 Minimum Requirements
- Android 7.0 (API 24) or higher
- Camera with minimum 2MP front-facing
- 100MB free storage
- 2GB RAM minimum

## 🔐 Permissions Used
- `CAMERA` - Required for gesture detection
- `READ_MEDIA_AUDIO` - Required to access music files (Android 13+)
- `READ_EXTERNAL_STORAGE` - Required for music access (Android 12 and below)
- `FOREGROUND_SERVICE` - Required for background music playback

## 📝 Additional Notes

### Testing Gestures
- Use the camera preview in top-right to see yourself
- Green text will show detected gesture name
- Practice each gesture before using

### Adding Music
- Connect phone to computer
- Copy MP3/M4A files to Music folder
- Or download music using any music app
- FlowBeats will auto-detect all audio files

### Performance Tips
- Close other camera apps
- Ensure good lighting for better detection
- Keep app updated

## 🆘 Need Help?
If you encounter any issues:
1. Check this guide's troubleshooting section
2. Ensure all setup steps are completed
3. Verify MediaPipe model file is in correct location
4. Check Android Studio build output for specific errors

## 🎉 You're All Set!
Once setup is complete, enjoy controlling your music with hand gestures! The future is here! 🚀
