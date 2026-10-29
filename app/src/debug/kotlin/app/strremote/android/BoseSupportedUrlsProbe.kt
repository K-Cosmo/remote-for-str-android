package app.strremote.android

import android.net.Network
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * BUILD-0012 / B1 — read-only capability inquiry against Bose's original API.
 *
 * Requests only GET /supportedURLs, limits memory to 64 KiB, parses in-memory
 * without emitting full XML or identifiable speaker/network details, and
 * returns a fixed-vocabulary summary of *advertised* Wi-Fi endpoints.
 *
 * This is NOT proof that POST /addWirelessProfile is accepted. No POST is made here.
 */
internal object BoseSupportedUrlsProbe {
    private const val ENDPOINT = "http://192.0.2.1:8090/supportedURLs"
    private const val MAX_RESPONSE_BYTES = 64 * 1024
    private const val TIMEOUT_MS = 2_500

    private val interestingEndpoints = listOf(
        "/addWirelessProfile",
        "/performWirelessSiteSurvey",
        "/getActiveWirelessProfile"
    )

    internal data class Report(
        val summary: String,
        val supportsAddWirelessProfile: Boolean
    )

    // Kept for the original read-only PoC call sites.
    fun inspect(network: Network): String = inspectReport(network).summary

    fun inspectReport(network: Network): Report {
        var connection: HttpURLConnection? = null
        val summary = try {
            connection = network.openConnection(URL(ENDPOINT)) as HttpURLConnection
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.useCaches = false

            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                "B1 /supportedURLs: HTTP $code – auf dieser Firmware nicht auswertbar"
            } else {
                val type = connection.contentType.orEmpty().substringBefore(';').lowercase()
                if (type != "text/xml" && type != "application/xml") {
                    "B1 /supportedURLs: HTTP 200, unerwarteter Antworttyp"
                } else {
                    val bytes = connection.inputStream.use { stream ->
                        val buffer = ByteArray(4096)
                        val collected = ByteArrayOutputStream()
                        while (true) {
                            if (Thread.currentThread().isInterrupted) {
                                throw InterruptedException("probe cancelled")
                            }
                            val read = stream.read(buffer)
                            if (read < 0) break
                            if (collected.size() + read > MAX_RESPONSE_BYTES) {
                                throw ResponseTooLargeException()
                            }
                            collected.write(buffer, 0, read)
                        }
                        collected.toByteArray()
                    }
                    val advertised = parseWhitelistedUrls(bytes)
                    return Report(
                        summary = buildString {
                        append("B1 GET /supportedURLs → HTTP 200, XML")
                        for (endpoint in interestingEndpoints) {
                            append("\n")
                            append(endpoint)
                            append(": ")
                            append(if (endpoint in advertised) "vom Gerät aufgelistet"
                                   else "nicht aufgelistet")
                        }
                        append("\nNur eine Funktionsliste – kein Nachweis der Profilübertragung.")
                        },
                        supportsAddWirelessProfile = "/addWirelessProfile" in advertised
                    )
                }
            }
        } catch (_: ResponseTooLargeException) {
            "B1 /supportedURLs: Antwort über 64 KiB – abgebrochen"
        } catch (_: InterruptedException) {
            "B1 /supportedURLs: Prüfung abgebrochen"
        } catch (_: Exception) {
            // Never log exception messages: they may contain device/network data.
            "B1 /supportedURLs: nicht auswertbar (Netzwerk- oder XML-Fehler)"
        } finally {
            connection?.disconnect()
        }
        return Report(summary = summary, supportsAddWirelessProfile = false)
    }

    private fun parseWhitelistedUrls(xml: ByteArray): Set<String> {
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(xml), "UTF-8")

        val found = mutableSetOf<String>()
        var sawRoot = false
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.DOCDECL) {
                throw IllegalArgumentException("DTD not accepted")
            }
            if (event == XmlPullParser.START_TAG) {
                if (!sawRoot) {
                    if (parser.name != "supportedURLs") {
                        throw IllegalArgumentException("Unexpected XML root")
                    }
                    sawRoot = true
                } else if (parser.name == "URL") {
                    // Extract only literal known endpoint paths, never IDs.
                    val location = parser.getAttributeValue(null, "location")
                    if (location in interestingEndpoints) found.add(location)
                }
            }
            event = parser.next()
        }
        if (!sawRoot) throw IllegalArgumentException("Missing XML root")
        return found
    }

    private class ResponseTooLargeException : Exception()
}
