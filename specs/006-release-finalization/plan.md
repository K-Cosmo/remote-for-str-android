# Plan

1. Promote version to `0.1.0` / versionCode 7.
2. Converge normative release status/evidence documents.
3. Run clean debug + lint + release assembly.
4. Align and sign the release APK locally without persisting signing secrets.
5. Verify signature and SHA-256.
6. Install/smoke-test the exact signed artifact.
7. Push final source, require green CI, then tag/publish `v0.1.0`.
