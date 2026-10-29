package app.strremote.android

/**
 * Explicit separation of a Bose Wi-Fi setup entry from a usable STR endpoint.
 * Pure policy shared by ListView and row click handlers; JVM regression-tested.
 */
internal enum class SpeakerTapAction {
    OPEN_STR_INSTALLATION,
    OPEN_STR_REMOTE,
    PROBE_SAVED_STR,
    PROBE_DISCOVERED_STR
}

internal object SpeakerTapPolicy {
    fun action(pendingBose: Boolean, hasStrEndpoint: Boolean, saved: Boolean): SpeakerTapAction =
        when {
            pendingBose -> SpeakerTapAction.OPEN_STR_INSTALLATION
            hasStrEndpoint -> SpeakerTapAction.OPEN_STR_REMOTE
            saved -> SpeakerTapAction.PROBE_SAVED_STR
            else -> SpeakerTapAction.PROBE_DISCOVERED_STR
        }
}
