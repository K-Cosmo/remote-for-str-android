# Release gates

A release candidate may be marked final/ACCEPTED only when all applicable gates pass.

## Build/toolchain

- [x] real debug APK build has succeeded with AGP 9.4.1 / Gradle 9.7.1
- [x] exact working Gradle JVM captured: JetBrains Runtime 25.0.3; daemon uses Android Studio JBR
- [x] trusted Gradle Wrapper 9.7.1 generated and verified on the development workstation
- [x] rc2 clean `assembleDebug + lintDebug` succeeds
- [x] rc2 lint contains 0 errors
- [x] rc2 `assembleRelease` succeeds from the Wrapper
- [ ] rc3 `assembleDebug + lintDebug` succeeds after launcher/daemon packaging convergence
- [ ] rc3 `assembleRelease` succeeds
- [ ] rc3 daemon criteria resolve to a Java-25 daemon on the development workstation
- [ ] release signing configuration/process verified without committing private keys/secrets

## Real-device core path

- [x] real-device install succeeds
- [x] real STR discovery succeeds
- [x] endpoint resolution opens the STR UI
- [x] system-bar layout verified on real device
- [x] discovery layout/readability verified on real device
- [x] WebView full-content geometry verified on real device
- [ ] rc3 adaptive launcher icon visually verified
- [ ] rc3 themed/monochrome launcher icon visually verified where supported
- [ ] native back-navigation behavior explicitly verified
- [ ] reconnect after app restart explicitly verified
- [ ] permission denial/recovery behavior explicitly verified
- [ ] normal STR remote controls smoke-tested through the wrapper

## Product/security/documentation

- [x] no new application dependency added by release hardening
- [x] public README identifies upstream STR as a prerequisite and separates upstream responsibilities from wrapper responsibilities
- [x] public README discloses AI-assisted development and clarifies there is no runtime AI dependency
- [x] top-level WebView navigation is pinned to the selected speaker host/STR ports
- [x] intentional JavaScript/cleartext requirements are documented rather than hidden by a lint baseline
- [x] independent app branding is used; upstream STR logo is not copied as application identity
- [x] original AI-generated icon source is retained outside packaged Android resources
- [ ] security/privacy docs rechecked against the final release artifact/runtime
- [x] findings/status/changelog converged from the latest real evidence
- [ ] final release artifact (APK/AAB as applicable) installed/tested from the exact signed output intended for publication

Until all applicable final gates are satisfied, the status remains release candidate rather than ACCEPTED final release.
