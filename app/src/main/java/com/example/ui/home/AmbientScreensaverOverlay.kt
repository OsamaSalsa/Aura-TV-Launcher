package com.example.ui.home

import android.view.LayoutInflater
import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.annotation.OptIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.LauncherPreferences
import com.example.ui.common.AuroraBackground
import com.example.ui.common.AuroraColors
import com.example.ui.common.BindWebViewLifecycle
import com.example.ui.common.findActivityContext
import com.example.ui.common.grantSafeWebPermissions
import com.example.ui.common.hardenWebView
import com.example.ui.common.injectWebViewStabilityShim
import com.example.ui.common.isWebViewDestroyed
import com.example.ui.common.postOnMain
import com.example.ui.common.safeDestroyWebView
import com.example.ui.settings.onlineWallpaperCategories
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.util.GreetingHelper
import com.example.util.LocaleHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class ScreensaverPresetVideo(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val defaultTitle: String,
    val defaultSubtitle: String
)

val screensaverPresetVideos = listOf(
    ScreensaverPresetVideo(
        id = "EG_g7BFaL5s",
        titleRes = R.string.screensaver_video_rain_fireplace,
        subtitleRes = R.string.screensaver_video_rain_fireplace_sub,
        defaultTitle = "Cozy Fireplace Ambience 4K",
        defaultSubtitle = "8 hours of relaxing fireplace to sleep & unwind in peace"
    ),
    ScreensaverPresetVideo(
        id = "TtLeec_DR-Y",
        titleRes = R.string.screensaver_video_winter_cabin,
        subtitleRes = R.string.screensaver_video_winter_cabin_sub,
        defaultTitle = "Modern Oriental Café Music",
        defaultSubtitle = "Relaxing instrumentals for work, study & focus"
    ),
    ScreensaverPresetVideo(
        id = "8xXHeRnLwNQ",
        titleRes = R.string.screensaver_video_nature_waterfalls,
        subtitleRes = R.string.screensaver_video_nature_waterfalls_sub,
        defaultTitle = "Fireplace & Crackling Fire Sounds",
        defaultSubtitle = "8 hours relaxing fireplace 4K with natural fire sounds (No Music)"
    )
)

fun extractYouTubeVideoId(input: String): String {
    val clean = input.trim()
    return when {
        clean.contains("youtu.be/") -> clean.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
        clean.contains("watch?v=") -> clean.substringAfter("watch?v=").substringBefore("&")
        clean.contains("embed/") -> clean.substringAfter("embed/").substringBefore("?").substringBefore("&")
        clean.contains("live/") -> clean.substringAfter("live/").substringBefore("?").substringBefore("&")
        clean.length in 8..15 && !clean.contains("/") -> clean
        else -> if (clean.isNotEmpty()) clean else "EG_g7BFaL5s"
    }
}

const val SCREENSAVER_YOUTUBE_VIDEO_ID = "EG_g7BFaL5s"

@Composable
fun AmbientScreensaverOverlay(
    is24hFormat: Boolean,
    auroraColors: AuroraColors,
    screensaverMode: String = "clock_date", // "clock_date", "wallpaper_info", "video_stream"
    screensaverVideoId: String = "EG_g7BFaL5s",
    wallpaperMode: String = "dynamic",
    customWallpaperUrl: String = "",
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val hostView = LocalView.current

    DisposableEffect(hostView) {
        val prevKeepScreenOn = hostView.keepScreenOn
        hostView.keepScreenOn = true
        onDispose {
            hostView.keepScreenOn = prevKeepScreenOn
        }
    }

    BackHandler {
        onDismiss()
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    when (screensaverMode) {
        "video_stream" -> {
            // Mode 3: Clean Fullscreen Video with ZERO text or buttons on screen
            VideoScreensaver(
                rawVideoInput = screensaverVideoId,
                focusRequester = focusRequester,
                onDismiss = onDismiss
            )
        }
        "slideshow" -> {
            // Mode 4: Photo Slideshow with Ken Burns motion, transitions, background music & particle effects
            SlideshowScreensaver(
                is24hFormat = is24hFormat,
                focusRequester = focusRequester,
                onDismiss = onDismiss
            )
        }
        "wallpaper_info" -> {
            // Mode 2: App Wallpaper with clock and subtle info at bottom-right
            WallpaperInfoScreensaver(
                is24hFormat = is24hFormat,
                auroraColors = auroraColors,
                wallpaperMode = wallpaperMode,
                customWallpaperUrl = customWallpaperUrl,
                focusRequester = focusRequester,
                onDismiss = onDismiss
            )
        }
        else -> {
            // Mode 1 (Default): Clock and Date centered with ambient glow
            ClockDateScreensaver(
                is24hFormat = is24hFormat,
                auroraColors = auroraColors,
                focusRequester = focusRequester,
                onDismiss = onDismiss
            )
        }
    }
}

private const val STREAM_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

/**
 * Mode 3: Fullscreen Cinematic Video with ZERO text or buttons on screen.
 * Plays the exact YouTube videos requested by the user using YouTubePlayerView
 * with package origin and third-party cookies enabled to bypass Error 152-4.
 */
@OptIn(UnstableApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun VideoScreensaver(
    rawVideoInput: String,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit
) {
    val cleanInput = rawVideoInput.trim()
    val isDirectStreamUrl = (cleanInput.startsWith("http://") || cleanInput.startsWith("https://")) &&
            !cleanInput.contains("youtube.com") && !cleanInput.contains("youtu.be")
    val videoId = remember(cleanInput) { extractYouTubeVideoId(cleanInput) }
    val hostView = LocalView.current

    DisposableEffect(hostView) {
        val prevKeepScreenOn = hostView.keepScreenOn
        hostView.keepScreenOn = true
        onDispose {
            hostView.keepScreenOn = prevKeepScreenOn
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        if (isDirectStreamUrl) {
            DirectStreamExoPlayerScreensaver(
                streamUrl = cleanInput,
                onStreamError = {}
            )
        } else {
            YouTubeWebScreensaver(videoId = videoId)
        }

        // Top-level invisible interaction catcher so any remote key or click dismisses immediately
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        onDismiss()
                        true
                    } else false
                }
        )
    }
}

/**
 * Plays YouTube video in pure full screen with auto-play, seamless loop, and zero controls or text
 * using YouTubePlayerView with package origin and third-party cookies to prevent Error 152-4.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubeWebScreensaver(
    videoId: String
) {
    var isVideoReady by remember(videoId) { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var crashCount by remember(videoId) { mutableStateOf(0) }

    LaunchedEffect(videoId) {
        isVideoReady = false
        delay(3800)
        isVideoReady = true
    }

    val playerUrl = remember(videoId) {
        "https://invidious.tiekoetter.com/watch?v=$videoId&autoplay=1&loop=1&controls=0&listen=0&comments=none&related_videos=false&quality=medium"
    }

    val cleanFullscreenScript = remember {
        """
        (function() {
            function applyCleanVideo() {
                try {
                    var text = (document.body && document.body.innerText) ? document.body.innerText : '';
                    if (text.indexOf('Making sure') !== -1 || text.indexOf('Anubis') !== -1) {
                        document.documentElement.style.background = '#000';
                        if (document.body) {
                            document.body.style.background = '#000';
                            document.body.style.opacity = '0';
                        }
                        return;
                    }
                    if (!document.getElementById('ambient-clean-style') && document.head) {
                        var st = document.createElement('style');
                        st.id = 'ambient-clean-style';
                        st.innerHTML = 'html, body { margin:0 !important; padding:0 !important; background:#000 !important; overflow:hidden !important; width:100vw !important; height:100vh !important; } ' +
                            '.navbar, header, footer, #comments, .watch-card, .pure-u-lg-1-5, .title, .flexible, #description-box, .vjs-control-bar, .vjs-big-play-button, .vjs-modal-dialog, .vjs-EndScreen, .vjs-title-bar, .vjs-DockText, .vjs-poster, .vjs-loading-spinner, .watch-on-invidious { display:none !important; opacity:0 !important; visibility:hidden !important; } ' +
                            '#player-container, #player, .video-js { position:fixed !important; top:0 !important; left:0 !important; width:100vw !important; height:100vh !important; max-width:100vw !important; max-height:100vh !important; background:#000 !important; z-index:99999 !important; border:none !important; outline:none !important; display:block !important; visibility:visible !important; opacity:1 !important; } ' +
                            'video, .vjs-tech { position:fixed !important; top:0 !important; left:0 !important; width:100vw !important; height:100vh !important; max-width:100vw !important; max-height:100vh !important; object-fit:cover !important; background:#000 !important; z-index:100000 !important; border:none !important; outline:none !important; display:block !important; visibility:visible !important; opacity:1 !important; }';
                        document.head.appendChild(st);
                    }
                    if (document.body) {
                        document.body.style.opacity = '1';
                    }
                    var v = document.querySelector('video');
                    if (v) {
                        v.loop = true;
                        v.muted = false;
                        v.volume = 1.0;
                        v.controls = false;
                        if (v.paused) {
                            v.play().catch(function() {
                                v.muted = true;
                                v.play().catch(function(){});
                            });
                        }
                    }
                } catch (e) {}
            }
            applyCleanVideo();
            if (!window.__ambientCleanTimer) {
                window.__ambientCleanTimer = setInterval(applyCleanVideo, 600);
            }
        })();
        """.trimIndent()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (crashCount <= 1) {
            androidx.compose.runtime.key(videoId, crashCount) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx.findActivityContext()).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.BLACK)
                            isVerticalScrollBarEnabled = false
                            isHorizontalScrollBarEnabled = false
                            isFocusable = false
                            isFocusableInTouchMode = false

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                userAgentString = STREAM_USER_AGENT
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    safeBrowsingEnabled = false
                                }
                            }

                            hardenWebView(this)

                            val webViewInstance = this
                            CookieManager.getInstance().apply {
                                setAcceptCookie(true)
                                setAcceptThirdPartyCookies(webViewInstance, true)
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    grantSafeWebPermissions(request)
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: android.graphics.Bitmap?
                                ) {
                                    super.onPageStarted(view, url, favicon)
                                    if (!isWebViewDestroyed(view)) {
                                        injectWebViewStabilityShim(view, disableWebGl = true)
                                        view?.evaluateJavascript(cleanFullscreenScript, null)
                                    }
                                }

                                override fun onPageCommitVisible(view: WebView?, url: String?) {
                                    super.onPageCommitVisible(view, url)
                                    if (!isWebViewDestroyed(view)) {
                                        injectWebViewStabilityShim(view, disableWebGl = true)
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    if (!isWebViewDestroyed(view)) {
                                        injectWebViewStabilityShim(view, disableWebGl = true)
                                        view?.evaluateJavascript(cleanFullscreenScript, null)
                                        if (url != null && url.contains("/watch")) {
                                            isVideoReady = true
                                        }
                                    }
                                }

                                override fun onRenderProcessGone(
                                    view: WebView?,
                                    detail: RenderProcessGoneDetail?
                                ): Boolean {
                                    safeDestroyWebView(view, rendererGone = true)
                                    if (webViewRef === view) webViewRef = null
                                    postOnMain(300L) {
                                        crashCount++
                                    }
                                    return true
                                }
                            }

                            loadUrl(playerUrl)
                            webViewRef = this
                        }
                    },
                    update = { webView ->
                        webViewRef = webView
                        if (!isWebViewDestroyed(webView) && webView.url == null) {
                            webView.loadUrl(playerUrl)
                        }
                    },
                    onRelease = { webView ->
                        if (webViewRef === webView) webViewRef = null
                        safeDestroyWebView(webView)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (!isVideoReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
    }

    BindWebViewLifecycle(webViewRef)

    DisposableEffect(Unit) {
        onDispose {
            safeDestroyWebView(webViewRef)
            webViewRef = null
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun DirectStreamExoPlayerScreensaver(
    streamUrl: String,
    onStreamError: () -> Unit
) {
    val context = LocalContext.current
    var stoppedByLifecycle by remember { mutableStateOf(false) }
    var retryCount by remember(streamUrl) { mutableStateOf(0) }
    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }

    val exoPlayer: ExoPlayer = remember(streamUrl) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(STREAM_USER_AGENT)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(12000)
            .setReadTimeoutMs(12000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(DefaultDataSource.Factory(context, httpDataSourceFactory))
            .setDrmSessionManagerProvider { DrmSessionManager.DRM_UNSUPPORTED }

        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            .setEnableDecoderFallback(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1500,  // minBufferMs
                15000, // maxBufferMs
                400,   // bufferForPlaybackMs (fast start)
                1000   // bufferForPlaybackAfterRebufferMs
            )
            .build()

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaItem(MediaItem.fromUri(streamUrl))
                prepare()
                playWhenReady = true
                volume = 1f
            }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    stoppedByLifecycle = true
                    try {
                        exoPlayer.playWhenReady = false
                        exoPlayer.stop()
                        exoPlayer.clearVideoSurface()
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_START -> {
                    if (stoppedByLifecycle) {
                        stoppedByLifecycle = false
                        try {
                            playerViewRef?.let { pv -> exoPlayer.setVideoTextureView(pv.videoSurfaceView as? android.view.TextureView) }
                        } catch (_: Exception) {}
                        try {
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        } catch (_: Exception) {}
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    retryCount = 0
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                if (!stoppedByLifecycle && retryCount < 2) {
                    retryCount++
                    postOnMain(350L) {
                        if (!stoppedByLifecycle) {
                            try {
                                exoPlayer.stop()
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            } catch (_: Exception) {}
                        }
                    }
                } else {
                    onStreamError()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            playerViewRef?.player = null
            playerViewRef = null
            try { exoPlayer.stop() } catch (_: Exception) {}
            try { exoPlayer.clearVideoSurface() } catch (_: Exception) {}
            try { exoPlayer.release() } catch (_: Exception) {}
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val view = LayoutInflater.from(ctx)
                .inflate(R.layout.layout_iptv_player, null) as PlayerView
            view.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                keepScreenOn = true
                useController = false
                player = exoPlayer
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                setEnableComposeSurfaceSyncWorkaround(true)
                playerViewRef = this
            }
        },
        update = { view ->
            view.keepScreenOn = true
            view.useController = false
            view.player = exoPlayer
            view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            playerViewRef = view
        },
        onRelease = { view ->
            view.player = null
            if (playerViewRef === view) playerViewRef = null
        }
    )
}

/**
 * Mode 2: Random 4K Wallpaper with sleek clock and info placed directly on the image
 * at the bottom-right corner (no black background card).
 * When 12-hour format is chosen, localized AM/PM appears beside the date at the date's font size and level.
 */
@Composable
private fun WallpaperInfoScreensaver(
    is24hFormat: Boolean,
    auroraColors: AuroraColors,
    wallpaperMode: String,
    customWallpaperUrl: String,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { LauncherPreferences(context) }
    val lang by prefs.languageFlow.collectAsState(initial = "system")

    val allPresetWallpapers = remember {
        onlineWallpaperCategories.flatMap { it.wallpapers }
    }

    var randomWallpaperUrl by remember {
        mutableStateOf(
            if (allPresetWallpapers.isNotEmpty()) {
                allPresetWallpapers.random().fullUrl
            } else {
                "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&w=1920&q=85"
            }
        )
    }

    // Automatically rotate to a new random wallpaper every 1 hour (3,600,000 ms) while in screensaver
    LaunchedEffect(Unit) {
        while (true) {
            delay(3_600_000L)
            if (allPresetWallpapers.size > 1) {
                var next = allPresetWallpapers.random().fullUrl
                if (next == randomWallpaperUrl) {
                    next = allPresetWallpapers.random().fullUrl
                }
                randomWallpaperUrl = next
            }
        }
    }

    var timeStr by remember { mutableStateOf("") }
    var secondsStr by remember { mutableStateOf("") }
    var amPmStr by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("") }
    var greetingStr by remember { mutableStateOf("") }

    val effectiveLang = LocaleHelper.getEffectiveLanguage(lang)
    val isArabic = effectiveLang == "ar" || effectiveLang == "ur" || effectiveLang == "fa"
    val dateFont = if (isArabic) CairoFontFamily else FontFamily.Default

    val directTextShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.85f),
            blurRadius = 12f,
            offset = Offset(0f, 3f)
        )
    }

    val greetingMode by prefs.greetingModeFlow.collectAsState(initial = "poetic")
    val customGreetingText by prefs.customGreetingTextFlow.collectAsState(initial = "")
    val dateFormatStyle by prefs.dateFormatStyleFlow.collectAsState(initial = "full")

    LaunchedEffect(is24hFormat, lang, greetingMode, customGreetingText, dateFormatStyle) {
        val pattern = if (is24hFormat) "HH:mm" else "hh:mm"
        val timeFmt = SimpleDateFormat(pattern, Locale.US)
        val secFmt = SimpleDateFormat("ss", Locale.US)
        val effectiveDateStyle = if (dateFormatStyle == "full") "full_year" else dateFormatStyle

        while (true) {
            val cal = Calendar.getInstance()
            timeStr = timeFmt.format(cal.time)
            secondsStr = secFmt.format(cal.time)
            val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
            amPmStr = if (!is24hFormat) LocaleHelper.getAmPmString(hourOfDay, lang) else ""
            dateStr = LocaleHelper.formatDate(cal.time, effectiveDateStyle, lang)
            greetingStr = GreetingHelper.getGreeting(hourOfDay, effectiveLang, greetingMode, customGreetingText)
            delay(1000)
        }
    }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        // Random 4K Wallpaper Layer
        AsyncImage(
            model = randomWallpaperUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        val wallpaperEffect by prefs.slideshowEffectFlow.collectAsState(initial = "none")
        if (wallpaperEffect != "none") {
            AtmosphericParticleOverlay(
                effect = wallpaperEffect,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Deep black corner shadow anchored flush at the active corner (bottom-left in RTL, bottom-right in LTR)
        // and fading out smoothly behind the clock without unbounded edge washout
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val cornerX = if (isRtl) 0f else size.width
            val cornerY = size.height
            val startCorner = Offset(x = cornerX, y = cornerY)
            val diagReach = 440.dp.toPx()

            // Exact 45-degree diagonal fade starting right at the corner
            val end45Deg = Offset(
                x = if (isRtl) cornerX + diagReach else cornerX - diagReach,
                y = cornerY - diagReach
            )

            drawRect(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Black.copy(alpha = 0.92f),
                        0.28f to Color.Black.copy(alpha = 0.72f),
                        0.55f to Color.Black.copy(alpha = 0.38f),
                        0.82f to Color.Black.copy(alpha = 0.10f),
                        1.00f to Color.Transparent
                    ),
                    start = startCorner,
                    end = end45Deg
                )
            )

            // Corner-anchored radial shadow wrapping smoothly from the exact corner behind the clock
            val radialRadius = maxOf(size.width * 0.56f, 520.dp.toPx())
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Black.copy(alpha = 0.96f),
                        0.28f to Color.Black.copy(alpha = 0.84f),
                        0.54f to Color.Black.copy(alpha = 0.54f),
                        0.78f to Color.Black.copy(alpha = 0.20f),
                        1.00f to Color.Transparent
                    ),
                    center = startCorner,
                    radius = radialRadius
                ),
                center = startCorner,
                radius = radialRadius
            )
        }

        // Fixed Bottom-End Info & Clock over the 45-degree corner blur (Bottom-Left in RTL, Bottom-Right in LTR)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 44.dp, bottom = 38.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (greetingStr.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = greetingStr,
                            color = TvAccentGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = dateFont,
                            style = TextStyle(
                                fontFamily = dateFont,
                                shadow = directTextShadow
                            )
                        )
                    }
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeStr,
                            color = TvTextPrimary,
                            fontSize = 62.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Default,
                            letterSpacing = (-1).sp,
                            style = TextStyle(
                                fontFeatureSettings = "tnum",
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.9f),
                                    blurRadius = 18f,
                                    offset = Offset(0f, 4f)
                                )
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = secondsStr,
                            color = TvAccentGold,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(
                                fontFeatureSettings = "tnum",
                                shadow = directTextShadow
                            ),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Date and localized AM/PM directly on the image at the same horizontal level and size
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!is24hFormat && amPmStr.isNotBlank()) {
                        Text(
                            text = amPmStr,
                            color = TvAccentGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = dateFont,
                            style = TextStyle(
                                fontFamily = dateFont,
                                shadow = directTextShadow
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•",
                            color = TvTextPrimary.copy(alpha = 0.85f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = dateFont,
                            style = TextStyle(
                                fontFamily = dateFont,
                                shadow = directTextShadow
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = dateStr,
                        color = TvTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = dateFont,
                        style = TextStyle(
                            fontFamily = dateFont,
                            shadow = directTextShadow
                        )
                    )
                }
            }
        }

        // Bottom hint directly on image
        Text(
            text = stringResource(R.string.ambient_press_any_key),
            color = TvTextPrimary.copy(alpha = 0.8f),
            fontSize = 12.sp,
            style = TextStyle(shadow = directTextShadow),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 36.dp, bottom = 36.dp)
        )
    }
}

/**
 * Mode 1 (Default): Centered large Clock, Date, Greetings & Ambient floating luminous orbs.
 * When 12-hour format is chosen, localized AM/PM appears beside the date at the date's font size and level.
 */
@Composable
private fun ClockDateScreensaver(
    is24hFormat: Boolean,
    auroraColors: AuroraColors,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { LauncherPreferences(context) }
    val lang by prefs.languageFlow.collectAsState(initial = "system")

    var timeStr by remember { mutableStateOf("") }
    var secondsStr by remember { mutableStateOf("") }
    var amPmStr by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("") }
    var greetingStr by remember { mutableStateOf("") }

    val effectiveLang = LocaleHelper.getEffectiveLanguage(lang)
    val isArabic = effectiveLang == "ar" || effectiveLang == "ur" || effectiveLang == "fa"
    val dateFont = if (isArabic) CairoFontFamily else FontFamily.Default

    val greetingMode by prefs.greetingModeFlow.collectAsState(initial = "poetic")
    val customGreetingText by prefs.customGreetingTextFlow.collectAsState(initial = "")
    val dateFormatStyle by prefs.dateFormatStyleFlow.collectAsState(initial = "full")

    LaunchedEffect(is24hFormat, lang, greetingMode, customGreetingText, dateFormatStyle) {
        val pattern = if (is24hFormat) "HH:mm" else "hh:mm"
        val timeFmt = SimpleDateFormat(pattern, Locale.US)
        val secFmt = SimpleDateFormat("ss", Locale.US)
        val effectiveDateStyle = if (dateFormatStyle == "full") "full_year" else dateFormatStyle

        while (true) {
            val cal = Calendar.getInstance()
            timeStr = timeFmt.format(cal.time)
            secondsStr = secFmt.format(cal.time)
            val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
            amPmStr = if (!is24hFormat) LocaleHelper.getAmPmString(hourOfDay, lang) else ""
            dateStr = LocaleHelper.formatDate(cal.time, effectiveDateStyle, lang)
            greetingStr = GreetingHelper.getGreeting(hourOfDay, effectiveLang, greetingMode, customGreetingText)
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_motion")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        AuroraBackground(colors = auroraColors)

        // Floating luminous ambient orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx1 = w * (0.3f + 0.15f * sin(phase))
            val cy1 = h * (0.35f + 0.12f * cos(phase))
            val cx2 = w * (0.7f + 0.14f * cos(phase * 0.8f))
            val cy2 = h * (0.65f + 0.12f * sin(phase * 0.8f))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auroraColors.glow.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(cx1, cy1),
                    radius = w * 0.32f
                ),
                center = Offset(cx1, cy1),
                radius = w * 0.32f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TvAccentGold.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(cx2, cy2),
                    radius = w * 0.28f
                ),
                center = Offset(cx2, cy2),
                radius = w * 0.28f
            )
        }

        // Dark cinema vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.68f)
                        )
                    )
                )
        )

        // Stationary Centered Clock & Date
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            if (greetingStr.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, TvAccentGold.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = TvAccentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = greetingStr,
                        color = TvAccentGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = dateFont
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeStr,
                        color = TvTextPrimary,
                        fontSize = 112.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Default,
                        letterSpacing = (-2).sp,
                        style = TextStyle(
                            fontFeatureSettings = "tnum",
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.75f),
                                blurRadius = 24f,
                                offset = Offset(0f, 6f)
                            )
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = secondsStr,
                        color = TvAccentGold,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        modifier = Modifier.padding(bottom = 18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date and localized AM/PM on the same horizontal line and exact same font size (22.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (!is24hFormat && amPmStr.isNotBlank()) {
                    Text(
                        text = amPmStr,
                        color = TvAccentGold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = dateFont
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = TvTextSecondary.copy(alpha = 0.7f),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = dateFont
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = dateStr,
                    color = TvTextPrimary.copy(alpha = 0.88f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = dateFont
                )
            }
        }

        // Bottom subtle hint
        Text(
            text = stringResource(R.string.ambient_press_any_key),
            color = TvTextSecondary.copy(alpha = 0.7f),
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}