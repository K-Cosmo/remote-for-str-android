package app.strremote.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppUpdateCheckerTest {
    @Test fun checksOnlyCanonicalRenamedRepository() {
        assertEquals(
            "https://api.github.com/repos/K-Cosmo/remote-for-str-android/releases/latest",
            AppUpdateChecker.API_URL
        )
    }

    @Test fun acceptsOfficialReleaseWithMatchingApk() {
        val json = """{"tag_name":"v0.4.0","draft":false,"prerelease":false,
            "assets":[{"name":"Remote-for-STR-0.4.0.apk"}]}"""
        assertEquals("0.4.0", AppUpdateChecker.parsePublicRelease(json, "0.3.0")?.version)
    }

    @Test fun acceptsLegacyApkNameDuringBrandMigration() {
        val json = """{"tag_name":"v0.4.0","draft":false,"prerelease":false,
            "assets":[{"name":"STR-Remote-0.4.0.apk"}]}"""
        assertEquals("0.4.0", AppUpdateChecker.parsePublicRelease(json, "0.3.0")?.version)
    }

    @Test fun ignoresMissingApkOrPreRelease() {
        val noApk = """{"tag_name":"v0.4.0","draft":false,"prerelease":false,
            "assets":[{"name":"other.apk"}]}"""
        assertNull(AppUpdateChecker.parsePublicRelease(noApk, "0.3.0"))
        assertNull(AppUpdateChecker.parsePublicRelease(
            """{"tag_name":"v0.4.0","draft":false,"prerelease":true,
            "assets":[{"name":"STR-Remote-0.4.0.apk"}]}""", "0.3.0"))
        assertNull(AppUpdateChecker.parsePublicRelease(
            """{"tag_name":"v0.4.0","draft":true,"prerelease":false,
            "assets":[{"name":"STR-Remote-0.4.0.apk"}]}""", "0.3.0"))
    }

    @Test fun doesNotShowAlreadyInstalledRelease() {
        assertNull(AppUpdateChecker.parsePublicRelease(
            """{"tag_name":"v0.3.0","draft":false,"prerelease":false,
            "assets":[{"name":"STR-Remote-0.3.0.apk"}]}""", "0.3.0"))
    }
}
