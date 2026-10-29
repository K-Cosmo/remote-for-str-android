package app.strremote.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseHostPolicyTest {
    @Test
    fun acceptsLocalHostsAndRejectsOtherAddresses() {
        assertTrue(BoseHostPolicy.isPrivateIpv4("192.168.10.20"))
        assertTrue(BoseHostPolicy.isPrivateIpv4("10.0.4.16"))
        assertTrue(BoseHostPolicy.isPrivateIpv4("172.20.50.3"))
        assertFalse(BoseHostPolicy.isPrivateIpv4("192.0.2.1"))
        assertFalse(BoseHostPolicy.isPrivateIpv4("1.1.1.1"))
        assertFalse(BoseHostPolicy.isPrivateIpv4("192.168.4.999"))
        assertFalse(BoseHostPolicy.isPrivateIpv4("abc"))
        assertFalse(BoseHostPolicy.isPrivateIpv4(""))
    }
}
