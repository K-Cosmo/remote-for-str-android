# Release gates

A release candidate may be marked final/ACCEPTED only when all applicable gates pass.

## Build/toolchain

- [x] real debug APK build has succeeded with AGP 9.4.1 / Gradle 9.7.1
- [x] exact working Gradle JVM captured: JetBrains Runtime 25.0.3 on workstation
- [x] trusted Gradle Wrapper 9.7.1 generated and verified on the development workstation
- [x] rc3 `assembleDebug + lintDebug` succeeds
- [x] rc3 lint contains 0 errors
- [x] rc3 `assembleRelease` succeeds from the Wrapper
- [x] rc3 daemon criteria resolve to Java 25
- [x] GitHub Actions independently validates the Wrapper and builds debug/lint/release on Ubuntu with Temurin 25
- [x] release-signing identity/keystore created outside the repository
- [ ] exact final `0.1.0` signed APK built and signature verified

## Real-device core path

- [x] real-device install succeeds
- [x] real STR discovery succeeds
- [x] endpoint resolution opens the STR UI
- [x] system-bar layout verified on real device
- [x] discovery layout/readability verified on real device
- [x] WebView full-content geometry verified on real device
- [x] rc3 adaptive launcher icon visually verified
- [x] rc3 themed/monochrome launcher icon visually verified where supported
- [x] native back-navigation behavior explicitly verified
- [x] reconnect after app restart explicitly verified
- [x] permission denial/recovery behavior explicitly verified
- [x] normal STR remote controls smoke-tested through the wrapper
- [ ] exact final signed `0.1.0` publication APK installed and smoke-tested

## Product/security/documentation

- [x] no new application dependency added by release hardening
- [x] public README identifies upstream STR as a prerequisite and separates upstream responsibilities from wrapper responsibilities
- [x] public README discloses AI-assisted development and clarifies there is no runtime AI dependency
- [x] top-level WebView navigation is pinned to the selected speaker host/STR ports
- [x] intentional JavaScript/cleartext requirements are documented rather than hidden by a lint baseline
- [x] independent app branding is used; upstream STR logo is not copied as application identity
- [x] original AI-generated icon source is retained outside packaged Android resources
- [x] security/privacy docs rechecked against the final source/runtime behavior
- [ ] final release artifact hash/signature recorded with publication evidence

Until the exact signed publication artifact passes its install/smoke test, the status remains final release candidate rather than ACCEPTED final release.
