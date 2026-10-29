package app.strremote.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.os.PatternMatcher
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
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
        const val LOG_TAG = "StrBoseSetup"
        const val NETWORK_REQUEST_TIMEOUT_MS = 20_000
        const val EXTRA_RESTART_AND_SEARCH = "bose_poc.restart_and_search"
        const val HTTP_TIMEOUT_MS = 1_800
        const val BACKGROUND_CLEANUP_DELAY_MS = 2_000L
        const val ACCESS_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK"
        data class LocalProbe(val host: String, val port: Int, val path: String)
        val LOCAL_PROBES = listOf(
            // Validated on the reference SoundTouch 20 Series III.
            // Remove known timeouts (192.168.1.1) and non-API root (8090/).
            LocalProbe("192.0.2.1", 80, "/"),
            LocalProbe("192.0.2.1", 8090, "/info")
        )
    }

    private val connectivity by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val handler = Handler(Looper.getMainLooper())

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
    private var hasStartedNetworkRequest = false
    private var resumeWithFreshSearch = false
    private var requestStartedAtMs = 0L
    private val debugSessionId = Integer.toHexString(System.identityHashCode(this))

    /**
     * Diagnostic events deliberately exclude SSIDs, BSSIDs, MACs, IP addresses,
     * passwords, response bodies and Android Network objects. Debug source only.
     */
    private fun trace(event: String) {
        val elapsed = if (requestStartedAtMs > 0L) {
            SystemClock.elapsedRealtime() - requestStartedAtMs
        } else {
            -1L
        }
        Log.d(
            LOG_TAG,
            "session=$debugSessionId event=$event elapsedMs=$elapsed api=${Build.VERSION.SDK_INT}"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        resumeWithFreshSearch = intent.getBooleanExtra(EXTRA_RESTART_AND_SEARCH, false)
        trace("activity_created freshRetry=$resumeWithFreshSearch")
        if (Build.VERSION.SDK_INT < 29) {
            statusView.text = "Nicht verfügbar: Dieser PoC benötigt Android 10 oder neuer."
            startButton.isEnabled = false
        }
    }

    private fun buildUi() {
        val ink = Color.rgb(23, 46, 67)
        val muted = Color.rgb(54, 70, 86)
        val accent = Color.rgb(18, 69, 113)
        val backgroundColor = Color.rgb(246, 248, 251)

        fun panelBackground(): GradientDrawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dp(12).toFloat()
            setStroke(dp(1), Color.rgb(218, 226, 234))
        }

        fun paragraph(value: String, size: Float, color: Int): TextView =
            TextView(this).apply {
                text = value
                textSize = size
                setTextColor(color)
                includeFontPadding = false
                setLineSpacing(0f, 1.13f)
                setPadding(0, 0, 0, dp(10))
            }

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(24))
            setBackgroundColor(backgroundColor)
        }

        column.addView(paragraph("Bose WLAN-Einrichtung", 22f, ink).apply {
            setTypeface(typeface, Typeface.BOLD)
        })
        column.addView(
            paragraph("SoundTouch 20 Series III · Originalfirmware", 14f, muted)
        )

        val steps = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(6))
            background = panelBackground()
        }
        fun step(title: String, detail: String) {
            steps.addView(paragraph(title, 16f, ink).apply {
                setTypeface(typeface, Typeface.BOLD)
            })
            steps.addView(paragraph(detail, 14.5f, muted))
        }
        step(
            "1 · Lautsprecher vorbereiten",
            "Einschalten, Preset 2 und Lautstärke − etwa fünf Sekunden gedrückt halten, " +
                "bis die WLAN-LED gelb leuchtet."
        )
        step(
            "2 · Bose-Setup-WLAN verbinden",
            "Auf „Setup-WLAN suchen“ tippen und das Bose-Netz in Android auswählen. " +
                "Die Verbindungsanfrage anschließend bestätigen."
        )
        step(
            "3 · Bose-Schnittstellen prüfen",
            "Die App prüft lesend die lokale Bose-API und die angebotenen " +
                "WLAN-Einrichtungsfunktionen. Danach gibt sie die temporäre " +
                "Verbindung automatisch frei."
        )
        column.addView(
            steps,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        )

        column.addView(
            paragraph(
                "Nur lesender Test. Keine WLAN-Passwörter, kein Werksreset, " +
                    "keine Änderung der Bose-Firmware.",
                13.5f,
                muted
            )
        )

        startButton = Button(this).apply {
            text = "Setup-WLAN suchen"
            isAllCaps = false
            minimumHeight = dp(48)
            setOnClickListener {
                if (hasStartedNetworkRequest) {
                    restartWizardForFreshSearch()
                } else {
                    startOrRequestPermission()
                }
            }
        }
        cancelButton = Button(this).apply {
            text = "Trennen / Abbrechen"
            isAllCaps = false
            minimumHeight = dp(48)
            setOnClickListener {
                // Return to the Devices screen after releasing the Bose network.
                closeSession()
                finish()
            }
        }
        column.addView(
            startButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
        )
        column.addView(
            cancelButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        statusView = paragraph(
            "Bereit. Die Verbindung erfolgt nur nach Android-Nutzerbestätigung.",
            15f,
            accent
        ).apply {
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = panelBackground()
        }
        resultsView = paragraph(
            "Hier erscheinen die Ergebnisse der lokalen HTTP-Prüfungen " +
                "und der Bose-API-Funktionsliste.",
            14f,
            ink
        ).apply {
            setTextIsSelectable(true)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = panelBackground()
        }
        column.addView(
            statusView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
        )
        column.addView(
            resultsView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(10) }
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(column)
            // Android 15+ edge-to-edge: protect the title from the status bar
            // and the last controls from navigation/gesture insets.
            setOnApplyWindowInsetsListener { view, insets ->
                if (Build.VERSION.SDK_INT >= 30) {
                    val bars = insets.getInsets(WindowInsets.Type.systemBars())
                    view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                } else {
                    @Suppress("DEPRECATION")
                    view.setPadding(
                        insets.systemWindowInsetLeft,
                        insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight,
                        insets.systemWindowInsetBottom
                    )
                }
                insets
            }
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

    /**
     * A same-Activity retry can inherit Android's stale Wi-Fi selection state.
     * The tester confirmed that fully restarting onboarding recovers discovery,
     * so retry now reproduces that successful lifecycle in one tap.
     *
     * Do not delay or otherwise modify the proven FIRST requestNetwork() path.
     */
    private fun restartWizardForFreshSearch() {
        trace("retry_new_activity")
        closeSession()
        val restart = Intent(this, BoseSetupProbeActivity::class.java).apply {
            putExtra(EXTRA_RESTART_AND_SEARCH, true)
        }
        startActivity(restart)
        finish()
    }

    private fun startOrRequestPermission() {
        trace("start_pressed")
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
            trace("runtime_permission_request count=${missing.size}")
            statusView.text = "Android benötigt zunächst die WLAN-/Netzwerkberechtigung."
            requestPermissions(missing.toTypedArray(), PERMISSION_REQUEST)
            return
        }
        trace("permissions_granted")
        requestSetupNetwork()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQUEST) return
        trace("runtime_permission_result allGranted=${grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }}")
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
        trace("request_prepare")
        closeSession()
        cancelButton.text = "Trennen / Abbrechen"

        // No manual SSID entry: Android finds nearby "Bose..." setup networks.
        // A real user must still approve the local-only Wi-Fi connection.
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsidPattern(PatternMatcher("Bose", PatternMatcher.PATTERN_PREFIX))
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

        val token = ++generation
        requestStartedAtMs = SystemClock.elapsedRealtime()
        trace("request_configured token=$token timeoutMs=$NETWORK_REQUEST_TIMEOUT_MS")
        hasStartedNetworkRequest = true
        awaitingConsent = true
        // Retry opens a fresh Activity and a fresh WifiNetworkSpecifier request.
        // No overlapping Android picker/network callbacks survive the restart.
        startButton.text = "Erneut versuchen"
        startButton.isEnabled = true
        resultsView.text = "Noch keine Probe-Ergebnisse."
        statusView.text =
            "Android sucht das Bose-Setup-WLAN (bis zu 20 Sekunden). " +
                "Bitte die Systemabfrage bestätigen. Ist die Box noch nicht bereit, " +
                "die Auswahl schließen und „Erneut versuchen“ wählen."

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trace("callback_onAvailable token=$token")
                runOnUiThread {
                    if (token != generation) {
                        trace("callback_onAvailable_stale token=$token")
                        return@runOnUiThread
                    }
                    trace("ui_onAvailable token=$token")
                    awaitingConsent = false
                    val seconds = (SystemClock.elapsedRealtime() - requestStartedAtMs) / 1000L
                    statusView.text =
                        "Bose-WLAN nach ${seconds} s verbunden. Prüfe lokale HTTP-Endpunkte …"
                    probeReadOnly(network, token)
                    scheduleBackgroundCleanup()
                }
            }

            override fun onUnavailable() {
                trace("callback_onUnavailable token=$token")
                runOnUiThread {
                    if (token != generation) {
                        trace("callback_onUnavailable_stale token=$token")
                        return@runOnUiThread
                    }
                    trace("ui_onUnavailable token=$token")
                    closeSession()
                    val seconds = (SystemClock.elapsedRealtime() - requestStartedAtMs) / 1000L
                    statusView.text =
                        "Android hat innerhalb von ${seconds} s keine Verbindung bereitgestellt " +
                            "oder die Auswahl wurde abgebrochen. " +
                            "Wenn die Bose-WLAN-LED gelb leuchtet, „Erneut versuchen“ wählen."
                }
            }

            override fun onLost(network: Network) {
                trace("callback_onLost token=$token")
                runOnUiThread {
                    if (token != generation) {
                        trace("callback_onLost_stale token=$token")
                        return@runOnUiThread
                    }
                    trace("ui_onLost token=$token")
                    closeSession()
                    statusView.text =
                        "Setup-WLAN getrennt. Für eine neue Verbindung hier " +
                            "„Erneut versuchen“ wählen. Messwerte bleiben angezeigt."
                }
            }
        }
        // Keep the original, device-validated immediate connection path.
        // Retry instead recreates the entire PoC Activity after callback release.
        activeCallback = callback
        try {
            trace("requestNetwork_call token=$token")
            connectivity.requestNetwork(request, callback, NETWORK_REQUEST_TIMEOUT_MS)
            trace("requestNetwork_returned token=$token")
        } catch (e: SecurityException) {
            trace("requestNetwork_error SecurityException")
            closeSession()
            statusView.text =
                "Android hat die WLAN-Anforderung verweigert (SecurityException). " +
                    "Bitte WLAN und die erteilten App-Berechtigungen prüfen."
        } catch (e: IllegalArgumentException) {
            trace("requestNetwork_error IllegalArgumentException")
            closeSession()
            statusView.text = "Netzwerk-Anforderung abgelehnt: ungültige Android-Konfiguration."
        } catch (e: RuntimeException) {
            trace("requestNetwork_error ${e.javaClass.simpleName}")
            closeSession()
            statusView.text = "Netzwerk-Anforderung fehlgeschlagen (${e.javaClass.simpleName})."
        }
    }

    private fun probeReadOnly(network: Network, token: Int) {
        trace("http_probe_start token=$token")
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
                // Target is one of two compile-time Bose endpoints. No response
                // body, host identifiers or credentials are ever emitted.
                trace("http_probe_result token=$token index=${observations.size} result=${if (observation.contains("-> HTTP 200")) "http200" else "other"}")
            }
            if (Thread.currentThread().isInterrupted) return@Thread
            // Phase B1: bounded, read-only capability inquiry. Only the names
            // of three known Wi-Fi-related API endpoints are surfaced; the
            // original XML body is discarded and cannot enter logs/UI.
            trace("b1_supported_urls_start token=$token")
            observations.add(BoseSupportedUrlsProbe.inspect(network))
            trace("b1_supported_urls_finished token=$token")
            runOnUiThread {
                if (token != generation) return@runOnUiThread
                trace("http_probe_complete token=$token")
                resultsView.text = observations.joinToString("\n")
                closeSession()
                cancelButton.text = "Zurück zu Geräte"
                statusView.text =
                    "Phase-A-Messung abgeschlossen. Die temporäre Bose-WLAN-Verbindung " +
                        "wurde automatisch freigegeben. Android kehrt zum Heim-WLAN zurück."
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
        trace("session_close callbackRegistered=${activeCallback != null}")
        ++generation
        awaitingConsent = false
        cancelBackgroundCleanup()
        activeWorker?.interrupt()
        activeWorker = null
        activeCallback?.let { callback ->
            try {
                connectivity.unregisterNetworkCallback(callback)
                trace("callback_unregistered")
            } catch (_: IllegalArgumentException) {
                trace("callback_unregistered_already")
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

    override fun onResume() {
        super.onResume()
        trace("activity_resumed retry=$resumeWithFreshSearch")
        if (resumeWithFreshSearch) {
            resumeWithFreshSearch = false
            // Post from resumed UI: no request while the previous Activity is
            // closing or the new Activity has not yet entered foreground.
            handler.post {
                if (!isFinishing && !isDestroyed) startOrRequestPermission()
            }
        }
    }

    override fun onStop() {
        visible = false
        // Android's own network-consent UI can temporarily stop this Activity.
        // Preserve the pending 20 s request; release connected networks in background.
        if (!awaitingConsent && activeCallback != null) {
            scheduleBackgroundCleanup()
        }
        super.onStop()
    }

    override fun onDestroy() {
        trace("activity_destroyed")
        closeSession()
        super.onDestroy()
    }
}
