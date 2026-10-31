# Findings register

## F-001 — 0.1.0 Kotlin visibility failure

**Evidence:** real Android Studio/Gradle build log supplied 2026-09-26.  
**Observed:** `MainActivity.onSpeakerFound(candidate: SpeakerCandidate)` exposed an `internal` parameter type through a public override.  
**Action:** keep `SpeakerCandidate` internal; move `StrDiscovery.Callback` implementation into a private callback object and delegate to private handlers.  
**State:** VERIFIED by the successful 0.1.1-dev Android Studio build.

## F-002 — Gradle baseline mismatch

**Evidence:** project owner reported that the project only built after moving to Gradle 9.7.1; subsequent builds complete successfully.  
**Action:** baseline and CI use Gradle 9.7.1.  
**State:** VERIFIED. Gradle 9.7.1 is the accepted baseline; local JBR 25.0.3 and Java-25 daemon criteria were later captured and accepted.

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
**State:** VERIFIED FIXED. Functional native back navigation was verified on real devices and remains regression coverage.

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
**State:** VERIFIED. Subsequent lint/build gates passed; the focused legacy fallback suppression remains the accepted implementation.

## F-014 — Intentional WebView JavaScript and cleartext LAN warnings

**Evidence:** rc1 lint report.  
**Observed:** lint warns about JavaScript and cleartext HTTP. Both are required by the upstream STR phone remote, which is served over HTTP on a dynamic LAN address.  
**Action:** retain JavaScript and cleartext only with explicit local suppressions/documentation, no native JavaScript bridge, file/content access disabled, and top-level WebView navigation pinned to the selected speaker host on ports 8888/17008.  
**State:** VERIFIED. Subsequent lint/runtime/release gates passed with the documented JavaScript/cleartext constraints.

## F-015 — Launcher resources need release-quality adaptive/themed icon

**Evidence:** rc1 lint report and approved icon review.  
**Observed:** the previous `mipmap-anydpi-v26` folder is redundant at minSdk 26 and adaptive icons lacked a monochrome themed layer.  
**Action:** use the approved independent STR Remote artwork as the adaptive icon, move the baseline adaptive resource to `mipmap-anydpi`, add a v33 monochrome layer, and retain the AI-generated source artwork under `/artwork`.  
**State:** VERIFIED. Adaptive/themed launcher presentation was subsequently verified on the real device.

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
**State:** VERIFIED. Gradle 9.7.1 runs with Java-25 daemon criteria locally and in CI.

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
**State:** VERIFIED / ACCEPTED in 0.1.1; Wi-Fi/no-device/unreachable/Retry behavior remains regression coverage.

## F-023 — BUILD-0007 initial Wi-Fi query used deprecated network enumeration

**Evidence:** Android Studio / Gradle build supplied 2026-09-27 reports a Kotlin deprecation warning for `ConnectivityManager.allNetworks` in `MainActivity.hasWifiTransport()`.  
**Observed:** BUILD-0007 functionally passes the first Pixel smoke tests, but the initial implementation polls all networks synchronously.  
**Action:** replace polling with a regular Wi-Fi `NetworkCallback`, keep the current matching Wi-Fi networks in app state, unregister the callback on destroy, and retain a short initialization fallback for the no-Wi-Fi case. Do not require Internet validation.  
**State:** VERIFIED / ACCEPTED in 0.1.1; the NetworkCallback implementation passed clean build/lint and Pixel regression verification.
## F-024 — BUILD-0009 speaker-management UX required explicit actions

**Evidence:** 0.2.0 BUILD-0009 Pixel screenshot/build iteration on 2026-09-29.
**Observed:** inline Room/Delete controls initially interfered with row selection, room state was not prominent, deletion behavior was visually confusing and hidden long-press guidance was unsuitable for a non-technical user experience.
**Action:** make the row body explicitly tappable, keep Room/Delete as visible inline actions, show device · room prominently, provide a direct Fernbedienung return action, make guidance dismissible and preserve balanced spacing.
**State:** VERIFIED / ACCEPTED in 0.2.0.

## F-025 — 0.2.0 WebView/log noise remains non-blocking

**Evidence:** focused 0.2.0 Pixel Logcat supplied 2026-09-29.
**Observed:** Chromium/WebView still emits hidden-API and Bluetooth-permission messages; the STR page also logged transient Failed to fetch messages for selected upstream API paths while the wrapper/main remote remained operational.
**Action:** retain the established no-speculative-permission policy. Treat these as upstream/WebView observations unless they correlate with a reproducible STR Remote failure.
**State:** OBSERVED / NON-BLOCKING.

## F-026 — Post-0.2.0 Markdown drift exposed duplicate/current-state documentation

**Evidence:** repository-wide Markdown audit after the accepted 0.2.0 release.
**Observed:** root duplicate changelog/privacy/contribution content and several /doc authorities still described 0.1.0-era build, blocker or persistence state; DECISIONS.md also contained two different D-019 headings.
**Action:** remove the duplicate root changelog, convert root privacy/contribution files to pointers, converge current normative /doc files on the accepted 0.2.0 baseline, normalize the duplicate decision ID and explicitly classify historical records as snapshots.
**State:** VERIFIED by documentation consistency, whitespace and duplicate-ID checks in the convergence change.

## F-027 — BUILD-0013 feedback mail subject/body missing in selected client

**Evidence:** physical Android test report from 2026-10-30: email application chooser appeared, selected mail client opened, recipient was present, but subject and body were blank.
**Observed:** the draft formatter generated the subject and body correctly but `ACTION_SENDTO` relied solely on `Intent.EXTRA_SUBJECT` and `Intent.EXTRA_TEXT`, which did not work with the tested client.
**Action:** retain `ACTION_SENDTO` and existing extras, additionally encode `subject` and `body` in an RFC 6068 `mailto:` URI using UTF-8 percent encoding and CRLF; add unit regression coverage and explicit on-device client QA. No automatic send, SMTP, credential or device identifier collection.
**State (2026-10-31):** CLOSED in signed 0.3.1 on-device acceptance; recipient, subject and body were confirmed. The no-email-client negative case remains [NOT TESTED / deferred](https://github.com/K-Cosmo/remote-for-str-android/issues/7).

## F-028 — BUILD-0013 WebView CSS relies on private upstream STR DOM

**Evidence:** upstream STR maintainer review of draft PR #5 on 2026-10-30.
**Observed:** `body > header`, `#dev` and `#powerBtn` are internal markup selectors and not a stable extension API. The visible 10px spacing was approved on device but future STR markup can break injection.
**Action:** discuss the desired safe-area adjustment with STR maintainer and prefer an explicit supported hook; retain compatibility regression testing until resolved. Track in STR-022.
**State (2026-10-31):** OPEN upstream compatibility risk in *published 0.3.1*, not a defect reproduced in the tested setup. See [STR-022](PLANNED.md#str-022--stable-str-webui-embedding-interface-proposal).

## F-029 — 0.3.0 accepted release status contradicted by legacy tail line

**Evidence:** `doc/STATUS.md` header correctly identified 0.3.0 RELEASED/ACCEPTED while its final sentence still claimed 0.2.1 was the current accepted release.
**Action:** correct the stale sentence on the ongoing feature branch without rewriting signed release evidence or historical release tags.
**State (2026-10-31):** CLOSED by signed 0.3.1 publication documentation and CI.

## F-030 — Early hardware QA address remains in historical Git references

**State (2026-10-31):** PARTIALLY RESOLVED: ordinary Git branches and five pre-existing tags rewritten, six stale branches retired; residual GitHub-managed old PR refs/caches remain open. See [provenance](GIT_HISTORY_PROVENANCE.md).

**Evidence:** Current branch redacts one real RFC1918 host address from two dated QA Markdown files using RFC 5737 documentation addresses. The original blob remains reachable through published historical refs, including `main` and release tags. There is no observed private key, token or password in the current tracked text audit. The old host address is not itself an Internet-routable secret, but it is unnecessary public operational metadata.

**Resolution:** Controlled, backed-up history rewrite across all relevant refs plus obsolete branch cleanup; review GitHub PR caches and external clones. Record exact impact on signed release source/tag identities before execution. This original observation preceded the executed ordinary-ref history rewrite; GitHub-managed old PR refs remain outside that rewrite.

## F-031 — Normative documentation version and process drift

**State (2026-10-31):** PRIOR DRIFT CLOSED after main/PR CI; new F-032 records post-0.3.1 drift.

**Evidence:** `doc/README.md` still described 0.2.0 as the public release; `doc/BLOCKERS.md` still described the accepted 0.2.0 baseline; governance text incorrectly said the document consistency script runs only locally although GitHub CI now invokes it. Current public signed release is 0.3.0, while 0.3.1 remains the unreleased candidate. Historical work/evidence records are intentionally not rewritten solely for outdated wording.

**Resolution:** Update current-state authorities, retain historical evidence and ensure `STATUS.md` is the canonical release-state source.

## F-032 — Post-0.3.1 documentation drift and misplaced public policy

**Evidence (2026-10-31):** Following signed 0.3.1 publication, root README had 124 lines with implementation, release-provenance and normative security detail that belongs under `doc/`. Current normative `SECURITY.md`, `PRIVACY.md` and `ARCHITECTURE.md` called released features planned; `BLOCKERS.md` treated finished PR #5 release gates as open; `SECURITY.md` still used an obsolete repository path. Upstream CSS uses private header markup, so no supported embedding API can be claimed.

**Action:** Shorten root README to public essentials, correct current canonical documents, retain dated QA/work notes and decision chronology, document upstream CSS hook only as a STR-022 proposal, and broaden current-doc link/ID consistency checks. See [dated review matrix](evidence/qa/2026-10-31-documentation-review.md).

**State:** REVIEW IMPLEMENTED on documentation-only branch; final CI verification required before acceptance.
