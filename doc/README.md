# Project documentation

`/doc` is the single normative source of truth for STR Remote. Root-level files may provide public or tooling entry points, but they must not duplicate normative project rules.

## Current release

- public release: **0.2.0**
- status: **RELEASED — ACCEPTED**
- release status and artifact identity: `STATUS.md`
- accepted release gates: `RELEASE_GATES.md`
- implemented release history: `CHANGELOG.md`

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

## Historical records

The following locations are evidence/process history, not current-state policy:

- `work/` — build-scoped execution records
- `evidence/qa/` — dated QA/build/release evidence
- `/specs/` — development specifications/plans/tasks for completed or active work
- `/.specify/` — process orchestration only

Historical records intentionally preserve the state and wording that existed when the work was performed. They can therefore contain superseded version numbers, pending gates or old implementation descriptions without creating documentation drift. Current truth always comes from the normative documents listed above.

## Root-level Markdown policy

- `README.md` is the public product overview.
- `AGENTS.md` and `.specify/memory/constitution.md` are non-normative entry points into `/doc`.
- root `CONTRIBUTING.md` and `PRIVACY.md` are thin pointers to the canonical `/doc` files.
- there is intentionally **no root `CHANGELOG.md`**; `doc/CHANGELOG.md` is the only changelog.

Development-process artifacts must reference and obey `/doc`; they are never a second policy system.
