package app.strremote.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseLanVerificationTest {
    @Test
    fun allowsPrivateIpv4AddressesForBoseLanChecks() {
        assertTrue(BoseLanVerification.isPrivateIpv4("192.168.178.44"))
        assertTrue(BoseLanVerification.isPrivateIpv4("10.11.12.13"))
        assertTrue(BoseLanVerification.isPrivateIpv4("172.20.1.23"))
        assertTrue(BoseLanVerification.isPrivateIpv4("169.254.2.3"))
    }

    @Test
    fun refusesPublicDocumentationMalformedAndIpv6Addresses() {
        for (address in listOf(
            "192.0.2.1",
            "8.8.8.8",
            "127.0.0.1",
            "::1",
            "bad.test",
            "192.168.1.999",
            "192.168.1.1:8090",
            "192.168.1",
            "192.168.1.1/24"
        )) {
            assertFalse(BoseLanVerification.isPrivateIpv4(address))
        }
    }
}
