# 007 — Discovery & Network Resilience

## Problem

STR Remote 0.1.0 can enter discovery without an available Wi-Fi connection and leave an indeterminate spinner. A normal scan with no STR device also lacks a finite terminal state.

## Required behavior

- automatic local probing/discovery requires an available Wi-Fi transport;
- Internet validation is not required;
- missing Wi-Fi starts no saved-endpoint probe or mDNS scan and shows same-Wi-Fi guidance;
- discovery ends after 10 seconds;
- no discovered STR service shows prerequisite/help plus Retry/manual-host paths;
- a discovered speaker whose STR endpoint is unreachable is a distinct state;
- Retry can recover after Wi-Fi becomes available without restarting the app;
- normal discovery, endpoint fallback, persistence, WebView navigation restrictions and controls remain unchanged.

## Acceptance

The applicable BUILD-0007 cases in `doc/TESTS.md` and `doc/RELEASE_GATES.md` pass with real build/device evidence.
