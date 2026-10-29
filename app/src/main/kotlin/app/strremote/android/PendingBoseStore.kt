package app.strremote.android

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Bose setup outcome, kept separate from STR's saved, usable endpoints.
 * Enabled by callers only in the debug PoC package. No Wi-Fi credentials,
 * SSIDs or Bose XML are persisted. Do not promote this to SavedSpeaker.
 */
internal data class PendingBoseDevice(
    val deviceId: String,
    val host: String? = null,
    val verifiedOnWifi: Boolean = false,
    val displayName: String = "SoundTouch"
)

internal class PendingBoseStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        "str_remote_bose_setup_poc", Context.MODE_PRIVATE
    )

    fun list(): List<PendingBoseDevice> = runCatching {
        val array = JSONArray(preferences.getString("pending_speakers_v1", "[]"))
        buildList {
            for (index in 0 until minOf(array.length(), 30)) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optString("id").trim().takeIf {
                    it.length in 1..128 && it.all { char -> char.isLetterOrDigit() || char == '-' || char == '_' || char == ':' }
                } ?: continue
                val host = item.optString("host").trim().takeIf {
                    it.isNotEmpty() && BoseHostPolicy.isPrivateIpv4(it)
                }
                add(PendingBoseDevice(
                    deviceId = id,
                    host = host,
                    verifiedOnWifi = host != null && item.optBoolean("verified"),
                    displayName = item.optString("name")
                        .trim().takeIf { it.length in 1..60 } ?: "SoundTouch"
                ))
            }
        }
    }.getOrElse { emptyList() }

    fun recordSubmission(id: String) {
        if (!isValidId(id)) return
        val existing = list().filterNot { it.deviceId == id }
        persist((existing + PendingBoseDevice(id, displayName = "SoundTouch")).takeLast(30))
    }

    fun recordVerified(id: String, host: String) {
        if (!isValidId(id) || !BoseHostPolicy.isPrivateIpv4(host)) return
        val existing = list().filterNot { it.deviceId == id }
        persist((existing + PendingBoseDevice(
            deviceId = id, host = host, verifiedOnWifi = true
        )).takeLast(30))
    }

    fun remove(id: String) = persist(list().filterNot { it.deviceId == id })

    /** Only remove the pending marker when STR itself is reachable. */
    fun removeMatchingStrDevice(id: String, host: String) {
        val remaining = list().filterNot {
            it.deviceId.equals(id, ignoreCase = true) ||
                (it.host != null && it.host.equals(host, ignoreCase = true))
        }
        persist(remaining)
    }

    private fun isValidId(id: String): Boolean =
        id.length in 1..128 && id.all {
            it.isLetterOrDigit() || it == '-' || it == '_' || it == ':'
        }

    private fun persist(items: List<PendingBoseDevice>) {
        val json = JSONArray()
        items.forEach { item ->
            json.put(JSONObject().apply {
                put("id", item.deviceId)
                put("host", item.host ?: "")
                put("verified", item.verifiedOnWifi)
                put("name", item.displayName)
            })
        }
        preferences.edit().putString("pending_speakers_v1", json.toString()).apply()
    }
}

/** Pure, JVM-testable validation for hosts returned by local discovery. */
internal object BoseHostPolicy {
    fun isPrivateIpv4(host: String): Boolean {
        val parts = host.split('.')
        if (parts.size != 4 || parts.any { it.isEmpty() || it.length > 3 ||
                it.any { char -> !char.isDigit() } }) return false
        val n = parts.map { it.toIntOrNull() ?: return false }
        if (n.any { it !in 0..255 }) return false
        return n[0] == 10 ||
            (n[0] == 172 && n[1] in 16..31) ||
            (n[0] == 192 && n[1] == 168) ||
            (n[0] == 169 && n[1] == 254)
    }
}
