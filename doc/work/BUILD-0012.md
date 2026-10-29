# BUILD-0012 — Bose Wi-Fi Provisioning PoC

**Status:** PHASE A CORE TRANSPORT/RETRY/NETWORK RECOVERY VERIFIED ON SOUNDTOUCH 20 III; REMAINING EVIDENCE AND PROVISIONING CONTRACT PENDING
**Reference device:** Bose SoundTouch 20 Series III (model 355589-SM2), original Bose firmware
**Source baseline:** accepted STR Remote 0.2.1; release tag and APK remain unchanged
**Decision:** Bose setup Wi-Fi / SoftAP via Android `WifiNetworkSpecifier`, **not** Bluetooth
**Availability:** Wizard/PoC on Android 10 (API 29) and newer; existing STR Remote remains supported on Android 8.0+ (minSdk 26)

## Goal and user experience

Enable an owner of a second-hand, still-original-firmware SoundTouch speaker to set up its home Wi-Fi before installing SoundTouch Reborn (STR). The final user journey is:

1. **Prepare speaker:** show model-specific button sequence and initiate the temporary Android system-approved connection to the Bose setup Wi-Fi.
2. **Configure home Wi-Fi:** select/enter home SSID, enter the passphrase once, submit to the **local Bose provisioning interface** after protocol validation and explicit user action.
3. **Verify:** release the temporary setup network; reconnect/recover the normal Android home connection and detect the speaker on that LAN. Hand off to separate STR installation instructions.

The first deliverable is **not** this complete wizard: it is a non-destructive real-device **Phase A discovery/protocol PoC**.

## Established references and open questions

- Bose's SoundTouch 20 Series III support material describes **Preset 2 + Volume minus** until the Wi-Fi LED is amber, the `Bose SoundTouch` setup SSID, and local setup via `http://192.0.2.1`.
  https://support.bose.de/article/soundtouch-20-series-iii-connecting-to-a-wi-fi-network
- Older SoundTouch documentation mentions `192.168.1.1`; actual gateway/host **must be measured** on this speaker/firmware, not assumed across models.
- A community guide proposes `POST /performWirelessSiteSurvey` and `POST /addWirelessProfile` on port 8090. Treat the route, request/response syntax, XML escaping and applicable firmware as **unverified hypotheses**, not as an official stable Bose contract:
  https://github.com/ekenhein/Bose-Sound-Touch/blob/main/docs/guides/DEVICE-INITIAL-SETUP.md
- Android `WifiNetworkSpecifier` is intended for a local peer/AP connection with system user confirmation; unlike an Internet network, it need not be validated for Internet access. Bind Bose HTTP requests to the returned `Network`, not to an assumed default route.
  https://developer.android.com/develop/connectivity/wifi/wifi-request
- The Android 13+ `NEARBY_WIFI_DEVICES` permission and Android 10–12L location-permission requirements must be checked against the exact APIs actually used; do not add broad location/Wi-Fi scan permissions by default:
  https://developer.android.com/develop/connectivity/wifi/wifi-permissions

## Phase A — read-only transport and protocol proof

**No Wi-Fi credentials, factory reset, firmware update or provisioning POST in Phase A.**

1. Confirm the SoundTouch 20 is on original firmware. Record the displayed firmware/software version only if obtainable without modification; never log serial numbers, credentials or network secrets.
2. Enter alternate/setup mode by the documented buttons; **do not reset the device**. If SoftAP is not exposed, stop and investigate before changing speaker state.
3. On Android 10+, obtain the Bose SSID from user confirmation/manual entry, construct `WifiNetworkSpecifier`, and call `ConnectivityManager.requestNetwork()`. Keep the request in the foreground and handle explicit consent, denial, timeout, `onUnavailable`, `onLost` and Activity lifecycle.
4. After `onAvailable`, verify local `Network` routing/link properties. Open a small, time-limited **read-only** HTTP request through that network's `SocketFactory` or `Network.openConnection`. Candidate hosts `192.0.2.1` and `192.168.1.1` require on-device observation/validation; do not send traffic to arbitrary external hosts.
5. Observe whether the setup page is served on port 80 and whether the Bose control endpoint on 8090 offers an identifiable read-only interface. Record only sanitized HTTP status codes/content types and documented API responses; do not collect or publish account identifiers, MAC/serial numbers or Wi-Fi credentials.
6. Always unregister the network callback, cancel pending operations and release the temporary Wi-Fi session on cancellation, failure, completion or Activity destruction. Normal STR discovery/Remote must remain unaffected.
7. Evaluate Android 10/11 and Android 13+ permissions/callback behavior on real hardware when available; do not claim broad compatibility from one Pixel test.

**A acceptance:** explicit Android join succeeds, the intended Bose local host responds through the returned network, tested failure/cancellation paths release the request, no destructive writes or password exposure, and results are documented with firmware-specific evidence. Otherwise **BLOCKED** with precise technical reason.

## Phase B — opt-in WLAN provisioning, gated by Phase A

Only after Phase A demonstrates the exact Bose endpoint and payload behavior on this SoundTouch 20:

1. Implement small, testable Bose provisioning client separated from `StrDiscovery`, `EndpointProbe` and the STR WebView.
2. Provide user-controlled SSID entry (auto-fill only if an API safely exposes it) and an editable passphrase input; never claim Android can reveal the stored home-Wi-Fi password.
3. Show a clear confirmation before the single state-changing profile submission. Properly encode XML/URL fields; reject malformed inputs and cap response/body sizes and timeouts.
4. Keep credentials **in volatile memory only**, without saved instance state, PreferencesStore, crash-report payloads, logs, analytics or WebView JavaScript injection. Do not persist passwords.
5. A successful HTTP response or disappearance of Bose SoftAP is **not enough** to claim success. Release the temporary network, restore Android's normal home-network path, then verify the speaker with fresh local discovery/reachability and user-visible retry/timeout.
6. Support recoverable outcomes: user denied system join; Bose SoftAP missing; HTTP endpoint unreachable; unsupported Bose firmware; invalid home credentials; WPA mode incompatibility; speaker left SoftAP but was not found; app background/rotation; no phone home-Wi-Fi connection.
7. No firmware flash, reset, account binding, automatic STR installation, cloud access or playback logic changes. The STR install remains a separate handoff.

**B acceptance:** verified credentials transfer on real 355589-SM2 original firmware, successful LAN join independently detected after setup AP release, safe failure/retry on wrong password, no secrets in logs/storage, stable default STR flows, clean local build/lint/CI, and manual Pixel QA.

## Design and security guardrails

- KISS: a separate, small `BoseSetupNetwork`/provisioning client and a simple wizard coordinator; avoid a generalized device-management framework or background service.
- **No system-wide** `bindProcessToNetwork()` by default: use network-scoped sockets/connections so an existing STR session and unrelated network traffic cannot be rerouted.
- No hardcoded 'Bose-only' SSID validation that would reject a user-renamed speaker; require user confirmation of the SSID and local-host identity.
- Do not treat `NET_CAPABILITY_VALIDATED` as mandatory for setup AP.
- Restore the app UI on cancel, errors, app background and process restart without retaining secrets or a dangling callback.
- Android 8/9: the current Remote/Devices features continue unaffected; wizard explicitly unavailable (not a blocked spinner).
- Permission changes must be isolated, justified, documented and regression-tested (existing app uses `NEARBY_WIFI_DEVICES` with API cap and `ACCESS_LOCAL_NETWORK`).
- Cleartext HTTP is permitted **only** to the verified local Bose setup host for this workflow; no broad URL routing/redirects or arbitrary web pages through the STR WebView.
- Do not generalize results to all SoundTouch generations until explicitly tested.
- `0.2.1` tag, published APK and accepted QA remain immutable.

## Task sequence

- [x] T01 Record product/transport/Android-floor decision for SoundTouch 20 Series III.
- [x] T02 Implement a read-only `WifiNetworkSpecifier` prototype, behind an explicit debug-only entry. Local build/CI and device proof remain separate gates.
- [x] T03 Real-device Phase A: setup SSID, Android consent, host/port response, graceful disconnection. Confirmed by tester: `192.0.2.1:80/` HTTP 200 (HTML), `192.0.2.1:8090/info` HTTP 200 (XML); root `:8090/` 404; alternate `192.168.1.1` timed out. In-place retry, Cancel/return-to-Devices and automatic home-Wi-Fi recovery after the probe all verified.
- [ ] T04 Record sanitized observations (Bose firmware, IP/gateway, ports, result timings, Android API version). Partial HTTP result evidence: [2026-10-29-build-0012-phase-a-soundtouch20.md](../evidence/qa/2026-10-29-build-0012-phase-a-soundtouch20.md).
- [ ] T05 Review actual Bose provisioning contract; decide **GO / BLOCKED** for Phase B.
- [ ] T06 Only if GO: implement credential submission with the safety guards above.
- [ ] T07 Test credentials transfer and LAN rediscovery on original firmware, including failures.
- [ ] T08 Design/implement the full 1–2–3 wizard, separate from STR installation.
- [ ] T09 Complete quality gates, doc convergence, and explicit acceptance before any new release.

## T02 prototype implementation (2026-10-29)

- Debug-only `app/src/debug/AndroidManifest.xml` declares appropriate runtime permissions and the internal PoC Activity (not exported); the debug app has **one launcher** labelled `STR Remote PoC`. The release variant contains no PoC Activity.
- Debug `applicationIdSuffix = \".poc\"` gives package `app.strremote.android.poc`, allowing co-installation alongside the signed `app.strremote.android` without changing its private settings or signing identity.
- T02 runtime correction: `ConnectivityManager.requestNetwork()` requires the normal install-time permission `CHANGE_NETWORK_STATE`. The first debug build omitted it and showed `SecurityException` before Android could display the Wi-Fi consent dialog. The debug manifest now declares it, and the PoC checks it before making the request. No new production permission was added; real-device SoftAP validation remains pending.
- `app/src/debug/kotlin/app/strremote/android/BoseSetupProbeActivity.kt` provides a minimal German UI with optional exact SSID. Blank SSID selects an Android system-approved SSID beginning with `Bose`.
- Android 10–12L: request `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION` together (Android 12+ combined-request rule); Android 13+: request `NEARBY_WIFI_DEVICES`; Android API 37+: also request `ACCESS_LOCAL_NETWORK`. The debug manifest removes the production `NEARBY_WIFI_DEVICES` API cap **only for debug**. The user still explicitly approves the `WifiNetworkSpecifier` request.
- The network request removes Internet capability and is bounded to 30 s; only `Network.openConnection` performs local HTTP. Neither default-network switching nor `bindProcessToNetwork` occurs.
- Initial candidate probes: HTTP `GET /` (status/headers only) to `192.0.2.1:80`, `192.0.2.1:8090`, `192.168.1.1:80`, `192.168.1.1:8090`. After first device evidence, read-only `GET /info` on `192.0.2.1:8090` was added. Only HTTP status/content-type; no body read, POST, password input, firmware update or reset.
- Explicit cancel, 30 s connection timeout, `onUnavailable`, `onLost`, Activity destruction and short background cleanup release the request. The pending Android system consent UI is not forcibly interrupted by `onStop`.
- Known limitation: one untested debug-only implementation is **not** real-device evidence. Do not mark Phase A PASS or move on to provisioning until hardware validation.
- Android documentation: https://developer.android.com/develop/connectivity/wifi/wifi-request and https://developer.android.com/develop/connectivity/wifi/wifi-permissions

### Build and manual test instructions

```powershell
Set-Location "D:\\Android\\STR\\str-remote-android"
git fetch origin
git switch feature/build-0012-bose-wifi-provisioning
git pull --ff-only
.\\gradlew.bat clean :app:assembleDebug :app:lintDebug :app:assembleRelease --stacktrace
adb devices
adb install -r .\\app\\build\\outputs\\apk\\debug\\app-debug.apk
```

The debug PoC installs as `app.strremote.android.poc` and is labelled **STR Remote PoC**, separately from the signed production **STR Remote** app. Start **STR Remote PoC → Geräte → Neues Gerät hinzufügen** (English: Devices → Add new device). This button is displayed only for the Android 10+ `.poc` package and opens the internal `BoseSetupProbeActivity`; it is hidden in release builds. A previously saved or active STR speaker can still send you directly to Remote, so tap **Geräte** first. There is no second launcher icon. Signed production STR Remote stays installed with its own data.

For T03 use a SoundTouch 20 Series III with original Bose firmware: enter Setup mode using **Preset 2 + Vol−** until the Wi-Fi LED is amber; do not perform a factory reset. Open the PoC, leave SSID blank to use the Android Bose-prefix picker (or type the exact Setup SSID), accept Android's join prompt, and capture only the local HTTP status/content-type results. The PoC automatically releases the network after inspection. Test **Cancel**, missing network and permission denial separately. Never publish home Wi-Fi credentials, SSIDs, MAC/serial identifiers or full HTTP page bodies.

## T03 partial device evidence and retry remediation (2026-10-29)

- User confirmed the Bose Setup SoftAP responds at `192.0.2.1:80/` with HTTP 200 (`text/html`), and the **Bose device-info API** at `192.0.2.1:8090/info` responds with HTTP 200 (`text/xml`). The root `:8090/` responds with HTTP 404 (`text/html`); **root 404 is not an API failure**. Two `192.168.1.1` candidates timed out. Evidence: [Phase A SoundTouch 20 observations](../evidence/qa/2026-10-29-build-0012-phase-a-soundtouch20.md).
- Reported issue: start setup-network discovery while Bose is not in SoftAP mode, then enter SoftAP mode; a retry does not find it until the whole flow is restarted.
- Root cause remains a hypothesis pending repeat test. The original PoC disables its action button while waiting; additionally, the regular STR device-list **Erneut versuchen** runs mDNS/STR discovery, not the Bose SoftAP picker.
- First retry revision added a 400 ms delayed request; the user reported the SoundTouch **was no longer found even on an otherwise normal attempt**. We removed the unverified delay and restored the immediate `requestNetwork()` call. **The user confirms first-time connection works again** and `/info` responds with 200/XML. In-place **Erneut versuchen** unregisters the previous callback and makes a fresh request; **the tester confirms it works for a late-starting SoftAP**. No automatic background looping.
- A real-device regression test confirmed **retry without leaving the PoC** after the speaker enters setup mode. Tester also confirmed that **Trennen / Abbrechen** restores Android's normal Wi-Fi, but exposed a UX defect: the button did not close the wizard. Code now calls `closeSession()` followed by `finish()`; this navigation change requires device validation. Verify normal Wi-Fi recovery after automatic test completion separately.
- Post-change GET `/info` on Bose `:8090` returned **200 / text/xml** on the tested SoundTouch 20 III. Only response status/content-type were inspected, not its XML body.
- Still no WLAN credential submission or configuration mutation in the PoC.

## Phase A debug UX correction (2026-10-29)

- The tester confirmed the temporary setup network is released by `Trennen / Abbrechen` and Android reconnects to its original home Wi-Fi. The original handler failed to close `BoseSetupProbeActivity`; the revised handler performs **`closeSession()` then `finish()`**, returning to the already open STR Remote PoC Devices screen. Return navigation has **not** yet been validated on hardware.
- The tester's screenshot showed a crowded single paragraph and content beginning too close to the system status icons. The debug-only wizard now uses the same system-bar-inset pattern as `MainActivity`, plus grouped preparation steps, higher-contrast status/results sections and clearer spacing. The Android 10–16+ layout/rotation still needs device verification.
- Changes are **debug-only**; no new Bose write calls, permissions, persistence, cloud access or release UI were introduced. The signed 0.2.1 release remains unchanged.

## Phase A automatic release / UI terminology clarification

- The read-only Phase A probe intentionally calls `closeSession()` immediately after all HTTP checks. This releases the temporary Bose network and allows Android to return to its regular Wi-Fi **before** the user taps Cancel. The tester observed exactly this expected behavior. The Cancel button is still needed to stop an unfinished attempt and leave the wizard.
- The Cancel button now also calls `finish()`. Tester confirmed the device page is displayed afterwards.
- `Bose-Setup-SSID` refers to the temporary Wi-Fi **broadcast by the speaker**, not the home-network SSID needed for later provisioning. The normal path requires no entry: Android displays candidate Bose networks for approval. The misleading optional textbox was moved behind the explicit advanced `Anderes Bose-Setup-WLAN verwenden` option; no generic SSID entry appears during normal setup. User-facing labels distinguish `Setup-WLAN suchen`, `Erneut versuchen` and `Zurück zu Geräte` after a completed read-only probe.
- These latest UX changes need a dedicated debug-device screenshot smoke and CI verification. No credentials are sent or persisted; Phase B is gated on validating the Bose WLAN provisioning request and ensuring the requested network remains connected throughout submission.

## Phase B protocol research — no write authorization (2026-10-29)

- Bose's own SoundTouch 20 III instructions confirm the alternate browser-based setup at `http://192.0.2.1/`: choose the home network, enter its password, and submit via the speaker's setup page. This is **official evidence that local Wi-Fi provisioning exists**, but not documentation of the underlying HTTP request payload.
  https://support.bose.ca/article/soundtouch-20-series-iii-connecting-to-a-wi-fi-network
- The community's SoundTouch setup investigation documents `POST /performWirelessSiteSurvey` and `POST /addWirelessProfile` on `:8090`, with an XML `AddWirelessProfile` payload. This is an **unverified community-derived contract** and has **not been called** on the test ST20 III. Do not assume its securityType values, firmware compatibility or credential-failure behavior without verifying the actual supported contract.
  https://github.com/gesellix/Bose-SoundTouch/blob/main/docs/content/docs/guides/DEVICE-INITIAL-SETUP.md
- An HTTP 200 from read-only `GET /info` proves the local API is listening, **not** that either Wi-Fi setup POST is supported on this firmware. Phase B remains blocked until remaining T03 recovery/cancellation gates and explicit write-path investigation. No credentials are handled by the current PoC.

## Evidence template

| Item | Observation |
|---|---|
| Hardware | ST20 Series III / 355589-SM2 |
| Original Bose firmware/software | To be measured |
| Android device/API level | To be measured |
| Setup entry sequence succeeded | Unknown |
| Setup SSID shown | Unknown; do not record private SSID verbatim publicly |
| Network callback/permission outcome | Unknown |
| Setup gateway or HTTP host | Unknown; probe after link established |
| HTTP 80 / HTTP 8090 read-only response | Unknown |
| OnLost/unregister and normal network recovery | Unknown |
| Phase A result | NOT TESTED |
| Phase B eligibility | BLOCKED UNTIL PHASE A |

## Done criteria

The PoC is a **measured, evidenced capability** rather than an inferred protocol match. Until the real Bose SoundTouch 20 test passes, no user-facing provisioning claim, no automatic profile POST and no app release version bump.
