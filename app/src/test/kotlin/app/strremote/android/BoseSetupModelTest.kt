package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseSetupModelTest {
    @Test fun verifiedReferenceModelIsDefault() {
        assertEquals(BoseSetupModel.SOUND_TOUCH_20_III, BoseSetupModel.fromId(null))
        assertTrue(BoseSetupModel.SOUND_TOUCH_20_III.protocolVerifiedOnDevice)
        assertEquals(BoseSetupModel.SOUND_TOUCH_20_III,
            BoseSetupModel.fromId("not-supported"))
    }

    @Test fun soundTouch10IsExplicitlyExperimental() {
        assertEquals(BoseSetupModel.SOUND_TOUCH_10,
            BoseSetupModel.fromId(BoseSetupModel.SOUND_TOUCH_10.name))
        assertFalse(BoseSetupModel.SOUND_TOUCH_10.protocolVerifiedOnDevice)
        assertTrue(BoseSetupModel.SOUND_TOUCH_10.setupHint.contains("LED"))
    }

    @Test fun onlyExplicitlyImplementedModelsAreSelectable() {
        assertEquals(2, BoseSetupModel.entries.size)
    }
}
