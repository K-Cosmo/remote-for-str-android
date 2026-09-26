# Release hardening evidence — rc1

**Date:** 2026-09-26  
**Source:** real developer workstation output supplied by the project owner

## Toolchain

- Gradle Wrapper: 9.7.1
- Gradle Wrapper generation: successful
- Gradle Wrapper second run: successful / up-to-date
- Launcher JVM: JetBrains Runtime 25.0.3
- Daemon JVM: Android Studio JBR (`C:\Program Files\Android\Android Studio\jbr`)
- OS: Windows 11 x64

## Build results

- `:app:assembleRelease`: **BUILD SUCCESSFUL**
- combined `clean :app:assembleDebug :app:lintDebug`: build reached lint and failed only because lint reported a blocking finding

## Lint results

rc1 lint result: **1 error, 8 warnings**.

Blocking error:

- `GestureBackNavigation` on the legacy `onBackPressed()` fallback.

Warnings reviewed individually:

- `UnusedAttribute` for `neverForLocation`: intentional API-level compatibility metadata; older Android ignores it.
- Gradle 9.8.0 available: intentionally deferred until after 0.1.0 because 9.7.1 is the verified release baseline.
- `SetJavaScriptEnabled`: required by upstream STR; accepted only with constrained WebView configuration.
- `InsecureBaseConfiguration`: required because upstream STR serves its dynamic LAN endpoint over plain HTTP.
- obsolete API check/resource qualifier: actionable cleanup for rc2.
- missing monochrome launcher layer: actionable icon cleanup for rc2.

## Convergence decision

Do not create a lint baseline. RC2 implements the smallest source/configuration changes necessary, with local suppressions only for intentional behavior. RC2 must be rebuilt/linted before acceptance.
