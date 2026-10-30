package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoseSoundTouchCatalogTest {
    @Test fun includesHistoricalProductFamiliesAndRevisions() {
        val models = BoseSoundTouchCatalog.modelSuggestions
        assertTrue(models.contains("SoundTouch 20 Series II"))
        assertTrue(models.contains("SoundTouch 30 Series III"))
        assertTrue(models.contains("SoundTouch Portable Series II"))
        assertTrue(models.contains("Wave SoundTouch Music System IV"))
        assertTrue(models.contains("SoundTouch SA-5 Amplifier"))
        assertTrue(models.contains("SoundTouch Wireless Link Adapter"))
        assertTrue(models.contains("SoundTouch 300 Soundbar"))
        assertTrue(models.contains("Lifestyle SoundTouch 535"))
        assertTrue(models.contains("VideoWave III 46 (SoundTouch adapter)"))
    }

    @Test fun everySuggestionFitsManualFeedbackValidation() {
        val models = BoseSoundTouchCatalog.modelSuggestions
        assertEquals(models.size, models.distinct().size)
        assertEquals(models.sorted(), models)
        assertTrue(models.size >= 30)
        assertTrue(models.all(BoseCommunityFeedback::validModel))
    }
}
