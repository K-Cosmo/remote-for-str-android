# Documentation structure and regression review — 2026-10-31

## Scope, authority and method

Reviewed the `main` snapshot after accepted signed [0.3.1](2026-10-31-release-0.3.1.md) and prepared a **documentation-only** convergence branch. GitHub's recursive repository tree enumerated **160 tracked files / 85 Markdown files**: **23 top-level `doc/*.md`**, **12 dated `doc/work/*.md`**, **21 `doc/evidence/qa/*.md`**, **24 `specs/**/*.md`**, **4 root public/helper Markdown files** and **1 `.specify/` constitution entry**. Historical work and QA entries were inventoried by heading/purpose and cross-checked against current release evidence; their dated historical claims were deliberately not reinterpreted as current rules. The normative, public and integration documents were examined in detail against code, GitHub Release data and relevant upstream STR WebUI markup.

**Source hierarchy:** `doc/SOURCE_OF_TRUTH.md` and `doc/GOVERNANCE.md` control documentation roles. Root README is **not** another specification. Current-state rules, released behavior, architectural boundaries, decisions and security policy belong to one corresponding canonical `doc/*.md`; dated feature experiments, release signoffs and rejected options stay in `doc/work`, `doc/evidence`, `specs` and the chronological `DECISIONS.md`.

## Findings and disposition by owner document

| Authoritative document | Review outcome / correction |
|---|---|
| `README.md` (root) | **124 → 40 lines**. Keep description, compact features, quick install, meaningful provisioning risk, community/marks disclaimer, support and links. Remove duplicated release provenance, internal ports, implementation decisions, toolchain and speculative future behavior |
| `doc/README.md` | Document index only. Remove duplicate version/hash/latest-public-release state; point to `STATUS`, `RELEASE_GATES`, `CHANGELOG` and dated QA |
| `doc/SOURCE_OF_TRUTH.md` | Canonical thin-wrapper and ownership rules are coherent; no product behavior change |
| `doc/ARCHITECTURE.md` | Shipped 0.3.1 update check, guarded WebView spacing and optional email were wrongly titled "Development"; correct to released scope and distinguish untested negative cases |
| `doc/API_CONTRACTS.md` | Add **current** STR DOM/spacing integration assumption, explicitly stating no agreed upstream embedding contract; refer to STR-022 proposal, never claim a released STR API |
| `doc/SECURITY.md` | Correct obsolete GitHub Release link origin; mark 0.3.1 update logic as released; scope existing guarded DOM injection and residual risk. Keep trust boundary, single WLAN credential transfer and WebView restrictions here |
| `doc/PRIVACY.md` | Change update-check and optional email behavior from "planned" to released. Retain GitHub observer/network metadata and opt-in email disclosures in this single authority |
| `doc/BLOCKERS.md` | Remove inaccurate open signing/smoke-test gates from old PR #5; document actual non-blocking STR CSS, deferred negative QA and legacy PR-ref privacy follow-ups |
| `doc/ANDROID_COMPATIBILITY.md` | Correct accepted release reference 0.3.0 → 0.3.1 and avoid pinning the developer's particular JBR 25.0.3 as a required local JVM; major 25 remains normative |
| `doc/PLANNED.md` | Remove detailed duplication of completed BUILD-0013 implementation and release evidence; leave prioritized upcoming work and an **unadopted** STR-022 CSS-hook design proposal |
| `doc/BACKLOG.md` | Keep tracked work/state, clarify STR remote model coverage (STR-012) versus original Bose-firmware provisioning evidence (STR-026), and link STR-022 proposal |
| `doc/STATUS.md` | Preserve published v0.3.1 provenance at top, remove obsolete "pending" BUILD-0013 tail sections duplicated by dated `work/BUILD-0013.md` |
| `doc/DECISIONS.md` | Preserve chronological D-001…D-043 entries (including rejected choices); add introductory supersession map for D-037→D-038, D-041→D-042 and D-042→D-043 |
| `doc/FINDINGS.md` | Update outdated F-027…F-031 resolution states without erasing historical observation context; create F-032 for this documentation drift and link this review |
| `doc/GIT_HISTORY_PROVENANCE.md` | Distinguish five old rewritten release tags from **new sixth tag v0.3.1** created after the history rewrite |
| `doc/GOVERNANCE.md` and `doc/TESTS.md` | Converge documentation test policy to link checking of all *current normative* files and uniqueness of D-/F-IDs |
| `doc/RELEASE_GATES.md`, `doc/CHANGELOG.md` | Reviewed as canonical release acceptance and change history; public 0.3.1 acceptance remains unchanged; no decision text moved into root README |
| `doc/CONTRIBUTING.md`, `doc/CLEAN_CODE.md`, `doc/AI_CODING_GUARDRAILS.md` | Found in correct policy locations. No parallel policies introduced |
| `doc/GIT_HISTORY_CLEANUP.md`, `doc/RELEASE_0.3.0_HANDOFF.md` | Historic destructive rewrite runbook and former-release handoff, **not a new current-release procedure**. Retained with source/provenance cross-links |
| `doc/work/`, `doc/evidence/qa/`, `specs/`, `.specify/` | Classified as dated history/orchestration. Historical "pending" and superseded decisions are not current drift, and were not mass-edited |

## STR-022 — source verification and proposed stable interface

The current upstream [STR phone WebUI](https://github.com/JRpersonal/streborn/blob/main/internal/webui/assets/index.html) has `header { ... padding-top:calc(16px + env(safe-area-inset-top)) ... }`; it needs that browser/PWA safe-area padding, including on notched iPhones. The released Android [StrWebViewHeader](../../app/src/main/kotlin/app/strremote/android/StrWebViewHeader.kt) instead guards on `body > header`, `#dev`, `#powerBtn` and injects `padding-top:10px !important`, because Android has already handled native system insets.

A safer option, to **propose but not claim as implemented**, is an upstream-controlled custom property `--str-embed-header-padding-top` with the browser/PWA expression as its default, plus a documented version/capability contract. After upstream adoption, Android could set the property to 10px for the recognized embedded context and preserve the old guarded behavior only as a compatibility fallback. STR retains header/controls and native WebView never adds an Android JS bridge. Owner discussion and multi-platform/a11y regression checks are tracked in [STR-022](../../PLANNED.md#str-022--stable-str-webui-embedding-interface-proposal).

## Regression controls

- Existing CI verifies `app/build.gradle.kts` application version, `doc/STATUS.md` development/current-public release, root README current release, changelog heading, accepted release gates, and no inappropriate private RFC1918 host strings in current `doc/` Markdown.
- Extended `tools/verify-doc-consistency.ps1` checks relative Markdown links in public/root entry points and **current top-level normative `doc/*.md`** against source paths, and unique decision/finding IDs. Historical snapshot links are not silently rewritten.
- Verification planned: GitHub Actions `Check documentation/version/release consistency`, `git diff --check`, source-tree-only diff inspection, and existing standard Android CI. Final run IDs and status must be taken from GitHub; no local PowerShell execution or real-device QA is claimed for this docs-only change.
- The signed public 0.3.1 APK, annotated tag, original package ID, signer identity and existing accepted six-case phone QA are **not changed**. Negative cases remain **deferred / NOT TESTED** in [STR-027](https://github.com/K-Cosmo/remote-for-str-android/issues/7).

## Explicit remaining work

- STR maintainer to agree and ship an actual CSS embedding contract; **not** included in this docs-only PR.
- Actual listing on upstream Community Apps remains unconfirmed; its request is tracked in STR-025.
- Do not confuse previous Git-history privacy cleanup with a full purge of GitHub-managed old PR refs/caches.
- Avoid massive historical transcript rewrites or inventing missing per-device negative test evidence.
