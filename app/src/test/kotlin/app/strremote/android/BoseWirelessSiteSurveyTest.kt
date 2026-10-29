package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseWirelessSiteSurveyTest {
    @Test
    fun parsesCommunitySurveyAndPrefersStrongestDuplicate() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <PerformWirelessSiteSurveyResponse error="none">
              <items>
                <item ssid="WeakNet" signalStrength="-80" secure="true">
                  <securityTypes><type>wpa_or_wpa2</type></securityTypes>
                </item>
                <item ssid="StrongNet" signalStrength="-45" secure="true">
                  <securityTypes><type>wpa2aes</type></securityTypes>
                </item>
                <item ssid="StrongNet" signalStrength="-69" secure="true">
                  <securityTypes><type>wpa2aes</type></securityTypes>
                </item>
              </items>
            </PerformWirelessSiteSurveyResponse>
        """.trimIndent().toByteArray()
        val result = BoseWirelessSiteSurvey.parseResponse(xml)
        assertEquals(listOf("StrongNet", "WeakNet"), result.candidates.map { it.ssid })
        assertTrue(result.candidates.all { it.selectable })
        assertEquals(-45, result.candidates[0].signalDbm)
    }

    @Test
    fun listsButDoesNotAllowOpenAndUnsupportedSecurity() {
        val xml = """
            <PerformWirelessSiteSurveyResponse error="none">
              <items>
                <item ssid="PublicGuest" secure="false" signalStrength="-51" />
                <item ssid="WPA3Only" secure="true" signalStrength="-49">
                  <securityTypes><type>wpa3sae</type></securityTypes>
                </item>
                <item ssid="TestWPA2" secure="true" signalStrength="-70">
                  <securityTypes><type>wpa_or_wpa2</type></securityTypes>
                </item>
              </items>
            </PerformWirelessSiteSurveyResponse>
        """.trimIndent().toByteArray()
        val result = BoseWirelessSiteSurvey.parseResponse(xml)
        assertEquals(3, result.candidates.size)
        assertEquals("TestWPA2", result.candidates.first().ssid)
        assertTrue(result.candidates.first().selectable)
        assertFalse(result.candidates[1].selectable)
        assertFalse(result.candidates[2].selectable)
    }

    @Test
    fun rejectsDoctypeWrongRootOrErrorResult() {
        val responses = listOf(
            "<!DOCTYPE x [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><PerformWirelessSiteSurveyResponse>&x;</PerformWirelessSiteSurveyResponse>",
            "<status>/performWirelessSiteSurvey</status>",
            "<PerformWirelessSiteSurveyResponse error=\"failed\"/>",
            "<PerformWirelessSiteSurveyResponse"
        )
        for (xml in responses) {
            assertTrue(BoseWirelessSiteSurvey.parseResponse(xml.toByteArray()).candidates.isEmpty())
        }
    }

    @Test
    fun ignoresBlankAndOversizedSsid() {
        val longName = "x".repeat(70)
        val xml = """
            <PerformWirelessSiteSurveyResponse><items>
              <item ssid="" secure="true"/>
              <item ssid="$longName" secure="true"/>
              <item ssid="TestValid" secure="true"><securityTypes>
                <type>wpa_or_wpa2</type>
              </securityTypes></item>
            </items></PerformWirelessSiteSurveyResponse>
        """.trimIndent().toByteArray()
        assertEquals(
            listOf("TestValid"),
            BoseWirelessSiteSurvey.parseResponse(xml).candidates.map { it.ssid }
        )
    }
}
