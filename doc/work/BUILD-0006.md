# BUILD-0006 — 0.1.0 final release

## Goal

Promote the verified rc3 implementation to the first public `0.1.0` release without changing application behavior.

## Inputs

- rc3 local clean build/lint/release evidence;
- rc3 lint: 0 errors / 1 informational Gradle-version warning;
- successful real-device icon/navigation/reconnect/permission/control checks;
- green public GitHub Actions debug/lint/release build;
- local release signing identity created outside the repository.

## Changes

- versionCode 6 -> 7;
- versionName `0.1.0-rc3` -> `0.1.0`;
- converge release status/gates/evidence documentation;
- add a local signing helper that never stores key material or passwords in the repository.

## Explicit non-goals

- no application feature changes;
- no STR discovery/API changes;
- no dependency changes;
- no Gradle upgrade;
- no signing secret or keystore committed to Git.

## Acceptance evidence

- final source commit: `4583e27deb734a5ac268af0c16939009f76c0d63`;
- annotated tag `v0.1.0` resolves to that commit;
- final GitHub Actions run for the source commit is successful;
- release-tag GitHub Actions run is successful;
- signed APK signature verification succeeds with one release signer;
- publication APK SHA-256: `5f48f99f604b7d861f305f4fda0b6383188ded6e45dc660d65d66ea68da8a2fe`;
- GitHub Release `v0.1.0` publishes that APK and the matching hash file;
- exact signed APK installs on the real device;
- App Info reports version `0.1.0`;
- discovery, connection and normal STR controls work with the publication APK.

## State

**ACCEPTED.** BUILD-0006 is closed. The next application work item is BUILD-0007 / 0.1.1 Discovery & Network Resilience.
