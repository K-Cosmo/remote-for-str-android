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
- No production APK was signed or published as part of implementing this change.

## Remaining acceptance

- Final CI on exact source commit, including documentation and JVM unit tests.
- Real handset check for short launcher label, in-app full branding, both email destinations and app data persistence after upgrade.
- Clean original-certificate signed build, device acceptance, user merge/release approval, public tag/asset checks. No reassignment of historic tags.
