# Status

**Development version:** 0.2.1
**Latest public release:** 0.2.0
**Status:** IMPLEMENTED — VERIFICATION PENDING
**Date:** 2026-09-30

## 0.2.1 development

BUILD-0011 implements focused UX refinements:

- a compact two-line native banner above the embedded STR remote, with the comfortable Devices/Remote button size restored and slightly more vertical spacing;
- stronger saved-speaker reachability presentation: reachable/online = green, unreachable from the app = red, with text labels so color is not the only cue;
- a clearer device-card hierarchy: `model · status` on the second line and `STR version · IP address` below in smaller text;
- native no-Wi-Fi guidance with Retry and direct Wi-Fi settings access on both Remote and Devices views, preventing Chromium's generic connection-error page from becoming the user-facing fallback;
- endpoint probing before recovery/device-selection WebView loads, with the WebView kept hidden until a successful page finish;
- persisted last-known saved-speaker reachability so background checks no longer make device cards visually jump through a transient neutral/checking state.

The implementation intentionally does not change STR discovery, endpoint probing, persistence, WebView security boundaries, playback behavior, permissions, Gradle or AGP.

Required before ACCEPTED:

- local documentation consistency;
- clean debug + lint + release assembly;
- GitHub CI on committed 0.2.1 source;
- Pixel smoke for compact header and green/red saved-speaker states;
- signed final APK verification/publication if 0.2.1 is released publicly.

## 0.2.0 release

Speaker Management / Rooms is released and accepted.

Release identity:

- source commit: `b5ff103e318f4342ba3a748b786084108d4093af`;
- annotated tag: `v0.2.0`;
- final APK: `STR-Remote-0.2.0.apk`;
- APK SHA-256: `5e7ba21f39752ae359a6de0103a440b363a1823223874778e44173f285243aa7`;
- signer: `C=DE, CN=STR Remote`;
- signer certificate SHA-256: `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`;
- public GitHub release ID: `399416342`;
- publication time: `2026-09-29T18:27:43Z`.

## Verified release gates

- local documentation consistency: PASS;
- clean debug build + lint + release assembly: PASS;
- GitHub Android CI green for the exact release source commit and release tag;
- signed APK built from clean committed source;
- APK Signature Scheme v2/v3 verification: PASS;
- post-sign 16 KiB-aware zipalign verification: PASS;
- annotated `v0.2.0` tag resolves to the exact release source;
- public GitHub APK digest matches the accepted local artifact;
- exact signed-APK device smoke: PASS.

## Accepted product baseline

0.2.0 includes:

- finite Wi-Fi-aware STR discovery/recovery;
- persistent saved speakers and quick switching;
- built-in/custom room assignment;
- explicit room/delete device actions;
- manual-host fallback;
- current-device/room presentation and direct Remote return path;
- dismissible local device-management guidance;
- constrained STR WebView wrapper with no cloud/account/analytics backend.

Accepted build baseline:

- minSdk 26;
- compile/target API 37;
- AGP 9.4.1;
- Gradle 9.7.1;
- Java 25 Gradle runtime criteria;
- Java source/target 17.

## Documentation baseline

Post-0.2.0 documentation drift has been converged:

- `/doc` remains the only normative documentation;
- root duplicate changelog is removed;
- root privacy/contribution files are pointers rather than duplicate policy;
- current architecture, compatibility, blocker, privacy, security and test documents describe the accepted 0.2.0 baseline;
- historical build/spec/evidence records remain unchanged as dated snapshots.

`0.2.0` is the current accepted public release.
