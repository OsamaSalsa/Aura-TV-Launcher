package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.iptv.IptvScreen
import com.example.ui.iptv.IptvViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.games.CrazyGamesScreen
import com.example.ui.games.PokiScreen
import com.example.ui.games.PlayhopScreen
import com.example.ui.games.EasyGamesScreen
import com.example.ui.games.AuraStoreScreen
import com.example.ui.games.CustomWebPortalScreen
import com.example.ui.downloader.MiniDownloaderScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.youtube.YouTubeScreen
import com.example.ui.youtube.YouTubeViewModel

import android.content.Intent
import android.net.VpnService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.example.util.LocaleHelper

enum class LauncherScreen {
    HOME, IPTV, YOUTUBE, SETTINGS, CRAZYGAMES, POKI, PLAYHOP, EASYGAMES, MINI_DOWNLOADER, AURA_STORE, CUSTOM_WEB_PORTAL
}

private var hasShownIntroProcessFlag = false

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_AURA_VOICE_SEARCH = "com.example.ACTION_AURA_VOICE_SEARCH"

        @Volatile
        private var activeInstanceRef: java.lang.ref.WeakReference<MainActivity>? = null

        @Volatile
        private var isForegroundResumed: Boolean = false

        fun triggerVoiceSearchIfForeground(): Boolean {
            val activity = activeInstanceRef?.get() ?: return false
            if (!isForegroundResumed || activity.isFinishing || activity.isDestroyed) return false
            activity.runOnUiThread {
                activity.requestVoiceSearch()
            }
            return true
        }
    }

    private var homeVmRef: HomeViewModel? = null
    private var isRtlLayout: Boolean = false
    private var shouldKeepScreenOnFlag: Boolean = true
    private var sawVoiceKeyDown: Boolean = false
    private val _onHomePressed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onHomePressed: SharedFlow<Unit> = _onHomePressed.asSharedFlow()
    private val _voiceSearchRequestCount = MutableStateFlow(0)
    val voiceSearchRequestCount: StateFlow<Int> = _voiceSearchRequestCount.asStateFlow()
    private val _onUserActivity = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val onUserActivity: SharedFlow<Long> = _onUserActivity.asSharedFlow()

    fun requestVoiceSearch() {
        _voiceSearchRequestCount.value = _voiceSearchRequestCount.value + 1
    }

    /**
     * Controls whether Aura TV keeps the system screen awake to block external/system screensavers:
     * - When internal screensaver is ENABLED (or media is playing): keepAwake = true -> sets FLAG_KEEP_SCREEN_ON
     *   and decorView.keepScreenOn = true so the app acts as continuously active and external screensavers never trigger.
     * - When internal screensaver is DISABLED ("never" / timeoutMinutes == 0) and no media is playing: keepAwake = false -> clears
     *   FLAG_KEEP_SCREEN_ON and decorView.keepScreenOn = false so external/system screensavers have full permission to run.
     */
    fun updateScreenAwakePolicy(keepAwake: Boolean) {
        shouldKeepScreenOnFlag = keepAwake
        val applyPolicy = {
            try {
                val flags = window.attributes?.flags ?: 0
                val hasKeepOnFlag = (flags and android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
                if (keepAwake) {
                    if (!hasKeepOnFlag) {
                        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    if (!window.decorView.keepScreenOn) {
                        window.decorView.keepScreenOn = true
                    }
                } else {
                    if (hasKeepOnFlag) {
                        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    if (window.decorView.keepScreenOn) {
                        window.decorView.keepScreenOn = false
                    }
                }
            } catch (_: Exception) {}
        }
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            applyPolicy()
        } else {
            runOnUiThread(applyPolicy)
        }
    }

    private fun isVoiceOrSearchIntent(intent: Intent?): Boolean {
        val action = intent?.action ?: return false
        return action == ACTION_AURA_VOICE_SEARCH ||
                action == Intent.ACTION_ASSIST ||
                action == Intent.ACTION_VOICE_COMMAND ||
                action == "android.intent.action.VOICE_ASSIST" ||
                action == Intent.ACTION_SEARCH ||
                action == Intent.ACTION_SEARCH_LONG_PRESS ||
                action == "android.search.action.GLOBAL_SEARCH" ||
                action == "android.speech.action.WEB_SEARCH" ||
                action == "android.speech.action.VOICE_SEARCH_HANDS_FREE"
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        _onUserActivity.tryEmit(System.currentTimeMillis())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installKeyInterceptionCallback()

        val app = application as AuraTvApp

        // Read initial screensaver preference immediately so window flags match before first frame
        updateScreenAwakePolicy(true)

        // Hide system bars (Status Bar, Navigation Bar) for full screen TV experience
        hideSystemBars(force = true)

        if (isVoiceOrSearchIntent(intent)) {
            requestVoiceSearch()
        }

        setContent {
            val registryOwner = requireNotNull(androidx.activity.compose.LocalActivityResultRegistryOwner.current) {
                "No ActivityResultRegistryOwner was provided"
            }
            val initialLang = remember { app.preferences.getCachedLanguage() }
            val currentLang by app.preferences.languageFlow.collectAsState(initial = initialLang)
            val baseContext = LocalContext.current
            val localizedContext = remember(currentLang, baseContext) {
                LocaleHelper.setLocale(baseContext, currentLang)
            }

            val effectiveLang = remember(currentLang) {
                LocaleHelper.getEffectiveLanguage(currentLang)
            }
            val isRtl = LocaleHelper.isRtlLanguage(currentLang)
            val isArabic = effectiveLang in setOf("ar", "ur", "fa")
            isRtlLayout = isRtl

            LaunchedEffect(currentLang) {
                LocaleHelper.applyLocaleToActivity(this@MainActivity, currentLang)
            }

            val useDynamicColor by app.preferences.useDynamicColorFlow.collectAsState(initial = false)
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                androidx.compose.ui.platform.LocalConfiguration provides localizedContext.resources.configuration,
                androidx.compose.ui.platform.LocalLayoutDirection provides if (isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr,
                androidx.activity.compose.LocalActivityResultRegistryOwner provides registryOwner
            ) {
                androidx.compose.runtime.key(currentLang) {
                    MyApplicationTheme(isArabic = isArabic, useDynamicColor = useDynamicColor) {
                        var isLauncherContentMounted by remember { mutableStateOf(hasShownIntroProcessFlag) }
                        var showStartupSplash by remember { mutableStateOf(!hasShownIntroProcessFlag) }

                        LaunchedEffect(Unit) {
                            if (!isLauncherContentMounted) {
                                androidx.compose.runtime.withFrameNanos { }
                                isLauncherContentMounted = true
                            }
                            if (showStartupSplash) {
                                delay(500L)
                                hasShownIntroProcessFlag = true
                                showStartupSplash = false
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = androidx.compose.ui.graphics.Color.Transparent
                        ) {
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                                if (isLauncherContentMounted) {
                                    var didSetBlackWindowBg by remember { mutableStateOf(false) }
                                    LauncherNavigation(
                                        app = app,
                                        onHomePressed = onHomePressed,
                                        voiceSearchRequestCount = voiceSearchRequestCount,
                                        onUserActivity = onUserActivity,
                                        onHomeViewModelCreated = { vm ->
                                            homeVmRef = vm
                                        },
                                        onUpdateScreenAwakePolicy = { keepAwake ->
                                            updateScreenAwakePolicy(keepAwake)
                                        },
                                        onResetWindowBackgroundBlack = {
                                            if (!didSetBlackWindowBg) {
                                                didSetBlackWindowBg = true
                                                try {
                                                    window.setBackgroundDrawable(
                                                        android.graphics.Color.BLACK.toDrawable()
                                                    )
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        appLanguage = effectiveLang
                                    )
                                }

                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showStartupSplash,
                                    enter = androidx.compose.animation.EnterTransition.None,
                                    exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(320)),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    StartupBlueSplashScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activeInstanceRef = java.lang.ref.WeakReference(this)
        isForegroundResumed = true
        updateScreenAwakePolicy(shouldKeepScreenOnFlag)
    }

    override fun onPause() {
        isForegroundResumed = false
        super.onPause()
    }

    override fun onDestroy() {
        isForegroundResumed = false
        if (activeInstanceRef?.get() === this) {
            activeInstanceRef = null
        }
        if (isFinishing && !isChangingConfigurations) {
            (application as? AuraTvApp)?.releaseWebViewKeepAliveAnchor()
        }
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars(force = false)
            updateScreenAwakePolicy(shouldKeepScreenOnFlag)
        }
    }

    private fun hideSystemBars(force: Boolean = false) {
        try {
            if (!force) {
                val rootInsets = androidx.core.view.ViewCompat.getRootWindowInsets(window.decorView)
                if (rootInsets != null && !rootInsets.isVisible(WindowInsetsCompat.Type.systemBars())) {
                    return
                }
            }
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } catch (_: Exception) {}
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (isVoiceOrSearchIntent(intent)) {
            requestVoiceSearch()
        } else {
            // When user presses remote Home button and launcher is re-invoked, return to Home screen
            _onHomePressed.tryEmit(Unit)
        }
    }

    override fun onSearchRequested(): Boolean {
        requestVoiceSearch()
        return true
    }

    override fun onSearchRequested(searchEvent: android.view.SearchEvent?): Boolean {
        requestVoiceSearch()
        return true
    }

    /**
     * Wraps Window.Callback so TV Remote D-pad reorder keys and Voice Search / Assist buttons
     * are intercepted before Compose focus traversal without overriding ComponentActivity.dispatchKeyEvent
     * (@RestrictTo) or intercepting KEYCODE_BACK (handled by OnBackPressedDispatcher / BackHandler).
     */
    private fun installKeyInterceptionCallback() {
        val baseCallback = window.callback ?: return
        window.callback = object : android.view.Window.Callback by baseCallback {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (handleInterceptedKeyEvent(event)) {
                    return true
                }
                return baseCallback.dispatchKeyEvent(event)
            }
        }
    }

    private fun handleInterceptedKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            _onUserActivity.tryEmit(System.currentTimeMillis())
        }

        if (event.keyCode == KeyEvent.KEYCODE_VOICE_ASSIST ||
            event.keyCode == KeyEvent.KEYCODE_SEARCH ||
            event.keyCode == KeyEvent.KEYCODE_ASSIST
        ) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (event.repeatCount == 0) {
                        sawVoiceKeyDown = true
                        requestVoiceSearch()
                    }
                }
                KeyEvent.ACTION_UP -> {
                    val hadDown = sawVoiceKeyDown
                    sawVoiceKeyDown = false
                    if (!hadDown && !event.isCanceled) {
                        requestVoiceSearch()
                    }
                }
            }
            return true
        }

        val vm = homeVmRef ?: return false
        val appState = vm.reorderState.value
        val channelState = vm.channelReorderState.value

        if (appState != null || channelState != null) {
            val keyCode = event.keyCode
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                keyCode == KeyEvent.KEYCODE_ENTER ||
                keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
            ) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val isRtl = isRtlLayout && vm.isRtl.value
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (isRtl) {
                                if (appState != null) vm.moveAppRight() else vm.moveChannelRight()
                            } else {
                                if (appState != null) vm.moveAppLeft() else vm.moveChannelLeft()
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (isRtl) {
                                if (appState != null) vm.moveAppLeft() else vm.moveChannelLeft()
                            } else {
                                if (appState != null) vm.moveAppRight() else vm.moveChannelRight()
                            }
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (appState != null) vm.moveAppUp()
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (appState != null) vm.moveAppDown()
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            if (appState != null) vm.confirmReorder() else vm.confirmChannelReorder()
                        }
                    }
                }
                return true
            }
        }
        return false
    }
}

@Composable
fun LauncherNavigation(
    app: AuraTvApp,
    onHomePressed: SharedFlow<Unit>,
    voiceSearchRequestCount: StateFlow<Int>,
    onUserActivity: SharedFlow<Long>,
    onHomeViewModelCreated: (HomeViewModel) -> Unit,
    onUpdateScreenAwakePolicy: (Boolean) -> Unit = {},
    onResetWindowBackgroundBlack: () -> Unit = {},
    appLanguage: String = "en"
) {
    var currentScreen by remember { mutableStateOf(LauncherScreen.HOME) }
    var settingsInitialSection by remember { mutableStateOf(com.example.ui.settings.SettingsSection.SYSTEM) }
    var settingsReturnScreen by remember { mutableStateOf(LauncherScreen.HOME) }
    var selectedChannelIdToPlay by remember { mutableStateOf<Long?>(null) }
    var voiceSearchTriggerTick by remember { mutableStateOf(0) }
    var consumedVoiceSearchTick by remember { mutableStateOf(0) }
    var pendingUpdateInfo by remember { mutableStateOf<com.example.data.update.AppUpdateInfo?>(null) }
    var dismissedUpdateVersion by remember { mutableStateOf<Int?>(null) }
    var activeBookmarkTitle by remember { mutableStateOf("") }
    var activeBookmarkUrl by remember { mutableStateOf("") }

    // Repositories & ViewModels
    val homeViewModel = remember {
        HomeViewModel(
            appRepository = app.appRepository,
            iptvRepository = app.iptvRepository,
            preferences = app.preferences
        )
    }

    val iptvViewModel = remember {
        IptvViewModel(
            iptvRepository = app.iptvRepository,
            preferences = app.preferences
        )
    }

    val youtubeViewModel = remember {
        YouTubeViewModel(
            preferences = app.preferences
        )
    }

    val settingsViewModel = remember {
        SettingsViewModel(
            appRepository = app.appRepository,
            iptvRepository = app.iptvRepository,
            preferences = app.preferences
        )
    }

    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager
    }

    val currentPlayingChannel by iptvViewModel.currentPlayingChannel.collectAsState()
    val miniPlayerChannel by iptvViewModel.miniPlayerChannel.collectAsState()
    val isYouTubeVideoPlaying by youtubeViewModel.isVideoPlaying.collectAsState()
    val nightComfortMode by app.preferences.nightComfortModeFlow.collectAsState(initial = "off")
    val screensaverTimeout by homeViewModel.screensaverTimeout.collectAsState()
    val screensaverMode by homeViewModel.screensaverMode.collectAsState()
    val screensaverWallpaper by homeViewModel.screensaverWallpaper.collectAsState()
    val screensaverVideo by homeViewModel.screensaverVideo.collectAsState()
    val isClock24h by homeViewModel.isClock24h.collectAsState()
    val auroraColors by homeViewModel.auroraColors.collectAsState()
    val wallpaperMode by homeViewModel.wallpaperMode.collectAsState()
    val customWallpaperUrl by homeViewModel.customWallpaperUrl.collectAsState()

    val isPlaybackActive = (currentScreen == LauncherScreen.YOUTUBE) ||
            isYouTubeVideoPlaying ||
            (currentPlayingChannel != null) ||
            (miniPlayerChannel != null)

    var lastGlobalInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var showGlobalAmbientMode by remember { mutableStateOf(false) }

    LaunchedEffect(onUserActivity) {
        onUserActivity.collect { ts ->
            lastGlobalInteractionTime = ts
        }
    }

    LaunchedEffect(isPlaybackActive) {
        if (isPlaybackActive) {
            onResetWindowBackgroundBlack()
        }
    }

    LaunchedEffect(currentScreen, isPlaybackActive) {
        lastGlobalInteractionTime = System.currentTimeMillis()
        if (isPlaybackActive && showGlobalAmbientMode) {
            showGlobalAmbientMode = false
        }
    }

    val timeoutMinutes = remember(screensaverTimeout) {
        if (screensaverTimeout == "never") 0 else (screensaverTimeout.toIntOrNull() ?: 5)
    }

    // If internal screensaver is ENABLED (timeoutMinutes > 0), keep window awake at all times so external/system screensavers NEVER run.
    // If internal screensaver is DISABLED ("never" / timeoutMinutes == 0), allow external/system screensavers to run (unless video playback is active).
    val keepWindowAwakeForScreensaver = (screensaverTimeout != "never" && timeoutMinutes > 0) || isPlaybackActive
    LaunchedEffect(keepWindowAwakeForScreensaver) {
        onUpdateScreenAwakePolicy(keepWindowAwakeForScreensaver)
    }

    // Global inactivity timer for non-Home screens (paused whenever YouTube is running, IPTV is playing, or any media/video is active)
    LaunchedEffect(screensaverTimeout, timeoutMinutes, currentScreen, isPlaybackActive, showGlobalAmbientMode, lastGlobalInteractionTime) {
        if (screensaverTimeout == "never" || timeoutMinutes <= 0) {
            showGlobalAmbientMode = false
        } else if (currentScreen != LauncherScreen.HOME && !showGlobalAmbientMode) {
            while (true) {
                delay(4000)
                val anyMediaPlaying = isPlaybackActive || (audioManager?.isMusicActive == true)
                if (anyMediaPlaying) {
                    lastGlobalInteractionTime = System.currentTimeMillis()
                } else {
                    val elapsed = System.currentTimeMillis() - lastGlobalInteractionTime
                    val limitMs = timeoutMinutes * 60 * 1000L
                    if (elapsed >= limitMs && !showGlobalAmbientMode) {
                        showGlobalAmbientMode = true
                    }
                }
            }
        } else if (currentScreen == LauncherScreen.HOME || isPlaybackActive) {
            showGlobalAmbientMode = false
        }
    }

    LaunchedEffect(homeViewModel) {
        onHomeViewModelCreated(homeViewModel)
    }

    // Start DNS Blocker Service if enabled and permission is granted
    LaunchedEffect(Unit) {
        val mode = app.preferences.dnsBlockerModeFlow.first()
        if (mode != "off") {
            val vpnIntent = VpnService.prepare(app)
            if (vpnIntent == null) {
                try {
                    val intent = Intent(app, com.example.service.DnsBlockerService::class.java).apply {
                        putExtra(com.example.service.DnsBlockerService.EXTRA_MODE, mode)
                    }
                    androidx.core.content.ContextCompat.startForegroundService(app, intent)
                } catch (_: Exception) {}
            }
        }
    }

    // Auto-check GitHub (OsamaSalsa/Aura-TV-Launcher) for newer versions outside Google Play
    LaunchedEffect(Unit) {
        delay(2500)
        val shouldAutoCheck = try {
            app.preferences.autoCheckUpdatesFlow.first()
        } catch (_: Exception) {
            true
        }
        if (shouldAutoCheck) {
            when (val res = com.example.data.update.AppUpdateManager.checkForUpdates()) {
                is com.example.data.update.UpdateCheckResult.UpdateAvailable -> {
                    if (res.info.mandatory || dismissedUpdateVersion != res.info.versionCode) {
                        pendingUpdateInfo = res.info
                    }
                }
                else -> {}
            }
        }
    }

    // Remote Home button press handler: returns back to Launcher Home instantly
    LaunchedEffect(Unit) {
        onHomePressed.collect {
            iptvViewModel.closePlayer()
            youtubeViewModel.setVideoPlaying(false)
            currentScreen = LauncherScreen.HOME
            selectedChannelIdToPlay = null
        }
    }

    // Remote Voice / Google Assistant button handler: dismisses screensaver/players, returns to Home, and opens Voice Search
    val latestVoiceRequest by voiceSearchRequestCount.collectAsState()
    var lastHandledVoiceRequest by remember { mutableStateOf(0) }
    LaunchedEffect(latestVoiceRequest) {
        if (latestVoiceRequest > lastHandledVoiceRequest) {
            lastHandledVoiceRequest = latestVoiceRequest
            showGlobalAmbientMode = false
            lastGlobalInteractionTime = System.currentTimeMillis()
            iptvViewModel.closePlayer()
            youtubeViewModel.setVideoPlaying(false)
            currentScreen = LauncherScreen.HOME
            selectedChannelIdToPlay = null
            voiceSearchTriggerTick++
        }
    }

    // Critical Requirement: Back button must NEVER exit launcher from Home screen to OS
    BackHandler(enabled = true) {
        if (currentScreen != LauncherScreen.HOME) {
            iptvViewModel.closePlayer()
            youtubeViewModel.setVideoPlaying(false)
            currentScreen = LauncherScreen.HOME
            selectedChannelIdToPlay = null
        }
        // When currentScreen == HOME, BackHandler is active and consumes the back press so launcher NEVER exits to OS!
    }

    // When a favorite channel is clicked from Home, open IPTV in Favorites category and auto-play
    LaunchedEffect(selectedChannelIdToPlay, currentScreen) {
        val channelId = selectedChannelIdToPlay
        if (currentScreen == LauncherScreen.IPTV && channelId != null) {
            iptvViewModel.selectCategory("Favorites")
            delay(100)
            val channels = iptvViewModel.channels.value
            val target = channels.firstOrNull { it.id == channelId }
            if (target != null) {
                iptvViewModel.playChannel(target)
            } else {
                val favChannels = app.iptvRepository.favoriteChannelsFlow.first()
                val favTarget = favChannels.firstOrNull { it.id == channelId }
                if (favTarget != null) {
                    iptvViewModel.playChannel(favTarget)
                }
            }
        }
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val exit = when (initialState) {
                    LauncherScreen.IPTV,
                    LauncherScreen.YOUTUBE,
                    LauncherScreen.CRAZYGAMES,
                    LauncherScreen.POKI,
                    LauncherScreen.PLAYHOP,
                    LauncherScreen.EASYGAMES,
                    LauncherScreen.AURA_STORE,
                    LauncherScreen.MINI_DOWNLOADER,
                    LauncherScreen.CUSTOM_WEB_PORTAL -> androidx.compose.animation.ExitTransition.None
                    else -> fadeOut()
                }
                fadeIn() togetherWith exit
            },
            label = "launcher_screen_transition"
        ) { screen ->
            when (screen) {
                LauncherScreen.HOME -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        voiceSearchTriggerTick = voiceSearchTriggerTick,
                        consumedVoiceSearchTick = consumedVoiceSearchTick,
                        onVoiceSearchConsumed = {
                            consumedVoiceSearchTick = voiceSearchTriggerTick
                        },
                        userActivityFlow = onUserActivity,
                        isPlaybackActive = isPlaybackActive,
                        onOpenIptv = { channelId ->
                            selectedChannelIdToPlay = channelId
                            currentScreen = LauncherScreen.IPTV
                        },
                        onOpenYouTube = {
                            currentScreen = LauncherScreen.YOUTUBE
                        },
                        onOpenCrazyGames = {
                            currentScreen = LauncherScreen.CRAZYGAMES
                        },
                        onOpenPoki = {
                            currentScreen = LauncherScreen.POKI
                        },
                        onOpenPlayhop = {
                            currentScreen = LauncherScreen.PLAYHOP
                        },
                        onOpenEasyGames = {
                            currentScreen = LauncherScreen.EASYGAMES
                        },
                        onOpenAuraStore = {
                            currentScreen = LauncherScreen.AURA_STORE
                        },
                        onOpenMiniDownloader = {
                            currentScreen = LauncherScreen.MINI_DOWNLOADER
                        },
                        onOpenWebBookmark = { title, url ->
                            activeBookmarkTitle = title
                            activeBookmarkUrl = url
                            currentScreen = LauncherScreen.CUSTOM_WEB_PORTAL
                        },
                        onOpenSettings = {
                            settingsInitialSection = com.example.ui.settings.SettingsSection.SYSTEM
                            settingsReturnScreen = LauncherScreen.HOME
                            currentScreen = LauncherScreen.SETTINGS
                        }
                    )
                }

                LauncherScreen.IPTV -> {
                    IptvScreen(
                        viewModel = iptvViewModel,
                        onOpenSettings = {
                            settingsInitialSection = com.example.ui.settings.SettingsSection.IPTV
                            settingsReturnScreen = LauncherScreen.IPTV
                            currentScreen = LauncherScreen.SETTINGS
                        },
                        onBackToHome = {
                            iptvViewModel.closePlayer()
                            currentScreen = LauncherScreen.HOME
                            selectedChannelIdToPlay = null
                        }
                    )
                }

                LauncherScreen.YOUTUBE -> {
                    YouTubeScreen(
                        viewModel = youtubeViewModel,
                        onOpenSettings = {
                            youtubeViewModel.setVideoPlaying(false)
                            currentScreen = LauncherScreen.SETTINGS
                        },
                        onBackToHome = {
                            youtubeViewModel.setVideoPlaying(false)
                            currentScreen = LauncherScreen.HOME
                        }
                    )
                }

                LauncherScreen.CRAZYGAMES -> {
                    CrazyGamesScreen(
                        onBackToHome = {
                            currentScreen = LauncherScreen.HOME
                        }
                    )
                }

                LauncherScreen.POKI -> {
                    PokiScreen(
                        onBackToHome = {
                            currentScreen = LauncherScreen.HOME
                        }
                    )
                }

                LauncherScreen.PLAYHOP -> {
                    PlayhopScreen(
                        onBackToHome = {
                            currentScreen = LauncherScreen.HOME
                        }
                    )
                }

                LauncherScreen.EASYGAMES -> {
                    EasyGamesScreen(
                        onBackToHome = {
                            currentScreen = LauncherScreen.HOME
                        }
                    )
                }

                LauncherScreen.AURA_STORE -> {
                    AuraStoreScreen(
                        onBackToHome = {
                            currentScreen = LauncherScreen.HOME
                        },
                        appLanguage = appLanguage
                    )
                }

                LauncherScreen.MINI_DOWNLOADER -> {
                    MiniDownloaderScreen(
                        onBackToHome = {
                            currentScreen = LauncherScreen.HOME
                        }
                    )
                }

                LauncherScreen.CUSTOM_WEB_PORTAL -> {
                    CustomWebPortalScreen(
                        title = activeBookmarkTitle,
                        url = activeBookmarkUrl,
                        onBackToHome = {
                            currentScreen = LauncherScreen.HOME
                        }
                    )
                }

                LauncherScreen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        initialSection = settingsInitialSection,
                        onUpdateAvailable = { updateInfo ->
                            pendingUpdateInfo = updateInfo
                        },
                        onBackToHome = {
                            currentScreen = settingsReturnScreen
                        }
                    )
                }
            }
        }

        val activeMiniChannel = miniPlayerChannel
        if (activeMiniChannel != null && currentScreen != LauncherScreen.IPTV) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                com.example.ui.iptv.IptvMiniPlayerOverlay(
                    channel = activeMiniChannel,
                    onExpand = {
                        iptvViewModel.expandMiniPlayer()
                        currentScreen = LauncherScreen.IPTV
                    },
                    onNextChannel = {
                        iptvViewModel.nextMiniPlayerChannel()
                    },
                    onClose = {
                        iptvViewModel.closeMiniPlayer()
                    }
                )
            }
        }

        pendingUpdateInfo?.let { updateInfo ->
            com.example.ui.common.AppUpdateDialog(
                updateInfo = updateInfo,
                onDismiss = {
                    dismissedUpdateVersion = updateInfo.versionCode
                    pendingUpdateInfo = null
                }
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showGlobalAmbientMode && !isPlaybackActive,
            enter = fadeIn(),
            exit = if (screensaverMode == "video_stream") androidx.compose.animation.ExitTransition.None else fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            com.example.ui.home.AmbientScreensaverOverlay(
                is24hFormat = isClock24h,
                auroraColors = auroraColors,
                screensaverMode = screensaverMode,
                screensaverVideoId = screensaverVideo,
                wallpaperMode = if (screensaverWallpaper == "current") wallpaperMode else "custom",
                customWallpaperUrl = if (screensaverWallpaper == "current") customWallpaperUrl else screensaverWallpaper,
                onDismiss = {
                    lastGlobalInteractionTime = System.currentTimeMillis()
                    showGlobalAmbientMode = false
                }
            )
        }

        // Night Eye-Comfort Warm Dim Filter ("always" or "auto" 22:00 - 06:00)
        var currentHour by remember {
            mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY))
        }
        LaunchedEffect(nightComfortMode) {
            if (nightComfortMode == "auto") {
                while (true) {
                    currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    delay(60_000L)
                }
            }
        }
        val isNightDimActive = when (nightComfortMode) {
            "always" -> true
            "auto" -> currentHour >= 22 || currentHour < 6
            else -> false
        }
        if (isNightDimActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x381A0D00))
            )
        }
    }
}

@Composable
private fun StartupBlueSplashScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2B6BE6),
                        Color(0xFF1F54C7),
                        Color(0xFF163E99)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = "Aura TV",
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1
            )
        }
    }
}

@Preview(widthDp = 960, heightDp = 540, showBackground = true)
@Composable
private fun StartupBlueSplashScreenPreview() {
    StartupBlueSplashScreen()
}
