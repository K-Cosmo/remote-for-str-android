# BUILD-0001 — 0.1.1-dev baseline convergence

**Status:** IMPLEMENTED  
**Date:** 2026-09-26

## Specification

Converge the initial MVP after real build feedback: fix the Kotlin visibility error, align the build stack to the observed working Gradle version, adopt current stable AGP patch level, remove the deprecated project property, evaluate daemon JVM criteria without guessing the local JVM, and establish the agreed `/doc` + Spec Kit governance baseline.

## Constraints

- no application dependency added
- no playback/business logic change
- no discovery/API contract change
- no broad MainActivity/UI refactor
- no guessed local Gradle daemon JVM pin
- internal speaker model remains internal
- real build/device evidence required for verification

## Changed files

- `build.gradle.kts`
- `gradle.properties`
- `app/build.gradle.kts`
- `app/src/main/kotlin/.../MainActivity.kt`
- `app/src/main/kotlin/.../EndpointProbe.kt`
- `.github/workflows/android.yml`
- root documentation entry points
- `/doc/*` governance baseline
- `.specify/` and `specs/001-baseline-convergence/` process artifacts

## Acceptance evidence required

See `../TESTS.md` and `../RELEASE_GATES.md`. No ACCEPTED claim until a real build/lint and device run are attached under `../evidence/qa/`.

## Real-device evidence received 2026-09-26

Confirmed:

- `:app:assembleDebug` PASS in 6 seconds;
- APK install/start PASS;
- ST09 discovered as SoundTouch 10;
- STR v0.9.86 metadata visible;
- port 8888 endpoint resolved;
- embedded STR WebView loads.

The same evidence exposed native layout defects (F-004 through F-006) and a deprecated back API warning (F-007). BUILD-0001 therefore remains not ACCEPTED and feeds BUILD-0002 rather than being declared complete by compilation alone.
