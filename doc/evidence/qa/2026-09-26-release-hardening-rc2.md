# RC2 release-hardening evidence — 2026-09-26

Source: real development-workstation output supplied by the project owner.

- Update script applied while preserving generated Gradle Wrapper files.
- A fresh PowerShell session initially lacked `JAVA_HOME`/`java` in `PATH`; after exposing Android Studio JBR, wrapper execution succeeded.
- `./gradlew clean :app:assembleDebug :app:lintDebug --stacktrace`: **BUILD SUCCESSFUL**, 9s.
- Lint: **0 errors, 5 warnings**.
- `./gradlew :app:assembleRelease --stacktrace`: **BUILD SUCCESSFUL**, 4s.
- Remaining actionable warnings were launcher-resource packaging; Gradle 9.8 availability is deliberately deferred until after 0.1.0.

This evidence closes the previous blocking lint gate and establishes the basis for rc3 packaging-only convergence.
