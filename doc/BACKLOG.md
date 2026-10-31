# Backlog

| ID | Item | Priority | State |
|---|---|---:|---|
| STR-001 | Verify debug build + lint on real Android toolchain | P0 | ACCEPTED in 0.1.0 |
| STR-002 | Real-device STR discovery + WebView smoke test | P0 | ACCEPTED in 0.1.0 |
| STR-008 | Converge status-bar/discovery/WebView layout from device screenshots | P0 | ACCEPTED in 0.1.0 |
| STR-009 | Modern/legacy back navigation behavior | P1 | ACCEPTED in 0.1.0 |
| STR-003 | Generate and commit trusted Gradle Wrapper 9.7.1 | P0 | ACCEPTED in 0.1.0 |
| STR-007 | Capture real Gradle JVM and Daemon JVM criteria | P0 | ACCEPTED in 0.1.0 |
| STR-004 | GitHub release signing/publication pipeline | P0 | ACCEPTED in 0.1.0 |
| STR-010 | Verify restart reconnect/back/permission/control smoke tests | P0 | ACCEPTED in 0.1.0 |
| STR-011 | Public GitHub README/repository presentation | P1 | ACCEPTED; simplified post-release |
| STR-013 | Discovery/network resilience: no-Wi-Fi, timeout, no-device/STR-unreachable guidance, retry | P0 | ACCEPTED in 0.1.1 |
| STR-005 | Persistent speaker picker / saved speakers / favorites / quick switching / room assignment | P1 | ACCEPTED in 0.2.0 |
| STR-015 | Compact remote banner + saved-speaker online/offline highlighting | P1 | ACCEPTED in 0.2.1 |
| STR-016 | Bose SoundTouch WLAN-Einrichtung (Android 10+), reference model ST20 III | P1 | ACCEPTED in 0.3.0; ST10 separate test WLAN confirmed afterwards |
| STR-014 | Evaluate Gradle 9.8.x maintenance upgrade | P2 | DEFERRED — eligible post-0.2.0; schedule separately |
| STR-012 | Additional SoundTouch model compatibility evidence | P2 | BACKLOG |
| STR-006 | Evaluate STR authentication changes when upstream provides them | P2 | BACKLOG |

## Next development and community integration (2026-10-31)

The signed public release `v0.3.0` stays unchanged. `IMPLEMENTED` refers to code in draft PR #6. The **six specified smartphone checks for the signed 0.3.1 candidate passed** on 2026-10-31. Owner approved deferral of untested edge cases and normal merge subject to final CI; public release is separate. Untested cases are not certified as working. Refer to [Planned](PLANNED.md) and [BUILD-0013](work/BUILD-0013.md).

| ID | Item | Priority | State |
|---|---|---:|---|
| STR-017 | BUILD-0013: GitHub update checker + compact STR WebView header | P1 | 0.3.1 signed candidate phone QA PASS; live future-version update alert not yet observable; upstream CSS contract pending |
| STR-018 | Three-model wizard, offline suggestions, optional feedback, DE/EN strings | P1 | English language, dropdown, compact feedback, signed candidate wizard PASS on phone; exhaustive negative cases still untested |
| STR-019 | Email composer: voluntary user-triggered feedback, prefill recipient/subject/body | P0 | Signed candidate phone QA PASS with installed mail client; no-client failure path untested; text spacing cosmetic/deferred |
| STR-020 | Product branding **Remote for STR**; preserve package and original signing identity | P0 | **Signed 0.3.1 phone QA PASS**, including 0.3.0 in-place upgrade and saved devices; public release not yet issued |
| STR-021 | Bilingual native independent-community Info/About notice next to Devices title | P0 | **Signed 0.3.1 phone QA PASS** for visible title/About placement and absence on Remote; exhaustive accessibility/negative cases not claimed |
| STR-022 | Coordinate robust upstream STR WebUI safe-area/header styling API instead of brittle `body > header`, `#dev`, `#powerBtn` injection | P1 | PLANNED — upstream offered to discuss a stable hook; PR #6 compatibility risk |
| STR-023 | Federated community support: own STR upstream Show-and-tell thread; link to project's GitHub Discussions/Issues + optional email feedback (no GitHub account needed) | P1 | PLANNED |
| STR-024 | Sanitize historical LAN information in Git refs, remove stale branches | P0 | **NORMAL REFS DONE:** 2 branches/5 tags rewritten, six stale branches deleted. **RESIDUAL:** GitHub-managed old PR refs and caches |
| STR-025 | Request listing on the upstream STR Community Apps page after name, About notice and EN wizard QA are complete | P1 | PLANNED — depends on STR-020/021/018 acceptance |
| STR-026 | Extend hardware evidence for additional original-Bose-firmware SoundTouch models; crowd reports remain unverified | P2 | BACKLOG |

| STR-027 | Follow-up QA after 0.3.1: absent email app, denied Wi-Fi permission, failure/timeout recovery, unverified Bose/router/firmware combinations and future-version update alert | P1 | **OWNER-APPROVED DEFERRED from 0.3.1 on 2026-10-31; NOT TESTED**. Track explicitly and avoid general compatibility claims |

Do not conflate optional user-reported Wi-Fi feedback with certified compatibility. Do not rename the Android `applicationId`, reset app data, alter published release tags or add automatic email/network telemetry.
