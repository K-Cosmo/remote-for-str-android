# BUILD-0012 — SoundTouch 20 Series III SoftAP Phase A observations

**Date:** 2026-10-29
**State:** PHASE A/B1 plus 3+3 confirmed; B2 Wi-Fi survey and profile submission USER-REPORTED WORKING; three-page wizard UI pending on-device QA
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

## B1 read-only capability result — 2026-10-29

**Observed by tester on the reference SoundTouch 20 Series III (original Bose firmware):**

```text
http://192.0.2.1:80/       → HTTP 200; Type: text/html
http://192.0.2.1:8090/info → HTTP 200; Type: text/xml
B1 GET /supportedURLs       → HTTP 200; valid XML
/addWirelessProfile         → listed by device
/performWirelessSiteSurvey  → listed by device
/getActiveWirelessProfile   → listed by device
```

The debug probe parses only three literal whitelisted paths and does not export the XML response body. This confirms **firmware-advertised endpoint membership**, not the HTTP method, XML payload, response contract or a successful Wi-Fi profile transfer. No POST, SSID/password transmission, firmware change or factory reset occurred. Device firmware version remains unmeasured. The B1 observation does **not** close the separate intermittent retry reliability gate.

Protocol comparison: Bose Corporation's official SoundTouch Web API **v1.1 (2026-04-01)** does not define these three routes; the AfterTouch community guide/Go source supply an unverified `POST /addWirelessProfile` candidate with `<AddWirelessProfile><profile ssid="..." password="..." securityType="wpa_or_wpa2" /></AddWirelessProfile>`. Treat this as a **community-derived hypothesis** until a controlled, explicitly approved single-POST hardware test. An HTTP timeout may be ambiguous; do not automatically resend credentials.

Sources:
- https://assets.bosecreative.com/m/496577402d128874/original/SoundTouch-Web-API.pdf
- https://github.com/gesellix/Bose-SoundTouch/blob/main/pkg/service/setup/wifi_provision.go
- https://github.com/gesellix/Bose-SoundTouch/blob/main/docs/content/docs/guides/DEVICE-INITIAL-SETUP.md

**Reliability gate (2026-10-29):** User explicitly confirms passing all 3 normal initial-pickup plus 3 delayed-start/fresh-Activity retries. This is a tester report, not supplied per-run Logcat evidence or wide Android compatibility proof. The debug-only single-submit test is now prepared, not executed; no B2 device outcome yet.

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
- [x] Reliability gate: user reports 3 initial-connection and 3 delayed-start fresh-Activity retry passes (2026-10-29); per-run Logcat not attached.
- [x] Verify explicit **Cancel** releases temporary network request and Android returns to the home Wi-Fi (tester confirmed)
- [x] Verify **updated Cancel button exits the wizard and returns to Devices**; tester confirms
- [x] Verify that automatic release after a **completed successful probe** returns the phone to its home Wi-Fi; tester directly observed this behavior
- [ ] Record Android API level and original Bose firmware version, if available without writing settings
- [x] Record status/content-type of read-only Bose `GET /info` on 8090: **HTTP 200; text/xml**
- [ ] Confirm local build/lint/CI of the retry revision and device smoke
- [ ] Verify Bose-specific WLAN-profile POST contract on the hardware with a separately consented single-shot B2 test using only an isolated WPA2 test network.

The 404 on `/` does **not** indicate failure of port 8090 or the entire Bose API. Do not infer Wi-Fi provisioning support from an open TCP/HTTP port without validating the exact endpoint and contract.

## B2 first hardware test — prepared; actual device result pending (2026-10-29)

- Debug-only `B2: WLAN-Hardwaretest vorbereiten` uses the established Wi-Fi picker and reads the existing Phase A/B1 endpoints. B1 must advertise `/addWirelessProfile` before the credential form is displayed.
- Explicit confirmation precedes exactly one POST. No retries after timeout or onLost, no secrets in debug logs/storage, and no claim of LAN association based on HTTP 200 alone.
- **Tester to record:** B2 result string (redacted), whether the Bose setup AP disappears, association on a separate WPA2 test router, reappearance of the same speaker's `/info` on destination LAN, Android Wi-Fi recovery. All outcomes **pending**.


## B2 functional hardware report and wizard UI follow-up — 2026-10-29

- **Tester explicitly reports:** WLAN list loads on the SoundTouch 20 Series III; setting the desired WLAN on the SoundTouch also works.
- No raw HTTP response, independently measured new speaker IP or sanitized LAN `/info` evidence was supplied in this conversation. Record *working according to tester*, not *independently verified by automated probe*.
- **UX issue:** preceding single-page B2 PoC was too technical and the scanned WLAN list needed better formatting. Tester requested **3 steps as 3 pages**: prepare box, connect via Android, choose WLAN and transfer, with guided rather than API terminology.
- New debug-only three-page wizard implemented with inline scan cards, eight initially shown entries and expandable list, hidden technical details, simple statuses, separate final verification page, and unchanged Android connection + Bose API submission logic.
- **On-device checks still open:** page sizing on Pixel, back/cancel behavior, first connection/retry, auto-advance, WPA2 list selection, manual SSID, submit prompt, output verification wording and phone Wi-Fi recovery.

## Three-page KISS refinements and automatic verification — device QA pending (2026-10-29)

- Tester feedback: network list better suited to a popup than inline content; double confirmation undesirable; disabled submit button without visible progress is confusing; manual switch back to a generic "Ziel-WLAN" should be unnecessary when Android restores WLAN automatically.
- New debug-only flow: popup SSID selection, one-tap POST, indeterminate progress indicator, restore Wi-Fi monitoring, bounded Bose mDNS/SSDP LAN discovery, original Bose identity comparison, and optional read-only active wireless profile check.
- Real-device verification **PENDING** for the new automatic discovery, same-speaker identification, active WLAN name and final status. Prior report that standalone B2 survey/POST worked is not evidence that the new automated LAN check has passed.

## Regression reported after guided wizard update (2026-10-29)

- The Android Bose network selector reportedly aborts automatically before a network is selected, or shortly after Bose selection, with the system message **"Die Anwendung hat die Anfrage zur Auswahl eines Gerätes abgebrochen."** The phone sometimes needs time before a new request can succeed. Tester reports an empty `bose-setup-debug.log`.
- The app still contains a 20-second `requestNetwork` timeout (elapsed since pressing Connect, **not** since the Bose network appears in the Android picker), and an aggressive 2-second `onStop` cleanup after `onAvailable`. These are concrete premature cancellation risks; precise runtime trigger cannot be claimed without callback event evidence.
- Changed debug-only wizard to untimed `requestNetwork`, 45-second background cleanup for connected session, 1.5-second retry cooldown, and visible sanitized in-app diagnostics under technical details.
- **Status: device retest pending; previous 3+3 reliability gate must be repeated.** Minimum tests: leave picker open >30 seconds without selection, select Bose with no Internet, retain Android-approved network during system dialog, fast and delayed retry, explicit cancellation, debug event evidence, no auto provisioning writes.
