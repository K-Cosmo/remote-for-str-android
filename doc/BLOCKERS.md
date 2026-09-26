# Blockers

There is no known functional blocker in the verified MVP core path.

Verified and no longer blocking:

- working Gradle/JVM baseline captured;
- trusted Gradle 9.7.1 Wrapper generated and independently validated;
- rc3 clean debug/lint build succeeds with 0 lint errors;
- rc3 release assembly succeeds;
- GitHub CI independently builds debug + lint + release on Ubuntu/JDK 25;
- launcher icon packaging and normal/themed icon presentation verified;
- back navigation verified;
- reconnect-after-restart verified;
- local-network permission denial/recovery verified;
- representative STR controls smoke-tested;
- local release-signing identity created outside the repository.

Remaining release-engineering gate before public `0.1.0`:

- build, cryptographically verify, install and smoke-test the exact signed `0.1.0` publication APK from the final source commit.

No feature changes are required for release.
