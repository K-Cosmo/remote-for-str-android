# Project documentation

`/doc` is the single normative source of truth for Remote for STR. Root-level files may provide public or tooling entry points, but they must not duplicate normative project rules.

## Release status and verification

- **Current public release, signed artifact and signing-time source:** [Status](STATUS.md)
- **Release acceptance and deferred-case disposition:** [Release gates](RELEASE_GATES.md)
- **Release notes / implemented changes:** [Changelog](CHANGELOG.md)
- **Dated publication and handset proof:** [QA evidence](evidence/qa/README.md)

Avoid repeating exact version numbers, hashes or release gate results in this index.

## Current normative documents

- `SOURCE_OF_TRUTH.md` — product purpose, architecture boundaries and documentation authority
- `STATUS.md` — current verified project/release state
- `ARCHITECTURE.md` — current component, persistence and data-flow design
- `ANDROID_COMPATIBILITY.md` — Android/AGP/Gradle/JDK baseline
- `API_CONTRACTS.md` — STR discovery, identity and endpoint integration contracts
- `SECURITY.md` / `PRIVACY.md` — security and privacy boundaries
- `TESTS.md` / `RELEASE_GATES.md` — current verification and acceptance rules
- `GOVERNANCE.md` — Spec → Plan → Tasks → Implement → Converge → Evidence → Acceptance
- `AI_CODING_GUARDRAILS.md` — rules for AI-assisted work and evidence-based acceptance
- `CLEAN_CODE.md` — code-quality rules
- `CONTRIBUTING.md` — contribution workflow
- `DECISIONS.md` — accepted design/policy decisions
- `FINDINGS.md` / `BLOCKERS.md` — findings and current blockers
- `PLANNED.md` / `BACKLOG.md` — future work
- `CHANGELOG.md` — canonical implemented-change history
- `GIT_HISTORY_CLEANUP.md` — historical/destructive Git rewrite runbook (completed ordinary refs; residual GitHub PR refs)
- `GIT_HISTORY_PROVENANCE.md` — source/tag provenance after the history rewrite and residual PR-reference exposure

## Historical records

The following locations are evidence/process history, not current-state policy:

- `work/` — dated build-scoped implementation and execution records
- `evidence/qa/` — dated QA, build, release and documentation-review evidence
- `/specs/` — historical/active development specifications, plans and tasks
- `/.specify/` — process orchestration only

Historical records intentionally preserve the state and wording that existed when the work was performed. They can therefore contain superseded version numbers, pending gates or old implementation descriptions without creating documentation drift. Current truth always comes from the normative documents listed above.

## Public entry points

- `README.md` is the public product overview.
- `AGENTS.md` and `.specify/memory/constitution.md` are non-normative entry points into `/doc`.
- root `CONTRIBUTING.md` and `PRIVACY.md` are thin pointers to the canonical `/doc` files.
- there is intentionally **no root `CHANGELOG.md`**; `doc/CHANGELOG.md` is the only changelog.

Development-process artifacts must reference and obey `/doc`; they are never a second policy system.
