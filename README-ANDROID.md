# Electron Android

## Overview

This is a port of [Electron](https://electronjs.org/) to Android. The goal is to enable running Electron applications on Android devices using a WebView-based approach.

## Project Structure

```
electron-android/
├── shell/
│   ├── android/                    # Android native code
│   │   ├── java/
│   │   │   └── org/electron/android/
│   │   │       ├── ElectronActivity.java      # Main activity
│   │   │       ├── ElectronApplication.java   # Application class
│   │   │       ├── bridge/
│   │   │       │   └── ElectronBridge.java     # JNI bridge
│   │   │       ├── content/
│   │   │       │   └── ElectronContentView.java # WebView wrapper
│   │   │       ├── view/
│   │   │       │   └── ElectronRootView.java   # Root view
│   │   │       └── service/
│   │   │           └── ElectronService.java    # Background service
│   │   └── electron_android_jni_bridge.h       # Native header
│   └── browser/
│       └── android/
│           ├── electron_android_delegate.h
│           └── electron_native_window_view_android.h
├── build/
│   ├── args/
│   │   └── android.gn              # Android build args
│   └── android/
│       └── AndroidManifest.xml     # Android manifest
└── BUILD.gn.android/               # Android build rules
```

## Features

### Implemented

- ✅ Android Activity and Application classes
- ✅ JNI bridge for Java/C++ communication
- ✅ WebView-based content rendering
- ✅ Touch input handling
- ✅ Hardware back button support
- ✅ Fullscreen/immersive mode
- ✅ Permission handling
- ✅ Android service for background tasks
- ✅ Status bar and navigation bar theming

### Planned

- 🔄 Window management integration
- 🔄 File system access (Storage Access Framework)
- 🔄 Notifications
- 🔄 Hardware acceleration for WebGL
- 🔄 Media capture (camera, microphone)
- 🔄 Biometric authentication
- 🔄 Clipboard integration
- 🔄 Bluetooth support
- 🔄 Location services
- 🔄 APK packaging

## Building

### Prerequisites

1. Install Android NDK
2. Install Android SDK
3. Install Chromium depot tools

### Build Steps

```bash
# Configure for Android
gn gen out/android --args-file=build/args/android.gn

# Build
ninja -C out/android electron
```

### Building APK

```bash
# Build the APK
ninja -C out/android chrome_public_apk
```

## Architecture

### Java Layer

The Java layer provides:
- `ElectronActivity`: Main activity that hosts the WebView
- `ElectronApplication`: Application class for initialization
- `ElectronBridge`: JNI bridge for native communication
- `ElectronContentView`: Custom WebView wrapper
- `ElectronService`: Background service

### Native Layer

The native layer (C++) provides:
- Browser process integration
- Window management
- WebContents management
- IPC communication

## Permissions

The app requests the following permissions:
- `INTERNET` - Network access
- `READ_EXTERNAL_STORAGE` - File access (up to API 32)
- `READ_MEDIA_*` - Media access (API 33+)
- `CAMERA` - Camera access (optional)
- `RECORD_AUDIO` - Microphone access (optional)
- `ACCESS_FINE_LOCATION` - Location services (optional)
- `POST_NOTIFICATIONS` - Notifications (API 33+)
- `BLUETOOTH_*` - Bluetooth (optional)

## Contributing

This is a work in progress. Contributions are welcome!

## License

Electron Android is licensed under the MIT license.
