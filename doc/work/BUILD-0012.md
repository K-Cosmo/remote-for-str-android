# BUILD-0012 — Bose Wi-Fi Provisioning PoC

**Status:** PHASE A PROTOTYPE IMPLEMENTED / REAL-DEVICE VERIFICATION PENDING
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
- [ ] T03 Real-device Phase A: setup SSID, Android consent, host/port response, graceful disconnection.
- [ ] T04 Record sanitized observations (Bose firmware, IP/gateway, ports, result timings, Android API version).
- [ ] T05 Review actual Bose provisioning contract; decide **GO / BLOCKED** for Phase B.
- [ ] T06 Only if GO: implement credential submission with the safety guards above.
- [ ] T07 Test credentials transfer and LAN rediscovery on original firmware, including failures.
- [ ] T08 Design/implement the full 1–2–3 wizard, separate from STR installation.
- [ ] T09 Complete quality gates, doc convergence, and explicit acceptance before any new release.

## T02 prototype implementation (2026-10-29)

- Debug-only `app/src/debug/AndroidManifest.xml` declares appropriate runtime permissions and a separate debug launcher entry. The release variant contains no PoC Activity/launcher.
- Debug `applicationIdSuffix = \".poc\"` gives package `app.strremote.android.poc`, allowing co-installation alongside the signed `app.strremote.android` without changing its private settings or signing identity.
- `app/src/debug/kotlin/app/strremote/android/BoseSetupProbeActivity.kt` provides a minimal German UI with optional exact SSID. Blank SSID selects an Android system-approved SSID beginning with `Bose`.
- Android 10–12L: request `ACCESS_FINE_LOCATION`; Android 13+: request `NEARBY_WIFI_DEVICES`; Android API 37+: also request `ACCESS_LOCAL_NETWORK`. The debug manifest removes the production `NEARBY_WIFI_DEVICES` API cap **only for debug**. The user still explicitly approves the `WifiNetworkSpecifier` request.
- The network request removes Internet capability and is bounded to 30 s; only `Network.openConnection` performs local HTTP. Neither default-network switching nor `bindProcessToNetwork` occurs.
- Fixed candidate probes: HTTP `GET /` (status/headers only) to `192.0.2.1:80`, `192.0.2.1:8090`, `192.168.1.1:80`, `192.168.1.1:8090`. No body read, POST, password input, firmware update or reset.
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

The debug PoC installs as `app.strremote.android.poc`. Launch the **Bose Wi-Fi PoC (Debug)** icon; the normal **STR Remote** icon from the debug build is also present. Signed production STR Remote stays installed with its own data.

For T03 use a SoundTouch 20 Series III with original Bose firmware: enter Setup mode using **Preset 2 + Vol−** until the Wi-Fi LED is amber; do not perform a factory reset. Open the PoC, leave SSID blank to use the Android Bose-prefix picker (or type the exact Setup SSID), accept Android's join prompt, and capture only the local HTTP status/content-type results. The PoC automatically releases the network after inspection. Test **Cancel**, missing network and permission denial separately. Never publish home Wi-Fi credentials, SSIDs, MAC/serial identifiers or full HTTP page bodies.

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
