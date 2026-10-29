# BUILD-0012 — Bose Wi-Fi Provisioning PoC

**Status:** PHASE A + B1 DEVICE VERIFIED; 3+3 WIFI PICKER/RETRY GATE CONFIRMED BY TESTER; DEBUG-ONLY B2 SINGLE-POST READY FOR FIRST HARDWARE TEST (NOT YET RUN)
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
3. On Android 10+, use the Android system network picker through `WifiNetworkSpecifier` with an SSID prefix filter (`Bose`). Do **not** request manual entry of the Bose-setup SSID. Keep the request in the foreground and handle explicit consent, denial, timeout, `onUnavailable`, `onLost` and Activity lifecycle.
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
- [x] T05 Accept debug-only controlled hardware investigation on this reference speaker. B1 on-device endpoint list confirmed; original Bose API v1.1 lacks POST contract. Tester independently confirmed all 3 first-start + 3 late-SoftAP retry passes (reported 2026-10-29; per-run logs not attached). Community payload remains unverified, so **production provisioning is NOT approved**.
- [ ] T06 Controlled credential submission proof: Debug-only single POST implementation prepared and CI-gated; actual isolated-test-network hardware outcome still PENDING. No automated POST retry, credentials persisted or release behavior change.
- [ ] T07 Test credentials transfer and LAN rediscovery on original firmware, including failures.
- [ ] T08 Design/implement the full 1–2–3 wizard, separate from STR installation.
- [ ] T09 Complete quality gates, doc convergence, and explicit acceptance before any new release.

## T02 prototype implementation (2026-10-29)

- Debug-only `app/src/debug/AndroidManifest.xml` declares appropriate runtime permissions and the internal PoC Activity (not exported); the debug app has **one launcher** labelled `STR Remote PoC`. The release variant contains no PoC Activity.
- Debug `applicationIdSuffix = \".poc\"` gives package `app.strremote.android.poc`, allowing co-installation alongside the signed `app.strremote.android` without changing its private settings or signing identity.
- T02 runtime correction: `ConnectivityManager.requestNetwork()` requires the normal install-time permission `CHANGE_NETWORK_STATE`. The first debug build omitted it and showed `SecurityException` before Android could display the Wi-Fi consent dialog. The debug manifest now declares it, and the PoC checks it before making the request. No new production permission was added; real-device SoftAP validation remains pending.
- `app/src/debug/kotlin/app/strremote/android/BoseSetupProbeActivity.kt` provides a small German UI. The **Bose-setup SSID is never manually entered**, including in advanced mode; Android supplies its system-controlled network picker with the `Bose` prefix pattern. This is distinct from the *home Wi-Fi SSID* needed in a future provisioning phase.
- Android 10–12L: request `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION` together (Android 12+ combined-request rule); Android 13+: request `NEARBY_WIFI_DEVICES`; Android API 37+: also request `ACCESS_LOCAL_NETWORK`. The debug manifest removes the production `NEARBY_WIFI_DEVICES` API cap **only for debug**. The user still explicitly approves the `WifiNetworkSpecifier` request.
- The network request removes Internet capability and now has a **20 s timeout**, instead of 30 s, with elapsed discovery time surfaced in diagnostic text. Only `Network.openConnection` performs local HTTP. Neither default-network switching nor `bindProcessToNetwork` occurs.
- The first exploratory probe included two **known-timeout** `192.168.1.1` routes and the `8090/` root (404). The active PoC now probes **only** two confirmed useful local endpoints: read-only `GET /` at `192.0.2.1:80` and read-only `GET /info` at `192.0.2.1:8090`. This removes up to ~3.6 s of timeout waiting from the post-connection test. No response bodies, POST, password input, firmware update or reset.
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
- `Bose-Setup-SSID` refers to the temporary Wi-Fi **broadcast by the speaker**, not the home-network SSID needed for later provisioning. **The user explicitly rejected even an advanced manual-SSID entry**, since the real workflow uses automatic Android selection. The entire manual field/advanced toggle is removed; the system picker is the sole path. User-facing labels distinguish `Setup-WLAN suchen`, `Erneut versuchen` and `Zurück zu Geräte` after a completed read-only probe.
- The earlier UX revision passed GitHub CI (code commit `69ecf083`, workflow run `40905126968`), but the tester later reported unreliable repeated discovery and long search times. The follow-up retry/latency revision is tracked separately below and requires new CI and device verification. No credentials are sent or persisted; Phase B is gated on validating the Bose WLAN provisioning request and ensuring the requested network remains connected throughout submission.

## Phase A follow-up — remove manual setup SSID and diagnose intermittent retry (2026-10-29)

**New user report:** manual Bose-setup SSID input is unnecessary, the Android device search feels unusually slow, and the previous in-place **Erneut suchen** flow is intermittently failing again. A previous success in a single test must **not** be treated as a stable fix; the reliability gate is reopened.

- **Simplify setup:** remove the optional and advanced SSID input entirely. Continue using only Android's `WifiNetworkSpecifier` selection with the proven `Bose` prefix, no general Wi-Fi scanner and no home-Wi-Fi credential input.
- **Latency:** the old read-only HTTP validation included two `192.168.1.1` endpoints which consistently timeout (~1.8 s connect timeout each); stop probing them. Likewise, no need to request `:8090/` once `/info` is confirmed. This improves the time **after** connection; it does not make Android's OS-owned Wi-Fi picker scan faster.
- **Bound the picker:** lower the request timeout from 30 s to 20 s and display the observed elapsed time in the `onAvailable`/`onUnavailable` feedback. This bounds failure waiting; it does not guarantee discovery within 20 s.
- **Retry lifecycle:** fresh `requestNetwork()` calls in the *same* Activity did not reliably recover after a failed selection; the user previously reported that fully restarting onboarding did. On an explicit second tap, tear down the network callback, start a **new** debug `BoseSetupProbeActivity` and close the old one. The new Activity starts a fresh request on `onResume` without delaying the first attempt. This is an engineering hypothesis and must be tested against repeated late-SoftAP retries, Android picker dismissal and cancellation. No background loop or extra location permissions.
- **Android scanning caveat:** `WifiNetworkSpecifier` uses a system-managed filtered network selection and does not provide the same scan observations as an independent Wi-Fi scanner. Direct `WifiManager.startScan()/getScanResults()` would require location permissions and is subject to OS scan throttling, so it is not added as a speculative fix.
  https://developer.android.com/reference/android/net/wifi/WifiNetworkSpecifier.Builder
  https://developer.android.com/develop/connectivity/wifi/wifi-scan
- **Regression gate:** test 3 consecutive first-try cases with SoftAP already active; then 3 cases where the speaker only enters SoftAP mode **after** the first Android picker is closed. Each retry must present a *new* Android dialog and connect to `:8090/info`. Record whether failure happens before the Android dialog, inside the dialog (no Bose entry), at `onUnavailable`, or only after `onAvailable`. No credentials are transmitted.
- **Don't release Phase B:** do not implement or claim write-path support until this reliability gate and firmware-specific provision contract are understood.

## B1 capability probe implementation (2026-10-29)

**Research basis:**

1. Bose supports Wi-Fi setup through the device-local browser UI on `http://192.0.2.1/` (official SoundTouch 20 Series III support page). This proves a usable setup route, but not its programmatic HTTP request body:
   https://support.bose.de/article/soundtouch-20-series-iii-connecting-to-a-wi-fi-network
2. Community project AfterTouch implements `POST /addWirelessProfile` using `<AddWirelessProfile><profile ssid="..." password="..." securityType="wpa_or_wpa2" /></AddWirelessProfile>` and optionally `POST /performWirelessSiteSurvey`. This is implementation evidence **from a third party and other tested Bose firmware**, not a successful write on our original-firmware ST20 III:
   https://github.com/gesellix/Bose-SoundTouch/blob/main/pkg/service/setup/wifi_provision.go
   https://github.com/gesellix/Bose-SoundTouch/blob/main/docs/content/docs/guides/DEVICE-INITIAL-SETUP.md
3. `GET /supportedURLs` returns a firmware-specific XML list of available API paths; only a test on our reference hardware can establish if the original ST20 III lists the two Wi-Fi endpoints:
   https://github.com/gesellix/Bose-SoundTouch/blob/main/docs/content/docs/reference/API-ENDPOINTS.md

**Historical B1 read-only behavior (now the default path; B2 is separately opt-in):**

- After the already verified read-only `GET http://192.0.2.1:80/` and `GET http://192.0.2.1:8090/info`, invoke `GET http://192.0.2.1:8090/supportedURLs` through the **exact Android `Network` object** authorized for the Bose SoftAP. No default-route binding.
- Accept only HTTP 200 XML, enforce 2.5 s connect/read timeouts, disable redirects and caching, and bound the XML body to 64 KiB.
- Parse XML strictly enough to reject an unexpected root and DOCTYPE, and **only show literal whitelisted path membership**: `/addWirelessProfile`, `/performWirelessSiteSurvey`, `/getActiveWirelessProfile`.
- Do **not** display/store/log full XML, device IDs, SSIDs, MACs, serials, credentials or arbitrary endpoints. A route listed in `/supportedURLs` is a **capability hint**, not proof of a supported POST schema or working write on this firmware.
- In the **default B1 mode**, release the temporary setup-Wi-Fi connection after read-only probes. The explicitly selected B2 mode instead retains it through a single consented POST.
- Code: `BoseSupportedUrlsProbe.kt` and the B1 call site in `BoseSetupProbeActivity.kt`. The original B1 milestone did not include any POST; the later **debug-only B2 experiment** is documented below. No automatic firmware modification.

**B1 evidence to capture on SoundTouch 20 Series III:**

- [x] On SoundTouch 20 III original firmware, `GET /supportedURLs` returned HTTP 200 with valid `supportedURLs` XML (user observation 2026-10-29).
- [x] The device advertises `/addWirelessProfile`, `/performWirelessSiteSurvey` and `/getActiveWirelessProfile`. Listing a URL is not POST success or schema validation.
- [ ] Confirm unchanged `/info` HTTP 200, normal Wi-Fi restoration, and no regression in Cancel/retry
- [ ] Inspect whether the Bose setup page and capabilities provide a sufficiently trustworthy indication of security types before designing a write test
- [ ] Only then prepare a separately approved **debug-only, explicit-consent write** to a throwaway test Wi-Fi with local network verification. Do not silently repeat an ambiguous write: an HTTP timeout might mean the speaker already accepted the profile and is switching networks.
- [ ] Run build/lint/CI and test on original-firmware hardware

**Historical gate at B1 completion:** B2 was blocked. Following the user's subsequent 3+3 confirmation, only the narrowly scoped debug-only experiment below is authorized; production provisioning is still blocked. Do not infer Android-wide compatibility.

## B1 device result and B2 contract review (2026-10-29)

**User-observed read-only result on original Bose SoundTouch 20 Series III / 355589-SM2:**

| Request | Observation | Meaning |
|---|---|---|
| `GET http://192.0.2.1:80/` | HTTP 200, `text/html` | Local setup page reachable |
| `GET http://192.0.2.1:8090/info` | HTTP 200, `text/xml` | Bose read-only information API reachable |
| `GET http://192.0.2.1:8090/supportedURLs` | HTTP 200, valid XML | Firmware-provided URL list parsed successfully |
| `/addWirelessProfile` | Listed | Capability hint; POST/payload still unverified |
| `/performWirelessSiteSurvey` | Listed | Capability hint; invocation still unverified |
| `/getActiveWirelessProfile` | Listed | Capability hint; response schema not verified |

No wireless profile has been sent, and no home SSID/password or full XML response has been collected. The earlier Phase A real-device observations support return to home Wi-Fi after read-only test completion; no distinct B1 retry reliability result is implied. Evidence: [2026-10-29 SoundTouch 20 QA](../evidence/qa/2026-10-29-build-0012-phase-a-soundtouch20.md).

**Documentation provenance and actual contract coverage:**

- **Bose Corporation, official SoundTouch Web API v1.1 (dated 2026-04-01)**: does **not** document `/addWirelessProfile`, `/performWirelessSiteSurvey` or `/getActiveWirelessProfile`, even though it describes regular API endpoints and site-survey notifications. Do not present the candidate profile schema as a Bose-published contract.
  https://assets.bosecreative.com/m/496577402d128874/original/SoundTouch-Web-API.pdf
- **AfterTouch community guide and Go source**: implement `POST http://192.0.2.1:8090/addWirelessProfile`, `Content-Type: text/xml`, with a candidate payload `<AddWirelessProfile><profile ssid="..." password="..." securityType="wpa_or_wpa2" /></AddWirelessProfile>` and expect `<AddWirelessProfileResponse />`. The Go implementation escapes XML attributes. These are community findings and **not yet verified on our firmware**.
  https://github.com/gesellix/Bose-SoundTouch/blob/main/docs/content/docs/guides/DEVICE-INITIAL-SETUP.md
  https://github.com/gesellix/Bose-SoundTouch/blob/main/pkg/service/setup/wifi_provision.go
- The community implementation automatically retries a POST after timeout. **Do not copy this behavior**: after an ambiguous timeout the speaker might already be applying the profile and dropping SoftAP. Our first real-device attempt must be single-shot and followed by independent state/LAN verification.

**Original B1-to-B2 gating checklist (status updated after 3+3 confirmation):**

1. **User reports passed:** 3 first-start + 3 late-SoftAP retries on the test setup; no per-run Logcat, exact Android build or Bose firmware version has been supplied. Do not generalize this result.
2. Define a debug-only, user-confirmed single-POST test against an **isolated test SSID** chosen by the tester (never a background auto-submit). Do not treat `wpa_or_wpa2` as proved for WPA3/open networks; initially limit the test to a compatible WPA2-Personal test AP. No factory reset.
3. Keep the Bose `Network` callback active through the single POST; send the community candidate with correct XML escaping, bounded request/response handling, redirects disabled and no credentials in storage/logs. No automatic retry after request ambiguity.
4. Record separately: HTTP status, parsed response *type* only, whether the SoftAP disappears, whether Android returns to home Wi-Fi, and **independent** rediscovery of the same physical speaker on the test LAN. Never mark success from HTTP 200 or AP disappearance alone.
5. If the first POST times out or fails, report **indeterminate** rather than automatically resubmitting. Reinspect local Bose state and the destination LAN before any new consent. Build/lint/CI and real device evidence must pass before advancing to full wizard implementation.

**Updated decision:** B1 discovery PASS; debug-only **single-shot** B2 hardware experiment now authorized by D-028; production/write wizard and release remain NOT AUTHORIZED. Product 0.2.1 and its release artifact stay unchanged.

## B2 offline request-encoding milestone (historical; followed by debug-only write test)

- Added **debug-only** `BoseWirelessProfileXml.buildCandidate()`, which produces the AfterTouch community's proposed `AddWirelessProfile` XML string. It does **not** open a socket, issue POST, collect home Wi-Fi input, persist credentials or log XML.
- For this initial *isolated WPA2 test* draft, SSID is limited to 1–32 printable ASCII characters and password to 8–63 printable ASCII characters. These are deliberate conservative **test-only** limits, not a claim about all SSIDs/security modes supported by Bose. Before a general wizard, revisit internationalized SSIDs, WPA variants and exact firmware requirements.
- Escapes XML attribute metacharacters (`&`, `<`, `>`, quotes, apostrophes). Added host-side JUnit tests for expected candidate syntax, escaping and invalid test inputs; CI now invokes `:app:testDebugUnitTest`.
- This was the first **offline-only** preparation milestone. It was followed by the D-028-authorized debug-only single-shot POST implementation. Actual device provisioning, recovery and production acceptance remain outstanding.

## B2 gated hardware experiment — 3+3 user confirmation (2026-10-29)

**User-reported test acceptance:** all 3 normal SoftAP first-start runs and all 3 late-SoftAP / fresh-Activity retry runs succeeded. This closes the **test-device Wi-Fi picker/retry reliability gate** by tester report; raw per-run Logcat, exact Android build and Bose firmware version are still not included. It is not a claim of broad compatibility across phone OS versions or Bose firmware.

**Debug-only test entry:** Devices → add new device → `B2: WLAN-Hardwaretest vorbereiten`. Android's normal Bose network selection dialog still appears. The app performs Phase A + B1 GET probes first; **only a valid B1 capability result advertising /addWirelessProfile** keeps the Bose-scoped `Network` alive and exposes two volatile credential inputs. A separate modal confirmation explicitly warns about configuring the Bose speaker.

- **Required prerequisites:** physically available original-firmware reference ST20 III, Bose setup AP active, a separate 2.4-GHz **WPA2-Personal** test WLAN configured and known to join, a second method (e.g. router client list) to establish destination-LAN presence. The test WLAN may temporarily replace the speaker's normal Wi-Fi connectivity; prepare a non-reset recovery route.
- **Operation:** a **single** scoped `POST http://192.0.2.1:8090/addWirelessProfile` with AfterTouch-candidate XML. No survey POST, automatic retry, redirects, global network binding, credential logging or persistence. In-memory payload bytes are wiped after the attempt; password/SSID EditText cleared before the network call. `FLAG_SECURE` suppresses screenshots and Recents previews in B2.
- **Response interpretation:** only status code + XML acknowledgement root are reported, never response text or credentials. The POST can succeed while the setup AP disconnects. A timeout, lost AP, or 2xx without expected XML is **indeterminate**, not permission to resend. Connection is released on completion/cancel, with background cleanup bounded by request timeout.
- **Verification after POST:** reconnect Android to the separate test WLAN, inspect router association/client table and `GET http://<device-LAN-IP>:8090/info` for **the same physical speaker**; verify it stays accessible there. Report whether setup AP vanished and whether the phone recovered Wi-Fi. An HTTP acknowledgement or vanished AP alone is not success.
- **Evidence to return, without secrets:** B1 supported endpoints, B2 HTTP/result classification, SoftAP disappearance yes/no, test LAN device association yes/no, same-device `/info` reachability yes/no, Android Wi-Fi restoration yes/no. Never paste passwords, actual SSIDs, MAC addresses or unredacted XML.
- **STOP:** if device is unreachable or the B2 result is ambiguous, do not repeat the POST until state is independently checked. Do not merge to main or release from this experiment.

**Next gate:** independently confirm the real-device test outcome and recovery from wrong credentials/failed association before progressing to a production wizard. Product 0.2.1 remains unchanged.

## Historical protocol research before B2 single-shot authorization (2026-10-29)

- Bose's own SoundTouch 20 III instructions confirm the alternate browser-based setup at `http://192.0.2.1/`: choose the home network, enter its password, and submit via the speaker's setup page. This is **official evidence that local Wi-Fi provisioning exists**, but not documentation of the underlying HTTP request payload.
  https://support.bose.ca/article/soundtouch-20-series-iii-connecting-to-a-wi-fi-network
- The community's SoundTouch setup investigation documents `POST /performWirelessSiteSurvey` and `POST /addWirelessProfile` on `:8090`, with an XML `AddWirelessProfile` payload. This is an **unverified community-derived contract** and has **not been called** on the test ST20 III. Do not assume its securityType values, firmware compatibility or credential-failure behavior without verifying the actual supported contract.
  https://github.com/gesellix/Bose-SoundTouch/blob/main/docs/content/docs/guides/DEVICE-INITIAL-SETUP.md
- An HTTP 200 from read-only `GET /info` proves the local API is listening, **not** that either Wi-Fi setup POST is supported on this firmware. The initial B1 probe handled no credentials; the later gated debug-only experiment is now implemented but has no hardware result yet.

## Evidence template

| Item | Observation |
|---|---|
| Hardware | ST20 Series III / 355589-SM2 |
| Original Bose firmware/software | To be measured |
| Android device/API level | To be measured |
| Setup entry sequence succeeded | Yes, based on user-reported 3+3 Wi-Fi picker/retry passes |
| Setup SSID shown | Unknown; do not record private SSID verbatim publicly |
| Network callback/permission outcome | 3 first-start + 3 delayed-SoftAP retries successful, user report (raw traces pending) |
| Setup gateway or HTTP host | `192.0.2.1` measured on reference Bose setup AP |
| HTTP 80 / HTTP 8090 read-only response | Both HTTP 200; B1 `/supportedURLs` XML lists three Wi-Fi routes |
| OnLost/unregister and normal network recovery | Unknown |
| Phase A result | GET/connection/read-only recovery observed; 3+3 reliability user-confirmed |
| Phase B eligibility | Debug-only single-shot test authorized; actual association result PENDING |

## Done criteria

The PoC is a **measured, evidenced capability** rather than an inferred protocol match. Until the real Bose SoundTouch 20 single-shot test and independent LAN join verification pass, no end-user provisioning claim, no automatic profile POST or retries, no main merge and no app release version bump.
