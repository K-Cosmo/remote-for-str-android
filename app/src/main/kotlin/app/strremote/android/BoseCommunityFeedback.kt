package app.strremote.android

/** Only opt-in user feedback and non-identifying app/Android versions enter mail. */
internal enum class BoseCommunityOutcome(val label: String) {
    SUCCESS("Erfolgreich"),
    FAILURE("Nicht erfolgreich")
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
        androidVersion: String
    ): BoseCommunityEmailDraft? {
        val model = modelInput.trim()
        if (!validModel(model) || outcome == null ||
            appVersion.length !in 1..40 || androidVersion.length !in 1..60
        ) return null

        return BoseCommunityEmailDraft(
            recipient = RECIPIENT,
            subject = "SoundTouch WLAN-Feedback: $model – ${outcome.label}",
            body = listOf(
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
            ).joinToString("\n")
        )
    }
}
