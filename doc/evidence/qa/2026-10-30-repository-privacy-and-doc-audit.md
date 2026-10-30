# Repository privacy, Git history and documentation audit — 2026-10-30

**Repository:** `K-Cosmo/remote-for-str-android`  
**Scope:** current PR branch `feature/update-checker-compact-str-header`, current `main`, older published tags, branch inventory and tracked `/doc` files.  
**Status:** inventory and current-document convergence performed; historical object purge / obsolete branch deletion **NOT yet executed**.

## Privacy findings

- Reviewed the present 51 tracked `doc/` text documents and 99 other tracked nonbinary/text files for apparent IP/MAC/credential assignments, embedded private keys, email addresses and local user paths. Pattern screening is heuristic, not a substitute for full credential scanning or historical blob inspection.
- Two dated 2026-09-26 real-device QA reports contained one actual RFC1918 LAN endpoint. Their **current PR-branch content has been redacted**, replaced with reserved `192.0.2.29` documentation addresses and explanatory notes. The exact original endpoint is intentionally **not** repeated in this audit.
- The same pre-redaction blob is still accessible from `main` and previous public release tags. Git history is **not clean**. Merely deleting unused branches cannot erase these objects.
- Additional RFC1918 addresses in historical Bose SoftAP test notes are documented candidate setup endpoints, not evidence of a user's household LAN identifier. Code/unit-test addresses and the fixed project support mailbox serve a functional purpose. Do **not** blindly delete all RFC1918 literals or replace protocol endpoints during a history rewrite.
- No apparent committed passwords, private key blocks, token assignments, MAC addresses or private home directory paths were detected by this scoped **current-text** screening. This is not a claim that every historical object or binary is free of sensitive information. Embedded icon artwork was not exhaustively inspected for privacy issues.

## Git refs inventory

- Eight branches: keep `main` and `feature/update-checker-compact-str-header` (PR #5) until release acceptance. Six other branches are old/merged-PR workstreams and eligible for review/deletion after backup.
- Five release tags (`v0.1.0`, `v0.1.1`, `v0.2.0`, `v0.2.1`, `v0.3.0`) point into original history and cannot all remain cryptographically identical if all their reachable Git objects are rewritten.
- Public APK assets have their own signed bytes and SHA-256 checksums; a Git history rewrite does **not** have to modify APK bytes, but it invalidates older source commit and annotated-tag object IDs and historical release attestations. Pull-request diffs, GitHub caches and clones may retain objects separately.
- Some old branches are **diverged**, not simply fast-forward-merged. Deleting them can erase unique development commits. Back up all refs and confirm no desired work is lost.

## Documentation quality

- `doc/STATUS.md` is the authoritative release state; `doc/README.md`, `doc/BLOCKERS.md` and CI policy in `doc/GOVERNANCE.md` had stale 0.2.0/local-only descriptions. Fixed on the active PR branch. Historical `work/` and `evidence/qa/` records intentionally retain old release terminology.
- Root `PRIVACY.md` / `CONTRIBUTING.md` are **pointers**, not duplicated policy files. The changelog lives only in `doc/CHANGELOG.md`. `PLANNED`, `BACKLOG`, `STATUS`, `RELEASE_GATES` and BUILD-0013 each describe different aspects; repeated status sentences should be kept short and defer to the canonical authority instead of maintaining competing release decisions.
- `doc/work/BUILD-0012.md` is unusually large because it preserves development chronology; it is archival evidence, **not** a second current specification. Keep rather than risk losing early protocol/test context.

## Next action: destructive operations still outstanding

The connected GitHub integration supports editing, creating Git objects and moving branch refs, **but does not expose deletion of branch refs or a full mirror fetch/push/history rewrite**, and the execution environment cannot reach GitHub over Git transport. Therefore **branch deletion and complete historical purging were not claimed as performed**.

Use the checked-in runbook `doc/GIT_HISTORY_CLEANUP.md` from a machine with Git, `git-filter-repo` and authenticated push access. It requires a protected offline mirror backup; a narrowly scoped replacement (never all private IPs); coordinated tag and PR updates; verifying no leaked blob is reachable; and explicit action to clear GitHub cached PR views where necessary. **Do not merge/publish while the rewrite is partly applied.**
