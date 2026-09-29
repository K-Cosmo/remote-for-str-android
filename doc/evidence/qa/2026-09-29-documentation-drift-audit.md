# Documentation drift audit — 2026-09-29

## Scope

Repository-wide Markdown review after the accepted 0.2.0 release.

Current release/source baseline at audit time:

- repository HEAD before the drift-convergence commit: `cedba6d66f39d3cadd0aa3e5615b7a70cdfe3856`;
- current public release: `0.2.0`;
- release source: `b5ff103e318f4342ba3a748b786084108d4093af`;
- `/doc` is normative.

## Confirmed drift

The audit identified current-state documents that still described earlier release stages:

- root `CHANGELOG.md` stopped at 0.1.0 while `doc/CHANGELOG.md` is canonical and contains later releases;
- root `PRIVACY.md` described only last-speaker persistence and omitted 0.2.0 saved speakers/rooms/UI preference data;
- root `CONTRIBUTING.md` still described the Gradle Wrapper as potentially not yet present;
- `doc/ANDROID_COMPATIBILITY.md` simultaneously contained old JDK-17/not-pinned/wrapper-pending text and later Java-25 evidence;
- `doc/ARCHITECTURE.md` described `PreferencesStore` as last-endpoint-only and omitted saved speakers/rooms/UI preferences;
- `doc/BLOCKERS.md` still described a remaining pre-0.1.0 publication gate;
- `doc/FINDINGS.md` contained several IMPLEMENTED/pending-verification states that were subsequently accepted;
- `doc/DECISIONS.md` contained two different `D-019` headings;
- several current policy documents still used “first release” wording after 0.2.0.

## Convergence decisions

- remove root `CHANGELOG.md`; retain only `doc/CHANGELOG.md`;
- retain root `CONTRIBUTING.md` and `PRIVACY.md` only as thin public pointers;
- update current normative architecture/build/security/privacy/test/blocker documents to the accepted 0.2.0 baseline;
- normalize the duplicate decision number without changing decision substance;
- close stale finding states and add findings for the 0.2.0 speaker-management/release/documentation convergence;
- explicitly classify `doc/work/`, `doc/evidence/qa/`, `/specs/` and `/.specify/` as historical/process snapshots.

## Historical-record rule

Historical build/spec/evidence records are intentionally **not rewritten** merely because their then-current version numbers or pending gates are now old. Their purpose is to preserve the evidence/process state at that time.

Current project truth is determined by the current normative files under `/doc`, especially `STATUS.md`, `SOURCE_OF_TRUTH.md`, `ARCHITECTURE.md`, `ANDROID_COMPATIBILITY.md`, `SECURITY.md`, `PRIVACY.md`, `TESTS.md`, `RELEASE_GATES.md`, `PLANNED.md` and `BACKLOG.md`.
