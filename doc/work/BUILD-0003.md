# BUILD-0003 — 0.1.0 release-candidate hardening

## Goal

Convert the verified 0.1.2-dev MVP into the first public release candidate without adding product features.

## Inputs

- successful 0.1.2-dev real Android build;
- second real-device discovery screenshot;
- second real-device connected/WebView screenshot;
- focused 0.1.2-dev Logcat;
- existing `/doc` governance and release gates;
- current upstream SoundTouch Reborn public documentation/API contract.

## Scope

- version line becomes `0.1.0-rc1`;
- expand the public GitHub README;
- disclose upstream STR prerequisite and project independence;
- disclose AI-assisted development and explicitly distinguish it from runtime AI;
- converge status/findings/changelog/release gates from BUILD-0002 evidence;
- feature-freeze the MVP;
- define the remaining reproducibility/signing/interaction evidence for final `0.1.0`.

## Non-goals

- no new application dependency;
- no native reimplementation of STR controls;
- no speculative permission changes;
- no configuration-cache optimization without measured need;
- no guessed Daemon JVM criteria;
- no signing secrets added to source control.

## Acceptance for this work item

BUILD-0003 itself is complete when source/docs are internally consistent and the release-candidate package is ready for the remaining external release-gate tests. It does not by itself make `0.1.0` final/ACCEPTED.
