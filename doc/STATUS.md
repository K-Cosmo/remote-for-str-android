# Status

## Development brand (PR #5 — NOT RELEASED)

The product name **Remote for STR** has been selected and implemented in the active branch, with DE/EN launcher/title and future APK naming. The public **STR Remote 0.3.0** release, original Android package `app.strremote.android`, signing certificate, checksum and saved-app upgrade identity remain unchanged. Next signed release artifacts will be `Remote-for-STR-<version>.apk`, with the updater also accepting the old `STR-Remote-<version>.apk` form. GitHub repository currently remains at `K-Cosmo/str-remote-android` pending a separate repository-admin rename; no API/documentation links have yet been switched. The new branding, English wizard and full real-device release gates are not yet accepted.

**Development version:** 0.3.0
**Latest public release:** 0.3.0
**Status:** RELEASED / ACCEPTED
**Date:** 2026-10-30

## 0.3.0 release — ACCEPTED

BUILD-0012 Bose SoundTouch WLAN-Einrichtung is released and accepted.

Release identity:

- version: `0.3.0` / versionCode `11`; package `app.strremote.android`;
- signed APK source commit: `5b351f04dc9f989eee48fc262764f3add8641573`;
- annotated tag `v0.3.0`, tag object `a071b2b6a4cbb0c6b5fb528cbe33f088969365ed`, resolving to the exact source commit;
- public [GitHub Release 0.3.0](https://github.com/K-Cosmo/str-remote-android/releases/tag/v0.3.0), release ID `429390125`, published 2026-10-30T19:03:45Z;
- signed APK `STR-Remote-0.3.0.apk`, size 1,156,741 bytes, SHA-256 `c4b1989e00ba21915e34192b2f4e0b73695416c93397e4f0c4d3cc6617a18290`;
- companion checksum asset `STR-Remote-0.3.0.apk.sha256.txt`;
- original release-signing certificate SHA-256 `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`.

Acceptance:

- exact release-source `main` CI: success, run [41108758228](https://github.com/K-Cosmo/str-remote-android/actions/runs/41108758228);
- exact release-source `v0.3.0` tag CI: success, run [41111993535](https://github.com/K-Cosmo/str-remote-android/actions/runs/41111993535);
- local signing script reported matching original signing certificate, signature verification and alignment checks;
- user confirmed that the signed production APK passed the planned smartphone acceptance test;
- public APK asset SHA-256 is an exact match to the locally signed SHA-256;
- production STR control remains compatible with Android 8+, the new WLAN assistant requires Android 10+;
- SoundTouch 20 III setup verified on the reference original firmware; SoundTouch 10 original-firmware WLAN setup remains experimental.

See [release evidence](evidence/qa/2026-10-30-release-0.3.0.md) and [release gates](RELEASE_GATES.md).

## 0.2.1 release

BUILD-0011 is released and accepted.

Release identity:

- source commit: `6a3352928dc177419f2f5d7f5bc49868e9ab0589`;
- annotated tag: `v0.2.1`;
- annotated tag object: `7347fad1f8693746ec5e806df25cd43d326e42b2`;
- final APK: `STR-Remote-0.2.1.apk`;
- APK SHA-256: `bf019801b4a47226ab1af616f4f74f1dd7abfb71731205253e1d9709af76de67`;
- signer: `C=DE, CN=STR Remote`;
- signer certificate SHA-256: `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`;
- public GitHub release ID: `400078904`;
- publication time: `2026-09-30T13:33:15Z`.

Verified release gates:

- local documentation consistency: PASS;
- clean debug build + lint + release assembly: PASS;
- GitHub Android CI green for the exact release source commit on `main` (run `36721458188`);
- GitHub Android CI green for the pushed `v0.2.1` tag (run `36722290992`);
- real-device UX/network smoke for the final 0.2.1 implementation: PASS;
- compact Remote header and stable saved-speaker card geometry: PASS;
- persisted Online/Offline state and changed-result-only color transitions: PASS;
- no-Wi-Fi guidance plus direct Wi-Fi settings action on Remote and Devices: PASS;
- Wi-Fi return / endpoint probe / Retry recovery path: PASS;
- selected-device endpoint probing prevents the Chromium error page from becoming the normal user-facing transition: PASS;
- signed APK built from clean committed source;
- APK Signature Scheme v2/v3 verification: PASS;
- post-sign 16 KiB-aware zipalign verification: PASS;
- annotated `v0.2.1` tag resolves to exact release source `6a3352928dc177419f2f5d7f5bc49868e9ab0589`;
- GitHub release is published, non-draft and non-prerelease;
- public APK asset digest matches the accepted local artifact.

0.2.1 extends the accepted 0.2.0 speaker-management baseline with:

- a more compact native Remote header;
- explicit and accessible saved-speaker Online/Offline presentation;
- persistent last-known reachability used while background validation runs;
- stable device-card layout during revalidation;
- native no-Wi-Fi guidance on both major app views;
- direct Android Wi-Fi settings access;
- endpoint-first reconnect/device-selection behavior before WebView presentation.

The existing app-private preferences store is extended with last-known reachability state; no new persistence dependency/model, permission, cloud service, analytics backend, playback/business logic, Gradle or AGP upgrade is introduced.

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

`0.3.0` is the current accepted public release (see release identity and acceptance evidence above).


## Pending BUILD-0013 development (not in v0.3.0)

The in-app GitHub release notification, optional compact STR WebView header and ST10 test-profile validation wording are implemented in development; they are not included in the signed, published `v0.3.0` APK. Physical-device WebView spacing and controls regression testing remains required before the next signed update. Separate SoundTouch 10 original-firmware WLAN test-profile success was reported on 2026-10-30, after the v0.3.0 release validation record.


## BUILD-0013 extension — pending device acceptance

The working development branch includes the unknown-model selection, offline historical SoundTouch model suggestions and an opt-in E-Mail feedback form. The 10 CSS px STR header spacing is user-accepted visually on the supplied screenshot; the community-feedback workflow, especially its early-failure path, needs real-device testing. This is **not** part of public v0.3.0. No release or signed APK is created from this branch yet.

## BUILD-0013 usability and locale follow-up (2026-10-30)

Tester screenshots identified unreadable black-on-black SoundTouch autocomplete suggestions and excessive first-page/feedback-page content, plus German-only wizard texts despite intended DE/EN product support. The development branch now includes white bounded model suggestions, compact no-scroll first/feedback screens, and a bilingual Wizard string-resource conversion. The published signed 0.3.0 is unchanged; phone UI and bilingual acceptance remain open.
