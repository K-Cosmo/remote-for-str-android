# Planned

## Current step — 0.1.0 final publication / BUILD-0006

Feature work remains frozen until the first public release is complete.

1. Apply the final source promotion to `0.1.0` / versionCode 7.
2. Run final clean debug + lint + release assembly through the verified Wrapper.
3. Build the signed APK locally with the private release keystore; do not store signing secrets in Git.
4. Verify APK signature and SHA-256.
5. Install and smoke-test that exact signed APK on the real device.
6. Push the final source commit and require green GitHub CI for the same source.
7. Converge the final artifact hash/evidence, tag `v0.1.0`, and publish the signed APK as the GitHub Release asset.

## After 0.1.0

- evaluate Gradle 9.8.x as a separate maintenance change;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes when they become available;
- consider further picker polish only from real user/device findings;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.
