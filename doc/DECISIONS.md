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

## D-019 — Make common speaker actions explicit and one-tap

**Decision:** the device list remains tap-to-connect. Row taps are handled explicitly so inline action controls cannot block connection/switching. Each row exposes an inline room action; assigning/changing a room implicitly saves the speaker. Saved speakers expose a visible delete icon with a confirmation dialog. Common actions must not depend on hidden long-press gestures.

## D-020 — Important guidance must be visually highlighted

**Decision:** explanatory/discovery guidance for the device screen should not hide as tiny footer text. Important hints and state messages use highlighted panels with clearer typography so non-technical users notice them quickly.

## D-021 — Device management must always offer an obvious way back to the remote

**Decision:** when a speaker is already active, entering the device view changes the toolbar action from `Geräte` to `Fernbedienung`. The user must not need to re-select the active speaker or rely on Android back navigation merely to return to the remote.

## D-022 — Deleting a saved discovered speaker hides it only for the current device view

**Decision:** deletion removes saved metadata and immediately removes the row from the current list. Because discovery is automatic, a fresh device search may legitimately find the physical speaker again; the confirmation text must say so.

## D-023 — Dismiss help text explicitly, not by hidden gesture

**Decision:** the highlighted device-management hint uses a visible `Hinweis ausblenden` / `Hide this hint` text link. Do not use swipe-to-dismiss or a trash icon for help text because those interactions are hidden or semantically ambiguous. Dismissal is remembered in app-private preferences.
