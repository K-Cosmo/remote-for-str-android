# Security

## Trust boundary

STR currently serves the phone UI over plain HTTP on the local network. STR Remote therefore permits the cleartext access needed for local STR endpoints. The LAN is the current trust boundary.

Because speakers can receive dynamic DHCP addresses, Android Network Security Configuration cannot practically enumerate the single permitted host ahead of time. The wrapper therefore combines the required cleartext base configuration with runtime WebView navigation restrictions.

## WebView baseline

- JavaScript is enabled because the upstream STR phone remote requires it;
- no `addJavascriptInterface()` bridge;
- file access disabled;
- content access disabled;
- Safe Browsing enabled (minSdk is 26, so no runtime API guard is needed);
- top-level in-app navigation is restricted to the currently selected speaker host on HTTP ports 8888/17008;
- other HTTP/HTTPS top-level links are handed to the system browser;
- subresource behavior remains owned by the upstream STR page to avoid reimplementing/breaking STR functionality;
- no analytics/tracking SDK;
- no Bluetooth permission unless STR Remote itself gains a documented Bluetooth feature; WebView/Chromium log noise is not sufficient justification.

## Lint policy

Security-related lint warnings are not globally disabled and no lint baseline is used for the first release. Intentional exceptions (JavaScript and cleartext LAN traffic) are suppressed only at the exact source/configuration point and are justified here and in the findings/decision registers.

## Change rule

Any relaxation of WebView navigation, cleartext scope, permission scope or local-network trust assumptions requires an explicit decision plus security evidence.
