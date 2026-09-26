# Static review — 2026-09-26

**Build:** 0.1.1-dev / BUILD-0001  
**Evidence class:** static review only; not Android build/runtime evidence.

Checks performed in the artifact environment:

- all 12 Android XML files parse successfully;
- all discovered `R.string` / `@string` references resolve against the base string resources;
- local Markdown links resolve;
- active build files no longer contain `android.useAndroidX=false`;
- active build/CI configuration references AGP 9.4.1 and Gradle 9.7.1;
- the public `MainActivity` no longer publicly implements the callback method that exposed internal `SpeakerCandidate`;
- required `/doc` governance files are present.

Limitations:

- no Android SDK is installed in this artifact environment;
- no authoritative `assembleDebug`, `lintDebug`, APK install, mDNS discovery or real STR speaker run was possible here;
- local Gradle daemon JVM major version is not present in the supplied build log and therefore was not guessed.
