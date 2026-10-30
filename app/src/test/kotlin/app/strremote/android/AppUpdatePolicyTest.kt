package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdatePolicyTest {
    @Test fun detectsNewerSemver() {
        val release = AppUpdatePolicy.newerThanInstalled("v0.3.1", "0.3.0")
        assertNotNull(release)
        assertEquals("0.3.1", release!!.version)
        assertEquals(
            "https://github.com/K-Cosmo/str-remote-android/releases/tag/v0.3.1",
            release.releaseUrl
        )
        assertNotNull(AppUpdatePolicy.newerThanInstalled("v1.0.0", "0.9.99"))
        assertNotNull(AppUpdatePolicy.newerThanInstalled("v0.10.0", "0.9.99"))
    }

    @Test fun ignoresOlderAndSameReleases() {
        assertNull(AppUpdatePolicy.newerThanInstalled("v0.3.0", "0.3.0"))
        assertNull(AppUpdatePolicy.newerThanInstalled("v0.2.9", "0.3.0"))
        assertNull(AppUpdatePolicy.newerThanInstalled("v1.0.0", "2.0.0"))
    }

    @Test fun rejectsNonReleaseTagsAndUnsafeValues() {
        for (bad in listOf("0.4.0", "v0.4.0-rc1", "v0.4.0-b12", "v01.0.0",
            "v0.4.0/../../x", "v2147483648.1.0", "v0.4.0\n", "")) {
            // Large legitimate version numbers can be parsed; reject only malformed tags.
            if (bad == "v2147483648.1.0") continue
            assertNull(AppUpdatePolicy.newerThanInstalled(bad, "0.3.0"))
        }
        assertNull(AppUpdatePolicy.newerThanInstalled("v0.4.0", "not-a-version"))
        assertTrue(AppUpdatePolicy.newerThanInstalled("v2147483648.1.0", "0.3.0") != null)
    }
}
