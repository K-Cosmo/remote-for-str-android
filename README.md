# Remote for STR

A small independent Android companion app for the **phone remote built into [SoundTouch Reborn (STR)](https://github.com/JRpersonal/streborn)**.

Remote for STR discovers STR-enabled SoundTouch speakers on the local network and opens the existing STR phone interface in a dedicated Android app. It does **not** replace STR or reimplement playback controls.

> Remote for STR is an independent, unofficial community app and is not affiliated with or endorsed by Bose Corporation or the SoundTouch Reborn (STR) project. **Bose** and **SoundTouch** are trademarks of Bose Corporation.

**Current published release:** v0.3.0 (published under the former name **STR Remote**). The **Remote for STR** rename is implemented in the open development branch but is not yet released.

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
- guided Bose SoundTouch Wi-Fi setup on Android 10+ before STR installation, with model-specific preparation, speaker-side WLAN selection and password entry;
- separate Bose-only device rows while STR is not yet available, with room assignment and a direct STR installation link;
- last-successful-speaker reconnect;
- embedded STR phone remote;
- German and English UI;
- a native bilingual About dialog with independent community attribution, trademark notice, version, source and support links (development branch; not in public v0.3.0);
- Android 8.0+;
- no account, analytics, telemetry or Remote for STR backend.

**Project identity:** The Android package remains `app.strremote.android` and future updates use the original signing certificate. The public GitHub repository is **still named `str-remote-android`**; links in this README intentionally point to the existing working address until the repository is actually renamed. The old v0.3.0 tag and signed APK remain unchanged.

## Requirements and installation

For remote control, Remote for STR requires **SoundTouch Reborn to be installed and running on the speaker**. Starting with version 0.3.0, the Android app can first help configure Wi-Fi on a compatible SoundTouch using its original Bose firmware. Wi-Fi setup does not install STR.

1. Install STR using the upstream project:
   - [STR website](https://st-reborn.de)
   - [STR on GitHub](https://github.com/JRpersonal/streborn)
2. Connect the Android device to the same local Wi-Fi network as the speaker.
3. Download the published signed APK from [GitHub Releases](https://github.com/K-Cosmo/str-remote-android/releases/latest). Published v0.3.0 is named `STR-Remote-0.3.0.apk`; future rebranded releases will use `Remote-for-STR-<version>.apk`.
4. Install the APK and grant the local-network permission when Android requests it.

Remote for STR does not install STR, modify speaker firmware or bundle the STR agent.

## Bose Wi-Fi setup compatibility (0.3.0)

- **SoundTouch 20 Series III**: provisioning flow hardware-tested on the reference original Bose firmware. Results may vary with other firmware revisions.
- **SoundTouch 10**: original Bose firmware provisioning was subsequently **hardware-tested successfully with a separate test WLAN profile** (2026-10-30). Other WLAN/router/firmware combinations remain unverified. The published v0.3.0 APK still displays its original "Testbetrieb" label; the next development version uses the updated scoped test status.
- Other SoundTouch generations are **not claimed compatible** with this provisioning flow.
- Requires Android 10+ for Android's system-approved Bose setup network picker; the STR remote itself remains usable on Android 8/9.

**Important:** Bose's original provisioning endpoint uses **unencrypted HTTP** to transfer the Wi-Fi password over the temporary Bose setup network. Perform setup only in a trusted physical/network environment, and choose an isolated WPA2 network if you want to minimize credential exposure. Never reuse a sensitive password on an untrusted network. The app does not log or persist the entered password and does not automatically repeat a failed/uncertain transfer.

## Planned follow-up (BUILD-0013)

An in-app update check and compact embedded STR header are implemented on the development branch but **not included in published 0.3.0**. The app will check GitHub Releases in the foreground (no recurring background work), display a local alert for a newer stable version with an official APK asset, and open the official release page on demand. Remote for STR will not download or install an APK silently.

The redundant top padding in STR's browser/PWA header has been reduced to a user-approved 10 CSS px separation without hiding the speaker selector, power or accessibility controls.

The optional “Anderes SoundTouch-Modell” feedback uses offline suggestions drawn from Bose's historical support list and an email draft to `str-remote-android@gmx.org` (model, user-reported success, Remote for STR/Android versions), with a separate email app handling voluntary sending. It does not require a GitHub account or silently transmit data.

## Compatibility

The accepted 0.3.0 baseline is:

- Android 8.0+ (`minSdk 26`);
- compile/target API 37;
- real-device verification with a SoundTouch 10;
- STR local web endpoints on ports 8888/17008.

Additional STR-supported SoundTouch models are expected to use the same upstream interface, but they have not yet been independently verified by this project. Exact release/build evidence is kept under [`doc/evidence/qa/`](doc/evidence/qa/).

## Security and privacy

Remote for STR communicates directly with the speaker on the local network. The current STR web interface is served over local HTTP, so the local network is the trust boundary.

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

STR owns the speaker-side agent, playback behavior, presets, services and web remote. Remote for STR only provides the Android integration around that existing local interface.

If STR changes discovery, ports, authentication or its local web/API contract, Remote for STR may require a compatibility update.

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

Remote for STR is distributed under the [MIT License](LICENSE).

## Wizard languages (BUILD-0013, pending next release)

The upcoming Wi-Fi setup assistant is localized for **German and English** like the rest of Remote for STR. Model autocomplete remains offline; suggestions have a readable, bounded light popup, and feedback email wording follows the app language. The signed public v0.3.0 is not modified; these enhancements require a new version after phone QA.
