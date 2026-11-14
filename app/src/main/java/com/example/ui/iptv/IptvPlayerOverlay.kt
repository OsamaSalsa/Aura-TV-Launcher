package com.example.ui.iptv

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.FormatHolder
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.mediacodec.MediaCodecRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.EpgManager
import com.example.data.EpgProgram
import com.example.data.model.IptvChannel
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PlayerHudGlassTop = Color(0xE60B1325)
private val PlayerHudGlassBottom = Color(0xF5070B16)

@OptIn(UnstableApi::class, ExperimentalComposeUiApi::class)
@Composable
fun IptvPlayerOverlay(
    channel: IptvChannel,
    channels: List<IptvChannel> = emptyList(),
    epgPrograms: Map<String, List<EpgProgram>> = emptyMap(),
    playbackError: String?,
    onNextChannel: () -> Unit,
    onPrevChannel: () -> Unit,
    onSelectChannel: (IptvChannel) -> Unit = {},
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit,
    onError: (String) -> Unit,
    onOpenIptvSettings: (() -> Unit)? = null,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var isBuffering by remember { mutableStateOf(true) }
    var isPaused by remember { mutableStateOf(false) }

    // When showControls == true (Channel Info Menu is visible), D-Pad Left/Right & Up/Down
    // strictly control the cursor inside the 2x5 Info Menu matrix and NEVER switch channels.
    var showControls by remember { mutableStateOf(true) }
    var controlsInteractionTick by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Passive top-start channel switch pill when zapping channels via Up/Down in fullscreen mode
    var showChannelSwitchBanner by remember { mutableStateOf(false) }
    var channelSwitchBannerTick by remember { mutableLongStateOf(0L) }

    var showQuickBar by remember { mutableStateOf(false) }
    var quickBarInteractionTick by remember { mutableLongStateOf(0L) }

    var streamQualityBadge by remember { mutableStateOf<String?>(null) }
    var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var sleepTimerMinutes by remember { mutableStateOf(0) }
    var numericBuffer by remember { mutableStateOf("") }
    var numericDigitTick by remember { mutableLongStateOf(0L) }

    val rootFocusRequester = remember { FocusRequester() }
    // Single-row (10 buttons) deterministic focus array for the Channel Info Menu
    val menuFocusRequesters = remember { Array(10) { FocusRequester() } }
    var focusedMenuCol by remember { mutableStateOf(0) }
    val quickStripFocusRequesters = remember { mutableMapOf<Long, FocusRequester>() }

    // Live clock for Top Broadcast Bar
    var currentClockText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        while (true) {
            currentClockText = sdf.format(Date())
            delay(15_000L)
        }
    }

    LaunchedEffect(sleepTimerMinutes) {
        if (sleepTimerMinutes > 0) {
            delay(sleepTimerMinutes * 60_000L)
            onClose()
        }
    }

    LaunchedEffect(numericBuffer, numericDigitTick) {
        if (numericBuffer.isNotEmpty()) {
            delay(1300L)
            val targetIdx = numericBuffer.toIntOrNull()?.minus(1)
            numericBuffer = ""
            if (targetIdx != null && targetIdx in channels.indices) {
                onSelectChannel(channels[targetIdx])
            }
        }
    }

    BackHandler {
        when {
            showQuickBar -> {
                showQuickBar = false
                try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
            }
            showControls -> {
                showControls = false
                try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
            }
            else -> onClose()
        }
    }

    // Auto-hide Info Menu after 8 seconds of inactivity
    LaunchedEffect(showControls, controlsInteractionTick) {
        if (showControls) {
            delay(8000)
            showControls = false
            if (!showQuickBar && playbackError == null) {
                try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
            }
        }
    }

    // Auto-hide passive channel-switch pill after 2.6 seconds
    LaunchedEffect(showChannelSwitchBanner, channelSwitchBannerTick) {
        if (showChannelSwitchBanner) {
            delay(2600)
            showChannelSwitchBanner = false
        }
    }

    // Auto-hide horizontal channel strip after 9 seconds of inactivity
    LaunchedEffect(showQuickBar, quickBarInteractionTick) {
        if (showQuickBar) {
            delay(9000)
            showQuickBar = false
            if (!showControls && playbackError == null) {
                try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
            }
        }
    }

    // Whenever the Channel Info Menu (showControls) is visible, immediately place the D-Pad cursor on the single-row menu
    LaunchedEffect(showControls, channel.id, playbackError) {
        if (showControls && playbackError == null) {
            delay(50)
            try {
                menuFocusRequesters[focusedMenuCol.coerceIn(0, 9)].requestFocus()
            } catch (_: Exception) {}
        } else if (!showControls && !showQuickBar && playbackError == null) {
            try {
                rootFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    var audioFallbackNotice by remember { mutableStateOf<String?>(null) }
    var audioDisabledForChannel by remember { mutableStateOf(false) }

    // Media3 ExoPlayer instance configured for live stream URLs with resilient audio fallback
    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    var exoPlayerRef by remember { mutableStateOf<ExoPlayer?>(null) }

    val exoPlayer: ExoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(DefaultDataSource.Factory(context, httpDataSourceFactory))
            .setDrmSessionManagerProvider { DrmSessionManager.DRM_UNSUPPORTED }

        val renderersFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioRenderers(
                ctx: android.content.Context,
                extensionRendererMode: Int,
                mediaCodecSelector: MediaCodecSelector,
                enableDecoderFallback: Boolean,
                audioSink: AudioSink,
                eventHandler: android.os.Handler,
                eventListener: AudioRendererEventListener,
                out: ArrayList<Renderer>
            ) {
                val safeAudioRenderer = object : MediaCodecAudioRenderer(
                    ctx,
                    mediaCodecSelector,
                    enableDecoderFallback,
                    eventHandler,
                    eventListener,
                    audioSink
                ) {
                    private var bypassUnsupportedAudio = false

                    private fun hasNativeDecoder(format: Format): Boolean {
                        val mime = format.sampleMimeType ?: return false
                        return try {
                            mediaCodecSelector.getDecoderInfos(mime, false, false).isNotEmpty() ||
                                MediaCodecUtil.getDecoderInfos(mime, false, false).isNotEmpty()
                        } catch (_: Exception) {
                            false
                        }
                    }

                    override fun onEnabled(joining: Boolean, mayRenderStartOfStream: Boolean) {
                        bypassUnsupportedAudio = false
                        super.onEnabled(joining, mayRenderStartOfStream)
                    }

                    override fun onDisabled() {
                        bypassUnsupportedAudio = false
                        super.onDisabled()
                    }

                    override fun onReset() {
                        bypassUnsupportedAudio = false
                        super.onReset()
                    }

                    override fun onInputFormatChanged(formatHolder: FormatHolder): DecoderReuseEvaluation? {
                        val incoming = formatHolder.format
                        if (incoming != null && incoming.sampleMimeType != null && !hasNativeDecoder(incoming)) {
                            // Stream advertised a MIME (e.g. audio/mpeg-L2 in TS PMT under group_aac / mp4a.40.2)
                            // with no native MediaCodec on this device. Disable audio cleanly so video keeps playing
                            // instead of throwing DecoderInitializationException [-49999].
                            bypassUnsupportedAudio = true
                            mainHandler.post {
                                val player = exoPlayerRef
                                if (player != null && !player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_AUDIO)) {
                                    audioDisabledForChannel = true
                                    audioFallbackNotice = context.getString(
                                        R.string.iptv_audio_unsupported_codec,
                                        incoming.sampleMimeType
                                    )
                                    player.trackSelectionParameters = player.trackSelectionParameters
                                        .buildUpon()
                                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                                        .build()
                                }
                            }
                            return null
                        }
                        bypassUnsupportedAudio = false
                        return super.onInputFormatChanged(formatHolder)
                    }

                    override fun render(positionUs: Long, elapsedRealtimeUs: Long) {
                        if (bypassUnsupportedAudio) {
                            return
                        }
                        super.render(positionUs, elapsedRealtimeUs)
                    }
                }
                out.add(safeAudioRenderer)
                super.buildAudioRenderers(
                    ctx,
                    extensionRendererMode,
                    mediaCodecSelector,
                    enableDecoderFallback,
                    audioSink,
                    eventHandler,
                    eventListener,
                    out
                )
                // Remove duplicate stock MediaCodecAudioRenderer added by super
                if (out.size > 1) {
                    val iterator = out.iterator()
                    while (iterator.hasNext()) {
                        val r = iterator.next()
                        if (r !== safeAudioRenderer && r is MediaCodecAudioRenderer) {
                            iterator.remove()
                        }
                    }
                }
            }
        }
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableDecoderFallback(true)

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                playWhenReady = true
                exoPlayerRef = this
            }
    }

    var stoppedByLifecycle by remember { mutableStateOf(false) }
    var codecRecoveryAttempts by remember(channel) { mutableStateOf(0) }
    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }

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

    DisposableEffect(channel) {
        isBuffering = true
        isPaused = false
        streamQualityBadge = null
        audioDisabledForChannel = false
        audioFallbackNotice = null
        codecRecoveryAttempts = 0
        stoppedByLifecycle = false
        try {
            exoPlayer.stop()
        } catch (_: Exception) {}
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
            .build()

        val mediaItem = MediaItem.fromUri(channel.streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = (state == Player.STATE_BUFFERING)
                if (state == Player.STATE_READY) {
                    codecRecoveryAttempts = 0
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPaused = !isPlaying && exoPlayer.playbackState == Player.STATE_READY
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                val h = videoSize.height
                streamQualityBadge = when {
                    h >= 2160 -> "4K UHD"
                    h >= 1080 -> "1080p FHD"
                    h >= 720 -> "720p HD"
                    h > 0 -> "${h}p SD"
                    else -> null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val exoError = error as? ExoPlaybackException
                val isAudioRendererFailure = exoError != null &&
                    exoError.type == ExoPlaybackException.TYPE_RENDERER &&
                    (
                        exoError.rendererFormat?.sampleMimeType?.startsWith("audio/") == true ||
                            MimeTypes.isAudio(exoError.rendererFormat?.sampleMimeType) ||
                            (exoError.rendererIndex in 0 until exoPlayer.rendererCount &&
                                exoPlayer.getRendererType(exoError.rendererIndex) == C.TRACK_TYPE_AUDIO) ||
                            error.cause is AudioSink.InitializationException ||
                            error.cause is AudioSink.ConfigurationException ||
                            (error.cause is MediaCodecRenderer.DecoderInitializationException &&
                                exoError.rendererFormat?.sampleMimeType?.startsWith("video/") != true)
                        )

                if (isAudioRendererFailure && !audioDisabledForChannel) {
                    audioDisabledForChannel = true
                    audioFallbackNotice = context.getString(R.string.iptv_audio_unsupported_generic)
                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                        .build()
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                    return
                }

                // Recover gracefully if MediaCodec was reclaimed by Android's ResourceManagerService
                val isCodecReclaimOrTransient = !stoppedByLifecycle &&
                    codecRecoveryAttempts < 2 &&
                    exoError?.type == ExoPlaybackException.TYPE_RENDERER &&
                    (
                        error.cause is IllegalStateException ||
                            error.cause is android.media.MediaCodec.CodecException ||
                            error.cause is androidx.media3.exoplayer.mediacodec.MediaCodecDecoderException ||
                            error.cause is MediaCodecRenderer.DecoderInitializationException
                        )

                if (isCodecReclaimOrTransient) {
                    codecRecoveryAttempts++
                    mainHandler.postDelayed({
                        if (exoPlayerRef === exoPlayer && !stoppedByLifecycle) {
                            try {
                                exoPlayer.stop()
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            } catch (_: Exception) {}
                        }
                    }, 350L)
                    return
                }

                isBuffering = false
                val cause = error.cause
                val errorDetails = when {
                    cause is HttpDataSource.InvalidResponseCodeException && cause.responseCode == 404 ->
                        context.getString(R.string.iptv_error_404)
                    cause is HttpDataSource.InvalidResponseCodeException && cause.responseCode == 403 ->
                        context.getString(R.string.iptv_error_403)
                    cause is HttpDataSource.InvalidResponseCodeException ->
                        context.getString(R.string.iptv_error_http, cause.responseCode)
                    else -> error.localizedMessage ?: context.getString(R.string.iptv_error_generic)
                }
                onError(errorDetails)
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayerRef = null
            playerViewRef?.player = null
            playerViewRef = null
            try { exoPlayer.stop() } catch (_: Exception) {}
            try { exoPlayer.clearVideoSurface() } catch (_: Exception) {}
            try { exoPlayer.release() } catch (_: Exception) {}
        }
    }

    var favoriteToastMessage by remember { mutableStateOf<String?>(null) }
    var isLongPressHandled by remember { mutableStateOf(false) }

    val addedToFavText = stringResource(R.string.iptv_added_to_favorites)
    val removedFromFavText = stringResource(R.string.iptv_removed_from_favorites)

    fun handleToggleFavoriteWithFeedback() {
        val willBeFavorite = !channel.isFavorite
        onToggleFavorite()
        controlsInteractionTick = System.currentTimeMillis()
        favoriteToastMessage = if (willBeFavorite) addedToFavText else removedFromFavText
    }

    val currentChannelIndex = remember(channel, channels) {
        val idx = channels.indexOfFirst { it.id == channel.id }
        if (idx >= 0) idx + 1 else 1
    }

    // Helper for single-row Info Menu focus navigation:
    // Left/Right moves cursor across the single row; Up/Down changes channels!
    fun Modifier.infoMenuSingleRowFocus(col: Int): Modifier {
        val prevCol = (col - 1 + 10) % 10
        val nextCol = (col + 1) % 10
        val leftCol = if (isRtl) nextCol else prevCol
        val rightCol = if (isRtl) prevCol else nextCol
        val leftTarget = menuFocusRequesters[leftCol]
        val rightTarget = menuFocusRequesters[rightCol]
        val selfTarget = menuFocusRequesters[col]
        return this
            .focusProperties {
                left = leftTarget
                right = rightTarget
                up = selfTarget
                down = selfTarget
            }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionLeft -> {
                            controlsInteractionTick = System.currentTimeMillis()
                            focusedMenuCol = leftCol
                            try { leftTarget.requestFocus() } catch (_: Exception) {}
                            true
                        }
                        Key.DirectionRight -> {
                            controlsInteractionTick = System.currentTimeMillis()
                            focusedMenuCol = rightCol
                            try { rightTarget.requestFocus() } catch (_: Exception) {}
                            true
                        }
                        Key.DirectionUp, Key.ChannelUp, Key.PageUp -> {
                            controlsInteractionTick = System.currentTimeMillis()
                            onNextChannel()
                            true
                        }
                        Key.DirectionDown, Key.ChannelDown, Key.PageDown -> {
                            controlsInteractionTick = System.currentTimeMillis()
                            onPrevChannel()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(rootFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                val nativeEvent = keyEvent.nativeKeyEvent
                val keyCode = nativeEvent.keyCode
                val isCenterKey = keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
                val isMenuKey = keyCode == KeyEvent.KEYCODE_MENU ||
                        keyCode == KeyEvent.KEYCODE_SETTINGS ||
                        keyCode == KeyEvent.KEYCODE_INFO ||
                        keyCode == KeyEvent.KEYCODE_GUIDE ||
                        keyCode == 82 ||
                        keyCode == 165 ||
                        keyCode == 172 ||
                        keyCode == 176

                val digitChar: Char? = when (keyCode) {
                    KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> '0'
                    KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> '1'
                    KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> '2'
                    KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> '3'
                    KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> '4'
                    KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> '5'
                    KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_NUMPAD_6 -> '6'
                    KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> '7'
                    KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> '8'
                    KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> '9'
                    else -> null
                }

                if (nativeEvent.action == KeyEvent.ACTION_DOWN) {
                    if (digitChar != null) {
                        if (numericBuffer.length < 4) {
                            numericBuffer += digitChar
                            numericDigitTick = System.currentTimeMillis()
                        }
                        return@onKeyEvent true
                    }
                    if (isCenterKey && numericBuffer.isNotEmpty()) {
                        val targetIdx = numericBuffer.toIntOrNull()?.minus(1)
                        numericBuffer = ""
                        if (targetIdx != null && targetIdx in channels.indices) {
                            onSelectChannel(channels[targetIdx])
                        }
                        return@onKeyEvent true
                    }
                    if (isMenuKey) {
                        showQuickBar = false
                        showChannelSwitchBanner = false
                        showControls = !showControls
                        controlsInteractionTick = System.currentTimeMillis()
                        return@onKeyEvent true
                    }
                    if (isCenterKey && !showQuickBar && !showControls) {
                        val isLong = nativeEvent.isLongPress || nativeEvent.repeatCount > 0
                        if (isLong && !isLongPressHandled) {
                            isLongPressHandled = true
                            handleToggleFavoriteWithFeedback()
                            return@onKeyEvent true
                        }
                    }
                } else if (nativeEvent.action == KeyEvent.ACTION_UP) {
                    if (isCenterKey && !showQuickBar && !showControls) {
                        if (isLongPressHandled) {
                            isLongPressHandled = false
                            return@onKeyEvent true
                        } else {
                            // Single press OK on fullscreen video opens the Channel Info Menu
                            showChannelSwitchBanner = false
                            showControls = true
                            controlsInteractionTick = System.currentTimeMillis()
                            return@onKeyEvent true
                        }
                    }
                }

                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false

                // 1. When Quick Channel Strip is visible: Left/Right moves on the strip, Up/Down is locked
                if (showQuickBar) {
                    quickBarInteractionTick = System.currentTimeMillis()
                    return@onKeyEvent when (keyEvent.key) {
                        Key.Back -> {
                            showQuickBar = false
                            try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
                            true
                        }
                        Key.DirectionUp, Key.DirectionDown -> true
                        else -> false
                    }
                }

                // 2. When Channel Info Menu (showControls) is visible on the channel:
                // Left/Right controls the cursor on the single-row menu; Up/Down switches channels!
                if (showControls) {
                    controlsInteractionTick = System.currentTimeMillis()
                    return@onKeyEvent when (keyEvent.key) {
                        Key.Back -> {
                            showControls = false
                            try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
                            true
                        }
                        Key.DirectionLeft, Key.DirectionRight -> {
                            try { menuFocusRequesters[focusedMenuCol.coerceIn(0, 9)].requestFocus() } catch (_: Exception) {}
                            true
                        }
                        Key.DirectionUp, Key.ChannelUp, Key.PageUp -> {
                            onNextChannel()
                            true
                        }
                        Key.DirectionDown, Key.ChannelDown, Key.PageDown -> {
                            onPrevChannel()
                            true
                        }
                        else -> false
                    }
                }

                // 3. Fullscreen video D-Pad shortcuts when Info Menu (showControls) is hidden
                when (keyEvent.key) {
                    Key.DirectionLeft, Key.DirectionRight -> {
                        showChannelSwitchBanner = false
                        showQuickBar = true
                        quickBarInteractionTick = System.currentTimeMillis()
                        true
                    }
                    Key.DirectionUp, Key.ChannelUp, Key.PageUp -> {
                        showControls = true
                        controlsInteractionTick = System.currentTimeMillis()
                        onNextChannel()
                        true
                    }
                    Key.DirectionDown, Key.ChannelDown, Key.PageDown -> {
                        showControls = true
                        controlsInteractionTick = System.currentTimeMillis()
                        onPrevChannel()
                        true
                    }
                    Key.MediaPlayPause -> {
                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        showControls = true
                        controlsInteractionTick = System.currentTimeMillis()
                        true
                    }
                    Key.MediaPlay -> {
                        exoPlayer.play()
                        showControls = true
                        controlsInteractionTick = System.currentTimeMillis()
                        true
                    }
                    Key.MediaPause -> {
                        exoPlayer.pause()
                        showControls = true
                        controlsInteractionTick = System.currentTimeMillis()
                        true
                    }
                    Key.Back -> {
                        onClose()
                        true
                    }
                    else -> false
                }
            }
    ) {
        // Player Surface
        AndroidView(
            factory = { ctx ->
                val view = android.view.LayoutInflater.from(ctx)
                    .inflate(R.layout.layout_iptv_player, null) as PlayerView
                view.apply {
                    keepScreenOn = true
                    player = exoPlayer
                    this.resizeMode = resizeMode
                    setEnableComposeSurfaceSyncWorkaround(true)
                    playerViewRef = this
                }
            },
            update = { view ->
                view.keepScreenOn = true
                view.resizeMode = resizeMode
                playerViewRef = view
            },
            onRelease = { view ->
                view.player = null
                if (playerViewRef === view) playerViewRef = null
            },
            modifier = Modifier.fillMaxSize()
        )

        // Sleek Buffering Indicator Pill
        if (isBuffering && playbackError == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(PlayerHudGlassBottom)
                        .border(1.5.dp, TvAccentGold.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 28.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = TvAccentGold,
                        strokeWidth = 3.5.dp,
                        modifier = Modifier.size(34.dp)
                    )
                    Column {
                        Text(
                            text = channel.name,
                            color = TvTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.iptv_loading),
                            color = TvAccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Passive Top-Start Channel Switch Pill (shown briefly when zapping with Up/Down in fullscreen)
        AnimatedVisibility(
            visible = showChannelSwitchBanner && !showControls && !showQuickBar && playbackError == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 36.dp, vertical = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PlayerHudGlassBottom)
                    .border(1.5.dp, TvAccentGold.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(TvAccentGold)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "CH %02d".format(currentChannelIndex),
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column {
                    Text(
                        text = channel.name,
                        color = TvTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (channel.groupTitle.isNotBlank()) {
                        Text(
                            text = channel.groupTitle,
                            color = TvAccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Playback Error Overlay (TV Remote Optimized)
        if (playbackError != null) {
            val retryFocusRequester = remember { FocusRequester() }
            LaunchedEffect(playbackError) {
                delay(80)
                try { retryFocusRequester.requestFocus() } catch (_: Exception) {}
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(560.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF141E33), Color(0xFF090E1A))
                            )
                        )
                        .border(1.5.dp, TvAccentRed.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
                        .padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(TvAccentRed.copy(alpha = 0.16f))
                            .border(1.5.dp, TvAccentRed.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = TvAccentRed,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = channel.name,
                        color = TvAccentGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.iptv_error_playback),
                        color = TvTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = playbackError,
                        color = TvTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlayerErrorActionButton(
                            icon = Icons.Default.Refresh,
                            label = stringResource(R.string.action_retry),
                            accentColor = TvAccentGold,
                            focusRequester = retryFocusRequester,
                            onClick = onRetry,
                            modifier = Modifier.weight(1f)
                        )
                        PlayerErrorActionButton(
                            icon = Icons.Default.KeyboardArrowUp,
                            label = stringResource(R.string.iptv_next_channel),
                            accentColor = TvAccentBlue,
                            onClick = onNextChannel,
                            modifier = Modifier.weight(1f)
                        )
                        PlayerErrorActionButton(
                            icon = Icons.Default.Close,
                            label = stringResource(R.string.action_cancel),
                            accentColor = TvAccentRed,
                            onClick = onClose,
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }
            }
        }

        // Top & Bottom Broadcast Cinema Info Menu (showControls)
        // When visible, D-Pad Left/Right & Up/Down move the cursor across its 2x5 button grid!
        AnimatedVisibility(
            visible = showControls && playbackError == null,
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Top Broadcast Status Pill Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 42.dp, vertical = 22.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: LIVE indicator + Channel Number + Group
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // LIVE Pulse Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isPaused) Color(0xFFD97706) else TvAccentRed)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Text(
                                text = if (isPaused) stringResource(R.string.iptv_paused_tag) else stringResource(R.string.iptv_live_tag),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Channel Number Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(PlayerHudGlassTop)
                                .border(1.dp, TvAccentGold.copy(alpha = 0.45f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "CH %02d / %02d".format(currentChannelIndex, channels.size.coerceAtLeast(1)),
                                color = TvAccentGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                            )
                        }

                        if (channel.groupTitle.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(PlayerHudGlassTop)
                                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = channel.groupTitle,
                                    color = TvTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Right: Stream Quality + Sleep Timer + Live Clock
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        streamQualityBadge?.let { badge ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(TvAccentGold.copy(alpha = 0.18f))
                                    .border(1.dp, TvAccentGold, RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = badge,
                                    color = TvAccentGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (sleepTimerMinutes > 0) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(TvAccentBlue.copy(alpha = 0.22f))
                                    .border(1.dp, TvAccentBlue, RoundedCornerShape(50))
                                    .padding(horizontal = 11.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = TvAccentBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${sleepTimerMinutes}m",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (currentClockText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(PlayerHudGlassTop)
                                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = currentClockText,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                        }
                    }
                }

                // 2. Bottom Cinema Floating OSD Info Menu Deck (2x5 TV Remote Matrix)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.76f),
                                    Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(horizontal = 42.dp, vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PlayerHudGlassBottom, PlayerHudGlassTop, PlayerHudGlassBottom)
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        TvAccentGold.copy(alpha = 0.45f),
                                        Color.White.copy(alpha = 0.16f),
                                        TvAccentBlue.copy(alpha = 0.35f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        // Top Row of Deck: Channel Identity + Menu Cursor Navigation Hint
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Channel Logo Box
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (channel.logoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = channel.logoUrl,
                                            contentDescription = channel.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.LiveTv,
                                            contentDescription = null,
                                            tint = TvAccentGold,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = channel.name,
                                            color = TvTextPrimary,
                                            fontSize = 21.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (channel.isFavorite) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = TvAccentGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (channel.groupTitle.isNotBlank()) {
                                            "CH %02d • %s".format(currentChannelIndex, channel.groupTitle)
                                        } else {
                                            "CH %02d".format(currentChannelIndex)
                                        },
                                        color = TvAccentGold.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Remote Quick-Keys Pill (Left/Right moves Menu Cursor, Up/Down changes Channels)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RemoteKeyBadge(keyLabel = "◀ ▶", actionLabel = stringResource(R.string.iptv_hint_menu_cursor))
                                RemoteKeyBadge(keyLabel = "▲ ▼", actionLabel = stringResource(R.string.iptv_hint_change_channels))
                                RemoteKeyBadge(keyLabel = "OK", actionLabel = stringResource(R.string.iptv_hint_select))
                                RemoteKeyBadge(keyLabel = "BACK", actionLabel = stringResource(R.string.iptv_hint_hide))
                            }
                        }

                        val epgInfo = remember(channel.id, channel.tvgId, epgPrograms) {
                            EpgManager.getChannelNowNext(channel, programsById = epgPrograms)
                        }
                        if (epgInfo.current != null || epgInfo.next != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    epgInfo.current?.let { nowProg ->
                                        Text(
                                            text = "${stringResource(R.string.iptv_epg_now)}: ${nowProg.title}",
                                            color = TvTextPrimary,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = epgInfo.formatTimeRange(),
                                            color = TvAccentGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                if (epgInfo.current != null) {
                                    LinearProgressIndicator(
                                        progress = { epgInfo.progressFraction() },
                                        color = TvAccentGold,
                                        trackColor = Color.White.copy(alpha = 0.14f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                    )
                                }
                                epgInfo.next?.let { nextProg ->
                                    Text(
                                        text = "${stringResource(R.string.iptv_epg_next)}: ${nextProg.title}",
                                        color = TvTextSecondary,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Single Horizontal Row of Interactive TV Remote Controls (10 items on 1 line)
                        val aspectLabel = when (resizeMode) {
                            AspectRatioFrameLayout.RESIZE_MODE_FILL -> stringResource(R.string.iptv_aspect_fill)
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> stringResource(R.string.iptv_aspect_zoom)
                            else -> stringResource(R.string.iptv_aspect_fit)
                        }
                        val timerOptions = listOf(0, 15, 30, 60, 90)
                        val timerLabel = if (sleepTimerMinutes == 0) {
                            stringResource(R.string.iptv_sleep_off)
                        } else {
                            "${sleepTimerMinutes}m"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // [0] Play / Pause (Initial Cursor Target)
                            PlayerControlHudButton(
                                icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                label = if (isPaused) stringResource(R.string.action_play) else stringResource(R.string.action_pause),
                                isActive = isPaused,
                                focusRequester = menuFocusRequesters[0],
                                onFocused = {
                                    focusedMenuCol = 0
                                    controlsInteractionTick = System.currentTimeMillis()
                                },
                                onClick = {
                                    controlsInteractionTick = System.currentTimeMillis()
                                    if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                        isPaused = true
                                    } else {
                                        exoPlayer.play()
                                        isPaused = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .infoMenuSingleRowFocus(col = 0)
                            )

                            // [1] Previous Channel
                            PlayerControlHudButton(
                                icon = Icons.Default.KeyboardArrowDown,
                                label = stringResource(R.string.iptv_prev_channel),
                                focusRequester = menuFocusRequesters[1],
                                onFocused = {
                                    focusedMenuCol = 1
                                    controlsInteractionTick = System.currentTimeMillis()
                                },
                                onClick = {
                                    controlsInteractionTick = System.currentTimeMillis()
                                    onPrevChannel()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .infoMenuSingleRowFocus(col = 1)
                            )

                            // [2] Next Channel
                            PlayerControlHudButton(
                                icon = Icons.Default.KeyboardArrowUp,
                                label = stringResource(R.string.iptv_next_channel),
                                focusRequester = menuFocusRequesters[2],
                                onFocused = {
                                    focusedMenuCol = 2
                                    controlsInteractionTick = System.currentTimeMillis()
                                },
                                onClick = {
                                    controlsInteractionTick = System.currentTimeMillis()
                                    onNextChannel()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .infoMenuSingleRowFocus(col = 2)
                            )

                            // [3] Quick Channel Carousel Strip
                            PlayerControlHudButton(
                                icon = Icons.Default.LiveTv,
                                label = stringResource(R.string.iptv_quick_channels),
                                accentColor = TvAccentBlue,
                                focusRequester = menuFocusRequesters[3],
                                onFocused = {
                                    focusedMenuCol = 3
                                    controlsInteractionTick = System.currentTimeMillis()
                                },
                                onClick = {
                                    showControls = false
                                    showQuickBar = true
                                    quickBarInteractionTick = System.currentTimeMillis()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .infoMenuSingleRowFocus(col = 3)
                            )

                            // [4] Favorite Toggle
                            PlayerControlHudButton(
                                icon = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                label = stringResource(R.string.iptv_toggle_favorite),
                                isActive = channel.isFavorite,
                                focusRequester = menuFocusRequesters[4],
                                onFocused = {
                                    focusedMenuCol = 4
                                    controlsInteractionTick = System.currentTimeMillis()
                                },
                                onClick = {
                                    controlsInteractionTick = System.currentTimeMillis()
                                    handleToggleFavoriteWithFeedback()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .infoMenuSingleRowFocus(col = 4)
                            )

                            // [5] Aspect Ratio Switcher
                            PlayerControlHudButton(
                                icon = Icons.Default.AspectRatio,
                                label = aspectLabel,
                                isActive = resizeMode != AspectRatioFrameLayout.RESIZE_MODE_FIT,
                                focusRequester = menuFocusRequesters[5],
                                onFocused = {
                                    focusedMenuCol = 5
                                    controlsInteractionTick = System.currentTimeMillis()
                                },
                                onClick = {
                                    controlsInteractionTick = System.currentTimeMillis()
                                    resizeMode = when (resizeMode) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .infoMenuSingleRowFocus(col = 5)
                            )

                            // [6] Sleep Timer Switcher
                            val sleepSetTemplate = stringResource(R.string.iptv_sleep_toast_set)
                            val sleepOffToast = stringResource(R.string.iptv_sleep_toast_off)
                            PlayerControlHudButton(
                                icon = Icons.Default.Timer,
                                label = timerLabel,
                                isActive = sleepTimerMinutes > 0,
                                focusRequester = menuFocusRequesters[6],
                                onFocused = {
                                    focusedMenuCol = 6
                                    controlsInteractionTick = System.currentTimeMillis()
                                },
                                onClick = {
                                    controlsInteractionTick = System.currentTimeMillis()
                                    val idx = timerOptions.indexOf(sleepTimerMinutes)
                                    val nextVal = timerOptions[(idx + 1) % timerOptions.size]
                                    sleepTimerMinutes = nextVal
                                    favoriteToastMessage = if (nextVal > 0) {
                                        sleepSetTemplate.format(nextVal)
                                    } else {
                                        sleepOffToast
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .infoMenuSingleRowFocus(col = 6)
                            )

                            // [7] Reload Stream
                            PlayerControlHudButton(
                                icon = Icons.Default.Refresh,
                                label = stringResource(R.string.action_retry),
                                focusRequester = menuFocusRequesters[7],
                                onFocused = {
                                    focusedMenuCol = 7
                                    controlsInteractionTick = System.currentTimeMillis()
                                },
                                onClick = {
                                    controlsInteractionTick = System.currentTimeMillis()
                                    onRetry()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .infoMenuSingleRowFocus(col = 7)
                            )

                            // [8] IPTV Settings Shortcut (or Hide Menu if null)
                            PlayerControlHudButton(
                                icon = if (onOpenIptvSettings != null) Icons.Default.Settings else Icons.Default.VisibilityOff,
                                label = if (onOpenIptvSettings != null) {
                                    stringResource(R.string.settings_open_iptv_settings)
                                } else {
                                    stringResource(R.string.iptv_hide_menu)
                                },
                                accentColor = TvAccentBlue,
                                focusRequester = menuFocusRequesters[8],
                                onFocused = {
                                    focusedMenuCol = 8
                                    controlsInteractionTick = System.currentTimeMillis()
                                },
                                onClick = {
                                    if (onOpenIptvSettings != null) {
                                        onOpenIptvSettings()
                                    } else {
                                        showControls = false
                                        try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .infoMenuSingleRowFocus(col = 8)
                            )

                            // [9] Exit Player
                            PlayerControlHudButton(
                                icon = Icons.Default.Close,
                                label = stringResource(R.string.action_cancel),
                                accentColor = TvAccentRed,
                                focusRequester = menuFocusRequesters[9],
                                onFocused = {
                                    focusedMenuCol = 9
                                    controlsInteractionTick = System.currentTimeMillis()
                                },
                                onClick = onClose,
                                modifier = Modifier
                                    .weight(1f)
                                    .infoMenuSingleRowFocus(col = 9)
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Quick Channel Carousel Strip (Triggered by Left/Right in fullscreen or Quick Channels button)
        AnimatedVisibility(
            visible = showQuickBar,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val listState = rememberLazyListState()
            val currentPlayingIndex = channels.indexOfFirst { it.id == channel.id }.coerceAtLeast(0)

            LaunchedEffect(showQuickBar, channel.id) {
                if (showQuickBar && channels.isNotEmpty()) {
                    listState.scrollToItem((currentPlayingIndex - 2).coerceAtLeast(0))
                    delay(80)
                    val targetChannel = channels.getOrNull(currentPlayingIndex)
                    if (targetChannel != null) {
                        try {
                            quickStripFocusRequesters[targetChannel.id]?.requestFocus()
                        } catch (_: Exception) {}
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f),
                                Color.Black.copy(alpha = 0.96f)
                            )
                        )
                    )
                    .padding(horizontal = 36.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.iptv_quick_channels),
                            color = TvAccentGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${channels.size}",
                                color = TvTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.iptv_quick_channels_hint),
                        color = TvTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(channels, key = { _, ch -> ch.id }) { index, ch ->
                        val isCurrent = (ch.id == channel.id)
                        val itemFr = quickStripFocusRequesters.getOrPut(ch.id) { FocusRequester() }
                        TvFocusableCard(
                            onClick = {
                                onSelectChannel(ch)
                                showQuickBar = false
                                try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
                            },
                            onFocused = {
                                quickBarInteractionTick = System.currentTimeMillis()
                            },
                            focusRequester = itemFr,
                            scaleUpRatio = 1.08f,
                            elevation = 0.dp,
                            cardBackgroundColor = Color.Transparent,
                            useDefaultBorder = false,
                            modifier = Modifier
                                .width(155.dp)
                                .height(108.dp)
                                .focusProperties {
                                    up = itemFr
                                    down = itemFr
                                }
                        ) { isFocused ->
                            val tileShape = RoundedCornerShape(18.dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(tileShape)
                                    .background(
                                        when {
                                            isFocused -> TvAccentGold.copy(alpha = 0.24f)
                                            isCurrent -> TvAccentBlue.copy(alpha = 0.28f)
                                            else -> PlayerHudGlassTop
                                        }
                                    )
                                    .border(
                                        width = if (isFocused) 2.5.dp else if (isCurrent) 1.5.dp else 1.dp,
                                        color = when {
                                            isFocused -> TvAccentGold
                                            isCurrent -> TvAccentBlue
                                            else -> Color.White.copy(alpha = 0.12f)
                                        },
                                        shape = tileShape
                                    )
                                    .padding(10.dp)
                            ) {
                                // Channel Index Top-Start
                                Text(
                                    text = "#${index + 1}",
                                    color = if (isFocused) TvAccentGold else TvTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.align(Alignment.TopStart)
                                )

                                // LIVE badge Top-End if current
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TvAccentRed)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.iptv_live_tag),
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                } else if (ch.isFavorite) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = TvAccentGold,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(13.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (ch.logoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = ch.logoUrl,
                                            contentDescription = ch.name,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.LiveTv,
                                            contentDescription = null,
                                            tint = if (isCurrent || isFocused) TvAccentGold else TvTextPrimary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = ch.name,
                                        color = if (isCurrent || isFocused) Color.White else TvTextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isCurrent || isFocused) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Numeric Channel Zapping OSD Badge (0-9)
        AnimatedVisibility(
            visible = numericBuffer.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 36.dp, vertical = 28.dp)
        ) {
            val previewChannel = numericBuffer.toIntOrNull()?.minus(1)?.let { channels.getOrNull(it) }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PlayerHudGlassBottom)
                    .border(1.5.dp, TvAccentGold, RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CH $numericBuffer",
                    color = TvAccentGold,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                )
                if (previewChannel != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = previewChannel.name,
                        color = TvTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Floating Favorite / Sleep Timer Toast Feedback Overlay
        AnimatedVisibility(
            visible = favoriteToastMessage != null || audioFallbackNotice != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 78.dp)
        ) {
            val activeNotice = favoriteToastMessage ?: audioFallbackNotice
            activeNotice?.let { msg ->
                LaunchedEffect(msg) {
                    delay(3000)
                    if (favoriteToastMessage == msg) favoriteToastMessage = null
                    if (audioFallbackNotice == msg) audioFallbackNotice = null
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(PlayerHudGlassBottom)
                        .border(1.5.dp, TvAccentGold, RoundedCornerShape(22.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (favoriteToastMessage != null) Icons.Default.Star else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteKeyBadge(
    keyLabel: String,
    actionLabel: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(TvAccentGold.copy(alpha = 0.22f))
                .border(1.dp, TvAccentGold.copy(alpha = 0.6f), RoundedCornerShape(5.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = keyLabel,
                color = TvAccentGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = actionLabel,
            color = TvTextSecondary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun PlayerControlHudButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    accentColor: Color = TvAccentGold,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onFocused: () -> Unit = {},
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        onFocused = onFocused,
        focusRequester = focusRequester,
        scaleUpRatio = 1.07f,
        elevation = 0.dp,
        cardBackgroundColor = Color.Transparent,
        useDefaultBorder = false,
        modifier = modifier.height(54.dp)
    ) { isFocused ->
        val shape = RoundedCornerShape(14.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    when {
                        isFocused -> accentColor
                        isActive -> accentColor.copy(alpha = 0.22f)
                        else -> Color.White.copy(alpha = 0.08f)
                    }
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = when {
                        isFocused -> Color.White
                        isActive -> accentColor.copy(alpha = 0.8f)
                        else -> Color.White.copy(alpha = 0.12f)
                    },
                    shape = shape
                )
                .padding(horizontal = 5.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = when {
                    isFocused -> if (accentColor == TvAccentRed) Color.White else Color.Black
                    isActive -> accentColor
                    else -> TvTextPrimary
                },
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                color = when {
                    isFocused -> if (accentColor == TvAccentRed) Color.White else Color.Black
                    isActive -> accentColor
                    else -> TvTextPrimary
                },
                fontSize = 10.sp,
                fontWeight = if (isFocused || isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlayerErrorActionButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    TvFocusableCard(
        onClick = onClick,
        focusRequester = focusRequester,
        scaleUpRatio = 1.06f,
        elevation = 0.dp,
        cardBackgroundColor = Color.Transparent,
        useDefaultBorder = false,
        modifier = modifier.height(48.dp)
    ) { isFocused ->
        val shape = RoundedCornerShape(14.dp)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    if (isFocused) accentColor else Color.White.copy(alpha = 0.08f)
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Color.White else accentColor.copy(alpha = 0.45f),
                    shape = shape
                )
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isFocused) (if (accentColor == TvAccentRed) Color.White else Color.Black) else accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (isFocused) (if (accentColor == TvAccentRed) Color.White else Color.Black) else TvTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
