# 009 — Speaker Management

## Problem

STR Remote can already discover and select multiple STR speakers, but the device list is transient. Only the last successful endpoint is persisted. Users with more than one speaker, or a speaker that is temporarily not announced by mDNS, need a lightweight list of intentionally saved devices and a human-friendly room assignment.

## Required behavior

- startup keeps the current last-successful-endpoint fast path;
- the device view can show explicitly saved speakers independently of the current mDNS scan;
- successful discovered/manual endpoints are saved implicitly when the user assigns a room and can later be removed through a visible delete icon with confirmation;
- the same physical/logical speaker must not appear twice merely because it is both saved and currently discovered;
- if both sides expose a discovery identifier, that identifier is authoritative; host is the fallback when one side has no discovery identifier;
- the current speaker is visibly identifiable;
- device rows remain directly tappable for connect/switch even when they contain Room/Delete controls;
- with an active speaker, the device view exposes an explicit `Fernbedienung` action to return to the remote;
- the device name and room are the primary row information; IP is shown beneath in smaller text;
- the remote screen shows the selected device/room as a clearly readable, larger text label without reintroducing a full-width status banner;
- the current device in the device list is indicated without an extra leading bullet before the title; card highlighting is sufficient.
- the highlighted device-management help text can be explicitly dismissed; the preference persists locally and must not affect speaker/discovery state.
- selecting another saved/discovered reachable speaker switches the WebView to that endpoint and updates the last-successful endpoint;
- an unreachable saved speaker stays in the saved list and reports an actionable unreachable state;
- each saved speaker can optionally be assigned to a room;
- the primary visible per-row management action is room assignment/change; applying it implicitly saves the speaker;
- saved speakers expose a visible delete icon; deletion requires explicit confirmation and removes the row from the current list until a fresh scan may rediscover it;
- built-in room choices are Wohnzimmer, Schlafzimmer, Küche, Bad, Kinderzimmer and Garten, localized for the active app language;
- built-in rooms persist language-neutral stable IDs so changing app language does not mutate the assignment;
- the user can choose a custom room and enter an arbitrary local room label;
- room metadata must not affect speaker identity, endpoint probing, playback logic or WebView navigation/security;
- saving/removing speakers must not change the existing Wi-Fi precondition, finite discovery timeout or STR-help behavior;
- persistence uses Android platform facilities already available to the app; no database/dependency is introduced;
- existing WebView host/port restrictions remain unchanged.

## Acceptance

The applicable BUILD-0009 cases in `doc/TESTS.md` pass with a clean build/lint/release assembly and focused real-device evidence. A real second online speaker is useful additional evidence but is not required to validate persistence, room metadata, de-duplication, switching behavior and unreachable handling.
