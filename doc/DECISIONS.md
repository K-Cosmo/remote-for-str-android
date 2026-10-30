# Decision log

## D-001 — Thin-wrapper architecture

**Decision:** STR Remote remains an Android integration layer around STR's existing web UI rather than reimplementing playback/business logic.

## D-002 — `/doc` is normative

**Decision:** all lasting product, architecture, compatibility, security and quality rules live in `/doc`. Spec Kit is process governance only.

## D-003 — Stable toolchain policy

**Decision:** use stable AGP 9.4.1 and Gradle 9.7.1 for the current baseline. Gradle daemon runtime is Java 25 on workstation/CI while application Java source/target stays at 17. Do not move to newer Gradle/AGP solely because the IDE or lint offers an upgrade during release freeze.

## D-004 — Preserve internal model visibility

**Decision:** fix the 0.1.0 callback compiler error by keeping speaker models internal and making the discovery callback an internal/private implementation detail of `MainActivity`, rather than widening model visibility.

## D-005 — Daemon JVM criteria require real JVM evidence

**Decision:** do not guess the local Gradle daemon JVM major version. The working environment is verified as JetBrains Runtime 25.0.3 with the Gradle daemon using the Android Studio JBR. After the green rc2 lint/build pass, pin only the daemon **major version 25**; do not pin a vendor and do not add a toolchain resolver/provisioning plugin until portability evidence shows it is needed.

## D-006 — Native shell owns system insets; WebView owns the content rectangle

**Decision:** system-bar insets are applied once at the native root. Product spacing belongs to the toolbar/discovery components, not to the root content container. The STR WebView remains match-parent inside the available content rectangle so upstream STR controls its own internal page padding.

## D-007 — Discovery UI reports one state at a time

**Decision:** do not show independent native messages for discovery explanation, endpoint probing and connection status at the same time. Discovery uses one state header plus speaker rows. The connected endpoint line is shown only while the WebView is active.

## D-008 — Do not request unrelated Bluetooth permission for WebView log noise

**Decision:** runtime permissions must reflect functionality owned by STR Remote. Chromium/WebView warnings alone are not a reason to request Bluetooth access when the app itself does not use Bluetooth APIs.

## D-009 — Feature-freeze the verified MVP for first public release preparation

**Decision:** after the second real-device screenshot/build/Logcat pass verified the previously identified layout blockers as fixed, stop adding MVP features and move the project to `0.1.0-rc1` release hardening. Remaining work is evidence, toolchain reproducibility, signing and publication quality unless a new release blocker is demonstrated.

## D-010 — Publicly disclose AI-assisted development without implying runtime AI

**Decision:** the public README explicitly states that AI assists implementation, analysis, debugging, review and documentation, while human direction plus real build/runtime evidence controls acceptance. It must also state that the Android application contains no AI/LLM runtime and does not send user data to an AI provider.

## D-011 — No lint baseline for the first release

**Decision:** release-hardening lint findings are handled individually. Do not introduce a lint baseline to hide the current findings. Intentional exceptions must be local, documented and narrowly scoped.

## D-012 — Pin WebView top-level navigation to the selected STR speaker

**Decision:** after an endpoint is selected, only HTTP top-level navigation to the same speaker host on STR ports 8888/17008 may remain inside the WebView. Other HTTP/HTTPS top-level links are handed to the system browser. Subresources remain owned by the upstream STR page so the wrapper does not reimplement or accidentally break upstream functionality.

## D-013 — Independent app branding; approved icon is AI-generated project artwork

**Decision:** STR Remote uses its own remote/speaker icon rather than copying the upstream STR logo as its application identity. The approved source artwork is retained under `/artwork` and may be adapted into Android adaptive/themed icon resources. Public documentation may state that the icon was AI-generated as part of the project's disclosed AI-assisted workflow.

## D-014 — Pin Gradle daemon JVM major version, not workstation-specific path/vendor

**Decision:** commit `gradle/gradle-daemon-jvm.properties` with `toolchainVersion=25`. Do not encode the Android Studio JBR path or JetBrains vendor. The development workstation may satisfy Java 25 with JBR 25.0.3; CI may satisfy it with another compatible JDK 25 distribution. The Gradle wrapper itself still requires a Java launcher through `JAVA_HOME` or `PATH` before daemon criteria can be evaluated.

## D-015 — Launcher artwork source is not an Android adaptive-icon layer

**Decision:** retain the approved square AI-generated artwork only as project source material under `/artwork`. Android packaging uses a solid background plus a transparent foreground motif sized within the adaptive-icon safe area, with a separate monochrome layer. Do not package the complete pre-masked square artwork as an adaptive-icon background.

## D-016 — Wi-Fi transport, not Internet validation, gates local STR discovery

**Decision:** before automatic saved-endpoint probing or mDNS discovery, STR Remote requires an available Android Wi-Fi transport. It must not require `NET_CAPABILITY_VALIDATED` or other proof of Internet reachability because STR is local-LAN functionality. Missing Wi-Fi is a stable user-visible state with Retry, not an endless discovery attempt.

## D-017 — Release APKs are built only from a clean committed source tree

**Decision:** the local release-signing tool derives artifact names from the application `versionName`, records Git HEAD, and refuses to build a release artifact from a dirty working tree. The exact source commit used for the signed APK is the commit later tagged for that release.

## D-018 — Keep Gradle 9.7.1 for the 0.2.0 feature line

**Decision:** do not upgrade from the accepted Gradle 9.7.1 baseline to Gradle 9.8.x solely because 9.8 is newer. AGP 9.4.x requires Gradle 9.6.0 or newer, and the project already has green local/CI evidence on 9.7.1. Reassess the Gradle line after 0.2.0 or earlier only when a concrete compatibility, build-fix or feature requirement justifies the change. Build-tool maintenance stays isolated from application feature work.

## D-019 — Room assignment is local saved-speaker metadata

**Decision:** room assignment is optional metadata owned by STR Remote for explicitly saved speakers. Built-in rooms use stable storage IDs (`living_room`, `bedroom`, `kitchen`, `bathroom`, `kids_room`, `garden`) and are localized only at display time. A custom room is stored as the user's local text. Room metadata does not participate in speaker identity, endpoint probing, playback behavior or WebView security decisions, and it is never cloud-synced.

## D-020 — Important guidance must be visually highlighted

**Decision:** explanatory/discovery guidance for the device screen should not hide as tiny footer text. Important hints and state messages use highlighted panels with clearer typography so non-technical users notice them quickly.

## D-021 — Device management must always offer an obvious way back to the remote

**Decision:** when a speaker is already active, entering the device view changes the toolbar action from `Geräte` to `Fernbedienung`. The user must not need to re-select the active speaker or rely on Android back navigation merely to return to the remote.

## D-022 — Deleting a saved discovered speaker hides it only for the current device view

**Decision:** deletion removes saved metadata and immediately removes the row from the current list. Because discovery is automatic, a fresh device search may legitimately find the physical speaker again; the confirmation text must say so.

## D-023 — Dismiss help text explicitly, not by hidden gesture

**Decision:** the highlighted device-management hint uses a visible `Hinweis ausblenden` / `Hide this hint` text link. Do not use swipe-to-dismiss or a trash icon for help text because those interactions are hidden or semantically ambiguous. Dismissal is remembered in app-private preferences.
## D-024 — Make common speaker actions explicit and one-tap

**Decision:** the device list remains tap-to-connect. Row taps are handled explicitly so inline action controls cannot block connection/switching. Each row exposes an inline room action; assigning/changing a room implicitly saves the speaker. Saved speakers expose a visible delete icon with a confirmation dialog. Common actions must not depend on hidden long-press gestures.

**Maintenance note:** this decision was originally written under a duplicate D-019 heading. The identifier was normalized to D-024 during the post-0.2.0 documentation drift audit; the decision itself is unchanged.

## D-025 — Use Android 10+ SoftAP provisioning rather than Bluetooth for Bose setup

**Decision:** BUILD-0012 explores the original Bose setup Wi-Fi (SoftAP) for a SoundTouch 20 Series III / model 355589-SM2 with original Bose firmware. Use Android `WifiNetworkSpecifier` with explicit system user approval; do not add Bluetooth provisioning. The wizard is available only on Android 10 (API 29)+ while the existing minSdk 26 app functionality remains unchanged. Phase A is strictly read-only and may not submit Wi-Fi credentials, factory-reset, flash firmware, or assume a provisioning contract. Phase B may send a wireless profile only after on-device verification of the original Bose local API and explicit user confirmation. Scope HTTP I/O to the network returned by Android; release it on cancel/failure; keep home Wi-Fi passwords volatile and out of logs/storage. Do not fold STR installation into Bose Wi-Fi setup. Record results per model/firmware and keep the accepted 0.2.1 source/tag/APK unchanged.

## D-026 — Bose-Setup-SSID exclusively through Android's approved network picker

**Decision:** BUILD-0012 and the later guided setup must not ask the user to manually enter the *Bose speaker's temporary setup SSID*. On Android 10+ use a `WifiNetworkSpecifier` SSID-prefix match and require Android's explicit network selection/consent. Do not substitute an unrestricted Wi-Fi scanner just to imitate Bose discovery; `WifiManager` scanning has different location permissions and OS throttling. The future *home Wi-Fi* SSID/password are separate inputs for provisioning after the Bose interface is validated. Keep the setup UI KISS: automatic discovery, bounded attempt, explicit cancel and a retry that starts an independently fresh Android request/session. Avoid duplicate network callbacks. The initial read-only probe may release the temporary connection on completion, but a future confirmed provisioning operation must keep it until submission is resolved. Hardware behavior and retry reliability must be demonstrated repeatedly, never inferred from CI alone.

## D-027 — B1 URL enumeration does not authorize a WLAN profile POST

**Decision:** On 2026-10-29 the tested SoundTouch 20 Series III original firmware returned HTTP 200/XML for `GET /supportedURLs` and advertised `/addWirelessProfile`, `/performWirelessSiteSurvey` and `/getActiveWirelessProfile`. This closes only the **read-only endpoint-advertisement** question. Bose's official Web API v1.1 (2026-04-01) does not define these setup routes; the AfterTouch community implementation provides a candidate `POST /addWirelessProfile` XML payload, not a Bose-supported on-device contract. Keep the write path blocked until the reproducible Android 3+3 retry gate and a safely controlled device-specific validation plan pass. A future test must be debug-only, explicitly confirmed, scoped to the Android-approved Bose `Network`, single-submit without automatic POST retry on ambiguous timeout, use only a test SSID, keep credentials in memory without logs or persistence, and verify the actual speaker on the destination LAN after releasing SoftAP. Preserve 0.2.1 release/main unchanged.

## D-028 — Authorize isolated single-shot B2 hardware experiment, not general provisioning

**Decision:** Tester reports the 3+3 Android Bose-setup picker/retry acceptance gate passed on 2026-10-29, without per-run logs or Bose firmware version. Authorize a **debug-only, manually confirmed, single-request hardware experiment** using an isolated WPA2-Personal test SSID and the community-derived `AddWirelessProfile` candidate. Do not auto-retry after ambiguous network loss, timeout or noncanonical response. Release Bose-scoped network after test or cancel; do not change production release or silently import device credentials. Follow up with independent observation of same speaker joining the test WLAN. This is permission to run a narrowly scoped experiment, not evidence of successful provisioning or approval for a full end-user setup wizard.

## D-029 — Three guided setup screens and a truthful post-transfer verification result

**Decision:** Once the Bose SoftAP connectivity, Bose-side WLAN survey and single-shot profile transfer have been reported working on the reference SoundTouch 20 III, replace the debug PoC's one-page protocol-oriented UI with three distinct user tasks: (1) physical setup-mode preparation, (2) Android system-approved speaker connection, and (3) future WLAN selection plus password entry. Automatically advance from 2 to 3 after verified read-only API capabilities. Use an inline, compact, manually expandable WLAN list and persistent, high-contrast input labels; keep diagnostics hidden behind an explicit detail control. After submission show a separate **verification result**, not a false success page: a Bose HTTP acknowledgement cannot establish the same speaker's actual LAN association. Preserve Android's tested Bose Wi-Fi picker/fresh-Activity retry and single scoped profile POST, never log credentials, and do not touch the accepted 0.2.1 production release. Document on-device QA before considering a merge.

## D-030 — KISS: single action for WLAN transfer and automatic same-device LAN verification

**Decision:** The third onboarding page must not show a long inline WLAN list or a second confirmation after the explicit send button. Use a concise selection popup, visible selected WLAN, and clear password entry. One tap on `WLAN auf SoundTouch übertragen` starts a single scoped POST after field validation and immediately displays a spinner; no automatic retries. Android generally restores normal Wi-Fi after unregistering the temporary setup network, so **observe** the return to Wi-Fi instead of telling users to switch networks unconditionally. Discover Bose devices through mDNS/SSDP on the phone's actual Wi-Fi `Network`, compare the original `/info` deviceID in memory with a candidate device's `/info`, and if the device supports `/getActiveWirelessProfile`, compare its SSID with the user's selection. Never claim full setup success based solely on HTTP acknowledgement, SSID text, or discovery without identity match. Bound verification to 45 seconds and offer independent discovery retry with no credential resubmission. Keep this implementation debug-only pending device QA.

## D-031 — Wi-Fi chooser must not expire while a user is still deciding

**Decision:** In BUILD-0012's Android 10+ `WifiNetworkSpecifier` flow, do not use `requestNetwork`'s fixed timeout while the Android system network picker is visible. A 20-second request timeout can abort selection before the person has even chosen the Bose AP. The Android picker and Activity are both UI surfaces: `onAvailable` can precede Activity `onStart` after chooser dismissal, so never release the callback through a 2-second background cleanup during Wi-Fi connection validation. Use the untimed request, user-driven cancellation, a bounded background cleanup *after* the connection becomes available, and a short cooldown only for fresh-Activity retry. Preserve explicit callback release on cancel/destruction, the local-only no-internet request capabilities and network-scoped Bose HTTP. Expose sanitized in-app connection events for QA when Logcat capture is empty. Real hardware must re-pass 3+3 picker/retry reliability before any main merge.

## D-032 — Device discovery must have a terminal state, and Bose setup is not STR installation

**Decision:** An mDNS discovery result establishes the presence of a network-advertised service, **not** a functioning STR web remote. Repeated advertisements may not requeue a host's in-flight or completed HTTP probe during the same discovery generation. STR probing must use bounded timeouts, concurrency and queue capacity; callback settlement must be deterministic even for malformed hosts or rejected work. A discovery deadline must always remove the indefinite "checking" progress UI while still allowing a late valid result to update the device row. A Bose WLAN provisioned through BUILD-0012 is kept in a separate **debug-only** pending store, not `SavedSpeaker`, and is shown grey with a clear STR-missing message; tapping it opens the existing STR installation website in the system browser. The pending marker is promoted/removed only when that same speaker is independently found with a usable STR `/api/status` endpoint. On wizard completion show the separate STR installation link. Centralize device row click routing in a pure, JVM-tested policy and keep STR production behavior unchanged. Do not merge without verifying the existing device list plus Bose setup end to end on real hardware.

## D-033 — User-owned setup UI: screenshots, reliable insets, explicit completion and room semantics

**Decision:** Do not set `FLAG_SECURE` on a user-controlled setup assistant solely because it contains a masked password; users must be able to screenshot troubleshooting steps. Continue to mask passwords and never store/log access credentials. For a light edge-to-edge wizard, explicitly request dark Android status/navigation icons and respect system-bar insets. A time-consuming Wi-Fi selection must present an indeterminate visible progress state and finish it deterministically on success/error. The final completion action is `Fertig` only when POST and LAN-verification reach a terminal result; offer cancellation while still running. Automatic STR discovery must be labeled separately from the manual Bose Wi-Fi setup action. Grey Bose-only devices keep a visible, usable room selector with an isolated pending-room store; room survives WLAN reconfiguration, stale prior WLAN verification does not, and successful STR probing may carry the room across without overriding prior STR user choices. Keep 10dp visual separation between device rows. Preserve the established Wi-Fi popup and the accepted STR 0.2.1 release boundary.

## D-034 — Explicit SoundTouch compatibility matrix and accessible Wi-Fi credential entry

**Decision:** A shared Bose setup-mode button sequence does not imply identical firmware behavior or reverse-engineered Wi-Fi API support. Provide an explicit model choice on wizard page 1 rather than a hardcoded SoundTouch 20 Series III instruction. Default to the hardware-verified SoundTouch 20 III; label the SoundTouch 10 as **Testbetrieb** until its SoftAP capabilities, network scan, single-shot provisioning and independent LAN join are verified. Do not advertise other models as compatible merely because official Bose documents show the same Preset 2 + Volume − setup action. Keep three wizard pages, present visible high-contrast progress while scanning Bose-side networks, and mask the password by default with an accessible in-field reveal/hide control that does not clear or persist the value. Do not modify the already validated Android chooser, firmware POST semantics or existing STR discovery.

## D-035 — Four distinct wizard tasks; Finish is a verified terminal-state action

**Decision:** Model choice is a labeled, visibly selectable dropdown, not a radio group hidden by theme styling. Keep four explicit pages: prepare/model, connect to Bose SoftAP, select SSID from the approved popup (or manual entry), and enter masked password. An eye indicator must describe the actual *current* visibility state while the screen-reader label describes its action. A single send button starts the existing one-time Bose WLAN POST and navigates to a dedicated processing page. The result and STR handoff are separate from the processing page; a **Fertig** action is shown only when the LAN verification has produced a terminal outcome, including bounded timeouts clearly identified as unverified. Enforce this with testable state/presentation policy rather than arbitrary button enabling. Discovery retry must never resubmit WLAN credentials. Existing device listing, Wi-Fi picker, STR probing and the accepted 0.2.1 release must not be modified for this UX work.


## D-036 — Optional app update notification; avoid WebView double safe-area padding

**Decision (2026-10-30, BUILD-0013):** Adopt the successful `abfahrtsradar-android` GitHub Releases notification pattern, but keep STR Remote's platform dependencies small: a bounded HTTPS GET from `MainActivity` in a background thread plus a localized native dialog to open the official release in the external browser. Stable semantic versions only, numeric comparison, require the public release's expected APK, at most one successful check per 24 h; neither automatic install/download nor background notification service. Do not run this for the isolated debug `.poc` package.

To reclaim the highlighted top spacing, do not crop the whole STR screen or hide the upstream header. The upstream phone PWA's `body > header` adds `padding-top:calc(16px + env(safe-area-inset-top))` for browser chrome while Android's native frame already consumes system-bar insets. Inject a compact `padding-top:10px` guarded on known STR speaker/power DOM IDs. On unexpected upstream DOM or failed WebView load, do nothing; test real hardware and navigation before release. Existing signed v0.3.0 remains immutable.

**Hardware validation note:** SoundTouch 10 original-firmware WLAN provisioning succeeded using the user's separate test profile; subsequent development may show a scoped verified label but must not imply general firmware/WPA compatibility. See `doc/evidence/qa/2026-10-30-st10-original-wifi-test-profile.md`.


## D-037 — Community reports without implicit hardware compatibility claims

**Decision (2026-10-30):** The model dropdown shows `SoundTouch 20 III · geprüft`, `SoundTouch 10 · geprüft` and `Anderes SoundTouch-Modell`. Both named models have reported successful original-firmware WLAN provisioning on hardware; ST10 evidence comes from a separate WPA2 test profile. The unknown model reuses the same established one-time network API process, accompanied by a caution that the preset+volume setup entry may differ. Do not make automatic compatibility assertions.

Optional, explicit community feedback asks only for a manually entered model label and binary user-reported WLAN setup outcome. Never infer user success from POST acknowledgement or a possibly inconclusive LAN lookup. Use a distinct final UI page, plus an early-failure entry point, to avoid forcing long forms into the verified wizard pages. On user click open a **pre-filled, unsubmitted** GitHub issue draft on the fixed project origin. The user must manually submit it in GitHub. Warn about public disclosure and refrain from including SSID/password, device IDs, IP/MAC or technical traces. Repository Issues are enabled; no GitHub login/credential integration is installed. Only maintainer-reviewed corroborated evidence may justify later additions to the „geprüft“ catalog. Existing signed 0.3.0 and its tag remain immutable.


## D-038 — Email feedback without GitHub account and offline Bose model autocomplete

**Decision (2026-10-30, supersedes D-037's GitHub-Issue feedback channel):** Because GitHub issue authors need GitHub accounts, the optional unknown-model feedback uses an explicitly triggered Android email draft to `str-remote-android@gmx.org`. The draft contains user-entered SoundTouch model and yes/no success, plus installed STR Remote version and Android version/API level. The user must deliberately send in an external email application; STR Remote collects no mail credentials or device/network identifiers. If no email client is installed, explain the limitation in the wizard.

The model text entry uses a local `AutoCompleteTextView` backed by 35 curated historical SoundTouch-app-compatible device names from Bose support documentation. Support in the original Bose SoundTouch application does **not** establish compatibility with our local WLAN setup endpoint; optional, freely typed models remain supported. Community reports require review before adding verified status. The published v0.3.0 remains immutable.

## D-039 — Readable bounded model autocomplete and complete DE/EN Wi-Fi setup

**Decision (2026-10-30, BUILD-0013):** Fix the unreadable dark native suggestion popup by setting **both** the autocomplete window and dropdown row backgrounds to white with dark foreground. Limit suggestions to 190dp high with compact, accessible rows; keep offline matching and freeform model names. On the unknown-model preparation page remove duplicate explanatory warnings so actions fit on screen. On optional feedback show only model, Yes/No, brief privacy text and voluntary email action; hide unrelated technical diagnostics/status on that page.

Internationalize the full user-facing wizard using Android resource directories `values` (English fallback) and `values-de` (German), including model options/hints, setup instructions, WifiNetworkSpecifier errors, progress and terminal verification. Email feedback subject and body follow the app locale and still disclose the exact installed STR Remote and Android version. Do not change proven Bose network API semantics, existing 0.3.0 tag or release. Require real-phone screenshots and bilingual QA before merging.

## D-040 — Native bilingual About entry without prematurely choosing the new app name

**Decision (2026-10-30):** To satisfy the STR maintainer's Community Apps requirements, provide an always-accessible native About dialog from the app's existing top toolbar. Explain in DE/EN that the product is an **unofficial, independent community app** and is not affiliated with or endorsed by Bose Corporation or the SoundTouch Reborn project. State that Bose and SoundTouch are Bose Corporation trademarks. Show the app version derived from the installed package and offer explicitly tapped source, project-issue and email-contact actions; the email path must not require GitHub login or auto-send.

Do not silently choose a successor name, change `applicationId`, modify the signing identity or rewrite already-published 0.3.0 artifacts. Keep the UI as a platform-native information surface, not a new WebView/remote page. The safe-area/toolbar integration and both translations require phone QA. The app-name decision (STR-020) is independent and still PLANNED.
