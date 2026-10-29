# BUILD-0012 — SoundTouch 20 Series III SoftAP Phase A observations

**Date:** 2026-10-29
**State:** PARTIAL PHASE A — API and network recovery confirmed, **intermittent retry regression reopened**, diagnostic revision pending real-device QA
**Reference hardware:** Bose SoundTouch 20 Series III / 355589-SM2 / original Bose firmware (per test owner)
**App:** STR Remote PoC, separate debug package `app.strremote.android.poc`

## User-observed results

The user confirms that a fresh Android Bose-setup connection flow works **again after removing the 400 ms deferred request** when the speaker is already in setup mode. The debug app issues read-only `GET` requests scoped to the Android-selected `Network`, and displays only HTTP status/content-type or network exception class. The user subsequently confirmed that **in-place retry works** when the speaker enters setup mode later.

| Target | User-observed result | Interpretation |
|---|---|---|
| `http://192.0.2.1:80/` | `HTTP 200; text/html` | Bose setup web UI responds |
| `http://192.0.2.1:8090/` | `HTTP 404; text/html` | Port 8090 HTTP responds; root path is not an exposed API endpoint |
| `http://192.0.2.1:8090/info` | `HTTP 200; text/xml` | Confirmed read-only Bose `/info` API endpoint on this original-firmware ST20 III |
| `http://192.168.1.1:80/` | `SocketTimeoutException` | Alternate candidate did not respond on tested device |
| `http://192.168.1.1:8090/` | `SocketTimeoutException` | Alternate candidate did not respond on tested device |

No Bose Wi-Fi profile was written; no password, SSID, MAC, serial or XML body was collected in this record.

## Observed retry defect

Test sequence:

1. User starts the Android Bose setup-network selection **before** the SoundTouch has reached setup/SoftAP mode.
2. Speaker enters setup mode later.
3. Existing **Erneut versuchen** action does not discover the now-available Bose SoftAP reliably.
4. Ending and **restarting the entire onboarding flow** does find and connect to the speaker.

Root cause is **not yet independently proven**. The current `MainActivity` Retry operates STR/mDNS discovery, which is distinct from original Bose SoftAP setup. The original PoC activity also did not expose a usable retry while its first `WifiNetworkSpecifier` request was waiting: its start button was disabled until callback/timeout. A stale Android system Wi-Fi picker/request may need a fresh network request rather than retrying STR discovery or the same picker session.

## Follow-up fix in test branch

- Dedicated in-place `Erneut versuchen` in the Bose PoC remains tappable during an outstanding request.
- The first retry implementation added a 400 ms delay to every `requestNetwork()` call. **Regression observed by tester:** after installing that version, the speaker was no longer detected. The actual root cause is not yet established.
- **Correction validated for initial connection:** after reverting the 400 ms delay, a fresh connection to an already-ready Bose setup AP again succeeded, including an HTTP 200 response from `/info`. The in-place retry is now independently confirmed by the tester. The PoC retains a manually triggered retry that unregisters the previous callback, discards its results, then performs a fresh immediate request. No periodic background retries.
- User guidance explains that an already-open Android system picker should be dismissed before triggering a new request.
- Expanded read-only HTTP probes now include `GET http://192.0.2.1:8090/info`. SoundTouch API documentation identifies `/info` as the read-only device information endpoint; this PoC does **not** read or persist the XML response body, only status/content-type.
- The public `v0.2.1` APK/source remain unchanged.

## Follow-up usability observations (2026-10-29)

- **Retry accepted by tester:** the Bose SoftAP is detected by pressing `Erneut versuchen` in the same setup screen after the speaker becomes ready.
- **Cancel releases the temporary Wi-Fi connection:** Android reconnects to the previous home Wi-Fi, observed by tester.
- **Cancel navigation accepted:** the corrected button calls `closeSession()` then `finish()`; the tester confirms it returns to the Devices page.
- **Visual usability:** revised screenshot supplied by tester: clearer step grouping and content below the system bar are accepted as an improvement. Further simplification requested for the poorly discoverable optional Bose-SSID field.

## Network lifecycle observation and SSID UX clarification (2026-10-29)

- After read-only Phase A completes its HTTP requests, `probeReadOnly()` deliberately calls `closeSession()`, which unregisters the Android `NetworkCallback`. The tester observed that Android immediately returns to the normal/home Wi-Fi **without pressing Cancel**. This is the expected and desired behavior for an independent, read-only connectivity probe.
- **Cancel** independently releases a running setup-network request and now also closes the Activity, returning to Devices; the tester confirmed the navigation.
- The displayed `Bose-Setup-SSID (optional)` field was an `EditText` used to override the Android request's default `Bose` SSID prefix. It was **not** intended for the future home-Wi-Fi SSID or password. The field looked like ordinary text and confused the tester. The next debug-only UX revision removes it from the normal flow and places an explicit exact-SSID field behind an `Erweitert` toggle. Hiding advanced mode clears any previously entered override.
- The read-only status now explicitly explains automatic network release and the button becomes `Zurück zu Geräte` after the measurement. **The changed labels/advanced toggle passed GitHub CI for commit `69ecf083` (run `40905126968`), but still require a fresh screenshot/device smoke.**
- In the later credential-provisioning phase, Android must keep the temporary Bose network until the **validated and user-confirmed submission** completes or fails; do not copy the Phase-A immediate-release point into the middle of a write operation.

## Subsequent regression and bounded discovery revision (2026-10-29)

The tester reports another **failure of "Erneut suchen"** (after a previously successful retest) and an unexpectedly slow search. The regression cannot be marked closed; the prior `[x]` reliability result documents **one observed success**, not repeated stability.

- Eliminated the entire manual Bose-setup-SSID field and advanced toggle at tester request. `WifiNetworkSpecifier` remains a system-approved SSID prefix selection; no scanner added.
- Removed `192.168.1.1` checks (known ~1.8 s timeout per probe) and the unused `:8090/` root check. Post-connect test uses only `192.0.2.1:80/` and `192.0.2.1:8090/info`, both confirmed HTTP 200 on this speaker. Prior port results remain preserved in the historical evidence table.
- Changed Android's pending request timeout from 30 seconds to 20 seconds and added elapsed seconds to `onAvailable` / `onUnavailable` status, separating **OS Wi-Fi discovery/connection latency** from subsequent HTTP probe latency.
- Replaced the unreliable same-Activity retry with a fresh Activity lifecycle: explicit retry unregisters the old callback, opens a new PoC Activity and immediately issues a new network request when resumed. This aims to emulate the previously successful complete wizard restart. The original immediate first-request behavior is retained. **Not yet validated on device**.
- Proposed QA: 3 first-search passes with Bose SoftAP already active, 3 delayed-Bose startup passes that must recover using in-screen retry without manual navigation, check no duplicated system prompts, Cancel/return to home Wi-Fi and both HTTP 200 endpoints. Record whether failure is in the Android network picker or in HTTP. Do not claim Phase B ready before repeated reliability tests.
- This revision requires fresh CI and user hardware verification; the result is a **hypothesis**, not a confirmed Android framework root cause.

## Android 17 / API 37 debug evidence requested (2026-10-29)

- Tester reports `ro.build.version.release=17`, `ro.build.version.sdk=37`. App build in this branch has `targetSdk=37`, with `ACCESS_LOCAL_NETWORK` declared and a runtime permission check on API 37+. No evidence yet of runtime permission denial: the speaker is reachable on a subsequent request.
- First uploaded filtered Logcat extract was **only 16 lines (~1.2 KB)**. It included Bluetooth A2DP and a generic `ConnectivityService NetReassign [no changes]` event, but **no identifiable Bose Wi-Fi request, scan/picker decision, `onAvailable` or `onUnavailable` callback**. It does not establish whether the first failure was Android's 20-second request timeout or an earlier abort.
- Added debug-only structured tag `StrBoseSetup` with an Activity session token; timestamps/elapsedMs for request initiation, permissions (counts and grant booleans only), `requestNetwork` returned/exception, `onAvailable`/`onUnavailable`/`onLost`, HTTP probe start/status, network callback unregister and Activity restart/destroy. **No Wi-Fi SSID, BSSID, MAC, passwords, IP addresses, HTTP bodies or Android Network object string representations** are logged. Logged `index` identifies a compile-time HTTP probe, not network identity.
- Logging changes intentionally preserve the current 20-second request timeout, fixed `Bose` setup-Wi-Fi prefix, immediate first request and fresh-Activity retry. Do not change these again before collecting a first-failure and second-success trace.

### Collect minimal app-only Logcat

On Windows PowerShell, with the new debug APK installed:

```powershell
adb logcat -c
# Reproduce failed first search and then successful retry, without clearing logcat.
adb logcat -d -v threadtime -s "StrBoseSetup:D" "*:S" > .\\bose-setup-debug.log
```

Review/redact before sharing, although the new debug logger deliberately contains no network identities. Record whether the Android system consent dialog appeared, whether the Bose AP was listed, and whether it was selected on each attempt.

### Expected diagnostic distinction

- `requestNetwork_call` followed after ~20000 ms by `callback_onUnavailable`: timed out or user rejected/system refused; user observation of picker differentiates
- `callback_onAvailable` followed by HTTP 200 result: Wi-Fi and HTTP both succeeded
- `requestNetwork_call` but no `requestNetwork_returned`: synchronous exception or process/lifecycle issue; look for `requestNetwork_error`
- `retry_new_activity`, `activity_created freshRetry=true`, then new `requestNetwork_call`: fresh-Activity retry executed as expected
- Early `session_close` / `activity_destroyed`: lifecycle or explicit Cancel rather than 20-second timeout

## Still open before Phase A acceptance

- [x] Reconfirm previously successful connection when the Bose is **already** in setup mode after reverting deferred request (user-reported successful)
- [ ] Reliability gate reopened: 3 delayed-start retry tests from the same wizard route, using a new network request and fresh Activity (earlier single test did pass; subsequent regression reported)
- [x] Verify explicit **Cancel** releases temporary network request and Android returns to the home Wi-Fi (tester confirmed)
- [x] Verify **updated Cancel button exits the wizard and returns to Devices**; tester confirms
- [x] Verify that automatic release after a **completed successful probe** returns the phone to its home Wi-Fi; tester directly observed this behavior
- [ ] Record Android API level and original Bose firmware version, if available without writing settings
- [x] Record status/content-type of read-only Bose `GET /info` on 8090: **HTTP 200; text/xml**
- [ ] Confirm local build/lint/CI of the retry revision and device smoke
- [ ] Validate Bose-specific WLAN-profile submission contract separately before any Phase B write

The 404 on `/` does **not** indicate failure of port 8090 or the entire Bose API. Do not infer Wi-Fi provisioning support from an open TCP/HTTP port without validating the exact endpoint and contract.
