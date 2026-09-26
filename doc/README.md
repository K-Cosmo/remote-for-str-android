# Project documentation

`/doc` is the single normative source of truth for STR Remote. Root-level files may point here but must not duplicate normative project rules.

## Start here

- `SOURCE_OF_TRUTH.md` — product purpose, architecture and immutable boundaries
- `STATUS.md` — current verified state
- `GOVERNANCE.md` — Spec → Plan → Tasks → Implement → Converge → Evidence → Acceptance
- `AI_CODING_GUARDRAILS.md` — mandatory rules for AI-assisted work and evidence-based acceptance
- `CLEAN_CODE.md` — code-quality rules
- `ARCHITECTURE.md` — component and data-flow design
- `ANDROID_COMPATIBILITY.md` — Android/AGP/Gradle/JDK baseline
- `API_CONTRACTS.md` — STR discovery and endpoint contracts
- `SECURITY.md` / `PRIVACY.md` — security and privacy boundaries
- `TESTS.md` / `RELEASE_GATES.md` — verification and acceptance
- `FINDINGS.md`, `BLOCKERS.md`, `DECISIONS.md` — evidence, blockers and decisions
- `PLANNED.md`, `BACKLOG.md` — future work
- `CHANGELOG.md` — implemented changes only
- `work/` — build-scoped execution records
- `evidence/qa/` — real QA evidence and run outputs

Development-process artifacts under `specs/` and `.specify/` are not a second policy system. They must reference and obey `/doc`.
