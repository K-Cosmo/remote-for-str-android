# Google Play — preparation and controlled first submission (October 2026)

**State: PREPARATION / NO AAB UPLOADED / NOT ON GOOGLE PLAY.** This is a **submission runbook and proposed listing**, not an accepted signing/key decision or proof of Play review.

## Observed state and constraints

- **Owner-confirmed Play Console observation (2026-10-31):** `app.strremote.android` is shown as **package name available**. The project dashboard offers **internal testing**; for production Google displays the requirements to complete setup, perform a closed test and apply for production access. This is an owner report, not independent Play-account access. Package availability is **not** verification of signing enrollment, first uploaded bundle, production eligibility or a live Play listing.
- **Public GitHub baseline:** [Remote for STR 0.3.2](https://github.com/K-Cosmo/remote-for-str-android/releases/tag/v0.3.2), `applicationId=app.strremote.android`, `versionCode=13`, `versionName=0.3.2`; historical GitHub release signing-certificate SHA-256 `3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`.
- **Android project:** `minSdk=26` (Android 8+), `targetSdk=37` (beyond Google's 2026 mobile submission requirement of API 36+), `compileSdk=37`; Wi-Fi onboarding additionally requires Android 10+.
- **Owner approval:** The prior approval covered the **public GitHub v0.3.2** merge/release. It did not authorize any first Play App Signing enrollment, private-key export, Play AAB upload, internal/closed track rollout or production rollout.

## First Play Console steps (safe to prepare before AAB)

1. On the app Dashboard, check **app identity** and whether a Play application record is already present; the owner observed package availability. Confirm `Remote for STR`, application (not game), free, DE default plus EN translation; preserve original package name. Review account identity/contact completion. Never copy personal verification details to git.
2. Fill the **main store listing** using the DE/EN proposals below. Prepare public *support email* in the Play Console, where disclosure is expected; a public storefront support contact cannot be protected by hiding an address from the app repository. Keep complete address literals out of public git Markdown/Android source, per D-045.
3. Draft policy forms under **App content** (privacy URL, Data safety, content rating, target audience, ads, permissions, any applicable access instructions). No Data safety answers may be marked verified until the Android **and embedded STR WebView** traffic is audited.
4. Prepare real screenshots with private IPs, speaker IDs, email addresses and home-network names removed; use native project branding, never pretend to be official Bose or STR. Gather a **512×512** high-resolution Play icon (PNG) and **1024×500** feature graphic (PNG/JPEG) to Google's specifications. These are separate from Android launcher resources.
5. Before first internal release, perform the P0 signing decision below. **No first bundle upload** until the owner has approved the signing route and versionCode.

## P0 — Play App Signing without splitting installed devices

Android checks **package name and signing certificate** for ordinary in-place updates. The public GitHub 0.3.2 APK is already signed with the historical key. Google's normal new-app flow generates a *different* Play app-signing key unless the developer deliberately chooses **Change app signing key → Export and upload** to enroll the **existing** key. A new app-signing key with the same `applicationId` will block seamless updates of preexisting GitHub installs. [Official Android documentation](https://developer.android.com/studio/publish/app-signing).

**Key roles and hold point:**

- **App-signing private key:** original locally protected release key used for public GitHub APKs; may need secure PEPK export to Google Play during first enrollment if continuity is chosen. This is sensitive and potentially irreversible; do **not** upload key files, passwords or export archives to GitHub or an issue.
- **Upload key:** preferably a separate new protected key used to sign future **AAB** uploads. Play validates the upload signature and signs delivered device APKs with the configured *app-signing key*. A successfully signed AAB alone **does not prove** the eventual installation certificate.
- **Stop condition:** the Play Console first-release flow cannot accept the existing original app-signing key, certificate differs, or owner has not explicitly approved key transfer → stop before enrollment or upload; document the incompatibility for a separate decision. **Do not generate a Play signing identity by default**.

**Version strategy:** v0.3.2 is already published with `versionCode=13`. A Play-delivered candidate intended to update that installation should be a newly identified candidate with `versionCode >=14`, from a reviewed code commit, with traceable signed AAB, bundle SHA-256, local tests and separate Play track approval. Do not republish the current v0.3.2 APK as a different build or change published history. Do not silently sign with a debug or `.poc` package.

**Acceptance experiment:** on a test handset install signed public GitHub 0.3.2 preserving its rooms/speakers; then opt into the owner-approved Play **internal test**, install the higher-code Play build *without uninstall*, verify the installed package/signer and preserved data, exercise STR controls and restart. Only this proves the intended channel transition on a device. Do not claim it passed before testing.

## Proposed store listing — DE (to review in Play Console)

**App-Name (max. 30):** Remote for STR

**Kurze Beschreibung (max. 80):** Fernbedienung für SoundTouch Reborn – Geräte im WLAN finden und steuern.

**Vollständige Beschreibung (max. 4.000):**

Remote for STR ist eine unabhängige, kostenlose Android-Fernbedienung für Lautsprecher, auf denen SoundTouch Reborn (STR) läuft.

Verbinde dein Smartphone mit dem WLAN deiner Lautsprecher und entdecke STR-Geräte im lokalen Netzwerk. Wähle einen Lautsprecher aus und bediene die von STR bereitgestellte Fernbedienungsoberfläche direkt in der App.

Funktionen:
- STR-Lautsprecher im lokalen WLAN finden und verbinden.
- Geräte speichern, Räumen zuordnen und zwischen ihnen wechseln.
- Wiedergabe, Lautstärke und Favoriten über die vorhandene STR-Oberfläche steuern.
- Zuletzt verwendeten Lautsprecher wieder aufrufen.
- Optionaler WLAN-Einrichtungsassistent für unterstützte SoundTouch-Lautsprecher mit Original-Firmware (Android 10 oder neuer; nicht für alle Modelle geprüft).

Voraussetzung für die Fernbedienung ist eine vorhandene SoundTouch-Reborn-Installation auf dem Lautsprecher. Der WLAN-Assistent installiert STR nicht. Die App benötigt kein eigenes Benutzerkonto und verwendet keine Analyse-SDKs. Der optionale Versionscheck fragt GitHub ab; Feedback-E-Mails werden nur nach deinem ausdrücklichen Aufruf im E-Mail-Programm vorbereitet und nicht automatisch gesendet.

Hinweis zum WLAN-Assistenten: Die Original-Firmware überträgt das WLAN-Passwort bei der Einrichtung auf dem temporären Lautsprecher-WLAN über unverschlüsseltes HTTP. Verwende die Funktion nur in einer vertrauenswürdigen Umgebung.

Remote for STR ist ein unabhängiges, inoffizielles Community-Projekt. Es steht weder mit Bose noch mit dem SoundTouch-Reborn-Projekt in geschäftlicher Verbindung und wird von ihnen nicht unterstützt. Bose und SoundTouch sind Marken der Bose Corporation.

## Proposed store listing — EN (to review in Play Console)

**App name (max. 30):** Remote for STR

**Short description (max. 80):** Find and control SoundTouch Reborn speakers on your local Wi-Fi network.

**Full description (max. 4,000):**

Remote for STR is a free, independent Android companion remote for speakers running SoundTouch Reborn (STR).

Connect your phone to the same Wi-Fi network as your speakers, discover STR-enabled devices, and open the existing STR remote interface directly in the app.

Features:
- Find and connect to STR speakers on your local network.
- Save speakers, assign rooms, and switch between devices.
- Use STR's existing playback, volume, and preset controls.
- Reconnect to your last-used speaker.
- Optionally prepare Wi-Fi on supported original-firmware SoundTouch speakers (Android 10 or later; compatibility has only been tested in specific configurations).

STR must already be installed on the speaker for remote-control features. The Wi-Fi assistant does not install STR. The app requires no separate user account and uses no analytics SDKs. An optional version check contacts GitHub. Feedback messages are only composed in your email app when you explicitly request them and are never sent automatically.

Wi-Fi setup notice: Original Bose firmware receives the Wi-Fi password over unencrypted HTTP on its temporary setup network. Only use this feature in a trusted environment.

Remote for STR is an independent, unofficial community project. It is not affiliated with or endorsed by Bose Corporation or the SoundTouch Reborn project. Bose and SoundTouch are trademarks of Bose Corporation.

## Content and Data safety — unresolved review matrix

| Topic | Evidence in current repository | Play declaration task |
|---|---|---|
| Internet and local LAN access | STR WebView uses local HTTP; optional GitHub version request uses HTTPS | Verify actual endpoints/data types; accurately explain unencrypted LAN provisioning |
| Android permissions | Internet, network state, location permission up to API 32, Nearby Wi-Fi Devices, local network on current target | Check merged **release AAB manifest** and the applicable Play permissions questionnaire; never assert physical location tracking |
| Saved speaker/room data | Stored in local Android preferences; `allowBackup=true` in manifest | Audit Android cloud/transfer backups before stating **local-only** absolute guarantees or Data safety status |
| STR WebView | Third-party/upstream STR content can create its own network requests | Review effective WebView data collection/transfer and whether developer controls delivered WebView behavior under Google's policy |
| GitHub update checker | Public GitHub HTTPS request once per 24 h after success; service may receive IP/network metadata | Evaluate whether any policy data categories apply and the accuracy of the public privacy notice |
| Feedback email | User-triggered `ACTION_SENDTO` draft with model/outcome/app and Android version; no auto-send | Classify Google Data safety exceptions for explicit user-initiated action only after verification |
| External third-party/data deletion | No user account, analytics or developer backend declared in app docs | Confirm actual STR/WebView and email-client boundaries; publish a reachable public privacy URL |

**Do not automatically answer “no data collected/shared” simply because Remote for STR has no own backend.** Google's rules explicitly cover libraries and developer-controlled embedded WebViews; user-initiated transmission and strictly local processing have distinct exceptions. [Official Data safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469?hl=de).

**Privacy URL:** `https://github.com/K-Cosmo/remote-for-str-android/blob/main/doc/PRIVACY.md` is a public starting point, **not yet confirmed accepted by Play**. Before submission, remove outdated 0.3.2 “in development” sections, correct claims about backup and email recipient, and ensure the page is public and permanently reachable; consider stable GitHub Pages. Public Play support contact is a separate requirement.

## Tests and production eligibility

- Internal testing is available as an **optional** rapid first release for a small trusted group. It is **not** a substitute for the closed-test production gate.
- The Play Console message explicitly requires the app's setup, a closed test and application for production access. For new private developer accounts opened after 2023-11-13, Google specifies **12 continuously opted-in testers for at least 14 consecutive days**; a tester invitation alone does not count. Confirm participants really opted in, actively exercise the app and collect meaningful feedback. [Official closed-test policy](https://support.google.com/googleplay/android-developer/answer/14151465?hl=de).
- Complete required Play setup/review tasks before starting the qualifying closed test, then apply for production access; **production requires a separate owner-approved rollout**.
- Internal/closed/production distribution actions are not approved by this document. The existing public GitHub APK remains unchanged.

## Authoritative source links

- [Create/set up app and store metadata](https://support.google.com/googleplay/android-developer/answer/9859152?hl=de)
- [Google Play signing and existing key export](https://developer.android.com/studio/publish/app-signing)
- [Google Play personal-account testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=de)
- [Data safety disclosure](https://support.google.com/googleplay/android-developer/answer/10787469?hl=de)
- [Store imagery specifications](https://support.google.com/googleplay/android-developer/answer/9866151?hl=de)
- [2026 target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=de)

Current documentation authority remains [Governance](GOVERNANCE.md), [Backlog](BACKLOG.md), [Planned](PLANNED.md), [Privacy](PRIVACY.md) and [Release gates](RELEASE_GATES.md). This checklist does not replace their normative requirements or create accepted architectural decisions.
