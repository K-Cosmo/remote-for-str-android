# Planned

## Current step — 0.1.1 Discovery & Network Resilience / BUILD-0007

The 0.1.0 release is complete. The next application change is a focused robustness build for the discovery/connection state machine.

1. Detect whether a usable Wi-Fi/local-network transport is present before probing a saved endpoint or starting mDNS discovery.
2. When Wi-Fi is unavailable, do not start discovery and do not leave an indeterminate spinner running; show a clear same-Wi-Fi requirement and retry action.
3. Keep Internet validation out of the decision: STR is local-LAN functionality and must work on Wi-Fi without Internet access.
4. Give discovery a finite timeout and stop the scan when no STR device is found.
5. On timeout, explain that SoundTouch Reborn must already be installed/running and provide an upstream STR link plus retry/manual-host paths.
6. Distinguish "no STR device discovered" from "speaker discovered but STR endpoint not reachable".
7. Allow retry after Wi-Fi becomes available without requiring an app restart.
8. Verify the focused real-device matrix and converge `/doc` evidence before acceptance.

## After 0.1.1

- evaluate Gradle 9.8.x as a separate maintenance change;
- consider speaker-picker/favorites polish as a separate feature line;
- evaluate Play/F-Droid/distribution improvements separately from app behavior;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.
