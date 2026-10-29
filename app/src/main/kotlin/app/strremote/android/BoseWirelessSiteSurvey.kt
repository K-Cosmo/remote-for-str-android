package app.strremote.android

import android.net.Network
import java.io.StringReader
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler

/**
 * BUILD-0012 B2: Wi-Fi networks visible to the Bose speaker, not phone scanning.
 * Community-defined survey POST, scoped to Android-approved Bose Network.
 * Does not configure any WLAN profile or transmit credentials.
 */
internal object BoseWirelessSiteSurvey {
    private const val ENDPOINT = "http://192.0.2.1:8090/performWirelessSiteSurvey"
    private const val REQUEST_XML = "<PerformWirelessSiteSurvey timeout=\"5\"/>"
    private const val MAX_XML_BYTES = 64 * 1024
    private val WPA2_TYPES = setOf("wpa_or_wpa2", "wpa2aes", "wpa2tkip")

    internal data class Candidate(
        val ssid: String,
        val signalDbm: Int?,
        val secure: Boolean,
        val securityTypes: Set<String>
    ) {
        val selectable: Boolean
            get() = secure && securityTypes.any { it in WPA2_TYPES } &&
                ssid.length in 1..32 && ssid.all { it.code in 0x20..0x7E }
    }

    internal data class Result(val candidates: List<Candidate>, val status: String)

    fun scan(network: Network): Result {
        // Two independently documented variants exist in the community:
        // POST triggers a survey, while GET may return the available networks.
        // Neither call transfers credentials. Probe GET only if POST yields no
        // usable WLAN candidates; never perform an automatic profile write.
        val posted = requestSurvey(network, "POST")
        if (posted.candidates.isNotEmpty() || Thread.currentThread().isInterrupted) {
            return posted
        }
        val fetched = requestSurvey(network, "GET")
        if (fetched.candidates.isNotEmpty()) return fetched
        return Result(
            emptyList(),
            "Bose WLAN-Suche ohne verwertbare Liste. " +
                "POST: ${posted.status}; GET: ${fetched.status}. " +
                "WLAN-Name kann manuell eingegeben werden."
        )
    }

    private fun requestSurvey(network: Network, method: String): Result {
        var connection: HttpURLConnection? = null
        return try {
            connection = network.openConnection(URL(ENDPOINT)) as HttpURLConnection
            connection.requestMethod = method
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 3_500
            connection.readTimeout = 7_000
            connection.useCaches = false
            if (method == "POST") {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "text/xml")
                val payload = REQUEST_XML.toByteArray(Charsets.UTF_8)
                connection.setFixedLengthStreamingMode(payload.size)
                connection.outputStream.use { it.write(payload) }
            }

            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                Result(emptyList(), "HTTP $code")
            } else {
                val type = connection.contentType.orEmpty().substringBefore(';').lowercase()
                if (type != "text/xml" && type != "application/xml") {
                    Result(emptyList(), "HTTP 200, kein XML (Content-Type)")
                } else {
                    val bytes = connection.inputStream.use { input ->
                        val out = ByteArrayOutputStream()
                        val buffer = ByteArray(4096)
                        while (true) {
                            if (Thread.currentThread().isInterrupted) throw InterruptedException()
                            val n = input.read(buffer)
                            if (n < 0) break
                            if (out.size() + n > MAX_XML_BYTES) {
                                throw IllegalArgumentException("oversize")
                            }
                            out.write(buffer, 0, n)
                        }
                        out.toByteArray()
                    }
                    parseResponse(bytes)
                }
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            Result(emptyList(), "abgebrochen")
        } catch (_: Exception) {
            // Do not include exception messages, addresses or response bodies.
            Result(emptyList(), "Netzwerk-/Antwortfehler")
        } finally {
            connection?.disconnect()
        }
    }

    internal fun parseResponse(xml: ByteArray): Result {
        if (xml.isEmpty() || xml.size > MAX_XML_BYTES) {
            return Result(emptyList(), "WLAN-Suche: Antwortgröße ungültig.")
        }
        return try {
            // Android does not support JAXP FEATURE_SECURE_PROCESSING. The
            // previous parser rejected every otherwise-valid Android response
            // during initialization (host-side JUnit tests did not catch it).
            // Strict UTF-8, no DOCTYPE/entities and a rejecting entity resolver
            // protect XML parsing without unsupported Android JAXP features.
            val text = Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(xml)).toString()
            if (text.contains("<!DOCTYPE", ignoreCase = true) ||
                text.contains("<!ENTITY", ignoreCase = true) ||
                text.any { it == '\u0000' }
            ) {
                return Result(emptyList(), "XML-DTD / ungültige Kodierung zurückgewiesen")
            }
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = false
                isValidating = false
                isExpandEntityReferences = false
            }
            val builder = factory.newDocumentBuilder()
            builder.setEntityResolver { _, _ ->
                throw SAXException("External entities forbidden")
            }
            builder.setErrorHandler(object : DefaultHandler() {
                override fun warning(e: SAXParseException) { throw e }
                override fun error(e: SAXParseException) { throw e }
                override fun fatalError(e: SAXParseException) { throw e }
            })
            val root = builder.parse(InputSource(StringReader(text))).documentElement
            if (root?.tagName == "status") {
                return if (root.textContent.trim() == "/performWirelessSiteSurvey") {
                    Result(emptyList(), "HTTP 200, Bose bestätigt Scan ohne WLAN-Liste")
                } else {
                    Result(emptyList(), "HTTP 200, Bose-Status ohne WLAN-Liste")
                }
            }
            if (root == null || root.tagName != "PerformWirelessSiteSurveyResponse") {
                return Result(emptyList(), "HTTP 200, unbekannter XML-Antworttyp")
            }
            val error = root.getAttribute("error")
            if (error.isNotEmpty() && error != "none") {
                return Result(emptyList(), "HTTP 200, Bose meldet Scan-Fehler")
            }
            val bySsid = linkedMapOf<String, Candidate>()
            val items = root.getElementsByTagName("item")
            for (i in 0 until minOf(items.length, 80)) {
                val item = items.item(i) as? Element ?: continue
                val ssid = item.getAttribute("ssid")
                if (ssid.isBlank() || ssid.length > 64 ||
                    ssid.any { it.code < 0x20 || it.code == 0x7F }) continue
                val types = buildSet {
                    val nodes = item.getElementsByTagName("type")
                    for (j in 0 until minOf(nodes.length, 8)) {
                        val type = nodes.item(j)?.textContent?.trim()?.lowercase().orEmpty()
                        if (type.isNotBlank()) add(type)
                    }
                }
                val candidate = Candidate(
                    ssid = ssid,
                    signalDbm = item.getAttribute("signalStrength")
                        .toIntOrNull()?.takeIf { it in -120..0 },
                    secure = item.getAttribute("secure").equals("true", ignoreCase = true),
                    securityTypes = types
                )
                val previous = bySsid[ssid]
                if (previous == null ||
                    (candidate.signalDbm ?: -120) > (previous.signalDbm ?: -120)
                ) bySsid[ssid] = candidate
            }
            val candidates = bySsid.values.sortedWith(
                compareByDescending<Candidate> { it.selectable }
                    .thenByDescending { it.signalDbm ?: -120 }
                    .thenBy { it.ssid.lowercase() }
            ).take(40)
            Result(
                candidates,
                if (candidates.isEmpty()) "HTTP 200, XML enthält keine WLAN-Einträge"
                else "Bose hat ${candidates.size} WLAN(s) gemeldet. Netzwerk auswählen."
            )
        } catch (_: javax.xml.parsers.ParserConfigurationException) {
            Result(emptyList(), "Android-XML-Parserkonfiguration fehlgeschlagen")
        } catch (_: org.xml.sax.SAXException) {
            Result(emptyList(), "HTTP 200, XML-Syntax / DTD nicht akzeptiert")
        } catch (_: Exception) {
            Result(emptyList(), "HTTP 200, XML-Verarbeitung fehlgeschlagen")
        }
    }
}
