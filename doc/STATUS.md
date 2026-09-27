# Status

**Development version:** 0.1.0  
**Latest public release:** 0.1.0  
**Status:** RELEASED / ACCEPTED  
**Date:** 2026-09-26

## Accepted 0.1.0 release

The first public STR Remote release is accepted from the verified final source and publication artifact.

- final release source commit: `4583e27deb734a5ac268af0c16939009f76c0d63`;
- annotated tag `v0.1.0` points to that commit;
- GitHub Actions is green for the final source commit and the release-tag run;
- final signed publication artifact: `STR-Remote-0.1.0.apk`;
- publication APK SHA-256: `5f48f99f604b7d861f305f4fda0b6383188ded6e45dc660d65d66ea68da8a2fe`;
- GitHub Release publishes the APK and matching SHA-256 file;
- the exact signed APK installs successfully on the real Android device;
- Android App Info reports version `0.1.0`;
- discovery, connection and normal STR control behavior work with the publication APK.

## Verified product path

- Gradle Wrapper 9.7.1 is committed and independently validated by GitHub Actions;
- Gradle 9.7.1 runs with Java 25 daemon criteria;
- debug assembly, lint and release assembly are green locally and in GitHub Actions;
- lint is free of blocking errors;
- STR discovery and endpoint probing resolve the real speaker;
- the embedded STR web UI loads and normal remote controls work;
- adaptive/themed icon, native back navigation, reconnect and local-network permission recovery are verified;
- signing material remains outside the repository.

## Next development step

The next planned application build is **BUILD-0007 / 0.1.1 — Discovery & Network Resilience**.

The first scope is error handling around missing Wi-Fi/local-network connectivity, finite discovery, no-device guidance, STR-unreachable guidance and retry without an app restart. No Internet connection is required for STR operation and must not be used as the connectivity criterion.
