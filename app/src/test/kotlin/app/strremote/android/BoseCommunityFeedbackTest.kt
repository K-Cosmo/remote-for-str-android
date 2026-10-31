package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
        assertTrue(mail.body.contains("Remote for STR Version: 0.3.0 (Debug)"))
        assertTrue(mail.body.contains("Android-Version: 16 (API 36)"))
        assertTrue(mail.body.startsWith("SoundTouch WLAN-Einrichtung – Community-Feedback\n\nLautsprecher\n"))
        assertTrue(mail.body.contains("WLAN-Einrichtung erfolgreich: Ja\n\nSoftware\n"))
        assertTrue(mail.body.contains("Android-Version: 16 (API 36)\n\nHinweis\n"))
        assertEquals(3, "\n\n".toRegex().findAll(mail.body).count())
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

    @Test fun englishLanguageProducesEnglishDraftAndOutcome() {
        val mail = BoseCommunityFeedback.compose(
            "SoundTouch 30 Series III", BoseCommunityOutcome.FAILURE,
            "0.3.0", "16 (API 36)", "en"
        )!!
        assertEquals(
            "SoundTouch Wi-Fi feedback: SoundTouch 30 Series III – Unsuccessful",
            mail.subject
        )
        assertTrue(mail.body.contains("Wi-Fi setup successful: No"))
        assertTrue(mail.body.contains("Android version: 16 (API 36)"))
        assertTrue(mail.body.startsWith("SoundTouch Wi-Fi setup – Community feedback\n\nSpeaker\n"))
        assertTrue(mail.body.contains("Wi-Fi setup successful: No\n\nSoftware\n"))
        assertTrue(mail.body.contains("Android version: 16 (API 36)\n\nNote\n"))
        assertFalse(mail.body.contains("WLAN-Einrichtung erfolgreich"))
    }

    @Test fun fixedRecipientDoesNotRequireGitHubAccount() {
        assertEquals("str-remote-android@gmx.org", BoseCommunityFeedback.RECIPIENT)
    }

    @Test fun mailtoUriRoundTripsGermanSubjectAndMultilineBody() {
        val draft = BoseCommunityFeedback.compose(
            "SoundTouch 30 Series III", BoseCommunityOutcome.FAILURE,
            "0.3.0 (Debug)", "16 (API 36)", "de"
        )!!
        val uri = BoseCommunityFeedback.mailtoUri(draft)
        assertTrue(uri.startsWith("mailto:str-remote-android@gmx.org?subject="))
        assertTrue(uri.contains("&body="))
        assertTrue(uri.contains("%0D%0A"))
        assertFalse(uri.contains("+"))
        val subjectEncoded = uri.substringAfter("subject=").substringBefore("&body=")
        val bodyEncoded = uri.substringAfter("&body=")
        val subject = URLDecoder.decode(subjectEncoded, StandardCharsets.UTF_8.name())
        val body = URLDecoder.decode(bodyEncoded, StandardCharsets.UTF_8.name())
        assertEquals(draft.subject, subject)
        assertEquals(draft.body.replace("\n", "\r\n"), body)
    }

    @Test fun mailtoUriRoundTripsEnglishDraftWithoutNetworkIdentifiers() {
        val draft = BoseCommunityFeedback.compose(
            "Wave SoundTouch", BoseCommunityOutcome.SUCCESS,
            "0.3.0", "16 (API 36)", "en"
        )!!
        val uri = BoseCommunityFeedback.mailtoUri(draft)
        assertEquals(draft.subject, URLDecoder.decode(
            uri.substringAfter("subject=").substringBefore("&body="),
            StandardCharsets.UTF_8.name()
        ))
        assertFalse(uri.contains("password=", ignoreCase = true))
        assertFalse(uri.contains("ssid=", ignoreCase = true))
    }

}
