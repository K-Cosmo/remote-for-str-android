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
    private lateinit var networkChoices: LinearLayout
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
        b2Panel.addView(heading("Verfügbare WLANs"))
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
        networkChoices = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        b2Panel.addView(networkChoices)
        b2Panel.addView(text(
            "Dein WLAN fehlt? Gib seinen Namen einfach selbst ein.",
            14f
        ).apply { setPadding(0, dp(14), 0, dp(8)) })
        b2Panel.addView(heading("WLAN-Name (SSID)"))
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
        resultPage.addView(heading("Verbindung überprüfen"))
        resultText = text(
            "Die SoundTouch versucht, sich mit dem ausgewählten WLAN zu verbinden.",
            16f, ink
        )
        resultPage.addView(resultText)
        val resultCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = card()
            setPadding(dp(16), dp(16), dp(16), dp(8))
        }
        resultCard.addView(heading("So geht es weiter"))
        resultCard.addView(text(
            "1. Verbinde dein Smartphone wieder mit dem Ziel-WLAN.\n" +
                "2. Prüfe im Router, ob die SoundTouch dort erscheint.\n" +
                "3. Erst wenn sie erreichbar ist, ist die Einrichtung bestätigt."
        ))
        resultPage.addView(resultCard)
        resultPage.addView(text(
            "Bei unklarem Ergebnis bitte zuerst den Router prüfen und nicht blind erneut senden.",
            14f
        ).apply { setPadding(0, dp(14), 0, 0) })
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

    private fun renderSurveyNetworks() {
        networkChoices.removeAllViews()
        val ink = Color.rgb(23, 46, 67)
        val muted = Color.rgb(66, 84, 101)
        val accent = Color.rgb(18, 69, 113)
        for (candidate in surveyCandidates) {
            val selected = candidate.ssid == ssidField.text.toString()
            val signal = when {
                candidate.signalDbm == null -> "Signal unbekannt"
                candidate.signalDbm >= -55 -> "Stark"
                candidate.signalDbm >= -70 -> "Mittel"
                else -> "Schwach"
            }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(10))
                background = GradientDrawable().apply {
                    setColor(if (selected) Color.rgb(232, 242, 251) else Color.WHITE)
                    cornerRadius = dp(10).toFloat()
                    setStroke(dp(if (selected) 2 else 1),
                        if (selected) accent else Color.rgb(218, 226, 234))
                }
                alpha = if (candidate.selectable) 1f else 0.52f
                isClickable = candidate.selectable
                isFocusable = candidate.selectable
                contentDescription = candidate.ssid + ", " + signal +
                    if (candidate.selectable) ", auswählen" else ", nicht unterstützt"
                if (candidate.selectable) setOnClickListener {
                    ssidField.setText(candidate.ssid)
                    ssidField.setSelection(ssidField.text.length)
                    surveyStatusView.text = "WLAN ausgewählt. Bitte das Passwort eingeben."
                    renderSurveyNetworks()
                }
            }
            row.addView(TextView(this).apply {
                text = (if (selected) "✓  " else "") + candidate.ssid
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(ink)
                setSingleLine(true)
                ellipsize = android.text.TextUtils.TruncateAt.END
            })
            row.addView(TextView(this).apply {
                text = if (candidate.selectable) signal + " · WPA2"
                       else signal + " · Für diesen Test nicht unterstützt"
                textSize = 13f
                setTextColor(muted)
                setPadding(0, dp(4), 0, 0)
            })
            networkChoices.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) })
        }
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
            observations.add(b1Report.summary)
            trace("b1_supported_urls_finished token=$token")
            runOnUiThread {
                if (token != generation) return@runOnUiThread
                trace("http_probe_complete token=$token")
                resultsView.text = observations.joinToString("\n")
                if (b2Mode && b1Report.supportsAddWirelessProfile) {
                    b2Network = network
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
        networkChoices.removeAllViews()
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
            }
        }, "BoseWirelessSiteSurvey")
        activeWorker = worker
        worker.start()
    }

    private fun confirmB2Post() {
        if (!b2Mode || !b2Verified || b2Network == null ||
            activeCallback == null || postAttempted || postInFlight || surveyInFlight
        ) return
        // Never expose the password, SSID, XML or network identifier in logs.
        val valid = try {
            BoseWirelessProfileXml.buildCandidate(
                ssidField.text.toString(), passwordField.text.toString()
            )
            true
        } catch (_: IllegalArgumentException) {
            false
        }
        if (!valid) {
            statusView.text =
                "Bitte WLAN-Name und Passwort überprüfen. Für diesen Test werden " +
                    "einfache WPA2-Netzwerknamen und Passwörter ab 8 Zeichen unterstützt."
            return
        }

        AlertDialog.Builder(this)
            .setTitle("SoundTouch mit diesem WLAN verbinden?")
            .setMessage(
                "Die SoundTouch erhält jetzt WLAN-Name und Passwort. " +
                    "Ihr Bose-Setup-WLAN wird möglicherweise sofort verschwinden. " +
                    "Da die Übertragung unverschlüsselt ist, bitte nur ein separates " +
                    "Test-WLAN mit eigenem Passwort verwenden. " +
                    "Bei einem Fehler zuerst den Router prüfen und nicht blind erneut senden."
            )
            .setNegativeButton("Nicht senden", null)
            .setPositiveButton("WLAN übertragen") { _, _ ->
                val network = b2Network
                if (network != null && b2Verified && !postAttempted && !postInFlight) {
                    val payload = try {
                        BoseWirelessProfileXml.buildCandidate(
                            ssidField.text.toString(), passwordField.text.toString()
                        ).toByteArray(Charsets.UTF_8)
                    } catch (_: IllegalArgumentException) {
                        statusView.text = "Testdaten ungültig; nichts gesendet."
                        return@setPositiveButton
                    }
                    // Remove text as soon as the request is scheduled.
                    ssidField.text?.clear()
                    passwordField.text?.clear()
                    submitButton.isEnabled = false
                    postAttempted = true
                    postInFlight = true
                    val token = generation
                    statusView.text =
                        "WLAN-Einstellungen werden an die SoundTouch übertragen …"
                    trace("b2_post_started token=$token")
                    val worker = Thread({
                        val result = try {
                            BoseWirelessProfilePost.submitOnce(network, payload)
                        } finally {
                            payload.fill(0)
                        }
                        runOnUiThread {
                            if (generation != token) return@runOnUiThread
                            trace("b2_post_completed token=$token")
                            val disconnected = b2NetworkLostDuringPost
                            closeSession()
                            resultsView.append("\n\n$result")
                            if (disconnected) {
                                resultsView.append("\nSetup-WLAN wurde während des POST getrennt.")
                            }
                            val acknowledged = result.contains("AddWirelessProfileResponse erkannt")
                            resultText.text = if (acknowledged) {
                                "Die SoundTouch hat die WLAN-Daten angenommen. " +
                                    "Ob sie bereits mit dem neuen WLAN verbunden ist, " +
                                    "musst du noch überprüfen."
                            } else {
                                "Die Übertragung konnte nicht sicher bestätigt werden. " +
                                    "Möglicherweise ist die SoundTouch bereits ins neue " +
                                    "WLAN gewechselt. Bitte zuerst den Router prüfen."
                            }
                            statusView.text =
                                "Bose-Setup-Verbindung beendet. Prüfe die SoundTouch im Ziel-WLAN."
                            startButton.isEnabled = false
                            showPage(4)
                        }
                    }, "BoseSetupSingleProfilePost")
                    activeWorker = worker
                    worker.start()
                }
            }
            .show()
    }

    private fun scheduleBackgroundCleanup() {
        if (visible || postInFlight) return
        cancelBackgroundCleanup()
        val action = Runnable {
            if (!visible && !awaitingConsent && !postInFlight && activeCallback != null) {
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
        b2Network = null
        b2Verified = false
        surveySupported = false
        surveyInFlight = false
        surveyCandidates = emptyList()
        if (::scanButton.isInitialized) scanButton.isEnabled = false
        if (::networkChoices.isInitialized) networkChoices.removeAllViews()
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
        closeSession()
        super.onDestroy()
    }
}
