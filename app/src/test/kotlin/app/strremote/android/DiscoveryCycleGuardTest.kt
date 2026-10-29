package app.strremote.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoveryCycleGuardTest {
    @Test
    fun duplicateMdnsAnnouncementsNeverRestartSameHostWithinScan() {
        val guard = DiscoveryCycleGuard()
        assertTrue(guard.claimProbe("192.168.1.30"))
        assertFalse(guard.claimProbe("192.168.1.30"))
        assertFalse(guard.claimProbe("192.168.1.30"))
        assertTrue(guard.claimProbe("192.168.1.31"))
    }

    @Test
    fun nextExplicitScanCanProbeKnownHostAgain() {
        val guard = DiscoveryCycleGuard()
        assertTrue(guard.claimProbe("192.168.1.30"))
        guard.newGeneration()
        assertTrue(guard.claimProbe("192.168.1.30"))
    }

    @Test
    fun discoveryDeadlineCannotKeepSpinnerRunning() {
        assertTrue(DiscoveryCycleGuard.shouldShowChecking(true, false, true))
        assertFalse(DiscoveryCycleGuard.shouldShowChecking(false, true, true))
        assertFalse(DiscoveryCycleGuard.shouldShowChecking(false, false, true))
        assertFalse(DiscoveryCycleGuard.shouldShowChecking(true, true, true))
        assertFalse(DiscoveryCycleGuard.shouldShowChecking(true, false, false))
    }

    @Test
    fun emptyHostCannotStartProbe() {
        assertFalse(DiscoveryCycleGuard().claimProbe(""))
    }
}
