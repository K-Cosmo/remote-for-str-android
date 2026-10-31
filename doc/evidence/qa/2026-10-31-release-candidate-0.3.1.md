# Remote for STR 0.3.1 — signed release-candidate evidence (2026-10-31)

## Scope and exact identity

- **Release state:** local signed APK produced and user-accepted on phone; **NOT published**, merged or tagged.
- **Signing-time Git source commit:** `0e6bab9d9e1bcac19b542755b7a1774c69d2776a` on the clean-history candidate branch.
- **Android app ID:** `app.strremote.android` (unchanged), `versionName=0.3.1`, `versionCode=12`.
- **Locally built signed artifact:** `dist/Remote-for-STR-0.3.1.apk`.
- **User-reported SHA-256:** `8100334f067346c9987a68969bdebd9065455bfe73c21a0634f0dba42ddbd759`.
- **Local checksum file:** `dist/Remote-for-STR-0.3.1.apk.sha256.txt`.
- **Release signing certificate expected by script:** SHA-256 `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`, the historic production app signer. Successful script completion means this invariant, signature verification and post-sign 16 KiB zipalign passed; raw signed APK/certificate inspection has not been performed by the GitHub connector.
- **GitHub source CI:** [push 41146044403](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41146044403), [PR 41146049240](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41146049240), both successful for the exact source commit.

## Signed-APK handset acceptance — explicit user report

On 2026-10-31, the tester explicitly confirmed **all six** listed smartphone release-candidate checks passed for this APK:

1. Installed as an upgrade over signed 0.3.0 without uninstalling; existing saved speakers and room assignments remained intact.
2. Android launcher and Devices show **Remote for STR**; circled info action appears next to the Devices title and not in the Remote screen.
3. STR connection, playback, volume and presets work.
4. SoundTouch setup model choice, DE/EN wizard copy and compact screen presentation work.
5. The user's installed email client opens a volunteer feedback message with the intended recipient, subject and model/app/Android data.
6. App restart preserves saved speakers, room assignments and the last connection.

Evidence scope: **tester-confirmed checks**, not automatically captured device logs or generalized compatibility for every SoundTouch firmware/router/Android version. Existing 0.3.0 signed release and data were not modified by GitHub operations.

## Deliberately not claimed as tested

- No-email-client installed: an exception fallback is implemented in source, but its Android presentation on a device without a handler was not part of the six QA checks.
- Permission denial and network-failure/timeout paths in the refreshed wizard, and setup behavior on untested Bose models.
- Update checker with a **genuinely newer public stable release**: at acceptance time latest GitHub Release remains v0.3.0, so the forward notification cannot be end-to-end tested against a later release. Deterministic release-policy/asset-name checks are covered in unit tests; the app does not silently install APKs.
- Upstream STR WebUI CSS selector stability is not a guaranteed API; coordinate a more robust safe-area hook as separate STR-022.
- Java/Kotlin lint warnings are not claimed to have all been eliminated; CI and the local final build completed without blocking lint errors.

These are scoped residual risks, **not test PASS entries**. **2026-10-31 owner decision:** explicitly defer the untested special/negative cases for 0.3.1 (no mail client, denied WLAN permission, connectivity failures, unverified hardware) and retain a tracked follow-up in STR-027; the future-positive update-checker flow also remains unobserved until a newer genuine public release exists. This permits a scoped release-candidate signoff, **not** a claim of passing these scenarios.

## Publication handoff — preserve the exact signed bytes

1. Confirm the local APK and checksum-file content match `8100334f067346c9987a68969bdebd9065455bfe73c21a0634f0dba42ddbd759`; never sign a new variant under the same published version without repeating acceptance.
2. Review the remaining negative-case deferrals; keep developer-facing details in [release gates](../../RELEASE_GATES.md).
3. After documentation-only reconciliation commit(s), check GitHub PR #6 CI; **the owner explicitly approved a normal merge on 2026-10-31, but public release was not separately authorised**. If merging, use a **normal merge commit** (no squash or rebase), preserving the tested source commit in main ancestry.
4. Create immutable annotated tag `v0.3.1` pointing to the **exact tested signed-source commit** `0e6bab9d9e1bcac19b542755b7a1774c69d2776a` (not the later docs-only head or merge commit), and verify the tag target.
5. Publish GitHub Release `v0.3.1` titled **Remote for STR 0.3.1**, upload **exactly** `Remote-for-STR-0.3.1.apk` and its `Remote-for-STR-0.3.1.apk.sha256.txt`; verify GitHub's returned APK SHA-256 matches `8100334f067346c9987a68969bdebd9065455bfe73c21a0634f0dba42ddbd759` before announcing the release.
6. Only after verified publication update root README Current release, `doc/STATUS.md` Latest public release, `doc/CHANGELOG.md` release heading and final gate state. The public release's tag source must remain pinned to the signing-time commit. GitHub-managed old PR refs remain a separate privacy follow-up.

### Proposed public release notes (not published)

**Remote for STR 0.3.1** is the first release under the new Android community app name, replacing the previous public name *STR Remote*. Existing Android installations update in place: package ID and signing identity are unchanged, and the tested upgrade preserved saved speakers and rooms.

Highlights: streamlined STR remote header, bilingual independent-community About screen, German/English SoundTouch WLAN assistant with model selection and voluntary user-triggered email feedback, and an optional GitHub Releases update notification. No account, analytics, automatic feedback or automatic APK installation is introduced.

Compatibility: Android 8+ for the remote; Android 10+ for SoundTouch WLAN onboarding. Original Bose firmware/model support is limited to the explicitly documented hardware tests. The app remains unofficial and independent from Bose and the upstream STR project.

## Owner decision — scoped deferral and merge authorization (2026-10-31)

The project owner explicitly agreed to defer non-tested edge cases such as absent mail handler and denied setup-WLAN permission for 0.3.1, and authorized **CI and normal merge** of PR #6. This decision is limited to development integration; it does **not** attest edge-case test success and does **not** authorize a public v0.3.1 GitHub Release or the tagging/upload of private signed assets. Final documentation-only commit CI must pass first. Track the deferred cases and future update-checker positive path in STR-027. The accepted signing-time source remains `0e6bab9d9e1bcac19b542755b7a1774c69d2776a` and the signed APK digest remains `8100334f067346c9987a68969bdebd9065455bfe73c21a0634f0dba42ddbd759`.
