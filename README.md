# Android TV Fullscreen ExoPlayer

A simple, high-performance Android TV application built with **Kotlin**, **Jetpack Compose**, and the latest **androidx.media3:media3-exoplayer:1.5.1**.

## Features

- **True Fullscreen Video:** Pure PlayerView filling the screen with no UI toolbars or status bars.
- **SmartTube-Inspired D-Pad Key Handling:** Custom `dispatchKeyEvent` logic overriding standard Android TV remote inputs:
  - `KEYCODE_MEDIA_PLAY_PAUSE` / `KEYCODE_DPAD_CENTER`: `player.playWhenReady = !player.playWhenReady`
  - `KEYCODE_DPAD_LEFT`: Seek backward by 10,000ms (coerced at least to 0ms).
  - `KEYCODE_DPAD_RIGHT`: Seek forward by 10,000ms (coerced at most to `player.duration`).
- **Focus Management:** `playerView.requestFocus()` is executed on launch and resume to reliably capture remote D-pad events.
- **minSdk = 24:** Compatible with Android 7.0+ through Android TV 14+.
- **Leanback Launcher:** Declared in `AndroidManifest.xml` with landscape orientation and screen wake-lock enabled.

## How to Build & Run in Android Studio

1. Click **"Download Project (.ZIP)"** from the web toolbar.
2. Unzip into your workspace directory.
3. Open **Android Studio** (Ladybug or newer recommended).
4. Select **File > Open** and select the unzipped directory.
5. Wait for Gradle sync to complete.
6. Select your target device (Android TV Emulator or connected TV / Fire TV via ADB).
7. Click **Run (Shift + F10)**.

### Testing on Android TV Emulator via ADB
```bash
# Play / Pause
adb shell input keyevent 85

# D-pad Left (Seek -10s)
adb shell input keyevent 21

# D-pad Right (Seek +10s)
adb shell input keyevent 22

# D-pad Center (Select / Play-Pause)
adb shell input keyevent 23
```
