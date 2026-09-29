# Status

**Development version:** 0.2.0
**Latest public release:** 0.1.1
**Status:** RELEASE CANDIDATE — committed-source CI and signed-artifact verification pending
**Date:** 2026-09-29

## BUILD-0009 release-candidate state

Speaker Management / Rooms for 0.2.0 is implemented and locally verified:

- versionName `0.2.0` / versionCode `9`;
- persistent saved-speaker metadata remains app-private in SharedPreferences;
- room assignment includes six localized presets plus custom room text;
- room assignment is an explicit inline action and implicitly saves the speaker;
- saved speakers expose a delete icon with confirmation;
- device rows remain directly tappable for connect/switch;
- the active device is card-highlighted without an extra title bullet;
- device name + room are primary row information; model/version and host are secondary;
- an active device view exposes `Fernbedienung` as the direct return path;
- selected device/room on the remote page is larger and bold without a full-width status banner;
- the highlighted usage hint can be dismissed and that preference persists;
- device-list spacing remains balanced with the hint visible or hidden;
- no database, cloud service, background discovery, new Android permission or third-party dependency was added;
- accepted Gradle 9.7.1 + AGP 9.4.1 remains unchanged.

## Verified locally

- documentation consistency: PASS;
- clean `assembleDebug + lintDebug + assembleRelease`: PASS;
- latest clean build after the final spacing refinement: PASS;
- Pixel UX path confirmed: room assignment, delete flow, speaker selection, remote return, current-speaker presentation, dismissible hint/persistence and final device-list spacing.

## Remaining release gates

Before public 0.2.0 publication:

1. commit and push the exact release-candidate source;
2. confirm GitHub Android CI for that exact commit;
3. build/sign 0.2.0 from that clean committed source;
4. install and smoke-test the exact signed APK, including the remaining persistence/offline/manual-host regression cases where practical;
5. tag the exact signed source commit as `v0.2.0`;
6. publish the exact signed APK plus SHA-256 file;
7. perform the post-release documentation closeout.

`0.1.1` remains the latest public accepted release until those publication gates complete.
