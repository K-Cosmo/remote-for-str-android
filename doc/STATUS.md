# Status

## Remote for STR 0.3.1 — signed, phone-accepted release candidate on PR #6

The product name **Remote for STR** has been selected and implemented in the active branch, with DE/EN launcher/title and future APK naming. The public **STR Remote 0.3.0** release, original Android package `app.strremote.android`, signing certificate, checksum and saved-app upgrade identity remain unchanged. Next signed release artifacts will be `Remote-for-STR-<version>.apk`, with the updater also accepting the old `STR-Remote-<version>.apk` form. GitHub repository has now been renamed to `K-Cosmo/remote-for-str-android`; active release-check API, project links and documentation target the new canonical address. The user confirmed English Wi-Fi wizard device QA (2026-10-30), then accepted all six checks of the signed 0.3.1 APK on a real phone (2026-10-31). Unexercised negative cases were explicitly deferred by the owner for 0.3.1 on 2026-10-31; they remain NOT TESTED. CI/normal-merge approval was given; public release authorisation remains separate.

**Signed release candidate:** `0.3.1` / `versionCode=12` was built and signed locally using `tools/build-signed-release.ps1` from **exact commit** `0e6bab9d9e1bcac19b542755b7a1774c69d2776a`. User-reported artifact: `dist/Remote-for-STR-0.3.1.apk`, SHA-256 `8100334f067346c9987a68969bdebd9065455bfe73c21a0634f0dba42ddbd759`; local checksum file exists. The signed APK completed all six on-phone acceptance checks, including upgrade over the existing 0.3.0 preserving speakers/rooms. **No `v0.3.1` tag or public Release exists yet.** Published `0.3.0` remains ACCEPTED and unchanged. Production `applicationId=app.strremote.android` remains stable. See [release-candidate evidence](evidence/qa/2026-10-31-release-candidate-0.3.1.md).

**Git history state:** both normal branch refs and all five release tags have been rewritten; six stale branches deleted. Original signed APK/checksum assets are unchanged. GitHub-managed old `refs/pull/*` remain outside the ordinary ref rewrite; see [provenance](GIT_HISTORY_PROVENANCE.md).

**Development version:** 0.3.1
**Latest public release:** 0.3.0
**Status:** IN DEVELOPMENT / RELEASE PREPARATION
**Date:** 2026-10-31

**Exact signed-source CI:** [push 41146044403](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41146044403) and [pull request 41146049240](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41146049240) both passed for `0e6bab9d9e1bcac19b542755b7a1774c69d2776a`. PR #6 has explicit owner approval for normal merge after final green CI. Untested negative cases are scoped/deferred to follow-up, not marked as passed; public Release and tag remain separately unapproved. Later documentation-only commits are not part of the APK source.

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

`0.3.0` is the current accepted public release (see release identity and acceptance evidence above).


## Pending BUILD-0013 development (not in v0.3.0)

The in-app GitHub release notification, optional compact STR WebView header and ST10 test-profile validation wording are implemented in development; they are not included in the signed, published `v0.3.0` APK. Physical-device WebView spacing and controls regression testing remains required before the next signed update. Separate SoundTouch 10 original-firmware WLAN test-profile success was reported on 2026-10-30, after the v0.3.0 release validation record.


## BUILD-0013 extension — pending device acceptance

The working development branch includes the unknown-model selection, offline historical SoundTouch model suggestions and an opt-in E-Mail feedback form. The 10 CSS px STR header spacing is user-accepted visually on the supplied screenshot; the community-feedback workflow, especially its early-failure path, needs real-device testing. This is **not** part of public v0.3.0. No release or signed APK is created from this branch yet.

## BUILD-0013 usability and locale follow-up (2026-10-30)

Tester screenshots identified unreadable black-on-black SoundTouch autocomplete suggestions and excessive first-page/feedback-page content, plus German-only wizard texts despite intended DE/EN product support. The development branch now includes white bounded model suggestions, compact no-scroll first/feedback screens, and a bilingual Wizard string-resource conversion. The published signed 0.3.0 is unchanged; phone UI and bilingual acceptance remain open.
