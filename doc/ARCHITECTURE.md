# Architecture

## Scope

STR Remote remains a thin Android integration layer around the web remote served by SoundTouch Reborn. STR owns playback/business behavior; STR Remote owns Android discovery, endpoint selection, local UI metadata, persistence, permission/network handling and WebView integration.

## Components

- `MainActivity` — Android lifecycle, local-network permission and Wi-Fi state, discovery/device-management UI, speaker switching, manual-host flow, system insets and WebView orchestration.
- `StrDiscovery` — mDNS/DNS-SD discovery and STR TXT-record parsing.
- `EndpointProbe` — asynchronous reachability checks and STR port fallback.
- `PreferencesStore` — app-private persistence for the last successful endpoint, explicitly saved speakers, room assignments and local UI preferences such as the dismissed device hint.
- `SpeakerModels` — internal candidate/endpoint/saved-speaker/room models and identity helpers.

No background service, database, cloud backend or native playback stack exists.

## Startup and connection flow

1. Initialize local-network permission and current Wi-Fi transport state.
2. Rebuild persistent saved-speaker presentation from app-private preferences.
3. When Wi-Fi is available, probe the last successful endpoint for fast reconnect.
4. Run finite STR discovery and merge discovered candidates with saved entries.
5. Resolve reachable STR endpoints using advertised-port handling plus 8888/17008 fallback.
6. A successful endpoint becomes the current/last-successful endpoint and opens in the constrained WebView.
7. STR's own web application handles playback/business behavior.

No validated Internet connection is required for these local operations.

## Speaker-management flow

The device list is one merged view of discovered and explicitly saved speakers:

- row tap connects/switches through the existing endpoint path;
- assigning a room implicitly saves the reachable speaker;
- built-in rooms are stored as language-neutral IDs and localized only for display;
- custom room text is stored locally as entered;
- deleting removes saved metadata and hides the row for the current view; a later discovery may find the physical speaker again;
- unreachable saved speakers remain representable so the user can retry/manage them;
- room/UI metadata never changes endpoint probing, speaker identity or WebView security.

## Persistence boundary

Persistence is deliberately small and app-private:

- last successful endpoint supports reconnect;
- explicitly saved speakers support durable device management;
- room metadata is presentation-only;
- the dismissed-help preference is UI-only;
- none of this data is uploaded or synchronized by STR Remote.

## Dependency direction

The Android wrapper depends on STR's local contracts. STR does not depend on the Android wrapper. There is no hosted STR Remote service.

## Native shell geometry

The native root owns Android system-bar insets. Toolbar/device-list spacing is component-local. The WebView fills the remaining content rectangle and the upstream STR web application owns layout inside that WebView.


## Bose setup (0.3.0)

- `BoseSetupProbeActivity`: Android 10+ four-step, model-aware setup wizard plus separate verification and result views.
- `BoseWirelessSiteSurvey`, `BoseWirelessProfileXml`, `BoseWirelessProfilePost`, `BoseSupportedUrlsProbe`: bounded, network-scoped interactions with Bose original firmware. State-changing `addWirelessProfile` runs only once after the explicit user action, with no automatic retry.
- `BoseLanVerification`: bounded same-device recognition after the system returns to the phone's normal Wi-Fi, using local discovery and Bose `/info`.
- `PendingBoseStore`: local, independent original-firmware speaker records (device identity, optional verified IP, room). These are never saved STR endpoints.
- `DiscoveryCycleGuard`, `SpeakerTapPolicy`: JVM-testable policies for finite probing and predictable behavior of STR-enabled versus Bose-only rows.

The production APK shares the original `app.strremote.android` package ID to permit upgrades from 0.2.1. Android 8/9 continue to use the existing STR features, while the Bose setup activity is only reachable from Android 10+. Browser installation guidance opens the upstream STR website.
