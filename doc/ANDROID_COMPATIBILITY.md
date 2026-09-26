# Android and build compatibility

## Current baseline

| Item | Version | State |
|---|---:|---|
| minSdk | 26 | normative |
| compileSdk | 37 | normative |
| targetSdk | 37 | normative |
| Android Gradle Plugin | 9.4.1 | stable baseline |
| Gradle | 9.7.1 | real build baseline reported by project owner |
| CI JDK | 17 | configured baseline |
| Java source/target | 17 | project baseline |
| Local Gradle daemon JVM criteria | not pinned yet | awaiting real `gradle -version` evidence |

AGP preview versions are not adopted merely because Android Studio offers them. A toolchain update requires a separate change, successful build/lint evidence and an entry in `DECISIONS.md` when it changes the baseline.

## Daemon JVM criteria

Android Studio recommends Daemon JVM criteria for consistent IDE/CLI daemon selection. The supplied real build log does not contain the JVM major version used by Gradle, so this baseline does **not** guess one. Before pinning criteria, capture `gradle -version` (or the equivalent Android Studio Gradle-JDK value) from the working environment. Then generate the criteria from that verified major version.

Automatic JDK provisioning is also intentionally deferred: it requires a toolchain resolver/provisioning setup and adds another build-time external dependency. Add it only if real portability needs justify it.

## Gradle Wrapper

A trusted Gradle Wrapper must be generated and committed before the final public 0.1.0 release. The release candidate still relies on an explicitly selected Gradle 9.7.1 in CI/local instructions; wrapper generation is a remaining release gate rather than being fabricated in the artifact environment.

## Gradle runtime / Daemon JVM criteria

The verified development workstation runs Gradle 9.7.1 with Android Studio JetBrains Runtime 25.0.3. `gradle/gradle-daemon-jvm.properties` pins the Gradle daemon requirement to Java major version 25 without pinning a vendor or machine-specific installation path. CI provisions a compatible JDK 25.

This criterion does **not** bootstrap the wrapper script itself. On Windows/macOS/Linux, `gradlew` still needs a Java executable available through `JAVA_HOME` or `PATH` before Gradle can start and apply daemon criteria.
