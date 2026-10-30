# Git history rewrite — release provenance after cutover

This reports the **user-verified regular branch/tag rewrite**, not global deletion of old GitHub PR objects.

The original offline mirror was preserved. The user verified matching branch-tip trees, `git fsck --full --strict`, 285 rewritten reachable commit trees containing no targeted old private IP, and five original signed APK/checksum pairs. Normal GitHub refs were then atomically rewritten: `main` and `feature/update-checker-compact-str-header`, plus the five annotated release tags; six old branches were deleted. GitHub independently confirmed this outcome.

Rewritten `main` was `951e38130fa523c3fe6e3dbac3eb1628e91b2db5`, and feature was `2d1e2d7bda545652027702c4ac9e5082658e2585` immediately after cutover (follow-up documentation commits on feature supersede that head). Both push-triggered Android CI runs passed: [main](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41144950249), [feature](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41144950285). Original PR #5 closed without merge; [draft PR #6](https://github.com/K-Cosmo/remote-for-str-android/pull/6) tracks clean history.

## Current rewritten release tags

| Tag | Rewritten tag-target commit | Rewritten annotated tag object | Existing APK SHA-256 |
|---|---|---|---|
| `v0.1.0` | `ec7d99c9a3d41589692f7b9f83e86f40dd787eae` | `2c69743b298263f17070be8c52b2732db2e60d68` | `5f48f99f604b7d861f305f4fda0b6383188ded6e45dc660d65d66ea68da8a2fe` |
| `v0.1.1` | `0c1acb8221da65900a9127a43c32656d387c2089` | `564ea86ce874fa55c18f2fab49114fcdae83292e` | `ecc8f8a78c0b65be5a74a44546275aa9d5688b54b2eacf577f3bbfd20b87a1e8` |
| `v0.2.0` | `945bb9d7cfaa9f737fd6fb8d24780ada571249f0` | `7296c8afb38ad6f02cab036bc65f1af438998fd7` | `5e7ba21f39752ae359a6de0103a440b363a1823223874778e44173f285243aa7` |
| `v0.2.1` | `8afa46d25e5ec2be10bf84c07df071eb555b0ed2` | `f3b020247bb195ee1c9c9d326c8590aa75743ff6` | `bf019801b4a47226ab1af616f4f74f1dd7abfb71731205253e1d9709af76de67` |
| `v0.3.0` | `edc64cd5468e60edff355ec1dd1fd73c338004af` | `2e0c29414f71ae711b410b8bfe0b1e370b0aa14b` | `c4b1989e00ba21915e34192b2f4e0b73695416c93397e4f0c4d3cc6617a18290` |

All five GitHub Releases still contain the original signed `STR-Remote-<version>.apk` and SHA-256 text files; ten assets in total. The GitHub reported APK digests equal the recorded historical values. Their APKs were **not rebuilt or re-signed**: the rewritten tag-target commits do **not** represent the original signing-time source commit hashes or a newly signed build attestation. Keep the original IDs/mapping only in the encrypted offline mirror.

## Residual exposure and safety

GitHub still reports `refs/pull/1/head` through `refs/pull/5/head` pointing to old commit objects. As documented by [GitHub Support](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository), these read-only refs and cached views may require a Support request. GitHub only helps with information it regards as qualifying sensitive data, so a private RFC1918 address may not qualify. Other people’s old clones and caches are outside our control; repository metadata reported no forks at inspection time.

Use a **fresh clone** for further development. Never merge/push the pre-rewrite worktree or original mirror back into clean Git history. Published 0.3.0 APK and original Android signer identity remain unchanged. Candidate 0.3.1 requires final on-phone acceptance and signed in-place upgrade test before any merge or release.
