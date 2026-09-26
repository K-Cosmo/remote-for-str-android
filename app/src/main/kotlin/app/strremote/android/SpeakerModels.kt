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

internal fun hostForDisplay(host: String): String =
    if (host.contains(':')) "[$host]" else host
