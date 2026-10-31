# Planned work

**Latest released version:** see [Status](STATUS.md). The completed BUILD-0013 work, release notes and device tests are maintained in [BUILD-0013](work/BUILD-0013.md), [Changelog](CHANGELOG.md) and the [v0.3.1 release evidence](evidence/qa/2026-10-31-release-0.3.1.md), not duplicated in this plan.

## Current priorities

| Item | Priority | Next action |
|---|---|---|
| **STR-022** | P1 | Agree a stable embedding/CSS hook with the upstream STR maintainer |
| **STR-025** | P1 | Request a Community Apps listing for Remote for STR; not yet confirmed |
| **STR-023** | P1 | Set up federated community support: upstream discovery thread, app Issues and optional email |
| **STR-027** | P1 | Run explicitly deferred negative-path QA and validate future positive release-update notification |
| **STR-024** | P1 | Follow up on residual GitHub-managed historical PR references and caches |
| **STR-026 / STR-012** | P2 | Independently collect original Bose-firmware provisioning / STR remote compatibility evidence |
| **STR-014 / BUILD-0008** | P2 | Evaluate Gradle 9.8.x as isolated maintenance work |

For details and current states, see [Backlog](BACKLOG.md) and [Findings](FINDINGS.md).

## STR-022 — Stable STR WebUI embedding interface proposal

**Currently released (0.3.1):** The upstream STR phone WebUI styles its sticky header with `padding-top: calc(16px + env(safe-area-inset-top))` for browser/PWA safe areas. Android already handles system insets natively, so Remote for STR currently overrides `body > header` to 10 CSS px, but only after it recognizes `#dev` and `#powerBtn`. Those DOM selectors are **private implementation details**, not a supported contract.

**Potential upstream-owned solution (proposal, NOT implemented):** STR could expose a documented CSS custom property that preserves the normal PWA default when not set:

```css
/* Proposed upstream STR change; not an existing public interface */
header {
  padding-top: var(
    --str-embed-header-padding-top,
    calc(16px + env(safe-area-inset-top))
  );
}
```

An embedding client that has verified STR supports this contract would set `--str-embed-header-padding-top: 10px` on the document root. STR would continue to own speaker selection, power and accessibility controls; the Android app would own only its native frame and requested header spacing. No new JavaScript bridge or duplicate playback logic is needed.

**Acceptance criteria:** Agree hook naming and version/capability discovery with the STR maintainer; preserve browser/PWA and notched-iPhone default insets; verify Android WebView, light/high-contrast, zoom/a11y layouts and controls. Existing STR installations without a hook must still work; retain the current guarded workaround only where known compatible and never forcibly modify unknown DOM. Until upstream ships and documents such a feature, this remains an **open architectural proposal**, not an API the Android app can rely on.

Sources: [current Android override](../app/src/main/kotlin/app/strremote/android/StrWebViewHeader.kt), [upstream STR WebUI](https://github.com/JRpersonal/streborn/blob/main/internal/webui/assets/index.html), [current integration assumptions](API_CONTRACTS.md), decision [D-036](DECISIONS.md).

## STR Community Apps and support

The STR maintainer's response of 2026-10-30 requested a distinct Android app name, visible unofficial/independent-community information and an understandable English WLAN assistant. **All three have been implemented and accepted for the tested 0.3.1 scope; the upstream listing itself is not yet confirmed.**

Request inclusion under **Remote for STR**, with links to the [repository](https://github.com/K-Cosmo/remote-for-str-android), [releases](https://github.com/K-Cosmo/remote-for-str-android/releases/latest), and optional upstream community Show-and-tell thread. App-specific issues belong in this repository's [Issues](https://github.com/K-Cosmo/remote-for-str-android/issues); volunteers without GitHub can prepare feedback in the app for `str-remote-android@gmx.org`. No new backend or automatic telemetry is planned.

## Google Play Console — staged distribution backlog (NOT a 0.3.2 release gate)

The owner reported completing developer-account registration and paying Google's one-time USD 25 fee on 2026-09-26. **An STR app entry, closed testing, developer/package verification, Play approval and publication have not been confirmed.** Keep GitHub APK releases and the Play Store rollout independently gated. See [Backlog STR-029–STR-034](BACKLOG.md#google-play-console-launch-preparation-2026-10-31-not-yet-published).

| ID | Priority | Next action / acceptance evidence |
|---|---|---|
| **STR-029** | P1 | Verify developer account / existing-package ownership; create Play Console entry for unchanged `app.strremote.android`; capture dashboard status without exposing account identifiers |
| **STR-030** | P0 for Play | Decide and validate Play App Signing with **the existing GitHub release-signing identity**. A Play-generated different signing certificate can prevent an in-place update of a sideloaded GitHub APK. Prepare AAB/upload-key workflow and test an actual channel transition without data loss before first rollout |
| **STR-031** | P1 | Complete bilingual store listing, imagery, support, privacy/Data safety and declarations. State that STR is required for remote functions and the app is independent/unofficial. Avoid publishing complete mailbox literals in repo docs/source; evaluate Play's necessarily public support-contact field separately |
| **STR-032** | P1 | Invite voluntary testers through STR community and GitHub once the scope is ready, without asserting upstream endorsement. If a post-2023 personal developer account, run Google's qualifying closed test with at least **12 continuously enrolled testers for 14 days**, record real feedback, and request production eligibility only when criteria are met |
| **STR-033** | P1 | Specify channel-aware update behavior, signed AAB/APK artifact provenance and monotonically increasing versionCode; avoid sending Play-managed installs blindly to a GitHub APK download |
| **STR-034** | P1 | Gather applicable hardware and negative-path acceptance (STR-027), comply with Google review/permissions/API policy, and obtain a **separate explicit approval** before any Play testing upload or production rollout |

Do not treat a paid Play developer registration as a created or approved app. Play app-signing choice, package ownership and public support-contact disclosure are **open decisions**, not silently authorized architectural changes. Google's target-API requirement for new mobile apps since 2026-08-31 is API 36+; the current project target is API 37, but Play submission acceptance still requires review of the actual AAB and Console policy forms. [Official Play testing rules](https://support.google.com/googleplay/android-developer/answer/14151465) · [Signing guidance](https://developer.android.com/studio/publish/app-signing) · [2026 developer verification](https://developer.android.com/developer-verification/guides/pdf-guides/pdc-guide.pdf) · [API rules](https://support.google.com/googleplay/android-developer/answer/11926878).

## Later candidates

F-Droid distribution, further hardware evidence and possible STR authentication changes remain separate future priorities. Preserve the original Android package ID, signing identity, stored user data and already published APKs.
