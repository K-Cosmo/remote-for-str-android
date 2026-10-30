# BUILD-0001 real-device evidence — 2026-09-26

**Source build:** 0.1.1-dev  
**Evidence class:** real Android Studio build + real-device runtime screenshots/Logcat  
**Acceptance effect:** partial verification only; findings generated for BUILD-0002
**Redaction:** The address `192.0.2.29` is an RFC 5737 TEST-NET-1 documentation placeholder; the originally captured private LAN address was removed in the current revision. Historical Git commits may still contain the original value.  

## Build evidence

Supplied build output records:

- `:app:compileDebugKotlin` completed;
- `:app:assembleDebug` completed;
- `BUILD SUCCESSFUL in 6s`;
- AGP 9.4.1 artifacts were used;
- remaining compiler warnings are both tied to deprecated `onBackPressed` handling.

No `lintDebug` result and no `gradle -version` output were included.

## Runtime evidence

The supplied screenshots show:

- ST09 discovered successfully;
- model `SoundTouch 10`;
- address `192.0.2.29`;
- STR `v0.9.86`;
- endpoint ready on port 8888;
- STR web remote loaded inside the app.

The same screenshots show:

- title/action overlap with the system status bar;
- duplicate discovery/mDNS text and visually competing state messages;
- fixed native horizontal padding around the WebView.

## Logcat observations

- app runs with `target_sdk_version=37`;
- Android WebView 154.0.8037.57 is loaded;
- WebView logs blocked hidden-API attempts but continues loading successfully;
- Chromium media code logs missing Bluetooth permission; no STR Remote Bluetooth feature is evidenced;
- PackageManager emits an alignment-check error before the app starts, but install/start and runtime proceed successfully.

## Result

The evidence is sufficient to reject additional speculative architecture work and define a narrow BUILD-0002 layout/navigation convergence. It is not sufficient to mark the baseline ACCEPTED.
