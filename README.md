# STR Remote for Android

A small independent Android companion app for the **phone remote built into [SoundTouch Reborn (STR)](https://github.com/JRpersonal/streborn)**.

STR Remote discovers STR-enabled SoundTouch speakers on the local network and opens the existing STR phone interface in a dedicated Android app. It does **not** replace STR or reimplement playback controls.

> STR Remote is an independent community project and is not affiliated with Bose Corporation or the SoundTouch Reborn project.

**Current release:** 0.1.0  
**Download:** [GitHub Releases](https://github.com/K-Cosmo/str-remote-android/releases/latest)

## Features

- automatic discovery of STR speakers on the local network;
- support for current and legacy STR discovery;
- automatic fallback between STR ports 8888 and 17008;
- speaker selection when multiple devices are found;
- remembers the last selected speaker;
- manual host/IP entry as a fallback;
- embedded STR phone remote;
- German and English UI;
- Android 8.0+;
- no account, analytics, telemetry or STR Remote backend.

## Requirements and installation

STR Remote requires **SoundTouch Reborn to already be installed and running on the speaker**.

1. Install STR using the upstream project:
   - [STR website](https://st-reborn.de)
   - [STR on GitHub](https://github.com/JRpersonal/streborn)
2. Connect the Android device to the same local network as the speaker.
3. Download the latest `STR-Remote-*.apk` from [GitHub Releases](https://github.com/K-Cosmo/str-remote-android/releases/latest).
4. Install the APK and grant the local-network permission when Android requests it.

STR Remote does not install STR, modify speaker firmware or bundle the STR agent.

## Compatibility

The initial release has been verified with:

- **SoundTouch 10**
- STR **v0.9.86**
- Android app targeting API 37

Other SoundTouch models supported by STR are expected to use the same upstream interface, but have not yet been independently verified by this project.

## Security and privacy

STR Remote communicates directly with the speaker on the local network. The current STR web interface is served over local HTTP, so the local network is the trust boundary.

The Android wrapper intentionally keeps its own scope small:

- no account or cloud service;
- no analytics or telemetry;
- no native JavaScript bridge;
- WebView file/content access disabled;
- external web links open in the system browser;
- local STR navigation is restricted to the selected speaker and expected STR ports.

The app stores only metadata for the last selected local speaker in Android app-private preferences.

See [SECURITY.md](doc/SECURITY.md) and [PRIVACY.md](doc/PRIVACY.md) for details.

## Relationship to SoundTouch Reborn

STR owns the speaker-side agent, playback behavior, presets, services and web remote. STR Remote only provides the Android integration around that existing local interface.

If STR changes discovery, ports, authentication or its local web/API contract, STR Remote may require a compatibility update.

## Development

Bug reports are most useful when they include the SoundTouch model, STR version, Android version/device, observed behavior and whether the STR page works directly in a browser.

See [CONTRIBUTING.md](doc/CONTRIBUTING.md) and the project documentation under [`/doc`](doc/README.md).

Development is AI-assisted for implementation, debugging, review and documentation. The Android app itself contains no AI/LLM runtime and does not send user data to an AI service.

## License

STR Remote is released under the [MIT License](LICENSE).
