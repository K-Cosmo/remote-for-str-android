# BUILD-0013 — GitHub update notification and compact STR WebView header

Status: **feature branch implemented; device UI acceptance pending**. Public release remains `v0.3.0`. Do not tag or publish a new version until the required tests pass and the existing signing key is reused.

## User-reported needs (2026-10-30)

1. STR Remote needs an in-app notification when a newer version is published, comparable to the existing Abfahrtsradar implementation.
2. On the main remote screen, the screenshot highlights an unused vertical region *between the native STR Remote title and the STR WebUI header buttons*. Remove the redundant top padding only; do **not** remove the speaker switcher, power or accessibility controls.
3. Bose SoundTouch 10 original-firmware WLAN setup was successfully hardware-tested against the user's **separate test WLAN profile**. Retain the limitation that other profiles and firmware versions are not independently tested.
4. Both tested models should show a concise **„geprüft“** label. Provide a third selection **„Anderes SoundTouch-Modell“** running the same four steps, followed by optional community feedback asking for the exact model and whether WLAN provisioning succeeded.

## Source investigation

- Prior STR Remote 0.3.0 had no GitHub release fetch, installed/current version comparison or in-app update dialog.
- The sibling `K-Cosmo/abfahrtsradar-android` has `GitHubReleaseApi`, `UpdateViewModel`, `UpdateReleasePolicy` and a Compose alert; it fetches GitHub Releases `/latest` and links to the official release for installation.
- STR Remote is a small Java/Kotlin native Activity **without** Retrofit, Compose or Hilt. Follow the proven behavior, but implement in a bounded `HttpsURLConnection` and a native `AlertDialog` with no additional app runtime dependencies.
- Upstream STR WebUI `JRpersonal/streborn`, `internal/webui/assets/index.html`: the sticky `body > header` has `padding-top: calc(16px + env(safe-area-inset-top))` for browsers/PWAs. STR Remote already applies Android system-bar insets to its own outer layout, creating redundant top spacing in the embedded WebView.

## Update-checker contract

- Only `GET https://api.github.com/repos/K-Cosmo/remote-for-str-android/releases/latest`, fixed official public endpoint; no authentication token, no analytics endpoint.
- HTTP I/O off the UI thread; 3.5-second connect/read timeouts and 64 KiB bounded JSON. On errors/network absence, continue normal STR workflow without an error pop-up.
- Parse `vMAJOR.MINOR.PATCH` as a stable semantic version, numeric comparison to installed `PackageInfo.versionName`. No prereleases or malformed/untrusted tags, and only announce releases with a matching `STR-Remote-VERSION.apk` asset.
- Limit successful checks to once per 24 h; if GitHub cannot be checked, allow retry on a later app start. No background service, notifications permission or polling while the app is closed.
- When newer: native language-specific `AlertDialog`: version, **Release öffnen** and **Später**. Open only a URL synthesized from the approved GitHub host and validated tag, externally via Android browser. **No silent APK download/install**.
- Skip the separately packaged `.poc` debug app, since a signed production APK cannot update that package. Test version policy and release parsing using JVM tests.

## Compact upstream header: safety and compatibility

- After a successful, allowed speaker WebView main-frame load, inject a static CSS `<style>` once per DOM.
- Apply `body > header { padding-top: 10px !important; }` **only when the DOM contains both** `#dev` and `#powerBtn`. Android already handles OS bars. On unknown upstream DOMs or errors, do nothing.
- Preserve the STR header, sticky behavior, speaker switcher, power button and accessibility (`Aa`), and all playback/preset elements. No JavaScript interface, host injection or changes to STR firmware.
- Regression concern: STR upstream UI revisions may change header structure and safe-area handling; require screenshot QA, both standard dark and accessibility text size if possible.

### On-device spacing feedback (2026-10-30)

The tester explicitly accepted the revised **10 CSS px spacing** in a new ST20 screenshot: “Abstand ist jetzt super!”. This visual gate for the header gap is complete; normal STR controls still need final functional smoke validation.

The first WebView revision with `padding-top: 0` did remove the large empty strip, as verified by the tester's ST20 screenshot. However, the STR control row visually touched/overlapped the Android native toolbar edge; button shadows were visible across the boundary. Restore **10 CSS px** of separation at the known STR header, not the original browser/PWA `16px + safe-area` padding. Do not alter the native toolbar, STR button sizing or sticky/header controls. Screenshot QA is still required on this revision.

## SoundTouch 10 validation

- Reported device-level success: original-firmware WLAN setup on SoundTouch 10 with a separate test profile (2026-10-30). Update model selector to **mit Test-WLAN geprüft**, with a limited-coverage information line.
- Do not rewrite the historical v0.3.0 release/tag or claim that all Bose firmware/WLAN combinations are validated.
- Record the user-reported result in a standalone QA note; previous ST10 STR remote testing was a different scope.

## Community validation workflow (unknown models)

- Keep SoundTouch 20 III and 10 marked **„geprüft“** in the dropdown. The ST10 validation applies to its successful separate test WLAN profile, not every router/firmware combination. The existing screenshot spacing is accepted.
- Offer **„Anderes SoundTouch-Modell“** as an opt-in experimental choice. Reuse the existing four-step Bose Wi-Fi transport and the one-shot profile POST; do not add auto-retry or new API calls. Clearly warn that the usual Preset 2 + Volume − sequence is not yet verified for an unknown model.
- On a terminal result, show a separate optional **Community-Feedback** screen accessible via a button, not an extra step in the four-step setup. Also expose a feedback path when entering Bose setup mode or connecting to its setup WLAN is impossible, so unsuccessful attempts are reportable.
- Ask users **only** for their model label (2–60 safe characters) and explicit **Ja, erfolgreich** / **Nein, nicht erfolgreich** selection, with neither preselected nor inferred from the HTTP response. Avoid optional network identifiers or freeform diagnostic data.
- On explicit button click, open a **new email draft** through Android `ACTION_SENDTO` / `mailto:` to `str-remote-android@gmx.org`. The external mail app receives the model, manually chosen result, installed STR Remote version and Android version (including API level); the user reviews and explicitly sends. No GitHub account, server integration, silent mail or analytics.
- No SSID, password, IP/MAC, Bose device ID, firmware data, technical traces or attachment is included. An unavailable mail client produces a visible and recoverable message.
- The local `BoseSoundTouchCatalog` contains **35 historical model suggestions** from Bose's SoundTouch-app compatibility listing, including speaker generations, Portable/Wave/Stereo JC and amplifiers, adapters, soundbars and home cinema. Android `AutoCompleteTextView` displays matches while typing; **free typing remains possible** for rare/regional models. This is a curated list, not a guaranteed complete global catalog and not proof of compatibility with the setup SoftAP.
- Community outcomes are **claims to review**, not automatically trusted compatibility evidence. Maintainer checks reports for model/firmware ambiguity, repeats and contradictory experiences before promoting a model to „geprüft“. A failed setup or a device not found after timeout does **not** prove a model is incompatible.
- Android form tests: normal tested models never show the feedback path; unknown models reach it both after terminal verification and after early setup failure; finish and back buttons remain correct; select neither outcome by default.
- Unit tests: exactly three wizard model types; strict input validation, successful/failed email drafts including STR Remote/Android versions; catalog uniqueness and validation, fixed mail recipient, no device/network identifiers or silent submission.

## Source for the suggestion catalog

Bose's official historical SoundTouch-app support list identifies families and revisions; the offline list is a best-effort transcription rather than a contract that the local Wi-Fi provisioning interface works for each device:

- https://support.bose.com/s/article/vw46iii-speakertv-download-the-bose-soundtouch-app-for-compatible-products---ka08c000000hgwkaau
- https://www.bose.com/soundtouch-trade-up

User-requested update (2026-10-30): Feedback moved from GitHub Issues to the default mail program because most users do not have a GitHub account. The two reported fields remain opt-in; app and Android version metadata is added transparently and shown in the email draft. No new permissions or background transmission.

## 2026-10-30: On-device feedback-screen failure and bilingual wizard correction

**Hardware screenshots** of BUILD-0013 demonstrate two genuine UI defects:

1. The "Other SoundTouch model" first screen was too long: the repeated untested-model paragraph, full preparation card, redundant Bose-WLAN explainer and two actions were not simultaneously visible.
2. The feedback screen had excessive explanatory text and displayed an oversize **dark Android autocomplete popup with dark text**, making model suggestions unreadable and covering the form. Technical diagnostics consumed additional vertical space.
3. The whole Wi-Fi setup wizard had German literal strings despite STR Remote's supported **German and English** locales. An English phone still displayed German setup guidance, connection errors and results.

**Fix in the existing draft PR #5:**

- Trim repeated warnings on the unknown-model first screen; leave the model-specific caution in the instructions card. Remove redundant after-card explainer. Do not shrink the model dropdown, instructions or confirm action below comfortable touch sizes.
- The optional feedback screen shows just a model name input, a single Yes/No group, short privacy note and email action. Hide global status and expandable technical diagnostics *on this screen only*. Preserve a bottom "Done" action. No scroll should be needed on the tester's ordinary phone at the previous text/display scale.
- Give `AutoCompleteTextView` a **white popup drawable** and white row backgrounds with dark text explicitly in `getDropDownView`. Limit popup height to **190dp** and each suggested row to 44dp; keep inline filtering and freeform names. No giant dark full-screen overlay.
- Move model labels, instructions, headings, UI buttons, dialogs, WLAN-picker captions, network/permission/retry/transfer/verification outcomes to localized Android `values/bose_setup.xml` (English default) and `values-de/bose_setup.xml` (German), using resource placeholders for SSID/version/count and dynamic results. Respect device/app locale without a language switcher. The prepared feedback email body/subject follows the app locale too; always disclose the STR Remote and Android versions and never attach network identifiers.
- Leave Bose network protocol / credential POST / retry state logic unchanged. Technical low-level Bose API reports are retained as diagnostics, not user instructions.
- Locale regression: `BoseTechnicalReportLocalization` translates *display-only* diagnostic strings without altering Bose protocol result values. This preserves the existing single-POST/ACK policy while allowing the expanded technical section to be understandable in English. Pure JVM tests cover English vs German report rendering.
- Lifecycle regression: localized diagnostic text is initialized only **after** `super.onCreate` when Android resources become available, preventing an Activity startup crash. Lint verifies locale-resource parity; actual handset QA remains pending.
- Existing resource code references are caught by Debug and Release compilation + lint. A temporary incomplete intermediate localization commit failed CI due to missing model keys; those keys were subsequently added in both locales, and a subsequent full build passed.
- **New phone acceptance gate**: screenshot the first screen with "Other model" and early-failure button visible without scrolling; screenshot the feedback page and autocomplete suggestions for "30", with all suggestions readable. Then switch Android app language DE ↔ EN (or device locale), navigate all four wizard screens and verification/result and ensure no German prose in the English UI. Check email draft for EN versions/recipient and that all prior STR/WLAN controls still work.

Status: candidate, **not** in published 0.3.0. Do not merge PR #5 or cut 0.3.1 until UI screenshot/locale QA.

## Acceptance gates

- [ ] Latest branch GitHub CI: debug and release assembly, debug/release lint, host tests
- [ ] JVM tests: strict version ordering, malformed tags, public release with matching APK, no draft/prerelease and unknown upstream header guard
- [x] Physical-device screenshot: 10 px header spacing accepted by tester (“Abstand ist jetzt super!”)
- [ ] Physical device: STR presets, volume, speaker selector, Android status bars and back navigation still work
- [ ] Android UI test: new-version dialog shows a correct version/link; non-newer and offline situations show none
- [ ] Unknown-model feedback flow validated on a physical device, including early-failure path, typing/filtering model suggestions and a prepared email (without actually sending a test message)
- [ ] Release version is bumped to 0.3.1 or next appropriate version **only after UI acceptance**, built, signed with existing certificate, tested as update and published through release gates

Development must remain separate from public `main` until acceptance.

## 2026-10-30: Conversation handoff and mailto prefill regression

Reported on physical Android device: Android successfully presents an installed email application and populates the fixed recipient, but **subject and body are blank**. The original implementation passed them only as `Intent.EXTRA_SUBJECT` and `Intent.EXTRA_TEXT`; these may be ignored by the selected mail app. Preserve Android `ACTION_SENDTO` (`mailto:`, only mail clients) and supply the UTF-8 percent-encoded `subject` and `body` in the `mailto:` data URI as specified by RFC 6068, alongside all three existing extras. Encode line breaks as CRLF, never log draft content, and keep the send action user-controlled.

Regression tests: deterministic localized DE/EN draft composition, RFC 6068 URI encoding/decoding with Unicode and multiline text, consent/no-network-identifier boundaries. **Still pending:** exact phone/email-client smoke test for correctly filled recipient/subject/body and cancel/no-app path. No claim that mail was sent or that all clients are compatible.

Upstream STR maintainer review added to the normative [BACKLOG](../BACKLOG.md) and [PLANNED](../PLANNED.md): a distinct name from the existing iOS `STR Remote`, a visible unofficial community/brand info notice and fully understandable English Wi-Fi wizard are required before upstream Community Apps listing. The pending CSS-selector coupling, an address in QA evidence, upstream Show-and-tell thread and federated support links are also tracked. The suggested working name `Reborn Companion` is not adopted until decided. No changes to the published `v0.3.0` artifact.

## 2026-10-30: User QA update — feedback body and model search

**Observed on actual Android device (user confirmation):**

- **PASS:** optional email draft opens the mail application with recipient, subject **and** body fully populated after the RFC 6068 fix. This closes the specifically reported missing-subject/body regression for the tested mail client. No statement about other mail clients or successful sending.
- **PASS:** searching for `30` and `SoundTouch` displays readable model suggestions.
- **PASS:** feedback page does not require unnecessary scrolling.
- **OPEN:** full end-to-end English device-language wizard walkthrough, including intermediate/error/result messages, permissions and English email subject/body.
- **OPEN:** negative path without an installed mail application.

**Requested polish, implemented after the above phone check:** DE/EN feedback email body now contains named plain-text sections (speaker/model and result, software versions, note/privacy), with blank lines between groups, while preserving version fields and confidentiality rules. This change uses the existing RFC 6068 UTF-8 CRLF mailto encoding, not HTML, for broad email-client interoperability. Host tests validate both layouts; **its final on-device visual appearance still requires a quick recheck**. Do not mark BUILD-0013 overall ACCEPTED until the remaining gates close.

## Follow-up: email client rendering limitation and About screen (STR-021)

**User device observation:** the mail-client-specific visual paragraph formatting enhancement did not display as intended. The essential draft handoff (recipient, subject and complete body) remains VERIFIED on the user's client. This cosmetic layout variance is accepted as a deferred, non-blocking limitation: preserve portable plain text, no HTML or new permissions, and do not infer reliable styling from `mailto:` across clients.

**New implementation in PR #5:** a 48dp-touch-target native toolbar info icon opens a scrollable, bilingual Android About dialog on both Remote and Devices views. It displays installed app version, explicitly states independent/unofficial community status and no affiliation/endorsement by Bose or STR, credits Bose and SoundTouch as Bose Corporation trademarks, and offers user-activated GitHub source/issues and an optional external email contact action. No external request on dialog open; no additional storage, runtime dependency, Android permission, or change of signed release identity. The app name remains provisional pending STR-020.

**Acceptance still open:** verify icon touch target/content description, legible dialog and navigation on a physical phone (including small screen/large text), both DE and EN notice, all three external actions, back/close handling and no-email-client behavior. The English WLAN wizard remains separately unverified on device.

## 2026-10-30: title-adjacent info affordance and app naming options

**User feedback:** About page successfully appears on their phone. Move its entry point off the Remote view and place it directly after the app name in the Devices title, visually `STR Remote ⓘ` until renaming.

**Implemented on PR #5:** a circled `ⓘ` text action with 48dp touch target and localized accessibility content description is positioned immediately after the toolbar title inside a horizontal title row. It is **visible only in the Devices/discovery panel**, hidden in active Remote/WebView and Remote-offline states. The existing bilingual About dialog and links are unchanged. Needs on-device screenshot and navigation QA.

**Brand candidates:** `Remote for STR` (recommended) and `Remote 4 STR` (alternative). No final user choice yet; do not change the visible title, Android `applicationId`, signing certificate, repo URL, updater endpoint or release links until the chosen name is finalized. A GitHub repository rename is feasible after final decision, but must include source/configuration/update-check URL/link adjustments. Maintaining Android package identity preserves upgrade compatibility for the two existing users.

## Product name finalized: Remote for STR (STR-020)

User chose **Remote for STR** as final app name. The implementation branch now uses this in both Android string locales, including launcher, Devices title (`Remote for STR ⓘ` on Devices only), permission/setup/update notices and optional email-version text. README reflects the new brand, while explicitly distinguishing the old published STR Remote 0.3.0 release.

Future signed APK: `Remote-for-STR-<version>.apk` via existing signing/release workflow. Update checker matches new and legacy release asset names, with host tests covering both. Android application ID and original signing certificate are intentionally unchanged to allow upgrades. No version bump or public release has occurred.

**Repository rename COMPLETE:** actual repository is `K-Cosmo/remote-for-str-android` (verified via connected GitHub repository metadata and PR #5). The proposed `remote-for-str` slug was not chosen. The app's direct HTTPS Releases API URL, release-page links, About links and associated host test now point to the actual new repo; redirect handling remains disabled as before. CI and actual-device QA remain pending.

**Acceptance still pending:** CI green on exact head; real phone screenshot of new launcher/Devices title; full English wizard/Info UI; release package/signature/versionCode and update-over-v0.3.0 tests. Preserve accepted `v0.3.0` release.

### Repository URL cutover — verified 2026-10-30

The user provided the actual renamed repository: `https://github.com/K-Cosmo/remote-for-str-android`. PR #5 survived the rename and remains open on `feature/update-checker-compact-str-header`. Coordinated one commit for update-checker HTTPS URL, release-page URL, About links, unit test and README. Updated normative status, planning, decisions and privacy docs to this canonical path. Do not confuse the published `v0.3.0` signed artifact names with a repository URL migration. A successful Android build proves compilation and tests, not device-level GitHub network reachability or English wizard UX.

## 2026-10-30: English Wi-Fi wizard — physical Android QA confirmation

The user explicitly confirmed **“Englische Geräteprüfung des WLAN-Assistenten: Bestätigt.”** Record **PASS** for the English-language WLAN setup wizard on the user's actual phone (STR-018 / upstream Community Apps language requirement). This confirmation is a language/UI acceptance report; it does not establish independent testing of every permission-denied path, every failure scenario, all compatible Bose speaker variants, or actual English-language email sending.

CI: both Android Actions runs **41138096537** and **41138090921** passed for source commit `e5759e6aed445eb03ca7d574d62c09a692a865eb` before this documentation-only evidence update. Recheck CI on the final candidate head.

Outstanding release-specific gates: complete any outstanding regressions for Remote control, native title/Info display after rebrand, production signing with original key, `versionCode` increment, signed update-over-v0.3.0 preserving saved data, and verifying the production update dialog against an actual newer release. Before promoting the GitHub repository to the upstream maintainer, sanitize the known private LAN address in historical QA evidence. The cosmetic paragraph spacing limitation in the external email client remains non-blocking. No merge, release, new APK or compatibility certification occurred from this confirmation.

## 0.3.1 release-candidate preparation and CI documentation gate

- Android `versionName=0.3.1`, `versionCode=12` staged on the open PR branch, **not** published, tagged or signed. The original application ID remains `app.strremote.android`, with debug variant `.poc`.
- Signed Windows release script already derives `Remote-for-STR-0.3.1.apk` and checksum artifact from versionName, and refuses dirty builds/changed signer cert. Release signing requires the original local keystore; do **not** copy the keystore/password into GitHub or the issue tracker.
- CI now executes `tools/verify-doc-consistency.ps1` before Android build, with the documented public 0.3.0 release treated separately from current 0.3.1 development state. Existing 0.3.0 signing evidence/tags remain untouched.
- Two 2026-09-26 Markdown QA reports now use reserved `192.0.2.29` documentation placeholders instead of the actual private host IP, with explanatory notes. Historic commits may still expose the old text; no repository history rewrite or forced push.

### Remaining exact-signed-APK verification checklist

1. Confirm the new debug/test APK has **Remote for STR** in launcher and **Remote for STR ⓘ** on Devices only; the Info action is absent in the Remote/WebView and Remote-offline views. Check saved devices/rooms, STR playback/preset/volume controls, Android back navigation, and DE/EN information dialog; do not infer these from compile-only CI.
2. Locally on a **clean, committed source tree** with original signing keystore available, run:

   ```powershell
   .\\tools\\build-signed-release.ps1 -ProjectRoot . -KeystorePath "<path-to-your-existing-release-keystore>"
   ```

   Supply the alias/password interactively. Script performs clean Android Debug+Release build, lint, unit tests, 16KiB zipalign, APK signing, signer SHA-256 validation and emits `dist/Remote-for-STR-0.3.1.apk` plus checksum. Never commit private signing materials.
3. **Upgrade test:** with the currently signed `v0.3.0` installed and saved speaker/room configuration retained, use `adb install -r dist/Remote-for-STR-0.3.1.apk` (no uninstall). Verify the installer treats it as an **update**, Android App Info shows 0.3.1, saved speakers/rooms persist, and normal remote controls work. Capture signer/certificate check output and the exact APK SHA-256 for release evidence.
4. Production update-checker: verify current/older versions are silent, and a newer public stable release with correctly named APK opens its official page **only after explicit consent**. This cannot be fully exercised against the actual latest public release until a newer public release is available; deterministic policy coverage exists in unit tests. Do not publish an intentionally broken test release.
5. Only after explicit phone/signer/user acceptance, run final clean CI, merge PR and create the `v0.3.1` signed release with its checksum. Currently **OPEN**.

## Follow-up Git privacy/documentation audit (unreleased 0.3.1)

- Corrected the stale legacy GitHub Releases endpoint in `doc/SECURITY.md` to `K-Cosmo/remote-for-str-android` and removed a repeated STR-024 status paragraph from `doc/PLANNED.md`.
- Distinguished the published accepted 0.3.0 state from 0.3.1 IN DEVELOPMENT/QA in `doc/README.md`.
- Scanned current Markdown `doc/`: the only RFC1918 values remaining are protocol-specific Bose setup candidate addresses; no obvious committed credentials, key blocks, MAC addresses or personal home directories were found by heuristic scans. Confirmed previously published QA reports are redacted in current `main` and feature branch; historic objects persist.
- Prepared audited offline `git-filter-repo` rewrite script and documented six obsolete branch refs. Updated script to scan every reachable commit, preserve an offline unmodified mirror and avoid remote mutation; new GitHub CI parses its PowerShell syntax without running it.
- Historic rewrite and branch deletions are **NOT executed**: installed integration does not provide delete-ref or mirror-push, while normal Git network transport is unavailable. Must be done as one coordinated operation on an authenticated local workstation after offline backup, open PR handling and release-tag provenance checks.

## Current post-rewrite continuation: draft PR #6

Normal Git history cleanup is complete on GitHub: two rewritten branches, five rewritten release tags and six obsolete branches removed. Original PR #5 was closed without merge; draft PR #6 now tracks the sanitized development branch. User-confirmed local checks included matching branch-tip trees, `git fsck --full --strict`, an exact-string scan of 285 rewritten reachable commit trees and all five signed APK backup SHA-256 files. Original and rewritten source-tag identities are explained in `doc/GIT_HISTORY_PROVENANCE.md`. Historic process descriptions above remain historical, not current directives. GitHub-managed old pull-request refs remain; 0.3.1 signing, in-place update and handset QA remain open.

## 2026-10-31: signed 0.3.1 candidate / six-case phone acceptance

- The user reported successful final local `tools/build-signed-release.ps1` invocation on a clean checkout of `0e6bab9d9e1bcac19b542755b7a1774c69d2776a`. Local output: `dist/Remote-for-STR-0.3.1.apk`, SHA-256 `8100334f067346c9987a68969bdebd9065455bfe73c21a0634f0dba42ddbd759`, matching checksum text file. The script's success indicates passed signing-certificate, `apksigner verify` and 16 KiB zipalign gates; source code has not changed since the build.
- The user explicitly reported **all six handset acceptance scenarios PASSED** for this exact signed candidate: 0.3.0 to 0.3.1 update with devices/rooms retained; Remote for STR app branding and Info placement; playback/volume/presets; DE/EN SoundTouch wizard; email compose prefill; relaunch/persistence.
- CI for the exact signed source commit passed twice: push run `41146044403` and PR run `41146049240`.
- Limitation: this is the user's reported QA and exact output digest, not an independently uploaded APK or full ADB/apksigner transcript. **No public 0.3.1 release, tag or GitHub APK asset exists yet.** See the scoped [signed candidate evidence](../evidence/qa/2026-10-31-release-candidate-0.3.1.md).
- Residual optional/negative-case checks (no mail app, rejected Wi-Fi permission, missing network and untested speakers), live newer-version checker demonstration and upstream CSS dependency remain separated from the six passed acceptance cases and require explicit scoped disposition before public release.
- This appended dated result supersedes older BUILD-0013 entries saying signed 0.3.1 work remains pending; do not rewrite historical process notes. Follow-up docs-only commits must **not** change/rebuild the tested signed APK and must not be mistaken for its source commit.

## 2026-10-31: final GitHub publication and exact artifact verification

- Owner approved deferring the untested special cases (absent email app, denied WLAN permission, network failures and untested hardware) to [STR-027](https://github.com/K-Cosmo/remote-for-str-android/issues/7). Deferred **does not mean passed**.
- PR #6 was normally merged to main with merge commit `ed7652783a9752e107449a572c5b6f2dff624922`; exact sign-source `0e6bab9d9e1bcac19b542755b7a1774c69d2776a` was preserved. Feature and post-merge CI passed, including [run 41189408057](https://github.com/K-Cosmo/remote-for-str-android/actions/runs/41189408057).
- User used `gh` to create annotated tag `v0.3.1` (object `dd0480df720d805b0c6b76fd20757b59d0ee5cfe`) pointing to **exact** signed source `0e6bab9d9e1bcac19b542755b7a1774c69d2776a`, staged the two previously signed release assets in draft Release ID `429849089`, and published the release on 2026-10-31. A tag-based draft lookup gave HTTP 404 even though the draft existed; publication via `gh release edit v0.3.1 --draft=false` succeeded. Do not recreate tag or assets.
- GitHub `releases/latest` confirms `v0.3.1`, non-draft, non-prerelease, published `2026-10-31T09:56:33Z`. GitHub's published `Remote-for-STR-0.3.1.apk` SHA-256 is `8100334f067346c9987a68969bdebd9065455bfe73c21a0634f0dba42ddbd759`, matching the local tester's signed APK exactly. `Remote-for-STR-0.3.1.apk.sha256.txt` is also uploaded. No re-signing or code change to the signed version.
- Dated previous lines about unpublished code remain historical and are superseded by this final publication record and [publication evidence](../evidence/qa/2026-10-31-release-0.3.1.md).
