# Planned

## Current step - 0.1.1 release finalization / BUILD-0007

The feature implementation, local build/lint/release assembly and committed-source GitHub CI are green. Current work is release finalization only.

1. Commit the release-preparation documentation/tooling snapshot.
2. Confirm GitHub Actions is green for that exact commit.
3. Run the signing tool from the clean committed tree.
4. Record signature verification, post-sign alignment and SHA-256.
5. Install exactly the signed APK and run the focused final Pixel smoke test.
6. Create annotated tag `v0.1.1` on the exact source commit used to build the APK.
7. Publish the APK and matching SHA-256 file in GitHub Releases.
8. Close BUILD-0007 and publication evidence in a post-release documentation commit.

Do not add further application behavior to 0.1.1 unless a release-blocking defect is found.

## After 0.1.1

- evaluate Gradle 9.8.x as a separate maintenance change;
- consider speaker-picker/favorites polish as a separate feature line;
- evaluate Play/F-Droid/distribution improvements separately from app behavior;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.
