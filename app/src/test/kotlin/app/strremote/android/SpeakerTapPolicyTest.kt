package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeakerTapPolicyTest {
    @Test
    fun boseWithoutStrOpensWebsiteEvenWithKnownIp() {
        assertEquals(
            SpeakerTapAction.OPEN_STR_INSTALLATION,
            SpeakerTapPolicy.action(pendingBose = true, hasStrEndpoint = false, saved = false)
        )
        // No stale field is allowed to turn a pending Bose into an STR remote.
        assertEquals(
            SpeakerTapAction.OPEN_STR_INSTALLATION,
            SpeakerTapPolicy.action(pendingBose = true, hasStrEndpoint = true, saved = true)
        )
    }

    @Test
    fun verifiedStrAndSavedSpeakersRetainPreviousNavigation() {
        assertEquals(
            SpeakerTapAction.OPEN_STR_REMOTE,
            SpeakerTapPolicy.action(pendingBose = false, hasStrEndpoint = true, saved = false)
        )
        assertEquals(
            SpeakerTapAction.PROBE_SAVED_STR,
            SpeakerTapPolicy.action(pendingBose = false, hasStrEndpoint = false, saved = true)
        )
        assertEquals(
            SpeakerTapAction.PROBE_DISCOVERED_STR,
            SpeakerTapPolicy.action(pendingBose = false, hasStrEndpoint = false, saved = false)
        )
    }
}
