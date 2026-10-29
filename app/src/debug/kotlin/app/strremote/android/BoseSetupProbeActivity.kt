package app.strremote.android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PatternMatcher
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL

/**
 * BUILD-0012 / T02 — debug-only, non-destructive SoundTouch 20 setup Wi-Fi probe.
 *
 * Uses system-consented WifiNetworkSpecifier; does not bind the process to the
 * requested Network. All GETs are scoped to the returned Network. No credentials,
 * POST requests, WebView, Bose firmware writes or persistent results.
 */
internal class BoseSetupProbeActivity : Activity() {
    private companion object {
        const val PERMISSION_REQUEST = 12012
        const val NETWORK_REQUEST_TIMEOUT_MS = 30_000
        const val HTTP_TIMEOUT_MS = 1_800
        const val BACKGROUND_CLEANUP_DELAY_MS = 2_000L
        // Give Android's Wi-Fi network factory time to release an earlier picker
        // before registering a fresh request when the speaker becomes ready.
        const val REQUEST_RESTART_SETTLE_MS = 400L
        const val ACCESS_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK"
        data class LocalProbe(val host: String, val port: Int, val path: String)
        val LOCAL_PROBES = listOf(
            LocalProbe("192.0.2.1", 80, "/"),
            LocalProbe("192.0.2.1", 8090, "/"),
            // / on port 8090 may return 404 even when the Bose API works.
            LocalProbe("192.0.2.1", 8090, "/info"),
            LocalProbe("192.168.1.1", 80, "/"),
            LocalProbe("192.168.1.1", 8090, "/")
        )
    }

    private val connectivity by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var ssidInput: EditText
    private lateinit var startButton: Button
    private lateinit var cancelButton: Button
    private lateinit var statusView: TextView
    private lateinit var resultsView: TextView

    private var activeCallback: ConnectivityManager.NetworkCallback? = null
    private var activeWorker: Thread? = null
    private var generation = 0
    private var awaitingConsent = false
    private var visible = false
    private var backgroundCleanup: Runnable? = null
    private var scheduledNetworkRequest: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        if (Build.VERSION.SDK_INT < 29) {
            statusView.text = "Nicht verfügbar: Dieser PoC benötigt Android 10 oder neuer."
            startButton.isEnabled = false
        }
    }

    private fun buildUi() {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(24))
            setBackgroundColor(Color.rgb(246, 248, 251))
        }
        fun paragraph(text: String, size: Float, color: Int): TextView =
            TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(color)
                setPadding(0, 0, 0, dp(12))
            }

        column.addView(
            paragraph("Bose Wi-Fi PoC · BUILD-0012", 23f, Color.rgb(21, 46, 70))
        )
        column.addView(
            paragraph(
                "SoundTouch 20 Series III (355589-SM2) · Originalfirmware.\n" +
                    "Nur lesender Test: keine Passwörter, kein Werksreset, kein WLAN-Profil.",
                15f,
                Color.rgb(45, 63, 81)
            )
        )
        column.addView(
            paragraph(
                "1. Lautsprecher einschalten. Preset 2 + Lautstärke − ca. 5 Sekunden " +
                    "gedrückt halten, bis die WLAN-LED gelb leuchtet.\n" +
                    "2. Optional die vollständige Bose-Setup-SSID eingeben. " +
                    "Ohne Eingabe zeigt Android passende Netze mit Präfix „Bose“ an.\n" +
                    "3. „Setup-WLAN anfordern“ wählen und die Android-Abfrage bestätigen. " +
                    "Fehlt Internet, trotzdem mit dem Bose-Netz verbinden.\n" +
                    "Ist der Lautsprecher erst später im Setup-Modus, Androids " +
                    "Netzauswahl schließen und hier „Erneut versuchen“ tippen.",
                15f,
                Color.rgb(45, 63, 81)
            )
        )

        ssidInput = EditText(this).apply {
            hint = "Exakte Bose-SSID (optional)"
            inputType = InputType.TYPE_CLASS_TEXT
            isSingleLine = true
            setTextColor(Color.rgb(21, 46, 70))
            setHintTextColor(Color.GRAY)
        }
        column.addView(
            ssidInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        startButton = Button(this).apply {
            text = "Setup-WLAN anfordern"
            isAllCaps = false
            setOnClickListener { startOrRequestPermission() }
        }
        cancelButton = Button(this).apply {
            text = "Trennen / Abbrechen"
            isAllCaps = false
            setOnClickListener {
                closeSession()
                statusView.text = "Test abgebrochen. Temporäre Netzwerk-Anforderung freigegeben."
            }
        }
        column.addView(startButton)
        column.addView(cancelButton)

        statusView = paragraph(
            "Bereit. Der PoC verbindet sich nicht ohne deine Android-Freigabe.",
            16f,
            Color.rgb(21, 70, 112)
        )
        resultsView = paragraph(
            "Nach erfolgreicher Verbindung werden nur HTTP-Status und Content-Type " +
                "der lokalen Kandidaten angezeigt; kein Antwortinhalt wird gespeichert.",
            14f,
            Color.rgb(45, 63, 81)
        )
        resultsView.setTextIsSelectable(true)
        column.addView(statusView)
        column.addView(resultsView)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(column)
        }
        setContentView(scroll)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun requiredPermissions(): List<String> = buildList {
        when {
            Build.VERSION.SDK_INT in 29..32 -> {
                // Android 12+ requires COARSE and FINE to be requested together.
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            Build.VERSION.SDK_INT >= 33 ->
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        if (Build.VERSION.SDK_INT >= 37) add(ACCESS_LOCAL_NETWORK)
    }

    private fun startOrRequestPermission() {
        if (Build.VERSION.SDK_INT < 29) return

        // CHANGE_NETWORK_STATE is a normal install-time permission, not a
        // runtime permission. A missing manifest entry causes requestNetwork()
        // to throw SecurityException before Android shows the Wi-Fi picker.
        if (checkSelfPermission(Manifest.permission.CHANGE_NETWORK_STATE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            statusView.text =
                "PoC-Buildfehler: CHANGE_NETWORK_STATE fehlt. " +
                    "Bitte die aktualisierte Debug-APK installieren."
            return
        }

        val missing = requiredPermissions().filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            statusView.text = "Android benötigt zunächst die WLAN-/Netzwerkberechtigung."
            requestPermissions(missing.toTypedArray(), PERMISSION_REQUEST)
            return
        }
        requestSetupNetwork()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQUEST) return
        if (grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED } &&
            requiredPermissions().all {
                checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            requestSetupNetwork()
        } else {
            statusView.text = "Berechtigung nicht erteilt. Ohne Freigabe kein Setup-WLAN-Test."
        }
    }

    @android.annotation.TargetApi(29)
    private fun requestSetupNetwork() {
        closeSession()
        val ssid = ssidInput.text.toString().trim()
        if (ssid.toByteArray(Charsets.UTF_8).size > 32) {
            statusView.text = "SSID zu lang (maximal 32 UTF-8-Bytes)."
            return
        }

        val specifier = try {
            WifiNetworkSpecifier.Builder().apply {
                if (ssid.isEmpty()) {
                    setSsidPattern(PatternMatcher("Bose", PatternMatcher.PATTERN_PREFIX))
                } else {
                    setSsid(ssid)
                }
            }.build()
        } catch (e: IllegalArgumentException) {
            statusView.text = "SSID nicht zulässig. Bitte die WLAN-Bezeichnung prüfen."
            return
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

        val token = ++generation
        awaitingConsent = true
        // Keep retry actionable without leaving this Activity. The previous
        // request is unregistered by closeSession() and the new one is fresh.
        startButton.text = "Erneut versuchen"
        startButton.isEnabled = true
        resultsView.text = "Noch keine Probe-Ergebnisse."
        statusView.text =
            "Neue WLAN-Suche wird gestartet. Androids Abfrage bestätigen " +
                "(max. 30 s). Ist die Box noch nicht bereit: " +
                "Auswahl schließen, dann hier „Erneut versuchen“. "+ "Das Bose-WLAN braucht kein Internet."

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    if (token != generation) return@runOnUiThread
                    awaitingConsent = false
                    statusView.text = "Bose-WLAN von Android bereitgestellt. Prüfe lokale HTTP-Endpunkte …"
                    probeReadOnly(network, token)
                    scheduleBackgroundCleanup()
                }
            }

            override fun onUnavailable() {
                runOnUiThread {
                    if (token != generation) return@runOnUiThread
                    closeSession()
                    statusView.text =
                        "Noch kein Bose-Setup-WLAN verfügbar oder Anfrage abgebrochen. " +
                            "Sobald die WLAN-LED gelb leuchtet, hier „Erneut versuchen“ " +
                            "wählen. Dies startet eine neue Android-Suche."
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    if (token != generation) return@runOnUiThread
                    closeSession()
                    statusView.text =
                        "Setup-WLAN getrennt. Für eine neue Verbindung hier " +
                            "„Erneut versuchen“ wählen. Messwerte bleiben angezeigt."
                }
            }
        }
        // Android's system picker may retain a stale scan if the speaker had
        // not entered setup mode yet. A retry must explicitly unregister the
        // old callback and issue a BRAND NEW NetworkRequest, not rescan STR.
        val issueFreshRequest = Runnable {
            if (token != generation || isFinishing || isDestroyed) return@Runnable
            scheduledNetworkRequest = null
            activeCallback = callback
            try {
                connectivity.requestNetwork(request, callback, NETWORK_REQUEST_TIMEOUT_MS)
            } catch (e: SecurityException) {
                closeSession()
                statusView.text =
                    "Android hat die WLAN-Anforderung verweigert (SecurityException). " +
                        "Bitte WLAN und die erteilten App-Berechtigungen prüfen."
            } catch (e: IllegalArgumentException) {
                closeSession()
                statusView.text = "Netzwerk-Anforderung abgelehnt: ungültige Android-Konfiguration."
            } catch (e: RuntimeException) {
                closeSession()
                statusView.text =
                    "Netzwerk-Anforderung fehlgeschlagen (${e.javaClass.simpleName})."
            }
        }
        scheduledNetworkRequest = issueFreshRequest
        handler.postDelayed(issueFreshRequest, REQUEST_RESTART_SETTLE_MS)
    }

    private fun probeReadOnly(network: Network, token: Int) {
        activeWorker?.interrupt()
        val worker = Thread({
            val observations = mutableListOf<String>()
            for (probe in LOCAL_PROBES) {
                if (Thread.currentThread().isInterrupted) return@Thread
                val target = "http://${probe.host}:${probe.port}${probe.path}"
                var connection: HttpURLConnection? = null
                val observation = try {
                    connection = network.openConnection(URL(target)) as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.instanceFollowRedirects = false
                    connection.connectTimeout = HTTP_TIMEOUT_MS
                    connection.readTimeout = HTTP_TIMEOUT_MS
                    connection.useCaches = false

                    // Intentionally inspect headers/status only; never collect page contents.
                    val code = connection.responseCode
                    val mediaType = connection.contentType
                        ?.substringBefore(';')
                        ?.take(60)
                        ?: "unbekannt"
                    "$target -> HTTP $code; Type: $mediaType"
                } catch (e: Exception) {
                    "$target -> nicht erreichbar (${e.javaClass.simpleName})"
                } finally {
                    connection?.disconnect()
                }
                observations.add(observation)
            }
            runOnUiThread {
                if (token != generation) return@runOnUiThread
                resultsView.text = observations.joinToString("\n")
                closeSession()
                statusView.text =
                    "Phase-A-Messung beendet. Temporäre Bose-Netzwerk-Anforderung " +
                        "freigegeben. Ergebnisse bitte ohne private Kennungen dokumentieren."
            }
        }, "BoseSetupReadOnlyProbe")
        activeWorker = worker
        worker.start()
    }

    private fun scheduleBackgroundCleanup() {
        if (visible) return
        cancelBackgroundCleanup()
        val action = Runnable {
            if (!visible && !awaitingConsent && activeCallback != null) {
                closeSession()
                statusView.text = "Test im Hintergrund beendet; Setup-Netzwerk freigegeben."
            }
        }
        backgroundCleanup = action
        handler.postDelayed(action, BACKGROUND_CLEANUP_DELAY_MS)
    }

    private fun cancelBackgroundCleanup() {
        backgroundCleanup?.let(handler::removeCallbacks)
        backgroundCleanup = null
    }

    private fun closeSession() {
        ++generation
        awaitingConsent = false
        scheduledNetworkRequest?.let(handler::removeCallbacks)
        scheduledNetworkRequest = null
        cancelBackgroundCleanup()
        activeWorker?.interrupt()
        activeWorker = null
        activeCallback?.let { callback ->
            try {
                connectivity.unregisterNetworkCallback(callback)
            } catch (_: IllegalArgumentException) {
                // Callback may already have been removed by Android.
            } catch (_: SecurityException) {
                // Do not keep the UI in a connected state on permission revocation.
            }
        }
        activeCallback = null
        if (::startButton.isInitialized) startButton.isEnabled = Build.VERSION.SDK_INT >= 29
    }

    override fun onStart() {
        super.onStart()
        visible = true
        cancelBackgroundCleanup()
    }

    override fun onStop() {
        visible = false
        // Android's own network-consent UI can temporarily stop this Activity.
        // Preserve the pending 30 s request; release connected networks in background.
        if (!awaitingConsent && activeCallback != null) {
            scheduleBackgroundCleanup()
        }
        super.onStop()
    }

    override fun onDestroy() {
        closeSession()
        super.onDestroy()
    }
}
