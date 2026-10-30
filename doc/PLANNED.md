# Planned

## Released baseline — STR Remote 0.3.0

BUILD-0012 Bose Wi-Fi setup was released and accepted on 2026-10-30 as STR Remote `0.3.0`. It is now part of the production Android app, no longer an unreleased PoC. The WLAN wizard requires Android 10+; the existing STR remote works from Android 8.

**Next hardware-specific gate:** SoundTouch 10 original-firmware Wi-Fi provisioning remains *experimental* and needs its own end-to-end verification. The already functioning STR remote on SoundTouch 10 does not prove the original Bose WLAN setup flow. SoundTouch 20 Series III is the tested reference model.

Sources: [BUILD-0012](work/BUILD-0012.md), [v0.3.0 release](https://github.com/K-Cosmo/str-remote-android/releases/tag/v0.3.0), [accepted gates](RELEASE_GATES.md).

## Deferred maintenance — BUILD-0008

Gradle 9.8.x evaluation remains deferred. The accepted build baseline stays Gradle 9.7.1 + AGP 9.4.1 until a dedicated maintenance build proves the upgrade.

## Later candidates

- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.