# BUILD-0009 - 0.2.0 Speaker Management

## Goal

Turn the existing transient discovery list into a useful lightweight speaker-management view without expanding STR Remote beyond its wrapper role.

## Existing baseline

- mDNS can already return multiple speakers;
- the device screen already lets the user choose a discovered speaker;
- only the last successful endpoint is persisted today;
- a manual host/IP can be entered as a fallback;
- startup first probes the last endpoint, then falls back to discovery.

## Planned behavior

- retain the current last-endpoint auto-connect behavior;
- allow explicit saving/favoriting of successful speaker endpoints;
- keep saved speakers visible even when they are not currently discovered;
- merge saved and discovered representations of the same speaker;
- identify the currently connected speaker in the device view;
- allow quick switching by selecting another saved/discovered speaker;
- allow a successfully resolved manual host to be explicitly saved;
- keep unreachable saved speakers actionable rather than silently removing them;
- persist with Android platform storage only; no Room/database/dependency addition;
- preserve all 0.1.1 Wi-Fi/discovery/error/security behavior.

## Non-goals

- no playback/preset/business logic;
- no background service or continuous background discovery;
- no cloud account/sync;
- no automatic LAN inventory/history;
- no speculative migration to Compose/Room or another UI/persistence framework;
- no Gradle 9.8.x upgrade in the same build.

## State

**PLANNED.** Specification and implementation tasks are captured under `specs/009-speaker-management/`.
