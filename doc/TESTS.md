# Test and evidence strategy

## Minimum maintenance gate

1. `gradle -version`
2. `gradle :app:assembleDebug :app:lintDebug --stacktrace`
3. install resulting debug APK on a real supported Android device
4. grant/deny local-network permission and verify both flows
5. verify discovery of a real STR speaker
6. verify 8888/17008 endpoint resolution against the real speaker
7. verify WebView loads and normal STR controls work
8. relaunch app and verify last-speaker reconnect
9. verify native back navigation from WebView and device picker
10. collect build output, focused Logcat and screenshots into `doc/evidence/qa/`

## BUILD-0002 visual regression checks

- app title and Devices action are fully below the status-bar inset;
- discovery view shows one primary state message, not duplicate mDNS/status text;
- discovered ST09 row remains readable without avoidable status wrapping;
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

A successful compile alone is not functional evidence.
