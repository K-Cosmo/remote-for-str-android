package app.strremote.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseWirelessProfilePostTest {
    @Test
    fun acceptsCommunityDocumentedAckRoot() {
        assertTrue(BoseWirelessProfilePost.isExpectedAcknowledgement(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><AddWirelessProfileResponse />"
                .toByteArray(Charsets.UTF_8)
        ))
    }

    @Test
    fun rejectsOtherRootsAndMalformedXml() {
        assertFalse(BoseWirelessProfilePost.isExpectedAcknowledgement(
            "<OtherResponse/>".toByteArray(Charsets.UTF_8)
        ))
        assertFalse(BoseWirelessProfilePost.isExpectedAcknowledgement(
            "<AddWirelessProfileResponse".toByteArray(Charsets.UTF_8)
        ))
    }

    @Test
    fun rejectsDoctypeAndOverlyLargeResponse() {
        assertFalse(BoseWirelessProfilePost.isExpectedAcknowledgement(
            "<!DOCTYPE x [<!ENTITY test SYSTEM \"file:///etc/passwd\">]>" +
                "<AddWirelessProfileResponse>&test;</AddWirelessProfileResponse>"
                .toByteArray(Charsets.UTF_8)
        ))
        assertFalse(BoseWirelessProfilePost.isExpectedAcknowledgement(
            ByteArray(8193) { 'x'.code.toByte() }
        ))
    }
}
