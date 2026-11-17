package com.example.ui.iptv

import android.content.Context
import android.os.Handler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.FormatHolder
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.video.VideoRendererEventListener
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.IptvChannel
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

internal val PlayerHudGlassTop = Color(0xE60B1325)
internal val PlayerHudGlassBottom = Color(0xF5070B16)

@OptIn(UnstableApi::class)
internal fun buildResilientIptvExoPlayer(
    context: Context,
    preferSoftwareVideoDecoder: Boolean,
    mainHandler: Handler,
    onUnsupportedAudioDetected: (ExoPlayer, String?) -> Unit,
    onPlayerCreated: (ExoPlayer) -> Unit = {}
): ExoPlayer {
    var createdPlayer: ExoPlayer? = null
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(15000)

    val extractorsFactory = DefaultExtractorsFactory()
        .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES)

    val mediaSourceFactory = DefaultMediaSourceFactory(context, extractorsFactory)
        .setDataSourceFactory(DefaultDataSource.Factory(context, httpDataSourceFactory))
        .setDrmSessionManagerProvider { DrmSessionManager.DRM_UNSUPPORTED }

    val trackSelector = DefaultTrackSelector(context).apply {
        setParameters(
            buildUponParameters()
                .setAllowVideoMixedMimeTypeAdaptiveness(true)
                .setAllowVideoNonSeamlessAdaptiveness(true)
                .setAllowAudioMixedMimeTypeAdaptiveness(true)
                .setExceedVideoConstraintsIfNecessary(true)
                .setExceedRendererCapabilitiesIfNecessary(true)
                .setTunnelingEnabled(false)
        )
    }

    val renderersFactory = object : DefaultRenderersFactory(context) {
        override fun buildVideoRenderers(
            ctx: Context,
            extensionRendererMode: Int,
            mediaCodecSelector: MediaCodecSelector,
            enableDecoderFallback: Boolean,
            eventHandler: Handler,
            eventListener: VideoRendererEventListener,
            allowedVideoJoiningTimeMs: Long,
            out: ArrayList<Renderer>
        ) {
            val effectiveSelector = if (preferSoftwareVideoDecoder) {
                MediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
                    val all = mediaCodecSelector.getDecoderInfos(
                        mimeType,
                        requiresSecureDecoder,
                        requiresTunnelingDecoder
                    )
                    all.sortedByDescending { info ->
                        val name = info.name.lowercase(Locale.US)
                        info.softwareOnly || name.startsWith("c2.android.") || name.startsWith("omx.google.")
                    }
                }
            } else {
                mediaCodecSelector
            }
            super.buildVideoRenderers(
                ctx,
                extensionRendererMode,
                effectiveSelector,
                true,
                eventHandler,
                eventListener,
                allowedVideoJoiningTimeMs,
                out
            )
        }

        override fun buildAudioRenderers(
            ctx: Context,
            extensionRendererMode: Int,
            mediaCodecSelector: MediaCodecSelector,
            enableDecoderFallback: Boolean,
            audioSink: AudioSink,
            eventHandler: Handler,
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
                        bypassUnsupportedAudio = true
                        mainHandler.post {
                            val player = createdPlayer
                            if (player != null && !player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_AUDIO)) {
                                onUnsupportedAudioDetected(player, incoming.sampleMimeType)
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

    return ExoPlayer.Builder(context, renderersFactory)
        .setTrackSelector(trackSelector)
        .setMediaSourceFactory(mediaSourceFactory)
        .build()
        .apply {
            playWhenReady = true
            createdPlayer = this
            onPlayerCreated(this)
        }
}

@Composable
internal fun IptvQuickChannelCarouselStrip(
    channel: IptvChannel,
    channels: List<IptvChannel>,
    showQuickBar: Boolean,
    quickStripFocusRequesters: MutableMap<Long, FocusRequester>,
    onSelectChannel: (IptvChannel) -> Unit,
    onInteraction: () -> Unit,
    onDismiss: () -> Unit
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
                        onDismiss()
                    },
                    onFocused = {
                        onInteraction()
                    },
                    focusRequester = itemFr,
                    scaleUpRatio = 1.08f,
                    elevation = 0.dp,
                    cardBackgroundColor = Color.Transparent,
                    useDefaultBorder = false,
                    modifier = Modifier
                        .width(180.dp)
                        .height(114.dp)
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
                        Text(
                            text = "#${index + 1}",
                            color = if (isFocused) TvAccentGold else TvTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(Alignment.TopStart)
                        )

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
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.LiveTv,
                                    contentDescription = null,
                                    tint = if (isCurrent || isFocused) TvAccentGold else TvTextPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = ch.name,
                                color = if (isCurrent || isFocused) Color.White else TvTextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 13.5.sp,
                                fontWeight = if (isCurrent || isFocused) FontWeight.ExtraBold else FontWeight.SemiBold,
                                maxLines = 2,
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

@Composable
internal fun PlayerControlHudButton(
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
        scaleUpRatio = 1.12f,
        elevation = 0.dp,
        cardBackgroundColor = Color.Transparent,
        useDefaultBorder = false,
        modifier = modifier.size(width = 36.dp, height = 32.dp)
    ) { isFocused ->
        val shape = RoundedCornerShape(10.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    when {
                        isFocused -> accentColor
                        isActive -> accentColor.copy(alpha = 0.20f)
                        else -> Color.White.copy(alpha = 0.06f)
                    }
                )
                .border(
                    width = if (isFocused) 1.5.dp else if (isActive) 1.dp else 0.6.dp,
                    color = when {
                        isFocused -> Color.White
                        isActive -> accentColor.copy(alpha = 0.72f)
                        else -> Color.White.copy(alpha = 0.10f)
                    },
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = when {
                    isFocused -> if (accentColor == TvAccentRed) Color.White else Color.Black
                    isActive -> accentColor
                    else -> TvTextPrimary.copy(alpha = 0.90f)
                },
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
internal fun PlayerErrorActionButton(
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

/**
 * Floating Picture-in-Picture (PiP) Mini IPTV Player Overlay.
 * Allows users to continue watching live IPTV in a sleek corner window while browsing
 * the Home Screen or Settings, with instant Expand, Next Channel, and Close controls.
 */
@OptIn(UnstableApi::class)
@Composable
fun IptvMiniPlayerOverlay(
    channel: IptvChannel,
    onExpand: () -> Unit,
    onNextChannel: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mainHandler = remember { Handler(android.os.Looper.getMainLooper()) }
    var isBuffering by remember(channel) { mutableStateOf(true) }

    val exoPlayer = remember {
        buildResilientIptvExoPlayer(
            context = context,
            preferSoftwareVideoDecoder = false,
            mainHandler = mainHandler,
            onUnsupportedAudioDetected = { player, _ ->
                player.trackSelectionParameters = player.trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                    .build()
            }
        )
    }

    DisposableEffect(channel, exoPlayer) {
        isBuffering = true
        try {
            exoPlayer.stop()
        } catch (_: Exception) {}
        exoPlayer.setMediaItem(MediaItem.fromUri(channel.streamUrl))
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = (state == Player.STATE_BUFFERING)
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            try { exoPlayer.stop() } catch (_: Exception) {}
            try { exoPlayer.clearVideoSurface() } catch (_: Exception) {}
            try { exoPlayer.release() } catch (_: Exception) {}
        }
    }

    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .width(308.dp)
            .height(176.dp)
            .clip(shape)
            .background(Color.Black)
            .border(1.5.dp, TvAccentGold.copy(alpha = 0.78f), shape)
    ) {
        AndroidView(
            factory = { ctx ->
                val view = android.view.LayoutInflater.from(ctx)
                    .inflate(R.layout.layout_iptv_player_texture, null, false) as PlayerView
                view.apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true
                }
            },
            update = { pv ->
                if (pv.player !== exoPlayer) {
                    pv.player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering) {
            CircularProgressIndicator(
                color = TvAccentGold,
                strokeWidth = 2.5.dp,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center)
            )
        }

        // Top Live Channel Info Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.82f), Color.Transparent)
                    )
                )
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(TvAccentRed)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = stringResource(R.string.iptv_live_tag),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = channel.name,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        // Bottom Controls Strip (Expand Fullscreen, Next Channel, Close Mini Player)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerControlHudButton(
                icon = Icons.Default.OpenInFull,
                label = stringResource(R.string.action_open),
                accentColor = TvAccentGold,
                onClick = onExpand
            )
            Spacer(modifier = Modifier.width(6.dp))
            PlayerControlHudButton(
                icon = Icons.Default.SkipNext,
                label = stringResource(R.string.iptv_next_channel),
                accentColor = TvAccentBlue,
                onClick = onNextChannel
            )
            Spacer(modifier = Modifier.width(6.dp))
            PlayerControlHudButton(
                icon = Icons.Default.Close,
                label = stringResource(R.string.action_cancel),
                accentColor = TvAccentRed,
                onClick = onClose
            )
        }
    }
}
