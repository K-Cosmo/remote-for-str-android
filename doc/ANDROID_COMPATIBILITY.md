# Android and build compatibility

## Accepted 0.3.1 baseline

| Item | Version / rule | State |
|---|---|---|
| minSdk | 26 (Android 8.0) | normative |
| compileSdk | 37 | normative |
| targetSdk | 37 | normative |
| Android Gradle Plugin | 9.4.1 | accepted baseline |
| Gradle Wrapper | 9.7.1 | accepted baseline |
| CI JDK | Temurin 25 | configured and verified |
| Local Gradle launcher | Java 25 (vendor/path not pinned) | accepted requirement; workstation JBR 25.0.3 and OpenJDK 25.0.4 are observed local examples |
| Gradle daemon criteria | Java major 25 | pinned |
| Java source/target | 17 | application baseline |

The published 0.3.1 signed APK and exact-source CI were built successfully with this unchanged Gradle/AGP/Java-major baseline.

## Gradle Wrapper and JVM

The repository contains the trusted Gradle 9.7.1 Wrapper and all normal build instructions use it.

`gradle/gradle-daemon-jvm.properties` pins the Gradle daemon requirement to Java major version 25 without pinning a vendor or workstation-specific path. Local builds have reported Android Studio JBR 25.0.3 and OpenJDK 25.0.4 in different sessions; neither workstation-specific version/path is normative. CI satisfies the Java 25 requirement with Temurin 25.

Daemon criteria do not bootstrap the wrapper process itself. `gradlew` / `gradlew.bat` still needs a Java executable available through `JAVA_HOME` or `PATH` before Gradle can start.

## Standard verification commands

Windows PowerShell:

```powershell
.\gradlew.bat --version
.\gradlew.bat clean :app:assembleDebug :app:lintDebug :app:assembleRelease --stacktrace
```

POSIX shells:

```bash
./gradlew --version
./gradlew clean :app:assembleDebug :app:lintDebug :app:assembleRelease --stacktrace
```

## Upgrade policy

Toolchain changes are isolated maintenance work. Do not combine a Gradle/AGP/JDK migration with unrelated application features unless a concrete build requirement makes that unavoidable.

BUILD-0008 tracks evaluation of Gradle 9.8.x. It became eligible after the 0.2.0 release but remains deferred until explicitly scheduled or justified by a compatibility/build need. Until that evaluation is accepted, Gradle 9.7.1 + AGP 9.4.1 + Java 25 remains the normative build baseline.

Automatic JDK provisioning remains intentionally absent; add it only if real portability needs justify the additional build-time dependency.
