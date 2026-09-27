# Release gates

A release candidate may be marked final/ACCEPTED only when all applicable gates pass.

## 0.1.1 — BUILD-0007 verification

### Build/toolchain

- [ ] local documentation consistency gate passes
- [ ] clean `assembleDebug + lintDebug` succeeds
- [ ] lint contains no blocking errors
- [ ] `assembleRelease` succeeds
- [ ] GitHub Actions builds debug/lint/release from the committed 0.1.1 source

### Discovery/network resilience

- [ ] Wi-Fi unavailable: no endpoint probe/discovery starts and no indefinite spinner remains
- [ ] Wi-Fi unavailable: same-Wi-Fi guidance and Retry are visible
- [ ] local Wi-Fi without Internet is accepted; Internet validation is not required
- [ ] no STR device: discovery stops after the finite timeout and shows STR prerequisite/help
- [ ] discovered speaker with unreachable STR endpoint shows a distinct unreachable state
- [ ] Retry works after Wi-Fi becomes available without an app restart
- [ ] manual host path respects the same Wi-Fi precondition
- [ ] normal known-speaker discovery/connection/control path remains functional
- [ ] saved-endpoint reconnect remains functional when Wi-Fi is available

### Regression/security

- [ ] local-network permission denial/recovery remains functional
- [ ] WebView top-level navigation restrictions remain unchanged
- [ ] native back navigation remains functional
- [ ] no new application dependency or permission added
- [ ] German and English states render correctly

`0.1.1` remains IMPLEMENTED/verification-pending until all applicable gates pass.

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
