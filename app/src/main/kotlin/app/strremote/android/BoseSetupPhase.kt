package app.strremote.android

/** Wizard state distinct from network transport and visible page number. */
internal enum class BoseSetupPhase {
    PREPARING, CHOOSING_NETWORK, ENTERING_PASSWORD, SENDING, VERIFYING, TERMINAL
}

internal object BoseSetupFlow {
    const val STEP_COUNT = 4
    const val PREPARE_PAGE = 1
    const val CONNECT_PAGE = 2
    const val NETWORK_PAGE = 3
    const val PASSWORD_PAGE = 4
    const val VERIFY_PAGE = 5
    const val COMPLETE_PAGE = 6
    const val FEEDBACK_PAGE = 7

    fun mayFinish(phase: BoseSetupPhase, page: Int): Boolean =
        phase == BoseSetupPhase.TERMINAL &&
            (page == COMPLETE_PAGE || page == FEEDBACK_PAGE)
}
