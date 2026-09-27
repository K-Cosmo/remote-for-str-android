# BUILD-0007 - 0.1.1 Discovery & Network Resilience

## Goal

Make discovery/connection behavior finite and actionable when the Android device is not on Wi-Fi, no STR speaker is present, or an announced speaker has no reachable STR endpoint.

## Implemented scope

- versionCode 8 / versionName `0.1.1`;
- Wi-Fi transport tracked with `ConnectivityManager.NetworkCallback` and a Wi-Fi `NetworkRequest`;
- no deprecated `ConnectivityManager.allNetworks` polling;
- no Internet-validation requirement;
- saved-endpoint, discovery and manual probing gated on Wi-Fi;
- finite 10-second mDNS discovery window;
- no-Wi-Fi, no-device and STR-unreachable states;
- Retry plus upstream STR website action;
- per-row checking versus completed unreachable endpoint state;
- stale endpoint-probe callbacks ignored across discovery generations;
- existing permission scope, STR service types/ports and WebView navigation restrictions preserved.

## Verification evidence

- local documentation consistency: PASS;
- local clean `assembleDebug + lintDebug + assembleRelease`: PASS;
- Android Studio `assembleDebug`: PASS without the previous `allNetworks` deprecation warning;
- Pixel functional observation: appropriate error states, Retry and STR website action behave as intended;
- post-cleanup Pixel Logcat: application starts, no app crash/ANR observed;
- committed source: `34d64aeb452be4b9d2e30c998e2b57888ff84c95`;
- GitHub Actions run `36324489315`: PASS.

## Release state

**RELEASE CANDIDATE.** Application behavior is frozen unless a release-blocking defect is found.

Remaining work is release preparation/signing, exact signed-APK smoke testing, tagging/publication and final evidence closeout.
