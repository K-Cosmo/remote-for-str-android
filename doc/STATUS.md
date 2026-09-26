# Status

**Development version:** 0.1.0-rc3  
**Status:** IMPLEMENTED release-packaging convergence candidate; local rc3 re-verification required  
**Date:** 2026-09-26

## Verified product path

Real workstation/device evidence verifies the core STR Remote path:

- Gradle Wrapper 9.7.1 was generated and verified on the development workstation;
- Gradle 9.7.1 runs with JetBrains Runtime 25.0.3 and the daemon uses the Android Studio JBR;
- rc2 `clean :app:assembleDebug :app:lintDebug` completed successfully in 9 seconds;
- rc2 lint has **0 errors and 5 warnings**;
- rc2 `:app:assembleRelease` completed successfully in 4 seconds;
- the APK installs and starts on a real Android device;
- STR discovery finds the real speaker `ST09` (`SoundTouch 10`);
- the discovered speaker reports STR v0.9.86;
- endpoint probing resolves the live remote on port 8888;
- the embedded STR web UI loads successfully;
- system-bar layout, discovery layout and full-width WebView geometry are verified on the real device.

## RC3 purpose

RC3 changes only release packaging/build consistency:

- replace the full-square launcher bitmap-as-background with a true adaptive icon: solid background plus transparent foreground motif;
- provide the monochrome layer directly in the adaptive icon definition;
- remove the now-unused launcher color/resource structure and obsolete duplicate icon resources;
- pin Gradle daemon JVM criteria to Java 25, matching the verified workstation major version;
- align GitHub Actions with Java 25 while keeping application Java source/target at 17.

No product behavior, STR discovery/API contract or Gradle 9.7.1 baseline is changed.

## Remaining release-quality evidence

RC3 is **not yet ACCEPTED as final 0.1.0**. Required next evidence:

- run `clean :app:assembleDebug :app:lintDebug` through the verified Wrapper; target: 0 errors;
- run `:app:assembleRelease`;
- verify `./gradlew --version` reports a Java-25 daemon after the criteria file is present;
- visually verify the normal and themed launcher icon on the real device;
- explicitly verify native back navigation;
- explicitly verify reconnect after app restart;
- explicitly verify local-network permission denial/recovery;
- smoke-test normal STR controls through the wrapper;
- configure and verify release signing;
- install/test the exact final signed publication artifact.

Feature work remains frozen unless new evidence identifies a release blocker.
