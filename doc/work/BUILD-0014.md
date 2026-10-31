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
- [ ] Independently record local APK SHA-256/checksum text and original-certificate/signature/16 KiB alignment log for the exact signed artifact before publication. Those bytes/logs were not supplied for this documentation update. Do not manufacture a digest.
- [ ] Fresh CI for acceptance-documentation commit and explicit owner merge/release approval. No tag, GitHub Release, re-signing or old-tag reassignment.

Dated source: [signed handset test evidence](../evidence/qa/2026-10-31-build-0014-0.3.2-device-qa.md). Documentation-only commits after the signing-time source SHA are **not** the source of the tested APK.
