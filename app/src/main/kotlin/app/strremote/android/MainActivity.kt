package app.strremote.android

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import java.net.URI
import java.util.LinkedHashMap

class MainActivity : Activity() {
    private companion object {
        const val PERMISSION_REQUEST = 401
        const val DISCOVERY_TIMEOUT_MS = 10_000L
        const val WIFI_CALLBACK_INITIALIZATION_MS = 500L
        const val REMOTE_RETRY_SETTLE_MS = 600L
        const val STR_WEBSITE = "https://st-reborn.de"
    }

    private lateinit var prefs: PreferencesStore
    private lateinit var discovery: StrDiscovery
    private lateinit var connectivityManager: ConnectivityManager
    private val probe = EndpointProbe()
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var statusText: TextView
    private lateinit var deviceButton: Button
    private lateinit var discoveryPanel: LinearLayout
    private lateinit var discoveryProgress: ProgressBar
    private lateinit var discoveryMessage: TextView
    private lateinit var managementHint: TextView
    private lateinit var discoveryWifiSettingsButton: Button
    private lateinit var retryButton: Button
    private lateinit var strHelpButton: Button
    private lateinit var remoteOfflinePanel: LinearLayout
    private lateinit var remoteOfflineMessage: TextView
    private lateinit var remoteWifiSettingsButton: Button
    private lateinit var remoteRetryButton: Button
    private lateinit var webView: WebView
    private lateinit var adapter: SpeakerAdapter

    private val speakers = LinkedHashMap<String, SpeakerRow>()
    private val hiddenSpeakerKeys = mutableSetOf<String>()
    private var currentEndpoint: SpeakerEndpoint? = null
    private var pageVisible = false
    private var initialConnectionAttempted = false
    private val wifiNetworks = LinkedHashSet<Network>()
    private var wifiCallbackRegistered = false
    private var wifiStateInitialized = false
    private var discoveryActive = false
    private var discoveryTimedOut = false
    private var discoveryGeneration = 0
    private var remoteProbeGeneration = 0
    private var webViewNeedsReload = false

    private val discoveryTimeout = Runnable { handleDiscoveryTimeout() }

    private val wifiInitializationTimeout = Runnable {
        settleInitialWifiState()
    }

    private val wifiCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread {
                val wasAvailable = hasWifiTransport()
                wifiNetworks.add(network)
                settleInitialWifiState()

                if (!wasAvailable && !pageVisible && initialConnectionAttempted) {
                    setDiscoveryState(
                        message = getString(R.string.wifi_available_retry),
                        showProgress = false,
                        showRetry = true,
                        showStrHelp = false
                    )
                }
            }
        }

        override fun onLost(network: Network) {
            runOnUiThread {
                wifiNetworks.remove(network)
                if (wifiStateInitialized && wifiNetworks.isEmpty()) {
                    markSavedSpeakersUnreachable()
                    remoteProbeGeneration++
                    if (pageVisible && currentEndpoint != null) {
                        showRemoteWifiRequired()
                    } else if (!pageVisible) {
                        showWifiRequired()
                    }
                }
            }
        }
    }

    private val discoveryCallback = object : StrDiscovery.Callback {
        override fun onDiscoveryStarted() = handleDiscoveryStarted()

        override fun onSpeakerFound(candidate: SpeakerCandidate) = handleSpeakerFound(candidate)

        override fun onSpeakerLost(serviceName: String) = handleSpeakerLost(serviceName)

        override fun onDiscoveryError(errorCode: Int) = handleDiscoveryError(errorCode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesStore(this)
        discovery = StrDiscovery(this, discoveryCallback)
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        buildUi()
        registerBackCallback()
        configureWebView()
        registerWifiMonitor()
    }

    override fun onResume() {
        super.onResume()
        if (wifiStateInitialized && !initialConnectionAttempted && hasLocalNetworkPermission()) {
            startInitialConnection()
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(wifiInitializationTimeout)
        if (wifiCallbackRegistered) {
            connectivityManager.unregisterNetworkCallback(wifiCallback)
            wifiCallbackRegistered = false
        }
        stopDiscoverySession()
        discoveryGeneration++
        probe.shutdown()
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }

    @SuppressLint("GestureBackNavigation")
    @Deprecated("Used only on Android 12L and earlier; Android 13+ uses OnBackInvokedDispatcher.")
    override fun onBackPressed() {
        if (!handleBackNavigation()) finish()
    }

    private fun registerBackCallback() {
        if (Build.VERSION.SDK_INT < 33) return
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT
        ) {
            if (!handleBackNavigation()) finish()
        }
    }

    private fun handleBackNavigation(): Boolean {
        if (pageVisible && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        if (!pageVisible && currentEndpoint != null) {
            showWebView()
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQUEST) return
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            initialConnectionAttempted = false
            if (wifiStateInitialized) startInitialConnection()
        } else {
            showPermissionRequired()
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        applySystemBarInsets(root)

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(6), dp(16), dp(6))
        }

        val titleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.START
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 19f
            includeFontPadding = false
            setTypeface(typeface, Typeface.BOLD)
        }
        titleColumn.addView(
            title,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        statusText = TextView(this).apply {
            textSize = 14f
            includeFontPadding = false
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(2), 0, 0)
            visibility = View.GONE
        }
        titleColumn.addView(
            statusText,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        toolbar.addView(
            titleColumn,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )

        deviceButton = Button(this).apply {
            text = getString(R.string.devices)
            isAllCaps = false
            textSize = 14f
            minWidth = 0
            minimumHeight = dp(36)
            setPadding(dp(12), dp(4), dp(12), dp(4))
            setOnClickListener {
                if (pageVisible) {
                    showDiscoveryPanel()
                    startDiscovery()
                } else if (currentEndpoint != null) {
                    showWebView()
                } else {
                    startDiscovery()
                }
            }
        }
        toolbar.addView(
            deviceButton,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(10)
            }
        )

        root.addView(
            toolbar,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        val content = FrameLayout(this)
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        discoveryPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(10))
        }
        content.addView(
            discoveryPanel,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        val discoveryHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = createPanelBackground(
                fillColor = "#EDF5FF",
                strokeColor = "#BFD9FF"
            )
        }

        discoveryProgress = ProgressBar(this, null, android.R.attr.progressBarStyleSmall).apply {
            isIndeterminate = true
        }
        discoveryHeader.addView(
            discoveryProgress,
            LinearLayout.LayoutParams(dp(24), dp(24)).apply {
                marginEnd = dp(10)
            }
        )

        discoveryMessage = TextView(this).apply {
            text = getString(R.string.searching)
            gravity = Gravity.CENTER_VERTICAL
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#163A63"))
        }
        discoveryHeader.addView(
            discoveryMessage,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        discoveryPanel.addView(
            discoveryHeader,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        managementHint = TextView(this).apply {
            textSize = 14f
            setLineSpacing(0f, 1.1f)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = createPanelBackground(
                fillColor = "#FFF7E6",
                strokeColor = "#FFD58A"
            )
            setTextColor(Color.parseColor("#5D4300"))
        }
        configureManagementHint()
        discoveryPanel.addView(
            managementHint,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(10)
            }
        )

        val speakerList = ListView(this).apply {
            dividerHeight = 0
        }
        adapter = SpeakerAdapter()
        speakerList.adapter = adapter
        speakerList.setOnItemClickListener { _, _, position, _ ->
            val row = adapter.getItem(position) ?: return@setOnItemClickListener
            when {
                row.endpoint != null -> loadEndpoint(row.endpoint)
                row.saved != null -> probeSavedSpeaker(row.saved, discoveryGeneration, connectOnSuccess = true)
                else -> probeCandidate(row.candidate, discoveryGeneration)
            }
        }
        discoveryPanel.addView(
            speakerList,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                topMargin = dp(10)
            }
        )

        discoveryWifiSettingsButton = Button(this).apply {
            text = getString(R.string.open_wifi_settings)
            isAllCaps = false
            visibility = View.GONE
            setOnClickListener { openWifiSettings() }
        }
        discoveryPanel.addView(
            discoveryWifiSettingsButton,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8)
            }
        )

        retryButton = Button(this).apply {
            text = getString(R.string.retry)
            isAllCaps = false
            visibility = View.GONE
            setOnClickListener { retryConnection() }
        }
        discoveryPanel.addView(
            retryButton,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8)
            }
        )

        strHelpButton = Button(this).apply {
            text = getString(R.string.str_help)
            isAllCaps = false
            visibility = View.GONE
            setOnClickListener { openStrWebsite() }
        }
        discoveryPanel.addView(
            strHelpButton,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8)
            }
        )

        val manualButton = Button(this).apply {
            text = getString(R.string.manual_ip)
            isAllCaps = false
            setOnClickListener { showManualDialog() }
        }
        discoveryPanel.addView(
            manualButton,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8)
            }
        )

        remoteOfflinePanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(12))
            visibility = View.GONE
        }

        remoteOfflineMessage = TextView(this).apply {
            text = getString(R.string.wifi_required)
            textSize = 16f
            setLineSpacing(0f, 1.12f)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setTextColor(Color.parseColor("#163A63"))
            background = createPanelBackground(
                fillColor = "#EDF5FF",
                strokeColor = "#BFD9FF"
            )
        }
        remoteOfflinePanel.addView(
            remoteOfflineMessage,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        remoteWifiSettingsButton = Button(this).apply {
            text = getString(R.string.open_wifi_settings)
            isAllCaps = false
            setOnClickListener { openWifiSettings() }
        }
        remoteOfflinePanel.addView(
            remoteWifiSettingsButton,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(12)
            }
        )

        remoteRetryButton = Button(this).apply {
            text = getString(R.string.retry)
            isAllCaps = false
            setOnClickListener { retryRemoteConnection() }
        }
        remoteOfflinePanel.addView(
            remoteRetryButton,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8)
            }
        )

        content.addView(
            remoteOfflinePanel,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        webView = WebView(this).apply {
            visibility = View.GONE
        }
        content.addView(
            webView,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        setContentView(root)
        root.requestApplyInsets()
    }

    private fun applySystemBarInsets(root: View) {
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= 30) {
                val systemBars = insets.getInsets(WindowInsets.Type.systemBars())
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mediaPlaybackRequiresUserGesture = true
            safeBrowsingEnabled = true
            userAgentString = "$userAgentString STR-Remote-Android"
        }
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.isForMainFrame) return false
                val uri = request.url
                val scheme = uri.scheme?.lowercase()

                if (isAllowedSpeakerNavigation(uri)) {
                    val endpoint = currentEndpoint ?: return true
                    currentEndpoint = endpoint.copy(port = uri.port)
                    return false
                }

                if (scheme == "http" || scheme == "https") {
                    return openExternalUri(uri)
                }
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (webViewNeedsReload) return

                currentEndpoint?.let { endpoint ->
                    prefs.saveReachability(endpoint, true)
                    updateSavedRowReachability(endpoint, reachable = true)
                }
                showWebView()
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                if (request.isForMainFrame) {
                    webViewNeedsReload = true
                    currentEndpoint?.let { endpoint ->
                        prefs.saveReachability(endpoint, false)
                        updateSavedRowReachability(endpoint, reachable = false)
                    }

                    if (!hasWifiTransport()) {
                        showRemoteWifiRequired()
                    } else if (currentEndpoint != null) {
                        showRemoteDeviceUnreachable()
                    } else {
                        Toast.makeText(this@MainActivity, R.string.page_failed, Toast.LENGTH_LONG).show()
                        showDiscoveryPanel()
                    }
                }
            }
        }
    }

    private fun isAllowedSpeakerNavigation(uri: Uri): Boolean {
        if (uri.scheme?.lowercase() != "http") return false
        if (uri.port != 8888 && uri.port != 17008) return false

        val endpoint = currentEndpoint ?: return false
        val navigationHost = uri.host ?: return false
        return navigationHost.equals(endpoint.host, ignoreCase = true)
    }

    private fun ensurePermissionAndStart() {
        val permission = requiredLocalNetworkPermission()
        if (permission == null || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            startInitialConnection()
        } else {
            requestPermissions(arrayOf(permission), PERMISSION_REQUEST)
        }
    }

    private fun requiredLocalNetworkPermission(): String? = when {
        Build.VERSION.SDK_INT >= 37 -> Manifest.permission.ACCESS_LOCAL_NETWORK
        Build.VERSION.SDK_INT == 36 -> Manifest.permission.NEARBY_WIFI_DEVICES
        else -> null
    }

    private fun showPermissionRequired() {
        stopDiscoverySession()
        showDiscoveryPanel()
        setDiscoveryState(
            message = getString(R.string.permission_body),
            showProgress = false,
            showRetry = false,
            showStrHelp = false
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.permission_title)
            .setMessage(R.string.permission_body)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                val permission = requiredLocalNetworkPermission()
                if (permission != null && shouldShowRequestPermissionRationale(permission)) {
                    requestPermissions(arrayOf(permission), PERMISSION_REQUEST)
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun startInitialConnection() {
        if (initialConnectionAttempted) return
        initialConnectionAttempted = true

        if (!hasWifiTransport()) {
            showWifiRequired()
            return
        }

        val saved = prefs.loadLastEndpoint()
        if (saved == null) {
            showDiscoveryPanel()
            startDiscovery()
            return
        }

        showDiscoveryPanel()
        setDiscoveryState(
            message = getString(R.string.connecting_last),
            showProgress = true,
            showRetry = false,
            showStrHelp = false
        )
        probe.probeHost(
            host = saved.host,
            preferredPort = saved.port,
            name = saved.name,
            model = saved.model,
            key = saved.key
        ) { endpoint ->
            runOnUiThread {
                if (endpoint != null) {
                    loadEndpoint(endpoint)
                } else if (hasWifiTransport()) {
                    showDiscoveryPanel()
                    startDiscovery()
                } else {
                    showWifiRequired()
                }
            }
        }
    }

    private fun retryConnection() {
        initialConnectionAttempted = false
        ensurePermissionAndStart()
    }

    private fun startDiscovery() {
        if (!hasLocalNetworkPermission()) {
            showPermissionRequired()
            return
        }
        if (!hasWifiTransport()) {
            showWifiRequired()
            return
        }

        stopDiscoverySession()
        discoveryGeneration++
        discoveryActive = true
        discoveryTimedOut = false
        hiddenSpeakerKeys.clear()

        rebuildPersistentRows()
        setDiscoveryState(
            message = getString(R.string.searching),
            showProgress = true,
            showRetry = false,
            showStrHelp = false
        )

        val generation = discoveryGeneration
        speakers.values
            .mapNotNull { it.saved }
            .distinctBy { speakerIdentity(it.endpoint) }
            .forEach { probeSavedSpeaker(it, generation, connectOnSuccess = false) }

        discovery.start()
        mainHandler.postDelayed(discoveryTimeout, DISCOVERY_TIMEOUT_MS)
    }

    private fun stopDiscoverySession() {
        mainHandler.removeCallbacks(discoveryTimeout)
        discoveryActive = false
        discovery.stop()
    }

    private fun hasLocalNetworkPermission(): Boolean {
        val permission = requiredLocalNetworkPermission() ?: return true
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun registerWifiMonitor() {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, wifiCallback)
        wifiCallbackRegistered = true
        mainHandler.postDelayed(wifiInitializationTimeout, WIFI_CALLBACK_INITIALIZATION_MS)
    }

    private fun settleInitialWifiState() {
        if (wifiStateInitialized) return
        wifiStateInitialized = true
        mainHandler.removeCallbacks(wifiInitializationTimeout)
        ensurePermissionAndStart()
    }

    private fun hasWifiTransport(): Boolean = wifiNetworks.isNotEmpty()

    private fun handleDiscoveryStarted() {
        runOnUiThread {
            if (!discoveryActive) return@runOnUiThread
            setDiscoveryState(
                message = getString(R.string.searching),
                showProgress = true,
                showRetry = false,
                showStrHelp = false
            )
        }
    }

    private fun handleSpeakerFound(candidate: SpeakerCandidate) {
        runOnUiThread {
            if (!discoveryActive) return@runOnUiThread
            if (speakerVisibilityKeys(candidate).any(hiddenSpeakerKeys::contains)) return@runOnUiThread

            val existingKey = findRowKey(candidate)
            val existing = existingKey?.let(speakers::get)
            val saved = existing?.saved ?: prefs.findSavedSpeaker(candidate.key, candidate.host)

            if (existingKey != null && existingKey != candidate.key) {
                speakers.remove(existingKey)
            }

            speakers[candidate.key] = SpeakerRow(
                candidate = candidate,
                endpoint = existing?.endpoint,
                probeFinished = existing?.probeFinished ?: false,
                saved = saved,
                discovered = true,
                lastReachable = existing?.lastReachable
                    ?: saved?.let { prefs.loadReachability(it.endpoint) }
            )
            updateList()
            if (existing == null || existing.probeFinished) {
                probeCandidate(candidate, discoveryGeneration)
            }
        }
    }

    private fun handleSpeakerLost(serviceName: String) {
        // Keep the row visible for this scan. STR speakers can briefly disappear from
        // mDNS while rebooting or entering/leaving standby.
    }

    private fun handleDiscoveryError(errorCode: Int) {
        runOnUiThread {
            mainHandler.removeCallbacks(discoveryTimeout)
            discoveryActive = false
            setDiscoveryState(
                message = getString(R.string.discovery_failed, errorCode),
                showProgress = false,
                showRetry = true,
                showStrHelp = false
            )
        }
    }

    private fun handleDiscoveryTimeout() {
        if (!discoveryActive) return

        discoveryActive = false
        discoveryTimedOut = true
        discovery.stop()

        if (!hasWifiTransport()) {
            showWifiRequired()
            return
        }

        val discoveredRows = speakers.values.filter { it.discovered }
        when {
            speakers.values.any { it.endpoint != null } -> {
                setDiscoveryState(
                    message = getString(R.string.found_devices),
                    showProgress = false,
                    showRetry = false,
                    showStrHelp = false
                )
            }
            discoveredRows.isEmpty() -> showNoDevicesFound()
            discoveredRows.all { it.probeFinished } -> showStrUnreachable()
            else -> {
                setDiscoveryState(
                    message = getString(R.string.checking_discovered),
                    showProgress = true,
                    showRetry = false,
                    showStrHelp = false
                )
            }
        }
    }

    private fun probeCandidate(candidate: SpeakerCandidate, generation: Int) {
        if (!hasWifiTransport()) {
            showWifiRequired()
            return
        }

        val existingKey = findRowKey(candidate)
        val rowKey = existingKey ?: candidate.key
        val existing = existingKey?.let(speakers::get)
        val saved = existing?.saved ?: prefs.findSavedSpeaker(candidate.key, candidate.host)
        speakers[rowKey] = (existing ?: SpeakerRow(candidate, null, false, saved, true)).copy(
            candidate = candidate,
            probeFinished = false,
            saved = saved,
            discovered = true,
            lastReachable = existing?.lastReachable
                ?: saved?.let { prefs.loadReachability(it.endpoint) }
        )
        if (saved == null || speakers[rowKey]?.lastReachable == null) {
            updateList()
        }

        probe.probe(candidate) { endpoint ->
            runOnUiThread {
                if (generation != discoveryGeneration) return@runOnUiThread

                val currentKey = findRowKey(candidate) ?: return@runOnUiThread
                val current = speakers[currentKey] ?: return@runOnUiThread
                val reachable = endpoint != null

                current.saved?.let { saved ->
                    prefs.saveReachability(saved.endpoint, reachable)
                }

                speakers[currentKey] = current.copy(
                    endpoint = endpoint,
                    probeFinished = true,
                    lastReachable = if (current.saved != null) reachable else current.lastReachable
                )
                if (endpoint != null && current.saved != null) {
                    prefs.updateSavedEndpoint(endpoint)
                }
                updateList()

                if (endpoint != null) {
                    val last = prefs.loadLastEndpoint()
                    if (currentEndpoint == null && last != null && speakerMatches(last, endpoint)) {
                        loadEndpoint(endpoint)
                    }
                } else if (
                    discoveryTimedOut &&
                    speakers.values.any { it.discovered } &&
                    speakers.values.filter { it.discovered }.all { it.probeFinished } &&
                    speakers.values.filter { it.discovered }.none { it.endpoint != null }
                ) {
                    showStrUnreachable()
                }
            }
        }
    }

    private fun updateList() {
        adapter.replace(speakers.values.toList())
        if (speakers.isEmpty()) return

        val discoveredRows = speakers.values.filter { it.discovered }
        when {
            speakers.values.any { it.endpoint != null } -> {
                setDiscoveryState(
                    message = getString(R.string.found_devices),
                    showProgress = false,
                    showRetry = false,
                    showStrHelp = false
                )
            }
            discoveredRows.any { !it.probeFinished } -> {
                setDiscoveryState(
                    message = getString(R.string.checking_discovered),
                    showProgress = true,
                    showRetry = false,
                    showStrHelp = false
                )
            }
            discoveryActive -> {
                setDiscoveryState(
                    message = getString(R.string.searching),
                    showProgress = true,
                    showRetry = false,
                    showStrHelp = false
                )
            }
            discoveryTimedOut && discoveredRows.isEmpty() -> showNoDevicesFound()
            discoveryTimedOut -> showStrUnreachable()
            else -> {
                setDiscoveryState(
                    message = getString(R.string.found_devices),
                    showProgress = false,
                    showRetry = false,
                    showStrHelp = false
                )
            }
        }
    }

    private fun showWifiRequired() {
        stopDiscoverySession()
        discoveryGeneration++
        discoveryTimedOut = false
        markSavedSpeakersUnreachable()
        rebuildPersistentRows()
        showDiscoveryPanel()
        setDiscoveryState(
            message = getString(R.string.wifi_required),
            showProgress = false,
            showRetry = true,
            showStrHelp = false
        )
        discoveryWifiSettingsButton.visibility = View.VISIBLE
    }

    private fun showNoDevicesFound() {
        setDiscoveryState(
            message = getString(
                if (prefs.loadSavedSpeakers().isEmpty()) {
                    R.string.no_devices_found
                } else {
                    R.string.no_devices_found_saved
                }
            ),
            showProgress = false,
            showRetry = true,
            showStrHelp = true
        )
    }

    private fun showStrUnreachable() {
        setDiscoveryState(
            message = getString(R.string.str_unreachable),
            showProgress = false,
            showRetry = true,
            showStrHelp = true
        )
    }

    private fun setDiscoveryState(
        message: CharSequence,
        showProgress: Boolean,
        showRetry: Boolean,
        showStrHelp: Boolean
    ) {
        discoveryMessage.text = message
        discoveryProgress.visibility = if (showProgress) View.VISIBLE else View.GONE
        discoveryWifiSettingsButton.visibility = View.GONE
        retryButton.visibility = if (showRetry) View.VISIBLE else View.GONE
        strHelpButton.visibility = if (showStrHelp) View.VISIBLE else View.GONE
    }

    private fun openStrWebsite() {
        openExternalUri(Uri.parse(STR_WEBSITE))
    }

    private fun openExternalUri(uri: Uri): Boolean {
        return try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (_: Exception) {
            true
        }
    }

    private fun loadEndpoint(endpoint: SpeakerEndpoint) {
        stopDiscoverySession()
        discoveryGeneration++
        discoveryTimedOut = false

        currentEndpoint = endpoint
        prefs.save(endpoint)
        prefs.updateSavedEndpoint(endpoint)
        refreshConnectedStatus(endpoint)

        if (hasWifiTransport()) {
            prefs.saveReachability(endpoint, true)
            updateSavedRowReachability(endpoint, reachable = true)
            webViewNeedsReload = false
            showRemoteConnectionState(
                message = getString(R.string.remote_connecting),
                showWifiSettings = false,
                showRetry = false
            )
            webView.stopLoading()
            webView.loadUrl(endpoint.baseUrl)
        } else {
            showRemoteWifiRequired()
        }
    }

    private fun refreshConnectedStatus(endpoint: SpeakerEndpoint) {
        val room = roomLabel(prefs.findSavedSpeaker(endpoint.key, endpoint.host)?.room)
        statusText.text = if (room == null) {
            endpoint.name
        } else {
            getString(R.string.selected_speaker_room, endpoint.name, room)
        }
    }

    private fun showWebView() {
        if (!hasWifiTransport()) {
            showRemoteWifiRequired()
            return
        }

        if (webViewNeedsReload) {
            currentEndpoint?.let { endpoint ->
                probeRemoteEndpoint(endpoint, settleBeforeProbe = false)
            } ?: showDiscoveryPanel()
            return
        }

        pageVisible = true
        deviceButton.text = getString(R.string.devices)
        deviceButton.visibility = View.VISIBLE
        statusText.visibility = View.VISIBLE
        discoveryPanel.visibility = View.GONE
        remoteOfflinePanel.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }

    private fun showRemoteConnectionState(
        message: CharSequence,
        showWifiSettings: Boolean,
        showRetry: Boolean
    ) {
        pageVisible = true
        deviceButton.text = getString(R.string.devices)
        deviceButton.visibility = View.VISIBLE
        statusText.visibility = if (currentEndpoint != null) View.VISIBLE else View.GONE
        discoveryPanel.visibility = View.GONE
        webView.visibility = View.GONE

        remoteOfflineMessage.text = message
        remoteWifiSettingsButton.visibility = if (showWifiSettings) View.VISIBLE else View.GONE
        remoteRetryButton.visibility = if (showRetry) View.VISIBLE else View.GONE
        remoteOfflinePanel.visibility = View.VISIBLE
    }

    private fun showRemoteWifiRequired() {
        webViewNeedsReload = true
        webView.stopLoading()
        showRemoteConnectionState(
            message = getString(R.string.wifi_required),
            showWifiSettings = true,
            showRetry = true
        )
    }

    private fun showRemoteDeviceUnreachable() {
        webViewNeedsReload = true
        webView.stopLoading()
        showRemoteConnectionState(
            message = getString(R.string.remote_device_unreachable),
            showWifiSettings = false,
            showRetry = true
        )
    }

    private fun retryRemoteConnection() {
        if (!hasWifiTransport()) {
            showRemoteWifiRequired()
            return
        }

        val endpoint = currentEndpoint ?: run {
            showDiscoveryPanel()
            return
        }

        probeRemoteEndpoint(endpoint, settleBeforeProbe = true)
    }

    private fun probeRemoteEndpoint(
        endpoint: SpeakerEndpoint,
        settleBeforeProbe: Boolean
    ) {
        if (!hasWifiTransport()) {
            showRemoteWifiRequired()
            return
        }

        remoteProbeGeneration++
        val generation = remoteProbeGeneration

        showRemoteConnectionState(
            message = getString(R.string.remote_connecting),
            showWifiSettings = false,
            showRetry = false
        )

        val runProbe = {
            probe.probeHost(
                host = endpoint.host,
                preferredPort = endpoint.port,
                name = endpoint.name,
                model = endpoint.model,
                key = endpoint.key
            ) { verified ->
                runOnUiThread {
                    if (generation != remoteProbeGeneration) return@runOnUiThread

                    if (!hasWifiTransport()) {
                        showRemoteWifiRequired()
                        return@runOnUiThread
                    }

                    if (verified == null) {
                        prefs.saveReachability(endpoint, false)
                        updateSavedRowReachability(endpoint, reachable = false)
                        showRemoteDeviceUnreachable()
                    } else {
                        prefs.saveReachability(verified, true)
                        updateSavedRowReachability(verified, reachable = true)
                        loadEndpoint(verified)
                    }
                }
            }
        }

        if (settleBeforeProbe) {
            mainHandler.postDelayed(runProbe, REMOTE_RETRY_SETTLE_MS)
        } else {
            runProbe()
        }
    }

    private fun openWifiSettings() {
        val intent = if (Build.VERSION.SDK_INT >= 29) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
    }

    private fun showDiscoveryPanel() {
        pageVisible = false
        deviceButton.text = getString(R.string.remote)
        deviceButton.visibility = if (currentEndpoint != null) View.VISIBLE else View.GONE
        statusText.visibility = View.GONE
        remoteOfflinePanel.visibility = View.GONE
        webView.visibility = View.GONE
        discoveryPanel.visibility = View.VISIBLE
        adapter.notifyDataSetChanged()
    }

    private fun showManualDialog() {
        if (!hasLocalNetworkPermission()) {
            showPermissionRequired()
            return
        }
        if (!hasWifiTransport()) {
            showWifiRequired()
            return
        }

        val input = EditText(this).apply {
            hint = getString(R.string.manual_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.manual_title)
            .setView(input)
            .setPositiveButton(R.string.connect) { _, _ ->
                if (!hasWifiTransport()) {
                    showWifiRequired()
                    return@setPositiveButton
                }

                val parsed = parseManualAddress(input.text?.toString().orEmpty())
                if (parsed == null) {
                    Toast.makeText(this, R.string.manual_invalid, Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                val (host, port) = parsed
                setDiscoveryState(
                    message = getString(R.string.connecting_to, host),
                    showProgress = true,
                    showRetry = false,
                    showStrHelp = false
                )
                probe.probeHost(host, port, host) { endpoint ->
                    runOnUiThread {
                        if (endpoint != null) {
                            loadEndpoint(endpoint)
                        } else if (!hasWifiTransport()) {
                            showWifiRequired()
                        } else {
                            setDiscoveryState(
                                message = getString(R.string.manual_failed),
                                showProgress = false,
                                showRetry = true,
                                showStrHelp = true
                            )
                            Toast.makeText(this, R.string.manual_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun rebuildPersistentRows() {
        speakers.clear()

        prefs.loadSavedSpeakers().forEach { saved ->
            val candidate = candidateFromEndpoint(saved.endpoint)
            speakers[speakerIdentity(saved.endpoint)] = SpeakerRow(
                candidate = candidate,
                endpoint = null,
                probeFinished = false,
                saved = saved,
                discovered = false,
                lastReachable = prefs.loadReachability(saved.endpoint)
            )
        }

        currentEndpoint?.let { endpoint ->
            val saved = prefs.findSavedSpeaker(endpoint.key, endpoint.host)
            val lastReachable = saved?.let { prefs.loadReachability(it.endpoint) }
            val existingKey = findRowKey(endpoint)

            if (existingKey != null) {
                val existing = speakers[existingKey] ?: return@let
                speakers[existingKey] = existing.copy(
                    endpoint = null,
                    probeFinished = false,
                    lastReachable = lastReachable ?: existing.lastReachable
                )
            } else {
                speakers[speakerIdentity(endpoint)] = SpeakerRow(
                    candidate = candidateFromEndpoint(endpoint),
                    endpoint = null,
                    probeFinished = false,
                    saved = saved,
                    discovered = false,
                    lastReachable = lastReachable
                )
            }
        }

        adapter.replace(speakers.values.toList())
    }

    private fun candidateFromEndpoint(endpoint: SpeakerEndpoint): SpeakerCandidate = SpeakerCandidate(
        key = endpoint.key ?: speakerIdentity(endpoint),
        serviceName = endpoint.name,
        friendlyName = endpoint.name,
        host = endpoint.host,
        advertisedPort = endpoint.port,
        model = endpoint.model,
        version = null
    )

    private fun findRowKey(candidate: SpeakerCandidate): String? {
        val candidateEndpoint = SpeakerEndpoint(
            host = candidate.host,
            port = candidate.advertisedPort,
            name = candidate.friendlyName,
            model = candidate.model,
            key = candidate.key
        )
        return speakers.entries.firstOrNull { (_, row) ->
            val rowEndpoint = row.endpoint ?: row.saved?.endpoint
            if (rowEndpoint != null) {
                speakerMatches(rowEndpoint, candidateEndpoint)
            } else {
                row.candidate.key == candidate.key ||
                    row.candidate.host.equals(candidate.host, ignoreCase = true)
            }
        }?.key
    }

    private fun findRowKey(endpoint: SpeakerEndpoint): String? =
        speakers.entries.firstOrNull { (_, row) ->
            val rowEndpoint = row.endpoint ?: row.saved?.endpoint ?: SpeakerEndpoint(
                host = row.candidate.host,
                port = row.candidate.advertisedPort,
                name = row.candidate.friendlyName,
                model = row.candidate.model,
                key = row.candidate.key
            )
            speakerMatches(rowEndpoint, endpoint)
        }?.key

    private fun probeSavedSpeaker(
        saved: SavedSpeaker,
        generation: Int,
        connectOnSuccess: Boolean
    ) {
        if (!hasWifiTransport()) {
            if (connectOnSuccess) showWifiRequired()
            return
        }

        val existingKey = findRowKey(saved.endpoint) ?: speakerIdentity(saved.endpoint)
        val current = speakers[existingKey] ?: SpeakerRow(
            candidate = candidateFromEndpoint(saved.endpoint),
            endpoint = null,
            probeFinished = false,
            saved = saved,
            discovered = false
        )
        speakers[existingKey] = current.copy(
            probeFinished = false,
            saved = saved,
            lastReachable = current.lastReachable ?: prefs.loadReachability(saved.endpoint)
        )

        probe.probeHost(
            host = saved.endpoint.host,
            preferredPort = saved.endpoint.port,
            name = saved.endpoint.name,
            model = saved.endpoint.model,
            key = saved.endpoint.key
        ) { endpoint ->
            runOnUiThread {
                if (generation != discoveryGeneration) return@runOnUiThread

                val key = findRowKey(saved.endpoint) ?: return@runOnUiThread
                val row = speakers[key] ?: return@runOnUiThread
                val reachable = endpoint != null

                prefs.saveReachability(saved.endpoint, reachable)
                speakers[key] = row.copy(
                    endpoint = endpoint,
                    probeFinished = true,
                    lastReachable = reachable
                )
                if (endpoint != null) {
                    prefs.updateSavedEndpoint(endpoint)
                }
                updateList()

                if (connectOnSuccess) {
                    if (endpoint != null) {
                        loadEndpoint(endpoint)
                    } else {
                        setDiscoveryState(
                            message = getString(R.string.saved_unreachable),
                            showProgress = false,
                            showRetry = true,
                            showStrHelp = true
                        )
                    }
                }
            }
        }
    }

    private fun connectSpeakerRow(row: SpeakerRow) {
        when {
            row.saved != null ->
                probeSavedSpeaker(row.saved, discoveryGeneration, connectOnSuccess = true)
            row.endpoint != null ->
                probeRemoteEndpoint(row.endpoint, settleBeforeProbe = false)
            else ->
                probeCandidate(row.candidate, discoveryGeneration)
        }
    }

    private fun resolveRoomEndpoint(row: SpeakerRow): SpeakerEndpoint? =
        row.endpoint ?: row.saved?.endpoint ?: currentEndpoint?.takeIf {
            it.host.equals(row.candidate.host, ignoreCase = true)
        }

    private fun assignRoomForRow(row: SpeakerRow) {
        val endpoint = resolveRoomEndpoint(row)
        if (endpoint == null) {
            Toast.makeText(this, R.string.save_requires_reachable, Toast.LENGTH_LONG).show()
            return
        }
        showRoomPicker(endpoint)
    }

    private fun confirmRemoveSavedSpeaker(row: SpeakerRow) {
        val saved = row.saved ?: return

        AlertDialog.Builder(this)
            .setTitle(row.candidate.friendlyName)
            .setMessage(R.string.remove_saved_speaker_confirm)
            .setPositiveButton(R.string.remove) { _, _ ->
                removeSavedSpeaker(saved)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showRoomPicker(endpoint: SpeakerEndpoint) {
        val labels = arrayOf(
            getString(R.string.room_none),
            getString(R.string.room_living),
            getString(R.string.room_bedroom),
            getString(R.string.room_kitchen),
            getString(R.string.room_bathroom),
            getString(R.string.room_kids),
            getString(R.string.room_garden),
            getString(R.string.room_custom)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.room_title)
            .setItems(labels) { _, which ->
                when (which) {
                    0 -> saveSpeakerWithRoom(endpoint, null)
                    1 -> saveSpeakerWithRoom(endpoint, RoomAssignment(preset = RoomPreset.LIVING_ROOM))
                    2 -> saveSpeakerWithRoom(endpoint, RoomAssignment(preset = RoomPreset.BEDROOM))
                    3 -> saveSpeakerWithRoom(endpoint, RoomAssignment(preset = RoomPreset.KITCHEN))
                    4 -> saveSpeakerWithRoom(endpoint, RoomAssignment(preset = RoomPreset.BATHROOM))
                    5 -> saveSpeakerWithRoom(endpoint, RoomAssignment(preset = RoomPreset.KIDS_ROOM))
                    6 -> saveSpeakerWithRoom(endpoint, RoomAssignment(preset = RoomPreset.GARDEN))
                    else -> showCustomRoomDialog(endpoint)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCustomRoomDialog(endpoint: SpeakerEndpoint) {
        val input = EditText(this).apply {
            hint = getString(R.string.room_custom_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.room_custom_title)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                val room = input.text?.toString()?.trim().orEmpty()
                if (room.isBlank()) {
                    Toast.makeText(this, R.string.room_custom_invalid, Toast.LENGTH_LONG).show()
                } else {
                    saveSpeakerWithRoom(endpoint, RoomAssignment(customName = room))
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun saveSpeakerWithRoom(endpoint: SpeakerEndpoint, room: RoomAssignment?) {
        val saved = SavedSpeaker(endpoint, room)
        prefs.saveSpeaker(saved)

        val key = findRowKey(endpoint) ?: speakerIdentity(endpoint)
        val existing = speakers[key]
        speakers[key] = if (existing == null) {
            SpeakerRow(
                candidate = candidateFromEndpoint(endpoint),
                endpoint = endpoint,
                probeFinished = true,
                saved = saved,
                discovered = false
            )
        } else {
            existing.copy(saved = saved, endpoint = existing.endpoint ?: endpoint)
        }

        updateList()
        currentEndpoint?.takeIf { speakerMatches(it, endpoint) }?.let(::refreshConnectedStatus)
        Toast.makeText(this, R.string.speaker_saved, Toast.LENGTH_SHORT).show()
    }

    private fun removeSavedSpeaker(saved: SavedSpeaker) {
        prefs.removeSavedSpeaker(saved.endpoint)
        hiddenSpeakerKeys.addAll(speakerVisibilityKeys(saved.endpoint))

        val keysToRemove = speakers
            .filterValues { row ->
                val endpoint = row.endpoint ?: row.saved?.endpoint ?: SpeakerEndpoint(
                    host = row.candidate.host,
                    port = row.candidate.advertisedPort,
                    name = row.candidate.friendlyName,
                    model = row.candidate.model,
                    key = row.candidate.key
                )
                speakerMatches(endpoint, saved.endpoint)
            }
            .keys
            .toList()

        keysToRemove.forEach(speakers::remove)

        updateList()
        currentEndpoint?.takeIf { speakerMatches(it, saved.endpoint) }?.let(::refreshConnectedStatus)
        Toast.makeText(this, R.string.speaker_removed, Toast.LENGTH_SHORT).show()
    }

    private fun speakerVisibilityKeys(endpoint: SpeakerEndpoint): Set<String> = buildSet {
        endpoint.key?.takeIf { it.isNotBlank() }?.let { add("key:${it.lowercase()}") }
        add("host:${endpoint.host.lowercase()}")
    }

    private fun speakerVisibilityKeys(candidate: SpeakerCandidate): Set<String> = buildSet {
        candidate.key.takeIf { it.isNotBlank() }?.let { add("key:${it.lowercase()}") }
        add("host:${candidate.host.lowercase()}")
    }

    private fun roomLabel(room: RoomAssignment?): String? {
        if (room == null) return null
        room.customName?.takeIf { it.isNotBlank() }?.let { return it }
        return when (room.preset) {
            RoomPreset.LIVING_ROOM -> getString(R.string.room_living)
            RoomPreset.BEDROOM -> getString(R.string.room_bedroom)
            RoomPreset.KITCHEN -> getString(R.string.room_kitchen)
            RoomPreset.BATHROOM -> getString(R.string.room_bathroom)
            RoomPreset.KIDS_ROOM -> getString(R.string.room_kids)
            RoomPreset.GARDEN -> getString(R.string.room_garden)
            null -> null
        }
    }

    private fun isCurrent(row: SpeakerRow): Boolean {
        val current = currentEndpoint ?: return false
        val rowEndpoint = row.endpoint ?: row.saved?.endpoint ?: SpeakerEndpoint(
            host = row.candidate.host,
            port = row.candidate.advertisedPort,
            name = row.candidate.friendlyName,
            model = row.candidate.model,
            key = row.candidate.key
        )
        return speakerMatches(current, rowEndpoint)
    }

    private fun reachabilityForDisplay(row: SpeakerRow): Boolean? {
        if (row.saved == null) {
            return if (row.endpoint != null) true else null
        }
        if (!hasWifiTransport()) return false
        return if (row.probeFinished) {
            row.endpoint != null
        } else {
            row.lastReachable
        }
    }

    private fun markSavedSpeakersUnreachable() {
        prefs.loadSavedSpeakers().forEach { saved ->
            prefs.saveReachability(saved.endpoint, false)
        }
    }

    private fun updateSavedRowReachability(endpoint: SpeakerEndpoint, reachable: Boolean) {
        val key = findRowKey(endpoint) ?: return
        val row = speakers[key] ?: return
        if (row.saved == null) return

        speakers[key] = row.copy(
            endpoint = if (reachable) endpoint else null,
            probeFinished = true,
            lastReachable = reachable
        )

        if (!pageVisible) {
            adapter.replace(speakers.values.toList())
        }
    }

    private fun configureManagementHint() {
        if (!prefs.shouldShowManagementHint()) {
            managementHint.visibility = View.GONE
            return
        }

        val message = getString(R.string.speaker_management_hint)
        val hideLabel = getString(R.string.speaker_management_hide)
        val text = SpannableStringBuilder()
            .append(message)
            .append("  ")
            .append(hideLabel)
        val linkStart = text.length - hideLabel.length

        text.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    prefs.hideManagementHint()
                    managementHint.visibility = View.GONE
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = Color.parseColor("#8A5A00")
                    ds.isUnderlineText = true
                }
            },
            linkStart,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        managementHint.text = text
        managementHint.movementMethod = LinkMovementMethod.getInstance()
        managementHint.highlightColor = Color.TRANSPARENT
        managementHint.visibility = View.VISIBLE
    }

    private fun createPanelBackground(
        fillColor: String,
        strokeColor: String,
        strokeWidthDp: Int = 1
    ): GradientDrawable = GradientDrawable().apply {
        cornerRadius = dp(14).toFloat()
        setColor(Color.parseColor(fillColor))
        setStroke(dp(strokeWidthDp), Color.parseColor(strokeColor))
    }

    private fun parseManualAddress(rawInput: String): Pair<String, Int?>? {
        val raw = rawInput.trim()
        if (raw.isEmpty()) return null
        return try {
            val uri = URI(if (raw.contains("://")) raw else "http://$raw")
            val host = uri.host?.takeIf { it.isNotBlank() } ?: return null
            val port = uri.port.takeIf { it > 0 }
            host to port
        } catch (_: Exception) {
            null
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private data class SpeakerRow(
        val candidate: SpeakerCandidate,
        val endpoint: SpeakerEndpoint?,
        val probeFinished: Boolean,
        val saved: SavedSpeaker? = null,
        val discovered: Boolean = true,
        val lastReachable: Boolean? = null
    )

    private data class SpeakerRowViewHolder(
        val container: LinearLayout,
        val title: TextView,
        val details: TextView,
        val address: TextView,
        val roomButton: Button,
        val deleteButton: ImageButton
    )

    private inner class SpeakerAdapter : ArrayAdapter<SpeakerRow>(this, 0) {
        private val rows = mutableListOf<SpeakerRow>()

        fun replace(newRows: List<SpeakerRow>) {
            rows.clear()
            rows.addAll(
                newRows.sortedWith(
                    compareBy<SpeakerRow>(
                        { roomLabel(it.saved?.room)?.lowercase().orEmpty() },
                        { it.candidate.friendlyName.lowercase() }
                    )
                )
            )
            notifyDataSetChanged()
        }

        override fun getCount(): Int = rows.size

        override fun getItem(position: Int): SpeakerRow? = rows.getOrNull(position)

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val holder: SpeakerRowViewHolder
            val view: View

            if (convertView == null) {
                val container = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(14), dp(12), dp(10), dp(12))
                    minimumHeight = dp(88)
                }

                val textColumn = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val title = TextView(this@MainActivity).apply {
                    textSize = 18f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(Color.parseColor("#17324A"))
                }

                val details = TextView(this@MainActivity).apply {
                    textSize = 13f
                    setTextColor(Color.parseColor("#566173"))
                }

                val address = TextView(this@MainActivity).apply {
                    textSize = 12f
                    setTextColor(Color.parseColor("#748091"))
                }

                textColumn.addView(
                    title,
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                )
                textColumn.addView(
                    details,
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = dp(4)
                    }
                )
                textColumn.addView(
                    address,
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = dp(2)
                    }
                )

                val roomButton = Button(this@MainActivity).apply {
                    text = getString(R.string.room_action_label)
                    isAllCaps = false
                    isFocusable = false
                    isFocusableInTouchMode = false
                    textSize = 13f
                    minimumHeight = dp(40)
                    minWidth = 0
                    setPadding(dp(12), dp(8), dp(12), dp(8))
                }

                val deleteButton = ImageButton(this@MainActivity).apply {
                    setImageResource(android.R.drawable.ic_menu_delete)
                    contentDescription = getString(R.string.remove_saved_speaker)
                    setBackgroundColor(Color.TRANSPARENT)
                    isFocusable = false
                    isFocusableInTouchMode = false
                    setPadding(dp(10), dp(10), dp(10), dp(10))
                    minimumWidth = dp(44)
                    minimumHeight = dp(44)
                }

                container.addView(
                    textColumn,
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginEnd = dp(10)
                    }
                )
                container.addView(
                    roomButton,
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                )
                container.addView(
                    deleteButton,
                    LinearLayout.LayoutParams(dp(48), dp(48)).apply {
                        marginStart = dp(4)
                    }
                )

                holder = SpeakerRowViewHolder(
                    container = container,
                    title = title,
                    details = details,
                    address = address,
                    roomButton = roomButton,
                    deleteButton = deleteButton
                )
                container.tag = holder
                view = container
            } else {
                view = convertView
                holder = view.tag as SpeakerRowViewHolder
            }

            val row = rows[position]
            val room = roomLabel(row.saved?.room)
            val speakerTitle = if (room == null) {
                row.candidate.friendlyName
            } else {
                getString(R.string.speaker_room_title, row.candidate.friendlyName, room)
            }

            holder.title.text = speakerTitle

            holder.details.text = buildList {
                row.candidate.model?.let(::add)

                if (row.saved != null) {
                    add(
                        getString(
                            when (reachabilityForDisplay(row)) {
                                true -> R.string.speaker_online
                                false -> R.string.speaker_offline
                                null -> R.string.speaker_status_unknown
                            }
                        )
                    )
                } else if (row.endpoint == null && hasWifiTransport()) {
                    add(
                        getString(
                            if (row.probeFinished) {
                                R.string.str_not_reachable_short
                            } else {
                                R.string.checking_connection
                            }
                        )
                    )
                }
            }.joinToString(" · ")

            holder.address.text = buildList {
                row.candidate.version?.let { add(getString(R.string.version_format, it)) }
                add(hostForDisplay(row.candidate.host))
            }.joinToString(" · ")

            val roomEndpoint = resolveRoomEndpoint(row)
            holder.roomButton.text = getString(R.string.room_action_label)
            holder.roomButton.contentDescription = getString(R.string.room_action_description)
            holder.roomButton.isEnabled = roomEndpoint != null
            holder.roomButton.alpha = if (roomEndpoint != null) 1f else 0.45f
            holder.roomButton.setOnClickListener {
                assignRoomForRow(row)
            }

            holder.deleteButton.visibility = if (row.saved != null) View.VISIBLE else View.GONE
            holder.deleteButton.contentDescription = getString(R.string.remove_saved_speaker)
            holder.deleteButton.setOnClickListener {
                confirmRemoveSavedSpeaker(row)
            }

            holder.container.setOnClickListener {
                connectSpeakerRow(row)
            }

            val savedReachability = reachabilityForDisplay(row)
            val cardColors = when {
                row.saved != null && savedReachability == true ->
                    "#D8F8E4" to "#159447"
                row.saved != null && savedReachability == false ->
                    "#FFE0E4" to "#D92D3A"
                row.saved != null ->
                    "#FFFFFF" to "#D7E1EC"
                isCurrent(row) ->
                    "#D8F8E4" to "#159447"
                else ->
                    "#FFFFFF" to "#D7E1EC"
            }
            holder.container.background = createPanelBackground(
                fillColor = cardColors.first,
                strokeColor = cardColors.second,
                strokeWidthDp = if (row.saved != null || isCurrent(row)) 2 else 1
            )

            return view
        }
    }

}
