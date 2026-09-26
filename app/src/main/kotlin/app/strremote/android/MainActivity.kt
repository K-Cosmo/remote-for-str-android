package app.strremote.android

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
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
    }

    private lateinit var prefs: PreferencesStore
    private lateinit var discovery: StrDiscovery
    private val probe = EndpointProbe()

    private lateinit var statusText: TextView
    private lateinit var discoveryPanel: LinearLayout
    private lateinit var discoveryProgress: ProgressBar
    private lateinit var discoveryMessage: TextView
    private lateinit var webView: WebView
    private lateinit var adapter: SpeakerAdapter

    private val speakers = LinkedHashMap<String, SpeakerRow>()
    private var currentEndpoint: SpeakerEndpoint? = null
    private var pageVisible = false
    private var initialConnectionAttempted = false

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
        buildUi()
        registerBackCallback()
        configureWebView()
        ensurePermissionAndStart()
    }

    override fun onResume() {
        super.onResume()
        if (!initialConnectionAttempted && hasLocalNetworkPermission()) {
            startInitialConnection()
        }
    }

    override fun onDestroy() {
        discovery.stop()
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
            startInitialConnection()
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
            setPadding(dp(16), dp(8), dp(16), dp(4))
        }
        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
        }
        toolbar.addView(title, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val deviceButton = Button(this).apply {
            text = getString(R.string.devices)
            setOnClickListener {
                showDiscoveryPanel()
                startDiscovery()
            }
        }
        toolbar.addView(deviceButton)
        root.addView(
            toolbar,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        statusText = TextView(this).apply {
            textSize = 13f
            setPadding(dp(16), dp(2), dp(16), dp(8))
            visibility = View.GONE
        }
        root.addView(
            statusText,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        val content = FrameLayout(this)
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        discoveryPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(4))
        }
        content.addView(
            discoveryPanel,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        val discoveryHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
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
        }
        discoveryHeader.addView(
            discoveryMessage,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        discoveryPanel.addView(
            discoveryHeader,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        val speakerList = ListView(this).apply {
            dividerHeight = 1
        }
        adapter = SpeakerAdapter()
        speakerList.adapter = adapter
        speakerList.setOnItemClickListener { _, _, position, _ ->
            val row = adapter.getItem(position) ?: return@setOnItemClickListener
            row.endpoint?.let { loadEndpoint(it) } ?: probeCandidate(row.candidate)
        }
        discoveryPanel.addView(
            speakerList,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )

        val manualButton = Button(this).apply {
            text = getString(R.string.manual_ip)
            setOnClickListener { showManualDialog() }
        }
        discoveryPanel.addView(
            manualButton,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8)
            }
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
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                        true
                    } catch (_: Exception) {
                        true
                    }
                }
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                showWebView()
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                if (request.isForMainFrame) {
                    Toast.makeText(this@MainActivity, R.string.page_failed, Toast.LENGTH_LONG).show()
                    showDiscoveryPanel()
                    startDiscovery()
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
        discovery.stop()
        showDiscoveryPanel()
        discoveryProgress.visibility = View.GONE
        discoveryMessage.text = getString(R.string.permission_body)

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
        val saved = prefs.loadLastEndpoint()
        if (saved == null) {
            showDiscoveryPanel()
            startDiscovery()
            return
        }

        showDiscoveryPanel()
        discoveryProgress.visibility = View.VISIBLE
        discoveryMessage.text = getString(R.string.connecting_last)
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
                } else {
                    showDiscoveryPanel()
                    startDiscovery()
                }
            }
        }
    }

    private fun startDiscovery() {
        if (!hasLocalNetworkPermission()) {
            showPermissionRequired()
            return
        }
        speakers.clear()
        adapter.replace(emptyList())
        discoveryMessage.text = getString(R.string.searching)
        discoveryProgress.visibility = View.VISIBLE
        discovery.start()
    }

    private fun hasLocalNetworkPermission(): Boolean {
        val permission = requiredLocalNetworkPermission() ?: return true
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun handleDiscoveryStarted() {
        runOnUiThread {
            discoveryProgress.visibility = View.VISIBLE
            discoveryMessage.text = getString(R.string.searching)
        }
    }

    private fun handleSpeakerFound(candidate: SpeakerCandidate) {
        runOnUiThread {
            val existing = speakers[candidate.key]
            speakers[candidate.key] = SpeakerRow(candidate, existing?.endpoint)
            updateList()
            probeCandidate(candidate)
        }
    }

    private fun handleSpeakerLost(serviceName: String) {
        // Keep the row visible for this scan. STR speakers can briefly disappear from
        // mDNS while rebooting or entering/leaving standby.
    }

    private fun handleDiscoveryError(errorCode: Int) {
        runOnUiThread {
            discoveryProgress.visibility = View.GONE
            discoveryMessage.text = getString(R.string.discovery_failed, errorCode)
        }
    }

    private fun probeCandidate(candidate: SpeakerCandidate) {
        probe.probe(candidate) { endpoint ->
            runOnUiThread {
                val current = speakers[candidate.key] ?: SpeakerRow(candidate, null)
                speakers[candidate.key] = current.copy(endpoint = endpoint)
                updateList()
                if (endpoint != null) {
                    discoveryProgress.visibility = View.GONE
                    if (currentEndpoint == null && prefs.loadLastEndpoint()?.key == candidate.key) {
                        loadEndpoint(endpoint)
                    }
                }
            }
        }
    }

    private fun updateList() {
        adapter.replace(speakers.values.toList())
        if (speakers.isNotEmpty()) {
            discoveryProgress.visibility = View.GONE
            discoveryMessage.text = getString(R.string.found_devices)
        }
    }

    private fun loadEndpoint(endpoint: SpeakerEndpoint) {
        currentEndpoint = endpoint
        prefs.save(endpoint)
        statusText.text = getString(
            R.string.status_connected,
            endpoint.name,
            hostForDisplay(endpoint.host),
            endpoint.port
        )
        discovery.stop()
        webView.loadUrl(endpoint.baseUrl)
        showWebView()
    }

    private fun showWebView() {
        pageVisible = true
        statusText.visibility = View.VISIBLE
        discoveryPanel.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }

    private fun showDiscoveryPanel() {
        pageVisible = false
        statusText.visibility = View.GONE
        webView.visibility = View.GONE
        discoveryPanel.visibility = View.VISIBLE
    }

    private fun showManualDialog() {
        val input = EditText(this).apply {
            hint = getString(R.string.manual_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.manual_title)
            .setView(input)
            .setPositiveButton(R.string.connect) { _, _ ->
                val parsed = parseManualAddress(input.text?.toString().orEmpty())
                if (parsed == null) {
                    Toast.makeText(this, R.string.manual_invalid, Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                val (host, port) = parsed
                discoveryProgress.visibility = View.VISIBLE
                discoveryMessage.text = getString(R.string.connecting_to, host)
                probe.probeHost(host, port, host) { endpoint ->
                    runOnUiThread {
                        if (endpoint != null) {
                            loadEndpoint(endpoint)
                        } else {
                            discoveryProgress.visibility = View.GONE
                            discoveryMessage.text = getString(R.string.manual_failed)
                            Toast.makeText(this, R.string.manual_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
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
        val endpoint: SpeakerEndpoint?
    )

    private inner class SpeakerAdapter : ArrayAdapter<SpeakerRow>(this, android.R.layout.simple_list_item_2) {
        private val rows = mutableListOf<SpeakerRow>()

        fun replace(newRows: List<SpeakerRow>) {
            rows.clear()
            rows.addAll(newRows.sortedBy { it.candidate.friendlyName.lowercase() })
            notifyDataSetChanged()
        }

        override fun getCount(): Int = rows.size

        override fun getItem(position: Int): SpeakerRow? = rows.getOrNull(position)

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
            val row = rows[position]
            val title = view.findViewById<TextView>(android.R.id.text1)
            val subtitle = view.findViewById<TextView>(android.R.id.text2)
            title.text = row.candidate.friendlyName

            val details = buildList {
                row.candidate.model?.let(::add)
                add(hostForDisplay(row.candidate.host))
                row.candidate.version?.let { add(getString(R.string.version_format, it)) }
                if (row.endpoint == null) add(getString(R.string.not_reachable))
            }
            subtitle.text = details.joinToString(" · ")
            return view
        }
    }
}
