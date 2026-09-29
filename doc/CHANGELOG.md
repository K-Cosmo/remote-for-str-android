# Changelog

Only implemented changes belong here. Verification/acceptance state is tracked separately.

## 0.2.0 — 2026-09-29

Speaker management:

- persist explicitly saved speakers independently of the current mDNS scan;
- merge saved and discovered representations using discovery identity with host fallback for unkeyed/manual endpoints;
- show the current speaker and allow quick switching through the existing endpoint path;
- keep unreachable saved speakers visible and actionable;
- make room assignment the obvious inline row action; assigning a room implicitly saves reachable speakers and successful manual endpoints;
- add optional room assignment with Wohnzimmer, Schlafzimmer, Küche, Bad, Kinderzimmer and Garten presets;
- persist built-in rooms with language-neutral IDs and localize only their display labels;
- allow arbitrary custom room labels;
- add an explicit delete icon for saved speakers with confirmation; removal hides the row immediately until the next fresh device search;
- move speaker guidance into highlighted hint/state panels instead of tiny footer-only text;
- keep the connected-speaker status on the remote screen compact instead of rendering it as a highlighted banner, but make the selected device/room label larger and bold enough to be noticed;
- make speaker rows directly tappable even with inline action controls, and provide an explicit Remote toolbar action to leave the device view;
- present device name and room together as the primary row title, with model/version and IP below in smaller text;
- remove the extra leading bullet before the selected device title in the device list; the active device stays indicated by card highlighting alone;
- make the highlighted device-management hint dismissible through an explicit text link and remember the dismissal locally;
- preserve comfortable spacing between `Gefundene Geräte` and the first speaker card when the hint is hidden;
- keep Gradle 9.7.1 + AGP 9.4.1 for the 0.2.0 feature line;
- bump application version to `0.2.0` / versionCode 9.

## 0.1.1 — 2026-09-27

Discovery and network resilience:

- require an available Wi-Fi transport before saved-endpoint probing, mDNS discovery or manual local probing;
- track matching Wi-Fi networks with `ConnectivityManager.NetworkCallback` instead of deprecated `allNetworks` enumeration;
- do not use Internet validation as an STR eligibility criterion;
- replace indefinite no-Wi-Fi discovery with a stable same-Wi-Fi guidance state and Retry action;
- stop mDNS discovery after a 10-second discovery window;
- show STR prerequisite guidance and an upstream `st-reborn.de` link when no STR device is found;
- distinguish no-device discovery from a discovered speaker whose STR web endpoint is unreachable;
- distinguish per-speaker endpoint checking from a completed unreachable probe;
- allow retry after Wi-Fi becomes available without requiring an app restart;
- keep WebView recovery on the same Wi-Fi-aware connection path;
- bump application version to `0.1.1` / versionCode 8;
- release tooling now derives signed artifact names from `versionName` and refuses dirty-tree release builds.

## 0.1.0 — 2026-09-26

Initial public release after MVP convergence and release hardening:

- STR discovery via `_streborn._tcp` with legacy `_soundtouchstick._tcp` support.
- Endpoint fallback across STR remote ports 8888 and 17008.
- Speaker picker, manual host entry and last-speaker persistence.
- Native Android wrapper around the existing STR phone remote with full-width WebView layout.
- Android 13+ back handling plus legacy fallback for older supported Android versions.
- Local-network permission handling/recovery and representative real-device STR control smoke tests.
- Hardened top-level WebView navigation restricted to the selected speaker host and STR ports.
- Required STR JavaScript/cleartext LAN access documented explicitly; no JavaScript bridge, tracking, cloud account or runtime AI integration.
- Independent adaptive/themed STR Remote launcher artwork.
- Gradle 9.7.1 frozen for the first release; daemon runtime pinned to Java 25 while application Java source/target remains 17.
- Local lint converged to 0 errors and one informational Gradle-version warning.
- GitHub Actions independently validates the Wrapper and builds debug/lint/release on Ubuntu/JDK 25.
- GitHub Actions helper runtimes updated to current Node-24-based action releases before the public release.
- Final application version promoted from `0.1.0-rc3` / versionCode 6 to `0.1.0` / versionCode 7.

## 0.1.0-rc3

Release-packaging convergence after the green rc2 lint/build pass:

- Recorded rc2 evidence: clean debug/lint build successful with 0 lint errors and 5 warnings; release assembly successful.
- Rebuilt launcher packaging as a true adaptive icon with a solid background and transparent foreground motif instead of using the complete square artwork as the background layer.
- Added the monochrome layer directly to the adaptive icon definition and removed duplicate v33 icon XML.
- Removed the app-packaged full-square source artwork; the original AI-generated source remains under `/artwork`.
- Pinned Gradle daemon JVM criteria to Java 25, matching the verified development JVM major version.
- Updated CI runtime from JDK 17 to JDK 25; application Java source/target remains 17.
- Kept Gradle 9.7.1 frozen for the first public release.

## 0.1.0-rc2

Release-hardening changes driven by the first full lint/toolchain evidence pass:

- Captured the verified local toolchain: Gradle 9.7.1 on JetBrains Runtime 25.0.3 using the Android Studio JBR.
- Recorded successful Gradle Wrapper generation and rc1 `assembleRelease` evidence.
- Added a narrowly scoped `GestureBackNavigation` lint suppression to the legacy Android 12L-and-earlier `onBackPressed()` fallback; modern devices continue to use `OnBackInvokedDispatcher`.
- Removed the obsolete `SDK_INT >= 26` Safe Browsing check because minSdk is already 26.
- Kept required STR JavaScript and cleartext LAN access with explicit local lint documentation instead of a global lint baseline.
- Hardened top-level WebView navigation so only the selected speaker host on ports 8888/17008 stays inside the app.
- Kept `neverForLocation` and documented its intentional API-level behavior.
- Integrated the approved independent STR Remote app icon, removed the obsolete `mipmap-anydpi-v26` qualifier, and added a themed monochrome icon for API 33+.
- Retained Gradle 9.7.1 for the 0.1.0 release despite the availability of 9.8.0; the upgrade is deferred to post-release maintenance.

## 0.1.0-rc1

Release-candidate consolidation of the verified MVP:

- Promoted the visually converged 0.1.2-dev implementation to the first public release-candidate version line (`0.1.0-rc1`).
- Confirmed real-device system-bar inset handling from the second screenshot/evidence pass.
- Confirmed the simplified discovery screen and readable discovered-speaker row on the real device.
- Confirmed the embedded STR WebView uses the full available native content width and reaches the content boundary above the navigation inset.
- Confirmed the 0.1.2 debug build completes without the deprecated back-navigation compiler warnings seen in 0.1.1-dev.
- Expanded the public GitHub README with STR prerequisite/setup, architecture, compatibility evidence, security/privacy, upstream relationship and AI-assisted-development transparency.
- Added BUILD-0002 real-device evidence and BUILD-0003 release-preparation/spec records.
- Kept WebView hidden-API/Bluetooth log messages and the PackageManager alignment message as documented non-blocking observations rather than adding speculative permissions/workarounds.

## 0.1.2-dev — layout convergence

- Applied system-bar insets so the native title/actions no longer intentionally render underneath status/navigation bars.
- Simplified the discovery screen to one state header and removed duplicate mDNS explanatory text.
- Replaced the large discovery spinner with a compact inline progress indicator.
- Simplified speaker-row details to reduce line wrapping and visual fragmentation.
- Removed global native horizontal padding from the content container so the embedded STR WebView can use the full available width and height.
- Moved connected-endpoint status visibility to the WebView state instead of showing it during discovery.
- Added Android 13+ `OnBackInvokedDispatcher` handling and retained a legacy fallback for older Android versions.
- Removed duplicated hard-coded app-version suffixes from HTTP/WebView user-agent strings.
- Removed four stale unused string resources discovered during static convergence.
- Added real-device BUILD-0001 evidence and BUILD-0002 layout-convergence documentation.

## 0.1.1-dev — baseline convergence

- Updated stable Android Gradle Plugin baseline from 9.4.0 to 9.4.1.
- Updated project/CI Gradle baseline from 9.6.0 to 9.7.1.
- Documented Daemon JVM criteria migration; pinning is deferred until the real Gradle JVM version is captured.
- Removed deprecated `android.useAndroidX=false` project option.
- Fixed Kotlin visibility leak in the discovery callback without making internal speaker models public.
- Established `/doc` as the normative source of truth and added Spec Kit-compatible development governance.
- Added Clean Code and AI coding guardrails.
- CI runs debug assembly plus Android lint.

## 0.1.0 — initial draft

Initial MVP source: discovery, endpoint fallback, speaker picker, persistence, manual host entry and WebView wrapper. The first supplied draft was not accepted because real build evidence exposed a Kotlin visibility compiler error.
