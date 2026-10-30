package app.strremote.android

/** Only explicit user-submitted model and outcome may enter an external draft. */
internal enum class BoseCommunityOutcome(val label: String) {
    SUCCESS("Erfolgreich"),
    FAILURE("Nicht erfolgreich")
}

internal data class BoseCommunityIssueDraft(val title: String, val body: String)

internal object BoseCommunityFeedback {
    private const val MAX_MODEL_LENGTH = 60
    const val ISSUE_PATH = "https://github.com/K-Cosmo/str-remote-android/issues/new"

    fun validModel(value: String): Boolean {
        val model = value.trim()
        return model.length in 2..MAX_MODEL_LENGTH &&
            model.all { it.isLetterOrDigit() || it in " -_.()/+" }
    }

    fun compose(modelInput: String, outcome: BoseCommunityOutcome?): BoseCommunityIssueDraft? {
        val model = modelInput.trim()
        if (!validModel(model) || outcome == null) return null
        // No identifiers, SSIDs, passwords, IP addresses, LAN results or traces.
        // GitHub receives data only if the user submits the issue in the browser.
        return BoseCommunityIssueDraft(
            title = "SoundTouch WLAN-Einrichtung: $model – ${outcome.label}",
            body = listOf(
                "## Community-Test der WLAN-Ersteinrichtung",
                "",
                "**SoundTouch-Modell:** $model",
                "**Ergebnis (selbst angegeben):** ${outcome.label}",
                "",
                "Getestet mit STR Remote (Bose-WLAN-Einrichtungsassistent).",
                "Dies ist eine persönliche Rückmeldung, keine offizielle Modellfreigabe.",
                "",
                "Bitte vor dem Absenden prüfen: Keine WLAN-Namen, Kennwörter, " +
                    "IP-/MAC-Adressen oder Gerätekennungen veröffentlichen."
            ).joinToString("\n")
        )
    }
}
