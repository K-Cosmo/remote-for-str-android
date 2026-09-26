# AI coding guardrails

These rules are mandatory for AI-assisted development.

## Core rule

Small changes, real tests, real debug data, clear failure descriptions, no blind magic.

## Required behavior

- Analyze the existing implementation before changing it.
- Prefer small, isolated patches over broad rewrites.
- Extend existing structures instead of creating parallel logic or a shadow architecture.
- Reality beats assumption: builds, logs, runtime behavior and reproducible tests outrank AI explanations.
- One goal per iteration where practical.
- No new dependency without documented benefit exceeding maintenance/supply-chain cost.
- No silent architecture, state-management, concurrency or API-contract change.
- Keep user behavior simple; avoid options and hidden special paths without clear value.
- Do not call a change correct merely because it compiles.

## Code-quality guardrails

Avoid unnecessary abstraction layers, one-off generic helpers, duplicate models, copy/paste variants, clever one-liners and speculative optimization. Prefer readable, localizable and debuggable code.

## Risk areas

Treat caching, concurrency, error handling, state management, retry logic, performance optimization and large refactors as high-risk. Measure/observe first.

## AI self-review is not acceptance

AI-generated code and AI review can help find issues but cannot establish correctness. Verification requires the evidence defined in `TESTS.md` and `RELEASE_GATES.md`.
