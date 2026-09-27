# Status

**Development version:** 0.1.1
**Latest public release:** 0.1.0
**Status:** RELEASE CANDIDATE - signed-artifact verification pending
**Date:** 2026-09-27

## BUILD-0007 verification

Discovery & Network Resilience is implemented and the committed source has passed the available build and device evidence:

- local documentation consistency gate passes;
- local clean debug/lint/release assembly passes;
- Android Studio debug assembly passes without the earlier `allNetworks` deprecation warning;
- GitHub Actions run `36324489315` is successful for source commit `34d64aeb452be4b9d2e30c998e2b57888ff84c95`;
- first Pixel verification showed the appropriate user-facing error states;
- Retry works without restarting the app;
- the upstream SoundTouch Reborn website action opens correctly;
- the post-cleanup Pixel run starts successfully and shows no app crash/ANR in the supplied focused Logcat;
- no new dependency or Android permission was introduced;
- Wi-Fi state is tracked using `ConnectivityManager.NetworkCallback` without an Internet-validation criterion.

## Release finalization

`0.1.0` remains the latest public release until publication of 0.1.1.

Before 0.1.1 can be accepted/public:

1. commit this release-preparation snapshot and obtain green GitHub Actions;
2. build the signed APK from that exact clean commit;
3. verify signature/alignment and record SHA-256;
4. install exactly that signed APK on the Pixel and run the final focused smoke test;
5. tag the source commit as `v0.1.1`;
6. publish the exact APK and SHA-256 file in the GitHub Release;
7. close the remaining publication gates in a post-release documentation commit.
