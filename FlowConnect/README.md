# Flow Connect (Scrcpy Flow phone app)

A small Android app (Android 11+) that works with Scrcpy Flow on your PC:

- One-tap **Wireless debugging** button and **PC connect** quick settings tile
- Keeps wireless debugging on after restarts and Wi-Fi changes
- **Start mirroring on the PC** from the phone (phone screen or second screen)
- Developer mode switch (Developer options, USB debugging, animation speed)

## Build

**Easiest:** in Scrcpy Flow, go to **Devices** and click the **download** button on your phone.
It builds and installs the app for you.

**Manually:** double-click **Build Flow Connect.bat** in this folder. It downloads a private copy of
Java 17, the Android SDK and Gradle into `.buildtools` (about 1.5 GB the first time, nothing is installed
system-wide), builds the app, and copies it next to Scrcpy Flow as `FlowConnect.apk`.

**With Android Studio:** open this folder, wait for Gradle sync, then **Build > Build APK(s)**.

Xiaomi / Redmi / POCO: in Developer options also turn on **Install via USB** and
**USB debugging (Security settings)**.

## How it works

Android only lets an app change the Wireless debugging setting with the `WRITE_SECURE_SETTINGS`
permission, which can only be granted over adb. Scrcpy Flow grants it once when it installs the app.
Scrcpy Flow and the app talk through a content provider and a broadcast receiver that are protected by
`android.permission.DUMP`, so only adb (and the system) can use them - other apps on the phone cannot.
