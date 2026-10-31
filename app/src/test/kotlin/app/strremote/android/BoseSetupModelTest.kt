package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseSetupModelTest {
    @Test fun verifiedReferenceModelIsDefault() {
        assertEquals(BoseSetupModel.SOUND_TOUCH_20_III, BoseSetupModel.fromId(null))
        assertTrue(BoseSetupModel.SOUND_TOUCH_20_III.protocolVerifiedOnDevice)
        assertEquals(BoseSetupModel.SOUND_TOUCH_20_III,
            BoseSetupModel.fromId("not-supported"))
    }

    @Test fun soundTouch10HasVerifiedTestWifiProfile() {
        assertEquals(BoseSetupModel.SOUND_TOUCH_10,
            BoseSetupModel.fromId(BoseSetupModel.SOUND_TOUCH_10.name))
        assertTrue(BoseSetupModel.SOUND_TOUCH_10.protocolVerifiedOnDevice)
        assertEquals(R.string.bose_model_10_hint, BoseSetupModel.SOUND_TOUCH_10.setupHintRes)
        assertEquals(R.string.bose_model_10_option, BoseSetupModel.SOUND_TOUCH_10.selectionLabelRes)
    }

    @Test fun onlyExplicitlyImplementedModelsAreSelectable() {
        assertEquals(3, BoseSetupModel.entries.size)
        assertEquals(BoseSetupModel.OTHER_SOUND_TOUCH,
            BoseSetupModel.fromId("OTHER_SOUND_TOUCH"))
        assertEquals(R.string.bose_model_other_hint, BoseSetupModel.OTHER_SOUND_TOUCH.setupHintRes)
        assertEquals(false, BoseSetupModel.OTHER_SOUND_TOUCH.protocolVerifiedOnDevice)
    }
}
