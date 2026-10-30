package app.strremote.android

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/** Optional best-effort check against the official public GitHub release API. */
internal data class AppUpdateCheckResult(val available: AvailableAppUpdate?)

internal object AppUpdateChecker {
    private const val API_URL =
        "https://api.github.com/repos/K-Cosmo/str-remote-android/releases/latest"
    private const val MAX_JSON_BYTES = 64 * 1024

    fun fetch(installedVersion: String): AppUpdateCheckResult? {
        var connection: HttpsURLConnection? = null
        return try {
            connection = URL(API_URL).openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "STR-Remote-Android")
            connection.connectTimeout = 3_500
            connection.readTimeout = 3_500
            connection.instanceFollowRedirects = false
            connection.useCaches = false
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val output = ByteArrayOutputStream()
            connection.inputStream.use { stream ->
                val buffer = ByteArray(2048)
                while (true) {
                    val count = stream.read(buffer)
                    if (count == -1) break
                    if (output.size() + count > MAX_JSON_BYTES) return null
                    output.write(buffer, 0, count)
                }
            }
            AppUpdateCheckResult(parsePublicRelease(output.toString("UTF-8"), installedVersion))
        } catch (_: Exception) {
            // GitHub/outside connectivity must never block STR discovery or playback.
            null
        } finally {
            connection?.disconnect()
        }
    }

    internal fun parsePublicRelease(json: String, installedVersion: String): AvailableAppUpdate? {
        val release = JSONObject(json)
        if (release.optBoolean("draft", true) || release.optBoolean("prerelease", true)) return null
        val candidate = AppUpdatePolicy.newerThanInstalled(
            tag = release.optString("tag_name"), current = installedVersion
        ) ?: return null

        // Do not announce incomplete releases without a downloadable official APK.
        val assets = release.optJSONArray("assets") ?: return null
        // New releases use the adopted product name. Accept the legacy asset
        // format too, so existing historical release naming is not confused
        // with an incomplete release during the migration.
        val allowedApkNames = setOf(
            "Remote-for-STR-${candidate.version}.apk",
            "STR-Remote-${candidate.version}.apk"
        )
        val foundApk = (0 until assets.length()).any { i ->
            assets.optJSONObject(i)?.optString("name") in allowedApkNames
        }
        return candidate.takeIf { foundApk }
    }
}
