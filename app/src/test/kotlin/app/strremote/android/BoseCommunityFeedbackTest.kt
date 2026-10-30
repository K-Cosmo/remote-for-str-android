package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseCommunityFeedbackTest {
    @Test fun onlyExplicitModelAndOutcomeProduceDraft() {
        assertNull(BoseCommunityFeedback.compose("", BoseCommunityOutcome.SUCCESS))
        assertNull(BoseCommunityFeedback.compose("SoundTouch 30", null))
        assertNotNull(BoseCommunityFeedback.compose("SoundTouch 30 III", BoseCommunityOutcome.SUCCESS))
        assertNotNull(BoseCommunityFeedback.compose("SoundTouch 300", BoseCommunityOutcome.FAILURE))
    }

    @Test fun draftsContainNoAutomaticPrivateNetworkData() {
        val success = BoseCommunityFeedback.compose(
            " SoundTouch Portable ", BoseCommunityOutcome.SUCCESS
        )!!
        assertEquals("SoundTouch WLAN-Einrichtung: Modellrückmeldung", success.title)
        assertTrue(success.body.contains("SoundTouch Portable"))
        assertTrue(success.body.contains("Erfolgreich"))
        assertTrue(success.body.contains("selbst angegeben"))
        assertFalse(success.body.contains("SSID:"))
        assertFalse(success.body.contains("deviceID"))
        val failure = BoseCommunityFeedback.compose(
            "SoundTouch 30", BoseCommunityOutcome.FAILURE
        )!!
        assertTrue(failure.body.contains("Nicht erfolgreich"))
        assertFalse(failure.body.contains("Erfolgreich\n"))
    }

    @Test fun rejectMultilineAndOversizedModelLabels() {
        assertFalse(BoseCommunityFeedback.validModel("SoundTouch 30\nKennwort"))
        assertFalse(BoseCommunityFeedback.validModel("A".repeat(61)))
        assertFalse(BoseCommunityFeedback.validModel("a"))
        assertFalse(BoseCommunityFeedback.validModel("SoundTouch <script>"))
        assertTrue(BoseCommunityFeedback.validModel("SoundTouch 30 Series III"))
    }

    @Test fun issueDraftUsesFixedOfficialGitHubLocation() {
        assertEquals(
            "https://github.com/K-Cosmo/str-remote-android/issues/new",
            BoseCommunityFeedback.ISSUE_PATH
        )
    }
}
