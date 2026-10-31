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
| STR-012 | Additional **STR remote/control compatibility** evidence for other SoundTouch hardware | P2 | BACKLOG; distinct from original Bose-firmware WLAN provisioning (STR-026) |
| STR-006 | Evaluate STR authentication changes when upstream provides them | P2 | BACKLOG |

## Post-0.3.1 development and community integration (2026-10-31)

The historical public release `v0.3.0` remains unchanged. **Remote for STR `v0.3.1` is now the latest published signed release**, with exactly matching APK digest and six device checks passed. PR #6 was merged normally with green CI. The owner explicitly deferred untested negative cases to STR-027: they are **not certified**. Refer to [Planned](PLANNED.md) and [BUILD-0013](work/BUILD-0013.md).

| ID | Item | Priority | State |
|---|---|---:|---|
| STR-017 | BUILD-0013: GitHub update checker + compact STR WebView header | P1 | **RELEASED in 0.3.1**; positive newer-version alert and upstream CSS contract remain deferred/follow-up |
| STR-018 | Three-model wizard, offline suggestions, optional feedback, DE/EN strings | P1 | **RELEASED / phone QA PASS in 0.3.1**; deferred negative paths STR-027 |
| STR-019 | Email composer: voluntary user-triggered feedback, prefill recipient/subject/body | P0 | **RELEASED / phone QA PASS in 0.3.1** with mail client; no-client path not tested, deferred STR-027 |
| STR-020 | Product branding **Remote for STR**; preserve package and original signing identity | P0 | **RELEASED / ACCEPTED in 0.3.1**, upgrade over 0.3.0 preserved speakers/rooms |
| STR-021 | Bilingual native independent-community Info/About notice next to Devices title | P0 | **RELEASED / phone QA PASS in 0.3.1**; extended accessibility/negative paths not certified |
| STR-022 | Propose documented upstream STR WebUI embedding CSS hook instead of private `body > header`, `#dev`, `#powerBtn` dependency | P1 | OPEN — [proposal](PLANNED.md#str-022--stable-str-webui-embedding-interface-proposal); current released workaround is not an upstream API |
| STR-023 | Federated community support: own STR upstream Show-and-tell thread; link to project's GitHub Discussions/Issues + optional email feedback (no GitHub account needed) | P1 | PLANNED |
| STR-024 | Sanitize historical LAN information in Git refs, remove stale branches | P0 | **NORMAL REFS DONE:** 2 branches/5 tags rewritten, six stale branches deleted. **RESIDUAL:** GitHub-managed old PR refs and caches |
| STR-025 | Request listing on upstream STR Community Apps page after brand, About notice, EN wizard QA | P1 | **READY TO REQUEST**; naming/Info/EN release requirements met in v0.3.1. Actual upstream listing not yet confirmed |
| STR-026 | Extend **original Bose-firmware WLAN provisioning** evidence for untested SoundTouch models; crowd reports remain unverified | P2 | BACKLOG; distinct from STR remote/control compatibility (STR-012) |
| STR-027 | [Follow-up QA after 0.3.1](https://github.com/K-Cosmo/remote-for-str-android/issues/7): absent email app, denied Wi-Fi permission, failure/timeout recovery, unverified Bose/router/firmware combinations and future-version update alert | P1 | **OWNER-APPROVED DEFERRED from 0.3.1 on 2026-10-31; NOT TESTED**. Track explicitly and avoid general compatibility claims |
| STR-028 | Next Android app display label **Remote STR**, centralized runtime-decoded feedback recipient and CI mailbox-harvesting guard | P1 | **IMPLEMENTED on 0.3.2 development branch; final CI + signed upgrade/phone QA pending** |

Do not conflate optional user-reported Wi-Fi feedback with certified compatibility. Do not rename the Android `applicationId`, reset app data, alter published release tags or add automatic email/network telemetry.
