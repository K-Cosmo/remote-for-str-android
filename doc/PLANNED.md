# Planned

## Current step - 0.2.0 speaker management verification / BUILD-0009

The BUILD-0009 implementation now includes persistent saved speakers, quick switching and optional room assignment. No further feature scope should be added before verification.

1. Run the local documentation consistency gate.
2. Run clean debug build, lint and release assembly.
3. Install the debug APK on the Pixel.
4. Verify saved-speaker persistence and restart behavior.
5. Verify all six standard rooms plus a custom room label.
6. Verify saved/discovered de-duplication, current-speaker marking and remove behavior.
7. Verify an offline saved speaker remains visible/actionable.
8. Verify manual-host and 0.1.1 Wi-Fi/discovery/WebView regressions.
9. Commit only after the local build/device gate is green, then verify GitHub Android CI.
10. Converge evidence before release preparation.

## Deferred maintenance - BUILD-0008

Gradle 9.8.x evaluation remains deferred. The accepted Gradle 9.7.1 + AGP 9.4.1 baseline stays in use for 0.2.0 unless a concrete compatibility/build requirement appears.

## Later

- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.
