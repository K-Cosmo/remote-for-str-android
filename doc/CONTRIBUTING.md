# Contributing

## Before a change

1. Read `SOURCE_OF_TRUTH.md`, `STATUS.md`, `GOVERNANCE.md`, `AI_CODING_GUARDRAILS.md` and `CLEAN_CODE.md`.
2. Check `FINDINGS.md`, `BLOCKERS.md`, `PLANNED.md`, `BACKLOG.md` and relevant decisions.
3. For non-trivial work, create/update the matching development spec under `/specs` and build record under `doc/work/`.
4. Keep the patch focused; separate toolchain maintenance from application features.
5. Run the applicable gates from `TESTS.md`.
6. Attach real build/runtime evidence before claiming VERIFIED/ACCEPTED.
7. Converge `/doc` before acceptance.

## Standard local gates

Documentation gate:

```powershell
.\tools\verify-doc-consistency.ps1 -ProjectRoot .
git diff --check
```

Android gate when code/resources/build configuration changed:

```powershell
.\gradlew.bat clean :app:assembleDebug :app:lintDebug :app:assembleRelease --stacktrace
```

Use `./gradlew` on POSIX systems.

Documentation-only changes do not require a new APK when they do not affect application source/resources/build configuration.

## Boundaries

Do not introduce analytics, accounts, hosted backends, Bose branding assets, unnecessary dependencies or native duplicates of STR playback/business logic without an explicit decision.

User-visible UI changes must preserve the supported German/English experience. Security/privacy/permission scope changes require explicit documentation and evidence.

Release APKs are built only from a clean committed source tree with the existing release-signing process.
