package app.strremote.android

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ColorDrawable
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
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.RadioButton
import android.widget.RadioGroup
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
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
 * BUILD-0012 / T02 — model-scoped, non-destructive SoundTouch 20 setup Wi-Fi probe.
 *
 * Uses system-consented WifiNetworkSpecifier; does not bind the process to the
 * requested Network. Read-only capability checks precede exactly one
 * user-triggered Wi-Fi profile transfer. Credentials are never saved or
 * logged; API acknowledgement alone does not prove Wi-Fi association.
 */
internal class BoseSetupProbeActivity : Activity() {
    private companion object {
        const val PERMISSION_REQUEST = 12012
        const val LOG_TAG = "StrBoseSetup"
        const val EXTRA_RESTART_AND_SEARCH = "bose_poc.restart_and_search"
        const val EXTRA_MODEL_ID = "bose_poc.model_id"
        const val EXTRA_B2_MODE = "bose_poc.b2_mode"
        const val HTTP_TIMEOUT_MS = 1_800
        // Android's Wi-Fi picker is itself an Activity. After association succeeds,
        // it may still be visible while Android finishes setup. A 2-second
        // background timer destroyed the approved network prematurely.
        const val BACKGROUND_CLEANUP_DELAY_MS = 45_000L
        const val RETRY_COOLDOWN_MS = 1_500L
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
    private val diagnosticEvents = mutableListOf<String>()
    // Android resources are not accessible before Activity.onCreate attaches Context.
    private var technicalReport = ""

    private lateinit var startButton: Button
    private lateinit var connectionSpinner: ProgressBar
    private lateinit var connectionProgressText: TextView
    private lateinit var cancelButton: Button
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var preparationPage: LinearLayout
    private lateinit var passwordPage: LinearLayout
    private lateinit var completionPage: LinearLayout
    private lateinit var feedbackPage: LinearLayout
    private lateinit var feedbackModelField: AutoCompleteTextView
    private lateinit var feedbackOutcomeGroup: RadioGroup
    private lateinit var feedbackErrorView: TextView
    private lateinit var completionFeedbackButton: Button
    private lateinit var earlyFeedbackPrepButton: Button
    private lateinit var earlyFeedbackConnectionButton: Button
    private var feedbackSuccessId = View.NO_ID
    private var feedbackFailureId = View.NO_ID
    private var feedbackOriginPage = 0
    private lateinit var manualNextButton: Button
    private lateinit var chosenPasswordNetworkView: TextView
    private lateinit var completionStatusText: TextView
    private lateinit var completionRetryButton: Button
    private lateinit var passwordEye: ImageButton
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
    private lateinit var strWebsiteButton: Button
    private var currentPage = 1
    private lateinit var pageScroll: ScrollView
    private lateinit var b2Panel: LinearLayout
    private lateinit var scanButton: Button
    private lateinit var surveyStatusView: TextView
    private lateinit var surveySpinner: ProgressBar
    private lateinit var modelInstructionTitle: TextView
    private lateinit var modelInstructionSteps: TextView
    private lateinit var modelInstructionHint: TextView
    private lateinit var modelTestNotice: TextView
    private var selectedModel = BoseSetupModel.SOUND_TOUCH_20_III
    private var passwordVisible = false
    private var setupPhase = BoseSetupPhase.PREPARING
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
     * passwords, response bodies and Android Network objects.
     */
    private fun trace(event: String) {
        val elapsed = if (requestStartedAtMs > 0L) {
            SystemClock.elapsedRealtime() - requestStartedAtMs
        } else {
            -1L
        }
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            Log.d(
                LOG_TAG,
                "session=$debugSessionId event=$event elapsedMs=$elapsed api=${Build.VERSION.SDK_INT}"
            )
        }
        // Sanitized in-app diagnostics are available if logcat cannot be read.
        // Event names never contain SSIDs, network IDs, credentials,
        // addresses, XML, response bodies or Android Network objects.
        synchronized(diagnosticEvents) {
            if (diagnosticEvents.size >= 70) diagnosticEvents.removeAt(0)
            diagnosticEvents.add("t=${elapsed}ms  ${event}")
        }
        handler.post {
            if (!isFinishing && !isDestroyed && ::resultsView.isInitialized) {
                refreshDiagnosticView()
            }
        }
    }

    private fun refreshDiagnosticView() {
        val events = synchronized(diagnosticEvents) { diagnosticEvents.toList() }
        resultsView.text = BoseTechnicalReportLocalization.translate(
            technicalReport, resources.configuration.locales[0].language
        ) + "\n\n" +
            getString(R.string.bose_technical_session) + "\n" +
            events.joinToString("\n")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        technicalReport = getString(R.string.bose_no_connection_initial)
        b2Mode = true // The guided setup is available in the production app.
        resumeWithFreshSearch = intent.getBooleanExtra(EXTRA_RESTART_AND_SEARCH, false)
        selectedModel = BoseSetupModel.fromId(
            savedInstanceState?.getString(EXTRA_MODEL_ID)
                ?: intent.getStringExtra(EXTRA_MODEL_ID)
        )
        buildUi()
        trace("activity_created freshRetry=$resumeWithFreshSearch")
        if (Build.VERSION.SDK_INT < 29) {
            statusView.text = getString(R.string.bose_android_too_old)
            nextButton.isEnabled = false
            startButton.isEnabled = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_MODEL_ID, selectedModel.name)
        super.onSaveInstanceState(outState)
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
        header.addView(text(getString(R.string.bose_title), 14f, accent))
        stepLabel = text(getString(R.string.bose_step_1_of_4), 13f, accent).apply {
            setTypeface(typeface, Typeface.BOLD)
        }
        header.addView(stepLabel)
        pageTitle = heading(getString(R.string.bose_prepare_title)).apply { textSize = 24f }
        header.addView(pageTitle)
        root.addView(header)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), 0, dp(18), dp(18))
        }
        preparationPage = panel()
        preparationPage.addView(text(
            getString(R.string.bose_prepare_intro)
        ))
        preparationPage.addView(heading(getString(R.string.bose_model_heading)))
        val models = BoseSetupModel.entries
        val modelSelection = Spinner(this).apply {
            minimumHeight = dp(58)
            background = card(Color.rgb(126, 148, 169))
            contentDescription = getString(R.string.bose_model_select_desc)
        }
        val modelAdapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item,
            models.map { getString(it.selectionLabelRes) }
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
                (super.getView(position, convertView, parent) as TextView).apply {
                    setTextColor(ink)
                    textSize = 16f
                    setPadding(dp(16), dp(12), dp(12), dp(12))
                }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
                (super.getDropDownView(position, convertView, parent) as TextView).apply {
                    setTextColor(ink)
                    setBackgroundColor(Color.WHITE)
                    textSize = 16f
                    minHeight = dp(54)
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                }
        }
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSelection.adapter = modelAdapter
        modelSelection.setSelection(models.indexOf(selectedModel).coerceAtLeast(0))
        modelSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selectedModel = models[position]
                renderModelInstructions()
            }
        }
        val dropdownFrame = FrameLayout(this)
        dropdownFrame.addView(modelSelection, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        // Custom Spinner background needs an explicit arrow so the selector
        // remains recognizable even in the light custom wizard theme.
        dropdownFrame.addView(TextView(this).apply {
            text = "▾"
            textSize = 25f
            setTextColor(ink)
            gravity = Gravity.CENTER
            isClickable = false
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }, FrameLayout.LayoutParams(
            dp(50), dp(54), Gravity.END or Gravity.CENTER_VERTICAL
        ))
        preparationPage.addView(dropdownFrame)
        modelTestNotice = text("", 14f, muted).apply {
            setPadding(0, dp(10), 0, dp(14))
        }
        preparationPage.addView(modelTestNotice)
        val instructionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = card()
            setPadding(dp(14), dp(12), dp(14), dp(6))
        }
        modelInstructionTitle = heading("")
        instructionCard.addView(modelInstructionTitle)
        modelInstructionSteps = text("")
        instructionCard.addView(modelInstructionSteps)
        modelInstructionHint = text("", 14f, muted)
        instructionCard.addView(modelInstructionHint)
        preparationPage.addView(instructionCard)
        renderModelInstructions()
        nextButton = Button(this).apply {
            text = getString(R.string.bose_yellow_button)
            isAllCaps = false
            minHeight = dp(54)
            setOnClickListener { showPage(2) }
        }
        preparationPage.addView(nextButton)
        earlyFeedbackPrepButton = Button(this).apply {
            text = getString(R.string.bose_early_feedback_prepare)
            isAllCaps = false
            minHeight = dp(54)
            visibility = View.GONE
            setOnClickListener { startCommunityFeedbackFromSetupFailure() }
        }
        preparationPage.addView(earlyFeedbackPrepButton)
        content.addView(preparationPage)

        connectionPage = panel()
        connectionPage.addView(text(
            getString(R.string.bose_connect_intro)
        ))
        val connectionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = card()
            setPadding(dp(16), dp(16), dp(16), dp(8))
        }
        connectionCard.addView(heading(getString(R.string.bose_how_it_works)))
        connectionCard.addView(text(
            getString(R.string.bose_connect_steps)
        ))
        connectionPage.addView(connectionCard)
        connectionPage.addView(text(
            getString(R.string.bose_connect_auto),
            14f
        ).apply { setPadding(0, dp(14), 0, dp(10)) })
        startButton = Button(this).apply {
            text = getString(R.string.bose_connect_button)
            isAllCaps = false
            minHeight = dp(54)
            setOnClickListener {
                if (b2Verified && b2Network != null) showPage(3)
                else if (hasStartedNetworkRequest) restartWizardForFreshSearch()
                else startOrRequestPermission()
            }
        }
        connectionPage.addView(startButton)
        earlyFeedbackConnectionButton = Button(this).apply {
            text = getString(R.string.bose_early_feedback_connect)
            isAllCaps = false
            minHeight = dp(54)
            visibility = View.GONE
            setOnClickListener { startCommunityFeedbackFromSetupFailure() }
        }
        connectionPage.addView(earlyFeedbackConnectionButton)
        val connectionProgress = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(14), dp(12), dp(6))
        }
        connectionSpinner = ProgressBar(
            this, null, android.R.attr.progressBarStyleSmall
        ).apply { isIndeterminate = true; visibility = View.GONE }
        connectionProgress.addView(connectionSpinner,
            LinearLayout.LayoutParams(dp(28), dp(28)))
        connectionProgressText = text(getString(R.string.bose_searching_ap), 14f).apply {
            visibility = View.GONE
            setPadding(dp(12), 0, 0, 0)
        }
        connectionProgress.addView(connectionProgressText)
        connectionPage.addView(connectionProgress)
        connectionPage.addView(text(
            getString(R.string.bose_connect_hint),
            14f
        ).apply { setPadding(0, dp(12), 0, 0) })
        content.addView(connectionPage)

        b2Panel = panel()
        b2Panel.addView(text(
            getString(R.string.bose_choose_intro)
        ))
        b2Panel.addView(heading(getString(R.string.bose_your_wifi)))
        selectedWifiView = text(getString(R.string.bose_none_selected), 16f, ink)
        b2Panel.addView(selectedWifiView)
        chooseWifiButton = Button(this).apply {
            text = getString(R.string.bose_choose_wifi)
            isAllCaps = false
            minHeight = dp(54)
            isEnabled = false
            setOnClickListener { showWifiPicker() }
        }
        b2Panel.addView(chooseWifiButton)
        scanButton = Button(this).apply {
            text = getString(R.string.bose_scan_again)
            isAllCaps = false
            minHeight = dp(48)
            isEnabled = false
            setOnClickListener { runBoseSurvey() }
        }
        b2Panel.addView(scanButton)
        val surveyBanner = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.rgb(229, 240, 251))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), Color.rgb(173, 205, 229))
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        surveySpinner = ProgressBar(
            this, null, android.R.attr.progressBarStyleSmall
        ).apply {
            isIndeterminate = true
            visibility = View.GONE
        }
        surveyBanner.addView(surveySpinner, LinearLayout.LayoutParams(
            dp(30), dp(30)
        ).apply { marginEnd = dp(14) })
        surveyStatusView = text(getString(R.string.bose_scan_preparing), 16f, ink).apply {
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, 0)
        }
        surveyBanner.addView(surveyStatusView, LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ))
        b2Panel.addView(surveyBanner, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(12); bottomMargin = dp(10) })
        // An extra popup owns the network list; do not make users scroll past
        // dozens of SSIDs to reach the password and submit button.
        manualWifiButton = Button(this).apply {
            text = getString(R.string.bose_manual_ssid_button)
            isAllCaps = false
            setOnClickListener {
                ssidField.text?.clear()
                passwordField.text?.clear()
                selectedWifiView.text = getString(R.string.bose_ssid_manual)
                manualNextButton.visibility = View.VISIBLE
                ssidLabel.visibility = View.VISIBLE
                ssidField.visibility = View.VISIBLE
                ssidField.requestFocus()
            }
        }
        b2Panel.addView(manualWifiButton)
        ssidLabel = heading(getString(R.string.bose_ssid_label)).apply { visibility = View.GONE }
        b2Panel.addView(ssidLabel)
        ssidField = EditText(this).apply {
            hint = getString(R.string.bose_ssid_hint)
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
        manualNextButton = Button(this).apply {
            text = getString(R.string.bose_next_password)
            isAllCaps = false
            minHeight = dp(54)
            visibility = View.GONE
            setOnClickListener {
                val name = ssidField.text.toString()
                if (name.length in 1..32 && name.all { it.code in 0x20..0x7e }) {
                    setupPhase = BoseSetupPhase.ENTERING_PASSWORD
                    showPage(4)
                } else {
                    surveyStatusView.text =
                        getString(R.string.bose_ssid_validation)
                }
            }
        }
        b2Panel.addView(manualNextButton)
        content.addView(b2Panel)

        passwordPage = panel()
        passwordPage.addView(text(
            getString(R.string.bose_enter_password_intro)
        ))
        chosenPasswordNetworkView = heading(getString(R.string.bose_wifi_label))
        passwordPage.addView(chosenPasswordNetworkView)
        passwordPage.addView(heading(getString(R.string.bose_password_label)).apply {
            setPadding(0, dp(18), 0, dp(10))
        })
        val passwordContainer = FrameLayout(this)
        passwordField = EditText(this).apply {
            hint = getString(R.string.bose_password_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            transformationMethod = PasswordTransformationMethod.getInstance()
            setTypeface(Typeface.DEFAULT)
            setSingleLine(true)
            textSize = 16f
            setTextColor(ink)
            setHintTextColor(muted)
            background = card(Color.rgb(126, 148, 169))
            setPadding(dp(12), dp(10), dp(60), dp(10))
            minHeight = dp(54)
            isSaveEnabled = false
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }
        passwordContainer.addView(passwordField, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        passwordEye = ImageButton(this).apply {
            // The icon depicts the CURRENT state: crossed-out when concealed,
            // open eye when visible. Accessible label describes the ACTION.
            setImageResource(R.drawable.ic_str_visibility_off)
            contentDescription = getString(R.string.bose_password_show)
            setBackgroundColor(Color.TRANSPARENT)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(dp(10), dp(10), dp(10), dp(10))
            isFocusable = true
            setOnClickListener { setPasswordVisible(!passwordVisible) }
        }
        passwordContainer.addView(passwordEye, FrameLayout.LayoutParams(
            dp(54), dp(54), Gravity.END or Gravity.CENTER_VERTICAL
        ))
        passwordPage.addView(passwordContainer)
        passwordPage.addView(text(
            getString(R.string.bose_password_security_note),
            13.5f
        ).apply { setPadding(0, dp(12), 0, dp(8)) })
        submitButton = Button(this).apply {
            text = getString(R.string.bose_submit)
            isAllCaps = false
            minHeight = dp(54)
            setOnClickListener { confirmB2Post() }
        }
        passwordPage.addView(submitButton)
        content.addView(passwordPage)

        resultPage = panel()
        resultPage.addView(heading(getString(R.string.bose_progress_heading)))
        resultSpinner = ProgressBar(this, null, android.R.attr.progressBarStyleLarge).apply {
            isIndeterminate = true
            visibility = View.GONE
        }
        resultPage.addView(resultSpinner, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(18) })
        resultText = text(
            getString(R.string.bose_progress_initial),
            16f, ink
        )
        resultPage.addView(resultText)
        resultPage.addView(text(
            getString(R.string.bose_verify_intro),
            14f
        ).apply { setPadding(0, dp(12), 0, 0) })
        content.addView(resultPage)

        completionPage = panel()
        completionPage.addView(heading(getString(R.string.bose_completion_heading)))
        completionStatusText = text(getString(R.string.bose_completion_initial), 16f, ink)
        completionPage.addView(completionStatusText)
        // The separate verification page owns the spinner. This summary
        // is shown only after a terminal verification result.
        completionPage.addView(heading(getString(R.string.bose_str_next)))
        completionPage.addView(text(
            getString(R.string.bose_str_explainer),
            14f
        ))
        strWebsiteButton = Button(this).apply {
            text = getString(R.string.bose_str_website_button)
            isAllCaps = false
            minHeight = dp(54)
            setOnClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://st-reborn.de")))
                } catch (_: Exception) {
                    statusView.text = getString(R.string.bose_str_website_failed)
                }
            }
        }
        completionPage.addView(strWebsiteButton)
        completionRetryButton = Button(this).apply {
            text = getString(R.string.bose_verify_again)
            isAllCaps = false
            visibility = View.GONE
            setOnClickListener { beginLanVerification() }
        }
        completionPage.addView(completionRetryButton)
        completionFeedbackButton = Button(this).apply {
            text = getString(R.string.bose_feedback_button)
            isAllCaps = false
            minHeight = dp(54)
            visibility = View.GONE
            setOnClickListener {
                feedbackOriginPage = BoseSetupFlow.COMPLETE_PAGE
                showPage(BoseSetupFlow.FEEDBACK_PAGE)
            }
        }
        completionPage.addView(completionFeedbackButton)
        content.addView(completionPage)

        feedbackPage = panel()
        feedbackPage.addView(heading(getString(R.string.bose_feedback_model_label)).apply {
            textSize = 17f
            setPadding(0, dp(8), 0, dp(4))
        })
        feedbackModelField = AutoCompleteTextView(this).apply {
            hint = getString(R.string.bose_feedback_model_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine(true)
            threshold = 1
            filters = arrayOf(InputFilter.LengthFilter(60))
            textSize = 16f
            setTextColor(ink)
            setHintTextColor(muted)
            background = card(Color.rgb(126, 148, 169))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            minHeight = dp(54)
            dropDownHeight = dp(190)
            setDropDownBackgroundDrawable(ColorDrawable(Color.WHITE))
            dropDownVerticalOffset = dp(4)
            isSaveEnabled = false
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            val models = BoseSoundTouchCatalog.modelSuggestions
            setAdapter(object : ArrayAdapter<String>(
                this@BoseSetupProbeActivity,
                android.R.layout.simple_dropdown_item_1line,
                models
            ) {
                override fun getView(
                    position: Int, convertView: View?, parent: ViewGroup
                ): View = (super.getView(position, convertView, parent) as TextView).apply {
                    setTextColor(ink)
                    setBackgroundColor(Color.WHITE)
                    textSize = 15f
                    setPadding(dp(12), dp(8), dp(12), dp(8))
                    minHeight = dp(44)
                }
                override fun getDropDownView(
                    position: Int, convertView: View?, parent: ViewGroup
                ): View = getView(position, convertView, parent)
            })
            // Suggestions are convenient, never mandatory: rare model names
            // can always be typed and submitted freely.
        }
        feedbackPage.addView(feedbackModelField)
        feedbackPage.addView(heading(getString(R.string.bose_feedback_success_label)).apply {
            textSize = 17f
            setPadding(0, dp(8), 0, dp(4))
        })
        feedbackOutcomeGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            background = card()
            setPadding(dp(12), dp(4), dp(12), dp(4))
        }
        feedbackSuccessId = View.generateViewId()
        feedbackFailureId = View.generateViewId()
        listOf(
            feedbackSuccessId to getString(R.string.bose_feedback_yes),
            feedbackFailureId to getString(R.string.bose_feedback_no)
        ).forEach { (id, label) ->
            feedbackOutcomeGroup.addView(RadioButton(this).apply {
                this.id = id
                text = label
                textSize = 16f
                setTextColor(ink)
                buttonTintList = ColorStateList.valueOf(accent)
                minHeight = dp(42)
                isChecked = false
            })
        }
        feedbackPage.addView(feedbackOutcomeGroup)
        feedbackErrorView = text("", 13f, Color.rgb(156, 49, 49)).apply {
            visibility = View.GONE
        }
        feedbackPage.addView(feedbackErrorView)
        feedbackPage.addView(text(
            getString(R.string.bose_feedback_email_intro),
            13f
        ).apply { setPadding(0, dp(6), 0, dp(6)) })
        feedbackPage.addView(Button(this).apply {
            text = getString(R.string.bose_feedback_mail_button)
            isAllCaps = false
            minHeight = dp(54)
            setOnClickListener { openCommunityFeedbackDraft() }
        })
        content.addView(feedbackPage)

        detailsButton = Button(this).apply {
            text = getString(R.string.bose_technical_show)
            isAllCaps = false
            setOnClickListener {
                val expanding = resultsView.visibility != View.VISIBLE
                resultsView.visibility = if (expanding) View.VISIBLE else View.GONE
                text = if (expanding) getString(R.string.bose_technical_hide)
                       else getString(R.string.bose_technical_show)
            }
        }
        content.addView(detailsButton)
        resultsView = text(getString(R.string.bose_diagnostics_empty), 13f, ink).apply {
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
            text = getString(R.string.bose_back)
            isAllCaps = false
            setOnClickListener {
                when (currentPage) {
                    2 -> {
                        closeSession()
                        hasStartedNetworkRequest = false
                        showPage(1)
                    }
                    3 -> showPage(2)
                    4 -> {
                        setPasswordVisible(false)
                        showPage(3)
                    }
                    BoseSetupFlow.FEEDBACK_PAGE -> {
                        if (feedbackOriginPage == BoseSetupFlow.COMPLETE_PAGE) {
                            showPage(BoseSetupFlow.COMPLETE_PAGE)
                        }
                    }
                }
            }
        }
        cancelButton = Button(this).apply {
            text = getString(R.string.bose_cancel_setup)
            isAllCaps = false
            setOnClickListener {
                if (postInFlight) {
                    AlertDialog.Builder(this@BoseSetupProbeActivity)
                        .setTitle(getString(R.string.bose_cancel_confirm_title))
                        .setMessage(
                            getString(R.string.bose_cancel_confirm_message)
                        )
                        .setNegativeButton(getString(R.string.bose_wait_continue), null)
                        .setPositiveButton(getString(R.string.bose_exit_anyway)) { _, _ ->
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

        // Users can take screenshots; password fields stay masked and
        // credentials are never persisted/logged. This is not an admin policy.
        // Light system bars require dark status/navigation icons.
        val lightBars = Color.rgb(246, 248, 251)
        @Suppress("DEPRECATION")
        window.statusBarColor = lightBars
        @Suppress("DEPRECATION")
        window.navigationBarColor = lightBars
        if (Build.VERSION.SDK_INT >= 30) {
            val appearance = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            window.insetsController?.setSystemBarsAppearance(appearance, appearance)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
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
        // Initial Spinner selection may be restored before the two optional
        // feedback actions exist; synchronize their visibility after all views.
        renderModelInstructions()
        showPage(if (resumeWithFreshSearch) 2 else 1)
    }

    private fun showPage(page: Int) {
        currentPage = page
        preparationPage.visibility = if (page == 1) View.VISIBLE else View.GONE
        connectionPage.visibility = if (page == 2) View.VISIBLE else View.GONE
        b2Panel.visibility = if (page == 3) View.VISIBLE else View.GONE
        passwordPage.visibility = if (page == 4) View.VISIBLE else View.GONE
        resultPage.visibility = if (page == 5) View.VISIBLE else View.GONE
        completionPage.visibility = if (page == 6) View.VISIBLE else View.GONE
        feedbackPage.visibility = if (page == BoseSetupFlow.FEEDBACK_PAGE) View.VISIBLE else View.GONE
        stepLabel.text = if (page == BoseSetupFlow.FEEDBACK_PAGE) getString(R.string.bose_step_feedback)
            else if (page >= 5) getString(R.string.bose_step_completion)
            else getString(R.string.bose_step_indicator, page, BoseSetupFlow.STEP_COUNT)
        pageTitle.text = when (page) {
            1 -> getString(R.string.bose_prepare_title)
            2 -> getString(R.string.bose_connect_button)
            3 -> getString(R.string.bose_page_choose)
            4 -> getString(R.string.bose_password_hint)
            5 -> getString(R.string.bose_page_verify)
            6 -> getString(R.string.bose_page_result)
            else -> getString(R.string.bose_page_feedback)
        }
        if (page == 4) {
            chosenPasswordNetworkView.text = getString(R.string.bose_chosen_ssid, ssidField.text.toString())
            setPasswordVisible(false)
        }
        statusView.visibility = if (page == 1 || page == BoseSetupFlow.FEEDBACK_PAGE)
            View.GONE else View.VISIBLE
        detailsButton.visibility = if (page == 1 || page == BoseSetupFlow.FEEDBACK_PAGE)
            View.GONE else View.VISIBLE
        if (page == BoseSetupFlow.FEEDBACK_PAGE) {
            resultsView.visibility = View.GONE
            detailsButton.text = getString(R.string.bose_technical_show)
        }
        backButton.visibility =
            if (page in 2..4 ||
                (page == BoseSetupFlow.FEEDBACK_PAGE &&
                    feedbackOriginPage == BoseSetupFlow.COMPLETE_PAGE)
            ) View.VISIBLE else View.GONE
        val mayFinish = BoseSetupFlow.mayFinish(setupPhase, page)
        cancelButton.text = if (mayFinish) getString(R.string.bose_finish)
                            else if (page == 5) getString(R.string.bose_cancel)
                            else getString(R.string.bose_cancel_setup)
        cancelButton.isEnabled = true
        pageScroll.post { pageScroll.scrollTo(0, 0) }
        trace("wizard_page_" + page)
    }

    private fun setConnectionBusy(busy: Boolean, message: String = getString(R.string.bose_searching_ap)) {
        connectionSpinner.visibility = if (busy) View.VISIBLE else View.GONE
        connectionProgressText.visibility = if (busy) View.VISIBLE else View.GONE
        connectionProgressText.text = message
        if (!busy && currentPage == 2) startButton.isEnabled = true
    }

    private fun finishSetup(offerRetry: Boolean = false) {
        setupPhase = BoseSetupPhase.TERMINAL
        resultSpinner.visibility = View.GONE
        completionStatusText.text = resultText.text
        completionRetryButton.visibility = if (offerRetry) View.VISIBLE else View.GONE
        completionFeedbackButton.visibility =
            if (selectedModel == BoseSetupModel.OTHER_SOUND_TOUCH) View.VISIBLE else View.GONE
        showPage(BoseSetupFlow.COMPLETE_PAGE)
    }

    private fun setPasswordVisible(visible: Boolean) {
        if (!::passwordField.isInitialized || !::passwordEye.isInitialized) return
        val selection = passwordField.selectionStart.coerceAtLeast(0)
        passwordVisible = visible
        passwordField.transformationMethod =
            if (visible) null else PasswordTransformationMethod.getInstance()
        passwordEye.setImageResource(
            if (visible) R.drawable.ic_str_visibility else R.drawable.ic_str_visibility_off
        )
        passwordEye.contentDescription =
            if (visible) getString(R.string.bose_password_hide) else getString(R.string.bose_password_show)
        passwordField.setSelection(selection.coerceAtMost(passwordField.text.length))
    }

    private fun renderModelInstructions() {
        if (!::modelInstructionTitle.isInitialized) return
        val other = selectedModel == BoseSetupModel.OTHER_SOUND_TOUCH
        modelInstructionTitle.text = getString(R.string.bose_model_instruction_title, getString(selectedModel.modelLabelRes))
        modelInstructionSteps.text =
            getString(R.string.bose_setup_button_steps)
        modelInstructionHint.text = getString(selectedModel.setupHintRes)
        // The model-specific instructions already warn about untested
        // devices. Avoid a second large warning that pushes the action below
        // the fold on ordinary phones.
        modelTestNotice.visibility = if (other) View.GONE else View.VISIBLE
        modelTestNotice.text = if (other) "" else getString(R.string.bose_model_checked)
        if (::earlyFeedbackPrepButton.isInitialized) {
            earlyFeedbackPrepButton.visibility = if (other) View.VISIBLE else View.GONE
        }
        if (::earlyFeedbackConnectionButton.isInitialized) {
            earlyFeedbackConnectionButton.visibility = if (other) View.VISIBLE else View.GONE
        }
        if (::completionFeedbackButton.isInitialized) {
            completionFeedbackButton.visibility = if (other) View.VISIBLE else View.GONE
        }
    }

    private fun startCommunityFeedbackFromSetupFailure() {
        if (selectedModel != BoseSetupModel.OTHER_SOUND_TOUCH || postInFlight) return
        closeSession()
        setupPhase = BoseSetupPhase.TERMINAL
        feedbackOriginPage = 0
        showPage(BoseSetupFlow.FEEDBACK_PAGE)
    }

    private fun openCommunityFeedbackDraft() {
        if (selectedModel != BoseSetupModel.OTHER_SOUND_TOUCH ||
            setupPhase != BoseSetupPhase.TERMINAL
        ) return

        val outcome = when (feedbackOutcomeGroup.checkedRadioButtonId) {
            feedbackSuccessId -> BoseCommunityOutcome.SUCCESS
            feedbackFailureId -> BoseCommunityOutcome.FAILURE
            else -> null
        }
        @Suppress("DEPRECATION")
        val installedVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: getString(R.string.bose_version_unknown)
        } catch (_: Exception) {
            getString(R.string.bose_version_unknown)
        }
        val appVersion = if (
            applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        ) "$installedVersion (Debug)" else installedVersion
        val androidVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        val draft = BoseCommunityFeedback.compose(
            feedbackModelField.text.toString(), outcome, appVersion, androidVersion,
            resources.configuration.locales[0].language
        )
        if (draft == null) {
            feedbackErrorView.text =
                getString(R.string.bose_feedback_validation)
            feedbackErrorView.visibility = View.VISIBLE
            return
        }
        feedbackErrorView.visibility = View.GONE
        // No automatic sending, no account integration and no user/device
        // identifiers beyond the explicitly requested app/Android versions.
        val mailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.parse("mailto:${BoseCommunityFeedback.RECIPIENT}")
        ).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(draft.recipient))
            putExtra(Intent.EXTRA_SUBJECT, draft.subject)
            putExtra(Intent.EXTRA_TEXT, draft.body)
        }
        try {
            startActivity(Intent.createChooser(mailIntent, getString(R.string.bose_email_chooser)))
        } catch (_: Exception) {
            feedbackErrorView.text =
                getString(R.string.bose_email_no_app)
            feedbackErrorView.visibility = View.VISIBLE
        }
    }

    private fun setSurveyBusy(busy: Boolean) {
        if (::surveySpinner.isInitialized) {
            surveySpinner.visibility = if (busy) View.VISIBLE else View.GONE
        }
    }

    private fun showWifiPicker() {
        if (surveyInFlight || postAttempted || currentPage != 3) return
        val available = surveyCandidates.filter { it.selectable }
        if (available.isEmpty()) {
            surveyStatusView.text =
                getString(R.string.bose_no_compatible_wifi)
            return
        }

        // Purpose-built, accessible two-line rows rather than AlertDialog.setItems
        // with concatenated SSID and signal labels. All SSIDs stay on this device.
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = false
            addView(list)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.bose_choose_wifi))
            .setView(scroll)
            .setNegativeButton(getString(R.string.bose_cancel), null)
            .create()

        available.forEach { candidate ->
            val signal = when {
                candidate.signalDbm == null -> getString(R.string.bose_signal_unknown)
                candidate.signalDbm >= -55 -> getString(R.string.bose_signal_strong)
                candidate.signalDbm >= -70 -> getString(R.string.bose_signal_medium)
                else -> getString(R.string.bose_signal_weak)
            }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(12), dp(12), dp(12))
                background = GradientDrawable().apply {
                    setColor(if (candidate.ssid == ssidField.text.toString())
                        Color.rgb(231, 243, 255) else Color.WHITE)
                    cornerRadius = dp(10).toFloat()
                    setStroke(dp(1), Color.rgb(199, 214, 228))
                }
                minimumHeight = dp(68)
                isFocusable = true
                isClickable = true
                contentDescription = getString(R.string.bose_ssid_row_accessibility, candidate.ssid, signal)
                setOnClickListener {
                    ssidField.setText(candidate.ssid)
                    ssidLabel.visibility = View.GONE
                    ssidField.visibility = View.GONE
                    manualNextButton.visibility = View.GONE
                    selectedWifiView.text = getString(R.string.bose_ssid_selected_label, candidate.ssid)
                    passwordField.text?.clear()
                    surveyStatusView.text = getString(R.string.bose_wifi_selected)
                    setupPhase = BoseSetupPhase.ENTERING_PASSWORD
                    dialog.dismiss()
                    showPage(4)
                }
            }
            row.addView(TextView(this).apply {
                text = (if (candidate.ssid == ssidField.text.toString()) "✓  " else "") +
                    candidate.ssid
                textSize = 17f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.rgb(23, 46, 67))
                setSingleLine(true)
                ellipsize = android.text.TextUtils.TruncateAt.END
            })
            row.addView(TextView(this).apply {
                text = "◉  " + signal + "  ·  WPA2"
                textSize = 13f
                setTextColor(Color.rgb(77, 100, 123))
                setPadding(0, dp(4), 0, 0)
            })
            list.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(7) })
        }
        scroll.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            minOf(dp(400), available.size * dp(76) + dp(24))
        )
        dialog.show()
    }

    private fun renderSurveyNetworks() {
        // The list is shown in a compact popup rather than inline rows.
        val count = surveyCandidates.count { it.selectable }
        chooseWifiButton.isEnabled = count > 0 && !surveyInFlight && !postAttempted
        chooseWifiButton.text = if (count > 0) getString(R.string.bose_networks_found, count)
                               else getString(R.string.bose_choose_wifi)
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
            putExtra(EXTRA_MODEL_ID, selectedModel.name)
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
                getString(R.string.bose_searching_permission)
            return
        }

        val missing = requiredPermissions().filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            trace("runtime_permission_request count=${missing.size}")
            statusView.text = getString(R.string.bose_connection_permission_first)
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
            statusView.text = getString(R.string.bose_connection_permission_denied)
        }
    }

    @android.annotation.TargetApi(29)
    private fun requestSetupNetwork() {
        trace("request_prepare")
        closeSession()
        showPage(2)
        setConnectionBusy(true, getString(R.string.bose_scan_start_wait))

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
        trace("request_configured token=$token timeout=none user_controls_picker")
        hasStartedNetworkRequest = true
        awaitingConsent = true
        // Retry opens a fresh Activity and a fresh WifiNetworkSpecifier request.
        // No overlapping Android picker/network callbacks survive the restart.
        startButton.text = getString(R.string.bose_try_again)
        startButton.isEnabled = false
        technicalReport = getString(R.string.bose_connection_prep_report)
        refreshDiagnosticView()
        statusView.text =
            getString(R.string.bose_network_picker_instruction)

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
                    setConnectionBusy(true, getString(R.string.bose_connection_verifying))
                    statusView.text =
                        getString(R.string.bose_connected_next)
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
                    setConnectionBusy(false)
                    statusView.text =
                        getString(R.string.bose_network_unavailable)
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
                        trace("wifi_setup_network_lost_during_transfer")
                        statusView.text =
                            getString(R.string.bose_network_switching)
                    } else {
                        closeSession()
                        showPage(2)
                        setConnectionBusy(false)
                        startButton.text = getString(R.string.bose_try_again)
                        statusView.text =
                            getString(R.string.bose_network_lost)
                    }
                }
            }
        }
        // Keep the original, device-validated immediate connection path.
        // Retry recreates the setup Activity after callback release.
        activeCallback = callback
        try {
            trace("requestNetwork_call token=$token")
            // No 20-second timeout: it also ran while the user was still
            // reading the Android chooser and made the system display
            // "App cancelled the device selection request".
            // The user can cancel in the system dialog or in this Activity.
            connectivity.requestNetwork(request, callback)
            trace("requestNetwork_returned token=$token")
        } catch (e: SecurityException) {
            trace("requestNetwork_error SecurityException")
            closeSession()
            setConnectionBusy(false)
            statusView.text =
                getString(R.string.bose_android_denied_network)
        } catch (e: IllegalArgumentException) {
            trace("requestNetwork_error IllegalArgumentException")
            closeSession()
            setConnectionBusy(false)
            statusView.text = getString(R.string.bose_permission_config_error)
        } catch (e: RuntimeException) {
            trace("requestNetwork_error ${e.javaClass.simpleName}")
            closeSession()
            setConnectionBusy(false)
            statusView.text = getString(R.string.bose_wifi_request_failure, e.javaClass.simpleName)
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
                        ?: getString(R.string.bose_version_unknown)
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
            // Bounded read-only capability inquiry. Only the names
            // of three known Wi-Fi-related API endpoints are surfaced; the
            // original XML body is discarded and cannot enter logs/UI.
            trace("bose_capabilities_check_start token=$token")
            val b1Report = BoseSupportedUrlsProbe.inspectReport(network)
            val identifiedDeviceId = BoseLanIdentity.getDeviceId(network, "192.0.2.1")
            observations.add(b1Report.summary)
            trace("bose_capabilities_check_finished token=$token")
            runOnUiThread {
                if (token != generation) return@runOnUiThread
                trace("http_probe_complete token=$token")
                technicalReport = observations.joinToString("\n")
                refreshDiagnosticView()
                if (b2Mode && b1Report.supportsAddWirelessProfile) {
                    b2Network = network
                    boseDeviceId = identifiedDeviceId
                    b2Verified = true
                    surveySupported = b1Report.supportsWirelessSiteSurvey
                    startButton.text = getString(R.string.bose_continue_to_wifi)
                    scanButton.isEnabled = surveySupported
                    surveyStatusView.text = if (surveySupported) {
                        getString(R.string.bose_scan_preparing)
                    } else {
                        getString(R.string.bose_survey_unavailable)
                    }
                    setSurveyBusy(false)
                    statusView.text =
                        getString(R.string.bose_wifi_scanning_error)
                    setupPhase = BoseSetupPhase.CHOOSING_NETWORK
                    setConnectionBusy(false)
                    trace("wifi_setup_ready token=$token surveySupported=$surveySupported")
                    showPage(3)
                    if (surveySupported) runBoseSurvey()
                } else {
                    closeSession()
                    showPage(2)
                    setConnectionBusy(false)
                    startButton.text = getString(R.string.bose_try_again)
                    statusView.text =
                        getString(R.string.bose_bose_setup_not_ready)
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
        surveyStatusView.text = getString(R.string.bose_scan_waiting)
        setSurveyBusy(true)
        val token = generation
        trace("wifi_search_start token=$token")
        val worker = Thread({
            val result = BoseWirelessSiteSurvey.scan(network)
            runOnUiThread {
                if (token != generation) return@runOnUiThread
                surveyInFlight = false
                setSurveyBusy(false)
                scanButton.isEnabled = true
                submitButton.isEnabled = true
                surveyCandidates = result.candidates
                surveyStatusView.text = if (result.candidates.isNotEmpty()) {
                    getString(R.string.bose_scan_found_count, result.candidates.size)
                } else {
                    getString(R.string.bose_survey_empty)
                }
                technicalReport += "\n\n" + getString(R.string.bose_technical_survey) + " " + result.status
                refreshDiagnosticView()
                trace("wifi_search_end token=$token count=${result.candidates.size}")
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
        if (currentPage != BoseSetupFlow.PASSWORD_PAGE) return
        val ssid = ssidField.text.toString()
        val payload = try {
            BoseWirelessProfileXml.buildCandidate(
                ssid, passwordField.text.toString()
            ).toByteArray(Charsets.UTF_8)
        } catch (_: IllegalArgumentException) {
            statusView.text =
                getString(R.string.bose_invalid_credentials)
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
        setupPhase = BoseSetupPhase.SENDING
        val token = generation
        resultSpinner.visibility = View.VISIBLE
        resultText.text = getString(R.string.bose_transfer_running)
        statusView.text = getString(R.string.bose_waiting_for_reconnect)
        showPage(BoseSetupFlow.VERIFY_PAGE)
        trace("wifi_transfer_started token=$token")
        val worker = Thread({
            val result = try {
                BoseWirelessProfilePost.submitOnce(network, payload)
            } finally {
                payload.fill(0)
            }
            runOnUiThread {
                if (generation != token || isFinishing || isDestroyed) return@runOnUiThread
                trace("wifi_transfer_completed token=$token")
                profileResponseAcknowledged = result.contains("AddWirelessProfileResponse erkannt")
                // Store only that setup was attempted/acknowledged; this is
                // deliberately NOT a saved STR endpoint. No SSID or password.
                if (profileResponseAcknowledged || result.contains("unbestimmt")) {
                    boseDeviceId?.let { PendingBoseStore(this).recordSubmission(it) }
                }
                val disconnected = b2NetworkLostDuringPost
                closeSession()
                technicalReport += "\n\n" + result
                refreshDiagnosticView()
                if (disconnected) {
                    technicalReport += "\n" + getString(R.string.bose_technical_lost)
                    refreshDiagnosticView()
                }
                resultText.text =
                    if (profileResponseAcknowledged) {
                        getString(R.string.bose_transfer_confirmed)
                    } else {
                        getString(R.string.bose_transfer_uncertain)
                    }
                statusView.text = getString(R.string.bose_wait_wifi)
                beginLanVerification()
            }
        }, "BoseSetupSingleProfilePost")
        activeWorker = worker
        worker.start()
    }

    private fun beginLanVerification() {
        setupPhase = BoseSetupPhase.VERIFYING
        showPage(BoseSetupFlow.VERIFY_PAGE)
        lanVerification?.stop()
        lanVerification = null
        val id = boseDeviceId
        val ssid = pendingWifiSsid
        if (id.isNullOrEmpty() || ssid.isNullOrEmpty()) {
            resultSpinner.visibility = View.GONE
                resultText.text =
                getString(R.string.bose_verification_unsupported)
            statusView.text = getString(R.string.bose_setup_unconfirmed_short)
            finishSetup()
            return
        }
        resultSpinner.visibility = View.VISIBLE
        resultText.text =
            getString(R.string.bose_wait_lan)
        val verifier = BoseLanVerification(this, id, ssid, lastBoseSetupNetwork) { outcome ->
            if (isFinishing || isDestroyed ||
                currentPage != BoseSetupFlow.VERIFY_PAGE ||
                setupPhase != BoseSetupPhase.VERIFYING
            ) return@BoseLanVerification
            when (outcome) {
                BoseLanVerification.Status.WaitingForWifi -> {
                    resultSpinner.visibility = View.VISIBLE
                    resultText.text = getString(R.string.bose_wait_screen_wifi)
                    statusView.text = getString(R.string.bose_wait_wifi_short)
                }
                BoseLanVerification.Status.Searching -> {
                    resultSpinner.visibility = View.VISIBLE
                    resultText.text = getString(R.string.bose_speaker_searching)
                    statusView.text = getString(R.string.bose_speaker_search_status)
                }
                is BoseLanVerification.Status.Found -> {
                    boseDeviceId?.let { PendingBoseStore(this).recordVerified(it, outcome.host) }
                    resultSpinner.visibility = View.GONE
                                resultText.text =
                        if (outcome.wlanName != null) {
                            getString(R.string.bose_verified_wifi, outcome.wlanName)
                        } else {
                            getString(R.string.bose_verified_no_ssid)
                        }
                    statusView.text = getString(R.string.bose_status_success)
                    finishSetup()
                }
                is BoseLanVerification.Status.OtherWifi -> {
                    // The exact Bose identity is confirmed, but the selected
                    // network name does not match. Do not mark setup verified.
                    resultSpinner.visibility = View.GONE
                    resultText.text =
                        getString(R.string.bose_different_wifi, outcome.wlanName)
                    statusView.text = getString(R.string.bose_status_wrong_wifi)
                    finishSetup(offerRetry = true)
                }
                BoseLanVerification.Status.NotFound -> {
                    resultSpinner.visibility = View.GONE
                    resultText.text =
                        getString(R.string.bose_not_found_final)
                    statusView.text = getString(R.string.bose_status_unconfirmed)
                    finishSetup(offerRetry = true)
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
                trace("background_cleanup_fired")
                closeSession()
                showPage(2)
                startButton.text = getString(R.string.bose_try_again)
                statusView.text =
                    getString(R.string.bose_background_connection_lost)
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
        setSurveyBusy(false)
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
        trace("activity_started")
    }

    override fun onResume() {
        super.onResume()
        trace("activity_resumed retry=$resumeWithFreshSearch")
        if (resumeWithFreshSearch) {
            resumeWithFreshSearch = false
            // Post from resumed UI: no request while the previous Activity is
            // closing or the new Activity has not yet entered foreground.
            handler.post {
                if (!isFinishing && !isDestroyed) {
                    statusView.text =
                        getString(R.string.bose_retry_wait)
                    handler.postDelayed({
                        if (!isFinishing && !isDestroyed) startOrRequestPermission()
                    }, RETRY_COOLDOWN_MS)
                }
            }
        }
    }

    override fun onStop() {
        visible = false
        trace("activity_stopped awaitingConsent=$awaitingConsent callbackRegistered=${activeCallback != null}")
        // Android's own network-consent UI can temporarily stop this Activity.
        // Keep the Android Wi-Fi picker request; release connected networks in background.
        if (!awaitingConsent && !postInFlight && activeCallback != null) {
            trace("background_cleanup_scheduled delayMs=$BACKGROUND_CLEANUP_DELAY_MS")
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
