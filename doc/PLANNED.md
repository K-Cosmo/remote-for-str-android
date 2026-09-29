# Planned

## Current step - 0.2.0 speaker management / BUILD-0009

Implement a small, persistent speaker-management layer on top of the existing discovery list.

1. Preserve the current fast startup path: probe the last successful endpoint first.
2. Extend the device view so explicitly saved speakers remain visible even when they are not currently discovered.
3. Add explicit favorite/save and remove actions without creating a separate database layer.
4. Merge saved and currently discovered speakers without duplicate rows.
5. Keep the current speaker clearly identifiable and allow quick switching.
6. Let a successfully probed manual host be saved explicitly.
7. Preserve the existing no-Wi-Fi, timeout, unreachable, WebView and security behavior.
8. Bump to 0.2.0 / next versionCode only when implementation starts.
9. Verify with the real speaker plus saved/offline entries, build/lint/release assembly and a focused device smoke test.

BUILD-0009 must stay inside Android wrapper concerns. It must not add playback/business logic, background discovery or a new persistence framework.

## Deferred maintenance - BUILD-0008

Gradle 9.8.x evaluation is deferred. The accepted Gradle 9.7.1 + AGP 9.4.1 baseline remains in use for the 0.2.0 feature line.

Revisit build-tool maintenance after 0.2.0, or earlier only when a concrete trigger exists: AGP/Android Studio compatibility, a relevant Gradle fix, a reproducible build issue or a feature that actually requires the newer Gradle line.

## Later

- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.
