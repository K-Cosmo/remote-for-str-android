package app.strremote.android

internal data class SpeakerCandidate(
    val key: String,
    val serviceName: String,
    val friendlyName: String,
    val host: String,
    val advertisedPort: Int,
    val model: String?,
    val version: String?
)

internal data class SpeakerEndpoint(
    val host: String,
    val port: Int,
    val name: String,
    val model: String? = null,
    val key: String? = null
) {
    val baseUrl: String
        get() = "http://${urlHost(host)}:$port/"

    private fun urlHost(value: String): String {
        if (!value.contains(':')) return value
        val escaped = value.replace("%", "%25")
        return if (escaped.startsWith("[") && escaped.endsWith("]")) escaped else "[$escaped]"
    }
}

internal enum class RoomPreset(val storageId: String) {
    LIVING_ROOM("living_room"),
    BEDROOM("bedroom"),
    KITCHEN("kitchen"),
    BATHROOM("bathroom"),
    KIDS_ROOM("kids_room"),
    GARDEN("garden");

    companion object {
        fun fromStorageId(value: String?): RoomPreset? =
            entries.firstOrNull { it.storageId == value }
    }
}

internal data class RoomAssignment(
    val preset: RoomPreset? = null,
    val customName: String? = null
)

internal data class SavedSpeaker(
    val endpoint: SpeakerEndpoint,
    val room: RoomAssignment? = null
)

internal fun speakerIdentity(endpoint: SpeakerEndpoint): String =
    endpoint.key?.takeIf { it.isNotBlank() } ?: "host:${endpoint.host.lowercase()}"

internal fun speakerMatches(first: SpeakerEndpoint, second: SpeakerEndpoint): Boolean {
    val firstKey = first.key?.takeIf { it.isNotBlank() }
    val secondKey = second.key?.takeIf { it.isNotBlank() }
    if (firstKey != null && secondKey != null) return firstKey == secondKey
    return first.host.equals(second.host, ignoreCase = true)
}

internal fun hostForDisplay(host: String): String =
    if (host.contains(':')) "[$host]" else host
