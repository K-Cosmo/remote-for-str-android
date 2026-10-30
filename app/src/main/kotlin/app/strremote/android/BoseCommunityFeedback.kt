package app.strremote.android

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
    const val RECIPIENT = "str-remote-android@gmx.org"
    private const val MAX_MODEL_LENGTH = 60

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
        val lines = if (de) {
            listOf(
                "SoundTouch WLAN-Ersteinrichtung – freiwillige Rückmeldung",
                "",
                "SoundTouch-Modell: $model",
                "WLAN-Einrichtung erfolgreich: ${if (outcome == BoseCommunityOutcome.SUCCESS) "Ja" else "Nein"}",
                "STR Remote Version: $appVersion",
                "Android-Version: $androidVersion",
                "",
                "Die Angaben sind eine persönliche Rückmeldung, keine Modellfreigabe.",
                "Bitte vor dem Senden prüfen und keine WLAN-Namen, Passwörter, " +
                    "IP-/MAC-Adressen oder Gerätekennungen ergänzen."
            )
        } else {
            listOf(
                "SoundTouch Wi-Fi setup – voluntary feedback",
                "",
                "SoundTouch model: $model",
                "Wi-Fi setup successful: ${if (outcome == BoseCommunityOutcome.SUCCESS) "Yes" else "No"}",
                "STR Remote version: $appVersion",
                "Android version: $androidVersion",
                "",
                "This is a personal report, not an official compatibility certification.",
                "Please review before sending. Do not include Wi-Fi names, passwords, " +
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
