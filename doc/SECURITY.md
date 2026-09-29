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
