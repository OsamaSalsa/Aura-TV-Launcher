package com.example.ui.home

import android.view.LayoutInflater
import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import android.net.Uri
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
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
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
import com.example.util.ResolvedYouTubeStream
import com.example.util.YouTubeStreamResolver
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
            // Mode 3: Fullscreen Video; displays Clock screensaver cleanly while video buffers instead of a black screen
            VideoScreensaver(
                rawVideoInput = screensaverVideoId,
                is24hFormat = is24hFormat,
                auroraColors = auroraColors,
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

@OptIn(UnstableApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun VideoScreensaver(
    rawVideoInput: String,
    is24hFormat: Boolean,
    auroraColors: AuroraColors,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit
) {
    val cleanInput = rawVideoInput.trim()
    val isDirectStreamUrl = (cleanInput.startsWith("http://") || cleanInput.startsWith("https://")) &&
            !cleanInput.contains("youtube.com") && !cleanInput.contains("youtu.be")
    val videoId = remember(cleanInput) { extractYouTubeVideoId(cleanInput) }
    val hostView = LocalView.current
    var isVideoReady by remember(cleanInput) { mutableStateOf(false) }
    var resolvedStream by remember(cleanInput) {
        mutableStateOf<ResolvedYouTubeStream?>(
            if (!isDirectStreamUrl) YouTubeStreamResolver.getCached(videoId) else null
        )
    }
    var useWebFallback by remember(cleanInput) { mutableStateOf(false) }

    LaunchedEffect(cleanInput, isDirectStreamUrl, videoId) {
        isVideoReady = false
        useWebFallback = false
        if (!isDirectStreamUrl) {
            val cached = YouTubeStreamResolver.getCached(videoId)
            if (cached != null) {
                resolvedStream = cached
            } else {
                val resolved = YouTubeStreamResolver.resolve(videoId)
                if (resolved != null) {
                    resolvedStream = resolved
                } else {
                    useWebFallback = true
                }
            }
        }
    }

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
    ) {
        if (isDirectStreamUrl) {
            DirectStreamExoPlayerScreensaver(
                streamUrl = cleanInput,
                onVideoActivelyPlaying = { isVideoReady = true },
                onStreamError = {}
            )
        } else {
            val activeResolved = resolvedStream
            if (activeResolved != null && !useWebFallback) {
                DirectStreamExoPlayerScreensaver(
                    streamUrl = activeResolved.videoUrl,
                    audioUrl = activeResolved.audioUrl,
                    isHls = activeResolved.isHls,
                    userAgent = activeResolved.userAgent,
                    onVideoActivelyPlaying = { isVideoReady = true },
                    onStreamError = {
                        YouTubeStreamResolver.invalidate(videoId)
                        isVideoReady = false
                        useWebFallback = true
                    }
                )
            } else if (useWebFallback) {
                YouTubeWebScreensaver(
                    videoId = videoId,
                    onVideoReady = { isVideoReady = true }
                )
            }
        }

        // Show Clock screensaver instead of a black screen until the video starts rendering frames
        AnimatedVisibility(
            visible = !isVideoReady,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = 400)),
            modifier = Modifier.fillMaxSize()
        ) {
            ClockDateScreensaver(
                is24hFormat = is24hFormat,
                auroraColors = auroraColors,
                focusRequester = null,
                onDismiss = onDismiss
            )
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
 * Fallback WebView YouTube player when direct native ExoPlayer stream extraction fails.
 * Requests HD (1080p/720p) quality and falls back swiftly if embed playback is blocked.
 */
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
private fun YouTubeWebScreensaver(
    videoId: String,
    onVideoReady: () -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var crashCount by remember(videoId) { mutableStateOf(0) }
    var useInvidiousFallback by remember(videoId) { mutableStateOf(false) }
    var hasVideoStarted by remember(videoId) { mutableStateOf(false) }
    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }

    val playerUrl = remember(videoId, useInvidiousFallback) {
        if (!useInvidiousFallback) {
            "https://www.youtube.com/watch?v=$videoId"
        } else {
            "https://invidious.tiekoetter.com/embed/$videoId?autoplay=1&muted=1&loop=1&controls=0&quality=hd720"
        }
    }

    LaunchedEffect(videoId, useInvidiousFallback, hasVideoStarted) {
        if (!useInvidiousFallback && !hasVideoStarted) {
            delay(8_500L)
            if (!hasVideoStarted) {
                useInvidiousFallback = true
            }
        } else if (useInvidiousFallback && !hasVideoStarted) {
            delay(10_000L)
            if (!hasVideoStarted) {
                onVideoReady()
            }
        }
    }

    val cleanFullscreenScript = remember(useInvidiousFallback) {
        """
        (function() {
            function notifyIfPlaying(v) {
                if (v && v.readyState >= 2 && !v.paused && v.videoWidth > 0 && v.currentTime > 0.15) {
                    if (!window.__auraVideoReadySent) {
                        window.__auraVideoReadySent = true;
                        try { AuraVideoBridge.onVideoStarted(); } catch(e) {}
                    }
                }
            }
            function tryDismissConsent() {
                try {
                    if (location.hostname && location.hostname.indexOf('consent.') !== -1) {
                        var btns = document.querySelectorAll('form button, button');
                        for (var i = 0; i < btns.length; i++) {
                            var txt = (btns[i].innerText || btns[i].getAttribute('aria-label') || '').toLowerCase();
                            if (txt.indexOf('reject') !== -1 || txt.indexOf('accept') !== -1 || txt.indexOf('agree') !== -1) {
                                btns[i].click();
                                return true;
                            }
                        }
                    }
                    var dialogBtn = document.querySelector('tp-yt-paper-dialog button[aria-label*="Accept" i], tp-yt-paper-dialog button[aria-label*="Reject" i], .eom-button-row button, ytd-consent-bump-v2-lightbox button[aria-label*="Reject" i], ytd-consent-bump-v2-lightbox button[aria-label*="Accept" i]');
                    if (dialogBtn) {
                        dialogBtn.click();
                        return true;
                    }
                } catch (e) {}
                return false;
            }
            function applyCleanVideo() {
                try {
                    tryDismissConsent();
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
                            '#masthead-container, #secondary, #below, #comments, #related, ytd-mini-guide-renderer, tp-yt-app-drawer, ' +
                            '.ytp-chrome-top, .ytp-chrome-bottom, .ytp-gradient-top, .ytp-gradient-bottom, .ytp-watermark, .ytp-show-cards-title, .ytp-title, .ytp-pause-overlay, .ytp-ce-element, .ytp-spinner, .ytp-impression-link, .ytp-bezel, .ytp-cued-thumbnail-overlay, ' +
                            '.navbar, header, footer, .watch-card, .pure-u-lg-1-5, .title, .flexible, #description-box, .vjs-control-bar, .vjs-modal-dialog, .vjs-EndScreen, .vjs-title-bar, .vjs-DockText, .vjs-poster, .vjs-loading-spinner, .watch-on-invidious { display:none !important; opacity:0 !important; visibility:hidden !important; } ' +
                            '.vjs-big-play-button, .ytp-large-play-button { opacity:0 !important; } ' +
                            '#movie_player, #player, #player-container, .html5-video-player, .html5-video-container, .video-js { position:fixed !important; top:0 !important; left:0 !important; width:100vw !important; height:100vh !important; max-width:100vw !important; max-height:100vh !important; background:#000 !important; z-index:999999 !important; border:none !important; outline:none !important; display:block !important; visibility:visible !important; opacity:1 !important; } ' +
                            'video, .video-stream, .vjs-tech { position:fixed !important; top:0 !important; left:0 !important; width:100vw !important; height:100vh !important; max-width:100vw !important; max-height:100vh !important; object-fit:cover !important; background:#000 !important; z-index:2147483647 !important; border:none !important; outline:none !important; display:block !important; visibility:visible !important; opacity:1 !important; }';
                        document.head.appendChild(st);
                    }
                    if (document.body) {
                        document.body.style.opacity = '1';
                    }
                    var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-skip-ad-button, .ytp-ad-skip-button-modern');
                    if (skipBtn) {
                        try { skipBtn.click(); } catch(e) {}
                    }
                    var mp = document.getElementById('movie_player');
                    if (mp && !window.__auraQualityApplied) {
                        try {
                            if (mp.setPlaybackQualityRange) { mp.setPlaybackQualityRange('hd720', 'hd1080'); }
                            if (mp.setPlaybackQuality) { mp.setPlaybackQuality('hd1080'); }
                            window.__auraQualityApplied = true;
                        } catch(e) {}
                    }
                    var v = document.querySelector('video');
                    if (v) {
                        v.loop = true;
                        v.controls = false;
                        if (mp && mp.classList && mp.classList.contains('ad-showing') && isFinite(v.duration) && v.duration > 0) {
                            try { v.currentTime = v.duration; } catch(e) {}
                        }
                        if (!v.__auraBound) {
                            v.__auraBound = true;
                            v.addEventListener('playing', function() { notifyIfPlaying(v); });
                            v.addEventListener('timeupdate', function() { notifyIfPlaying(v); });
                        }
                        if (v.paused) {
                            v.muted = true;
                            v.play().then(function() {
                                if (!window.__auraUnmuteScheduled) {
                                    window.__auraUnmuteScheduled = true;
                                    setTimeout(function() {
                                        try {
                                            v.muted = false;
                                            v.volume = 1.0;
                                            setTimeout(function() {
                                                if (v.paused) {
                                                    v.muted = true;
                                                    v.play().catch(function(){});
                                                }
                                            }, 300);
                                        } catch(e) {}
                                    }, 500);
                                }
                            }).catch(function() {
                                var playBtn = document.querySelector('.ytp-large-play-button, .ytp-play-button, .vjs-big-play-button');
                                if (playBtn) { try { playBtn.click(); } catch(e) {} }
                            });
                        }
                        notifyIfPlaying(v);
                    }
                } catch (e) {}
            }
            applyCleanVideo();
            if (!window.__ambientCleanTimer) {
                window.__ambientCleanTimer = setInterval(applyCleanVideo, 250);
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
            androidx.compose.runtime.key(videoId, useInvidiousFallback, crashCount) {
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
                                cacheMode = WebSettings.LOAD_DEFAULT
                                userAgentString = STREAM_USER_AGENT
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    safeBrowsingEnabled = false
                                }
                            }

                            hardenWebView(this)

                            addJavascriptInterface(
                                object {
                                    @android.webkit.JavascriptInterface
                                    fun onVideoStarted() {
                                        mainHandler.post {
                                            hasVideoStarted = true
                                            onVideoReady()
                                        }
                                    }

                                    @android.webkit.JavascriptInterface
                                    fun onEmbedError() {
                                        mainHandler.post {
                                            if (!useInvidiousFallback && !hasVideoStarted) {
                                                useInvidiousFallback = true
                                            }
                                        }
                                    }
                                },
                                "AuraVideoBridge"
                            )

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
private class ChunkedHttpDataSource(
    private val upstream: HttpDataSource,
    private val chunkSizeBytes: Long = 2L * 1024L * 1024L
) : DataSource {
    private var currentDataSpec: DataSpec? = null
    private var currentPosition: Long = 0L
    private var bytesRemainingTotal: Long = C.LENGTH_UNSET.toLong()
    private var bytesRemainingInChunk: Long = 0L
    private var openedChunk: Boolean = false

    override fun addTransferListener(transferListener: TransferListener) {
        upstream.addTransferListener(transferListener)
    }

    override fun open(dataSpec: DataSpec): Long {
        currentDataSpec = dataSpec
        currentPosition = dataSpec.position
        bytesRemainingTotal = if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
            dataSpec.length
        } else {
            C.LENGTH_UNSET.toLong()
        }
        openNextChunk(initial = true)
        return bytesRemainingTotal
    }

    private fun openNextChunk(initial: Boolean): Boolean {
        val base = currentDataSpec ?: return false
        if (bytesRemainingTotal == 0L) return false

        val toRequest = if (bytesRemainingTotal != C.LENGTH_UNSET.toLong()) {
            minOf(chunkSizeBytes, bytesRemainingTotal)
        } else {
            chunkSizeBytes
        }

        val chunkSpec = base.buildUpon()
            .setPosition(currentPosition)
            .setLength(toRequest)
            .build()

        val openedLen = upstream.open(chunkSpec)
        openedChunk = true

        if (initial && bytesRemainingTotal == C.LENGTH_UNSET.toLong()) {
            val contentRange = upstream.responseHeaders["Content-Range"]?.firstOrNull()
                ?: upstream.responseHeaders["content-range"]?.firstOrNull()
            val totalSize = contentRange?.substringAfterLast("/")?.toLongOrNull()
            if (totalSize != null && totalSize > currentPosition) {
                bytesRemainingTotal = totalSize - currentPosition
            }
        }

        bytesRemainingInChunk = if (openedLen != C.LENGTH_UNSET.toLong()) openedLen else toRequest
        return bytesRemainingInChunk != 0L
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (bytesRemainingTotal == 0L) return C.RESULT_END_OF_INPUT

        if (bytesRemainingInChunk == 0L) {
            closeUpstreamQuietly()
            if (!openNextChunk(initial = false)) {
                return C.RESULT_END_OF_INPUT
            }
        }

        val maxToRead = if (bytesRemainingInChunk > 0L) {
            minOf(length.toLong(), bytesRemainingInChunk).toInt()
        } else {
            length
        }

        val read = upstream.read(buffer, offset, maxToRead)
        if (read == C.RESULT_END_OF_INPUT) {
            if (bytesRemainingTotal == C.LENGTH_UNSET.toLong() || bytesRemainingTotal > 0L) {
                if (bytesRemainingInChunk > 0L && bytesRemainingTotal == C.LENGTH_UNSET.toLong()) {
                    bytesRemainingTotal = 0L
                    return C.RESULT_END_OF_INPUT
                }
                closeUpstreamQuietly()
                if (!openNextChunk(initial = false)) {
                    return C.RESULT_END_OF_INPUT
                }
                return read(buffer, offset, length)
            }
            return C.RESULT_END_OF_INPUT
        }

        if (read > 0) {
            currentPosition += read
            if (bytesRemainingTotal != C.LENGTH_UNSET.toLong()) {
                bytesRemainingTotal = (bytesRemainingTotal - read).coerceAtLeast(0L)
            }
            if (bytesRemainingInChunk > 0L) {
                bytesRemainingInChunk = (bytesRemainingInChunk - read).coerceAtLeast(0L)
            }
        }
        return read
    }

    override fun getUri(): Uri? = upstream.uri ?: currentDataSpec?.uri

    override fun getResponseHeaders(): Map<String, List<String>> = upstream.responseHeaders

    override fun close() {
        currentDataSpec = null
        closeUpstreamQuietly()
    }

    private fun closeUpstreamQuietly() {
        if (openedChunk) {
            openedChunk = false
            try { upstream.close() } catch (_: Exception) {}
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun DirectStreamExoPlayerScreensaver(
    streamUrl: String,
    audioUrl: String? = null,
    isHls: Boolean = false,
    userAgent: String = STREAM_USER_AGENT,
    onVideoActivelyPlaying: () -> Unit = {},
    onStreamError: () -> Unit
) {
    val context = LocalContext.current
    var stoppedByLifecycle by remember { mutableStateOf(false) }
    var retryCount by remember(streamUrl, audioUrl) { mutableStateOf(0) }
    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }
    var firstFrameRendered by remember(streamUrl, audioUrl) { mutableStateOf(false) }
    var activePlaybackConfirmed by remember(streamUrl, audioUrl) { mutableStateOf(false) }
    var stallReported by remember(streamUrl, audioUrl) { mutableStateOf(false) }

    val exoPlayer: ExoPlayer = remember(streamUrl, audioUrl, isHls, userAgent) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(8000)
            .setReadTimeoutMs(10000)

        val effectiveUpstreamFactory = if (!isHls && streamUrl.contains("googlevideo.com", ignoreCase = true)) {
            DataSource.Factory { ChunkedHttpDataSource(httpDataSourceFactory.createDataSource()) }
        } else {
            httpDataSourceFactory
        }

        val dataSourceFactory = DefaultDataSource.Factory(context, effectiveUpstreamFactory)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
            .setDrmSessionManagerProvider { DrmSessionManager.DRM_UNSUPPORTED }

        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            .setEnableDecoderFallback(true)

        val trackSelector = DefaultTrackSelector(context).apply {
            parameters = buildUponParameters()
                .setMaxVideoSize(1920, 1080)
                .setPreferredVideoMimeTypes("video/avc", "video/hevc", "video/x-vnd.on2.vp9")
                .setForceHighestSupportedBitrate(true)
                .build()
        }

        val bandwidthMeter = DefaultBandwidthMeter.Builder(context)
            .setInitialBitrateEstimate(10_000_000L)
            .build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2000,  // minBufferMs
                25000, // maxBufferMs
                250,   // bufferForPlaybackMs (near-instant start)
                800    // bufferForPlaybackAfterRebufferMs
            )
            .build()

        val mediaSource = when {
            isHls || streamUrl.contains(".m3u8", ignoreCase = true) -> {
                HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(streamUrl))
            }
            !audioUrl.isNullOrBlank() -> {
                val videoSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(streamUrl))
                val audioSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(audioUrl))
                MergingMediaSource(true, true, videoSource, audioSource)
            }
            else -> {
                mediaSourceFactory.createMediaSource(MediaItem.fromUri(streamUrl))
            }
        }

        ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(bandwidthMeter)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaSource(mediaSource)
                prepare()
                playWhenReady = true
                volume = 1f
            }
    }

    // Confirm real frame advancement before hiding clock overlay, and recover automatically if stalled
    LaunchedEffect(exoPlayer) {
        var lastPos = exoPlayer.currentPosition
        var stalledMs = 0L
        while (!stallReported) {
            delay(300L)
            if (stoppedByLifecycle) continue
            val currentPos = exoPlayer.currentPosition
            val isAdvancing = currentPos > lastPos || (exoPlayer.isPlaying && exoPlayer.playbackState == Player.STATE_READY && currentPos > 100L)
            if (isAdvancing) {
                stalledMs = 0L
                if (firstFrameRendered && !activePlaybackConfirmed) {
                    activePlaybackConfirmed = true
                    onVideoActivelyPlaying()
                }
            } else {
                stalledMs += 300L
                if (stalledMs >= 4500L && !activePlaybackConfirmed) {
                    stallReported = true
                    onStreamError()
                    break
                }
            }
            lastPos = currentPos
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

            override fun onRenderedFirstFrame() {
                firstFrameRendered = true
                if (exoPlayer.currentPosition > 80L && !activePlaybackConfirmed) {
                    activePlaybackConfirmed = true
                    onVideoActivelyPlaying()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                if (!stoppedByLifecycle && retryCount < 1) {
                    retryCount++
                    postOnMain(300L) {
                        if (!stoppedByLifecycle) {
                            try {
                                exoPlayer.stop()
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            } catch (_: Exception) {}
                        }
                    }
                } else if (!stallReported) {
                    stallReported = true
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
                .inflate(R.layout.layout_screensaver_player, null) as PlayerView
            view.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                keepScreenOn = true
                useController = false
                player = exoPlayer
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setEnableComposeSurfaceSyncWorkaround(true)
                playerViewRef = this
            }
        },
        update = { view ->
            view.keepScreenOn = true
            view.useController = false
            view.player = exoPlayer
            view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
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
    val isArabic = LocaleHelper.isArabicOrRtlLanguage(lang) ||
        LocaleHelper.containsArabicScript(dateStr) ||
        LocaleHelper.containsArabicScript(greetingStr)
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
    focusRequester: FocusRequester? = null,
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
    val isArabic = LocaleHelper.isArabicOrRtlLanguage(lang) ||
        LocaleHelper.containsArabicScript(dateStr) ||
        LocaleHelper.containsArabicScript(greetingStr)
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

    val boxModifier = if (focusRequester != null) {
        Modifier
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
    } else {
        Modifier.fillMaxSize()
    }

    Box(
        modifier = boxModifier
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