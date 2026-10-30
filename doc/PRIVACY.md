# Privacy

STR Remote does not operate a server, create user accounts, collect analytics or transmit telemetry.

The app accesses the local network to discover and connect to SoundTouch Reborn speakers. The embedded STR web application may perform its own network requests required by STR features; STR Remote does not collect those requests.

STR Remote stores local data in Android app-private preferences. Current 0.2.0 data can include:

- the last successful speaker endpoint;
- explicitly saved speaker host/port/display/model/discovery metadata;
- optional built-in or custom room assignment;
- local UI preferences such as whether the device-management help hint was dismissed.

A custom room name is stored exactly as local user-entered text. Room/UI metadata is presentation-only and is not used for speaker authentication, WebView security or playback behavior.

Saved-speaker, room and UI-preference data is not uploaded or cloud-synced by STR Remote.

Removing application data or uninstalling STR Remote removes this app-private metadata.


## Bose Wi-Fi setup in 0.3.0

The optional setup assistant uses the phone's Android-approved connection to the temporary Bose setup Wi-Fi. The selected SSID and password are passed directly from this app to the speaker's **original Bose firmware** over local, unencrypted HTTP. They are not sent to STR Remote servers or analytics services; STR Remote runs no such servers. The Wi-Fi password is kept only for the active attempt and is not written to preferences or logs.

After the attempt, this app may locally retain a Bose device identifier, verified local IP address, setup status and user-assigned room to display the speaker separately from STR-enabled devices until STR becomes reachable. The selected Wi-Fi password is never part of that record. Other platform-level backup policies remain governed by Android settings.


## Planned optional update check (BUILD-0013, not in released 0.3.0)

Once implemented and distributed, the Android app will query GitHub's **public** `api.github.com/repos/K-Cosmo/str-remote-android/releases/latest` endpoint over HTTPS on app start, at most once per 24 h after a successful response. This requires a direct request to GitHub: GitHub can observe the requesting IP address, standard network information and the minimal `STR-Remote-Android` User-Agent. The app sends no speaker identifier, Wi-Fi SSID, password, usage analytics, account ID or device telemetry. The result is only used to show a local release notification; the last successful check timestamp is kept in app-private preferences. The app does not fetch or install APK files automatically.
