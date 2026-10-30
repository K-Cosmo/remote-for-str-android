# Planned

## Released baseline — STR Remote 0.3.0

BUILD-0012 Bose Wi-Fi setup was released and accepted on 2026-10-30 as STR Remote `0.3.0`. It is now part of the production Android app, no longer an unreleased PoC. The WLAN wizard requires Android 10+; the existing STR remote works from Android 8.

**Additional hardware evidence (2026-10-30):** SoundTouch 10 original-firmware Wi-Fi provisioning was successfully tested with a separate test WLAN profile, independently of its existing STR remote functionality. More firmware and WLAN profiles remain unverified. The published v0.3.0 APK retains its release-time "Testbetrieb" label; the development branch revises the label to the scoped test status.

**Current work — BUILD-0013:** add a foreground GitHub Releases update notification and remove redundant browser/PWA safe-area padding from the embedded STR WebView. Do not alter the accepted 0.3.0 tag or signed APK; test UI and update-check behavior before the next release.

Sources: [BUILD-0012](work/BUILD-0012.md), [v0.3.0 release](https://github.com/K-Cosmo/str-remote-android/releases/tag/v0.3.0), [accepted gates](RELEASE_GATES.md).

## Deferred maintenance — BUILD-0008

Gradle 9.8.x evaluation remains deferred. The accepted build baseline stays Gradle 9.7.1 + AGP 9.4.1 until a dedicated maintenance build proves the upgrade.

## Later candidates

- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.