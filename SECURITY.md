# Security

Scrcpy Flow controls your phone through **adb** (Android Debug Bridge). Please read this before using it.

## What is protected
- **QR pairing uses a new random password every time** you click "Show QR code". No pairing secret is stored in the code.
- **Wireless debugging** (Android 11+) only accepts PCs you paired, and the connection is encrypted.
- **Flow Connect** (phone app) only accepts commands from adb itself: its bridge and setup receiver are protected by
  `android.permission.DUMP`, which normal apps cannot get.
- Your settings (`scrcpy-flow.json`, with phone names and IP addresses) stay on your PC and are excluded by `.gitignore`.

## Things to know
- **"USB to Wi-Fi" uses classic adb mode on port 5555.** It is not encrypted and anyone on the same Wi-Fi can try to connect
  (your phone still asks you to allow a new PC). It closes when the phone restarts. Prefer QR / pairing-code pairing.
- **Use it on your home Wi-Fi.** Avoid public Wi-Fi (cafes, airports, hotels).
- **Never tap "Allow" on a USB debugging prompt you did not start.**
- Turn Wireless debugging off when you don't need it (Flow Connect has a one-tap button and tile).

## Planned
- Button to close port 5555 and switch to encrypted mode
- Warning when the PC is on a Public network

## Reporting a problem
Please open a GitHub issue. Do not include IP addresses, phone serial numbers or pairing codes in public issues.
