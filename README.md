# Remote for STR

**Remote for STR** is an independent Android companion for [SoundTouch Reborn (STR)](https://github.com/JRpersonal/streborn). It discovers STR-enabled Bose SoundTouch speakers on the local Wi-Fi network and displays STR's existing phone remote in an Android app. Playback and streaming features are provided by STR.

**Current release:** 0.3.1 · [Download signed APK](https://github.com/K-Cosmo/remote-for-str-android/releases/tag/v0.3.1) · **Android 8.0+** (Android 10+ for Wi-Fi setup)

## Features

- Discover and control STR-enabled SoundTouch speakers; reconnect to the last speaker.
- Save speakers, assign rooms, switch between devices and find offline devices.
- Use the original STR web remote with playback, volume and presets.
- Configure Wi-Fi on supported original-firmware SoundTouch speakers **before** installing STR (Android 10+); SoundTouch 10 and 20 Series III have been tested in specific configurations. Other models and firmware combinations are **not verified**.
- German and English interface, with an independent-community About dialog.
- Optional GitHub update notification and voluntary, user-initiated setup feedback by email.

## Installation

1. For remote control, [install STR](https://st-reborn.de) on the speaker. If the speaker still runs original Bose firmware, the Android 10+ Wi-Fi assistant can help prepare its network connection; **it does not install STR**.
2. Connect the phone to the same Wi-Fi network as the STR-enabled speaker.
3. Download and install the signed APK from [GitHub Releases](https://github.com/K-Cosmo/remote-for-str-android/releases/latest). Existing installations update in place; do not uninstall first.
4. Open Remote for STR and select a discovered speaker. Manual host entry is available if discovery does not find it.

## Privacy and safety

Remote for STR does not require an account, collect analytics or operate its own backend. Speaker and room settings remain in Android's app-private storage. The optional update check contacts GitHub; sending community feedback requires opening and explicitly sending an email in your mail app.

**Wi-Fi setup warning:** Original Bose firmware receives the Wi-Fi password over **unencrypted HTTP** on its temporary setup network. Use only a trusted setup environment. Remote for STR does not store or log the password.

See [Privacy](doc/PRIVACY.md), [Security](doc/SECURITY.md) and [tested compatibility / open cases](doc/STATUS.md).

## Community and attribution

**Unofficial community app.** Remote for STR is independently developed and is **not affiliated with or endorsed by Bose Corporation or the SoundTouch Reborn (STR) project**. **Bose** and **SoundTouch** are trademarks of Bose Corporation.

For bugs and feature requests, use [GitHub Issues](https://github.com/K-Cosmo/remote-for-str-android/issues). For unverified SoundTouch Wi-Fi setups, you can prepare an optional feedback email directly in the app and send it through your email client. No GitHub account is needed.

[Project documentation](doc/README.md) · [Changelog](doc/CHANGELOG.md) · [Contributing](CONTRIBUTING.md) · [MIT License](LICENSE)

Development is AI-assisted; the published Android app has **no AI runtime or AI-service data path**.
