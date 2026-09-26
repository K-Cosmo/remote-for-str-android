# Architecture

## Components

- `MainActivity` — Android lifecycle, permission flow, native shell/speaker selection, system-inset handling and WebView orchestration.
- `StrDiscovery` — mDNS/DNS-SD discovery and STR TXT record parsing.
- `EndpointProbe` — asynchronous reachability checks for STR endpoints.
- `PreferencesStore` — last successful speaker endpoint in app-private preferences.
- `SpeakerModels` — internal speaker candidate/endpoint models and display helpers.

## Primary flow

1. App obtains required local-network permission for the running Android version.
2. Previously saved endpoint is probed first.
3. If unavailable, discovery scans STR service types.
4. Discovered candidate is probed on advertised STR port when valid, then 8888/17008 fallback order.
5. Successful endpoint is persisted and loaded in the WebView.
6. STR's own web application handles playback/business behavior.

## Dependency direction

The Android wrapper depends on STR's published local behavior. STR does not depend on the Android wrapper. No remote STR Remote service exists.

## Native shell geometry

The native root owns Android system-bar insets. Toolbar/discovery spacing is component-local. The WebView fills the remaining content rectangle and STR's upstream web application owns all padding/layout inside that WebView.
