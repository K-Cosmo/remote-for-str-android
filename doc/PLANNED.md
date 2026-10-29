# Planned

## Current development — BUILD-0012 Bose Wi-Fi Provisioning PoC

`0.2.1` remains the latest released and accepted product baseline. BUILD-0012 is a **separate, unreleased PoC** on `feature/build-0012-bose-wifi-provisioning`.

Reference hardware: **Bose SoundTouch 20 Series III (355589-SM2), original Bose firmware**.

Decision: Android 10+ (`WifiNetworkSpecifier` + system consent) to join the Bose setup SoftAP, **not Bluetooth**. Keep the existing app `minSdk 26`; do not offer the new wizard on Android 8/9.

Phase A: **read-only** SoftAP connection, actual local Bose setup-host/port identification, callback cleanup and sanitized evidence. **No profile submission and no factory reset**.

Phase B is permitted only after a positive Phase A result: verified Bose Wi-Fi-profile submission, network transition, independent detection of the speaker in the home LAN, credential safety and a 1–2–3 wizard. STR installation remains a separate follow-up.

Specification, gates, task order and evidence requirements: [BUILD-0012](work/BUILD-0012.md). No release version bump until an implementation and real-device evidence exist.

## Deferred maintenance — BUILD-0008

Gradle 9.8.x evaluation remains deferred. The accepted build baseline stays Gradle 9.7.1 + AGP 9.4.1 until a dedicated maintenance build proves the upgrade.

## Later candidates

- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.