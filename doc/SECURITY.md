# Security

## Trust boundary

STR currently serves the phone UI over plain HTTP on the local network. STR Remote therefore permits the cleartext access needed for local STR endpoints. The LAN is the current trust boundary.

Because speakers can receive dynamic DHCP addresses, Android Network Security Configuration cannot practically enumerate the single permitted host ahead of time. The wrapper combines the required cleartext base configuration with runtime WebView navigation restrictions.

## WebView baseline

- JavaScript is enabled because the upstream STR phone remote requires it;
- no `addJavascriptInterface()` bridge;
- file access disabled;
- content access disabled;
- Safe Browsing enabled;
- top-level in-app navigation restricted to the currently selected speaker host on HTTP ports 8888/17008;
- other HTTP/HTTPS top-level links handed to the system browser;
- subresource behavior left to the upstream STR page to avoid reimplementing/breaking STR functionality;
- no analytics/tracking SDK;
- no Bluetooth permission unless STR Remote itself gains a documented Bluetooth feature.

Chromium/WebView hidden-API or Bluetooth warnings are not a sufficient reason to broaden permission scope.

## Local speaker metadata

Saved-speaker entries, room assignments and UI preferences are app-private local metadata.

They do not relax:

- speaker identity rules;
- endpoint probing;
- selected-host/port WebView restrictions;
- the LAN trust boundary.

A room label must never be treated as an authentication or routing attribute.

## Network eligibility

Automatic discovery/probing requires Wi-Fi transport but not validated Internet access. This is a local-network eligibility rule, not an authentication mechanism.

## Lint/security policy

Security-related lint warnings are not globally disabled and the project uses no lint baseline. Intentional exceptions such as required JavaScript and cleartext LAN traffic are narrowly scoped and documented.

## Change rule

Any relaxation of WebView navigation, cleartext scope, permission scope, persistence sensitivity or local-network trust assumptions requires an explicit decision plus security/runtime evidence before acceptance.


## Bose original-firmware Wi-Fi provisioning (0.3.0)

The optional setup wizard on Android 10+ temporarily uses an Android system-consented Bose SoftAP `Network`, with Bose HTTP requests explicitly scoped to that `Network`. It does not change the app process's default routing or add a native WebView bridge.

**Bose's original `/addWirelessProfile` API accepts SSID/password over plain HTTP on the temporary setup network.** There is no TLS confidentiality against an adversary able to observe or access that network. The wizard warns before the single explicit user-triggered transfer. Never describe this as encrypted or offer this feature as safe on a hostile/shared setup network. WPA2 compatibility is currently constrained; WPA3-only and unsupported radio/security modes must not be silently claimed supported.

The app masks the password by default, provides an intentional reveal control, clears the password field after submission, does not log or persist the credential, and does not retry ambiguous writes. The user may take screenshots; an explicitly revealed password may be visible in them.

Subsequent read-only LAN verification requires that the discovered device's Bose `deviceID` match the identity from setup mode. An optional active SSID check can strengthen this proof. A timeout does not mean the transfer failed. STR itself is only considered installed when its local status endpoint responds.

Bose device identity, optional LAN IP, setup-verification state and room label are stored in app-private preferences solely to represent speakers awaiting STR; they are not STR login credentials.


## GitHub release notification (BUILD-0013, pending next signed release)

The new version checker only accesses the fixed `https://api.github.com/repos/K-Cosmo/str-remote-android/releases/latest` over TLS, with bounded read size and timeouts. Only a strictly parsed `vMAJOR.MINOR.PATCH` newer than installed is eligible; GitHub draft/prerelease versions, incomplete releases without matching named APK asset and malformed tags are ignored. Any displayed release link is synthesized from the validated tag and fixed `github.com/K-Cosmo/str-remote-android` path, not taken from arbitrary remote JSON fields. User decides whether to open the external browser. No automatic APK download, package installation, new Android permission or sensitive network data is introduced.

For the embedded STR WebView, static CSS is injected only into the already permitted local STR page after an exact upstream `#dev` and `#powerBtn` DOM check. No JavaScript bridge or access to other domains is added; STR controls remain unchanged.


## Community issue feedback boundaries (BUILD-0013)

The experimental “Anderes SoundTouch-Modell” shares the exact one-shot Bose provisioning process; a compatible setup-mode button sequence or matching original API must not be assumed for arbitrary hardware. All claims are user-supplied and do not directly change compatibility status. GitHub issue drafts are composed using only a strictly validated 2–60-character model label and a consciously selected success/failure outcome, and the URL is built with Android `Uri.Builder` on the fixed `github.com/K-Cosmo/str-remote-android/issues/new` path (not user-controlled redirects). No identity, SSID, credentials, LAN endpoint or diagnostics are attached. Nothing is submitted without additional user action on GitHub.
