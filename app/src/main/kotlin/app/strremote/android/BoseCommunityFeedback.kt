package app.strremote.android

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** User-triggered email only, containing no device or network identifiers. */
internal enum class BoseCommunityOutcome {
    SUCCESS, FAILURE
}

internal data class BoseCommunityEmailDraft(
    val recipient: String,
    val subject: String,
    val body: String
)

internal object BoseCommunityFeedback {
    /**
     * Public-source anti-harvesting measure, NOT secrecy or encryption.
     * Decode the fixed feedback recipient only when an email draft is created.
     * Never commit a complete mailbox address as a literal in source or docs.
     */
    val RECIPIENT: String by lazy {
        val encoded = intArrayOf(
            40, 63, 55, 53, 46, 63, 119, 60, 53, 40,
            119, 41, 46, 40, 119, 59, 52, 62, 40, 53,
            51, 62, 26, 61, 55, 34, 116, 53, 40, 61
        )
        buildString(encoded.size) {
            encoded.forEach { append((it xor 0x5A).toChar()) }
        }
    }
    private const val MAX_MODEL_LENGTH = 60

    /**
     * RFC 6068 mailto URI. Some email apps ignore ACTION_SENDTO extras but
     * honor subject/body from the data URI. Keep both representations.
     */
    fun mailtoUri(draft: BoseCommunityEmailDraft): String {
        fun encode(value: String): String =
            URLEncoder.encode(value, StandardCharsets.UTF_8.name())
                .replace("+", "%20")
        val normalizedBody = draft.body.replace("\r\n", "\n").replace("\n", "\r\n")
        return "mailto:${draft.recipient}?subject=${encode(draft.subject)}&body=${encode(normalizedBody)}"
    }

    fun validModel(value: String): Boolean {
        val model = value.trim()
        return model.length in 2..MAX_MODEL_LENGTH &&
            model.all { it.isLetterOrDigit() || it in " -_.()/+" }
    }

    fun compose(
        modelInput: String,
        outcome: BoseCommunityOutcome?,
        appVersion: String,
        androidVersion: String,
        language: String = "de"
    ): BoseCommunityEmailDraft? {
        val model = modelInput.trim()
        if (!validModel(model) || outcome == null ||
            appVersion.length !in 1..40 || androidVersion.length !in 1..60
        ) return null

        val de = language.equals("de", ignoreCase = true)
        val result = if (de) {
            if (outcome == BoseCommunityOutcome.SUCCESS) "Erfolgreich"
            else "Nicht erfolgreich"
        } else {
            if (outcome == BoseCommunityOutcome.SUCCESS) "Successful"
            else "Unsuccessful"
        }
        // Keep this portable plain text: blank lines separate sections in
        // Gmail, Outlook and other email apps without requiring HTML support.
        val lines = if (de) {
            listOf(
                "SoundTouch WLAN-Einrichtung – Community-Feedback",
                "",
                "Lautsprecher",
                "SoundTouch-Modell: $model",
                "WLAN-Einrichtung erfolgreich: ${if (outcome == BoseCommunityOutcome.SUCCESS) "Ja" else "Nein"}",
                "",
                "Software",
                "Remote for STR Version: $appVersion",
                "Android-Version: $androidVersion",
                "",
                "Hinweis",
                "Diese Rückmeldung beschreibt einen einzelnen Test und ist keine allgemeine Modellfreigabe.",
                "Bitte vor dem Senden prüfen. Keine WLAN-Namen, Passwörter,",
                "IP-/MAC-Adressen oder Gerätekennungen ergänzen."
            )
        } else {
            listOf(
                "SoundTouch Wi-Fi setup – Community feedback",
                "",
                "Speaker",
                "SoundTouch model: $model",
                "Wi-Fi setup successful: ${if (outcome == BoseCommunityOutcome.SUCCESS) "Yes" else "No"}",
                "",
                "Software",
                "Remote for STR version: $appVersion",
                "Android version: $androidVersion",
                "",
                "Note",
                "This is one user's test, not a general model compatibility certification.",
                "Please review before sending. Do not add Wi-Fi names, passwords,",
                "IP/MAC addresses or device identifiers."
            )
        }

        return BoseCommunityEmailDraft(
            recipient = RECIPIENT,
            subject = if (de) "SoundTouch WLAN-Feedback: $model – $result"
                else "SoundTouch Wi-Fi feedback: $model – $result",
            body = lines.joinToString("\n")
        )
    }
}
