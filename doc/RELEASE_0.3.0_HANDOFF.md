# STR Remote 0.3.0 — final signed-APK and publication handoff

**State: COMPLETED — v0.3.0 accepted and published on 2026-10-30.** This document remains available as a reproducible, historical signing and publication playbook, not a pending request.

- Signed source commit: `5b351f04dc9f989eee48fc262764f3add8641573`
- Annotated release tag: `v0.3.0`, main and tag CI successful
- Signed APK SHA-256: `c4b1989e00ba21915e34192b2f4e0b73695416c93397e4f0c4d3cc6617a18290`
- Published [STR Remote 0.3.0](https://github.com/K-Cosmo/str-remote-android/releases/tag/v0.3.0)
- User confirmed signed APK smartphone acceptance: passed
- [Detailed release evidence](evidence/qa/2026-10-30-release-0.3.0.md)

## Source and CI

1. Merge the `release/v0.3.0-prep` PR into `main` only after the final combined CI run is successful.
2. On Windows, in the local repository:

   ```powershell
   git switch main
   git pull --ff-only
   git status --short
   .\gradlew.bat clean :app:assembleDebug :app:lintDebug :app:testDebugUnitTest :app:assembleRelease :app:lintRelease
   ```

3. Confirm `app/build.gradle.kts` identifies `versionName = "0.3.0"`, `versionCode = 11` and production application ID `app.strremote.android`. Do **not** sign or tag an uncommitted local modification.

## Reuse the existing v0.2.1 release certificate

Run the existing release script against the **same private keystore** that signed 0.2.1. The key and password must stay on the user's trusted local machine; do not upload them to GitHub, a chat, a build artifact or an Actions secret without a separate approved signing design.

```powershell
.\tools\build-signed-release.ps1 -ProjectRoot . -KeystorePath "C:\PRIVATE\str-remote-release.keystore" -KeyAlias "<existing-key-alias>"
```

**Replace the placeholder path and alias with the actual private values; do not store credentials in the repository.** The script prompts for the keystore passwords when signing.

The script now enforces the signing certificate's exact SHA-256 identity from v0.2.1:
`3cc2e7272d8c2d1433a57c4462df39adfe5e8c1931a4a924710c0e4087aa1e32`.

Expected artifacts in `dist\`:

- `STR-Remote-0.3.0.apk` — signed with the existing certificate
- `STR-Remote-0.3.0.apk.sha256.txt` — SHA-256 digest of the signed APK

The script validates v2/v3 signing and 16 KiB zipalign. Do **not** use `app-release-unsigned.apk` as a release asset.

**Build Tools 37 compatibility:** `apksigner verify --verbose --print-certs` may print `V2 Signer: certificate SHA-256 digest` or `V3.0 Signer: certificate SHA-256 digest` instead of the older `Signer #1 certificate SHA-256 digest`. The release script parses both and **rejects** missing, conflicting or unexpected certificates. It reads native output without `Out-String` so PowerShell's console width cannot split the 64-character digest. A standalone parser regression test is available via `pwsh -NoProfile -File .\tools\test-apksigner-certificate.ps1` and is run in GitHub CI. Java restricted-method warnings on stderr do not waive exit-code or certificate checks.


## Test the exact signed APK as an update

**Do not uninstall v0.2.1 or clear its data**: the upgrade must preserve existing speaker/room preferences.

```powershell
adb -d install -r .\dist\STR-Remote-0.3.0.apk
adb -d shell dumpsys package app.strremote.android
```

Confirm from Android App Info (or `dumpsys`) that version 0.3.0 (code 11) is installed and the existing STR Remote installation was updated in place.

Acceptance matrix:

- [ ] STR-enabled saved SoundTouch 10 remains online/reachable and the remote controls function
- [ ] Existing custom/preset room assignments survive update and app restart
- [ ] Discovery remains finite with no indefinite "STR-Verbindung wird geprüft" spinner
- [ ] New "WLAN eines Lautsprechers einrichten" action opens the four-step wizard on Android 10+
- [ ] SoundTouch 20 III Bose setup chooser works; WLAN popup, masked password/eye and one POST work as tested
- [ ] After transfer the result page appears only after a terminal LAN search; failures do not cause an automatic repeat POST
- [ ] Bose-only row is grey with "STR fehlt", retains its room and opens `https://st-reborn.de`
- [ ] Screenshots remain possible, Android status icons are visible and the wizard's "Fertig" appears only at the end
- [ ] Existing STR discovery/remote remains available on Android 8/9 (Bose setup action is unavailable on those systems)

**Note:** The preceding experimental debug APK has a separate `.poc` package ID. Its app-private Bose pending records are not automatically imported into the production package; this is intentional Android data isolation, not data loss from the 0.2.1 installation.

## Publish on GitHub only after the test

1. From the exact clean, accepted commit on `main`:

   ```powershell
   git status --short
   git rev-parse HEAD
   git tag -a v0.3.0 -m "STR Remote 0.3.0"
   git push origin v0.3.0
   ```

2. Verify tag CI is green. In [GitHub Releases](https://github.com/K-Cosmo/str-remote-android/releases), create the non-prerelease release **STR Remote 0.3.0** from tag `v0.3.0`.
3. Upload *exactly* `dist\STR-Remote-0.3.0.apk` and `dist\STR-Remote-0.3.0.apk.sha256.txt`. Confirm GitHub's resulting asset SHA-256 equals the local file.
4. Update `README.md` current public release, `doc/STATUS.md`, `doc/RELEASE_GATES.md` and release evidence only after GitHub publication is verified.

### Suggested GitHub release notes

**STR Remote 0.3.0 — SoundTouch WLAN einrichten**

- Guided Wi-Fi setup for original Bose SoundTouch firmware, before SoundTouch Reborn is installed.
- Four steps: choose model/prepare, connect to Bose, choose home Wi-Fi, enter password.
- Android 10+ system Wi-Fi selection, readable WLAN popup, password visibility control and automatic post-setup verification.
- Bose devices without STR shown grey with room assignment and a link to STR.
- More resilient existing STR discovery without indefinite progress.
- SoundTouch 20 III setup hardware-tested; SoundTouch 10 setup still **experimental**. STR remote remains supported on Android 8+.
- **Security:** Bose transfers the WLAN password over unencrypted HTTP on its temporary setup network. Use setup only in a trusted environment.

