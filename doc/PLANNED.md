# Planned

## Released baseline — STR Remote 0.3.0

BUILD-0012 Bose Wi-Fi setup was released and accepted on 2026-10-30 as STR Remote `0.3.0`. It is now part of the production Android app, no longer an unreleased PoC. The WLAN wizard requires Android 10+; the existing STR remote works from Android 8.

**Additional hardware evidence (2026-10-30):** SoundTouch 10 original-firmware Wi-Fi provisioning was successfully tested with a separate test WLAN profile, independently of its existing STR remote functionality. More firmware and WLAN profiles remain unverified. The published v0.3.0 APK retains its release-time "Testbetrieb" label; the development branch revises the label to the scoped test status.

**Current work — BUILD-0013:** add a foreground GitHub Releases update notification and remove redundant browser/PWA safe-area padding from the embedded STR WebView. Do not alter the accepted 0.3.0 tag or signed APK; test UI and update-check behavior before the next release.

Sources: [BUILD-0012](work/BUILD-0012.md), [v0.3.0 release](https://github.com/K-Cosmo/remote-for-str-android/releases/tag/v0.3.0), [accepted gates](RELEASE_GATES.md).

## Active: BUILD-0013 quality gates (PR #5)

These changes are **implemented on the development branch, not released or accepted**.

1. **P0 — Email draft interoperability (STR-019):** **device-tested PASS** on 2026-10-30 for recipient, subject and body in the selected Android mail client (previously subject/body were blank). The RFC 6068 `mailto:` subject/body plus standard Intent extras remain in place. A subsequent **plain-text paragraph formatting enhancement** did **not** display as intended in the user's selected mail client; treat it as a non-blocking cosmetic limitation and do not add HTML, account integrations or special-case hacks without further evidence. The unavailable-mail-app case is still pending; no send without user confirmation.
2. **P1 — Wizard DE/EN and compact UI (STR-018):** **device-tested PASS** (2026-10-30) for readable model suggestions with “30” and “SoundTouch”, and the compact non-scrolling feedback page. **PASS (user confirmed 2026-10-30):** English-language WLAN wizard walkthrough on a physical Android device. This verifies the English wizard as a user-accepted language experience, **not** every possible permission-denied, transport-failure or no-email-client negative path; optional freeform entry and unexercised edge paths remain subject to release QA. Hardware-verified ST10 and ST20 III display “geprüft”; the generic “other” model is experimental, never auto-certified.
3. **P1 — GitHub update alert and STR header (STR-017):** verify official stable GitHub APK release only, 24-hour throttling, no debug-app alert, correct external browser and no implicit installer. Validate header spacing and all STR WebUI controls; assess the upstream CSS selector dependency described below.
4. **Candidate staging:** 0.3.1 / versionCode 12 is prepared in the development branch. **No signed APK or public release yet.** Final CI, real-phone rebrand/regression checks and the original-certificate signed update-over-0.3.0 test must pass before PR merge or release. Preserve the `v0.3.0` tag, signing certificate, user app data and production package identity.

## Upstream STR Community Apps listing — maintainer response of 2026-10-30

The STR maintainer reviewed the independent Android app and considers its local-only v0.3.0 design suitable for the Community Apps page. The previously broken footer link is repaired. Existing entries include the **STR Remote iOS** app by Wagges, **SoundFork** on Android and **SoundRebornRemote** on Android; this makes the Android title “STR Remote” ambiguous. A listing is conditional on **all three** items below, not merely code compilation:

1. **STR-020 / P0 — New app name:** The user explicitly selected **Remote for STR** on 2026-10-30, replacing the Android public name **STR Remote** (which conflicted with the existing iOS app). **Implemented on PR #5; no release yet.** DE/EN launcher/app title, in-app permission/update/setup copy, community email version label, README and future signed APK convention use the new brand. The original `v0.3.0` release and signed `STR-Remote-0.3.0.apk` remain historical facts. The release script will generate `Remote-for-STR-<version>.apk`, and the release checker recognizes both new and legacy names. Keep `applicationId=app.strremote.android` and signing identity to preserve users' saved data and update path. **Repository rename COMPLETE:** user moved the repo to `K-Cosmo/remote-for-str-android`, verified by GitHub; update-checker/API, release URLs, About source/issues, tests and README have been changed to this existing canonical repository. Do not use the older proposed `remote-for-str` repository slug.
2. **STR-021 / P0 — About/Info and legal clarity:** **implemented in PR #5, device QA open.** User verified the information dialog exists. Following their UI request, the button was moved to a circled-info touch target **immediately beside the app title on the Devices view only**, hidden in the Remote/WebView and Remote-offline views; updated placement still requires device verification. The button opens a DE/EN information dialog identifying the Android product as an **unofficial, independent community app**, not affiliated with or endorsed by Bose Corporation or STR; **Bose** and **SoundTouch** are Bose Corporation trademarks. It shows the installed version and offers GitHub source, project-specific Issues and optional email contact without login. README contains the matching legal wording. **Do not mark accepted until phone navigation/accessibility and both languages pass.** No upstream endorsement is implied.
3. **STR-018 / P0 listing gate — English WLAN setup:** the full safety-sensitive wizard must be understandable in English, including consent, warnings and results, or be hidden on unsupported languages. PR #5 implements full DE/EN resources; **English WLAN wizard device QA was explicitly confirmed by the user on 2026-10-30 (PASS)**. This closes the English-language listing requirement; other release gates remain open.

Further maintainer observations:

- **STR-024 / P0:** the affected files in the current PR branch are sanitized. **The same address is still reachable through `main` and legacy release tags**. A controlled full Git-ref history rewrite and deletion of obsolete branches is required for the requested privacy cleanup; back up first, protect PR #5, preserve the signed APK assets and account for the loss of historic commit/tag SHAs. See the dated audit and its locally executable runbook. Editing HEAD or deleting old branches alone is insufficient.
- **STR-022 / P1:** the PR #5 injected WebView CSS depends on upstream `body > header`, `#dev` and `#powerBtn`, which can change at any time. Share the exact 10-px safe-area requirement with the STR maintainer and ask for a supported, durable header spacing hook. Until clarified, treat this as a compatibility risk, **not** a guaranteed upstream contract.
- **STR-023 / P1:** open a dedicated upstream [Show and tell discussion](https://github.com/JRpersonal/streborn/discussions), similar to the [existing iOS community thread](https://github.com/JRpersonal/streborn/discussions/1233). This acts as a public directory/feedback rendezvous. Feature requests and reproducible bugs live in the Android project's own GitHub Issues/Discussions; non-GitHub users can voluntarily submit wizard feedback by mail (`str-remote-android@gmx.org`). Clearly route responsibility and link the paths from the About page/README. No new backend, account requirement or automatic telemetry.
- **STR-025 / P1:** once the three listing requirements are accepted, provide the maintainer the final app name, repository/releases URL and community thread and request the actual listing; do not describe inclusion as already granted.

**Brand cutover acceptance:** verify launcher text, Devices title `Remote for STR ⓘ`, hidden info action on Remote, DE/EN permissions/setup/update dialogs, email version label, exact signed APK naming, original app ID/signature, and update-over-v0.3.0 behavior. Repository rename and direct-URL code cutover are implemented on the branch; confirm the final release checker and links in CI/QA. Avoid changing historical release evidence or re-signing old releases.

**STR-024 evidence privacy:** current branch redacts the actual private LAN endpoint in both affected 2026-09-26 QA Markdown files using reserved RFC 5737 example addresses. Git commit history still contains previous content; no force-push/history rewrite was done. Decide separately whether an invasive history purge is warranted before public promotion.

The separate proposed **Play Store/F-Droid** distributions remain later candidates. This upstream listing and community workflow does not require them.

## Deferred maintenance — BUILD-0008

Gradle 9.8.x evaluation remains deferred. The accepted build baseline stays Gradle 9.7.1 + AGP 9.4.1 until a dedicated maintenance build proves the upgrade.

## Later candidates

- BUILD-0010: distribution improvements such as Play/F-Droid evaluation;
- add compatibility evidence from additional STR-supported SoundTouch models;
- evaluate upstream STR authentication changes only when they become available;
- track the non-blocking WebView destruction/lifecycle warning separately;
- do not reimplement STR playback/business logic natively without an explicit architecture decision.

### BUILD-0013 — model coverage through voluntary community feedback

The next development wizard offers a third **Anderes SoundTouch-Modell** option beside the two hardware-checked SoundTouch 10/20 III options. Results and early setup failures can be reported via an explicitly requested email draft to `str-remote-android@gmx.org`. The draft includes model, selected success/failure and STR Remote/Android versions; the maintainer must review reports before extending the checked model list. The offline Bose model suggestion list is not a verified Wi-Fi compatibility matrix. No feedback feature is present in the already published v0.3.0 APK.
