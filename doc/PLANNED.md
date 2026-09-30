# Planned

## Current state — 0.2.1 UX refinement

BUILD-0011 is implemented and awaiting verification.

Scope:

- compact the native title/device/status banner so the embedded STR remote receives more vertical space;
- show saved speakers as green when a completed probe confirms STR reachability;
- show saved speakers as red when a completed probe confirms STR is unreachable;
- show explicit Online/Offline text so device state is not communicated by color alone;
- keep unknown/checking state neutral.

The public 0.2.0 release remains unchanged until 0.2.1 completes release gates.

## Deferred maintenance — BUILD-0008

Gradle 9.8.x evaluation remains deferred. The accepted build baseline stays Gradle 9.7.1 + AGP 9.4.1.

## Later

- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.