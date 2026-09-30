# BUILD-0011 - 0.2.1 UX Compactness and Device Status

## Goal

Improve vertical space on the remote screen and make saved-speaker reachability immediately understandable without changing STR behavior or Android architecture.

## Scope

1. Compact native banner
   - combine app title and selected-speaker label into one compact two-line text column;
   - vertically center the Devices/Remote action beside that column;
   - restore the comfortable v3 button sizing and add a little more vertical breathing room;
   - leave the embedded STR WebView untouched.

2. Saved-speaker reachability presentation
   - saved + reachable: stronger green fill + 2 dp green border + Online;
   - saved + unreachable from the app (including no Wi-Fi path): stronger red fill + 2 dp red border + Offline;
   - saved + probe in progress: neutral card;
   - keep text status so color is never the only signal.

3. Device-card hierarchy
   - title: device name + optional room;
   - second line: model + Online/Offline/checking state;
   - third line: STR version + IP address when version is available;
   - retain decreasing text sizes for title, status line and technical-detail line.

4. Remote no-Wi-Fi and reconnect handling
   - never intentionally expose Chromium's generic connection-error page for a known no-Wi-Fi state;
   - show native same-Wi-Fi guidance while preserving the selected-speaker header;
   - provide Retry plus direct Android Wi-Fi settings/panel navigation on the Remote view;
   - provide the same direct Wi-Fi settings/panel action on the Devices no-Wi-Fi state;
   - probe the selected endpoint before recovery/device-selection WebView loads;
   - keep the WebView hidden behind a native checking state until the main page finishes successfully;
   - after Wi-Fi returns, allow a short settle interval before Retry probing;
   - if the endpoint is still unavailable, remain on native device-unreachable guidance rather than loading Chromium error content.

5. Stable saved-speaker state
   - persist the last known Online/Offline reachability for saved speakers in app-private preferences;
   - display that last state while background revalidation runs;
   - change green/red only when a completed probe produces a changed result;
   - use a short Unknown status only when no previous result exists;
   - keep card geometry and the model/status + STR-version/IP hierarchy stable during checks.

## Non-goals

- no change to mDNS discovery;
- no change to endpoint probe ports or fallback;
- no new persistence model;
- no new permission;
- no WebView/API contract change;
- no native playback/business logic;
- no Gradle/AGP upgrade.

## Implementation state

**IMPLEMENTED — VERIFICATION PENDING**

Implementation targets `0.2.1` / versionCode 10.

## Required verification

- `tools/verify-doc-consistency.ps1`;
- clean `assembleDebug`, `lintDebug` and `assembleRelease`;
- GitHub Android CI;
- real-device Pixel smoke of compact header;
- real-device saved-speaker states:
  - reachable -> green + Online;
  - unreachable after completed probe -> red + Offline;
  - checking/unknown -> neutral;
- regression smoke that the embedded STR remote still loads and controls normally.

Acceptance and release evidence must be appended only after those checks exist.