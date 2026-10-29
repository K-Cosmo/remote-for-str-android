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
