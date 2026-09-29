# Planned

## Current state — 0.2.0 released and accepted

BUILD-0009 Speaker Management / Rooms is complete. The public `v0.2.0` release is published from source commit `b5ff103e318f4342ba3a748b786084108d4093af`.

Do not add follow-up changes to the 0.2.0 release artifact or move the `v0.2.0` tag. New work starts in a new build/version line.

## Deferred maintenance — BUILD-0008

Gradle 9.8.x evaluation is now eligible for reconsideration because 0.2.0 is complete, but it remains deferred until explicitly scheduled or a concrete compatibility/build requirement appears. The accepted baseline remains Gradle 9.7.1 + AGP 9.4.1.

## Later

- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.
