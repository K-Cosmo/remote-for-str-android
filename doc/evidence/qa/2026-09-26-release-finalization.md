# 2026-09-26 — 0.1.0 release finalization evidence

## Local rc3 evidence

- `clean :app:assembleDebug :app:lintDebug` succeeds.
- lint result: 0 errors / 1 warning.
- remaining warning is the intentionally deferred Gradle 9.8 availability notice; project baseline remains 9.7.1 for 0.1.0.
- `:app:assembleRelease` succeeds.
- adaptive icon is visually verified on the real device.
- back navigation, reconnect after restart, permission denial/recovery and representative STR controls were explicitly smoke-tested successfully.

## Independent GitHub CI evidence

GitHub Actions run for the public repository independently executed on Ubuntu 24.04 with Temurin Java 25 and Gradle 9.7.1:

- Gradle Wrapper validation succeeded.
- `assembleDebug` succeeded.
- `lintDebug` succeeded.
- `assembleRelease` succeeded.
- overall build result: `BUILD SUCCESSFUL`.
- debug APK artifact upload succeeded.

A follow-up CI-only change updated checkout/setup-java/setup-gradle/upload-artifact to current Node-24-based action releases. The subsequent CI run remained green and uploaded the debug artifact successfully.

## Final release transition

- promote `0.1.0-rc3` / versionCode 6 to `0.1.0` / versionCode 7;
- keep Gradle 9.7.1 and Java-25 daemon criteria unchanged;
- keep feature set frozen;
- release signing key remains local/outside the repository.

## Remaining evidence

The only remaining acceptance evidence is generated from the exact final source commit:

1. build signed `0.1.0` APK;
2. verify APK signature and record SHA-256;
3. install that exact APK on the real device;
4. smoke-test discovery/connection/control;
5. tag/publish only after the final source commit also has green GitHub CI.
