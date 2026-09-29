# Test and evidence strategy

## Minimum maintenance gate

For every change:

1. run the documentation consistency gate;
2. run `git diff --check`;
3. select additional Android/runtime gates according to the changed scope.

Windows PowerShell:

```powershell
.\tools\verify-doc-consistency.ps1 -ProjectRoot .
git diff --check
```

When application source/resources/build configuration changed:

```powershell
.\gradlew.bat --version
.\gradlew.bat clean :app:assembleDebug :app:lintDebug :app:assembleRelease --stacktrace
```

Use `./gradlew` on POSIX systems.

Real-device verification is required when runtime/user-visible behavior changed. Relevant cases include:

- local-network permission grant/deny/recovery;
- discovery of a real STR speaker;
- 8888/17008 endpoint resolution;
- WebView loading and normal STR control;
- app relaunch and last-speaker reconnect;
- native back navigation;
- changed speaker-management behavior;
- focused Logcat/screenshots when diagnosing runtime behavior.

Store applicable evidence under `doc/evidence/qa/`.

Documentation-only changes do not require a new Android APK when they do not change application source/resources/build configuration. GitHub Actions remains an Android build/lint/release-assembly check only.

## Accepted regression matrix — Discovery & Network Resilience (BUILD-0007 / 0.1.1)

These cases remain regression coverage:

1. Wi-Fi off while mobile data remains available -> no local probe/discovery; stable no-Wi-Fi state.
2. No usable Wi-Fi -> same stable state, no indefinite spinner.
3. Wi-Fi present but no STR speaker -> finite discovery ends and exposes STR help/Retry/manual-host paths.
4. Known STR speaker present -> normal discovery/probe/WebView/control path.
5. Saved endpoint plus Wi-Fi unavailable -> no pointless saved-endpoint probe.
6. Speaker discovered but ports 8888/17008 unreachable -> explicit STR-unreachable state distinct from no-device.
7. Enable Wi-Fi and Retry -> recovery without app restart.
8. Wi-Fi without Internet validation -> local STR operation remains eligible.
9. Manual host while Wi-Fi absent -> no-Wi-Fi state rather than pointless probe.
10. Permission recovery, native back navigation and constrained WebView navigation remain regression checks.

## Accepted regression matrix — Speaker Management / Rooms (BUILD-0009 / 0.2.0)

These cases remain regression coverage:

1. Room action on a reachable discovered speaker saves it and survives app restart.
2. Built-in rooms persist and display localized labels.
3. Custom room text persists unchanged.
4. Language change localizes built-in room display without changing stored identity; custom text is unchanged.
5. Saved + discovered representation of the same keyed speaker produces one row.
6. Unkeyed/manual saved endpoint may merge with later discovery by host fallback.
7. Two different keyed speakers do not merge solely because a host is reused.
8. Delete confirmation: cancel preserves; confirm removes saved metadata/current row; later discovery may rediscover the physical speaker.
9. Offline/unreachable saved speaker remains representable/actionable.
10. Switching rows uses the existing endpoint/WebView path and updates reconnect state.
11. Current speaker is clearly indicated; room label does not affect connectivity/security.
12. Successful manual connection can be saved/room-assigned.
13. Dismissed device-management hint stays hidden after relaunch.
14. Device-list spacing remains balanced with the hint visible or hidden.
15. Wi-Fi, timeout, no-device/unreachable, permission, back and WebView restrictions remain regression coverage.

## Release artifact gate

For a public release:

- build only from a clean committed source tree;
- record exact source commit;
- sign with the accepted STR Remote signing identity;
- verify APK signature schemes and certificate;
- run post-sign 16 KiB-aware zipalign verification;
- record final SHA-256;
- install and smoke-test the exact signed APK;
- tag the exact source commit;
- publish exactly the accepted APK + checksum file;
- verify the public asset digest;
- close out `/doc` without moving the release tag or rebuilding the released artifact.

## Documentation consistency regression checks

- `app/build.gradle.kts` versionName equals `doc/STATUS.md` Development version;
- README Current release equals `doc/STATUS.md` Latest public release;
- accepted releases have a matching `doc/CHANGELOG.md` heading and no unchecked release gates;
- current planning no longer points at an already accepted final-publication step;
- relative Markdown links resolve inside the repository;
- root helper Markdown does not duplicate normative `/doc` policy.

A successful compile alone is not functional evidence.
