# VS Code for Android

🎉 **Visual Studio Code running on your Android device!**

A full-featured code editor powered by Electron and Monaco Editor, now available on Android.

![VS Code Android](https://img.shields.io/badge/VS%20Code-Android-blue?style=for-the-badge&logo=visual-studio-code)

## Features

- ✅ **Monaco Editor** - The same editor that powers VS Code
- ✅ **Syntax Highlighting** - Support for 100+ programming languages
- ✅ **IntelliSense** - Smart code completion
- ✅ **Debugging** - Built-in debugging support
- ✅ **Extensions** - 105 built-in VS Code extensions
- ✅ **File Explorer** - Browse and edit files
- ✅ **Integrated Terminal** - Run commands directly
- ✅ **Dark Theme** - Easy on the eyes

## Screenshots

```
┌─────────────────────────────────────┐
│  ≡  VS Code              ⚙️  ─  □  ✕ │
├─────────────────────────────────────┤
│ EXPLORER  SEARCH  🔌 Extensions    │
│─────────────────────────────────────│
│ 📁 project                          │
│  📁 src                            │
│    📄 index.ts                     │
│    📄 app.ts                       │
│  📄 package.json                   │
├─────────────────────────────────────┤
│                                     │
│  1 │ const hello = () => {         │
│  2 │   console.log("Hello!");      │
│  3 │   return hello;               │
│  4 │ };                            │
│  5 │                               │
│  6 │ hello();                      │
│                                     │
├─────────────────────────────────────┤
│ main  │ Ln 6, Col 1  │ JavaScript  │
└─────────────────────────────────────┘
```

## Download & Install

### 📥 Method 1: Build from Source

1. **Download the project:**
   - Go to [Releases](../../releases)
   - Download `source-code.zip`

2. **Open in Android Studio:**
   ```
   Open Android Studio → File → Open → Select "shell/android/" folder
   ```

3. **Sync Gradle:**
   ```
   Android Studio will prompt: "Sync Now"
   ```

4. **Build APK:**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

5. **Install on device:**
   ```
   Transfer APK to phone → Open → Install
   ```

### 📱 Method 2: Using AIDE (Code Editor on Android)

1. Download this project as ZIP
2. Extract to: `/storage/emulated/0/AIDE/projects/vscode-android/`
3. Open AIDE → Open Project → Select the folder
4. Tap **Run** to build and install

### 🖥️ Method 3: Command Line

```bash
# Clone the repository
git clone https://github.com/acoumbossa699-eng/electron-android.git
cd electron-android/shell/android

# Open in Android Studio
studio .

# Or build from command line
./gradlew assembleDebug
```

## Requirements

| Requirement | Minimum | Recommended |
|------------|---------|-------------|
| Android Version | 7.0 (API 24) | 10.0+ |
| RAM | 2 GB | 4 GB+ |
| Storage | 500 MB | 1 GB+ |
| Screen | Any | 720p+ |

## Project Structure

```
shell/android/
├── app/
│   ├── src/main/
│   │   ├── java/org/electron/android/   # Java source code
│   │   ├── res/                        # Resources (icons, themes)
│   │   └── AndroidManifest.xml         # App configuration
│   └── build.gradle                    # App build config
├── assets/
│   └── lib/vs/                        # VS Code Monaco Editor (140 MB)
├── build.gradle                       # Project build config
├── settings.gradle                    # Gradle settings
└── gradlew                            # Build script
```

## VS Code Components

This project uses official VS Code components from [microsoft/vscode](https://github.com/microsoft/vscode):

- **Monaco Editor** - Code editing engine
- **Workbench** - Full VS Code UI
- **Extensions** - 105 built-in extensions
- **Language Services** - IntelliSense, debugging

## Building for Release

### Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (Unsigned)
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

### Signed Release APK
```bash
# 1. Create keystore
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias

# 2. Configure signing in app/build.gradle
# 3. Build
./gradlew assembleRelease
```

## Troubleshooting

### "Gradle sync failed"
- Ensure you have Android Studio Hedgehog or newer
- Check Java JDK 17 is installed
- Try: File → Invalidate Caches → Invalidate and Restart

### "WebView not loading"
- Ensure Chrome for Android is up to date
- Check internet permission in AndroidManifest.xml

### "App crashes on startup"
- Check logcat for errors: `adb logcat`
- Ensure device meets minimum requirements

### "Monaco Editor not working"
- Clear app cache: Settings → Apps → VS Code → Clear Cache
- Reinstall the app

## Contributing

Contributions welcome! Please read the contribution guidelines first.

## License

This project uses VS Code components licensed under the [MIT License](https://github.com/microsoft/vscode/blob/main/LICENSE.txt).

VS Code itself is licensed under the [MIT License](https://github.com/microsoft/vscode/blob/main/LICENSE.txt).

## Support

- 🐛 [Report a Bug](../../issues)
- 💡 [Request a Feature](../../issues)
- 📖 [Documentation](https://code.visualstudio.com/docs)

## Acknowledgments

- [Microsoft VS Code Team](https://github.com/microsoft/vscode) - For the amazing VS Code
- [Electron Team](https://github.com/electron/electron) - For the Electron framework
- [Monaco Editor Team](https://github.com/microsoft/monaco-editor) - For the code editor

---

**Made with ❤️ for Android developers**

[![GitHub stars](https://img.shields.io/github/stars/acoumbossa699-eng/electron-android?style=social)](../../stargazers)
[![GitHub forks](https://img.shields.io/github/forks/acoumbossa699-eng/electron-android?style=social)](../../network/members)
