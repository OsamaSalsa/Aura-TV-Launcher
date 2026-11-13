package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.example.ui.downloader.MiniDownloaderScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.youtube.YouTubeScreen
import com.example.ui.youtube.YouTubeViewModel

import android.content.Intent
import android.net.VpnService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.example.util.LocaleHelper

enum class LauncherScreen {
    HOME, IPTV, YOUTUBE, SETTINGS, CRAZYGAMES, POKI, PLAYHOP, EASYGAMES, MINI_DOWNLOADER, AURA_STORE
}

private var hasShownIntroProcessFlag = false

class MainActivity : ComponentActivity() {

    private var homeVmRef: HomeViewModel? = null
    private var isRtlLayout: Boolean = false
    private var shouldKeepScreenOnFlag: Boolean = true
    private val _onHomePressed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onHomePressed: SharedFlow<Unit> = _onHomePressed.asSharedFlow()
    private val _onVoiceSearchPressed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onVoiceSearchPressed: SharedFlow<Unit> = _onVoiceSearchPressed.asSharedFlow()
    private val _onUserActivity = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val onUserActivity: SharedFlow<Long> = _onUserActivity.asSharedFlow()

    /**
     * Controls whether Aura TV keeps the system screen awake to block external/system screensavers:
     * - When internal screensaver is ENABLED (or media is playing): keepAwake = true -> sets FLAG_KEEP_SCREEN_ON
     *   and decorView.keepScreenOn = true so the app acts as continuously active and external screensavers never trigger.
     * - When internal screensaver is DISABLED ("never") and no media is playing: keepAwake = false -> clears
     *   FLAG_KEEP_SCREEN_ON and decorView.keepScreenOn = false so external/system screensavers have full permission to run.
     */
    fun updateScreenAwakePolicy(keepAwake: Boolean) {
        shouldKeepScreenOnFlag = keepAwake
        runOnUiThread {
            try {
                if (keepAwake) {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    window.decorView.keepScreenOn = true
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    window.decorView.keepScreenOn = false
                }
            } catch (_: Exception) {}
        }
    }

    private fun isVoiceOrSearchIntent(intent: Intent?): Boolean {
        val action = intent?.action ?: return false
        return action == Intent.ACTION_ASSIST ||
                action == Intent.ACTION_VOICE_COMMAND ||
                action == Intent.ACTION_SEARCH ||
                action == Intent.ACTION_SEARCH_LONG_PRESS ||
                action == "android.speech.action.WEB_SEARCH"
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        _onUserActivity.tryEmit(System.currentTimeMillis())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AuraTvApp

        // Use 32-bit RGBA_8888 color depth and hardware dithering to eliminate gradient color banding on TV panels
        try {
            window.setFormat(android.graphics.PixelFormat.RGBA_8888)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DITHER)
        } catch (_: Exception) {}

        // Read initial screensaver preference immediately so window flags match before first frame
        updateScreenAwakePolicy(true)

        // Hide system bars (Status Bar, Navigation Bar) for full screen TV experience
        hideSystemBars()

        if (isVoiceOrSearchIntent(intent)) {
            _onVoiceSearchPressed.tryEmit(Unit)
        }

        setContent {
            val registryOwner = requireNotNull(androidx.activity.compose.LocalActivityResultRegistryOwner.current) {
                "No ActivityResultRegistryOwner was provided"
            }
            val currentLang by app.preferences.languageFlow.collectAsState(initial = "system")
            val baseContext = LocalContext.current
            val localizedContext = remember(currentLang, baseContext) {
                LocaleHelper.setLocale(baseContext, currentLang)
            }

            val effectiveLang = remember(currentLang) {
                LocaleHelper.getEffectiveLanguage(currentLang)
            }
            val isRtl = currentLang == "ar_rtl" || (currentLang == "system" && effectiveLang == "ar")
            val isArabic = effectiveLang == "ar"
            isRtlLayout = isRtl

            LaunchedEffect(currentLang) {
                LocaleHelper.applyLocaleToActivity(this@MainActivity, currentLang)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                androidx.compose.ui.platform.LocalConfiguration provides localizedContext.resources.configuration,
                androidx.compose.ui.platform.LocalLayoutDirection provides if (isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr,
                androidx.activity.compose.LocalActivityResultRegistryOwner provides registryOwner
            ) {
                androidx.compose.runtime.key(currentLang) {
                    MyApplicationTheme(isArabic = isArabic) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = TvMidnightBackground
                        ) {
                            LauncherNavigation(
                                app = app,
                                onHomePressed = onHomePressed,
                                onVoiceSearchPressed = onVoiceSearchPressed,
                                onUserActivity = onUserActivity,
                                onHomeViewModelCreated = { vm ->
                                    homeVmRef = vm
                                },
                                onUpdateScreenAwakePolicy = { keepAwake ->
                                    updateScreenAwakePolicy(keepAwake)
                                },
                                appLanguage = effectiveLang
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateScreenAwakePolicy(shouldKeepScreenOnFlag)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
            updateScreenAwakePolicy(shouldKeepScreenOnFlag)
        }
    }

    private fun hideSystemBars() {
        try {
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } catch (_: Exception) {}
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (isVoiceOrSearchIntent(intent)) {
            _onVoiceSearchPressed.tryEmit(Unit)
        } else {
            // When user presses remote Home button and launcher is re-invoked, return to Home screen
            _onHomePressed.tryEmit(Unit)
        }
    }

    override fun onSearchRequested(): Boolean {
        _onVoiceSearchPressed.tryEmit(Unit)
        return true
    }

    /**
     * TV Remote D-pad key dispatch:
     * Dispatches key events BEFORE Compose focus navigation when reordering apps,
     * and intercepts remote Voice Search / Search / Assist buttons to open in-app Voice Search.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            _onUserActivity.tryEmit(System.currentTimeMillis())
        }

        if (event.keyCode == KeyEvent.KEYCODE_VOICE_ASSIST ||
            event.keyCode == KeyEvent.KEYCODE_SEARCH ||
            event.keyCode == KeyEvent.KEYCODE_ASSIST
        ) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                _onVoiceSearchPressed.tryEmit(Unit)
            }
            return true
        }

        val vm = homeVmRef ?: return super.dispatchKeyEvent(event)
        
        val appState = vm.reorderState.value
        val channelState = vm.channelReorderState.value
        
        if (appState != null || channelState != null) {
            val keyCode = event.keyCode
            if (keyCode in listOf(
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    KeyEvent.KEYCODE_DPAD_RIGHT,
                    KeyEvent.KEYCODE_DPAD_UP,
                    KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_CENTER,
                    KeyEvent.KEYCODE_ENTER,
                    KeyEvent.KEYCODE_BACK
                )
            ) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val isRtl = isRtlLayout || vm.isRtl.value ||
                            resources.configuration.layoutDirection == android.util.LayoutDirection.RTL
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
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (appState != null) vm.confirmReorder() else vm.confirmChannelReorder()
                        }
                        KeyEvent.KEYCODE_BACK -> {
                            if (appState != null) vm.confirmReorder() else vm.confirmChannelReorder()
                        }
                    }
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}

@Composable
fun LauncherNavigation(
    app: AuraTvApp,
    onHomePressed: SharedFlow<Unit>,
    onVoiceSearchPressed: SharedFlow<Unit>,
    onUserActivity: SharedFlow<Long>,
    onHomeViewModelCreated: (HomeViewModel) -> Unit,
    onUpdateScreenAwakePolicy: (Boolean) -> Unit = {},
    appLanguage: String = "en"
) {
    var currentScreen by remember { mutableStateOf(LauncherScreen.HOME) }
    var selectedChannelIdToPlay by remember { mutableStateOf<Long?>(null) }
    var voiceSearchTriggerTick by remember { mutableStateOf(0) }

    // Repositories & ViewModels
    val homeViewModel = remember {
        HomeViewModel(
            appRepository = app.appRepository,
            iptvRepository = app.iptvRepository,
            preferences = app.preferences
        )
    }

    val iptvViewModel = remember {
        IptvViewModel(iptvRepository = app.iptvRepository)
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
    val isYouTubeVideoPlaying by youtubeViewModel.isVideoPlaying.collectAsState()
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
            (currentPlayingChannel != null)

    var lastGlobalInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var showGlobalAmbientMode by remember { mutableStateOf(false) }

    LaunchedEffect(onUserActivity) {
        onUserActivity.collect { ts ->
            lastGlobalInteractionTime = ts
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
                    app.startService(intent)
                } catch (_: Exception) {}
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

    // Remote Voice Search button press handler: opens Universal Search in Voice Listening mode
    LaunchedEffect(Unit) {
        onVoiceSearchPressed.collect {
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
                    LauncherScreen.YOUTUBE,
                    LauncherScreen.CRAZYGAMES,
                    LauncherScreen.POKI,
                    LauncherScreen.PLAYHOP,
                    LauncherScreen.EASYGAMES,
                    LauncherScreen.AURA_STORE,
                    LauncherScreen.MINI_DOWNLOADER -> androidx.compose.animation.ExitTransition.None
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
                        onOpenSettings = {
                            currentScreen = LauncherScreen.SETTINGS
                        }
                    )
                }

                LauncherScreen.IPTV -> {
                    IptvScreen(
                        viewModel = iptvViewModel,
                        onOpenSettings = {
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

                LauncherScreen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBackToHome = {
                            currentScreen = LauncherScreen.HOME
                        }
                    )
                }
            }
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
    }
}
