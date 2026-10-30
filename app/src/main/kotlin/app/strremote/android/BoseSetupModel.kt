package app.strremote.android

/**
 * Explicitly scoped to models with documented Setup-mode instructions.
 * Device-side provisioning compatibility is a separate hardware test gate.
 * Do not silently treat all Bose speakers as interchangeable.
 */
internal enum class BoseSetupModel(
    val modelLabel: String,
    val selectionLabel: String,
    val setupHint: String,
    val protocolVerifiedOnDevice: Boolean
) {
    SOUND_TOUCH_20_III(
        "SoundTouch 20 Series III",
        "SoundTouch 20 III · geprüft",
        "Die SoundTouch 20 III zeigt einen Countdown und anschließend „Setup“ im Display.",
        true
    ),
    SOUND_TOUCH_10(
        "SoundTouch 10",
        "SoundTouch 10 · geprüft",
        "Die SoundTouch 10 besitzt kein Textdisplay. Achte auf die dauerhaft gelbe WLAN-LED.",
        true
    ),
    OTHER_SOUND_TOUCH(
        "anderen SoundTouch-Modell",
        "Anderes SoundTouch-Modell",
        "Die Tastenkombination Preset 2 + Leiser ist für dieses Modell noch " +
            "nicht bestätigt. Nur fortfahren, wenn die Box tatsächlich ein " +
            "Bose-Setup-WLAN anbietet.",
        false
    );

    companion object {
        fun fromId(value: String?): BoseSetupModel =
            entries.firstOrNull { it.name == value } ?: SOUND_TOUCH_20_III
    }
}
