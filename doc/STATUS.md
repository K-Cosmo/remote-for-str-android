# Status

## Remote for STR 0.3.2 — DEVICE QA PASSED / RELEASE PENDING

**Development version:** 0.3.2
**Latest public release:** 0.3.1
**Status:** DEVICE QA PASSED / RELEASE PENDING
**Date:** 2026-10-31

STR-028 staged the compact **Remote STR** Android launcher label (public project and Devices title remain **Remote for STR**), one updated runtime-decoded mailbox for both opt-in email actions, and the public Markdown / Android-source anti-harvesting guard. The user explicitly confirmed **all six signed 0.3.2 device-acceptance checks passed on 2026-10-31**, including in-place update over 0.3.1 with stored devices/rooms retained and functioning controls. Tested app source: `547fe4e52935bac03ddb0c3eb1f1ca4c7e517323`; source CI [push 41203798614](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41203798614) and [PR 41203849504](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41203849504) both passed. The user has **not supplied the local signed APK SHA-256 or signing-script transcript** for GitHub publication provenance. **PR #9 remains Draft/unmerged; no `v0.3.2` tag, GitHub Release or publication approval.** Production `app.strremote.android`, signing compatibility and saved speaker/room data were maintained in the tested upgrade; published **0.3.1** is unchanged. See [BUILD-0014](work/BUILD-0014.md), [dated device evidence](evidence/qa/2026-10-31-build-0014-0.3.2-device-qa.md) and [release gates](RELEASE_GATES.md).

## Remote for STR 0.3.1 — RELEASED / ACCEPTED

**Release date:** 2026-10-31

BUILD-0013 was merged via [PR #6](https://github.com/K-Cosmo/remote-for-str-android/pull/6) and publicly released as **[Remote for STR 0.3.1](https://github.com/K-Cosmo/remote-for-str-android/releases/tag/v0.3.1)** on 2026-10-31 at **09:56:33 UTC** (10:56:33 CET). GitHub Release ID `429849089`, `draft=false`, `prerelease=false`, currently returned by `releases/latest`.

### Release provenance

- **Signed-source commit:** `0e6bab9d9e1bcac19b542755b7a1774c69d2776a` (distinct from the later merge/docs commits); `v0.3.1` is an **annotated tag** with tag-object `dd0480df720d805b0c6b76fd20757b59d0ee5cfe`, verified to target the exact signed source.
- **Version:** `0.3.1` / versionCode `12`; production `applicationId=app.strremote.android` unchanged.
- **Published APK:** [`Remote-for-STR-0.3.1.apk`](https://github.com/K-Cosmo/remote-for-str-android/releases/download/v0.3.1/Remote-for-STR-0.3.1.apk), 1,197,701 bytes, GitHub Asset ID `694988694`.
- **Published APK SHA-256:** `8100334f067346c9987a68969bdebd9065455bfe73c21a0634f0dba42ddbd759`, matching the signed local build and GitHub's independent reported asset digest.
- **Checksum asset:** `Remote-for-STR-0.3.1.apk.sha256.txt`, GitHub Asset ID `694988695`, published and uploaded.
- **Signer:** the release script enforces the historic production certificate SHA-256 `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32` and signature/zipalign verification.
- **CI:** exact signed-source [push 41146044403](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41146044403) and [PR 41146049240](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41146049240) passed; final PR runs `41189139487` and `41189143522` passed; post-merge `main` runs `41189328176` and `41189408057` passed. Normal merge commit `ed7652783a9752e107449a572c5b6f2dff624922` preserved signed-source ancestry.
- **Device acceptance (tester-reported):** six defined checks passed, including the signed v0.3.0 → v0.3.1 in-place update with stored speakers/rooms, control, DE/EN wizard, email composer and restart.
- **Explicitly untested/deferred:** no-email-handler, denied WLAN permission, network failures, unverified Bose configurations and positive future-version update notification; tracked under [STR-027](https://github.com/K-Cosmo/remote-for-str-android/issues/7). **Deferral is not test success.**
- **Privacy provenance:** ordinary branch/tag historical rewrite completed earlier; GitHub-managed legacy PR refs/caches remain a separate cleanup item. Historic signed release assets were not rewritten.

See [0.3.1 publication evidence](evidence/qa/2026-10-31-release-0.3.1.md), [signed-device candidate evidence](evidence/qa/2026-10-31-release-candidate-0.3.1.md), and [release gates](RELEASE_GATES.md).

## 0.3.0 release — ACCEPTED

BUILD-0012 Bose SoundTouch WLAN-Einrichtung is released and accepted.

Release identity:

- version: `0.3.0` / versionCode `11`; package `app.strremote.android`;
- historical APK was signed from pre-rewrite source (original identity retained in encrypted offline mirror); rewritten tag source: `edc64cd5468e60edff355ec1dd1fd73c338004af` (the signed APK was not rebuilt);
- current rewritten annotated tag `v0.3.0` object: `2e0c29414f71ae711b410b8bfe0b1e370b0aa14b`; this is **not** the original signing-time tag identity;
- public [GitHub Release 0.3.0](https://github.com/K-Cosmo/remote-for-str-android/releases/tag/v0.3.0), release ID `429390125`, published 2026-10-30T19:03:45Z;
- signed APK `STR-Remote-0.3.0.apk`, size 1,156,741 bytes, SHA-256 `c4b1989e00ba21915e34192b2f4e0b73695416c93397e4f0c4d3cc6617a18290`;
- companion checksum asset `STR-Remote-0.3.0.apk.sha256.txt`;
- original release-signing certificate SHA-256 `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`.

Acceptance:

- exact release-source `main` CI: success, run [41108758228](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41108758228);
- exact release-source `v0.3.0` tag CI: success, run [41111993535](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41111993535);
- local signing script reported matching original signing certificate, signature verification and alignment checks;
- user confirmed that the signed production APK passed the planned smartphone acceptance test;
- public APK asset SHA-256 is an exact match to the locally signed SHA-256;
- production STR control remains compatible with Android 8+, the new WLAN assistant requires Android 10+;
- SoundTouch 20 III setup verified on the reference original firmware; SoundTouch 10 original-firmware WLAN setup remains experimental.

See [release evidence](evidence/qa/2026-10-30-release-0.3.0.md) and [release gates](RELEASE_GATES.md).

## 0.2.1 release

BUILD-0011 is released and accepted.

Release identity:

- current rewritten tag source commit: `8afa46d25e5ec2be10bf84c07df071eb555b0ed2` (signed APK predates history rewrite);
- annotated tag: `v0.2.1`;
- rewritten annotated tag object: `f3b020247bb195ee1c9c9d326c8590aa75743ff6`;
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

- current rewritten tag source commit: `945bb9d7cfaa9f737fd6fb8d24780ada571249f0` (signed APK predates history rewrite);
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

`0.3.0` remains an accepted **historical** public release; the current release is **0.3.1** (see the release identity and acceptance evidence above).


These dated paragraphs describe pre-release testing and are superseded by the accepted 0.3.1 release evidence above.

## Historical development notes

BUILD-0013's dated preparation, UI, locale and failed-test history is preserved in [BUILD-0013](work/BUILD-0013.md) and [the signed-candidate QA record](evidence/qa/2026-10-31-release-candidate-0.3.1.md). These are not current release blockers or pending acceptance requirements; current public status is defined at the top of this document.
