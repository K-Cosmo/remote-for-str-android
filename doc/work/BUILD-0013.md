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

- Only `GET https://api.github.com/repos/K-Cosmo/str-remote-android/releases/latest`, fixed official public endpoint; no authentication token, no analytics endpoint.
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
