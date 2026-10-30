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
        assertTrue(BoseSetupModel.SOUND_TOUCH_10.setupHint.contains("LED"))
        assertTrue(BoseSetupModel.SOUND_TOUCH_10.selectionLabel.endsWith("geprüft"))
    }

    @Test fun onlyExplicitlyImplementedModelsAreSelectable() {
        assertEquals(3, BoseSetupModel.entries.size)
        assertEquals(BoseSetupModel.OTHER_SOUND_TOUCH,
            BoseSetupModel.fromId("OTHER_SOUND_TOUCH"))
        assertTrue(BoseSetupModel.OTHER_SOUND_TOUCH.setupHint.contains("nicht bestätigt"))
        assertEquals(false, BoseSetupModel.OTHER_SOUND_TOUCH.protocolVerifiedOnDevice)
    }
}
