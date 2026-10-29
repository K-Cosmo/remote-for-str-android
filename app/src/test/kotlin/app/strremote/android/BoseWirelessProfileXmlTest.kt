package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BoseWirelessProfileXmlTest {
    @Test
    fun buildsCommunityCandidateFormat() {
        val xml = BoseWirelessProfileXml.buildCandidate("TestNetwork", "Fictitious123")
        assertEquals(
            "<AddWirelessProfile><profile ssid=\"TestNetwork\" password=\"Fictitious123\" " +
                "securityType=\"wpa_or_wpa2\" /></AddWirelessProfile>",
            xml
        )
    }

    @Test
    fun escapesAllXmlAttributeMetacharacters() {
        val xml = BoseWirelessProfileXml.buildCandidate("Test&\"'<Net>", "Fake&\"'<123")
        assertEquals(
            "<AddWirelessProfile><profile ssid=\"Test&amp;&quot;&apos;&lt;Net&gt;\" " +
                "password=\"Fake&amp;&quot;&apos;&lt;123\" " +
                "securityType=\"wpa_or_wpa2\" /></AddWirelessProfile>",
            xml
        )
    }

    @Test
    fun rejectsEmptyOrOversizedTestSsid() {
        assertThrows(IllegalArgumentException::class.java) {
            BoseWirelessProfileXml.buildCandidate("", "Fictitious123")
        }
        assertThrows(IllegalArgumentException::class.java) {
            BoseWirelessProfileXml.buildCandidate("x".repeat(33), "Fictitious123")
        }
    }

    @Test
    fun rejectsInvalidTestPasswordsAndControlCharacters() {
        assertThrows(IllegalArgumentException::class.java) {
            BoseWirelessProfileXml.buildCandidate("TestNetwork", "short")
        }
        assertThrows(IllegalArgumentException::class.java) {
            BoseWirelessProfileXml.buildCandidate("Test\nNetwork", "Fictitious123")
        }
        assertThrows(IllegalArgumentException::class.java) {
            BoseWirelessProfileXml.buildCandidate("TestNetwork", "Fictitious\n123")
        }
    }
}
