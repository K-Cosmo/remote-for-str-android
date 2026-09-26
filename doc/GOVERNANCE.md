# Development governance

## Workflow

Normal feature work follows:

`Specification → Plan → Tasks → Analyze → Implement → Converge → Tests → Real run → Evidence → Acceptance`

Small maintenance/correctness changes may use:

`Specification → smallest implementation → Tests/Evidence → Acceptance`

## Status model

`PLANNED → IMPLEMENTED → VERIFIED → ACCEPTED`

- **PLANNED:** agreed work, no code claim.
- **IMPLEMENTED:** code/document change exists.
- **VERIFIED:** required builds/tests/evidence passed.
- **ACCEPTED:** release gates satisfied and `/doc` converged.

A compiling change is not automatically verified. A plausible AI explanation is not evidence.

## Spec Kit boundary

- `/doc` contains normative product, architecture, security, compatibility and quality truth.
- `.specify/` and `specs/<feature>/` contain development-process artifacts only.
- A spec may propose a normative change, but the change is not accepted until the relevant `/doc` file and decision record are updated.
- Never create a second policy/specification world beside `/doc`.

## Convergence rule

If code/runtime evidence contradicts documentation, record the discrepancy in `FINDINGS.md`; verify the actual behavior; update the appropriate `/doc` authority; then rerun the relevant gates.

## Public AI transparency

STR Remote is intentionally AI-assisted during development. Public project material must distinguish development assistance from runtime behavior: AI may help implement, analyze, debug, review and document the project, but acceptance remains evidence-based and human-directed. The shipped Android app has no AI/LLM runtime or AI-service data path unless a future explicit product decision changes that boundary.
