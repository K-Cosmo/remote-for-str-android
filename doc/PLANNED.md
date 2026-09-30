# Planned

## Current state — post-0.2.1

`0.2.1` is released and accepted. No functional release line is currently in progress.

The next change should start with a separate BUILD record and should not be mixed into the accepted 0.2.1 source/tag.

## Deferred maintenance — BUILD-0008

Gradle 9.8.x evaluation remains deferred. The accepted build baseline stays Gradle 9.7.1 + AGP 9.4.1 until a dedicated maintenance build proves the upgrade.

## Later candidates

- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.