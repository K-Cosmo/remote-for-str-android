# Blockers

## Current state

**Remote for STR 0.3.1 is published and accepted.** No outstanding issue is known to block the already published signed release. The signed-APK device smoke test, unchanged package identity, CI, annotated tag and public APK checksum are recorded in [release gates](RELEASE_GATES.md) and [release evidence](evidence/qa/2026-10-31-release-0.3.1.md).

## Open follow-ups — not release blockers

- **STR-022 / [F-028](FINDINGS.md):** current 10px header-spacing workaround depends on private STR DOM; request a supported upstream embedding/CSS hook. The presently tested layout works but future upstream markup may change.
- **[STR-027](https://github.com/K-Cosmo/remote-for-str-android/issues/7):** the owner explicitly deferred negative tests (no email app, rejected WLAN permissions, network failure/timeout and unverified hardware) plus the positive update-alert test for a later release. **Deferred does not mean tested.**
- **STR-024:** ordinary Git history was cleaned; residual GitHub-managed old PR refs/caches may retain old objects. See [Git history provenance](GIT_HISTORY_PROVENANCE.md). This is a privacy-maintenance concern, not a runtime-failure claim.

## Non-blocking observations

- Chromium/WebView may log hidden-API and Bluetooth-permission warnings without a native Bluetooth feature.
- PackageManager may emit alignment warnings despite successful signed-APK alignment verification.
- The upstream STR WebUI may log transient API fetch failures; escalate only if they cause a reproducible user-visible fault.

Do not add unrelated Android permissions or speculative workarounds just to silence logs. Forward planned compatibility and maintenance work to [Backlog](BACKLOG.md) / [Planned](PLANNED.md), and maintain release identity only in [Status](STATUS.md).
