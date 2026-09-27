# Findings register

## F-001 — 0.1.0 Kotlin visibility failure

**Evidence:** real Android Studio/Gradle build log supplied 2026-09-26.  
**Observed:** `MainActivity.onSpeakerFound(candidate: SpeakerCandidate)` exposed an `internal` parameter type through a public override.  
**Action:** keep `SpeakerCandidate` internal; move `StrDiscovery.Callback` implementation into a private callback object and delegate to private handlers.  
**State:** VERIFIED by the successful 0.1.1-dev Android Studio build.

## F-002 — Gradle baseline mismatch

**Evidence:** project owner reported that the project only built after moving to Gradle 9.7.1; subsequent builds complete successfully.  
**Action:** baseline and CI use Gradle 9.7.1.  
**State:** VERIFIED for build compatibility. Exact local Gradle JVM is still not captured.

## F-003 — deprecated AndroidX project option

**Evidence:** 0.1.0 AGP warning reported that `android.useAndroidX=false` is deprecated and scheduled for removal.  
**Action:** remove the unnecessary property rather than suppress the warning.  
**State:** VERIFIED; later build logs no longer contain this warning.

## F-004 — native chrome overlaps the status bar on the real device

**Evidence:** first real-device screenshot pass showed the app title/action inside the status-bar area; second screenshot pass (`2026-09-26`, 0.1.2-dev) shows the native chrome fully below the status bar.  
**Cause:** the original native root did not consume/apply system-bar insets while modern Android renders edge-to-edge.  
**Action:** apply platform `WindowInsets` to the native root while keeping internal component spacing separate.  
**State:** VERIFIED FIXED on the real device.

## F-005 — discovery screen contains duplicate/fragmented state text

**Evidence:** first screenshot pass showed duplicate mDNS explanation and competing connection-state text; second 0.1.2-dev screenshot shows one `Gefundene Geräte` state header and one compact speaker row.  
**Action:** use one discovery state header, remove the redundant static mDNS hint, and keep connection status out of the discovery layout.  
**State:** VERIFIED FIXED on the real device.

## F-006 — embedded STR WebView does not use the full content width

**Evidence:** first WebView screenshot showed native horizontal gutters; second 0.1.2-dev screenshot shows the embedded STR page spanning the full native content width and ending immediately above the navigation-bar area.  
**Action:** move native spacing to toolbar/discovery components and leave the WebView match-parent inside the content rectangle.  
**State:** VERIFIED FIXED on the real device.

## F-007 — deprecated back-navigation API warning

**Evidence:** 0.1.1-dev build warned about deprecated `onBackPressed()` handling; the supplied 0.1.2-dev build output compiles Kotlin and completes successfully without those warnings.  
**Action:** Android 13+ uses `OnBackInvokedDispatcher`; the annotated legacy override remains only for older supported Android versions.  
**State:** BUILD WARNING VERIFIED FIXED. Functional back-navigation behavior remains an explicit release gate.

## F-008 — WebView emits hidden-API and Bluetooth permission messages

**Evidence:** 0.1.1 and 0.1.2 real-device Logcat.  
**Observed:** WebView 154 emits hidden-API denial messages and media-layer warnings for missing Bluetooth permission while the STR page still loads successfully.  
**Action:** do not add Bluetooth permission merely to silence WebView internals. Monitor; escalate only if a reproducible app function actually fails.  
**State:** OBSERVED / NON-BLOCKING.

## F-009 — PackageManager alignment-check message during install/start

**Evidence:** supplied real-device Logcat continues to report an error while checking package alignment before app start; installation and runtime nevertheless succeed.  
**Action:** retain as a release-quality observation. Inspect the exact signed release APK/alignment tooling if the message persists there or correlates with install/runtime failure.  
**State:** OBSERVED / NON-BLOCKING.

## F-010 — user-agent version drift in source

**Evidence:** static source review: EndpointProbe used `0.1.1-dev`, while WebView still appended `0.1.0`.  
**Action:** use the stable product token `STR-Remote-Android` without a duplicated hard-coded version string.  
**State:** VERIFIED FIXED in 0.1.2-dev.

## F-011 — user-visible MVP converged after second real-device pass

**Evidence:** 0.1.2-dev real-device discovery and connected-WebView screenshots plus successful build and focused Logcat supplied 2026-09-26.  
**Observed:** the previously identified visual release blockers are no longer present; discovery and connected states are coherent and the core STR remote loads correctly.  
**Action:** feature-freeze the MVP and move to release hardening rather than add speculative functionality.  
**State:** VERIFIED; drives 0.1.0-rc1.

## F-012 — Release-hardening toolchain evidence captured

**Evidence:** real developer-workstation Gradle Wrapper session supplied 2026-09-26.  
**Observed:** Gradle 9.7.1 runs with JetBrains Runtime 25.0.3; the daemon uses the Android Studio JBR. The 9.7.1 Wrapper was generated successfully and `:app:assembleRelease` completed successfully.  
**Action:** keep Gradle 9.7.1 frozen for 0.1.0 and use the Wrapper for all remaining release-gate commands.  
**State:** VERIFIED for rc1 toolchain/release compilation.

## F-013 — Lint blocks rc1 on legacy back fallback

**Evidence:** `lintDebug` report supplied 2026-09-26.  
**Observed:** one `GestureBackNavigation` error points at the Android 12L-and-earlier `onBackPressed()` fallback even though Android 13+ is already handled through `OnBackInvokedDispatcher`. Lint explicitly notes that per-activity migration can require a focused suppression.  
**Action:** add a local `GestureBackNavigation` suppression to the documented legacy fallback only. Do not add AndroidX solely to silence this detector and do not create a global lint baseline.  
**State:** IMPLEMENTED in rc2; requires lint re-run.

## F-014 — Intentional WebView JavaScript and cleartext LAN warnings

**Evidence:** rc1 lint report.  
**Observed:** lint warns about JavaScript and cleartext HTTP. Both are required by the upstream STR phone remote, which is served over HTTP on a dynamic LAN address.  
**Action:** retain JavaScript and cleartext only with explicit local suppressions/documentation, no native JavaScript bridge, file/content access disabled, and top-level WebView navigation pinned to the selected speaker host on ports 8888/17008.  
**State:** IMPLEMENTED in rc2; requires lint/runtime re-verification.

## F-015 — Launcher resources need release-quality adaptive/themed icon

**Evidence:** rc1 lint report and approved icon review.  
**Observed:** the previous `mipmap-anydpi-v26` folder is redundant at minSdk 26 and adaptive icons lacked a monochrome themed layer.  
**Action:** use the approved independent STR Remote artwork as the adaptive icon, move the baseline adaptive resource to `mipmap-anydpi`, add a v33 monochrome layer, and retain the AI-generated source artwork under `/artwork`.  
**State:** IMPLEMENTED in rc2; requires device/launcher visual verification.

## F-016 — RC2 release-hardening build is green

**Evidence:** real development-workstation wrapper run supplied 2026-09-26.  
**Observed:** `clean :app:assembleDebug :app:lintDebug` completes successfully in 9 seconds; `:app:assembleRelease` completes successfully in 4 seconds; lint reports 0 errors and 5 warnings.  
**Action:** treat rc2 functional/build blockers as closed and address only the remaining packaging warnings that are actionable before release.  
**State:** VERIFIED.

## F-017 — RC2 adaptive icon still packages the full square artwork as a layer

**Evidence:** rc2 lint reports `IconLauncherShape`, plus the base adaptive-icon files still trigger monochrome warnings and the launcher background resource is unused.  
**Action:** extract the remote/speaker motif onto transparency, use a solid background color, provide a monochrome motif in the same adaptive icon declaration, remove duplicate v33 icon XML, and retain the original generated artwork only under `/artwork`.  
**State:** IMPLEMENTED in rc3; requires lint/device visual verification.

## F-018 — Daemon criteria do not bootstrap the Gradle wrapper

**Evidence:** the wrapper failed in a fresh PowerShell session until `JAVA_HOME`/`PATH` exposed Android Studio JBR; once Java was available, Gradle 9.7.1 reported Launcher JVM 25.0.3 and daemon JBR 25.0.3.  
**Action:** pin daemon criterion to Java 25 for consistency, but document that CLI wrapper startup still requires a Java executable available through `JAVA_HOME` or `PATH`.  
**State:** IMPLEMENTED/documented in rc3; daemon selection requires local re-verification.

## F-019 — RC3 packaging and interaction gates converged

**Evidence:** final rc3 local lint/build output and real-device verification supplied 2026-09-26.  
**Observed:** lint reports 0 errors / 1 informational Gradle-version warning; debug/release assembly succeeds; adaptive/themed icon, native back navigation, reconnect, permission denial/recovery and representative STR controls are verified.  
**Action:** promote the verified implementation to final `0.1.0` source without behavior changes.  
**State:** VERIFIED.

## F-020 — Public GitHub CI independently reproduces the build

**Evidence:** public GitHub Actions runs supplied 2026-09-26.  
**Observed:** Ubuntu 24.04 + Temurin 25 validates the Gradle Wrapper, runs Gradle 9.7.1 with Java-25 daemon criteria, completes debug/lint/release successfully and uploads the debug APK artifact.  
**Action:** retain CI as an independent release gate.  
**State:** VERIFIED.

## F-021 — GitHub Actions helper runtime deprecations removed

**Evidence:** first CI run emitted setup-java v4 / Node-20 deprecation warnings; follow-up run after action updates uses current Node-24-based checkout/setup-java/setup-gradle/upload-artifact releases and remains green.  
**Action:** keep helper-action maintenance separate from application dependencies.  
**State:** VERIFIED.

## F-022 — Discovery spins indefinitely without Wi-Fi and has no no-device terminal state

**Evidence:** real-world observation after the public 0.1.0 release, reported 2026-09-27.  
**Observed:** when the phone has no Wi-Fi connection, STR Remote still starts the discovery path and leaves the indeterminate spinner running. A normal discovery with no matching STR device also has no finite user-facing terminal state.  
**Action:** BUILD-0007 gates saved-endpoint probing and mDNS discovery on Wi-Fi transport, adds a finite discovery timeout, separates no-device from STR-endpoint-unreachable states, and provides Retry/STR help. Internet validation is explicitly not required.  
**State:** IMPLEMENTED in 0.1.1; real-device verification pending.

## F-023 — BUILD-0007 initial Wi-Fi query used deprecated network enumeration

**Evidence:** Android Studio / Gradle build supplied 2026-09-27 reports a Kotlin deprecation warning for `ConnectivityManager.allNetworks` in `MainActivity.hasWifiTransport()`.  
**Observed:** BUILD-0007 functionally passes the first Pixel smoke tests, but the initial implementation polls all networks synchronously.  
**Action:** replace polling with a regular Wi-Fi `NetworkCallback`, keep the current matching Wi-Fi networks in app state, unregister the callback on destroy, and retain a short initialization fallback for the no-Wi-Fi case. Do not require Internet validation.  
**State:** IMPLEMENTED; clean build/lint and Pixel regression verification pending.
