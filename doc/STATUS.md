# Status

**Development version:** 0.1.1  
**Latest public release:** 0.1.0  
**Status:** IMPLEMENTED — BUILD-0007 real-device verification pending  
**Date:** 2026-09-27

## Accepted public release

`0.1.0` remains the latest accepted/public release. Its source, signed artifact, hash and real-device evidence remain unchanged.

## BUILD-0007 / 0.1.1 implementation

Discovery & Network Resilience is implemented for verification:

- local probing/discovery starts only when a Wi-Fi transport is present;
- Wi-Fi presence is tracked with `ConnectivityManager.NetworkCallback` rather than deprecated network enumeration;
- Wi-Fi presence is detected independently of Internet validation, so local-only Wi-Fi remains valid;
- missing Wi-Fi produces a stable explanatory state instead of an endless discovery spinner;
- discovery is finite and stops after 10 seconds;
- no-device timeout explains the STR prerequisite and provides retry plus the upstream STR website;
- a discovered speaker whose STR endpoint cannot be reached is reported separately from no-device discovery;
- retry can re-enter the saved-endpoint/discovery path without restarting the app;
- manual host probing and WebView recovery use the same Wi-Fi precondition;
- per-row endpoint state distinguishes "checking" from "STR web remote not reachable".

Application version is `0.1.1` / versionCode `8`.

## Required verification before acceptance

- clean debug build + lint;
- release assembly;
- real device: Wi-Fi off / mobile data on -> no scan and no spinner;
- real device: no network / airplane-mode equivalent -> same stable no-Wi-Fi state;
- Wi-Fi with no STR speaker -> finite timeout and STR guidance;
- Wi-Fi with the known STR speaker -> normal discovery/connection/control path;
- saved endpoint with Wi-Fi unavailable -> no pointless endpoint probe;
- discovered speaker with unreachable STR ports -> explicit STR-unreachable state;
- enable Wi-Fi and use Retry -> successful recovery without app restart;
- confirm Wi-Fi without Internet is not rejected solely for lacking Internet validation.
