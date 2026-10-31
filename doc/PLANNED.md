# Planned work

**Latest released version:** see [Status](STATUS.md). The completed BUILD-0013 work, release notes and device tests are maintained in [BUILD-0013](work/BUILD-0013.md), [Changelog](CHANGELOG.md) and the [v0.3.1 release evidence](evidence/qa/2026-10-31-release-0.3.1.md), not duplicated in this plan.

## Current priorities

| Item | Priority | Next action |
|---|---|---|
| **STR-022** | P1 | Agree a stable embedding/CSS hook with the upstream STR maintainer |
| **STR-025** | P1 | Request a Community Apps listing for Remote for STR; not yet confirmed |
| **STR-023** | P1 | Set up federated community support: upstream discovery thread, app Issues and optional email |
| **STR-027** | P1 | Run explicitly deferred negative-path QA and validate future positive release-update notification |
| **STR-028** | P1 | Validate the new short Android launcher label, updated shared mail recipient and public-doc/source anti-harvest gate for a signed 0.3.2 candidate |
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

Request inclusion under **Remote for STR**, with links to the [repository](https://github.com/K-Cosmo/remote-for-str-android), [releases](https://github.com/K-Cosmo/remote-for-str-android/releases/latest), and optional upstream community Show-and-tell thread. App-specific issues belong in this repository's [Issues](https://github.com/K-Cosmo/remote-for-str-android/issues); volunteers without GitHub can prepare optional email feedback directly in the app. No new backend or automatic telemetry is planned.

## Later candidates

Play Store / F-Droid distribution, further hardware evidence and possible STR authentication changes remain separate future priorities. Preserve the original Android package ID, signing identity, stored user data and already published APKs.

## STR-028 — 0.3.2 display label and feedback recipient acceptance

- Confirm **Remote STR** under the Android app icon after an in-place upgrade from signed public 0.3.1. The native Devices title, About notices and public project name remain **Remote for STR**.
- Test both SoundTouch model-feedback and About contact actions on a real device, verifying their drafts go to the updated project mailbox. Never publish recipient addresses in QA logs or documentation.
- Original app ID, signing key, saved speakers/rooms and existing v0.3.1 signed APK must be preserved. Build/sign 0.3.2 from a clean exact source commit only after PR review.
- The reversible runtime string is an anti-harvest nuisance measure, not encryption, guaranteed secrecy or server-side spam filtering.
