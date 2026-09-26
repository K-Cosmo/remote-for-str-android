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

## Required evidence

- final clean build/lint/release from version 0.1.0;
- signed APK generated with the private local release key;
- `apksigner verify --verbose --print-certs` succeeds;
- SHA-256 recorded;
- exact signed APK installed and smoke-tested;
- GitHub CI green for the final source commit.
