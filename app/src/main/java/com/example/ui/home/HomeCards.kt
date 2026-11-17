package com.example.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AppItem
import com.example.data.model.IptvChannel
import com.example.ui.common.BookmarkTileImage
import com.example.ui.common.GlassSheenOverlay
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.drawableToBitmapSafe
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvDeepBlue

private val AppCardShape = RoundedCornerShape(16.dp)

@Composable
fun AppItemCard(
    app: AppItem,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: androidx.compose.ui.focus.FocusRequester = remember { androidx.compose.ui.focus.FocusRequester() },
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onFocused: () -> Unit,
    onRequestFocusUp: (() -> Unit)? = null,
    opacityPercent: Int = 75
) {
    TvFocusableCard(
        onClick = onClick,
        onLongClick = onLongClick,
        onFocused = onFocused,
        focusRequester = focusRequester,
        scaleUpRatio = 1.07f,
        elevation = 8.dp,
        useDefaultBorder = false,
        focusBorderColor = app.auroraColors?.glow ?: TvAccentGold,
        modifier = modifier
            .aspectRatio(1.77f)
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == android.view.KeyEvent.ACTION_DOWN &&
                    keyEvent.type == KeyEventType.KeyDown &&
                    keyEvent.key == Key.DirectionUp
                ) {
                    if (onRequestFocusUp != null) {
                        onRequestFocusUp()
                        true
                    } else false
                } else false
            }
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(AppCardShape)
        ) {
            if (app.packageName == "com.minidownloader.portal") {
                Image(painter = painterResource(R.drawable.ic_minidownloader_banner), "Mini Downloader", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.aurastore.portal") {
                Image(painter = painterResource(R.drawable.ic_aurastore_banner), "Aura Store", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.miniyube.portal") {
                Image(painter = painterResource(R.drawable.ic_minitube_banner), "MiniTube", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.crazygames.portal") {
                Image(painter = painterResource(R.drawable.ic_crazygames_banner), "CrazyGames", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.poki.portal") {
                Image(painter = painterResource(R.drawable.ic_poki_banner), "Poki", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.playhop.portal") {
                Image(painter = painterResource(R.drawable.ic_playhop_banner), "Playhop", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (app.packageName == "com.easygames.portal") {
                Image(painter = painterResource(R.drawable.ic_easygames_banner), "EasyGames", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                val bannerImageBitmap = app.bannerBitmap ?: remember(app.banner) {
                    app.banner?.let { drawableToBitmapSafe(it).asImageBitmap() }
                }
                val iconImageBitmap = app.iconBitmap ?: remember(app.icon) {
                    app.icon?.let { drawableToBitmapSafe(it).asImageBitmap() }
                }

                if (bannerImageBitmap != null) {
                    Image(bitmap = bannerImageBitmap, app.label, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (!app.bookmarkIconUrl.isNullOrBlank()) {
                    BookmarkTileImage(
                        iconUrl = app.bookmarkIconUrl,
                        contentDescription = app.label,
                        modifier = Modifier.fillMaxSize(),
                        iconVerticalPadding = 5.dp,
                        iconHorizontalPadding = 10.dp,
                        loadingContent = {
                            AppTileFallbackContent(app = app, iconImageBitmap = iconImageBitmap)
                        },
                        errorContent = {
                            AppTileFallbackContent(app = app, iconImageBitmap = iconImageBitmap)
                        }
                    )
                } else {
                    AppTileFallbackContent(app = app, iconImageBitmap = iconImageBitmap)
                }
            }

            if (app.isLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.72f))
                        .border(1.dp, TvAccentGold.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TvAccentGold,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            GlassSheenOverlay(isFocused = isFocused || isHighlighted, shape = AppCardShape)
        }
    }
}

@Composable
private fun AppTileFallbackContent(
    app: AppItem,
    iconImageBitmap: ImageBitmap?
) {
    val domColor = app.dominantColor?.let { Color(it) } ?: TvAccentBlue
    val glowColor = TvAccentBlue
    val deepColor = TvDeepBlue
    val glassBg = Color(0x331E293B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(glassBg),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.radialGradient(
                    0.0f to domColor.copy(alpha = 0.50f),
                    0.55f to deepColor.copy(alpha = 0.25f),
                    1.0f to Color.Transparent,
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.85f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to glowColor.copy(alpha = 0.35f),
                    0.55f to glowColor.copy(alpha = 0.12f),
                    1.0f to Color.Transparent,
                    center = Offset(w * 0.50f, h * 0.35f),
                    radius = w * 0.60f
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.42f)
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            if (app.bookmarkUrl != null) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = app.label,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier
                        .size(38.dp)
                        .padding(bottom = 4.dp)
                )
            } else if (iconImageBitmap != null) {
                Image(
                    bitmap = iconImageBitmap,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(46.dp)
                        .padding(bottom = 4.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = app.label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FavoriteChannelCard(
    channel: IptvChannel,
    cardSize: Dp = 110.dp,
    opacityPercent: Int = 75,
    isHighlighted: Boolean = false,
    focusRequester: androidx.compose.ui.focus.FocusRequester = remember { androidx.compose.ui.focus.FocusRequester() },
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFocused: () -> Unit,
    onMoveLeft: (() -> Unit)? = null,
    onMoveRight: (() -> Unit)? = null,
    onRequestFocusUp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        onLongClick = onLongClick,
        onFocused = onFocused,
        focusRequester = focusRequester,
        scaleUpRatio = if (isHighlighted) 1.10f else 1.08f,
        elevation = 0.dp,
        cardBackgroundColor = Color.Transparent,
        useDefaultBorder = false,
        modifier = modifier
            .size(cardSize)
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == android.view.KeyEvent.ACTION_DOWN &&
                    keyEvent.type == KeyEventType.KeyDown
                ) {
                    if (keyEvent.key == Key.DirectionUp && !isHighlighted) {
                        if (onRequestFocusUp != null) {
                            onRequestFocusUp()
                            true
                        } else false
                    } else if (isHighlighted) {
                        if (keyEvent.key == Key.DirectionLeft) {
                            onMoveLeft?.invoke()
                            true
                        } else if (keyEvent.key == Key.DirectionRight) {
                            onMoveRight?.invoke()
                            true
                        } else false
                    } else false
                } else false
            }
    ) { isFocused ->
        val glassAlpha = (opacityPercent.coerceIn(10, 100) / 100f)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val channelShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(channelShape)
                    .background(Color.White.copy(alpha = glassAlpha))
                    .border(
                        width = if (isHighlighted || isFocused) 2.5.dp else 0.dp,
                        color = if (isHighlighted || isFocused) TvAccentGold else Color.Transparent,
                        shape = channelShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        channel.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                GlassSheenOverlay(
                    isFocused = isFocused || isHighlighted,
                    shape = channelShape
                )
            }

            if (isHighlighted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(1.dp, TvAccentGold, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_move),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    badge: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onFocused: () -> Unit
) {
    TvFocusableCard(onClick = onClick, onFocused = onFocused, scaleUpRatio = 1.06f, modifier = modifier.height(130.dp)) { isFocused ->
        val cardShape = RoundedCornerShape(16.dp)
        Box(modifier = Modifier.fillMaxSize().clip(cardShape).background(gradient)) {
            Box(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.35f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                        }
                    }
                    Column {
                        Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            GlassSheenOverlay(isFocused = isFocused, shape = cardShape)
        }
    }
}
