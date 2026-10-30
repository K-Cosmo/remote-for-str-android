package app.strremote.android

/**
 * Presentation-only adaptation of Bose protocol diagnostics. Never transform
 * response strings before the provisioning/acknowledgement state logic reads them.
 */
internal object BoseTechnicalReportLocalization {
    private val translations = listOf(
        "Bose-Funktionsprüfung" to "Bose capability check",
        "Bose-Funktionen abrufbar" to "Bose capabilities available",
        "vom Gerät aufgelistet" to "advertised by device",
        "nicht aufgelistet" to "not advertised",
        "Nur eine Funktionsliste – kein Nachweis der Profilübertragung." to
            "Capability listing only – not proof of Wi-Fi transfer.",
        "auf dieser Firmware nicht auswertbar" to "cannot assess on this firmware",
        "unerwarteter Antworttyp" to "unexpected response type",
        "Antwort zu groß – abgebrochen" to "response too large – cancelled",
        "Bose capability check abgebrochen" to "Bose capability check cancelled",
        "Bose capability check nicht auswertbar" to "Bose capability check inconclusive",
        "Netzwerk- oder XML-Fehler" to "network or XML error",
        "WLAN-Übertragung" to "Wi-Fi transfer",
        "Übernahme nicht bestätigt; keine automatische Wiederholung" to
            "Transfer not confirmed; no automatic retry",
        "unerwartete Antwort" to "unexpected response",
        "Ergebnis unbestimmt" to "result uncertain",
        "nicht erneut senden" to "do not resend",
        "AddWirelessProfileResponse erkannt" to "AddWirelessProfileResponse received",
        "Nur API-Antwort; WLAN-Beitritt des Lautsprechers separat prüfen." to
            "API response only; verify actual speaker Wi-Fi connection.",
        "aber keine erwartete Bestätigung" to "but no expected acknowledgement",
        "Annahme unbestimmt; keinen weiteren POST ausführen." to
            "Acceptance unknown; do not submit another POST.",
        "Verbindung/Timeout/XML" to "connection/timeout/XML",
        "Lautsprecher im WLAN prüfen; nicht automatisch erneut senden." to
            "Check the speaker on Wi-Fi; no automatic resend.",
        "Bose WLAN-Suche ohne verwertbare Liste." to
            "Bose Wi-Fi scan returned no usable list.",
        "WLAN-Name kann manuell eingegeben werden." to
            "The network name can be entered manually.",
        "WLAN-Suche: Antwortgröße ungültig." to "Wi-Fi scan: invalid response size.",
        "HTTP 200, kein XML (Content-Type)" to
            "HTTP 200, non-XML response (Content-Type)",
        "HTTP 200, Bose bestätigt Scan ohne WLAN-Liste" to
            "HTTP 200, Bose confirms scan without a network list",
        "HTTP 200, Bose-Status ohne WLAN-Liste" to
            "HTTP 200, Bose status without a network list",
        "HTTP 200, unbekannter XML-Antworttyp" to
            "HTTP 200, unrecognized XML response",
        "HTTP 200, Bose meldet Scan-Fehler" to
            "HTTP 200, Bose reports a scan error",
        "HTTP 200, XML enthält keine WLAN-Einträge" to
            "HTTP 200, XML contains no Wi-Fi networks",
        "Android-XML-Parserkonfiguration fehlgeschlagen" to
            "Android XML parser configuration failed",
        "HTTP 200, XML-Syntax / DTD nicht akzeptiert" to
            "HTTP 200, XML syntax / DTD rejected",
        "HTTP 200, XML-Verarbeitung fehlgeschlagen" to
            "HTTP 200, XML processing failed",
        "XML-DTD / ungültige Kodierung zurückgewiesen" to
            "XML DTD / invalid encoding rejected",
        "abgebrochen" to "cancelled",
        "Netzwerk-/Antwortfehler" to "network/response error",
        "nicht erreichbar" to "unreachable"
    )

    fun translate(report: String, language: String): String {
        if (language.equals("de", ignoreCase = true)) return report
        var rendered = report
        for ((de, en) in translations) rendered = rendered.replace(de, en)
        return rendered.replace(
            Regex("Bose hat (\\d+) WLAN\\(s\\) gemeldet\\. Netzwerk auswählen\\.")
        ) { match -> "Bose reported ${match.groupValues[1]} networks. Select a network." }
    }
}
