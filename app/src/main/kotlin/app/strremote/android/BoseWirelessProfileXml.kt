package app.strremote.android

/**
 * BUILD-0012 / B2 preparation ONLY.
 *
 * Builds the candidate AddWirelessProfile XML documented by AfterTouch.
 * No networking, HTTP POST, UI wiring, credentials storage or logging occurs here.
 * This candidate has not been verified against the original SoundTouch 20 III.
 *
 * Intentional initial test restriction: printable ASCII SSID/passphrase and WPA2
 * test network. Future production support must revisit these limits and confirm
 * the actual Bose firmware protocol before accepting general credentials.
 */
internal object BoseWirelessProfileXml {
    private const val SECURITY_TYPE = "wpa_or_wpa2"

    fun buildCandidate(ssid: String, passphrase: String): String {
        require(ssid.length in 1..32) { "Test SSID must be 1..32 ASCII characters" }
        require(passphrase.length in 8..63) {
            "Test WPA2 passphrase must be 8..63 ASCII characters"
        }
        require(ssid.all(::isPrintableAscii)) { "Test SSID contains unsupported characters" }
        require(passphrase.all(::isPrintableAscii)) {
            "Test WPA2 passphrase contains unsupported characters"
        }

        return buildString {
            append("<AddWirelessProfile><profile ssid=\"")
            append(escapeXmlAttribute(ssid))
            append("\" password=\"")
            append(escapeXmlAttribute(passphrase))
            append("\" securityType=\"")
            append(SECURITY_TYPE)
            append("\" /></AddWirelessProfile>")
        }
    }

    private fun isPrintableAscii(char: Char): Boolean = char.code in 0x20..0x7E

    private fun escapeXmlAttribute(value: String): String = buildString {
        for (char in value) {
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> append(char)
            }
        }
    }
}
