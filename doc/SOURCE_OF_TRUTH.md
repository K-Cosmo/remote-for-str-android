# Source of Truth

## Product

STR Remote is a thin Android integration layer around the web remote already served by SoundTouch Reborn (STR). The Android app discovers STR speakers on the local network, resolves a reachable STR endpoint and displays the existing STR web UI in a constrained WebView.

## Architectural boundaries

1. STR owns playback/business logic and the phone-remote web application.
2. STR Remote owns Android discovery, endpoint selection, local persistence, permission handling and WebView integration.
3. Do not duplicate STR playback/business logic natively without an explicit recorded decision.
4. No analytics, tracking, account system or hosted STR Remote backend.
5. Prefer Android platform APIs. A new dependency requires a documented reason and decision.
6. STR discovery uses DNS-SD/mDNS service types `_streborn._tcp.` and legacy `_soundtouchstick._tcp.`.
7. Endpoint probing accepts STR web ports 8888 and 17008; 8888 is preferred when reachable.
8. The local network is the current trust boundary.
9. The native shell owns Android system insets; the embedded STR WebView fills the remaining content rectangle and is not wrapped in product padding.

## Documentation authority

`/doc` is the only normative project documentation. Spec Kit files orchestrate work but cannot silently change product policy or architecture.

Runtime/build evidence can prove that this documentation has drifted. Such evidence does not create an undocumented new rule: the drift is recorded as a finding and `/doc` must be converged to verified reality before acceptance.
