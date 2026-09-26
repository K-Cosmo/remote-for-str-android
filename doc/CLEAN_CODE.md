# Clean Code concept

Clean Code in STR Remote means simple ownership, small responsibilities and code that is easy to locate and debug — not maximum abstraction.

## Rules

- One clear responsibility per class/function where it materially improves readability.
- Keep framework entry points thin; orchestration belongs there, domain/network details remain in focused classes.
- Keep internal implementation types `internal` unless a real public API exists.
- Prefer descriptive names over comments that explain unclear code.
- Replace repeated magic values with a single meaningful source only when repetition actually exists.
- Keep methods short enough that control flow is obvious; extract only cohesive logic.
- No interface solely to wrap one implementation without a testing/architecture need.
- No premature repository/use-case/view-model layers for this small wrapper.
- Do not suppress build/lint warnings instead of fixing the cause unless the suppression is documented and justified.
- Preserve security boundaries when simplifying WebView/network code.

## Current architecture fit

`MainActivity` orchestrates Android lifecycle/UI, `StrDiscovery` owns DNS-SD discovery, `EndpointProbe` owns reachability probing, `PreferencesStore` owns local persistence, and `SpeakerModels` owns the small internal data model.

## Refactoring threshold

Real evidence still favors the existing small platform-only architecture. BUILD-0002 therefore improves cohesive UI/layout methods in place and does not introduce Compose, AndroidX architecture layers, a second UI framework, or new abstractions solely to reduce line count. Extract a new component only when a verified responsibility can stand on its own and the extraction reduces rather than redistributes complexity.
