package app.strremote.android

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import java.net.Inet4Address
import java.nio.charset.StandardCharsets
import java.util.ArrayDeque

internal class StrDiscovery(
    context: Context,
    private val callback: Callback
) {
    interface Callback {
        fun onDiscoveryStarted()
        fun onSpeakerFound(candidate: SpeakerCandidate)
        fun onSpeakerLost(serviceName: String)
        fun onDiscoveryError(errorCode: Int)
    }

    private val appContext = context.applicationContext
    private val nsdManager = appContext.getSystemService(NsdManager::class.java)
    private val wifiManager = appContext.getSystemService(WifiManager::class.java)
    private val listeners = mutableMapOf<String, NsdManager.DiscoveryListener>()
    private val resolveQueue = ArrayDeque<NsdServiceInfo>()
    private val queuedNames = mutableSetOf<String>()
    private var resolving = false
    private var multicastLock: WifiManager.MulticastLock? = null
    private var running = false

    fun start() {
        stop()
        running = true
        acquireMulticastLock()
        callback.onDiscoveryStarted()
        discoverType("_streborn._tcp.")
        discoverType("_soundtouchstick._tcp.")
    }

    fun stop() {
        running = false
        listeners.values.toList().forEach { listener ->
            try {
                nsdManager.stopServiceDiscovery(listener)
            } catch (_: IllegalArgumentException) {
                // Listener may already have been stopped by the framework.
            }
        }
        listeners.clear()
        synchronized(resolveQueue) {
            resolveQueue.clear()
            queuedNames.clear()
            resolving = false
        }
        multicastLock?.let { lock ->
            if (lock.isHeld) lock.release()
        }
        multicastLock = null
    }

    private fun discoverType(serviceType: String) {
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) = Unit

            override fun onServiceFound(service: NsdServiceInfo) {
                enqueueResolve(service)
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                callback.onSpeakerLost(service.serviceName ?: "")
            }

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                callback.onDiscoveryError(errorCode)
                try {
                    nsdManager.stopServiceDiscovery(this)
                } catch (_: IllegalArgumentException) {
                    // Ignore framework race during failed startup.
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                callback.onDiscoveryError(errorCode)
            }
        }
        listeners[serviceType] = listener
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
    }

    private fun enqueueResolve(service: NsdServiceInfo) {
        if (!running) return
        val queueKey = "${service.serviceType}|${service.serviceName}"
        synchronized(resolveQueue) {
            if (!queuedNames.add(queueKey)) return
            resolveQueue.addLast(service)
            if (!resolving) resolveNextLocked()
        }
    }

    private fun resolveNextLocked() {
        val service = if (resolveQueue.isEmpty()) null else resolveQueue.removeFirst()
        if (service == null || !running) {
            resolving = false
            return
        }
        resolving = true

        @Suppress("DEPRECATION")
        nsdManager.resolveService(service, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                finishResolve(serviceInfo)
                if (errorCode == NsdManager.FAILURE_PERMISSION_DENIED) {
                    callback.onDiscoveryError(errorCode)
                }
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                toCandidate(serviceInfo)?.let(callback::onSpeakerFound)
                finishResolve(serviceInfo)
            }
        })
    }

    private fun finishResolve(serviceInfo: NsdServiceInfo) {
        val queueKey = "${serviceInfo.serviceType}|${serviceInfo.serviceName}"
        synchronized(resolveQueue) {
            queuedNames.remove(queueKey)
            resolving = false
            if (running) resolveNextLocked()
        }
    }

    private fun toCandidate(info: NsdServiceInfo): SpeakerCandidate? {
        val addresses = if (Build.VERSION.SDK_INT >= 34) {
            info.hostAddresses
        } else {
            @Suppress("DEPRECATION")
            listOfNotNull(info.host)
        }
        val address = addresses.firstOrNull { it is Inet4Address } ?: addresses.firstOrNull() ?: return null
        val host = address.hostAddress ?: return null

        fun txt(key: String): String? = info.attributes[key]
            ?.let { String(it, StandardCharsets.UTF_8) }
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val deviceId = txt("deviceID") ?: txt("boxDeviceID")
        val serviceName = info.serviceName?.takeIf { it.isNotBlank() } ?: host
        val friendlyName = txt("friendlyName") ?: serviceName
        val key = deviceId ?: "$serviceName@$host"

        return SpeakerCandidate(
            key = key,
            serviceName = serviceName,
            friendlyName = friendlyName,
            host = host,
            advertisedPort = info.port,
            model = txt("model"),
            version = txt("version")
        )
    }

    private fun acquireMulticastLock() {
        try {
            multicastLock = wifiManager.createMulticastLock("STRRemote-mdns").apply {
                setReferenceCounted(false)
                acquire()
            }
        } catch (_: SecurityException) {
            multicastLock = null
        }
    }
}
