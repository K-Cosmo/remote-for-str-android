# STR local contracts

These contracts describe what the Android wrapper currently relies on. They are integration assumptions that must be validated against real STR behavior when changed.

## Discovery

- primary DNS-SD type: `_streborn._tcp.`
- legacy DNS-SD type: `_soundtouchstick._tcp.`
- useful TXT attributes: `deviceID`, `boxDeviceID`, `friendlyName`, `model`, `version`

## Web endpoint

- supported ports: 8888 and 17008
- reachability probe: `GET /api/status`
- accepted probe response: HTTP 200–399
- UI base URL: `http://<host>:<resolved-port>/`

## Compatibility rule

Do not alter these assumptions from memory or guesswork. Use current STR source/documentation plus real speaker evidence, record any discrepancy in `FINDINGS.md`, then update this contract.
