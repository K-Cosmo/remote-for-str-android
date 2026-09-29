# Status

**Development version:** 0.2.0
**Latest public release:** 0.2.0
**Status:** RELEASED — ACCEPTED
**Date:** 2026-09-29

## 0.2.0 release

Speaker Management / Rooms is released and accepted.

Release identity:

- source commit: `b5ff103e318f4342ba3a748b786084108d4093af`;
- annotated tag: `v0.2.0`;
- final APK: `STR-Remote-0.2.0.apk`;
- APK SHA-256: `5e7ba21f39752ae359a6de0103a440b363a1823223874778e44173f285243aa7`;
- signer: `C=DE, CN=STR Remote`;
- signer certificate SHA-256: `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`;
- public GitHub release ID: `399416342`;
- publication time: `2026-09-29T18:27:43Z`.

## Verified release gates

- local documentation consistency: PASS;
- clean debug build + lint + release assembly: PASS;
- GitHub Android CI is green for the exact release source commit (main run `36610855156`);
- GitHub Android CI is also green for the pushed release tag (run `36611841390`);
- the signed APK was built from clean committed source `b5ff103e318f4342ba3a748b786084108d4093af`;
- `apksigner` verification succeeds with APK Signature Scheme v2 and v3;
- post-sign 16 KiB-aware `zipalign` verification succeeds;
- the annotated `v0.2.0` tag resolves to `b5ff103e318f4342ba3a748b786084108d4093af`;
- the public GitHub release is non-draft/non-prerelease and publishes the APK plus SHA-256 file;
- GitHub reports the public APK asset digest as `sha256:5e7ba21f39752ae359a6de0103a440b363a1823223874778e44173f285243aa7`, matching the accepted local artifact;
- final signed-APK device smoke was explicitly confirmed during post-release closeout.

## BUILD-0009 outcome

0.2.0 adds persistent saved speakers, quick switching, optional room assignment, explicit room/delete actions, improved device-list presentation, a direct Remote return path and dismissible local guidance while retaining the thin-wrapper architecture and existing local-network/WebView security boundaries.

`0.2.0` is the current accepted public release.
