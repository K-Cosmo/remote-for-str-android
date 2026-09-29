# Plan — 009 Speaker Management

1. Extend `PreferencesStore` from one last endpoint to last endpoint plus an explicit saved-speaker collection.
2. Keep serialization inside the platform layer; do not add Room, Gson/Moshi or another persistence dependency.
3. Introduce one normalized speaker identity used to de-duplicate saved and discovered entries.
4. Extend the existing device list model so a row can represent saved, discovered and current state without creating a second competing list architecture.
5. Add explicit save/remove actions and current-speaker indication.
6. Allow successful manual endpoints to participate in the same saved-speaker model.
7. Keep endpoint probing and switching on the existing `EndpointProbe`/`loadEndpoint` path.
8. Update German/English strings.
9. Bump to 0.2.0 / next versionCode when implementation starts.
10. Run doc consistency, clean debug/lint/release assembly and the focused device matrix.
