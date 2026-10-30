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

## Next development and community integration (2026-10-30)

The signed public release `v0.3.0` stays unchanged. `IMPLEMENTED` refers to the open development PR, **not** an accepted hardware test or a public release. Refer to [Planned](PLANNED.md) and [BUILD-0013](work/BUILD-0013.md).

| ID | Item | Priority | State |
|---|---|---:|---|
| STR-017 | BUILD-0013: foreground GitHub release checker + compact STR WebView header | P1 | IMPLEMENTED in PR #6; final QA, upstream CSS contract pending |
| STR-018 | Three-model wizard, offline suggestions, optional feedback, complete German/English strings | P1 | **English WLAN wizard phone QA PASS (2026-10-30)**; model dropdown and feedback layout device tested. Negative-case/regression release QA outstanding |
| STR-019 | E-mail compose interoperability: prefill recipient **and** subject/body across mail clients; no automatic sending | P0 | VERIFIED on tested client; extra paragraph styling not preserved on user's device — cosmetic, DEFERRED; no-mail-app path QA pending |
| STR-020 | Public product name **Remote for STR**; UI, docs and future artifacts; preserve Android package ID and original signer | P0 | IMPLEMENTED in PR #6; GitHub repo renamed to `K-Cosmo/remote-for-str-android` and canonical URLs migrated; final CI + device/release QA pending |
| STR-021 | Visible bilingual Info/About: independent unofficial community app, no Bose/STR affiliation, trademark notice and README | P0 | User verified About screen opens; UI adjusted so circled info sits next to title on Devices only; updated placement and EN phone QA pending |
| STR-022 | Coordinate robust upstream STR WebUI safe-area/header styling API instead of brittle `body > header`, `#dev`, `#powerBtn` injection | P1 | PLANNED — upstream offered to discuss a stable hook; PR #6 compatibility risk |
| STR-023 | Federated community support: own STR upstream Show-and-tell thread; link to project's GitHub Discussions/Issues + optional email feedback (no GitHub account needed) | P1 | PLANNED |
| STR-024 | Sanitize historical LAN information in Git refs, remove stale branches | P0 | **NORMAL REFS DONE:** 2 branches/5 tags rewritten, six stale branches deleted. **RESIDUAL:** GitHub-managed old PR refs and caches |
| STR-025 | Request listing on the upstream STR Community Apps page after name, About notice and EN wizard QA are complete | P1 | PLANNED — depends on STR-020/021/018 acceptance |
| STR-026 | Extend hardware evidence for additional original-Bose-firmware SoundTouch models; crowd reports remain unverified | P2 | BACKLOG |

Do not conflate optional user-reported Wi-Fi feedback with certified compatibility. Do not rename the Android `applicationId`, reset app data, alter published release tags or add automatic email/network telemetry.
