# BUILD-0002 — 0.1.2-dev first-device layout convergence

**Status:** IMPLEMENTED  
**Date:** 2026-09-26

## Specification

Converge the smallest native-shell defects demonstrated by the first successful real-device run without changing STR discovery, endpoint probing or upstream web-remote behavior.

## Evidence driving this build

- successful 0.1.1-dev Android Studio build;
- real-device screenshot of discovery/picker state;
- real-device screenshot of loaded STR WebView;
- focused Logcat supplied with the run.

See `../evidence/qa/2026-09-26-build-0001-real-device.md` and `../FINDINGS.md`.

## Constraints

- no new application dependency;
- no STR playback/business logic reimplementation;
- no discovery/API-contract change;
- no broad MainActivity architecture rewrite;
- no guessed Daemon JVM criteria;
- preserve upstream STR page/CSS; only fix the native container geometry;
- runtime permissions remain least-privilege.

## Implemented change set

- root system-bar inset handling;
- component-local padding instead of global content padding;
- full-width/full-height WebView content rectangle;
- single discovery-state header and compact spinner;
- redundant mDNS explanatory line removed;
- speaker-row readiness clutter reduced;
- connected status line only visible with WebView;
- Android 13+ platform back dispatcher plus legacy fallback;
- duplicate hard-coded user-agent versions removed.

## Acceptance evidence required

See `../TESTS.md` and `../RELEASE_GATES.md`. BUILD-0002 remains IMPLEMENTED until a real build/lint/device run verifies the changed geometry and navigation.
