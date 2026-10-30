# BUILD-0013 — GitHub update notification and compact STR WebView header

Status: **feature branch implemented; device UI acceptance pending**. Public release remains `v0.3.0`. Do not tag or publish a new version until the required tests pass and the existing signing key is reused.

## User-reported needs (2026-10-30)

1. STR Remote needs an in-app notification when a newer version is published, comparable to the existing Abfahrtsradar implementation.
2. On the main remote screen, the screenshot highlights an unused vertical region *between the native STR Remote title and the STR WebUI header buttons*. Remove the redundant top padding only; do **not** remove the speaker switcher, power or accessibility controls.
3. Bose SoundTouch 10 original-firmware WLAN setup was successfully hardware-tested against the user's **separate test WLAN profile**. Retain the limitation that other profiles and firmware versions are not independently tested.

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
- Apply `body > header { padding-top: 0 !important; }` **only when the DOM contains both** `#dev` and `#powerBtn`. Android already handles OS bars. On unknown upstream DOMs or errors, do nothing.
- Preserve the STR header, sticky behavior, speaker switcher, power button and accessibility (`Aa`), and all playback/preset elements. No JavaScript interface, host injection or changes to STR firmware.
- Regression concern: STR upstream UI revisions may change header structure and safe-area handling; require screenshot QA, both standard dark and accessibility text size if possible.

## SoundTouch 10 validation

- Reported device-level success: original-firmware WLAN setup on SoundTouch 10 with a separate test profile (2026-10-30). Update model selector to **mit Test-WLAN geprüft**, with a limited-coverage information line.
- Do not rewrite the historical v0.3.0 release/tag or claim that all Bose firmware/WLAN combinations are validated.
- Record the user-reported result in a standalone QA note; previous ST10 STR remote testing was a different scope.

## Acceptance gates

- [ ] Latest branch GitHub CI: debug and release assembly, debug/release lint, host tests
- [ ] JVM tests: strict version ordering, malformed tags, public release with matching APK, no draft/prerelease and unknown upstream header guard
- [ ] Physical-device screenshot: highlighted top gap is reduced, and STR header controls remain usable
- [ ] Physical device: STR presets, volume, speaker selector, Android status bars and back navigation still work
- [ ] Android UI test: new-version dialog shows a correct version/link; non-newer and offline situations show none
- [ ] Release version is bumped to 0.3.1 or next appropriate version **only after UI acceptance**, built, signed with existing certificate, tested as update and published through release gates

Development must remain separate from public `main` until acceptance.
