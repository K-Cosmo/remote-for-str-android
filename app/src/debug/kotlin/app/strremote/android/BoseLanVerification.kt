package app.strremote.android

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.URL
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler

/**
 * Debug-only Bose LAN verification after credentials were submitted.
 *
 * Reuses Android's (automatically restored) Wi-Fi Network. Uses bounded
 * SoundTouch mDNS discovery plus SSDP M-SEARCH, then checks the exact deviceID
 * obtained from the temporary Bose setup AP. No subnet sweep, HTTP write,
 * credentials, network binding of the app process or persistent identifiers.
 */
internal class BoseLanVerification(
    private val context: Context,
    private val expectedDeviceId: String,
    private val expectedSsid: String,
    private val onUpdate: (Status) -> Unit
) {
    internal sealed class Status {
        object WaitingForWifi : Status()
        object Searching : Status()
        data class Found(val wlanName: String?) : Status()
        object NotFound : Status()
    }

    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private val nsd = context.getSystemService(NsdManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
    private var wifiCallback: ConnectivityManager.NetworkCallback? = null
    private var nsdListener: NsdManager.DiscoveryListener? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private var currentWifi: Network? = null
    private var stopped = true
    private var verified = false
    private var startedAt = 0L
    private var scanGeneration = 0
    private val attempts = mutableSetOf<String>()

    fun start() {
        stop()
        stopped = false
        verified = false
        startedAt = android.os.SystemClock.elapsedRealtime()
        onUpdate(Status.WaitingForWifi)

        val wifiRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                handler.post { attachWifi(network) }
            }
            override fun onLost(network: Network) {
                handler.post {
                    if (currentWifi == network && !stopped && !verified) {
                        currentWifi = null
                        scanGeneration++
                        stopNsd()
                        onUpdate(Status.WaitingForWifi)
                    }
                }
            }
        }
        wifiCallback = callback
        try {
            cm.registerNetworkCallback(wifiRequest, callback)
        } catch (_: Exception) {
            stop()
            onUpdate(Status.NotFound)
            return
        }
        handler.postDelayed({
            if (!stopped && !verified) {
                stop()
                onUpdate(Status.NotFound)
            }
        }, 45_000L)
    }

    fun stop() {
        stopped = true
        scanGeneration++
        stopNsd()
        wifiCallback?.let {
            try { cm.unregisterNetworkCallback(it) } catch (_: Exception) { }
        }
        wifiCallback = null
        currentWifi = null
        multicastLock?.let { lock ->
            try { if (lock.isHeld) lock.release() } catch (_: Exception) { }
        }
        multicastLock = null
        attempts.clear()
    }

    private fun attachWifi(network: Network) {
        if (stopped || verified || currentWifi == network) return
        // Android returns the phone's regular Wi-Fi after its Bose setup
        // network request is released. Never inspect any cellular network.
        currentWifi = network
        scanGeneration++
        attempts.clear()
        onUpdate(Status.Searching)
        if (multicastLock == null) {
            try {
                multicastLock = wifiManager.createMulticastLock("STRRemote-BoseVerify").apply {
                    setReferenceCounted(false)
                    acquire()
                }
            } catch (_: Exception) { }
        }
        startNsd(network, scanGeneration)
        startSsdp(network, scanGeneration)
    }

    private fun startNsd(network: Network, token: Int) {
        stopNsd()
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) = Unit
            override fun onDiscoveryStopped(serviceType: String) = Unit
            override fun onServiceLost(service: NsdServiceInfo) = Unit
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                stopNsd()
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
            override fun onServiceFound(service: NsdServiceInfo) {
                if (stopped || verified || token != scanGeneration) return
                @Suppress("DEPRECATION")
                nsd.resolveService(service, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) = Unit
                    override fun onServiceResolved(info: NsdServiceInfo) {
                        val addresses = if (Build.VERSION.SDK_INT >= 34) {
                            info.hostAddresses
                        } else {
                            @Suppress("DEPRECATION")
                            listOfNotNull(info.host)
                        }
                        addresses.filterIsInstance<Inet4Address>().forEach { address ->
                            considerCandidate(network, token, address.hostAddress.orEmpty())
                        }
                    }
                })
            }
        }
        nsdListener = listener
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                nsd.discoverServices(
                    "_soundtouch._tcp.", NsdManager.PROTOCOL_DNS_SD,
                    network, context.mainExecutor, listener
                )
            } else {
                nsd.discoverServices("_soundtouch._tcp.", NsdManager.PROTOCOL_DNS_SD, listener)
            }
        } catch (_: Exception) {
            nsdListener = null
        }
    }

    private fun stopNsd() {
        nsdListener?.let {
            try { nsd.stopServiceDiscovery(it) } catch (_: Exception) { }
        }
        nsdListener = null
    }

    private fun startSsdp(network: Network, token: Int) {
        Thread({
            val message = (
                "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: 239.255.255.250:1900\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 2\r\n" +
                    "ST: urn:schemas-upnp-org:device:MediaRenderer:1\r\n\r\n"
            ).toByteArray(Charsets.UTF_8)
            repeat(4) {
                if (stopped || verified || token != scanGeneration) return@Thread
                try {
                    DatagramSocket().use { socket ->
                        network.bindSocket(socket)
                        socket.soTimeout = 600
                        val group = InetAddress.getByName("239.255.255.250")
                        socket.send(DatagramPacket(message, message.size, group, 1900))
                        val until = android.os.SystemClock.elapsedRealtime() + 2_500
                        val buffer = ByteArray(4096)
                        while (android.os.SystemClock.elapsedRealtime() < until &&
                            !stopped && !verified && token == scanGeneration
                        ) {
                            try {
                                val packet = DatagramPacket(buffer, buffer.size)
                                socket.receive(packet)
                                val payload = String(packet.data, 0, packet.length, Charsets.ISO_8859_1)
                                val location = payload.lineSequence().firstOrNull {
                                    it.startsWith("location:", ignoreCase = true)
                                }?.substringAfter(':')?.trim()
                                if (location != null) {
                                    val host = try { URL(location).host } catch (_: Exception) { "" }
                                    considerCandidate(network, token, host)
                                }
                            } catch (_: java.net.SocketTimeoutException) {
                                // Continue receiving until bounded deadline.
                            }
                        }
                    }
                } catch (_: Exception) { }
                if (stopped || verified || token != scanGeneration) return@Thread
                try { Thread.sleep(3_500) } catch (_: InterruptedException) { return@Thread }
            }
        }, "BoseLanSsdp").start()
    }

    private fun considerCandidate(network: Network, token: Int, host: String) {
        if (stopped || verified || token != scanGeneration || !isPrivateIpv4(host)) return
        synchronized(attempts) {
            if (!attempts.add(host)) return
        }
        Thread({
            val actualId = BoseLanIdentity.getDeviceId(network, host)
            if (actualId != null && actualId.equals(expectedDeviceId, ignoreCase = true)) {
                val wlan = BoseLanIdentity.getActiveSsid(network, host)
                handler.post {
                    if (!stopped && !verified && token == scanGeneration) {
                        // When supported, Bose's active profile is stronger
                        // verification than reachability alone.
                        if (wlan == null || wlan == expectedSsid) {
                            verified = true
                            onUpdate(Status.Found(wlan))
                            stop()
                        }
                    }
                }
            }
        }, "BoseLanVerifyHost").start()
    }

    internal companion object {
        fun isPrivateIpv4(host: String): Boolean {
            val parts = host.split('.')
            if (parts.size != 4 || parts.any { it.isEmpty() || it.length > 3 ||
                    it.any { ch -> !ch.isDigit() } }) return false
            val n = parts.map { it.toIntOrNull() ?: return false }
            if (n.any { it !in 0..255 }) return false
            return n[0] == 10 ||
                (n[0] == 172 && n[1] in 16..31) ||
                (n[0] == 192 && n[1] == 168) ||
                (n[0] == 169 && n[1] == 254)
        }
    }
}

/** Reads a Bose identity without displaying, storing or logging device IDs. */
internal object BoseLanIdentity {
    fun getDeviceId(network: Network, host: String): String? =
        getXml(network, host, "info")?.let { root ->
            if (root.tagName != "info") null
            else root.getAttribute("deviceID").trim().takeIf { it.isNotEmpty() }
        }

    fun getActiveSsid(network: Network, host: String): String? =
        getXml(network, host, "getActiveWirelessProfile")?.let { root ->
            if (root.tagName != "GetActiveWirelessProfileResponse") null
            else root.getElementsByTagName("ssid").item(0)?.textContent?.trim()
                ?.takeIf { it.isNotEmpty() }
        }

    private fun getXml(network: Network, host: String, path: String): Element? {
        var connection: java.net.HttpURLConnection? = null
        return try {
            connection = network.openConnection(URL("http://$host:8090/$path")) as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 1_800
            connection.readTimeout = 1_800
            connection.useCaches = false
            if (connection.responseCode != 200) return null
            val type = connection.contentType.orEmpty().substringBefore(';').lowercase()
            if (type != "text/xml" && type != "application/xml") return null
            val bytes = connection.inputStream.use { input ->
                val out = ByteArrayOutputStream()
                val buf = ByteArray(1024)
                while (true) {
                    val count = input.read(buf)
                    if (count < 0) break
                    if (out.size() + count > 16 * 1024) return null
                    out.write(buf, 0, count)
                }
                out.toByteArray()
            }
            val xml = Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString()
            if (xml.contains("<!DOCTYPE", true) || xml.contains("<!ENTITY", true)) return null
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = false
                isValidating = false
                isExpandEntityReferences = false
            }
            val builder = factory.newDocumentBuilder()
            builder.setEntityResolver { _, _ -> throw SAXException("Entity resolution disabled") }
            builder.setErrorHandler(object : DefaultHandler() {
                override fun warning(e: SAXParseException) { throw e }
                override fun error(e: SAXParseException) { throw e }
                override fun fatalError(e: SAXParseException) { throw e }
            })
            builder.parse(InputSource(StringReader(xml))).documentElement
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
