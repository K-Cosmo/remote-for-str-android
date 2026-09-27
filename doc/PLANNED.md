# Planned

## Current step — 0.1.1 Discovery & Network Resilience / BUILD-0007

Implementation is complete. The current step is focused build/lint and real-device verification.

1. Run local documentation consistency check.
2. Run clean debug build + lint and release assembly.
3. Verify Wi-Fi-off behavior: no discovery, no indefinite spinner, clear same-Wi-Fi guidance.
4. Verify no-device timeout after the finite discovery window and the STR prerequisite/help link.
5. Verify discovered-but-STR-unreachable is distinct from no-device discovery.
6. Verify normal saved-endpoint/discovery/controls path with the known speaker.
7. Verify Retry recovers after Wi-Fi is enabled without restarting the app.
8. Verify local Wi-Fi without Internet is not rejected solely because Internet validation is absent.
9. Record evidence and converge status/release gates before accepting/publishing 0.1.1.

## After 0.1.1

- evaluate Gradle 9.8.x as a separate maintenance change;
- consider speaker-picker/favorites polish as a separate feature line;
- evaluate Play/F-Droid/distribution improvements separately from app behavior;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.
