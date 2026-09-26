# Planned

## Current step — 0.1.0-rc3 release packaging / BUILD-0005

Feature work is frozen unless new real-device evidence identifies a release blocker.

1. Apply rc3 launcher/daemon packaging convergence.
2. Ensure the development shell has a Java launcher (`JAVA_HOME`/`PATH`); daemon criteria alone do not bootstrap `gradlew`.
3. Run `./gradlew --version` and verify Gradle 9.7.1 plus daemon Java 25.
4. Run `clean :app:assembleDebug :app:lintDebug --stacktrace`; retain lint output.
5. Run `:app:assembleRelease --stacktrace`.
6. Visually verify normal and themed launcher icon.
7. Explicitly verify native back navigation, reconnect, permission denial/recovery and representative STR controls.
8. Configure release signing without storing signing secrets in the repository.
9. Build/install/test the exact signed publication artifact.
10. Converge `/doc`, then tag the final public `0.1.0` release.

## After 0.1.0

- evaluate Gradle 9.8.x as a separate maintenance change;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes when they become available;
- consider further picker polish only from real user/device findings;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.
