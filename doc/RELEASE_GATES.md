# Release gates

A release candidate may be marked final/ACCEPTED only when all applicable gates pass.

## 0.1.1 - ACCEPTED

### Build/toolchain

- [x] local documentation consistency gate passes
- [x] clean `assembleDebug + lintDebug` succeeds
- [x] lint contains no blocking errors
- [x] `assembleRelease` succeeds
- [x] deprecated `ConnectivityManager.allNetworks` warning removed
- [x] GitHub Actions builds debug/lint/release from committed feature source `34d64aeb452be4b9d2e30c998e2b57888ff84c95` (run `36324489315`)
- [x] GitHub Actions is green for the final release source commit `d01b47dd348f0a52d7b81917c60c74c5229e17c4` (run `36325864924`)

### Discovery/network resilience

- [x] no-Wi-Fi/error states render as stable user-facing guidance rather than an indefinite spinner
- [x] Retry works without an app restart
- [x] STR prerequisite/help path opens the upstream website
- [x] no-device and STR-unreachable states are implemented as distinct states
- [x] automatic local networking uses a Wi-Fi `NetworkCallback` without requiring Internet validation
- [x] no new Android permission or application dependency was added
- [x] normal app start/WebView path remains operational on the Pixel after the NetworkCallback cleanup
- [x] exact signed final APK passes focused Pixel smoke: no-Wi-Fi -> Retry -> normal STR connection/control
- [x] exact signed final APK confirms saved-endpoint reconnect after relaunch
- [x] exact signed final APK confirms permission recovery and native back navigation remain functional

### Signing/publication

- [x] release build starts from clean committed source `d01b47dd348f0a52d7b81917c60c74c5229e17c4`
- [x] `apksigner verify --verbose --print-certs` succeeds
- [x] signing identity remains `C=DE, CN=STR Remote`
- [x] signer certificate SHA-256 is `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`
- [x] APK Signature Scheme v2 and v3 verify successfully
- [x] post-sign `zipalign -c -P 16 -v 4` succeeds
- [x] final `STR-Remote-0.1.1.apk` SHA-256 recorded: `ecc8f8a78c0b65be5a74a44546275aa9d5688b54b2eacf577f3bbfd20b87a1e8`
- [x] exact signed `STR-Remote-0.1.1.apk` installed and smoke-tested
- [x] annotated tag `v0.1.1` points to source commit `d01b47dd348f0a52d7b81917c60c74c5229e17c4`
- [x] GitHub Release `v0.1.1` publishes that exact APK and matching SHA-256 file
- [x] GitHub public APK asset digest equals `sha256:ecc8f8a78c0b65be5a74a44546275aa9d5688b54b2eacf577f3bbfd20b87a1e8`

The `0.1.1` release is **ACCEPTED**.

---

## 0.1.0 — ACCEPTED

### Build/toolchain

- [x] real debug APK build succeeded with AGP 9.4.1 / Gradle 9.7.1
- [x] working Gradle/JVM baseline captured
- [x] trusted Gradle Wrapper 9.7.1 generated and independently validated
- [x] final `0.1.0` clean debug + lint + release assembly succeeded
- [x] lint contains no blocking errors
- [x] daemon criteria resolve to Java 25
- [x] GitHub Actions independently validates the Wrapper and builds debug/lint/release on Ubuntu with Temurin 25
- [x] release-signing identity/keystore remains outside the repository
- [x] exact final `0.1.0` APK built and cryptographic signature verified

### Real-device core path

- [x] exact final signed `0.1.0` APK installs and starts
- [x] Android App Info reports `0.1.0`
- [x] real STR discovery succeeds
- [x] endpoint resolution opens the STR UI
- [x] system-bar layout and full-width WebView geometry verified
- [x] adaptive/themed launcher icon verified
- [x] native back navigation verified
- [x] reconnect after app restart verified
- [x] permission denial/recovery verified
- [x] normal STR controls smoke-tested through the wrapper

### Product/security/documentation/publication

- [x] no unnecessary application dependency added by release hardening
- [x] README identifies upstream STR as a prerequisite and separates upstream from wrapper responsibilities
- [x] public AI transparency distinguishes development assistance from runtime behavior
- [x] top-level WebView navigation is pinned to the selected speaker host/STR ports
- [x] intentional JavaScript/cleartext LAN requirements are documented
- [x] independent app branding is used
- [x] security/privacy docs match the accepted runtime behavior
- [x] final signed publication APK installed and smoke-tested
- [x] final APK SHA-256 recorded: `5f48f99f604b7d861f305f4fda0b6383188ded6e45dc660d65d66ea68da8a2fe`
- [x] annotated `v0.1.0` tag points to final source commit `4583e27deb734a5ac268af0c16939009f76c0d63`
- [x] GitHub Release `v0.1.0` publishes the signed APK and SHA-256 file

The `0.1.0` release is **ACCEPTED**.
