# SoundTouch 10 original-firmware WLAN setup — hardware validation (2026-10-30)

## Scope and provenance

On 2026-10-30, the tester explicitly reported: **“Die Hardwarevalidierung der SoundTouch 10 für die WLAN-Ersteinrichtung mit dem Testprofil war erfolgreich.”**

This is **user-reported real-hardware QA evidence**, not an automatically collected diagnostics report. The user did not provide a firmware build, Wi-Fi SSID (which should remain private), security suite trace, resulting LAN IP or protocol captures. No such details are invented.

## Supported conclusion

- The WLAN setup workflow for SoundTouch 10 running original Bose firmware **worked with the separate test WLAN profile** on physical hardware.
- This is a separate achievement from the earlier working SoundTouch 10 STR 1.1.4 remote-control session.
- It is now reasonable for the development wizard model chooser to say **“SoundTouch 10 · mit Test-WLAN geprüft”** and to explain the profile-specific scope.
- Other firmware revisions, WPA2/WPA3 mixed modes, other routers and long-term reliability are **not** implied by this one successful profile.

## Follow-up UI gate

The same user provided a smartphone screenshot of the running STR Remote app and asked to remove redundant empty top spacing above the STR header controls. A separate in-app GitHub Releases update checker was requested. Both changes are tracked under [BUILD-0013](../../work/BUILD-0013.md).

The already published `v0.3.0` tag and signed APK remain unchanged.
