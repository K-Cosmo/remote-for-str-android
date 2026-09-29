# BUILD-0008 - Gradle 9.8.x Maintenance Evaluation

## Goal

Evaluate whether the project gains a concrete maintenance benefit from moving beyond the accepted Gradle 9.7.1 baseline.

## 2026-09-28 evaluation

- project baseline: AGP 9.4.1 + Gradle 9.7.1 + Java 25;
- Android's AGP 9.4 compatibility guidance requires Gradle 9.6.0 or newer;
- Gradle 9.8.0 was released on 2026-09-24;
- its headline additions include Java 27 support, Maven mirror reuse and Windows performance improvements;
- none of those capabilities is currently required by STR Remote;
- the accepted 9.7.1 baseline already builds and lints successfully locally and in CI.

## Decision

**DEFERRED.**

Do not spend the next application-development cycle on the build-system upgrade. Revisit after 0.2.0, or earlier only if AGP/Android Studio compatibility, a relevant Gradle bug fix or a concrete feature makes the upgrade useful.

If resumed later, BUILD-0008 remains an isolated maintenance build and must not be combined with application feature changes.
