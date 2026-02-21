# FlowBeats 🎵

## Gesture-Controlled Music Player for Android

FlowBeats is an innovative Android music player that lets you control music playback using hand gestures! No need to touch your phone - just show your hand to the camera and control everything.

### 🌟 Features

- **Gesture Control** - Control music with hand gestures using MediaPipe AI
- **Spotify-Style UI** - Premium dark theme interface
- **Camera Integration** - Real-time hand tracking with CameraX
- **Music Library** - Automatically scans and plays music from your device
- **Playlists** - Create and manage your custom playlists
- **Background Playback** - Music continues playing in the background

### 🤚 Supported Gestures

| Gesture | Action |
|---------|--------|
| 🖐️ Open Palm (5 fingers) | Play / Pause |
| ✌️ Two Fingers | Next Song |
| 🤟 Three Fingers | Previous Song |
| 👆 Point Up | Volume Up |
| 👇 Point Down | Volume Down |

### 📱 Screenshots

> Add screenshots here after building the app

### 🛠️ Technologies Used

- **Java** - Primary programming language
- **Android Studio** - IDE
- **CameraX** - Camera preview and image analysis
- **MediaPipe Hand Landmarker** - Hand gesture detection AI
- **Room Database** - Local data persistence
- **Material Design 3** - UI components
- **Glide** - Image loading
- **Lottie** - Animations

### 📋 Requirements

- Android 7.0 (API 24) or higher
- Front-facing camera
- Storage for music files
- Android Studio (for development)

### 🚀 Setup Instructions

See **COMPLETE_SETUP_GUIDE.md** for detailed step-by-step instructions.

#### Quick Start:

1. Download and extract FlowBeats.zip
2. Open in Android Studio
3. Download MediaPipe model file from:
   ```
   https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
   ```
4. Place `hand_landmarker.task` in `app/src/main/assets/`
5. Sync Gradle
6. Build and Run on physical device

### 📂 Project Structure

```
FlowBeats/
├── app/
│   ├── src/main/
│   │   ├── java/com/flowbeats/app/
│   │   │   ├── activities/        # UI Screens
│   │   │   ├── gesture/           # Gesture Detection
│   │   │   ├── player/            # Music Playback
│   │   │   ├── models/            # Data Models
│   │   │   ├── database/          # Room Database
│   │   │   └── utils/             # Utilities
│   │   ├── res/                   # Resources
│   │   └── assets/                # MediaPipe Model
│   └── build.gradle
└── build.gradle
```

### 🎯 How It Works

1. **Camera Capture** - CameraX continuously captures frames from front camera
2. **Hand Detection** - MediaPipe processes frames to detect hand landmarks
3. **Gesture Classification** - Custom algorithm identifies specific gestures
4. **Music Control** - Detected gestures trigger music player actions

### ⚙️ Configuration

- Gesture cooldown: 1 second (prevents accidental triggers)
- Camera resolution: 640x480 (optimized for performance)
- Hand detection confidence: 50%
- Maximum hands detected: 1

### 🐛 Troubleshooting

**Camera not working?**
- Grant camera permission
- Ensure no other app is using camera
- Test on physical device (not emulator)

**Gestures not detected?**
- Improve lighting
- Hold hand 12-18 inches from camera
- Face palm toward camera
- Make clear, distinct gestures

**No music found?**
- Grant storage permission
- Add MP3/M4A files to device
- Click "Scan for music"

### 🔒 Permissions

- **CAMERA** - Required for gesture detection
- **READ_MEDIA_AUDIO** / **READ_EXTERNAL_STORAGE** - Required for music access
- **FOREGROUND_SERVICE** - Required for background playback

### 📝 Future Enhancements

- [ ] Custom gesture training
- [ ] Spotify/YouTube Music integration
- [ ] Advanced gesture combinations
- [ ] Gesture history analytics
- [ ] Cloud sync for playlists
- [ ] Gesture sensitivity settings
- [ ] Multi-hand gesture support

### 👨‍💻 Developer

Created with ❤️ for gesture-based music control

### 📄 License

This project is for educational purposes.

### 🙏 Acknowledgments

- Google MediaPipe team for hand landmark detection
- Android CameraX team
- Spotify for UI inspiration

---

**Enjoy controlling your music with gestures! 🎵✋**
