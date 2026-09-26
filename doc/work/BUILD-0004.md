# BUILD-0004 — rc2 lint/security/icon convergence

## Goal

Converge the first public release candidate against real developer-workstation lint/toolchain evidence without adding features, dependencies or a new architecture.

## Inputs

- verified Gradle 9.7.1 Wrapper/JBR 25.0.3 evidence;
- rc1 lint report: 1 error, 8 warnings;
- approved independent STR Remote app icon artwork.

## Implemented scope

- local `GestureBackNavigation` suppression on the legacy pre-Android-13 fallback;
- local `SetJavaScriptEnabled`, cleartext and `neverForLocation` lint documentation/suppression;
- remove redundant minSdk 26 runtime API check and obsolete `mipmap-anydpi-v26` resource qualifier;
- pin top-level WebView navigation to the selected speaker host on ports 8888/17008;
- integrate adaptive/themed launcher icon and preserve source artwork under `/artwork`;
- version `0.1.0-rc2` / versionCode 5;
- converge `/doc` and release gates.

## Explicit non-scope

- no Gradle 9.8.0 upgrade;
- no new AndroidX/app dependency;
- no lint baseline;
- no playback/discovery/API redesign;
- no signing changes yet.

## Required evidence after implementation

1. Wrapper: `clean :app:assembleDebug :app:lintDebug --stacktrace`.
2. Wrapper: `:app:assembleRelease --stacktrace`.
3. review remaining lint warnings.
4. device check of launcher icon and core app path.
5. back-navigation, reconnect, permission-recovery and control smoke tests.
