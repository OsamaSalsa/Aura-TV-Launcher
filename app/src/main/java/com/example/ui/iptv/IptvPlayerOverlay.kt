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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PictureInPictureAlt
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
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.mediacodec.MediaCodecRenderer
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
    onMinimizeToPiP: (() -> Unit)? = null,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var isBuffering by remember { mutableStateOf(true) }
    var isPaused by remember { mutableStateOf(false) }

    // When showControls == true (Channel Info Menu is visible), D-Pad Left/Right & Up/Down
    // strictly control the cursor inside the Info Menu row and switch channels cleanly.
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
    val menuButtonsCount = if (onMinimizeToPiP != null) 11 else 10
    val menuFocusRequesters = remember(menuButtonsCount) { Array(menuButtonsCount) { FocusRequester() } }
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

    // Whenever the Channel Info Menu (showControls) is visible, keep the D-Pad cursor strictly on the bottom options row
    LaunchedEffect(showControls, playbackError) {
        if (showControls && playbackError == null) {
            delay(65)
            try {
                menuFocusRequesters[focusedMenuCol.coerceIn(0, menuButtonsCount - 1)].requestFocus()
            } catch (_: Exception) {}
        } else if (!showControls && !showQuickBar && playbackError == null) {
            try {
                rootFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    var audioFallbackNotice by remember { mutableStateOf<String?>(null) }
    var audioDisabledForChannel by remember { mutableStateOf(false) }
    var useTextureViewFallback by remember { mutableStateOf(false) }
    var preferSoftwareVideoDecoder by remember { mutableStateOf(false) }
    var hasRenderedFirstFrame by remember(channel) { mutableStateOf(false) }
    var blackScreenFallbackAttempt by remember(channel) { mutableStateOf(0) }
    var manualReloadTick by remember { mutableStateOf(0) }

    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    var exoPlayerRef by remember { mutableStateOf<ExoPlayer?>(null) }

    val exoPlayer: ExoPlayer = remember(preferSoftwareVideoDecoder) {
        buildResilientIptvExoPlayer(
            context = context,
            preferSoftwareVideoDecoder = preferSoftwareVideoDecoder,
            mainHandler = mainHandler,
            onUnsupportedAudioDetected = { player, sampleMime ->
                audioDisabledForChannel = true
                audioFallbackNotice = context.getString(
                    R.string.iptv_audio_unsupported_codec,
                    sampleMime
                )
                player.trackSelectionParameters = player.trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                    .build()
            },
            onPlayerCreated = { created ->
                exoPlayerRef = created
            }
        )
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
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_START -> {
                    if (stoppedByLifecycle) {
                        stoppedByLifecycle = false
                        try {
                            playerViewRef?.let { pv ->
                                if (pv.player !== exoPlayer) {
                                    pv.player = exoPlayer
                                }
                                when (val sv = pv.videoSurfaceView) {
                                    is android.view.SurfaceView -> exoPlayer.setVideoSurfaceView(sv)
                                    is android.view.TextureView -> exoPlayer.setVideoTextureView(sv)
                                }
                            }
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

    // Automatic black-screen watchdog: if audio plays cleanly while a video track exists yet zero
    // frames render after 3.8s, automatically step through TextureView and Software Decoder fallbacks.
    LaunchedEffect(
        channel,
        exoPlayer,
        isBuffering,
        isPaused,
        hasRenderedFirstFrame,
        playbackError,
        blackScreenFallbackAttempt
    ) {
        if (playbackError == null && !isBuffering && !isPaused && !hasRenderedFirstFrame && blackScreenFallbackAttempt < 2) {
            delay(3800L)
            val hasVideoTrack = try {
                exoPlayer.currentTracks.groups.any { it.type == C.TRACK_TYPE_VIDEO } || exoPlayer.videoFormat != null
            } catch (_: Exception) {
                false
            }
            if (hasVideoTrack && !hasRenderedFirstFrame && exoPlayer.playbackState == Player.STATE_READY && exoPlayer.playWhenReady) {
                if (blackScreenFallbackAttempt == 0 && !useTextureViewFallback) {
                    blackScreenFallbackAttempt = 1
                    useTextureViewFallback = true
                } else if (!preferSoftwareVideoDecoder) {
                    blackScreenFallbackAttempt = 2
                    preferSoftwareVideoDecoder = true
                } else {
                    blackScreenFallbackAttempt = 2
                }
            }
        }
    }

    // Ensure the active PlayerView and its underlying SurfaceView/TextureView remain attached
    // cleanly whenever surface fallback mode or ExoPlayer instance changes.
    LaunchedEffect(useTextureViewFallback, exoPlayer) {
        delay(60L)
        try {
            playerViewRef?.let { pv ->
                if (pv.player !== exoPlayer) {
                    pv.player = exoPlayer
                }
                when (val sv = pv.videoSurfaceView) {
                    is android.view.SurfaceView -> exoPlayer.setVideoSurfaceView(sv)
                    is android.view.TextureView -> exoPlayer.setVideoTextureView(sv)
                }
            }
        } catch (_: Exception) {}
    }

    DisposableEffect(channel, exoPlayer, manualReloadTick) {
        isBuffering = true
        isPaused = false
        hasRenderedFirstFrame = false
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

            override fun onRenderedFirstFrame() {
                hasRenderedFirstFrame = true
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

                val isVideoRendererFailure = exoError != null &&
                    exoError.type == ExoPlaybackException.TYPE_RENDERER &&
                    !isAudioRendererFailure &&
                    (
                        exoError.rendererFormat?.sampleMimeType?.startsWith("video/") == true ||
                            MimeTypes.isVideo(exoError.rendererFormat?.sampleMimeType) ||
                            (exoError.rendererIndex in 0 until exoPlayer.rendererCount &&
                                exoPlayer.getRendererType(exoError.rendererIndex) == C.TRACK_TYPE_VIDEO)
                        )

                if (isVideoRendererFailure && !preferSoftwareVideoDecoder) {
                    preferSoftwareVideoDecoder = true
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

    DisposableEffect(exoPlayer) {
        onDispose {
            if (exoPlayerRef === exoPlayer) {
                exoPlayerRef = null
            }
            if (playerViewRef?.player === exoPlayer) {
                playerViewRef?.player = null
            }
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

    // Helper for Info Menu bottom options row focus navigation:
    // Left/Right moves cursor strictly across the 10 option buttons;
    // Up/Down & CH+/CH- switch channels without ever leaving the bottom row.
    fun Modifier.infoMenuSingleRowFocus(col: Int): Modifier {
        val prevCol = (col - 1 + menuButtonsCount) % menuButtonsCount
        val nextCol = (col + 1) % menuButtonsCount
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
            .background(if (useTextureViewFallback) Color.Black else Color.Transparent)
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
                        }
                        return@onKeyEvent true
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
                if (showControls) {
                    controlsInteractionTick = System.currentTimeMillis()
                    return@onKeyEvent when (keyEvent.key) {
                        Key.Back -> {
                            showControls = false
                            try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
                            true
                        }
                        Key.DirectionLeft, Key.DirectionRight -> {
                            try { menuFocusRequesters[focusedMenuCol.coerceIn(0, menuButtonsCount - 1)].requestFocus() } catch (_: Exception) {}
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
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            try { menuFocusRequesters[focusedMenuCol.coerceIn(0, menuButtonsCount - 1)].requestFocus() } catch (_: Exception) {}
                            true
                        }
                        else -> false
                    }
                }

                // 3. Fullscreen video D-Pad shortcuts when Info Menu (showControls) is hidden
                when (keyEvent.key) {
                    Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                        showChannelSwitchBanner = false
                        showControls = true
                        controlsInteractionTick = System.currentTimeMillis()
                        true
                    }
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
        // Player Surface (SurfaceView primary for hardware TV decoders; TextureView automatic fallback)
        androidx.compose.runtime.key(useTextureViewFallback) {
            AndroidView(
                factory = { ctx ->
                    val layoutId = if (useTextureViewFallback) {
                        R.layout.layout_iptv_player_texture
                    } else {
                        R.layout.layout_iptv_player
                    }
                    val view = android.view.LayoutInflater.from(ctx)
                        .inflate(layoutId, null) as PlayerView
                    view.apply {
                        keepScreenOn = true
                        isFocusable = false
                        isFocusableInTouchMode = false
                        this.resizeMode = resizeMode
                        setEnableComposeSurfaceSyncWorkaround(true)
                        player = exoPlayer
                        playerViewRef = this
                    }
                },
                update = { view ->
                    view.keepScreenOn = true
                    view.isFocusable = false
                    view.isFocusableInTouchMode = false
                    view.resizeMode = resizeMode
                    if (view.player !== exoPlayer) {
                        view.player = exoPlayer
                    }
                    playerViewRef = view
                },
                onRelease = { view ->
                    view.player = null
                    if (playerViewRef === view) playerViewRef = null
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Touch-Screen Video Tap Detector:
        // When watching a channel on a touch device, tapping the screen opens the Info Bar
        // (with embedded channel navigation). Tapping outside when Quick Bar is open closes Quick Bar.
        if (!showControls && playbackError == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusProperties { canFocus = false }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (showQuickBar) {
                            showQuickBar = false
                        } else {
                            showChannelSwitchBanner = false
                            showControls = true
                            controlsInteractionTick = System.currentTimeMillis()
                        }
                    }
            )
        }

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
        // Triggered by touching the screen or pressing OK on the remote!
        AnimatedVisibility(
            visible = showControls && playbackError == null,
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusProperties { canFocus = false }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Touching the video area outside the Info Bar dismisses it
                        showControls = false
                        try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
                    }
            ) {
                // 1. Top Broadcast Status Pill Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 28.dp, vertical = 14.dp),
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

                // 2. Compact Bottom Cinema Floating OSD Info Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.58f),
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                        .padding(horizontal = 28.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .focusProperties { canFocus = false }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // Keep Info Bar alive when interacting inside it via touch
                                controlsInteractionTick = System.currentTimeMillis()
                            }
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xE00C1426), Color(0xF2060A14))
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        TvAccentGold.copy(alpha = 0.35f),
                                        Color.White.copy(alpha = 0.14f),
                                        TvAccentBlue.copy(alpha = 0.30f)
                                    )
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        val epgInfo = remember(channel.id, channel.tvgId, epgPrograms) {
                            EpgManager.getChannelNowNext(channel, programsById = epgPrograms)
                        }

                        // Top Row: Compact Channel Identity + Inline EPG + Touch Prev/Next
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
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                                        .padding(4.dp),
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
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = channel.name,
                                            color = TvTextPrimary,
                                            fontSize = 16.5.sp,
                                            lineHeight = 19.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (channel.isFavorite) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = TvAccentGold,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (channel.groupTitle.isNotBlank()) {
                                            "CH %02d • %s".format(currentChannelIndex, channel.groupTitle)
                                        } else {
                                            "CH %02d".format(currentChannelIndex)
                                        },
                                        color = TvAccentGold.copy(alpha = 0.88f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (epgInfo.current != null || epgInfo.next != null) {
                                Column(
                                    modifier = Modifier
                                        .weight(0.85f)
                                        .padding(horizontal = 10.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(0.75.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        epgInfo.current?.let { nowProg ->
                                            Text(
                                                text = nowProg.title,
                                                color = TvTextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = epgInfo.formatTimeRange(),
                                                color = TvAccentGold,
                                                fontSize = 10.sp,
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
                                                .height(2.5.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                        )
                                    }
                                    epgInfo.next?.let { nextProg ->
                                        Text(
                                            text = "${stringResource(R.string.iptv_epg_next)}: ${nextProg.title}",
                                            color = TvTextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Compact Touch Prev/Next Quick Channel Stepper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(0.75.dp, TvAccentGold.copy(alpha = 0.4f), CircleShape)
                                        .focusProperties { canFocus = false }
                                        .clickable {
                                            controlsInteractionTick = System.currentTimeMillis()
                                            onPrevChannel()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRtl) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
                                        contentDescription = stringResource(R.string.iptv_prev_channel),
                                        tint = TvAccentGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(0.75.dp, TvAccentGold.copy(alpha = 0.4f), CircleShape)
                                        .focusProperties { canFocus = false }
                                        .clickable {
                                            controlsInteractionTick = System.currentTimeMillis()
                                            onNextChannel()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRtl) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                                        contentDescription = stringResource(R.string.iptv_next_channel),
                                        tint = TvAccentGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Display-only & Touch-scrollable Compact Channel Strip
                        if (channels.isNotEmpty()) {
                            val infoStripListState = rememberLazyListState()
                            val currentPlayingIdx = remember(channel.id, channels) {
                                channels.indexOfFirst { it.id == channel.id }.coerceAtLeast(0)
                            }

                            LaunchedEffect(showControls, channel.id, channels.size) {
                                if (showControls && channels.isNotEmpty()) {
                                    val targetScrollIdx = (currentPlayingIdx - 2).coerceAtLeast(0)
                                    infoStripListState.animateScrollToItem(targetScrollIdx)
                                }
                            }

                            LaunchedEffect(infoStripListState.isScrollInProgress) {
                                while (infoStripListState.isScrollInProgress) {
                                    controlsInteractionTick = System.currentTimeMillis()
                                    delay(250L)
                                }
                            }

                            Spacer(modifier = Modifier.height(7.dp))

                            LazyRow(
                                state = infoStripListState,
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusProperties { canFocus = false }
                            ) {
                                itemsIndexed(channels, key = { _, ch -> ch.id }) { index, ch ->
                                    val isCurrent = (ch.id == channel.id)
                                    val pillShape = RoundedCornerShape(11.dp)
                                    Row(
                                        modifier = Modifier
                                            .widthIn(min = 148.dp, max = 280.dp)
                                            .height(50.dp)
                                            .clip(pillShape)
                                            .focusProperties { canFocus = false }
                                            .clickable {
                                                controlsInteractionTick = System.currentTimeMillis()
                                                if (ch.id != channel.id) {
                                                    onSelectChannel(ch)
                                                }
                                            }
                                            .background(
                                                if (isCurrent) TvAccentGold.copy(alpha = 0.20f)
                                                else Color.White.copy(alpha = 0.06f)
                                            )
                                            .border(
                                                width = if (isCurrent) 1.5.dp else 0.75.dp,
                                                color = if (isCurrent) TvAccentGold else Color.White.copy(alpha = 0.12f),
                                                shape = pillShape
                                            )
                                            .padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(Color.White.copy(alpha = 0.08f))
                                                .padding(3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (ch.logoUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = ch.logoUrl,
                                                    contentDescription = ch.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Fit
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.LiveTv,
                                                    contentDescription = null,
                                                    tint = if (isCurrent) TvAccentGold else TvTextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.widthIn(max = 225.dp),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    color = if (isCurrent) TvAccentGold else TvTextSecondary,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                if (isCurrent) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(TvAccentRed)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = ch.name,
                                                color = if (isCurrent) Color.White else TvTextPrimary,
                                                fontSize = 11.sp,
                                                lineHeight = 12.5.sp,
                                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sleek Compact Floating Control Icon Dock + Focused Action Label Pill
                        val aspectFitLabel = stringResource(R.string.iptv_aspect_fit)
                        val aspectFillLabel = stringResource(R.string.iptv_aspect_fill)
                        val aspectZoomLabel = stringResource(R.string.iptv_aspect_zoom)
                        val aspectLabel = when (resizeMode) {
                            AspectRatioFrameLayout.RESIZE_MODE_FILL -> aspectFillLabel
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> aspectZoomLabel
                            else -> aspectFitLabel
                        }
                        val timerOptions = listOf(0, 15, 30, 60, 90)
                        val timerLabel = if (sleepTimerMinutes == 0) {
                            stringResource(R.string.iptv_sleep_off)
                        } else {
                            "${sleepTimerMinutes}m"
                        }
                        val sleepSetTemplate = stringResource(R.string.iptv_sleep_toast_set)
                        val sleepOffToast = stringResource(R.string.iptv_sleep_toast_off)

                        val settingsIdx = if (onMinimizeToPiP != null) 9 else 8
                        val exitIdx = if (onMinimizeToPiP != null) 10 else 9
                        val focusedActionLabel = when (focusedMenuCol) {
                            0 -> if (isPaused) stringResource(R.string.action_play) else stringResource(R.string.action_pause)
                            1 -> stringResource(R.string.iptv_prev_channel)
                            2 -> stringResource(R.string.iptv_next_channel)
                            3 -> stringResource(R.string.iptv_quick_channels)
                            4 -> stringResource(R.string.iptv_toggle_favorite)
                            5 -> aspectLabel
                            6 -> timerLabel
                            7 -> stringResource(R.string.action_retry)
                            8 -> if (onMinimizeToPiP != null) {
                                "PiP"
                            } else if (onOpenIptvSettings != null) {
                                stringResource(R.string.settings_open_iptv_settings)
                            } else {
                                stringResource(R.string.iptv_hide_menu)
                            }
                            settingsIdx -> if (onOpenIptvSettings != null) {
                                stringResource(R.string.settings_open_iptv_settings)
                            } else {
                                stringResource(R.string.iptv_hide_menu)
                            }
                            else -> stringResource(R.string.action_cancel)
                        }
                        val focusedAccentColor = when (focusedMenuCol) {
                            3, 8, settingsIdx -> TvAccentBlue
                            exitIdx -> TvAccentRed
                            else -> TvAccentGold
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.045f))
                                    .border(0.75.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // [0] Play / Pause
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
                                    modifier = Modifier.infoMenuSingleRowFocus(col = 0)
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
                                    modifier = Modifier.infoMenuSingleRowFocus(col = 1)
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
                                    modifier = Modifier.infoMenuSingleRowFocus(col = 2)
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
                                    modifier = Modifier.infoMenuSingleRowFocus(col = 3)
                                )

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .width(1.dp)
                                        .height(18.dp)
                                        .background(Color.White.copy(alpha = 0.12f))
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
                                    modifier = Modifier.infoMenuSingleRowFocus(col = 4)
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
                                        val nextMode = when (resizeMode) {
                                            AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                            AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        }
                                        resizeMode = nextMode
                                        favoriteToastMessage = when (nextMode) {
                                            AspectRatioFrameLayout.RESIZE_MODE_FILL -> aspectFillLabel
                                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> aspectZoomLabel
                                            else -> aspectFitLabel
                                        }
                                    },
                                    modifier = Modifier.infoMenuSingleRowFocus(col = 5)
                                )

                                // [6] Sleep Timer Switcher
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
                                    modifier = Modifier.infoMenuSingleRowFocus(col = 6)
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
                                        if (!hasRenderedFirstFrame) {
                                            if (!useTextureViewFallback) {
                                                useTextureViewFallback = true
                                            } else if (!preferSoftwareVideoDecoder) {
                                                preferSoftwareVideoDecoder = true
                                            } else {
                                                useTextureViewFallback = false
                                                preferSoftwareVideoDecoder = false
                                                manualReloadTick++
                                                onRetry()
                                            }
                                        } else {
                                            manualReloadTick++
                                            onRetry()
                                        }
                                    },
                                    modifier = Modifier.infoMenuSingleRowFocus(col = 7)
                                )

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .width(1.dp)
                                        .height(18.dp)
                                        .background(Color.White.copy(alpha = 0.12f))
                                )

                                // [8] Picture-in-Picture (Mini Player) if available
                                if (onMinimizeToPiP != null) {
                                    PlayerControlHudButton(
                                        icon = Icons.Default.PictureInPictureAlt,
                                        label = "PiP",
                                        accentColor = TvAccentBlue,
                                        focusRequester = menuFocusRequesters[8],
                                        onFocused = {
                                            focusedMenuCol = 8
                                            controlsInteractionTick = System.currentTimeMillis()
                                        },
                                        onClick = onMinimizeToPiP,
                                        modifier = Modifier.infoMenuSingleRowFocus(col = 8)
                                    )
                                }

                                // [8 or 9] IPTV Settings Shortcut
                                PlayerControlHudButton(
                                    icon = if (onOpenIptvSettings != null) Icons.Default.Settings else Icons.Default.VisibilityOff,
                                    label = if (onOpenIptvSettings != null) {
                                        stringResource(R.string.settings_open_iptv_settings)
                                    } else {
                                        stringResource(R.string.iptv_hide_menu)
                                    },
                                    accentColor = TvAccentBlue,
                                    focusRequester = menuFocusRequesters[settingsIdx],
                                    onFocused = {
                                        focusedMenuCol = settingsIdx
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
                                    modifier = Modifier.infoMenuSingleRowFocus(col = settingsIdx)
                                )

                                // [9 or 10] Exit Player
                                PlayerControlHudButton(
                                    icon = Icons.Default.Close,
                                    label = stringResource(R.string.action_cancel),
                                    accentColor = TvAccentRed,
                                    focusRequester = menuFocusRequesters[exitIdx],
                                    onFocused = {
                                        focusedMenuCol = exitIdx
                                        controlsInteractionTick = System.currentTimeMillis()
                                    },
                                    onClick = onClose,
                                    modifier = Modifier.infoMenuSingleRowFocus(col = exitIdx)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(focusedAccentColor.copy(alpha = 0.14f))
                                    .border(0.75.dp, focusedAccentColor.copy(alpha = 0.42f), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(focusedAccentColor)
                                )
                                Text(
                                    text = focusedActionLabel,
                                    color = TvTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Quick Channel Carousel Strip
        AnimatedVisibility(
            visible = showQuickBar,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            IptvQuickChannelCarouselStrip(
                channel = channel,
                channels = channels,
                showQuickBar = showQuickBar,
                quickStripFocusRequesters = quickStripFocusRequesters,
                onSelectChannel = onSelectChannel,
                onInteraction = {
                    quickBarInteractionTick = System.currentTimeMillis()
                },
                onDismiss = {
                    showQuickBar = false
                    try { rootFocusRequester.requestFocus() } catch (_: Exception) {}
                }
            )
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

