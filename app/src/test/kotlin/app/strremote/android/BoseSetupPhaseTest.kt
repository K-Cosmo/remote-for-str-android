package app.strremote.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseSetupPhaseTest {
    @Test fun finishNeverAppearsWhileSendingOrVerifying() {
        assertFalse(BoseSetupFlow.mayFinish(
            BoseSetupPhase.SENDING, BoseSetupFlow.VERIFY_PAGE))
        assertFalse(BoseSetupFlow.mayFinish(
            BoseSetupPhase.VERIFYING, BoseSetupFlow.VERIFY_PAGE))
        assertFalse(BoseSetupFlow.mayFinish(
            BoseSetupPhase.TERMINAL, BoseSetupFlow.VERIFY_PAGE))
        assertFalse(BoseSetupFlow.mayFinish(
            BoseSetupPhase.VERIFYING, BoseSetupFlow.COMPLETE_PAGE))
    }

    @Test fun finishRequiresCompletedVerificationAndSeparatePage() {
        assertTrue(BoseSetupFlow.mayFinish(
            BoseSetupPhase.TERMINAL, BoseSetupFlow.COMPLETE_PAGE))
        assertEquals(4, BoseSetupFlow.STEP_COUNT)
        assertEquals(4, BoseSetupFlow.PASSWORD_PAGE)
    }
}
