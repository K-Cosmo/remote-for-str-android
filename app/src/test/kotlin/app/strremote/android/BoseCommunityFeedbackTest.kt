package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseCommunityFeedbackTest {
    @Test fun onlyExplicitModelAndOutcomeProduceDraft() {
        assertNull(BoseCommunityFeedback.compose("", BoseCommunityOutcome.SUCCESS, "0.3.0", "16 (API 36)"))
        assertNull(BoseCommunityFeedback.compose("SoundTouch 30", null, "0.3.0", "16 (API 36)"))
        assertNotNull(BoseCommunityFeedback.compose(
            "SoundTouch 30 III", BoseCommunityOutcome.SUCCESS, "0.3.0", "16 (API 36)"
        ))
        assertNotNull(BoseCommunityFeedback.compose(
            "SoundTouch 300", BoseCommunityOutcome.FAILURE, "0.3.0", "16 (API 36)"
        ))
    }

    @Test fun mailIncludesVersionsAndNoAutomaticNetworkIdentifiers() {
        val mail = BoseCommunityFeedback.compose(
            " SoundTouch Portable ", BoseCommunityOutcome.SUCCESS,
            "0.3.0 (Debug)", "16 (API 36)"
        )!!
        assertEquals("str-remote-android@gmx.org", mail.recipient)
        assertEquals(
            "SoundTouch WLAN-Feedback: SoundTouch Portable – Erfolgreich",
            mail.subject
        )
        assertTrue(mail.body.contains("SoundTouch Portable"))
        assertTrue(mail.body.contains("WLAN-Einrichtung erfolgreich: Ja"))
        assertTrue(mail.body.contains("STR Remote Version: 0.3.0 (Debug)"))
        assertTrue(mail.body.contains("Android-Version: 16 (API 36)"))
        assertFalse(mail.body.contains("SSID:"))
        assertFalse(mail.body.contains("deviceID"))
        assertFalse(mail.body.contains("192.168."))
        val failure = BoseCommunityFeedback.compose(
            "SoundTouch 30", BoseCommunityOutcome.FAILURE, "0.3.0", "15 (API 35)"
        )!!
        assertTrue(failure.body.contains("WLAN-Einrichtung erfolgreich: Nein"))
        assertFalse(failure.body.contains("WLAN-Einrichtung erfolgreich: Ja"))
    }

    @Test fun rejectMultilineOversizedAndUnsafeLabels() {
        assertFalse(BoseCommunityFeedback.validModel("SoundTouch 30\nKennwort"))
        assertFalse(BoseCommunityFeedback.validModel("A".repeat(61)))
        assertFalse(BoseCommunityFeedback.validModel("a"))
        assertFalse(BoseCommunityFeedback.validModel("SoundTouch <script>"))
        assertTrue(BoseCommunityFeedback.validModel("SoundTouch 30 Series III"))
        assertNull(BoseCommunityFeedback.compose(
            "SoundTouch 30", BoseCommunityOutcome.SUCCESS, "", "16"
        ))
    }

    @Test fun fixedRecipientDoesNotRequireGitHubAccount() {
        assertEquals("str-remote-android@gmx.org", BoseCommunityFeedback.RECIPIENT)
    }
}
