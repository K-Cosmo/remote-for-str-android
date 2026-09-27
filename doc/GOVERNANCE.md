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

## Automated documentation consistency gate

`tools/verify-doc-consistency.ps1` is the deterministic drift gate for facts that can be checked mechanically.

It verifies at minimum:

- application `versionName` matches `/doc/STATUS.md` `Development version`;
- public README `Current release` matches `/doc/STATUS.md` `Latest public release`;
- the latest public release has a corresponding `/doc/CHANGELOG.md` heading;
- an ACCEPTED release has no unchecked entries in `RELEASE_GATES.md`;
- an ACCEPTED release is no longer described as the current final-publication step in `PLANNED.md`;
- relative Markdown links resolve to files/directories that exist in the repository.

The script is a local documentation/release-maintenance gate and is not part of GitHub Actions. GitHub CI remains focused on proving that the Android application can be built. The documentation gate is intentionally narrow: it catches objective drift but does not replace human review of wording, architecture, security or product meaning.

## Public AI transparency

STR Remote is intentionally AI-assisted during development. Public project material must distinguish development assistance from runtime behavior: AI may help implement, analyze, debug, review and document the project, but acceptance remains evidence-based and human-directed. The shipped Android app has no AI/LLM runtime or AI-service data path unless a future explicit product decision changes that boundary.
