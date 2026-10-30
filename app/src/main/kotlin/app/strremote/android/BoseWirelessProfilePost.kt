package app.strremote.android

import android.net.Network
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler

/**
 * Controlled WLAN-profile transfer, called only after explicit user action.
 * Exactly ONE POST attempt. No automatic retries, redirects, logging or persistence.
 * A positive API acknowledgement is NOT proof of joining the destination WLAN.
 */
internal object BoseWirelessProfilePost {
    private const val ENDPOINT = "http://192.0.2.1:8090/addWirelessProfile"
    private const val HTTP_TIMEOUT_MS = 12_000
    private const val MAX_RESPONSE_BYTES = 8 * 1024

    fun submitOnce(network: Network, xmlBytes: ByteArray): String {
        var connection: HttpURLConnection? = null
        return try {
            connection = network.openConnection(URL(ENDPOINT)) as HttpURLConnection
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = HTTP_TIMEOUT_MS
            connection.readTimeout = HTTP_TIMEOUT_MS
            connection.useCaches = false
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/xml")
            connection.setFixedLengthStreamingMode(xmlBytes.size)
            connection.outputStream.use { stream ->
                stream.write(xmlBytes)
                stream.flush()
            }

            val code = connection.responseCode
            if (code !in 200..299) {
                "WLAN-Übertragung: HTTP $code. Übernahme nicht bestätigt; keine automatische Wiederholung."
            } else {
                val mediaType = connection.contentType.orEmpty().substringBefore(';').lowercase()
                if (mediaType != "text/xml" && mediaType != "application/xml") {
                    "WLAN-Übertragung: HTTP $code, unerwartete Antwort. Ergebnis unbestimmt; nicht erneut senden."
                } else {
                    val body = readBounded(connection)
                    if (isExpectedAcknowledgement(body)) {
                        "WLAN-Übertragung: HTTP $code, AddWirelessProfileResponse erkannt. " +
                            "Nur API-Antwort; WLAN-Beitritt des Lautsprechers separat prüfen."
                    } else {
                        "WLAN-Übertragung: HTTP $code, aber keine erwartete Bestätigung. " +
                            "Annahme unbestimmt; keinen weiteren POST ausführen."
                    }
                }
            }
        } catch (_: Exception) {
            // A timeout/lost SoftAP may mean the speaker accepted the credentials.
            // Never log exceptions (some may include network identifiers).
            "WLAN-Übertragung: Ergebnis unbestimmt (Verbindung/Timeout/XML). " +
                "Lautsprecher im WLAN prüfen; nicht automatisch erneut senden."
        } finally {
            connection?.disconnect()
        }
    }

    private fun readBounded(connection: HttpURLConnection): ByteArray =
        connection.inputStream.use { input ->
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (out.size() + count > MAX_RESPONSE_BYTES) {
                    throw IllegalArgumentException("oversize response")
                }
                out.write(buffer, 0, count)
            }
            out.toByteArray()
        }

    internal fun isExpectedAcknowledgement(response: ByteArray): Boolean {
        if (response.isEmpty() || response.size > MAX_RESPONSE_BYTES) return false
        return try {
            // Android's JAXP does not support FEATURE_SECURE_PROCESSING.
            // Reject DTD/entities before parsing, disallow external resolution
            // and use only platform-compatible factory configuration.
            val text = Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(response)).toString()
            if (text.contains("<!DOCTYPE", ignoreCase = true) ||
                text.contains("<!ENTITY", ignoreCase = true) ||
                text.any { it == '\u0000' }
            ) return false
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = false
                isValidating = false
                isExpandEntityReferences = false
            }
            val builder = factory.newDocumentBuilder()
            builder.setEntityResolver { _, _ -> throw SAXException("External entities forbidden") }
            builder.setErrorHandler(object : DefaultHandler() {
                override fun warning(e: SAXParseException) { throw e }
                override fun error(e: SAXParseException) { throw e }
                override fun fatalError(e: SAXParseException) { throw e }
            })
            val document = builder.parse(InputSource(StringReader(text)))
            document.documentElement?.tagName == "AddWirelessProfileResponse"
        } catch (_: Exception) {
            false
        }
    }
}
