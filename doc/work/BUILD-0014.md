# BUILD-0014 — Launcher label and feedback address hygiene (0.3.2 candidate)

## Scope and rationale

- 2026-10-31: The Android launcher truncates the full **Remote for STR** product name to “Remote for ...”. Use **Remote STR** as the *device app label*; keep the full brand in the Devices title, project documentation and independent-community/trademark notice.
- The project email mailbox has changed. Avoid any complete recipient literal in public documents or Kotlin/XML source; the user still explicitly opens and sends mail via an external email app.
- Both About contact and SoundTouch WLAN-model feedback must use one central recipient implementation. Reversible XOR-coded characters reconstructed at runtime deter only elementary plaintext scanning; they **do not** hide the address from binary analysis. No MD5 (one-way hash), background email, SMTP backend or dependency is introduced.
- Prepare `versionName=0.3.2`, `versionCode=13` as a future signed in-place update. Preserve `app.strremote.android`, signing identity, preferences and the immutable published 0.3.1 APK/tag.

## Implementation

- The manifest's application label references the localized `launcher_name` resource. Debug remains visibly separate.
- The native Devices title uses the unchanged `app_name`. Both mail paths use `BoseCommunityFeedback.RECIPIENT`; the old native About literal was removed.
- JVM tests verify that the decoded recipient matches the new project mailbox without including its complete value in source, and preserve `mailto:` subject/body behavior.
- Historical public Markdown contact literals are redacted (without rewriting decision rationale); CI rejects complete mailbox literals across current Markdown and Android Kotlin/XML sources.
- The implementation commit itself did not publish an APK. **On 2026-10-31 the tester subsequently confirmed all six on-phone checks passed for a locally signed 0.3.2 candidate** from exact source commit `547fe4e52935bac03ddb0c3eb1f1ca4c7e517323`. No v0.3.2 GitHub Release or tag exists.

## Device acceptance — 2026-10-31

- [x] Exact-source CI for `547fe4e52935bac03ddb0c3eb1f1ca4c7e517323`: push run `41203798614` and PR run `41203849504`, both successful.
- [x] User confirmed six on-device PASS scenarios against the locally signed candidate: launcher **Remote STR**; full **Remote for STR** identity and community/brand disclosures; About contact destination; SoundTouch feedback destination and prepared details; in-place 0.3.1→0.3.2 update preserving speakers/rooms; STR remote functionality and restart persistence.
- [x] **Tester-supplied local signed APK SHA-256 (2026-10-31):** `19b0cf987351fde11ad76c9c61d5334784dbeb5466f98e5b5602ea91549e10ec` for `Remote-for-STR-0.3.2.apk`, source `547fe4e52935bac03ddb0c3eb1f1ca4c7e517323`. The digest was **reported by the user**, not independently computed from an uploaded binary.
- [x] **2026-10-31 owner-executed local verification:** The tester supplied successful console output from `Verify-RemoteSTR-0.3.2.ps1`, verifying the existing signed APK and its `.apk.sha256.txt` against digest `19b0cf987351fde11ad76c9c61d5334784dbeb5466f98e5b5602ea91549e10ec`, the historical signer certificate `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`, `apksigner verify`, and `zipalign -c -P 16 -v 4`. No APK bytes were supplied for a second, independent hash/signature analysis. No rebuilding or re-signing; preserve this exact signed artifact.
- [ ] After owner-authorized upload of exactly this APK and checksum, compare the GitHub asset digest; no public 0.3.2 asset exists yet.
- [x] First post-device-acceptance documentation-only CI passed at `cd310d5defd70813930fb31fa2b8f7ea6c290b34` ([push 41208464702](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41208464702), [PR 41208468955](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41208468955)).
- [x] Both CI runs passed for prior SHA-256 evidence documentation head `3c84d37a856190fd86ffdf41ce11b5bf543aa5a5`: [push](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41209899536), [PR](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41209902852).
- [ ] Fresh CI for the documentation-only **final local-verification evidence** head, followed by **explicit owner merge/release approval**. No tag, GitHub Release, re-signing or old-tag reassignment.

Dated source: [signed handset test evidence](../evidence/qa/2026-10-31-build-0014-0.3.2-device-qa.md). Documentation-only commits after the signing-time source SHA are **not** the source of the tested APK.
