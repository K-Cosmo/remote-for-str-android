# Status

**Development version:** 0.1.0  
**Status:** FINAL RELEASE CANDIDATE; signed-publication-artifact verification pending  
**Date:** 2026-09-26

## Verified product path

Real workstation/device evidence verifies the STR Remote 0.1.0 product path:

- Gradle Wrapper 9.7.1 is generated, committed and independently validated by GitHub Actions;
- Gradle 9.7.1 runs with Java 25 daemon criteria; workstation uses Android Studio JBR 25 and CI uses Temurin 25;
- rc3 clean `assembleDebug + lintDebug` succeeds;
- rc3 lint reports **0 errors and 1 informational Gradle-version warning**;
- rc3 `assembleRelease` succeeds;
- GitHub Actions independently completes `assembleDebug`, `lintDebug` and `assembleRelease` on Ubuntu 24.04;
- the APK installs and starts on a real Android device;
- STR discovery finds the real speaker and endpoint probing resolves the live STR remote;
- the embedded STR web UI loads and normal remote controls work;
- normal and themed/adaptive launcher icon behavior is verified on the real device;
- native back navigation is verified;
- reconnect after app restart is verified;
- local-network permission denial/recovery is verified.

## 0.1.0 finalization

The final source version is `0.1.0` / versionCode `7`.

Release signing identity has been created locally and remains outside the repository. No private signing key or password is committed or required by normal CI.

## Remaining final release evidence

Before tagging/publication:

- build the exact `0.1.0` signed release APK from the final source commit;
- verify the APK signature and SHA-256;
- install that exact signed APK on the real device and smoke-test discovery/connection/control once;
- confirm final GitHub CI is green for the same source commit;
- tag `v0.1.0` and publish the signed APK as the GitHub Release asset.

Feature work remains frozen until the first public release is complete.
