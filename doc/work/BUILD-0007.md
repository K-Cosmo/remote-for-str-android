# BUILD-0007 — 0.1.1 Discovery & Network Resilience

## Goal

Make discovery/connection behavior finite and actionable when the Android device is not on Wi-Fi, no STR speaker is present, or an announced speaker has no reachable STR endpoint.

## Inputs

- public 0.1.0 is accepted;
- real-world observation: discovery starts without Wi-Fi and can leave an indefinite spinner;
- current discovery has no ordinary no-device timeout state;
- current app already has `ACCESS_NETWORK_STATE` / `ACCESS_WIFI_STATE`, so no new permission is required.

## Changes

- versionCode 7 -> 8;
- versionName `0.1.0` -> `0.1.1`;
- use Android `ConnectivityManager.NetworkCallback` with a Wi-Fi `NetworkRequest` to track matching Wi-Fi transports;
- avoid deprecated `ConnectivityManager.allNetworks` polling;
- do not require validated Internet access;
- gate saved-endpoint, discovery and manual probing on Wi-Fi;
- stop mDNS discovery after 10 seconds;
- add no-Wi-Fi, no-device and STR-unreachable states;
- add Retry and upstream STR website actions;
- distinguish per-row checking from completed unreachable endpoint probing;
- ignore stale endpoint-probe callbacks from earlier discovery generations;
- preserve existing permissions, STR discovery/API contracts and WebView restrictions.

## Explicit non-goals

- no Gradle/AGP upgrade;
- no new dependency;
- no new Android permission;
- no speaker favorites/device-management feature;
- no changes to STR web/business logic;
- no Play/F-Droid work.

## Required evidence

See `doc/TESTS.md` BUILD-0007 matrix and the open 0.1.1 gates in `doc/RELEASE_GATES.md`.

## State

**IMPLEMENTED.** Build/lint and real-device verification pending.
