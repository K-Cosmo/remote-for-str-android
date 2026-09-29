# STR local contracts

These contracts describe what the Android wrapper currently relies on. They are integration assumptions that must be validated against real STR behavior when changed.

## Network eligibility

- automatic saved-endpoint probing and mDNS discovery require an available Wi-Fi transport;
- Internet validation is **not** required;
- local-only Wi-Fi is a valid STR network;
- manual local probing follows the same Wi-Fi eligibility rule.

## Discovery

- primary DNS-SD type: `_streborn._tcp.`
- legacy DNS-SD type: `_soundtouchstick._tcp.`
- useful TXT attributes: `deviceID`, `boxDeviceID`, `friendlyName`, `model`, `version`
- discovery is finite; the current user-facing discovery window is 10 seconds

## Speaker identity and merge behavior

- keyed discovery identity is preferred when STR provides a stable device identifier;
- saved and discovered entries with the same keyed identity represent one speaker;
- unkeyed/manual saved endpoints may use host fallback for merging with later discovery;
- two different keyed speakers must not merge solely because a host address is reused;
- room assignment and other local UI metadata never participate in connectivity/security identity.

## Web endpoint

- supported ports: 8888 and 17008
- reachability probe: `GET /api/status`
- accepted probe response: HTTP 200–399
- UI base URL: `http://<host>:<resolved-port>/`
- top-level in-app WebView navigation remains limited to the selected host and STR ports

## Compatibility rule

Do not alter these assumptions from memory or guesswork. Use current STR source/documentation plus real speaker evidence, record any discrepancy in `FINDINGS.md`, then update this contract and the relevant decision/security documentation before acceptance.
