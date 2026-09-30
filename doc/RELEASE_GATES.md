# Release gates

A release candidate may be marked final/ACCEPTED only when all applicable gates pass.

## 0.2.1 — IMPLEMENTED / VERIFICATION PENDING

### Implementation/scope

- [x] application version is `0.2.1` / versionCode 10
- [x] native remote-screen banner is compacted into a two-line text block with the comfortable Devices/Remote button size restored and slightly more vertical spacing, without changing the STR remote itself
- [x] Remote navigation with no Wi-Fi is intercepted before the Chromium error page and replaced by native same-Wi-Fi guidance
- [x] Remote no-Wi-Fi guidance exposes Retry and a direct Android Wi-Fi settings action
- [x] Devices no-Wi-Fi guidance exposes the same direct Android Wi-Fi settings action
- [x] recovery/device-selection paths probe the STR endpoint before starting a WebView load
- [x] WebView remains hidden behind native connection state until a successful main-page finish
- [x] saved-speaker last-known reachability persists locally and remains visually stable while background revalidation runs
- [x] device-card geometry/status remains stable during revalidation and only changes color after a changed result
- [x] saved reachable speakers receive stronger green fill plus a 2 dp green border
- [x] saved speakers that are currently unreachable from the app receive stronger red fill plus a 2 dp red border
- [x] saved-speaker state also displays explicit Online/Offline text
- [x] active checking/probe state remains neutral until reachability is known
- [x] device-card metadata hierarchy is model/status followed by STR version/IP with decreasing text size
- [x] existing current-speaker highlight remains available for unsaved/current rows
- [x] no discovery, persistence, permission, WebView security, playback or toolchain behavior is intentionally changed
- [x] Gradle 9.7.1 + AGP 9.4.1 remains unchanged

### Verification / release

- [ ] local documentation consistency gate passes
- [ ] clean `assembleDebug + lintDebug + assembleRelease` succeeds
- [ ] GitHub Android CI passes for committed 0.2.1 source
- [ ] Pixel confirms the remote header remains compact but no longer feels cramped, and STR controls remain usable
- [ ] Pixel confirms Remote with Wi-Fi disabled shows native Wi-Fi guidance rather than the Chromium connection-error page
- [ ] Pixel confirms the Wi-Fi settings button opens the Android Wi-Fi panel/settings from both Remote and Devices no-Wi-Fi states
- [ ] Pixel confirms Wi-Fi restore -> Retry waits for endpoint probe and either opens STR cleanly or stays on native unreachable guidance
- [ ] Pixel confirms selecting a saved device never briefly exposes Chromium's generic error page
- [ ] Pixel confirms opening Devices keeps the previous green/red state and card geometry stable until the background probe result changes
- [ ] Pixel confirms saved reachable speaker = green + Online
- [ ] Pixel confirms saved unreachable speaker = red + Offline after probe completion
- [ ] Pixel confirms checking/unknown state is neutral
- [ ] signed final APK verification and publication completed if 0.2.1 is released publicly

0.2.1 must remain **not ACCEPTED** until the applicable unchecked gates are completed.

---

## 0.2.0 — ACCEPTED

### Implementation/scope

- [x] saved speakers use app-private platform persistence without a new dependency
- [x] last-successful-endpoint startup path is preserved
- [x] saved/discovered rows use one de-duplicated device list
- [x] current speaker and quick switching use the existing endpoint/WebView path
- [x] unreachable saved speakers remain visible/actionable by design
- [x] optional room assignment includes six built-in presets plus custom text
- [x] built-in room storage is language-neutral and room metadata is excluded from connectivity/security identity
- [x] room assignment is an explicit inline action and implicitly saves the speaker
- [x] saved speakers expose a visible delete action with confirmation
- [x] common device-management actions do not rely on hidden long-press gestures
- [x] active-speaker device card, room/device hierarchy and Remote return path are implemented
- [x] usage guidance can be dismissed and the preference persists locally
- [x] no new permission, background service, cloud feature or playback/business logic is introduced
- [x] Gradle 9.7.1 + AGP 9.4.1 remains unchanged
- [x] application version is `0.2.0` / versionCode 9

### Local/final device evidence

- [x] local documentation consistency gate passes
- [x] clean `assembleDebug + lintDebug` succeeds
- [x] lint contains no blocking errors
- [x] `assembleRelease` succeeds
- [x] Pixel confirms room assignment and room visibility
- [x] Pixel confirms row tap connect/switch and explicit `Fernbedienung` return path
- [x] Pixel confirms delete confirmation/current-list removal behavior
- [x] Pixel confirms dismissible help text remains hidden after relaunch
- [x] Pixel confirms final device-list spacing and selected-device presentation
- [x] normal STR WebView/control path remains operational
- [x] exact signed final APK confirms custom-room persistence after relaunch
- [x] exact signed final APK confirms saved/offline speaker handling and recovery
- [x] exact signed final APK confirms manual-host connection can be assigned/saved
- [x] exact signed final APK confirms inherited Wi-Fi/permission/back-navigation regression checks

### Committed-source/signing/publication

- [x] GitHub Android CI passes for exact release source `b5ff103e318f4342ba3a748b786084108d4093af` (main run `36610855156`)
- [x] GitHub Android CI passes for pushed `v0.2.0` (run `36611841390`)
- [x] release build starts from clean committed source `b5ff103e318f4342ba3a748b786084108d4093af`
- [x] `apksigner verify --verbose --print-certs` succeeds
- [x] signing identity is `C=DE, CN=STR Remote`
- [x] signer certificate SHA-256 is `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`
- [x] APK Signature Scheme v2 and v3 verify successfully
- [x] post-sign `zipalign -c -P 16 -v 4` succeeds
- [x] final `STR-Remote-0.2.0.apk` SHA-256 is `5e7ba21f39752ae359a6de0103a440b363a1823223874778e44173f285243aa7`
- [x] exact signed `STR-Remote-0.2.0.apk` installs and passes the final smoke test
- [x] annotated tag `v0.2.0` resolves to exact signed-artifact source commit `b5ff103e318f4342ba3a748b786084108d4093af`
- [x] GitHub Release `v0.2.0` is published, non-draft and non-prerelease
- [x] GitHub Release `v0.2.0` publishes `STR-Remote-0.2.0.apk` and `STR-Remote-0.2.0.apk.sha256.txt`
- [x] GitHub public APK asset digest is `sha256:5e7ba21f39752ae359a6de0103a440b363a1823223874778e44173f285243aa7`, matching the accepted local artifact

The `0.2.0` release is **ACCEPTED**.

---

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
- [x] GitHub Release `v0.1.1` publishes the signed APK and SHA-256 file
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
- [x] intentional JavaScript/cleartext LAN requirements are documented explicitly
- [x] independent app branding is used
- [x] security/privacy docs match the accepted runtime behavior
- [x] final signed publication APK installed and smoke-tested
- [x] final APK SHA-256 recorded: `5f48f99f604b7d861f305f4fda0b6383188ded6e45dc660d65d66ea68da8a2fe`
- [x] annotated `v0.1.0` tag points to final source commit `4583e27deb734a5ac268af0c16939009f76c0d63`
- [x] GitHub Release `v0.1.0` publishes the signed APK and SHA-256 file

The `0.1.0` release is **ACCEPTED**.
