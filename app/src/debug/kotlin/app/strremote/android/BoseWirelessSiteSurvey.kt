package app.strremote.android

import android.net.Network
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource
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
        var connection: HttpURLConnection? = null
        return try {
            connection = network.openConnection(URL(ENDPOINT)) as HttpURLConnection
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 3_500
            connection.readTimeout = 7_000
            connection.useCaches = false
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/xml")
            val payload = REQUEST_XML.toByteArray(Charsets.UTF_8)
            connection.setFixedLengthStreamingMode(payload.size)
            connection.outputStream.use { it.write(payload) }
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                Result(emptyList(), "WLAN-Suche: HTTP $code. Testnetz bei Bedarf manuell eingeben.")
            } else {
                val type = connection.contentType.orEmpty().substringBefore(';').lowercase()
                if (type != "text/xml" && type != "application/xml") {
                    Result(emptyList(), "WLAN-Suche: Antwort ist kein XML. SSID manuell eingeben.")
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
        } catch (_: Exception) {
            Result(emptyList(),
                "WLAN-Suche nicht auswertbar. Bitte erneut suchen oder SSID manuell eingeben.")
        } finally {
            connection?.disconnect()
        }
    }

    internal fun parseResponse(xml: ByteArray): Result {
        if (xml.isEmpty() || xml.size > MAX_XML_BYTES) {
            return Result(emptyList(), "WLAN-Suche: Antwortgröße ungültig.")
        }
        return try {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = false
                isXIncludeAware = false
                isExpandEntityReferences = false
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            }
            val builder = factory.newDocumentBuilder()
            builder.setErrorHandler(object : DefaultHandler() {
                override fun warning(e: SAXParseException) { throw e }
                override fun error(e: SAXParseException) { throw e }
                override fun fatalError(e: SAXParseException) { throw e }
            })
            val root = builder.parse(InputSource(ByteArrayInputStream(xml))).documentElement
            if (root == null || root.tagName != "PerformWirelessSiteSurveyResponse") {
                return Result(emptyList(),
                    "WLAN-Suche: unbekanntes Bose-Antwortformat. Bitte SSID manuell eingeben.")
            }
            val error = root.getAttribute("error")
            if (error.isNotEmpty() && error != "none") {
                return Result(emptyList(), "WLAN-Suche: Bose meldet einen Scan-Fehler.")
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
                if (candidates.isEmpty()) "Bose hat keine WLANs gemeldet. SSID manuell eingeben."
                else "Bose hat ${candidates.size} WLAN(s) gemeldet. Testnetz auswählen."
            )
        } catch (_: Exception) {
            Result(emptyList(), "WLAN-Suche: XML nicht auswertbar. SSID manuell eingeben.")
        }
    }
}
