package app.strremote.android

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EndpointProbeTest {
    @Test
    fun invalidHostStillCompletesCallbackExactlyOnce() {
        val probe = EndpointProbe()
        try {
            val done = CountDownLatch(1)
            val called = AtomicInteger()
            probe.probeHost("%invalid-host%") { endpoint ->
                assertNull(endpoint)
                called.incrementAndGet()
                done.countDown()
            }
            assertTrue("Probe must settle; UI cannot spin indefinitely", done.await(5, TimeUnit.SECONDS))
            assertEquals(1, called.get())
        } finally {
            probe.shutdown()
        }
    }

    @Test
    fun stoppedProbeReturnsNoEndpoint() {
        val probe = EndpointProbe()
        probe.shutdown()
        var called = false
        probe.probeHost("192.168.1.99") { endpoint ->
            assertNull(endpoint)
            called = true
        }
        assertTrue(called)
    }
}
