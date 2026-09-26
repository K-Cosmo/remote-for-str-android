# BUILD-0002 real-device evidence — 2026-09-26

**Source build:** 0.1.2-dev  
**Evidence class:** real Android Studio build + real-device screenshots + focused Logcat  
**Acceptance effect:** verifies the visual convergence and moves the MVP to release-candidate hardening

## Build evidence

Supplied build output records:

- `:app:compileDebugKotlin` completed without the deprecated back-navigation warnings seen in 0.1.1-dev;
- `:app:assembleDebug` completed;
- `BUILD SUCCESSFUL in 2s`;
- 34 actionable tasks, 17 executed and 17 up-to-date;
- build output still suggests optional Gradle configuration-cache optimization; no configuration-cache change is justified by this functional release pass.

No `lintDebug` result and no `gradle -version` output were included, so those remain open release gates.

## Discovery-screen evidence

The supplied 0.1.2-dev screenshot shows:

- `STR Remote` and the `GERÄTE` action fully below the Android status bar;
- one concise discovery heading (`Gefundene Geräte`);
- one readable speaker row for `ST09`;
- model `SoundTouch 10`;
- local address `REDACTED_LOCAL_IP`;
- STR `v0.9.86`;
- no duplicate mDNS explanation or competing connection-status text;
- manual-address fallback remains available at the bottom.

This verifies the fixes for F-004 and F-005 on the tested device.

## Connected/WebView evidence

The supplied connected-state screenshot shows:

- the active endpoint line `ST09 · REDACTED_LOCAL_IP:8888` below the native toolbar;
- the upstream STR UI loaded successfully;
- the WebView spans the full available native content width with no wrapper gutter;
- the upstream STR bottom navigation remains intact;
- the WebView ends above the Android navigation-bar inset rather than rendering underneath it.

This verifies the fix for F-006 on the tested device.

## Logcat observations

The supplied 0.1.2-dev Logcat records:

- application process starts with `target_sdk_version=37`;
- Android WebView 154.0.8037.57 loads successfully;
- the app's `OnBackInvokedDispatcher` callback is registered;
- the previously observed WebView hidden-API messages persist;
- Chromium still reports missing Bluetooth permission from its media layer;
- PackageManager still emits the alignment-check message before app start;
- none of those observations prevents the STR UI from loading and operating to the level evidenced by the screenshots.

The log does **not** by itself verify user-triggered back navigation, permission denial/recovery, restart reconnect or individual STR control actions. Those remain explicit release gates rather than being inferred.

## Result

The visual defects found in BUILD-0001 are verified fixed. The MVP is sufficiently converged for feature freeze and `0.1.0-rc1` release preparation. Final `0.1.0` acceptance still requires the remaining gates in `doc/RELEASE_GATES.md`.
