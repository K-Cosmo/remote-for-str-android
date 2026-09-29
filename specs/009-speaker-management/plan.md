# Plan — 009 Speaker Management

1. Extend `PreferencesStore` from one last endpoint to last endpoint plus an explicit saved-speaker collection.
2. Keep serialization inside the platform layer using SharedPreferences and Android-provided JSON; do not add Room, Gson/Moshi or another persistence dependency.
3. Introduce one normalized speaker matching rule for saved/discovered de-duplication.
4. Add optional room metadata to saved speakers. Built-in rooms use stable IDs: `living_room`, `bedroom`, `kitchen`, `bathroom`, `kids_room`, `garden`.
5. Localize built-in room labels at display time and allow a custom free-text room label.
6. Extend the existing device list model so one row can represent saved, discovered and current state without creating a second competing list architecture.
7. Handle row taps explicitly for connect/switch, add an inline Room button for save + room assignment/change, and a visible delete icon with confirmation for saved speakers. Deletion hides the current row until the next fresh scan; common actions do not rely on long-press gestures.
8. Keep an explicit `Fernbedienung` toolbar return path while the device view is open and a speaker is active.
9. Present device name + room prominently, with model/version and IP as secondary information.
10. Allow successful manual endpoints to participate in the same saved-speaker and room model.
11. Keep endpoint probing and switching on the existing `EndpointProbe`/`loadEndpoint` path.
12. Preserve 0.1.1 Wi-Fi/discovery/error handling and WebView host/port restrictions.
13. Update German/English strings.
14. Bump to 0.2.0 / versionCode 9.
15. Run doc consistency, clean debug/lint/release assembly and the focused device matrix.
