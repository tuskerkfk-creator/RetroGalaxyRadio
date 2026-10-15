# Retro Galaxy Radio — Samsung SC-05G / Android 7.0

A **real** radio interface for an embedded Galaxy S6, written in Android Java + offline HTML/CSS/JavaScript. Android's built-in `MediaPlayer` plays live MP3 streams; the WebView draws the UI. No modern Android libraries or external JavaScript dependencies.

## Installation (build APK)

1. Install Android Studio on a Windows/Mac/Linux computer. Open the `RetroGalaxyRadio` folder as a project (not the ZIP itself).
2. Let Android Studio download the Android SDK 33 and Android Gradle Plugin 7.4.2 as needed. The project needs JDK 11 (Android Studio's embedded JDK is normally suitable for AGP 7.4.2).
3. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**. A debug APK will be generated under `app/build/outputs/apk/debug/app-debug.apk`.
4. On the Galaxy SC-05G running Android 7.0, enable **Settings > About device > Software information > Build number** (tap 7 times) to reveal Developer options. Enable USB debugging, connect phone by USB and install with Android Studio **Run**, or copy the APK to the phone and open it (grant Install unknown apps / Unknown sources permission as shown by your phone).
5. Turn on Wi-Fi. Open Retro Galaxy Radio. Touch the round ▶ control. Drag/click the horizontal tuner scale to select one of four stations. Adjust volume with the slider. Tap SETTINGS to configure city and station streams.

## Test on a PC or another device

Open `app/src/main/assets/index.html` in a modern browser to preview the screen and controls. Clock and tuner work locally; streaming and weather need a working internet connection and browser permissions. Android app audio is more reliable than the browser preview on old devices.

## Controls / behaviors

- Big clock and date update every second from device time.
- Weather updates every 15 minutes using Open-Meteo city lookup; set your city in SETTINGS.
- FM-style tuner is decorative: 4 presets are mapped to 88.0, 94.7, 101.3 and 108.0 MHz **on screen**, but those are not actual over-the-air frequencies. It receives radio via Wi-Fi only.
- Presets: SomaFM Groove Salad, Drone Zone, Groove Salad Classic, Radio Paradise. Providers may change their direct URLs or geographic access.
- User-editable station titles and direct HTTP(S) MP3/AAC streams, saved locally.
- Volume slider, play/pause, buffering and error feedback.
- Screen stays awake and runs landscape immersive full-screen while open.
- Audio stops when app is closed/destroyed; it is *not* a background service. It does not automatically start on boot. The user must press play to begin streaming.

## Hardware precautions

The original phone battery must be inspected before embedding, especially if it is swollen or overheats. Don't seal a continuously charging old lithium battery in an inaccessible enclosure. Provide removable access, ventilation and an appropriate USB power supply. The phone headphone jack can feed an amplifier's line-level input with volume kept moderate, or use a Bluetooth speaker.

## Notes

- Project has not been compiled on this machine (Android SDK/Gradle not present). It is a ready-to-build source project, not a prebuilt or device-tested APK.
- For Open-Meteo weather, no API key is required for noncommercial use subject to their terms.
- If station playback fails, use SETTINGS to supply another station **direct MP3** stream URL.
- Content of station streams depends on external broadcasters and your network access.

## Build environment note

This ZIP includes a Gradle 7.6 version pin in `gradle/wrapper/gradle-wrapper.properties`, but **does not contain gradle-wrapper.jar** because no Android/Gradle toolchain is available in this environment. Android Studio may ask you to configure a local Gradle 7.6 installation or regenerate the Gradle wrapper. The actual app sources do not require any third-party Android library. If Studio cannot sync immediately, use Gradle 7.6 to run `gradle wrapper --gradle-version 7.6` in this directory, then reopen the project.

## Version 1.1 (touch controls and auto-refresh)
- Tap the large triangle to play. While connecting or playing, tap the square to stop the stream. Live-stream audio resumes by reconnecting rather than by resuming at the old broadcast position.
- MUTE / UNMUTE instantly silences or restores the player without resetting volume or changing stations.
- REFRESH updates the clock, weather and display; it does not interrupt music.
- The clock refreshes every second, the display is refreshed every minute, and the weather fetches automatically every 15 minutes and when the app becomes visible. Tiny periodic display shifts reduce (but do not eliminate) AMOLED burn-in risk.
- Weather errors now show WEATHER ERROR rather than showing invalid temperature. Ensure the phone can access api.open-meteo.com and geocoding-api.open-meteo.com over HTTPS in its browser. Old Android 7 devices can encounter TLS/certificate problems.
- This project preserves the applicationId and upgrades versionCode to 2 so the new debug build can install over the previous APK if it is signed by the same GitHub build debug key. GitHub-hosted build signing keys may differ between runs, so if Android reports APP NOT INSTALLED, uninstall the old app and install the new one (custom station settings will be lost).

## Updating your existing GitHub repository
Overwrite the existing project files while keeping their paths under `app/src/main/...` and `.github/workflows/...`; committing to main triggers the Build Android 7 APK action. You can use GitHub's browser editor for individual files, or the web-based github.dev editor to drag the extracted project files/folders into the repository, then commit and push. Uploading a ZIP without extracting does not update the app source.


## Version 1.2 — talk radio and improved tuning
- BBC World Service and NPR 24 Hour Program Stream added to NEWS/TALK. Four music presets retained.
- Swipe left/right across the station strip to step stations, tap a named station, or touch the vintage dial.
- Older custom preset storage with four entries is intentionally not migrated to the new six-entry list; custom edits can be re-entered in Settings.
- BBC direct stream is provisional; international access is officially supported via https://www.bbc.com/audio/stations but BBC does not guarantee this direct URL to third-party players.
- NPR stream may be HTTP; Android manifest permits cleartext connections. Streaming URLs may change or be geographically restricted.
