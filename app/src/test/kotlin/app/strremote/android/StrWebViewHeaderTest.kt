package app.strremote.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrWebViewHeaderTest {
    @Test fun preservesUpstreamButtonsAndScopesCssToRecognizedHeader() {
        val script = StrWebViewHeader.compactTopPaddingScript
        assertTrue(script.contains("header.querySelector('#dev')"))
        assertTrue(script.contains("header.querySelector('#powerBtn')"))
        assertTrue(script.contains("padding-top: 10px !important"))
        assertFalse(script.contains("display: none"))
        assertFalse(script.contains("innerHTML"))
    }
}
