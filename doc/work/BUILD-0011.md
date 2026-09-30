# BUILD-0011 - 0.2.1 UX Compactness and Device Status

## Goal

Improve vertical space on the remote screen and make saved-speaker reachability immediately understandable without changing STR behavior or Android architecture.

## Scope

1. Compact native banner
   - combine app title and selected-speaker label into one compact two-line text column;
   - vertically center the Devices/Remote action beside that column;
   - restore the comfortable v3 button sizing and add a little more vertical breathing room;
   - leave the embedded STR WebView untouched.

2. Saved-speaker reachability presentation
   - saved + reachable: stronger green fill + 2 dp green border + Online;
   - saved + unreachable from the app (including no Wi-Fi path): stronger red fill + 2 dp red border + Offline;
   - saved + probe in progress: neutral card;
   - keep text status so color is never the only signal.

3. Device-card hierarchy
   - title: device name + optional room;
   - second line: model + Online/Offline/checking state;
   - third line: STR version + IP address when version is available;
   - retain decreasing text sizes for title, status line and technical-detail line.

4. Remote no-Wi-Fi and reconnect handling
   - never intentionally expose Chromium's generic connection-error page for a known no-Wi-Fi state;
   - show native same-Wi-Fi guidance while preserving the selected-speaker header;
   - provide Retry plus direct Android Wi-Fi settings/panel navigation on the Remote view;
   - provide the same direct Wi-Fi settings/panel action on the Devices no-Wi-Fi state;
   - probe the selected endpoint before recovery/device-selection WebView loads;
   - keep the WebView hidden behind a native checking state until the main page finishes successfully;
   - after Wi-Fi returns, allow a short settle interval before Retry probing;
   - if the endpoint is still unavailable, remain on native device-unreachable guidance rather than loading Chromium error content.

5. Stable saved-speaker state
   - persist the last known Online/Offline reachability for saved speakers in app-private preferences;
   - display that last state while background revalidation runs;
   - change green/red only when a completed probe produces a changed result;
   - use a short Unknown status only when no previous result exists;
   - keep card geometry and the model/status + STR-version/IP hierarchy stable during checks.

## Non-goals

- no change to mDNS discovery;
- no change to endpoint probe ports or fallback;
- no new persistence model;
- no new permission;
- no WebView/API contract change;
- no native playback/business logic;
- no Gradle/AGP upgrade.

## Implementation state

**ACCEPTED in 0.2.1 — 2026-09-30**

## Verification and release evidence

Local/source evidence:

- documentation consistency: PASS;
- clean `assembleDebug`, `lintDebug` and `assembleRelease`: PASS;
- exact release source: `6a3352928dc177419f2f5d7f5bc49868e9ab0589`;
- application version: `0.2.1` / versionCode 10;
- GitHub CI on `main`: run `36721458188`, PASS;
- GitHub CI on `v0.2.1`: run `36722290992`, PASS.

Real-device evidence:

- compact Remote header accepted;
- Online/Offline saved-speaker presentation accepted;
- device-card geometry remains stable while background validation runs;
- no-Wi-Fi guidance accepted on Remote and Devices views;
- Android Wi-Fi settings action accepted;
- Wi-Fi return / Retry / endpoint-probe recovery accepted;
- saved-device selection no longer normally exposes Chromium's generic connection-error page;
- normal embedded STR control path remains operational.

Signing/publication evidence:

- final APK: `STR-Remote-0.2.1.apk`;
- SHA-256: `bf019801b4a47226ab1af616f4f74f1dd7abfb71731205253e1d9709af76de67`;
- signer DN: `C=DE, CN=STR Remote`;
- signer certificate SHA-256: `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`;
- APK Signature Scheme v2/v3: PASS;
- post-sign 16 KiB-aware zipalign check: PASS;
- annotated tag: `v0.2.1`;
- tag object: `7347fad1f8693746ec5e806df25cd43d326e42b2`;
- tag target: exact release source commit;
- GitHub Release ID: `400078904`;
- publication time: `2026-09-30T13:33:15Z`;
- public APK digest matches the local accepted artifact.

## Result

BUILD-0011 is complete. 0.2.1 is released and accepted.

The documentation-finalization commit occurs after the release tag and must not trigger a rebuild/replacement of the already published 0.2.1 APK.