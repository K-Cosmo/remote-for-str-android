# BUILD-0009 - 0.2.0 Speaker Management

## Goal

Turn the transient discovery list into lightweight persistent speaker management while keeping STR Remote a thin Android wrapper.

## Implemented behavior

- versionCode 9 / versionName `0.2.0`;
- the last-successful-endpoint startup path remains unchanged;
- explicitly saved speakers persist in Android app-private SharedPreferences;
- saved speakers remain visible independently of mDNS and remain present when unreachable/offline;
- saved and discovered representations merge into one row;
- the current speaker is marked and rows use the existing probe/load path for switching;
- reachable discovered/current speakers can be saved and saved speakers can be removed;
- successful manually entered endpoints can be saved through the same speaker action;
- room assignment is optional and belongs only to saved-speaker UI metadata;
- built-in room choices: Wohnzimmer, Schlafzimmer, Küche, Bad, Kinderzimmer and Garten, with localized English display strings;
- users can enter an arbitrary custom room label;
- built-in rooms persist stable IDs rather than localized labels;
- room labels are shown with saved speakers and in connected status;
- each device row exposes an obvious inline room button; assigning/changing a room implicitly saves the speaker;
- saved speakers expose a visible delete icon with an explicit confirmation dialog and disappear immediately from the current list after confirmation;
- highlighted hint/state panels replace the tiny footer-only guidance; common actions do not depend on long-press gestures;
- the remote screen keeps only a compact selected-speaker label rather than a large highlighted status banner; the device/room label uses larger bold text for better visibility;
- device rows remain explicitly tappable for connect/switch despite their Room/Delete child controls;
- when a speaker is active, the device-view toolbar exposes `Fernbedienung` as an obvious return path;
- the primary row line is `device name · room`; model/STR version and IP are displayed below at smaller sizes;
- the selected/current device indicator uses the highlighted card state only; no extra bullet is shown before the title.
- the highlighted usage hint can be dismissed through an explicit text link and remains hidden on later launches;
- device-list spacing remains visually balanced after the hint is dismissed.
- no Room database, serialization dependency, cloud sync, background service, new permission or playback/business logic is added;
- Gradle 9.7.1 + AGP 9.4.1 remains unchanged.

## Identity rule

When both endpoints have a real discovery key, equality is based on that key. If one side has no discovery key (for example a manually saved host), host equality is the fallback. This prevents duplicate saved/discovered rows while avoiding host-only merging of two keyed devices.

## Verification state

**RELEASED — ACCEPTED in 0.2.0.**

Release evidence:

- exact source commit: `b5ff103e318f4342ba3a748b786084108d4093af`;
- local clean debug/lint/release gate: PASS;
- GitHub Android CI: main run `36610855156` PASS; tag run `36611841390` PASS;
- signed APK verification: PASS (v2/v3, signer `C=DE, CN=STR Remote`, certificate `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`);
- post-sign 16 KiB-aware zipalign: PASS;
- final APK SHA-256: `5e7ba21f39752ae359a6de0103a440b363a1823223874778e44173f285243aa7`;
- annotated `v0.2.0` tag resolves to `b5ff103e318f4342ba3a748b786084108d4093af`;
- public GitHub release publishes the exact APK and checksum file;
- public APK asset digest matches the accepted local artifact;
- final signed-APK device smoke confirmed during post-release closeout.

BUILD-0009 is closed. Future changes require a new build/version line.
