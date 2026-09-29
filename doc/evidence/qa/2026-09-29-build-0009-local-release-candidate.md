# BUILD-0009 local release-candidate evidence — 2026-09-29

## Build state

Application: STR Remote 0.2.0 / versionCode 9.

Latest local gate after the final device-list spacing refinement:

- documentation consistency: PASS;
- `clean :app:assembleDebug :app:lintDebug :app:assembleRelease --stacktrace`: PASS;
- build result: `BUILD SUCCESSFUL`;
- Gradle baseline remains 9.7.1 with AGP 9.4.1.

## Pixel evidence

The final development APK was exercised on the real Pixel/STR setup. Confirmed behavior includes:

- device list renders correctly;
- room assignment is visible as `device · room`;
- device row tap activates the corresponding remote;
- device view offers an explicit `Fernbedienung` return action;
- saved-device delete flow shows confirmation and removes the row from the current list;
- selected device/room is visible on the remote screen without a full-width banner;
- device-management hint can be dismissed and remains hidden after restart;
- spacing remains visually balanced after the hint is hidden;
- normal STR WebView/control path remains operational.

## Remaining release evidence

This is pre-signing evidence only. Final release acceptance still requires:

- committed-source GitHub Android CI;
- exact signed `STR-Remote-0.2.0.apk` verification;
- final signed-APK regression smoke, including the remaining persistence/offline/manual-host cases where practical;
- tag/publication verification and final SHA-256 recording.
