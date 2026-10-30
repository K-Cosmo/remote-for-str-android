# Blockers

The signed public **0.3.0** baseline has no known unresolved release blockers. The **unreleased 0.3.1** release candidate still has unfulfilled acceptance gates (signing with original key, update-over-0.3.0 device test and final QA). Repository-history privacy cleanup is a separate maintenance action, not evidence that a product runtime feature is broken.

## Accepted baseline

The following release-critical areas are closed by real build/runtime evidence:

- Gradle 9.7.1 / AGP 9.4.1 / Java 25 build baseline;
- trusted Gradle Wrapper and GitHub Android CI;
- debug/lint/release assembly;
- 16 KiB-aware release APK alignment and signing verification;
- STR discovery, Wi-Fi/no-device/unreachable handling and Retry;
- device selection, saved speakers, room assignment and quick switching;
- reconnect, permission recovery and native back navigation;
- constrained WebView navigation and normal STR controls;
- signed 0.3.0 publication artifact and GitHub release.

## Open pre-release tasks (0.3.1 / PR #5)

- confirm final user-device smoke after the display-name change and the production APK update path;
- protect the original Android application ID and signing certificate;
- address historical private-LAN endpoint exposure across Git refs (requires an offline history rewrite; the present PR branch is already redacted);
- ensure both release and documentation consistency gates pass on the final commit.

See `RELEASE_GATES.md`, `PLANNED.md` and the dated privacy audit for authoritative status.

## Non-blocking observations

These are monitored findings, not current blockers:

- Chromium/WebView may log hidden-API denials and missing-Bluetooth-permission messages even though STR Remote owns no Bluetooth feature;
- Android PackageManager may log an alignment-check message despite the final signed APK passing explicit zipalign verification and running normally;
- the upstream STR web page can occasionally log transient API `Failed to fetch` messages while the wrapper and main remote UI remain operational.

Do not add permissions, dependencies or speculative workarounds merely to silence these logs. Escalate only when a reproducible STR Remote function fails.

## Future compatibility watchpoints

- additional SoundTouch model evidence;
- upstream STR authentication or local API/discovery changes;
- intentionally scheduled build-tool maintenance.

Those items belong in `BACKLOG.md` / `PLANNED.md`, not in the active blocker list.
