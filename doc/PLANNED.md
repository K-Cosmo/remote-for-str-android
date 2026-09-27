# Planned

## Current step - 0.1.2 maintenance / BUILD-0008

Evaluate the Gradle 9.8.x line as an isolated maintenance change after the accepted 0.1.1 release.

1. Confirm the current stable Gradle/AGP compatibility matrix.
2. Change only the Gradle baseline required for the evaluation.
3. Run the local documentation consistency gate.
4. Run clean debug build, lint and release assembly.
5. Compare warnings/behavior with the accepted 0.1.1 baseline.
6. Keep the upgrade only if the real build evidence is clean and there is a concrete maintenance benefit.

Do not combine BUILD-0008 with application features.

## Later

- BUILD-0009 / 0.2.0: speaker picker, multi-speaker and favorites polish based on device evidence;
- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.
