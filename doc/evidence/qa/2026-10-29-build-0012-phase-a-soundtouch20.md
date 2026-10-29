# BUILD-0012 — SoundTouch 20 Series III SoftAP Phase A observations

**Date:** 2026-10-29
**State:** PARTIAL PHASE A — setup transport reachable; API path and retry regression still to be validated
**Reference hardware:** Bose SoundTouch 20 Series III / 355589-SM2 / original Bose firmware (per test owner)
**App:** STR Remote PoC, separate debug package `app.strremote.android.poc`

## User-observed results

The user confirms that a fresh Android Bose-setup connection flow succeeds once the speaker is actually in setup mode. The debug app issues read-only `GET /` requests scoped to the Android-selected `Network`, and displays only HTTP status/content-type or network exception class.

| Target | User-observed result | Interpretation |
|---|---|---|
| `http://192.0.2.1:80/` | `HTTP 200; text/html` | Bose setup web UI responds |
| `http://192.0.2.1:8090/` | `HTTP 404; text/html` | API HTTP server responds, but root path is not a usable endpoint |
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
- **Correction for next test:** restore the previously working **immediate** `requestNetwork()` invocation; retain a manually triggered in-place retry that unregisters the previous callback and discards its results, then performs a fresh request. No periodic background retries.
- User guidance explains that an already-open Android system picker should be dismissed before triggering a new request.
- Expanded read-only HTTP probes now include `GET http://192.0.2.1:8090/info`. SoundTouch API documentation identifies `/info` as the read-only device information endpoint; this PoC does **not** read or persist the XML response body, only status/content-type.
- The public `v0.2.1` APK/source remain unchanged.

## Still open before Phase A acceptance

- [ ] First re-confirm previously successful connection when the Bose is **already** in setup mode; then re-test device appearing **after** initial failed/waiting attempt using the **Bose PoC retry in the same screen**; no Activity restart
- [ ] Verify explicit **Cancel** releases temporary network request
- [ ] Verify a completed successful probe returns the phone to its regular Wi-Fi route
- [ ] Record Android API level and original Bose firmware version, if available without writing settings
- [ ] Record status/content-type of read-only Bose `GET /info` on 8090
- [ ] Confirm local build/lint/CI of the retry revision and device smoke
- [ ] Validate Bose-specific WLAN-profile submission contract separately before any Phase B write

The 404 on `/` does **not** indicate failure of port 8090 or the entire Bose API. Do not infer Wi-Fi provisioning support from an open TCP/HTTP port without validating the exact endpoint and contract.
