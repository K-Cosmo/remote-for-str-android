package app.strremote.android

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class EndpointProbe {
    private val executor: ExecutorService = Executors.newCachedThreadPool()

    fun probe(candidate: SpeakerCandidate, callback: (SpeakerEndpoint?) -> Unit) {
        val preferred = candidate.advertisedPort.takeIf { it == 8888 || it == 17008 }
        probeHost(
            host = candidate.host,
            preferredPort = preferred,
            name = candidate.friendlyName,
            model = candidate.model,
            key = candidate.key,
            callback = callback
        )
    }

    fun probeHost(
        host: String,
        preferredPort: Int? = null,
        name: String = host,
        model: String? = null,
        key: String? = null,
        callback: (SpeakerEndpoint?) -> Unit
    ) {
        executor.execute {
            val ports = buildList {
                if (preferredPort == 8888 || preferredPort == 17008) add(preferredPort)
                if (!contains(8888)) add(8888)
                if (!contains(17008)) add(17008)
            }

            val endpoint = ports.firstNotNullOfOrNull { port ->
                val candidateEndpoint = SpeakerEndpoint(host, port, name, model, key)
                if (isStrReachable(candidateEndpoint)) candidateEndpoint else null
            }
            callback(endpoint)
        }
    }

    fun shutdown() {
        executor.shutdownNow()
    }

    private fun isStrReachable(endpoint: SpeakerEndpoint): Boolean {
        val url = URL(endpoint.baseUrl + "api/status")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 1_500
            readTimeout = 1_500
            instanceFollowRedirects = false
            useCaches = false
            setRequestProperty("Accept", "application/xml,text/xml,*/*")
            setRequestProperty("User-Agent", "STR-Remote-Android")
        }

        return try {
            val code = connection.responseCode
            code in 200..399
        } catch (_: Exception) {
            false
        } finally {
            connection.disconnect()
        }
    }
}
