# FlowBeats - Required Downloads

## ⚠️ CRITICAL: MediaPipe Model File

**You MUST download this file for gesture detection to work!**

### MediaPipe Hand Landmarker Model
- **File**: `hand_landmarker.task`
- **Size**: ~10 MB
- **Direct Download Link**: 
  ```
  https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
  ```
- **Where to place it**: `app/src/main/assets/hand_landmarker.task`

**Steps:**
1. Click the link above or copy-paste it into your browser
2. File will download automatically
3. In Android Studio, create `assets` folder in `app/src/main/` if it doesn't exist
4. Copy the downloaded file into the `assets` folder
5. Rebuild the project

---

## 📱 Software Requirements

### 1. Android Studio
- **What**: Official IDE for Android development
- **Download**: https://developer.android.com/studio
- **Version**: Arctic Fox (2020.3.1) or newer recommended
- **Size**: ~1 GB
- **Installation**:
  - Download installer for your OS (Windows/Mac/Linux)
  - Run installer and follow wizard
  - Install Android SDK when prompted

### 2. Java Development Kit (JDK)
- **What**: Required to compile Java code
- **Version**: JDK 8 or higher
- **Download**: Usually bundled with Android Studio
- **Alternative**: https://www.oracle.com/java/technologies/downloads/

---

## 🔧 Dependencies (Auto-Downloaded by Gradle)

These will be automatically downloaded when you open the project in Android Studio:

### Core Libraries
- ✅ **CameraX** (1.3.1) - Camera functionality
- ✅ **MediaPipe Tasks Vision** (0.10.9) - AI gesture detection
- ✅ **Room Database** (2.6.1) - Local storage
- ✅ **Material Components** (1.11.0) - UI components
- ✅ **Glide** (4.16.0) - Image loading
- ✅ **Lottie** (6.3.0) - Animations

**No action required** - Gradle will handle these automatically!

---

## 📦 Project Structure After Setup

```
FlowBeats/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/
│   │   │   │   └── hand_landmarker.task  ⬅️ PUT MODEL FILE HERE
│   │   │   ├── java/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   └── build.gradle
├── gradle/
├── README.md
└── COMPLETE_SETUP_GUIDE.md
```

---

## ⏱️ Download Time Estimates

| Item | Size | Time (Fast Internet) |
|------|------|---------------------|
| Android Studio | ~1 GB | 5-15 minutes |
| MediaPipe Model | ~10 MB | < 1 minute |
| Gradle Dependencies | ~200 MB | 2-5 minutes |
| **Total** | ~1.2 GB | **10-25 minutes** |

---

## ✅ Verification Checklist

Before building the app, ensure:

- [ ] Android Studio is installed
- [ ] Project is opened in Android Studio
- [ ] `hand_landmarker.task` is in `app/src/main/assets/`
- [ ] Gradle sync completed successfully (check bottom status bar)
- [ ] No red errors in files (yellow warnings are okay)
- [ ] Physical Android device is connected via USB
- [ ] USB Debugging is enabled on device

---

## 🆘 Download Issues?

### MediaPipe Model File
**Problem**: Link doesn't work or file won't download
**Solution**:
1. Try using a different browser
2. Check your internet connection
3. Try this alternative: Search "MediaPipe hand landmarker task file" on Google

### Android Studio
**Problem**: Download is slow or fails
**Solution**:
1. Use a download manager
2. Try mirror sites: https://developer.android.com/studio/archive
3. Ensure at least 5 GB free disk space

### Gradle Dependencies
**Problem**: Dependencies won't download
**Solution**:
1. Check internet connection
2. In Android Studio: File → Invalidate Caches / Restart
3. Try syncing again: File → Sync Project with Gradle Files

---

## 📞 Need Help?

If downloads fail:
1. Check COMPLETE_SETUP_GUIDE.md troubleshooting section
2. Ensure stable internet connection
3. Verify at least 5 GB free disk space
4. Try downloading from a different network

---

## 🎉 After Downloading

Once all downloads are complete:
1. Follow COMPLETE_SETUP_GUIDE.md for setup
2. Build and run the project
3. Test gestures with your hand!

**Happy gesture controlling! ✋🎵**
