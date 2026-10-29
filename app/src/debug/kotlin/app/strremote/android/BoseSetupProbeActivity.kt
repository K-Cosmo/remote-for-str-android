package app.strremote.android

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
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
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL

/**
 * BUILD-0012 / T02 — debug-only, non-destructive SoundTouch 20 setup Wi-Fi probe.
 *
 * Uses system-consented WifiNetworkSpecifier; does not bind the process to the
 * requested Network. GET probes remain read-only. A separate opt-in debug-only B2
 * hardware mode can perform one confirmed WLAN-profile POST; no credentials are
 * saved or logged, and API acknowledgement alone is not considered provisioning success.
 */
internal class BoseSetupProbeActivity : Activity() {
    private companion object {
        const val PERMISSION_REQUEST = 12012
        const val LOG_TAG = "StrBoseSetup"
        const val NETWORK_REQUEST_TIMEOUT_MS = 20_000
        const val EXTRA_RESTART_AND_SEARCH = "bose_poc.restart_and_search"
        const val EXTRA_B2_MODE = "bose_poc.b2_mode"
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
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var preparationPage: LinearLayout
    private lateinit var connectionPage: LinearLayout
    private lateinit var resultPage: LinearLayout
    private lateinit var stepLabel: TextView
    private lateinit var pageTitle: TextView
    private lateinit var resultText: TextView
    private lateinit var detailsButton: Button
    private lateinit var chooseWifiButton: Button
    private lateinit var manualWifiButton: Button
    private lateinit var selectedWifiView: TextView
    private lateinit var ssidLabel: TextView
    private lateinit var resultSpinner: ProgressBar
    private lateinit var retryVerificationButton: Button
    private var currentPage = 1
    private lateinit var pageScroll: ScrollView
    private lateinit var b2Panel: LinearLayout
    private lateinit var scanButton: Button
    private lateinit var surveyStatusView: TextView
    private lateinit var ssidField: EditText
    private lateinit var passwordField: EditText
    private lateinit var submitButton: Button
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
    private var b2Mode = true
    private var b2Network: Network? = null
    private var b2Verified = false
    private var surveySupported = false
    private var surveyInFlight = false
    private var surveyCandidates: List<BoseWirelessSiteSurvey.Candidate> = emptyList()
    private var postAttempted = false
    private var postInFlight = false
    private var b2NetworkLostDuringPost = false
    private var boseDeviceId: String? = null
    private var pendingWifiSsid: String? = null
    private var lastBoseSetupNetwork: Network? = null
    private var profileResponseAcknowledged = false
    private var lanVerification: BoseLanVerification? = null
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
        b2Mode = true // Debug-only: the guided setup is now the default.
        resumeWithFreshSearch = intent.getBooleanExtra(EXTRA_RESTART_AND_SEARCH, false)
        buildUi()
        trace("activity_created freshRetry=$resumeWithFreshSearch")
        if (Build.VERSION.SDK_INT < 29) {
            statusView.text = "Nicht verfügbar: Dieser PoC benötigt Android 10 oder neuer."
            nextButton.isEnabled = false
            startButton.isEnabled = false
        }
    }

    private fun buildUi() {
        val ink = Color.rgb(23, 46, 67)
        val muted = Color.rgb(66, 84, 101)
        val accent = Color.rgb(18, 69, 113)

        fun card(stroke: Int = Color.rgb(216, 226, 236)): GradientDrawable =
            GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), stroke)
            }

        fun text(value: String, size: Float = 15f, color: Int = muted): TextView =
            TextView(this).apply {
                this.text = value
                textSize = size
                setTextColor(color)
                setLineSpacing(0f, 1.15f)
                setPadding(0, 0, 0, dp(12))
            }

        fun heading(value: String) = text(value, 17f, ink).apply {
            setTypeface(typeface, Typeface.BOLD)
        }

        fun panel(): LinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(3), dp(14), dp(3), dp(14))
            visibility = View.GONE
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(246, 248, 251))
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(8))
        }
        header.addView(text("SoundTouch einrichten", 14f, accent))
        stepLabel = text("Schritt 1 von 3", 13f, accent).apply {
            setTypeface(typeface, Typeface.BOLD)
        }
        header.addView(stepLabel)
        pageTitle = heading("Lautsprecher vorbereiten").apply { textSize = 24f }
        header.addView(pageTitle)
        root.addView(header)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), 0, dp(18), dp(18))
        }
        preparationPage = panel()
        preparationPage.addView(text(
            "Wir verbinden deine SoundTouch mit deinem WLAN. Bereite dazu zuerst die Box vor."
        ))
        val instructionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = card()
            setPadding(dp(16), dp(16), dp(16), dp(8))
        }
        instructionCard.addView(heading("Am SoundTouch 20 Series III"))
        instructionCard.addView(text(
            "1. Schalte den Lautsprecher ein.\n" +
                "2. Halte Preset 2 und Leiser (−) gleichzeitig etwa fünf Sekunden gedrückt.\n" +
                "3. Warte, bis die WLAN-Anzeige gelb leuchtet."
        ))
        preparationPage.addView(instructionCard)
        preparationPage.addView(text(
            "Die Box öffnet dann ein eigenes Bose-WLAN. Du musst dessen Namen nicht eingeben.",
            14f
        ).apply { setPadding(0, dp(16), 0, dp(12)) })
        nextButton = Button(this).apply {
            text = "Die WLAN-Anzeige leuchtet gelb"
            isAllCaps = false
            minHeight = dp(54)
            setOnClickListener { showPage(2) }
        }
        preparationPage.addView(nextButton)
        content.addView(preparationPage)

        connectionPage = panel()
        connectionPage.addView(text(
            "Android öffnet gleich eine WLAN-Auswahl. Wähle dort das Bose-Setup-WLAN aus."
        ))
        val connectionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = card()
            setPadding(dp(16), dp(16), dp(16), dp(8))
        }
        connectionCard.addView(heading("So funktioniert es"))
        connectionCard.addView(text(
            "1. Tippe auf „Mit SoundTouch verbinden“.\n" +
                "2. Wähle das Bose-WLAN im Android-Fenster aus.\n" +
                "3. Bestätige die Verbindung, auch wenn sie kein Internet bietet."
        ))
        connectionPage.addView(connectionCard)
        connectionPage.addView(text(
            "Nach erfolgreicher Verbindung geht es automatisch weiter.",
            14f
        ).apply { setPadding(0, dp(14), 0, dp(10)) })
        startButton = Button(this).apply {
            text = "Mit SoundTouch verbinden"
            isAllCaps = false
            minHeight = dp(54)
            setOnClickListener {
                if (b2Verified && b2Network != null) showPage(3)
                else if (hasStartedNetworkRequest) restartWizardForFreshSearch()
                else startOrRequestPermission()
            }
        }
        connectionPage.addView(startButton)
        connectionPage.addView(text(
            "Wenn die Box nicht angezeigt wird: Prüfe, ob die WLAN-Anzeige gelb leuchtet, " +
                "und wähle „Erneut versuchen“.",
            14f
        ).apply { setPadding(0, dp(12), 0, 0) })
        content.addView(connectionPage)

        b2Panel = panel()
        b2Panel.addView(text(
            "Wähle das WLAN aus, in dem du die SoundTouch verwenden möchtest. " +
                "Die Liste zeigt die Netze, die die Box selbst empfängt."
        ))
        b2Panel.addView(heading("Dein WLAN"))
        selectedWifiView = text("Noch kein WLAN ausgewählt.", 16f, ink)
        b2Panel.addView(selectedWifiView)
        chooseWifiButton = Button(this).apply {
            text = "WLAN auswählen"
            isAllCaps = false
            minHeight = dp(54)
            isEnabled = false
            setOnClickListener { showWifiPicker() }
        }
        b2Panel.addView(chooseWifiButton)
        scanButton = Button(this).apply {
            text = "WLANs erneut suchen"
            isAllCaps = false
            minHeight = dp(48)
            isEnabled = false
            setOnClickListener { runBoseSurvey() }
        }
        b2Panel.addView(scanButton)
        surveyStatusView = text("Suche nach WLANs …", 14f).apply {
            setPadding(0, dp(10), 0, dp(8))
        }
        b2Panel.addView(surveyStatusView)
        // An extra popup owns the network list; do not make users scroll past
        // dozens of SSIDs to reach the password and submit button.
        manualWifiButton = Button(this).apply {
            text = "WLAN-Name manuell eingeben"
            isAllCaps = false
            setOnClickListener {
                ssidLabel.visibility = View.VISIBLE
                ssidField.visibility = View.VISIBLE
                ssidField.requestFocus()
            }
        }
        b2Panel.addView(manualWifiButton)
        ssidLabel = heading("WLAN-Name (SSID)").apply { visibility = View.GONE }
        b2Panel.addView(ssidLabel)
        ssidField = EditText(this).apply {
            hint = "WLAN wählen oder Namen eingeben"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            setSingleLine(true)
            textSize = 16f
            setTextColor(ink)
            setHintTextColor(muted)
            background = card(Color.rgb(126, 148, 169))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            minHeight = dp(54)
            isSaveEnabled = false
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            visibility = View.GONE
        }
        b2Panel.addView(ssidField)
        b2Panel.addView(heading("WLAN-Passwort").apply {
            setPadding(0, dp(18), 0, dp(10))
        })
        passwordField = EditText(this).apply {
            hint = "Passwort für das ausgewählte WLAN"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true)
            textSize = 16f
            setTextColor(ink)
            setHintTextColor(muted)
            background = card(Color.rgb(126, 148, 169))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            minHeight = dp(54)
            isSaveEnabled = false
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }
        b2Panel.addView(passwordField)
        b2Panel.addView(text(
            "Für den ersten Test nur ein separates WPA2-WLAN mit eigenem Passwort verwenden. " +
                "Die Übertragung an Bose erfolgt ohne Verschlüsselung.",
            13.5f
        ).apply { setPadding(0, dp(12), 0, dp(8)) })
        submitButton = Button(this).apply {
            text = "WLAN auf SoundTouch übertragen"
            isAllCaps = false
            minHeight = dp(54)
            setOnClickListener { confirmB2Post() }
        }
        b2Panel.addView(submitButton)
        content.addView(b2Panel)

        resultPage = panel()
        resultPage.addView(heading("Wir richten deine SoundTouch ein"))
        resultSpinner = ProgressBar(this, null, android.R.attr.progressBarStyleLarge).apply {
            isIndeterminate = true
            visibility = View.GONE
        }
        resultPage.addView(resultSpinner, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(18) })
        resultText = text(
            "Die WLAN-Einstellungen werden an deine SoundTouch übertragen.",
            16f, ink
        )
        resultPage.addView(resultText)
        retryVerificationButton = Button(this).apply {
            text = "SoundTouch erneut suchen"
            isAllCaps = false
            visibility = View.GONE
            setOnClickListener { beginLanVerification() }
        }
        resultPage.addView(retryVerificationButton)
        resultPage.addView(text(
            "Nach dem Senden kehrt Android normalerweise selbst zu deinem WLAN zurück. " +
                "Die App sucht die SoundTouch anschließend in diesem Netzwerk. " +
                "Bei unklarem Ergebnis wird das Passwort nicht erneut übertragen.",
            14f
        ).apply { setPadding(0, dp(12), 0, 0) })
        content.addView(resultPage)

        detailsButton = Button(this).apply {
            text = "Technische Details anzeigen"
            isAllCaps = false
            setOnClickListener {
                val expanding = resultsView.visibility != View.VISIBLE
                resultsView.visibility = if (expanding) View.VISIBLE else View.GONE
                text = if (expanding) "Technische Details ausblenden"
                       else "Technische Details anzeigen"
            }
        }
        content.addView(detailsButton)
        resultsView = text("Diagnose noch nicht verfügbar.", 13f, ink).apply {
            visibility = View.GONE
            setTextIsSelectable(true)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = card()
        }
        content.addView(resultsView)

        pageScroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
        root.addView(pageScroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        statusView = text("", 14f, accent).apply {
            visibility = View.GONE
            setPadding(dp(18), dp(10), dp(18), dp(10))
            background = card()
        }
        root.addView(statusView)

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(14), dp(8), dp(14), dp(14))
        }
        backButton = Button(this).apply {
            text = "Zurück"
            isAllCaps = false
            setOnClickListener {
                when (currentPage) {
                    2 -> {
                        closeSession()
                        hasStartedNetworkRequest = false
                        showPage(1)
                    }
                    3 -> showPage(2)
                }
            }
        }
        cancelButton = Button(this).apply {
            text = "Einrichtung abbrechen"
            isAllCaps = false
            setOnClickListener {
                if (postInFlight) {
                    AlertDialog.Builder(this@BoseSetupProbeActivity)
                        .setTitle("Einrichtung wirklich abbrechen?")
                        .setMessage(
                            "Die WLAN-Daten könnten bereits gesendet worden sein. " +
                                "Bitte prüfe die Box vor einem weiteren Versuch."
                        )
                        .setNegativeButton("Weiter abwarten", null)
                        .setPositiveButton("Trotzdem beenden") { _, _ ->
                            closeSession()
                            finish()
                        }.show()
                } else {
                    closeSession()
                    finish()
                }
            }
        }
        footer.addView(backButton, LinearLayout.LayoutParams(0, dp(54), 1f))
        footer.addView(cancelButton, LinearLayout.LayoutParams(0, dp(54), 2f))
        root.addView(footer)

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(
                    insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom
                )
            }
            insets
        }
        setContentView(root)
        showPage(if (resumeWithFreshSearch) 2 else 1)
    }

    private fun showPage(page: Int) {
        currentPage = page
        preparationPage.visibility = if (page == 1) View.VISIBLE else View.GONE
        connectionPage.visibility = if (page == 2) View.VISIBLE else View.GONE
        b2Panel.visibility = if (page == 3) View.VISIBLE else View.GONE
        resultPage.visibility = if (page == 4) View.VISIBLE else View.GONE
        stepLabel.text = if (page == 4) "Einrichtung – Abschluss"
                         else "Schritt " + page + " von 3"
        pageTitle.text = when (page) {
            1 -> "Lautsprecher vorbereiten"
            2 -> "Mit SoundTouch verbinden"
            3 -> "Heim-WLAN auswählen"
            else -> "WLAN-Verbindung prüfen"
        }
        statusView.visibility = if (page == 1) View.GONE else View.VISIBLE
        detailsButton.visibility = if (page == 1) View.GONE else View.VISIBLE
        backButton.visibility = if (page == 2 || page == 3) View.VISIBLE else View.GONE
        cancelButton.text = if (page == 4) "Zur Geräteliste" else "Einrichtung abbrechen"
        pageScroll.post { pageScroll.scrollTo(0, 0) }
        trace("wizard_page_" + page)
    }

    private fun showWifiPicker() {
        if (surveyInFlight || postAttempted || currentPage != 3) return
        // List only networks allowed by the conservative WPA2 PoC. Never log SSIDs.
        val available = surveyCandidates.filter { it.selectable }
        if (available.isEmpty()) {
            surveyStatusView.text = "Keine unterstützten WLANs gefunden. Du kannst den Namen selbst eingeben."
            return
        }
        val choices = available.map { candidate ->
            val signal = when {
                candidate.signalDbm == null -> "Signal unbekannt"
                candidate.signalDbm >= -55 -> "Starkes Signal"
                candidate.signalDbm >= -70 -> "Mittleres Signal"
                else -> "Schwaches Signal"
            }
            candidate.ssid + "   ·   " + signal
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("WLAN auswählen")
            .setItems(choices) { _, position ->
                val choice = available[position]
                ssidField.setText(choice.ssid)
                ssidField.visibility = View.GONE
                ssidLabel.visibility = View.GONE
                selectedWifiView.text = "Ausgewählt: " + choice.ssid
                passwordField.text?.clear()
                surveyStatusView.text = "Gib nun das Passwort für dieses WLAN ein."
                pageScroll.post {
                    val top = b2Panel.top + passwordField.top
                    pageScroll.smoothScrollTo(0, top.coerceAtLeast(0))
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun renderSurveyNetworks() {
        // The list is shown in a compact popup rather than inline rows.
        val count = surveyCandidates.count { it.selectable }
        chooseWifiButton.isEnabled = count > 0 && !surveyInFlight && !postAttempted
        chooseWifiButton.text = if (count > 0) "WLAN auswählen ($count gefunden)"
                               else "WLAN auswählen"
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
        if (b2Mode) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        showPage(2)

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
        resultsView.text = "Die Verbindung wird vorbereitet …"
        statusView.text =
            "Android sucht die SoundTouch. Wähle das Bose-WLAN im Systemfenster " +
                "und bestätige die Verbindung."

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
                        "Verbunden! Wir prüfen die SoundTouch und öffnen die WLAN-Auswahl …"
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
                        "Die SoundTouch wurde nicht gefunden. Prüfe, ob die WLAN-Anzeige " +
                            "gelb leuchtet, und tippe auf „Erneut versuchen“."
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
                    if (postInFlight) {
                        b2NetworkLostDuringPost = true
                        trace("b2_network_lost_during_post")
                        statusView.text =
                            "Die SoundTouch wechselt möglicherweise gerade ins neue WLAN. " +
                                "Bitte das Ergebnis abwarten."
                    } else {
                        closeSession()
                        showPage(2)
                        startButton.text = "Erneut versuchen"
                        statusView.text =
                            "Verbindung zur SoundTouch verloren. Prüfe den Einrichtungsmodus " +
                                "und versuche es erneut."
                    }
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
            val b1Report = BoseSupportedUrlsProbe.inspectReport(network)
            val identifiedDeviceId = BoseLanIdentity.getDeviceId(network, "192.0.2.1")
            observations.add(b1Report.summary)
            trace("b1_supported_urls_finished token=$token")
            runOnUiThread {
                if (token != generation) return@runOnUiThread
                trace("http_probe_complete token=$token")
                resultsView.text = observations.joinToString("\n")
                if (b2Mode && b1Report.supportsAddWirelessProfile) {
                    b2Network = network
                    boseDeviceId = identifiedDeviceId
                    b2Verified = true
                    surveySupported = b1Report.supportsWirelessSiteSurvey
                    startButton.text = "Weiter zur WLAN-Auswahl"
                    scanButton.isEnabled = surveySupported
                    surveyStatusView.text = if (surveySupported) {
                        "Suche nach WLANs in Reichweite der SoundTouch …"
                    } else {
                        "Keine WLAN-Liste verfügbar. Bitte den Namen manuell eingeben."
                    }
                    statusView.text =
                        "SoundTouch verbunden. Wähle jetzt ihr zukünftiges WLAN aus."
                    trace("b2_ready token=$token surveySupported=$surveySupported")
                    showPage(3)
                    if (surveySupported) runBoseSurvey()
                } else {
                    closeSession()
                    showPage(2)
                    startButton.text = "Erneut versuchen"
                    statusView.text =
                        "Die SoundTouch konnte nicht für die WLAN-Einrichtung " +
                            "vorbereitet werden. Bitte erneut verbinden."
                }
            }
        }, "BoseSetupReadOnlyProbe")
        activeWorker = worker
        worker.start()
    }

    private fun runBoseSurvey() {
        val network = b2Network ?: return
        if (!b2Mode || !b2Verified || !surveySupported || surveyInFlight ||
            postInFlight || postAttempted || activeCallback == null
        ) return

        surveyInFlight = true
        surveyCandidates = emptyList()
        scanButton.isEnabled = false
        chooseWifiButton.isEnabled = false
        submitButton.isEnabled = false
        surveyStatusView.text = "Der Bose-Lautsprecher sucht verfügbare WLANs …"
        val token = generation
        trace("b2_site_survey_start token=$token")
        val worker = Thread({
            val result = BoseWirelessSiteSurvey.scan(network)
            runOnUiThread {
                if (token != generation) return@runOnUiThread
                surveyInFlight = false
                scanButton.isEnabled = true
                submitButton.isEnabled = true
                surveyCandidates = result.candidates
                surveyStatusView.text = if (result.candidates.isNotEmpty()) {
                    result.candidates.size.toString() + " WLANs gefunden. Wähle dein Netzwerk."
                } else {
                    "Keine WLAN-Liste verfügbar. Du kannst die Suche wiederholen " +
                        "oder den WLAN-Namen selbst eingeben."
                }
                resultsView.append("\n\nWLAN-Suche: " + result.status)
                trace("b2_site_survey_end token=$token count=${result.candidates.size}")
                renderSurveyNetworks()
                if (result.candidates.any { it.selectable } && visible && currentPage == 3) {
                    showWifiPicker()
                }
            }
        }, "BoseWirelessSiteSurvey")
        activeWorker = worker
        worker.start()
    }

    private fun confirmB2Post() {
        if (!b2Mode || !b2Verified || b2Network == null ||
            activeCallback == null || postAttempted || postInFlight || surveyInFlight
        ) return
        val ssid = ssidField.text.toString()
        val payload = try {
            BoseWirelessProfileXml.buildCandidate(
                ssid, passwordField.text.toString()
            ).toByteArray(Charsets.UTF_8)
        } catch (_: IllegalArgumentException) {
            statusView.text =
                "Bitte überprüfe WLAN-Namen und Passwort. Das Passwort muss für " +
                    "diesen WPA2-Test 8–63 Zeichen enthalten."
            return
        }

        // This is the single explicit user action. No second confirmation dialog.
        val network = b2Network ?: return
        pendingWifiSsid = ssid
        lastBoseSetupNetwork = network
        ssidField.text?.clear()
        passwordField.text?.clear()
        submitButton.isEnabled = false
        postAttempted = true
        postInFlight = true
        val token = generation
        resultSpinner.visibility = View.VISIBLE
        retryVerificationButton.visibility = View.GONE
        resultText.text = "WLAN-Einstellungen werden übertragen …"
        statusView.text = "Bitte warten. Die SoundTouch stellt ihre WLAN-Verbindung um."
        showPage(4)
        trace("b2_post_started token=$token")
        val worker = Thread({
            val result = try {
                BoseWirelessProfilePost.submitOnce(network, payload)
            } finally {
                payload.fill(0)
            }
            runOnUiThread {
                if (generation != token || isFinishing || isDestroyed) return@runOnUiThread
                trace("b2_post_completed token=$token")
                profileResponseAcknowledged = result.contains("AddWirelessProfileResponse erkannt")
                val disconnected = b2NetworkLostDuringPost
                closeSession()
                resultsView.append("\n\n" + result)
                if (disconnected) {
                    resultsView.append("\nBose-Setup-WLAN während der Übertragung getrennt.")
                }
                resultText.text =
                    if (profileResponseAcknowledged) {
                        "Die SoundTouch hat die WLAN-Einstellungen bestätigt. " +
                            "Wir prüfen jetzt, ob sie mit dem WLAN verbunden ist."
                    } else {
                        "Die Übertragung konnte nicht eindeutig bestätigt werden. " +
                            "Wir suchen trotzdem nach der SoundTouch im WLAN."
                    }
                statusView.text = "Warte auf dein WLAN …"
                beginLanVerification()
            }
        }, "BoseSetupSingleProfilePost")
        activeWorker = worker
        worker.start()
    }

    private fun beginLanVerification() {
        lanVerification?.stop()
        lanVerification = null
        val id = boseDeviceId
        val ssid = pendingWifiSsid
        if (id.isNullOrEmpty() || ssid.isNullOrEmpty()) {
            resultSpinner.visibility = View.GONE
            retryVerificationButton.visibility = View.GONE
            resultText.text =
                "Eine automatische Prüfung ist bei diesem Lautsprecher noch nicht möglich. " +
                    "Die Übertragung und die Verbindung zum WLAN konnten nicht " +
                    "unabhängig bestätigt werden."
            statusView.text = "Einrichtung noch nicht automatisch bestätigt."
            return
        }
        resultSpinner.visibility = View.VISIBLE
        retryVerificationButton.visibility = View.GONE
        resultText.text =
            "Wir warten auf die WLAN-Verbindung deines Smartphones und " +
                "suchen dort deine SoundTouch …"
        val verifier = BoseLanVerification(this, id, ssid, lastBoseSetupNetwork) { outcome ->
            if (isFinishing || isDestroyed || currentPage != 4) return@BoseLanVerification
            when (outcome) {
                BoseLanVerification.Status.WaitingForWifi -> {
                    resultSpinner.visibility = View.VISIBLE
                    resultText.text = "Android verbindet sich wieder mit deinem WLAN …"
                    statusView.text = "Warte auf WLAN …"
                }
                BoseLanVerification.Status.Searching -> {
                    resultSpinner.visibility = View.VISIBLE
                    resultText.text = "Mit WLAN verbunden. Die SoundTouch wird gesucht …"
                    statusView.text = "Suche im WLAN des Smartphones …"
                }
                is BoseLanVerification.Status.Found -> {
                    resultSpinner.visibility = View.GONE
                    retryVerificationButton.visibility = View.GONE
                    resultText.text =
                        if (outcome.wlanName != null) {
                            "Fertig! Die SoundTouch wurde in deinem WLAN gefunden. " +
                                "Sie meldet als aktives WLAN: " + outcome.wlanName + "."
                        } else {
                            "Fertig! Deine SoundTouch wurde im WLAN des Smartphones " +
                                "gefunden und anhand ihrer Gerätekennung erkannt."
                        }
                    statusView.text = "SoundTouch erfolgreich im WLAN gefunden."
                }
                is BoseLanVerification.Status.OtherWifi -> {
                    resultSpinner.visibility = View.GONE
                    retryVerificationButton.visibility = View.VISIBLE
                    resultText.text =
                        "Deine SoundTouch wurde gefunden, ist aber laut Bose derzeit " +
                            "mit dem WLAN „" + outcome.wlanName + "“ verbunden. " +
                            "Bitte prüfe, ob das gewünschte WLAN ausgewählt war."
                    statusView.text = "SoundTouch gefunden, WLAN stimmt nicht überein."
                }
                BoseLanVerification.Status.NotFound -> {
                    resultSpinner.visibility = View.GONE
                    retryVerificationButton.visibility = View.VISIBLE
                    resultText.text =
                        "Die SoundTouch wurde noch nicht im WLAN des Smartphones gefunden. " +
                            "Sie könnte noch verbinden oder in einem anderen WLAN sein. " +
                            "Du kannst erneut suchen, ohne das Passwort erneut zu senden."
                    statusView.text = "WLAN-Verbindung der SoundTouch nicht bestätigt."
                }
            }
        }
        lanVerification = verifier
        verifier.start()
    }

    private fun scheduleBackgroundCleanup() {
        if (visible || postInFlight) return
        cancelBackgroundCleanup()
        val action = Runnable {
            if (!visible && !awaitingConsent && !postInFlight && activeCallback != null) {
                closeSession()
                showPage(2)
                startButton.text = "Erneut versuchen"
                statusView.text =
                    "Die Verbindung zur SoundTouch wurde im Hintergrund getrennt. " +
                        "Bitte stelle die Verbindung erneut her."
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
        b2Network = null
        b2Verified = false
        surveySupported = false
        surveyInFlight = false
        surveyCandidates = emptyList()
        if (::scanButton.isInitialized) scanButton.isEnabled = false
        if (::chooseWifiButton.isInitialized) chooseWifiButton.isEnabled = false
        postInFlight = false
        b2NetworkLostDuringPost = false
        if (::ssidField.isInitialized) ssidField.text?.clear()
        if (::passwordField.isInitialized) passwordField.text?.clear()
        if (::b2Panel.isInitialized) b2Panel.visibility = View.GONE
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
        if (!awaitingConsent && !postInFlight && activeCallback != null) {
            scheduleBackgroundCleanup()
        }
        super.onStop()
    }

    override fun onDestroy() {
        trace("activity_destroyed")
        lanVerification?.stop()
        lanVerification = null
        pendingWifiSsid = null
        lastBoseSetupNetwork = null
        boseDeviceId = null
        closeSession()
        super.onDestroy()
    }
}
