package app.strremote.android

/** Tested models are distinct from unverified hardware; labels are localized. */
internal enum class BoseSetupModel(
    val modelLabelRes: Int,
    val selectionLabelRes: Int,
    val setupHintRes: Int,
    val protocolVerifiedOnDevice: Boolean
) {
    SOUND_TOUCH_20_III(
        R.string.bose_model_20_name,
        R.string.bose_model_20_option,
        R.string.bose_model_20_hint,
        true
    ),
    SOUND_TOUCH_10(
        R.string.bose_model_10_name,
        R.string.bose_model_10_option,
        R.string.bose_model_10_hint,
        true
    ),
    OTHER_SOUND_TOUCH(
        R.string.bose_model_other_name,
        R.string.bose_model_other_option,
        R.string.bose_model_other_hint,
        false
    );

    companion object {
        fun fromId(value: String?): BoseSetupModel =
            entries.firstOrNull { it.name == value } ?: SOUND_TOUCH_20_III
    }
}
