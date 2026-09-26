# Contributing

Thanks for helping improve STR Remote.

For the first releases, please keep changes deliberately small. This project is meant to remain a thin Android integration layer around the web remote already maintained by SoundTouch Reborn.

## Principles

- Prefer Android platform APIs over new dependencies.
- Do not duplicate STR playback/business logic in the Android app without a strong reason.
- Keep discovery compatible with STR's documented `_streborn._tcp` service and 8888/17008 port behavior.
- Do not add analytics, tracking, accounts or a hosted backend.
- Avoid Bose branding assets and do not imply affiliation with Bose or the STR maintainer.
- Preserve German and English strings for user-visible UI changes.

## Before a pull request

```bash
gradle :app:assembleDebug
```

If a Gradle wrapper has been added to the repository, use `./gradlew` instead.
