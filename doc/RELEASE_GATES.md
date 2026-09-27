# Release gates

A release candidate may be marked final/ACCEPTED only when all applicable gates pass.

## 0.1.1 - RELEASE CANDIDATE

### Build/toolchain

- [x] local documentation consistency gate passes
- [x] clean `assembleDebug + lintDebug` succeeds
- [x] lint contains no blocking errors
- [x] `assembleRelease` succeeds
- [x] deprecated `ConnectivityManager.allNetworks` warning removed
- [x] GitHub Actions builds debug/lint/release from committed source `34d64aeb452be4b9d2e30c998e2b57888ff84c95` (run `36324489315`)
- [ ] GitHub Actions is green for the final release-preparation source commit

### Discovery/network resilience

- [x] no-Wi-Fi/error states render as stable user-facing guidance rather than an indefinite spinner
- [x] Retry works without an app restart
- [x] STR prerequisite/help path opens the upstream website
- [x] no-device and STR-unreachable states are implemented as distinct states
- [x] automatic local networking uses a Wi-Fi `NetworkCallback` without requiring Internet validation
- [x] no new Android permission or application dependency was added
- [x] normal app start/WebView path remains operational on the Pixel after the NetworkCallback cleanup
- [ ] exact signed final APK passes focused Pixel smoke: no-Wi-Fi -> Retry -> normal STR connection/control
- [ ] exact signed final APK confirms saved-endpoint reconnect after relaunch
- [ ] exact signed final APK confirms permission recovery and native back navigation remain functional

### Signing/publication

- [ ] release build starts from a clean committed working tree
- [ ] `apksigner verify --verbose --print-certs` succeeds
- [ ] post-sign `zipalign -c -P 16 -v 4` succeeds
- [ ] final `STR-Remote-0.1.1.apk` SHA-256 recorded
- [ ] exact signed `STR-Remote-0.1.1.apk` installed and smoke-tested
- [ ] annotated tag `v0.1.1` points to the source commit used for the signed artifact
- [ ] GitHub Release `v0.1.1` publishes that exact APK and matching SHA-256 file

`0.1.1` remains a release candidate until the signed-artifact and publication gates are complete.

---

## 0.1.0 - ACCEPTED

The complete accepted 0.1.0 evidence remains recorded in the release evidence/history. Its final publication APK SHA-256 is:

`5f48f99f604b7d861f305f4fda0b6383188ded6e45dc660d65d66ea68da8a2fe`

Tag `v0.1.0` points to source commit `4583e27deb734a5ac268af0c16939009f76c0d63`.
