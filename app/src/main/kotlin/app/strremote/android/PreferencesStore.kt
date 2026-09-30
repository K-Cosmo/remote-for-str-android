package app.strremote.android

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal class PreferencesStore(context: Context) {
    private companion object {
        const val SAVED_SPEAKERS = "saved_speakers_v1"
        const val SHOW_MANAGEMENT_HINT = "show_management_hint"
        const val REACHABILITY_PREFIX = "speaker_reachability:"
    }

    private val prefs = context.getSharedPreferences("str_remote", Context.MODE_PRIVATE)

    fun shouldShowManagementHint(): Boolean =
        prefs.getBoolean(SHOW_MANAGEMENT_HINT, true)

    fun hideManagementHint() {
        prefs.edit().putBoolean(SHOW_MANAGEMENT_HINT, false).apply()
    }

    fun loadLastEndpoint(): SpeakerEndpoint? {
        val host = prefs.getString("host", null)?.takeIf { it.isNotBlank() } ?: return null
        val port = prefs.getInt("port", 0).takeIf { it > 0 } ?: return null
        return SpeakerEndpoint(
            host = host,
            port = port,
            name = prefs.getString("name", null) ?: host,
            model = prefs.getString("model", null),
            key = prefs.getString("key", null)
        )
    }

    fun save(endpoint: SpeakerEndpoint) {
        prefs.edit()
            .putString("host", endpoint.host)
            .putInt("port", endpoint.port)
            .putString("name", endpoint.name)
            .putString("model", endpoint.model)
            .putString("key", endpoint.key)
            .apply()
    }

    fun loadSavedSpeakers(): List<SavedSpeaker> {
        val raw = prefs.getString(SAVED_SPEAKERS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val host = item.optString("host").trim()
                    val port = item.optInt("port", 0)
                    if (host.isBlank() || port <= 0) continue

                    val endpoint = SpeakerEndpoint(
                        host = host,
                        port = port,
                        name = item.optString("name").takeIf { it.isNotBlank() } ?: host,
                        model = item.optionalString("model"),
                        key = item.optionalString("key")
                    )

                    val preset = RoomPreset.fromStorageId(item.optionalString("roomPreset"))
                    val customName = item.optionalString("roomCustom")
                    val room = when {
                        preset != null -> RoomAssignment(preset = preset)
                        !customName.isNullOrBlank() -> RoomAssignment(customName = customName)
                        else -> null
                    }

                    add(SavedSpeaker(endpoint, room))
                }
            }
        }.getOrElse { emptyList() }
    }

    fun findSavedSpeaker(key: String?, host: String): SavedSpeaker? =
        loadSavedSpeakers().firstOrNull { saved ->
            val requestedKey = key?.takeIf { it.isNotBlank() }
            val savedKey = saved.endpoint.key?.takeIf { it.isNotBlank() }
            if (requestedKey != null && savedKey != null) {
                savedKey == requestedKey
            } else {
                saved.endpoint.host.equals(host, ignoreCase = true)
            }
        }

    fun saveSpeaker(saved: SavedSpeaker) {
        val speakers = loadSavedSpeakers().toMutableList()
        val index = speakers.indexOfFirst { speakerMatches(it.endpoint, saved.endpoint) }
        if (index >= 0) {
            speakers[index] = saved
        } else {
            speakers.add(saved)
        }
        writeSavedSpeakers(speakers)
    }

    fun removeSavedSpeaker(endpoint: SpeakerEndpoint) {
        removeReachability(endpoint)
        writeSavedSpeakers(loadSavedSpeakers().filterNot { speakerMatches(it.endpoint, endpoint) })
    }

    fun updateSavedEndpoint(endpoint: SpeakerEndpoint) {
        val saved = findSavedSpeaker(endpoint.key, endpoint.host) ?: return
        val previousReachability = loadReachability(saved.endpoint)
        val previousKey = reachabilityKey(saved.endpoint)
        val updatedKey = reachabilityKey(endpoint)

        saveSpeaker(saved.copy(endpoint = endpoint))

        if (previousKey != updatedKey) {
            prefs.edit().remove(previousKey).apply()
            if (previousReachability != null) {
                saveReachability(endpoint, previousReachability)
            }
        }
    }

    fun loadReachability(endpoint: SpeakerEndpoint): Boolean? {
        val key = reachabilityKey(endpoint)
        if (!prefs.contains(key)) return null
        return prefs.getBoolean(key, false)
    }

    fun saveReachability(endpoint: SpeakerEndpoint, reachable: Boolean) {
        prefs.edit().putBoolean(reachabilityKey(endpoint), reachable).apply()
    }

    fun removeReachability(endpoint: SpeakerEndpoint) {
        prefs.edit().remove(reachabilityKey(endpoint)).apply()
    }

    private fun reachabilityKey(endpoint: SpeakerEndpoint): String =
        REACHABILITY_PREFIX + speakerIdentity(endpoint)

    private fun writeSavedSpeakers(speakers: List<SavedSpeaker>) {
        val array = JSONArray()
        speakers.forEach { saved ->
            array.put(
                JSONObject().apply {
                    put("host", saved.endpoint.host)
                    put("port", saved.endpoint.port)
                    put("name", saved.endpoint.name)
                    putNullable("model", saved.endpoint.model)
                    putNullable("key", saved.endpoint.key)
                    putNullable("roomPreset", saved.room?.preset?.storageId)
                    putNullable("roomCustom", saved.room?.customName?.trim()?.takeIf { it.isNotBlank() })
                }
            )
        }
        prefs.edit().putString(SAVED_SPEAKERS, array.toString()).apply()
    }

    private fun JSONObject.optionalString(name: String): String? =
        if (isNull(name)) null else optString(name).trim().takeIf { it.isNotBlank() }

    private fun JSONObject.putNullable(name: String, value: String?) {
        if (value == null) put(name, JSONObject.NULL) else put(name, value)
    }
}
