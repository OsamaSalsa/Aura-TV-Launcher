package com.example.ui.iptv

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Timer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.IptvChannel
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun IptvPlayerOverlay(
    channel: IptvChannel,
    channels: List<IptvChannel> = emptyList(),
    playbackError: String?,
    onNextChannel: () -> Unit,
    onPrevChannel: () -> Unit,
    onSelectChannel: (IptvChannel) -> Unit = {},
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit,
    onError: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var showQuickBar by remember { mutableStateOf(false) }
    var quickBarLastInteractionTime by remember { mutableStateOf(0L) }
    var streamQualityBadge by remember { mutableStateOf<String?>(null) }
    var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var sleepTimerMinutes by remember { mutableStateOf(0) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(sleepTimerMinutes) {
        if (sleepTimerMinutes > 0) {
            delay(sleepTimerMinutes * 60_000L)
            onClose()
        }
    }

    BackHandler {
        if (showQuickBar) {
            showQuickBar = false
        } else if (showControls) {
            showControls = false
        } else {
            onClose()
        }
    }

    // Auto-hide controls after 5 seconds
    LaunchedEffect(channel, showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    // Auto-hide horizontal channel strip after 10 seconds of no interaction
    LaunchedEffect(showQuickBar, quickBarLastInteractionTime) {
        if (showQuickBar) {
            delay(10000)
            showQuickBar = false
        }
    }

    // Media3 ExoPlayer instance configured for live stream URLs
    val exoPlayer: ExoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(DefaultDataSource.Factory(context, httpDataSourceFactory))
            .setDrmSessionManagerProvider { DrmSessionManager.DRM_UNSUPPORTED }

        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            .setEnableDecoderFallback(true)

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                playWhenReady = true
            }
    }

    DisposableEffect(channel) {
        isBuffering = true
        streamQualityBadge = null
        val mediaItem = MediaItem.fromUri(channel.streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = (state == Player.STATE_BUFFERING)
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
            exoPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    var favoriteToastMessage by remember { mutableStateOf<String?>(null) }
    var isLongPressHandled by remember { mutableStateOf(false) }

    val addedToFavText = stringResource(R.string.iptv_added_to_favorites)
    val removedFromFavText = stringResource(R.string.iptv_removed_from_favorites)

    fun handleToggleFavoriteWithFeedback() {
        val willBeFavorite = !channel.isFavorite
        onToggleFavorite()
        showControls = true
        favoriteToastMessage = if (willBeFavorite) addedToFavText else removedFromFavText
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                val nativeEvent = keyEvent.nativeKeyEvent
                val keyCode = nativeEvent.keyCode
                val isCenterKey = keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                        keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                        keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER

                if (nativeEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                    if (isCenterKey) {
                        val isLong = nativeEvent.isLongPress || nativeEvent.repeatCount > 0
                        if (isLong && !isLongPressHandled) {
                            isLongPressHandled = true
                            handleToggleFavoriteWithFeedback()
                            return@onKeyEvent true
                        }
                    }
                } else if (nativeEvent.action == android.view.KeyEvent.ACTION_UP) {
                    if (isCenterKey) {
                        if (isLongPressHandled) {
                            isLongPressHandled = false
                            return@onKeyEvent true
                        } else {
                            if (showQuickBar) {
                                quickBarLastInteractionTime = System.currentTimeMillis()
                                return@onKeyEvent false
                            } else {
                                showControls = !showControls
                                return@onKeyEvent true
                            }
                        }
                    }
                }

                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false

                when (keyEvent.key) {
                    Key.DirectionLeft, Key.DirectionRight -> {
                        if (!showQuickBar) {
                            showQuickBar = true
                            showControls = false
                            quickBarLastInteractionTime = System.currentTimeMillis()
                            true
                        } else {
                            quickBarLastInteractionTime = System.currentTimeMillis()
                            false
                        }
                    }
                    Key.DirectionUp, Key.ChannelUp, Key.PageUp -> {
                        showControls = true
                        onNextChannel()
                        true
                    }
                    Key.DirectionDown, Key.ChannelDown, Key.PageDown -> {
                        showControls = true
                        onPrevChannel()
                        true
                    }
                    Key.Back -> {
                        if (showQuickBar) {
                            showQuickBar = false
                            true
                        } else if (showControls) {
                            showControls = false
                            true
                        } else {
                            onClose()
                            true
                        }
                    }
                    else -> {
                        showControls = true
                        false
                    }
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
                }
            },
            update = { view ->
                view.keepScreenOn = true
                view.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering indicator
        if (isBuffering && playbackError == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = TvAccentGold,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.iptv_loading),
                        color = TvTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Playback Error Overlay
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = TvAccentRed,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.iptv_error_playback),
                        color = TvTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = playbackError,
                        color = TvTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TvFocusableCard(
                            onClick = onClose,
                            modifier = Modifier
                                .height(46.dp)
                                .width(140.dp)
                        ) { isFocused ->
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = TvTextSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.action_cancel), color = TvTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Top & Bottom Channel Info Overlays
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Bottom Channel Details Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
                            )
                        )
                        .padding(horizontal = 48.dp, vertical = 28.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (channel.logoUrl.isNotBlank()) {
                            AsyncImage(
                                model = channel.logoUrl,
                                contentDescription = channel.name,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(6.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = channel.name,
                                    color = TvTextPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                streamQualityBadge?.let { badge ->
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TvAccentGold.copy(alpha = 0.22f))
                                            .border(1.dp, TvAccentGold, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = badge,
                                            color = TvAccentGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.iptv_channel_up_down_hint),
                                color = TvAccentGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Aspect Ratio Switcher
                        val aspectLabel = when (resizeMode) {
                            AspectRatioFrameLayout.RESIZE_MODE_FILL -> stringResource(R.string.iptv_aspect_fill)
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> stringResource(R.string.iptv_aspect_zoom)
                            else -> stringResource(R.string.iptv_aspect_fit)
                        }
                        TvFocusableCard(
                            onClick = {
                                showControls = true
                                resizeMode = when (resizeMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            modifier = Modifier.height(46.dp)
                        ) { isFocused ->
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = aspectLabel,
                                    tint = if (isFocused) TvAccentGold else TvTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = aspectLabel,
                                    color = if (isFocused) TvAccentGold else TvTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Sleep Timer Switcher
                        val timerOptions = listOf(0, 15, 30, 60, 90)
                        val timerLabel = if (sleepTimerMinutes == 0) {
                            stringResource(R.string.iptv_sleep_off)
                        } else {
                            "${sleepTimerMinutes}m"
                        }
                        TvFocusableCard(
                            onClick = {
                                showControls = true
                                val idx = timerOptions.indexOf(sleepTimerMinutes)
                                sleepTimerMinutes = timerOptions[(idx + 1) % timerOptions.size]
                            },
                            modifier = Modifier.height(46.dp)
                        ) { isFocused ->
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = timerLabel,
                                    tint = if (sleepTimerMinutes > 0 || isFocused) TvAccentGold else TvTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = timerLabel,
                                    color = if (sleepTimerMinutes > 0 || isFocused) TvAccentGold else TvTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Next Channel (Up arrow)
                        TvFocusableCard(
                            onClick = onNextChannel,
                            modifier = Modifier.size(46.dp)
                        ) { isFocused ->
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = stringResource(R.string.iptv_next_channel),
                                tint = if (isFocused) TvAccentGold else TvTextPrimary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Previous Channel (Down arrow)
                        TvFocusableCard(
                            onClick = onPrevChannel,
                            modifier = Modifier.size(46.dp)
                        ) { isFocused ->
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.iptv_prev_channel),
                                tint = if (isFocused) TvAccentGold else TvTextPrimary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Favorite toggle
                        TvFocusableCard(
                            onClick = { handleToggleFavoriteWithFeedback() },
                            modifier = Modifier.size(46.dp)
                        ) { isFocused ->
                            Icon(
                                imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = stringResource(R.string.iptv_toggle_favorite),
                                tint = if (channel.isFavorite) TvAccentGold else if (isFocused) TvAccentGold else TvTextPrimary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Quick Channel Strip Overlay (Triggered by Left/Right)
        AnimatedVisibility(
            visible = showQuickBar,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                        )
                    )
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.iptv_quick_channels),
                        color = TvAccentGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.iptv_quick_channels_hint),
                        color = TvTextSecondary,
                        fontSize = 12.sp
                    )
                }

                val listState = rememberLazyListState()
                val currentPlayingIndex = channels.indexOfFirst { it.id == channel.id }
                LaunchedEffect(showQuickBar, channel) {
                    if (showQuickBar && currentPlayingIndex >= 0) {
                        listState.scrollToItem(currentPlayingIndex)
                    }
                }

                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(channels, key = { it.id }) { ch ->
                        val isCurrent = (ch.id == channel.id)
                        TvFocusableCard(
                            onClick = {
                                onSelectChannel(ch)
                                showQuickBar = false
                            },
                            onFocused = {
                                quickBarLastInteractionTime = System.currentTimeMillis()
                            },
                            scaleUpRatio = 1.08f,
                            modifier = Modifier.size(105.dp)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isCurrent -> TvAccentGold.copy(alpha = 0.45f)
                                            isFocused -> Color.White.copy(alpha = 0.15f)
                                            else -> Color(0xFF1E293B).copy(alpha = 0.85f)
                                        }
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (ch.logoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = ch.logoUrl,
                                            contentDescription = ch.name,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.LiveTv,
                                            contentDescription = null,
                                            tint = if (isCurrent) TvAccentGold else TvTextPrimary,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = ch.name,
                                        color = if (isCurrent || isFocused) TvTextPrimary else TvTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent || isFocused) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TvAccentRed)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.iptv_live_tag),
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Favorite Toast Feedback Overlay
        AnimatedVisibility(
            visible = favoriteToastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        ) {
            favoriteToastMessage?.let { msg ->
                LaunchedEffect(msg) {
                    delay(2500)
                    favoriteToastMessage = null
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.88f))
                        .border(1.5.dp, TvAccentGold, RoundedCornerShape(20.dp))
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
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
