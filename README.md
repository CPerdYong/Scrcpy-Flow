<p align="center">
  <img src="docs/icon.png" width="112" alt="Scrcpy Flow icon">
</p>

<h1 align="center">Scrcpy Flow</h1>
<p align="center"><b>Smooth wireless phone mirroring for mobile gamers - powered by <a href="https://github.com/Genymobile/scrcpy">scrcpy</a></b></p>

> **Unofficial.** Scrcpy Flow is a Windows app built on top of **scrcpy** by Genymobile.
> It is not made by or affiliated with Genymobile. All the mirroring magic is scrcpy's.

---

## What it is

Scrcpy Flow puts a simple, modern window on top of scrcpy, so you can mirror, control,
record and stream your Android phone over **Wi-Fi** without typing commands.
It comes with **Flow Connect**, a small Android app that switches wireless debugging on with one tap.

## Features

**Scrcpy Flow (Windows)**
- Dark, animated interface: Home, Devices, Game Mode, Recordings, Settings
- **No cable needed**: pair your phone by scanning a QR code (Android 11+)
- Saved phones, each with its own profile; reconnects automatically when the phone is back on Wi-Fi
- Profiles: Balanced, Best quality, Smooth (Wi-Fi), Gaming - and your own
- **Game Mode**: up to 144 fps, low-latency settings, real keyboard / mouse / controller input, play on a separate virtual display
- Record to MP4 / MKV (also in the background) and take screenshots
- Live FPS and activity log
- Optional animation add-ons (XamlFlair, LottieSharp), downloaded from inside the app

**Flow Connect (Android 11+)**
- One-tap **Wireless debugging** button and a **PC connect** quick settings tile
- Keeps wireless debugging on after restarts
- **Start mirroring on the PC from your phone** - phone screen or second screen
- Developer mode switch (Developer options, USB debugging, animation speed)

## Requirements

- Windows 10 or 11 (uses the built-in Windows PowerShell 5.1 - nothing to install)
- [scrcpy for Windows](https://github.com/Genymobile/scrcpy/releases) (4.0 or newer; 5.0 recommended)
- An Android phone; Android 11+ for cable-free pairing
- Phone and PC on the **same Wi-Fi**

## Install

1. Download **scrcpy** (`scrcpy-win64-vX.X.zip`) from the [official releases](https://github.com/Genymobile/scrcpy/releases) and extract it.
2. Download Scrcpy Flow (green **Code** button > **Download ZIP**, or from Releases).
   Before extracting: right-click the zip > **Properties** > tick **Unblock**.
3. Extract it next to your scrcpy folder, for example:
   ```
   D:\Apps\scrcpy-win64-v5.0\
   D:\Apps\Scrcpy-Flow\
   ```
4. Double-click **Start Scrcpy Flow.bat**. If Windows shows "Windows protected your PC", click **More info > Run anyway**.
   If it can't find scrcpy by itself, pick `scrcpy.exe` when asked.

## Connect your phone (no cable)

1. On the phone: **Settings > About phone** > tap **Build number** (or **OS version**) 7 times.
2. **Developer options** > turn on **Wireless debugging**
   (Xiaomi / Redmi / POCO: also **USB debugging (Security settings)**).
3. In Scrcpy Flow: **Devices > Show QR code**.
4. On the phone: **Wireless debugging > Pair device with QR code** and scan it.
5. Back on **Home**, pick the phone and press **Start mirroring**.

## Flow Connect (phone app)

The app is in the [`FlowConnect`](FlowConnect) folder.
In Scrcpy Flow, open **Devices** and click the **download** button on your phone:
Scrcpy Flow builds the app (first time about 10-20 minutes, everything is downloaded automatically)
and installs it on the phone. You can also run `FlowConnect\Build Flow Connect.bat` yourself.

## Security

Please read [SECURITY.md](SECURITY.md). In short: use it on your home Wi-Fi, prefer QR pairing,
and never allow a debugging prompt you didn't start.

## Credits

- [scrcpy](https://github.com/Genymobile/scrcpy) by Genymobile (Apache 2.0) - does all the mirroring
- [XamlFlair](https://github.com/XamlFlair/XamlFlair) and [LottieSharp](https://github.com/quicoli/LottieSharp) - optional animation add-ons
- Wi-Fi tile icon based on Google Material Icons (Apache 2.0)

## License

[MIT](LICENSE) for Scrcpy Flow and Flow Connect. scrcpy itself is under its own Apache 2.0 license.
