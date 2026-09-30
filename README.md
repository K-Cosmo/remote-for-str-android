# STR Remote for Android

A small independent Android companion app for the **phone remote built into [SoundTouch Reborn (STR)](https://github.com/JRpersonal/streborn)**.

STR Remote discovers STR-enabled SoundTouch speakers on the local network and opens the existing STR phone interface in a dedicated Android app. It does **not** replace STR or reimplement playback controls.

> STR Remote is an independent community project and is not affiliated with Bose Corporation or the SoundTouch Reborn project.

**Current release:** 0.2.1

**Download:** [GitHub Releases](https://github.com/K-Cosmo/str-remote-android/releases/latest)

## Features

- automatic discovery of STR speakers on the local network;
- current and legacy STR discovery support;
- automatic fallback between STR ports 8888 and 17008;
- finite discovery with actionable no-Wi-Fi, no-device and STR-unreachable states;
- retry after Wi-Fi becomes available without restarting the app;
- direct Android Wi-Fi settings access from no-Wi-Fi Remote and Devices states;
- persistent saved-speaker management with stable Online/Offline reachability presentation;
- quick switching between discovered and saved speakers;
- optional room assignment with built-in and custom room names;
- clear current-speaker indication and a direct return path to the active remote;
- manual host/IP entry as a fallback;
- last-successful-speaker reconnect;
- embedded STR phone remote;
- German and English UI;
- Android 8.0+;
- no account, analytics, telemetry or STR Remote backend.

## Requirements and installation

STR Remote requires **SoundTouch Reborn to already be installed and running on the speaker**.

1. Install STR using the upstream project:
   - [STR website](https://st-reborn.de)
   - [STR on GitHub](https://github.com/JRpersonal/streborn)
2. Connect the Android device to the same local Wi-Fi network as the speaker.
3. Download the latest `STR-Remote-*.apk` from [GitHub Releases](https://github.com/K-Cosmo/str-remote-android/releases/latest).
4. Install the APK and grant the local-network permission when Android requests it.

STR Remote does not install STR, modify speaker firmware or bundle the STR agent.

## Compatibility

The accepted 0.2.1 baseline is:

- Android 8.0+ (`minSdk 26`);
- compile/target API 37;
- real-device verification with a SoundTouch 10;
- STR local web endpoints on ports 8888/17008.

Additional STR-supported SoundTouch models are expected to use the same upstream interface, but they have not yet been independently verified by this project. Exact release/build evidence is kept under [`doc/evidence/qa/`](doc/evidence/qa/).

## Security and privacy

STR Remote communicates directly with the speaker on the local network. The current STR web interface is served over local HTTP, so the local network is the trust boundary.

The Android wrapper intentionally keeps its own scope small:

- no account or cloud service;
- no analytics or telemetry;
- no native JavaScript bridge;
- WebView file/content access disabled;
- external web links open in the system browser;
- local STR navigation is restricted to the selected speaker and expected STR ports.

The app stores local speaker metadata, saved-speaker entries, optional room labels and local UI preferences in Android app-private preferences. This data is not cloud-synced.

See [Security](doc/SECURITY.md) and [Privacy](doc/PRIVACY.md) for the normative details.

## Relationship to SoundTouch Reborn

STR owns the speaker-side agent, playback behavior, presets, services and web remote. STR Remote only provides the Android integration around that existing local interface.

If STR changes discovery, ports, authentication or its local web/API contract, STR Remote may require a compatibility update.

## Project documentation

The normative project documentation lives under [`/doc`](doc/README.md). In particular:

- [Current status](doc/STATUS.md)
- [Architecture](doc/ARCHITECTURE.md)
- [Android/build compatibility](doc/ANDROID_COMPATIBILITY.md)
- [Changelog](doc/CHANGELOG.md)
- [Contributing](doc/CONTRIBUTING.md)

Root-level helper files do not define a second policy/documentation set.

Development is AI-assisted for implementation, debugging, review and documentation. The Android app itself contains no AI/LLM runtime and does not send user data to an AI service.

## License

STR Remote is released under the [MIT License](LICENSE).
