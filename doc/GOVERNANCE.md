# Development governance

## Workflow

Normal feature work follows:

`Specification → Plan → Tasks → Analyze → Implement → Converge → Tests → Real run → Evidence → Acceptance`

Small maintenance/correctness/documentation changes may use:

`Smallest scoped change → Consistency/Tests → Evidence → Acceptance`

## Status model

`PLANNED → IMPLEMENTED → VERIFIED → ACCEPTED`

- **PLANNED:** agreed work, no code claim.
- **IMPLEMENTED:** code/document change exists.
- **VERIFIED:** required builds/tests/evidence passed.
- **ACCEPTED:** release/change gates satisfied and `/doc` converged.

A compiling change is not automatically verified. A plausible AI explanation is not evidence.

## Documentation authority

- `/doc` contains normative product, architecture, security, compatibility and quality truth.
- the public root `README.md` is a product overview, not a second normative specification;
- root helper files may point into `/doc` but must not duplicate normative content;
- `.specify/` and `specs/<feature>/` are development-process artifacts only;
- `doc/work/` and `doc/evidence/qa/` are dated/historical records.

Historical records preserve the state that existed when evidence/work was captured. Superseded wording inside such a historical record is not current-state drift. Current truth must be read from the normative `/doc` authorities.

## Convergence rule

If code/runtime evidence contradicts current normative documentation:

1. record the discrepancy in `FINDINGS.md`;
2. verify actual behavior;
3. update the relevant `/doc` authority and decision record;
4. rerun the applicable documentation/build/runtime gates;
5. only then mark the change VERIFIED/ACCEPTED.

## Duplication rule

There must be one canonical copy of each normative subject.

- changelog: `doc/CHANGELOG.md` only;
- privacy: `doc/PRIVACY.md` (root `PRIVACY.md` is only a pointer);
- contribution rules: `doc/CONTRIBUTING.md` (root `CONTRIBUTING.md` is only a pointer).

When a convenient public entry point is useful, prefer a short link/pointer over copied text.

## Automated documentation consistency gate

`tools/verify-doc-consistency.ps1` is the deterministic drift gate for facts that can be checked mechanically.

It verifies at minimum:

- application `versionName` matches `/doc/STATUS.md` `Development version`;
- public README `Current release` matches `/doc/STATUS.md` `Latest public release`;
- the latest public release has a corresponding `/doc/CHANGELOG.md` heading;
- an ACCEPTED release has no unchecked entries in `RELEASE_GATES.md`;
- an ACCEPTED release is no longer described as the current final-publication step in `PLANNED.md`;
- relative Markdown links resolve inside the repository.

The script remains a local documentation/release-maintenance gate. GitHub CI stays focused on proving the Android build. Human review is still required for semantic drift that cannot be checked mechanically.

## Public AI transparency

STR Remote is intentionally AI-assisted during development. Public project material must distinguish development assistance from runtime behavior: AI may help implement, analyze, debug, review and document the project, but acceptance remains evidence-based and human-directed. The shipped Android app has no AI/LLM runtime or AI-service data path unless a future explicit product decision changes that boundary.
