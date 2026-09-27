# Plan — 007 Discovery & Network Resilience

1. Record the Wi-Fi-without-Internet product rule in `/doc`.
2. Add Wi-Fi transport detection using Android platform APIs only.
3. Add a finite discovery timer owned by `MainActivity`.
4. Extend the existing single discovery-state UI with Retry and STR-help actions.
5. Track endpoint probe completion so "checking" and "unreachable" are distinct.
6. Guard against stale probe callbacks across discovery retries.
7. Update German/English strings.
8. Bump to 0.1.1 / versionCode 8.
9. Run doc consistency, build/lint/release assembly and the focused real-device matrix.
