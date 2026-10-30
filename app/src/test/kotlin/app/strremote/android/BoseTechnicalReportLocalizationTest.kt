package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseTechnicalReportLocalizationTest {
    @Test fun englishDiagnosticsAreReadableWithoutChangingProtocolTokens() {
        val raw = "Bose-Funktionsprüfung: HTTP 200, unerwarteter Antworttyp\n" +
            "WLAN-Übertragung: AddWirelessProfileResponse erkannt\n" +
            "Bose hat 4 WLAN(s) gemeldet. Netzwerk auswählen."
        val result = BoseTechnicalReportLocalization.translate(raw, "en")
        assertTrue(result.contains("Bose capability check"))
        assertTrue(result.contains("unexpected response type"))
        assertTrue(result.contains("Wi-Fi transfer: AddWirelessProfileResponse received"))
        assertTrue(result.contains("Bose reported 4 networks"))
        assertFalse(result.contains("WLAN-Übertragung"))
        assertEquals(raw, BoseTechnicalReportLocalization.translate(raw, "de"))
    }
}
