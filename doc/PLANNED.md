# Planned

## Released baseline — Remote for STR 0.3.1

BUILD-0013 shipped as **Remote for STR `0.3.1`** on 2026-10-31, following accepted signed phone QA, a normal merge and public GitHub APK+checksum verification. BUILD-0012 Bose setup was the prior STR Remote `0.3.0` baseline. WLAN setup requires Android 10+; STR remote control works from Android 8.

**Additional hardware evidence (2026-10-30):** SoundTouch 10 original-firmware Wi-Fi provisioning was successfully tested with a separate test WLAN profile, independently of its existing STR remote functionality. More firmware and WLAN profiles remain unverified. The historic published v0.3.0 APK retains its original "Testbetrieb" label; the current 0.3.1 app has scoped verified-model labels.

**Released — BUILD-0013:** foreground GitHub Releases update check, compact STR WebView spacing, public rebranding and bilingual SoundTouch wizard. **Next:** track explicit negative-case/future release-update validation in [STR-027](https://github.com/K-Cosmo/remote-for-str-android/issues/7), upstream CSS contract in STR-022, and Community Apps listing in STR-025. Do not alter historical signed APKs.

Sources: [BUILD-0013](work/BUILD-0013.md), [0.3.1 Release](https://github.com/K-Cosmo/remote-for-str-android/releases/tag/v0.3.1), [accepted gates](RELEASE_GATES.md), [STR-027](https://github.com/K-Cosmo/remote-for-str-android/issues/7).

## Completed: BUILD-0013 release scope (merged PR #6)

These changes are **released in 0.3.1**, merged into `main` via PR #6. The six defined on-device QA cases passed. On 2026-10-31, the owner explicitly **deferred untested negative cases** and approved the normal merge `ed7652783a9752e107449a572c5b6f2dff624922`; final CI passed. The public signed APK matches the accepted SHA-256. Deferred cases are **NOT TESTED**, not general hardware or failure-path certification.

1. **P0 — Email draft interoperability (STR-019):** **device-tested PASS** on 2026-10-30 for recipient, subject and body in the selected Android mail client (previously subject/body were blank). The RFC 6068 `mailto:` subject/body plus standard Intent extras remain in place. A subsequent **plain-text paragraph formatting enhancement** did **not** display as intended in the user's selected mail client; treat it as a non-blocking cosmetic limitation and do not add HTML, account integrations or special-case hacks without further evidence. The unavailable-mail-app case is still pending; no send without user confirmation.
2. **P1 — Wizard DE/EN and compact UI (STR-018):** **device-tested PASS** (2026-10-30) for readable model suggestions with “30” and “SoundTouch”, and the compact non-scrolling feedback page. **PASS (user confirmed 2026-10-30):** English-language WLAN wizard walkthrough on a physical Android device. This verifies the English wizard as a user-accepted language experience, **not** every possible permission-denied, transport-failure or no-email-client negative path; optional freeform entry and unexercised edge paths remain subject to release QA. Hardware-verified ST10 and ST20 III display “geprüft”; the generic “other” model is experimental, never auto-certified.
3. **P1 — GitHub update alert and STR header (STR-017):** verify official stable GitHub APK release only, 24-hour throttling, no debug-app alert, correct external browser and no implicit installer. Validate header spacing and all STR WebUI controls; assess the upstream CSS selector dependency described below.
4. **Candidate acceptance (2026-10-31):** signed 0.3.1 / versionCode 12 exists **locally**, SHA-256 `8100334f067346c9987a68969bdebd9065455bfe73c21a0634f0dba42ddbd759`, built from `0e6bab9d9e1bcac19b542755b7a1774c69d2776a`. Tester confirmed all six acceptance checks, including in-place upgrade over 0.3.0, data preservation and playback. **Published GitHub `v0.3.1` and both signed assets now verified** (SHA-256 unchanged). Negative-path and future-version update checks remain **owner-approved deferred** under STR-027, with no implied PASS; upstream CSS compatibility remains an open follow-up. See [evidence](evidence/qa/2026-10-31-release-candidate-0.3.1.md).

## Upstream STR Community Apps listing — maintainer response of 2026-10-30

The STR maintainer reviewed the independent Android app and considers its local-only v0.3.0 design suitable for the Community Apps page. The previously broken footer link is repaired. Existing entries include the **STR Remote iOS** app by Wagges, **SoundFork** on Android and **SoundRebornRemote** on Android; this makes the Android title “STR Remote” ambiguous. A listing is conditional on **all three** items below, not merely code compilation:

1. **STR-020 / P0 — New app name:** The user explicitly selected **Remote for STR** on 2026-10-30, replacing the Android public name **STR Remote** (which conflicted with the existing iOS app). **Released in v0.3.1 via PR #6.** DE/EN launcher/app title, in-app permission/update/setup copy, community email version label, README and future signed APK convention use the new brand. The original `v0.3.0` release and signed `STR-Remote-0.3.0.apk` remain historical facts. The release script will generate `Remote-for-STR-<version>.apk`, and the release checker recognizes both new and legacy names. Keep `applicationId=app.strremote.android` and signing identity to preserve users' saved data and update path. **Repository rename COMPLETE:** user moved the repo to `K-Cosmo/remote-for-str-android`, verified by GitHub; update-checker/API, release URLs, About source/issues, tests and README have been changed to this existing canonical repository. Do not use the older proposed `remote-for-str` repository slug.
2. **STR-021 / P0 — About/Info and legal clarity:** **released in v0.3.1; tested in the six-case signed APK phone acceptance on 2026-10-31.** User verified the information dialog exists. Following their UI request, the button was moved to a circled-info touch target **immediately beside the app title on the Devices view only**, hidden in the Remote/WebView and Remote-offline views; updated placement was accepted as part of the signed-candidate phone review. The button opens a DE/EN information dialog identifying the Android product as an **unofficial, independent community app**, not affiliated with or endorsed by Bose Corporation or STR; **Bose** and **SoundTouch** are Bose Corporation trademarks. It shows the installed version and offers GitHub source, project-specific Issues and optional email contact without login. README contains the matching legal wording. **User accepted launcher/About placement and the DE/EN wizard in the signed-candidate device review; exhaustive accessibility and unsupported-language/negative-case verification is not claimed.** No upstream endorsement is implied.
3. **STR-018 / P0 listing gate — English WLAN setup:** the full safety-sensitive wizard must be understandable in English, including consent, warnings and results, or be hidden on unsupported languages. v0.3.1 includes full DE/EN resources; **English WLAN wizard device QA was explicitly confirmed by the user on 2026-10-30 (PASS)**. This closes the English-language listing requirement; the release was subsequently published with owner-approved deferred negative cases.

Further maintainer observations:

- **STR-024 / P0:** ordinary history rewrite (two branches/five tags) and six obsolete-branch deletions completed. GitHub-managed old PR refs and cached old SHA pages remain a potential exposure; GitHub Support may decline cleanup for a private LAN address.
- **STR-022 / P1:** the PR #6 injected WebView CSS depends on upstream `body > header`, `#dev` and `#powerBtn`, which can change at any time. Share the exact 10-px safe-area requirement with the STR maintainer and ask for a supported, durable header spacing hook. Until clarified, treat this as a compatibility risk, **not** a guaranteed upstream contract.
- **STR-023 / P1:** open a dedicated upstream [Show and tell discussion](https://github.com/JRpersonal/streborn/discussions), similar to the [existing iOS community thread](https://github.com/JRpersonal/streborn/discussions/1233). This acts as a public directory/feedback rendezvous. Feature requests and reproducible bugs live in the Android project's own GitHub Issues/Discussions; non-GitHub users can voluntarily submit wizard feedback by mail (`str-remote-android@gmx.org`). Clearly route responsibility and link the paths from the About page/README. No new backend, account requirement or automatic telemetry.
- **STR-025 / P1:** once the three listing requirements are accepted, provide the maintainer the final app name, repository/releases URL and community thread and request the actual listing; do not describe inclusion as already granted.

**Brand cutover accepted for the tested scope:** launcher text, Devices title `Remote for STR ⓘ`, hidden info action on Remote, DE/EN wizard, email version label, exact signed APK naming, original app ID/signature and update-over-v0.3.0 preservation were accepted on device. Canonical URLs are in the published 0.3.1 source; a genuinely newer-release update alert remains deferred. Avoid altering historical release evidence or re-signing old releases.

**STR-024 remaining privacy follow-up:** ordinary branch/tag history rewrite and branch retirement are completed; GitHub-managed old PR refs and caches remain separate. See [provenance](GIT_HISTORY_PROVENANCE.md).

The separate proposed **Play Store/F-Droid** distributions remain later candidates. This upstream listing and community workflow does not require them.

## Deferred maintenance — BUILD-0008

Gradle 9.8.x evaluation remains deferred. The accepted build baseline stays Gradle 9.7.1 + AGP 9.4.1 until a dedicated maintenance build proves the upgrade.

## Later candidates

- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.

### Released BUILD-0013 — model coverage through voluntary community feedback

The released 0.3.1 wizard offers a third **Anderes SoundTouch-Modell** option beside the two hardware-checked SoundTouch 10/20 III options. Results and early setup failures can be reported via an explicitly requested email draft to `str-remote-android@gmx.org`. The draft includes model, selected success/failure and Remote for STR/Android versions; the maintainer must review reports before extending the checked model list. The offline Bose model suggestion list is not a verified Wi-Fi compatibility matrix. The optional feedback feature is in v0.3.1; the older v0.3.0 APK remains unchanged.
