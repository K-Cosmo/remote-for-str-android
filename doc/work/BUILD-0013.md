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

## Acceptance gates

- [ ] Latest branch GitHub CI: debug and release assembly, debug/release lint, host tests
- [ ] JVM tests: strict version ordering, malformed tags, public release with matching APK, no draft/prerelease and unknown upstream header guard
- [x] Physical-device screenshot: 10 px header spacing accepted by tester (“Abstand ist jetzt super!”)
- [ ] Physical device: STR presets, volume, speaker selector, Android status bars and back navigation still work
- [ ] Android UI test: new-version dialog shows a correct version/link; non-newer and offline situations show none
- [ ] Unknown-model feedback flow validated on a physical device, including early-failure path, typing/filtering model suggestions and a prepared email (without actually sending a test message)
- [ ] Release version is bumped to 0.3.1 or next appropriate version **only after UI acceptance**, built, signed with existing certificate, tested as update and published through release gates

Development must remain separate from public `main` until acceptance.
