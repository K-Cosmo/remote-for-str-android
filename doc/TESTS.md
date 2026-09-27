# Test and evidence strategy

## Minimum maintenance gate

1. run `tools/verify-doc-consistency.ps1`
2. `gradle -version`
3. `gradle :app:assembleDebug :app:lintDebug --stacktrace`
4. install resulting debug APK on a real supported Android device when runtime behavior changed
5. grant/deny local-network permission and verify both flows when permission/discovery behavior changed
6. verify discovery of a real STR speaker when discovery behavior changed
7. verify 8888/17008 endpoint resolution against the real speaker when endpoint behavior changed
8. verify WebView loads and normal STR controls work when wrapper/runtime behavior changed
9. relaunch app and verify last-speaker reconnect when connection behavior changed
10. verify native back navigation when navigation behavior changed
11. collect applicable build output, focused Logcat and screenshots into `doc/evidence/qa/`

Documentation-only changes do not require a new Android APK when they do not change application source/resources/build configuration. They require the local documentation consistency gate. GitHub Actions remains an Android build/lint/release-assembly check only.

## Documentation consistency regression checks

- `app/build.gradle.kts` versionName equals `doc/STATUS.md` Development version;
- README Current release equals `doc/STATUS.md` Latest public release;
- accepted releases have a matching `doc/CHANGELOG.md` heading and no unchecked release gates;
- current planning no longer points at an already accepted final-publication step;
- relative Markdown links resolve inside the repository.

## BUILD-0002 visual regression checks

- app title and Devices action are fully below the status-bar inset;
- discovery view shows one primary state message, not duplicate mDNS/status text;
- discovered speaker row remains readable without avoidable status wrapping;
- connected endpoint status appears only with the WebView;
- WebView uses the full available native content width;
- WebView bottom reaches the content boundary immediately above the system navigation inset;
- upstream STR page retains its own internal padding/navigation behavior unchanged.

## Regression focus

- speaker discovery
- permission handling
- endpoint fallback
- saved endpoint reconnect
- navigation/security restrictions
- manual host fallback
- system-bar geometry on modern edge-to-edge Android
- WebView content rectangle
- documentation/release-state consistency

A successful compile alone is not functional evidence.
