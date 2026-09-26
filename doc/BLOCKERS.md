# Blockers

There is no known functional blocker in the verified MVP core path. The project is in release-candidate hardening rather than feature development.

Verified and no longer blocking:

- working Gradle JVM and version captured;
- trusted Gradle 9.7.1 Wrapper generated;
- rc2 clean debug/lint build succeeds with 0 lint errors;
- rc2 release assembly succeeds.

Remaining release-engineering/evidence gates before final public `0.1.0`:

- rc3 launcher/daemon packaging re-verification;
- normal/themed icon visual check;
- explicit back-navigation behavior check;
- reconnect-after-restart check;
- local-network permission denial/recovery check;
- representative STR control smoke test;
- release signing configuration and verification of the exact signed publication artifact.

The artifact environment has no Android SDK, so it cannot substitute for these real target/release-artifact checks.
