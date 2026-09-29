# 009 — Speaker Management

## Problem

STR Remote can already discover and select multiple STR speakers, but the device list is transient. Only the last successful endpoint is persisted. Users with more than one speaker, or a speaker that is temporarily not announced by mDNS, have no lightweight list of intentionally saved devices.

## Required behavior

- startup keeps the current last-successful-endpoint fast path;
- the device view can show explicitly saved speakers independently of the current mDNS scan;
- successful discovered/manual endpoints can be saved and later removed by an explicit user action;
- the same physical/logical speaker must not appear twice merely because it is both saved and currently discovered;
- the current speaker is visibly identifiable;
- selecting another saved/discovered reachable speaker switches the WebView to that endpoint and updates the last-successful endpoint;
- an unreachable saved speaker stays in the saved list and reports an actionable unreachable state;
- saving/removing speakers must not change the existing Wi-Fi precondition, finite discovery timeout or STR-help behavior;
- persistence uses Android platform facilities already available to the app; no database/dependency is introduced;
- existing WebView host/port restrictions remain unchanged.

## Acceptance

The applicable BUILD-0009 cases pass with a clean build/lint/release assembly and focused real-device evidence. A real second online speaker is useful additional evidence but is not required to validate persistence, de-duplication, switching behavior and unreachable handling.
