# BUILD-0015 — first Google Play internal-test AAB candidate

## Scope and provenance (2026-10-31)

This **draft-only** workstream stages a minimal version bump from published GitHub `v0.3.2` to internal-test candidate `0.3.3`, `versionCode=14`, **without functional app code changes**. It is **not** authorization to merge, publish a GitHub release, upload a Play AAB or roll out any track.

- Existing public signed GitHub APK `0.3.2` uses package `app.strremote.android`, versionCode `13`, and historical app-signing certificate SHA-256 `3CC2E7272D8C2D1433A57C4462DF39ADFE5E8C1931A4A924710C0E4087AA1E32`.
- **Owner-reported Google Play evidence (2026-10-31):** Google displays the **same** app-signing SHA-256 after PEPK import. The assistant has not accessed the owner's Play Console or inspected any private key.
- Separate **upload key** was reportedly generated during the owner's chosen PEPK workflow. Its actual certificate registration/fingerprint has **not** yet been evidenced and must be verified separately.
- The Play AAB must be built/signed **locally** with the dedicated **upload key**, not the GitHub app-signing key and not the debug/`.poc` identity. Play will sign delivered APKs with the enrolled *app-signing* key; verify this on an installed test device.
- AAB signing credentials, certificates containing private keys, passwords and encrypted PEPK export archives **must never** be committed to this repository, attached to issues/PRs or sent to AI tools.

## Read-only upload certificate validation — before preparing AAB

Check the **Upload key certificate** SHA-256 on Play Console **App signing** page. Compare it to the locally generated upload certificate; it is **expected to differ from** the public GitHub **app-signing** certificate.

In a *local PowerShell* session using the existing protected upload keystore (example paths only):

```powershell
$UploadKeystore = "C:\path\outside-repository\remote-str-upload.jks"
$UploadAlias = "remote-str-upload"
$UploadCert = "C:\path\outside-repository\upload_certificate.pem"

keytool -list -v -keystore "$UploadKeystore" -alias "$UploadAlias"
if ($LASTEXITCODE -ne 0) { throw "Cannot read upload-keystore alias." }

keytool -printcert -file "$UploadCert"
if ($LASTEXITCODE -ne 0) { throw "Cannot read public upload certificate." }
```

Only compare/share the **public** SHA-256 fingerprints, not passwords or keys. If the upload certificate is not yet enrolled with Google, follow Google's **upload-certificate registration** flow first; do not upload a bundle assuming enrollment succeeded.

## Local candidate build (after separate approval)

1. Fetch this PR branch at its approved exact source commit. Confirm the clean working tree and `app/build.gradle.kts`: `applicationId=app.strremote.android`, `versionName=0.3.3`, `versionCode=14`. Confirm GitHub CI for this exact commit passed.
2. In **Android Studio → Build → Generate Signed Bundle / APK → Android App Bundle → app → Next**, select the **dedicated upload-keystore**, its upload alias, and **release** build type. Let Android Studio ask for key/store passwords locally. **Never** put passwords into tracked `build.gradle.kts`, `gradle.properties`, project files or command arguments.
3. Android Studio produces a release `.aab` (typically `app/release/app-release.aab`). Copy it to a private distribution directory **outside the git checkout** and record the candidate source SHA and time.
4. Verify the JAR-signed **AAB** (it is **not** an APK; do not apply `apksigner` or APK `zipalign` to an AAB):

```powershell
$Aab = "C:\path\outside-repository\app-release.aab"
jarsigner -verify -strict -verbose "$Aab"
if ($LASTEXITCODE -ne 0) { throw "AAB JAR-signature verification failed." }

keytool -printcert -jarfile "$Aab"
if ($LASTEXITCODE -ne 0) { throw "Cannot read AAB signer certificate." }

Get-FileHash -Algorithm SHA256 -LiteralPath "$Aab"
```

5. **STOP** if the AAB signer SHA-256 does not match the **registered Play upload certificate**, if `versionCode` is not 14, if the bundle is unsigned, if tests fail, or if there is no explicit owner approval to upload.
6. Following approval, create the Play **internal test** using the signed AAB and opt in the authorized tester(s). Do not confuse internal testing with the 12-person/14-day qualifying **closed** test; the two tracks have separate eligibility and enrollment.
7. On an Android test phone with **already installed GitHub-signed 0.3.2**, enroll in the Play internal track and install the **versionCode 14** update without uninstall/data clearing. Check that the installed **app-signing certificate** matches the original GitHub one; saved speakers/rooms and STR controls remain functional. Record positive and negative tests, as applicable.
8. Only after this acceptance consider a separately authorized closed-test rollout and production-access request. Any later GitHub/Play dual-channel versionCode strategy must avoid track downgrades and skipped data migrations.

## Explicit negative claims

- The owner-supplied Play SHA-256 is proof of **reported enrolled app-signing certificate identity**, not of the distinct **upload certificate** or installation outcome.
- No AAB has been built, cryptographically inspected, published or handed to Google as part of this PR.
- No internal testers, closed testers or production eligibility have been observed.
- Public GitHub `v0.3.2`, its SHA-256, signed binaries, tag and `main` remain unchanged by this branch.

**Related:** [current status](../STATUS.md), [version gates](../RELEASE_GATES.md), [public 0.3.2 provenance](../evidence/qa/2026-10-31-release-0.3.2.md), [parallel Play submission planning PR #10](https://github.com/K-Cosmo/remote-for-str-android/pull/10).

**Official Google sources:** [Play App Signing](https://developer.android.com/studio/publish/app-signing) · [Android app signing via Studio](https://developer.android.com/studio/publish/app-signing#sign-apk) · [Play internal and closed tests](https://support.google.com/googleplay/android-developer/answer/9845334?hl=de).
