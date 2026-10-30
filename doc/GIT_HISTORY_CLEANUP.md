# Git history clean-up — deliberate, destructive maintenance

**State: prepared only; NOT executed on GitHub.** This procedure changes historic commit hashes and tag objects. Do not confuse editing current files with complete removal of information from GitHub.

**Repository:** https://github.com/K-Cosmo/remote-for-str-android

## Motivation and scope

One real private LAN host address appeared in two early Android QA Markdown files. The current development branch uses a reserved documentation placeholder instead. The older value remains reachable from main and published release tags. Only the **exact historic host address** should be replaced across all Git text objects. Do not rewrite all private IP literals indiscriminately: Bose setup endpoints, test vectors, and the SSDP multicast group are part of application logic.

The current audit did not identify an obvious committed password, token, MAC address or private key in present tracked text; that does NOT prove every historical blob or fork is clean.

## Git references — inventory before migration

- **Keep:** main; the active feature/update-checker-compact-str-header branch while PR #5 is being resolved.
- **Five release tag names:** v0.1.0, v0.1.1, v0.2.0, v0.2.1, v0.3.0. Their tagged commit SHAs and annotated tag object IDs will change after a full history rewrite. Cryptographic commit/tag signatures, if present, may be removed.
- **Six branches to retire after offline backup:** feature/0.2.1-ux, feature/build-0012-bose-wifi-provisioning, fix/release-030-apksigner-37-cert-format, fix/release-030-apksigner-stderr, fix/release-030-clean-user-text, release/v0.3.0-prep. Some have unique diverged commits, so deletion must not precede a full backup.
- **Published GitHub Releases:** Keep all APK and SHA-256 assets byte-for-byte. Rewriting Git commit history does not itself change the signed APK, but it breaks historic source commit/tag attestations and can invalidate release provenance links.

## Phase A — freeze and offline preparation (safe: no remote changes)

**Prerequisites:** Git, PowerShell 5.1+ and git-filter-repo supporting the sensitive-data-removal option. An authenticated GitHub account is needed only later for pushing.

**Before starting:** GitHub advises merging or closing open pull requests ahead of a full rewrite. PR #5 is OPEN and holds the unreleased 0.3.1 changes. Do NOT force-push an in-progress rewrite while it is still under normal review. Either finish the outstanding signed-upgrade/merge acceptance first, or explicitly coordinate closing and recreating PR #5 after the clean refs are published. Freeze writes and notify anyone with local clones.

~~~powershell
git filter-repo --version

# Execute from your local remote-for-str-android repository root.
# Use a new directory outside the Git working tree.
.\tools\prepare-history-rewrite.ps1 -Workspace "D:\remote-for-str-cleanup"
~~~

The script requests the historical private IPv4 interactively; obtain it only from a local read of an old ref, e.g. the original 2026-09-26 device QA reports. Do NOT put the value in the Git repository, an Issue, this runbook, CI, PR comments, or a screenshot.

The script creates two fresh full mirrors: ORIGINAL-full-mirror.git (sensitive offline backup) and CLEANED-preview.git (rewritten history). It also records an original ref snapshot, uses a temporary replacement file outside Git, prepares deletion of the six obsolete branches in the cleaned mirror, checks that the old address is absent from remaining branch/tag tip trees and commit diff search, and deletes the temporary mapping.

**It does not push or delete any remote references.** The offline backup itself contains the old information. Keep it encrypted and away from public storage.

## Phase B — review the rewritten history

1. Confirm main and the PR feature branch both exist with expected file trees, all five tag names are present, and the six stale branches exist only in the offline backup.
2. Compare the rewritten app against the accepted 0.3.0 functionality. Run Android compilation, lint, unit tests and documentation checks where feasible.
3. Independently scan all reachable Git objects for other private endpoints, passwords, keys, real SSIDs, serials and local user paths. The exact-string script check alone is insufficient for a complete repository-security assertion.
4. Preserve a local mapping of the old and new release tag/commit IDs and reconcile public release provenance. Public signed APK hashes must stay unchanged.
5. Determine whether refs/pull/*, closed pull-request diffs, forks and caches still contain original objects. These may not be controllable through Git push.
6. Confirm nobody has pushed new work since the initial mirror cloning. Do not silently discard a new contribution.

## Phase C — manual publication (destructive, requires coordination)

The following is NOT run automatically by the script. These commands are for a session *inside* CLEANED-preview.git, AFTER PR/CI/tag preparation and review. GitHub branch protections or rulesets may reject force pushes.

~~~powershell
# Change directory to the rewritten bare mirror.
cd "D:\remote-for-str-cleanup\CLEANED-preview.git"

git remote -v
# If origin was removed by git-filter-repo:
git remote add origin https://github.com/K-Cosmo/remote-for-str-android.git

git for-each-ref --format="%(refname) %(objectname)" refs/heads refs/tags

# WARNING: replaces ALL public branch histories and prunes obsolete branches.
git push --force --prune origin "refs/heads/*:refs/heads/*"

# WARNING: rewrites all five historical release tag refs.
git push --force --prune origin "refs/tags/*:refs/tags/*"
~~~

Do not push refs/pull/* blindly: GitHub pull-request refs are managed/read-only. Do not assume that git push --mirror is atomic or succeeds for every GitHub-managed ref. Stop if any forced update is rejected, reconcile the partial state and check tags, branches, Releases and PRs. The user-authorized deletion of obsolete branches must still be verified against GitHub, not just the cleaned mirror.

## Phase D — cleanup confirmation and collaboration

- Verify the original value is not reachable from current GitHub branch and tag tips, and that main/feature CI is green after rewriting.
- Inspect previous commit IDs and GitHub PR diff/cache views. GitHub may still retain old PR objects or cache pages. [GitHub's official sensitive-data removal guide](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository) describes contacting GitHub Support; GitHub can decline requests for low-sensitivity information such as a private RFC1918 address.
- Ensure collaborators use fresh clones, NOT git pull/merge against rewritten history. A stale merge can reintroduce the original data. Discuss other forks if they exist.
- Do not claim global erasure from GitHub, forks, third-party clones, caches or archives unless independently verified.
- Document new release-tag/source lineage while retaining the unchanged original signed APK/checksum identities. Do not rewrite release historical statements as though original source IDs had never existed.

## Why this must be coordinated

GitHub warns that rewriting history changes all downstream commit hashes, can invalidate signed Git commits/tags, breaks PR diff views and can leave hidden references intact. An ordinary private LAN address is low-sensitivity operational information compared to an exposed credential, but the cleanup can still be performed if its consequences are accepted and coordinated. An open PR, previously verified release tags and published checksum-bearing APKs make a push without review inappropriate.

**Completion definition:** all six remote obsolete branches deleted; rewritten main and active feature/release history verified; tags and signed APKs accounted for; relevant pull-request/other-clone/cache limitations recorded; final CI and device testing remain valid. Until then STR-024 stays PARTIAL.
