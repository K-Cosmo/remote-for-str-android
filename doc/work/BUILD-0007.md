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
- Pixel functional verification: appropriate error states, Retry and STR website action behave as intended;
- exact signed 0.1.1 APK passed the final Pixel smoke test, including reconnect and regression checks;
- final release source commit: `d01b47dd348f0a52d7b81917c60c74c5229e17c4`;
- final source GitHub Actions run `36325864924`: PASS;
- signer certificate SHA-256: `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`;
- final APK SHA-256: `ecc8f8a78c0b65be5a74a44546275aa9d5688b54b2eacf577f3bbfd20b87a1e8`;
- annotated tag `v0.1.1` points to the release source commit;
- public GitHub Release `v0.1.1` publishes the accepted APK and SHA-256 file.

## Release state

**ACCEPTED in 0.1.1.**

BUILD-0007 is closed. No additional application change belongs to this build.
