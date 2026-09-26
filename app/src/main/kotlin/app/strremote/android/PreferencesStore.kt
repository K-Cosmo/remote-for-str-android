package app.strremote.android

import android.content.Context

internal class PreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences("str_remote", Context.MODE_PRIVATE)

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
}
