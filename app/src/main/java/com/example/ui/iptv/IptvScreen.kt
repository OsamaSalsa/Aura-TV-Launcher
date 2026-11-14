package com.example.ui.iptv

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ChannelEpgInfo
import com.example.data.model.IptvChannel
import com.example.data.model.IptvPlaylist
import com.example.ui.common.GlassSheenOverlay
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.TvTextField
import com.example.ui.home.AppLockPinDialog
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay

private val IptvGlassPanelBg = Color(0xD90A1020)
private val IptvCardSurface = Color(0xCC131D33)

@Composable
fun IptvScreen(
    viewModel: IptvViewModel,
    onOpenSettings: () -> Unit,
    onBackToHome: () -> Unit
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val activePlaylist by viewModel.activePlaylist.collectAsState()
    val categories by viewModel.rawCategories.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val favoriteChannels by viewModel.favoriteChannels.collectAsState()
    val currentPlayingChannel by viewModel.currentPlayingChannel.collectAsState()
    val playbackError by viewModel.playbackError.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lockedCategories by viewModel.lockedCategories.collectAsState()
    val sessionUnlockedCategories by viewModel.sessionUnlockedCategories.collectAsState()
    val appLockPin by viewModel.appLockPin.collectAsState()
    val lastPlayedChannel by viewModel.lastPlayedChannel.collectAsState()
    val autoResumeLastChannel by viewModel.autoResumeLastChannel.collectAsState()
    val epgState by viewModel.epgState.collectAsState()

    var showIptvSettingsModal by remember { mutableStateOf(false) }
    var pendingUnlockCategory by remember { mutableStateOf<String?>(null) }
    var pendingToggleLockCategory by remember { mutableStateOf<String?>(null) }
    var autoResumedOnce by remember { mutableStateOf(false) }

    val firstChannelFocusRequester = remember { FocusRequester() }
    val firstCategoryFocusRequester = remember { FocusRequester() }
    var initialFocusAssigned by remember { mutableStateOf(false) }

    LaunchedEffect(autoResumeLastChannel, lastPlayedChannel) {
        val lastCh = lastPlayedChannel
        if (autoResumeLastChannel && !autoResumedOnce && lastCh != null && currentPlayingChannel == null) {
            autoResumedOnce = true
            viewModel.playChannel(lastCh)
        }
    }

    LaunchedEffect(channels.isNotEmpty(), currentPlayingChannel) {
        if (currentPlayingChannel == null && (!initialFocusAssigned || channels.isNotEmpty())) {
            delay(120)
            try {
                if (channels.isNotEmpty()) {
                    firstChannelFocusRequester.requestFocus()
                } else {
                    firstCategoryFocusRequester.requestFocus()
                }
                initialFocusAssigned = true
            } catch (_: Exception) {}
        }
    }

    BackHandler {
        if (showIptvSettingsModal) {
            showIptvSettingsModal = false
        } else {
            onBackToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050913),
                        TvMidnightBackground,
                        Color(0xFF04070F)
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(TvAccentGold.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.18f, size.height * 0.15f),
                        radius = size.width * 0.45f
                    ),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.18f, size.height * 0.15f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(TvAccentBlue.copy(alpha = 0.09f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.80f),
                        radius = size.width * 0.50f
                    ),
                    radius = size.width * 0.50f,
                    center = Offset(size.width * 0.85f, size.height * 0.80f)
                )
            }
    ) {
        if (currentPlayingChannel == null) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Left Glass Sidebar: Header + Active Playlist Summary + Categories List
            Column(
                modifier = Modifier
                    .width(296.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(IptvGlassPanelBg)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                TvAccentGold.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.10f),
                                TvAccentBlue.copy(alpha = 0.20f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(vertical = 18.dp)
            ) {
                // Sidebar Top Header: Back + Title + Quick IPTV Settings Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IptvHeaderIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBackToHome
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.iptv_title),
                            color = TvTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activePlaylist?.name ?: stringResource(R.string.iptv_subtitle),
                            color = TvAccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Sync Button
                    IptvHeaderIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.settings_iptv_sync_button),
                        onClick = { viewModel.refreshActivePlaylist() }
                    )

                    // Open TV-Remote IPTV Settings Modal
                    IptvHeaderIconButton(
                        icon = Icons.Default.Tune,
                        contentDescription = stringResource(R.string.settings_open_iptv_settings),
                        accentColor = TvAccentGold,
                        onClick = { showIptvSettingsModal = true }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Playlist Switcher Strip (if playlists exist)
                if (playlists.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp)
                    ) {
                        items(playlists, key = { it.id }) { pl ->
                            val isActive = (pl.id == activePlaylist?.id)
                            TvFocusableCard(
                                onClick = { viewModel.switchPlaylist(pl.id) },
                                scaleUpRatio = 1.05f,
                                elevation = 0.dp,
                                cardBackgroundColor = Color.Transparent,
                                useDefaultBorder = false,
                                modifier = Modifier.height(38.dp)
                            ) { isFocused ->
                                val pillShape = RoundedCornerShape(12.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(pillShape)
                                        .background(
                                            when {
                                                isFocused -> TvAccentGold
                                                isActive -> TvAccentGold.copy(alpha = 0.22f)
                                                else -> Color.White.copy(alpha = 0.07f)
                                            }
                                        )
                                        .border(
                                            width = if (isFocused || isActive) 1.5.dp else 1.dp,
                                            color = when {
                                                isFocused -> Color.White
                                                isActive -> TvAccentGold
                                                else -> Color.White.copy(alpha = 0.12f)
                                            },
                                            shape = pillShape
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = if (isFocused) Color.Black else if (isActive) TvAccentGold else TvTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = pl.name,
                                        color = if (isFocused) Color.Black else if (isActive) TvAccentGold else TvTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isActive || isFocused) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                    if (pl.channelCount > 0) {
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "${pl.channelCount}",
                                            color = if (isFocused) Color.Black.copy(alpha = 0.75f) else TvTextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Categories list with Favorites & All at top
                val allCategories = remember(categories) {
                    listOf("Favorites", "All") + categories.filter {
                        it.isNotBlank() && it != "Favorites" && it != "All"
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    items(allCategories, key = { it }) { category ->
                        val isSelected = (category == selectedCategory) && searchQuery.isBlank()
                        val isLocked = lockedCategories.contains(category)
                        val isSessionUnlocked = sessionUnlockedCategories.contains(category)
                        val isLockProtected = category != "All" && category != "Favorites"
                        val itemFr = if (category == "All") firstCategoryFocusRequester else remember { FocusRequester() }
                        TvFocusableCard(
                            onClick = {
                                if (isLocked && !isSessionUnlocked) {
                                    pendingUnlockCategory = category
                                } else {
                                    viewModel.selectCategory(category)
                                }
                            },
                            onLongClick = if (isLockProtected) {
                                { pendingToggleLockCategory = category }
                            } else null,
                            focusRequester = itemFr,
                            scaleUpRatio = 1.03f,
                            elevation = 0.dp,
                            cardBackgroundColor = Color.Transparent,
                            useDefaultBorder = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) { isFocused ->
                            val catShape = RoundedCornerShape(14.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(catShape)
                                    .background(
                                        when {
                                            isFocused -> TvAccentGold
                                            isSelected -> TvAccentGold.copy(alpha = 0.18f)
                                            else -> Color.White.copy(alpha = 0.04f)
                                        }
                                    )
                                    .border(
                                        width = if (isFocused) 2.dp else if (isSelected) 1.5.dp else 1.dp,
                                        color = when {
                                            isFocused -> Color.White
                                            isSelected -> TvAccentGold.copy(alpha = 0.85f)
                                            else -> Color.White.copy(alpha = 0.06f)
                                        },
                                        shape = catShape
                                    )
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Active indicator bar or icon
                                when {
                                    category == "Favorites" -> {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (isFocused) Color.Black else TvAccentGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    category == "All" -> {
                                        Icon(
                                            imageVector = Icons.Default.LiveTv,
                                            contentDescription = null,
                                            tint = if (isFocused) Color.Black else if (isSelected) TvAccentGold else TvAccentBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    isLocked -> {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = stringResource(R.string.iptv_category_locked),
                                            tint = if (isFocused) Color.Black else if (isSessionUnlocked) Color(0xFF4ADE80) else TvAccentGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isFocused) Color.Black
                                                    else if (isSelected) TvAccentGold
                                                    else TvTextSecondary.copy(alpha = 0.5f)
                                                )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = when (category) {
                                        "Favorites" -> stringResource(R.string.iptv_favorites)
                                        "All" -> stringResource(R.string.iptv_all_categories)
                                        else -> category
                                    },
                                    color = when {
                                        isFocused -> Color.Black
                                        isSelected -> TvAccentGold
                                        else -> TvTextPrimary
                                    },
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected || isFocused) FontWeight.ExtraBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                if (category == "Favorites" && favoriteChannels.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                if (isFocused) Color.Black.copy(alpha = 0.2f)
                                                else TvAccentGold.copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${favoriteChannels.size}",
                                            color = if (isFocused) Color.Black else TvAccentGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Sidebar Button: Manage Playlists & IPTV Settings
                Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                    TvFocusableCard(
                        onClick = { showIptvSettingsModal = true },
                        scaleUpRatio = 1.03f,
                        elevation = 0.dp,
                        cardBackgroundColor = Color.Transparent,
                        useDefaultBorder = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) { isFocused ->
                        val btnShape = RoundedCornerShape(14.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(btnShape)
                                .background(
                                    if (isFocused) TvAccentGold else Color(0xFF1A2540)
                                )
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) Color.White else TvAccentGold.copy(alpha = 0.45f),
                                    shape = btnShape
                                )
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = if (isFocused) Color.Black else TvAccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_open_iptv_settings),
                                color = if (isFocused) Color.Black else TvTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Right Main Content Area: Top Control Bar + Channels Grid + Bottom Remote Legend
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Top Search & Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(IptvGlassPanelBg)
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TvTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = stringResource(R.string.iptv_search_hint),
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                color = TvAccentGold,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        lastPlayedChannel?.let { lastCh ->
                            TvFocusableCard(
                                onClick = { viewModel.resumeLastChannel() },
                                scaleUpRatio = 1.05f,
                                elevation = 0.dp,
                                cardBackgroundColor = Color.Transparent,
                                useDefaultBorder = false,
                                modifier = Modifier.height(38.dp)
                            ) { isFocused ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isFocused) TvAccentGold else Color(0xFF10B981).copy(alpha = 0.2f))
                                        .border(
                                            width = 1.dp,
                                            color = if (isFocused) Color.White else Color(0xFF10B981).copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = stringResource(R.string.iptv_resume_last),
                                        tint = if (isFocused) Color.Black else Color(0xFF4ADE80),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "${stringResource(R.string.iptv_resume_last)}: ${lastCh.name}",
                                        color = if (isFocused) Color.Black else TvTextPrimary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 160.dp)
                                    )
                                }
                            }
                        }

                        // Active Category Badge
                        Box(
                            modifier = Modifier
                                .widthIn(max = 170.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TvAccentBlue.copy(alpha = 0.16f))
                                .border(1.dp, TvAccentBlue.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = when (selectedCategory) {
                                    "Favorites" -> stringResource(R.string.iptv_favorites)
                                    "All" -> stringResource(R.string.iptv_all_categories)
                                    else -> selectedCategory
                                },
                                color = TvAccentBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Channel Count Pill (always crisp single horizontal line)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(TvAccentGold.copy(alpha = 0.16f))
                                .border(1.dp, TvAccentGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.iptv_channels_plural).format(channels.size),
                                color = TvAccentGold,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Channels Grid or Empty State
                if (channels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(IptvGlassPanelBg)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(TvAccentGold.copy(alpha = 0.12f))
                                    .border(1.5.dp, TvAccentGold.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LiveTv,
                                    contentDescription = null,
                                    tint = TvAccentGold,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedCategory == "Favorites") {
                                    stringResource(R.string.iptv_empty)
                                } else {
                                    stringResource(R.string.iptv_no_playlist)
                                },
                                color = TvTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IptvEmptyStateButton(
                                    icon = Icons.Default.PlaylistAddCheck,
                                    label = stringResource(R.string.settings_iptv_load_presets),
                                    accentColor = TvAccentGold,
                                    onClick = { viewModel.loadPresetPlaylists() }
                                )
                                IptvEmptyStateButton(
                                    icon = Icons.Default.Tune,
                                    label = stringResource(R.string.settings_open_iptv_settings),
                                    accentColor = TvAccentBlue,
                                    onClick = { showIptvSettingsModal = true }
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 225.dp),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(channels, key = { _, ch -> ch.id }) { index, channel ->
                            val cardFr = if (index == 0) firstChannelFocusRequester else remember { FocusRequester() }
                            val epgInfo = remember(channel.id, channel.tvgId, epgState) {
                                viewModel.getChannelEpg(channel, epgState)
                            }
                            ChannelGridCard(
                                channel = channel,
                                channelNumber = index + 1,
                                epgInfo = epgInfo,
                                focusRequester = cardFr,
                                onClick = { viewModel.playChannel(channel) },
                                onToggleFavorite = { viewModel.toggleFavorite(channel) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom TV Remote D-Pad Legend Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(IptvGlassPanelBg)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IptvRemoteHintChip(keyText = "OK", actionText = stringResource(R.string.action_play))
                        IptvRemoteHintChip(keyText = "OK ⏱ / MENU", actionText = stringResource(R.string.iptv_toggle_favorite))
                        IptvRemoteHintChip(keyText = "◀ ▲ ▼ ▶", actionText = stringResource(R.string.iptv_all_categories))
                    }

                    Text(
                        text = activePlaylist?.let { "${it.name} • ${channels.size}" } ?: "",
                        color = TvTextSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        }

        // Fullscreen ExoPlayer Overlay when channel is active
        currentPlayingChannel?.let { channel ->
            IptvPlayerOverlay(
                channel = channel,
                channels = channels,
                epgPrograms = epgState,
                playbackError = playbackError,
                onNextChannel = { viewModel.nextChannel() },
                onPrevChannel = { viewModel.prevChannel() },
                onSelectChannel = { viewModel.playChannel(it) },
                onToggleFavorite = { viewModel.toggleFavorite(channel) },
                onRetry = { viewModel.retryPlayback() },
                onError = { err -> viewModel.setPlaybackError(err) },
                onOpenIptvSettings = {
                    viewModel.closePlayer()
                    showIptvSettingsModal = true
                },
                onClose = { viewModel.closePlayer() }
            )
        }

        // Category Unlock PIN Dialog
        pendingUnlockCategory?.let { cat ->
            AppLockPinDialog(
                title = stringResource(R.string.iptv_category_locked),
                subtitle = cat,
                expectedPin = appLockPin,
                onPinVerified = {
                    pendingUnlockCategory = null
                    viewModel.unlockCategory(cat)
                    viewModel.selectCategory(cat)
                },
                onDismiss = { pendingUnlockCategory = null }
            )
        }

        // Toggle Category Lock/Unlock PIN Dialog
        pendingToggleLockCategory?.let { cat ->
            val willLock = !lockedCategories.contains(cat)
            AppLockPinDialog(
                title = if (willLock) stringResource(R.string.iptv_lock_category) else stringResource(R.string.iptv_unlock_category),
                subtitle = cat,
                expectedPin = appLockPin,
                onPinVerified = {
                    pendingToggleLockCategory = null
                    viewModel.toggleCategoryLock(cat)
                },
                onDismiss = { pendingToggleLockCategory = null }
            )
        }

        // Built-in TV-Remote-Optimized IPTV Settings & Playlist Manager Dialog
        if (showIptvSettingsModal) {
            IptvQuickSettingsDialog(
                playlists = playlists,
                activePlaylist = activePlaylist,
                isSyncing = isSyncing,
                onAddPlaylist = { name, url -> viewModel.addPlaylist(name, url) },
                onSwitchPlaylist = { id -> viewModel.switchPlaylist(id) },
                onRefreshActive = { viewModel.refreshActivePlaylist() },
                onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                onDeleteAllPlaylists = { viewModel.deleteAllPlaylists() },
                onLoadPresets = { viewModel.loadPresetPlaylists() },
                onOpenFullSettings = {
                    showIptvSettingsModal = false
                    onOpenSettings()
                },
                onDismiss = { showIptvSettingsModal = false }
            )
        }
    }
}

@Composable
private fun IptvHeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    accentColor: Color = TvTextPrimary,
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.08f,
        elevation = 0.dp,
        cardBackgroundColor = Color.Transparent,
        useDefaultBorder = false,
        modifier = Modifier.size(40.dp)
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.08f)
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isFocused) Color.Black else accentColor,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun IptvEmptyStateButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.05f,
        elevation = 0.dp,
        cardBackgroundColor = Color.Transparent,
        useDefaultBorder = false,
        modifier = Modifier.height(48.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(14.dp))
                .background(if (isFocused) accentColor else accentColor.copy(alpha = 0.18f))
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Color.White else accentColor.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.Black else accentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = if (isFocused) Color.Black else TvTextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun IptvRemoteHintChip(
    keyText: String,
    actionText: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(TvAccentGold.copy(alpha = 0.18f))
                .border(1.dp, TvAccentGold.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            Text(
                text = keyText,
                color = TvAccentGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = actionText,
            color = TvTextSecondary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Single-focus TV Channel Card (no nested focusable traps!) so D-Pad Left/Right/Up/Down
 * glides smoothly across the grid in 1 click.
 * - Single press OK: Play channel
 * - Long press OK or MENU button: Toggle favorite
 */
@Composable
private fun ChannelGridCard(
    channel: IptvChannel,
    channelNumber: Int,
    epgInfo: ChannelEpgInfo = ChannelEpgInfo(),
    focusRequester: FocusRequester,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        onLongClick = onToggleFavorite,
        focusRequester = focusRequester,
        scaleUpRatio = 1.05f,
        elevation = 0.dp,
        cardBackgroundColor = Color.Transparent,
        useDefaultBorder = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
    ) { isFocused ->
        val cardShape = RoundedCornerShape(18.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(cardShape)
                .background(
                    when {
                        isFocused -> Brush.horizontalGradient(
                            listOf(
                                TvAccentGold.copy(alpha = 0.24f),
                                Color(0xFF1A2744).copy(alpha = 0.92f)
                            )
                        )
                        else -> Brush.horizontalGradient(
                            listOf(
                                IptvCardSurface,
                                Color(0xB30F172A)
                            )
                        )
                    }
                )
                .border(
                    width = if (isFocused) 2.5.dp else 1.dp,
                    color = if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.10f),
                    shape = cardShape
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Channel Logo Container
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = if (isFocused) 0.14f else 0.07f))
                        .border(
                            width = 1.dp,
                            color = if (isFocused) TvAccentGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(14.dp)
                        )
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
                            tint = if (isFocused) TvAccentGold else TvTextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.10f)
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "#$channelNumber",
                                color = if (isFocused) Color.Black else TvAccentGold,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        if (channel.groupTitle.isNotBlank()) {
                            Text(
                                text = channel.groupTitle,
                                color = if (isFocused) TvAccentGold else TvTextSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = channel.name,
                        color = TvTextPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = if (isFocused) FontWeight.ExtraBold else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val nowProg = epgInfo.current
                    if (nowProg != null) {
                        Text(
                            text = nowProg.title,
                            color = if (isFocused) TvAccentGold else TvTextSecondary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { epgInfo.progressFraction() },
                            color = TvAccentGold,
                            trackColor = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isFocused) TvAccentGold else TvTextSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isFocused) "OK • LIVE" else "LIVE",
                                color = if (isFocused) TvAccentGold else TvTextSecondary.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Non-focusable visual Favorite Indicator (toggled via Long-Press OK / MENU)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (channel.isFavorite) TvAccentGold.copy(alpha = 0.18f)
                            else Color.White.copy(alpha = 0.05f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = stringResource(R.string.iptv_toggle_favorite),
                        tint = if (channel.isFavorite) TvAccentGold else TvTextSecondary.copy(alpha = 0.45f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            GlassSheenOverlay(isFocused = isFocused, shape = cardShape)
        }
    }
}

/**
 * Dedicated TV-Remote-Optimized IPTV Settings & Playlist Manager Dialog
 * Allows instant playlist switching, 1-click remote URL helpers, preset bouquet loading,
 * syncing, and deleting without leaving the IPTV screen.
 */
@Composable
fun IptvQuickSettingsDialog(
    playlists: List<IptvPlaylist>,
    activePlaylist: IptvPlaylist?,
    isSyncing: Boolean,
    onAddPlaylist: (name: String, url: String) -> Unit,
    onSwitchPlaylist: (Long) -> Unit,
    onRefreshActive: () -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onDeleteAllPlaylists: () -> Unit,
    onLoadPresets: () -> Unit,
    onOpenFullSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var newName by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("") }
    val initialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(80)
        try { initialFocusRequester.requestFocus() } catch (_: Exception) {}
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFA080D1A),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .fillMaxHeight(0.88f)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            TvAccentGold.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.15f),
                            TvAccentBlue.copy(alpha = 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 22.dp)
            ) {
                // 1. Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(TvAccentGold.copy(alpha = 0.18f))
                                .border(1.dp, TvAccentGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = TvAccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.settings_open_iptv_settings),
                                color = TvTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = activePlaylist?.let {
                                    "${it.name} (${stringResource(R.string.settings_iptv_channels_count).format(it.channelCount)})"
                                } ?: stringResource(R.string.iptv_no_playlist),
                                color = TvAccentGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                color = TvAccentGold,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Sync Active Playlist Button
                        IptvDialogTopActionPill(
                            icon = Icons.Default.Refresh,
                            label = stringResource(R.string.settings_iptv_sync_button),
                            accentColor = TvAccentBlue,
                            focusRequester = initialFocusRequester,
                            onClick = onRefreshActive
                        )

                        // Load Default Presets Button
                        IptvDialogTopActionPill(
                            icon = Icons.Default.FolderSpecial,
                            label = stringResource(R.string.settings_iptv_load_presets),
                            accentColor = TvAccentGold,
                            onClick = onLoadPresets
                        )

                        // Close Dialog Button
                        IptvHeaderIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_cancel),
                            accentColor = TvAccentRed,
                            onClick = onDismiss
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Two-Column Layout: Left = Add M3U with Remote Quick-Chips | Right = Saved Playlists Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column: Add New Playlist with TV Remote Quick-Fill Chips
                    Column(
                        modifier = Modifier
                            .width(340.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF10192E))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.settings_iptv_add_new),
                                color = TvAccentGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            TvTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                placeholder = stringResource(R.string.settings_iptv_name_placeholder),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            TvTextField(
                                value = newUrl,
                                onValueChange = { newUrl = it },
                                placeholder = stringResource(R.string.settings_iptv_url_placeholder),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Remote Quick-Insert Chips (Saves typing with TV Remote!)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val quickTokens = listOf("https://", "http://", ".m3u", ".m3u8")
                                items(quickTokens) { token ->
                                    TvFocusableCard(
                                        onClick = {
                                            newUrl = if (token.startsWith("http")) {
                                                if (newUrl.startsWith("http")) newUrl else token + newUrl
                                            } else {
                                                if (newUrl.endsWith(token)) newUrl else newUrl + token
                                            }
                                        },
                                        scaleUpRatio = 1.06f,
                                        elevation = 0.dp,
                                        cardBackgroundColor = Color.Transparent,
                                        useDefaultBorder = false,
                                        modifier = Modifier.height(32.dp)
                                    ) { isFocused ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.08f)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.14f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = token,
                                                color = if (isFocused) Color.Black else TvAccentGold,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Paste from Clipboard Chip
                                item {
                                    TvFocusableCard(
                                        onClick = {
                                            try {
                                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                val clipText = cm?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
                                                if (!clipText.isNullOrBlank()) {
                                                    newUrl = clipText
                                                }
                                            } catch (_: Exception) {}
                                        },
                                        scaleUpRatio = 1.06f,
                                        elevation = 0.dp,
                                        cardBackgroundColor = Color.Transparent,
                                        useDefaultBorder = false,
                                        modifier = Modifier.height(32.dp)
                                    ) { isFocused ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isFocused) TvAccentBlue else TvAccentBlue.copy(alpha = 0.18f)
                                                )
                                                .padding(horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentPaste,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Paste",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Add & Activate Playlist Button
                            TvFocusableCard(
                                onClick = {
                                    if (newUrl.isNotBlank()) {
                                        onAddPlaylist(newName, newUrl)
                                        newName = ""
                                        newUrl = ""
                                    }
                                },
                                scaleUpRatio = 1.04f,
                                elevation = 0.dp,
                                cardBackgroundColor = Color.Transparent,
                                useDefaultBorder = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) { isFocused ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isFocused) TvAccentGold else TvAccentGold.copy(alpha = 0.85f)
                                        )
                                        .border(
                                            width = if (isFocused) 2.dp else 1.dp,
                                            color = if (isFocused) Color.White else TvAccentGold,
                                            shape = RoundedCornerShape(14.dp)
                                        ),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.settings_iptv_add_button),
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Open Full IPTV Settings Page Button
                        TvFocusableCard(
                            onClick = onOpenFullSettings,
                            scaleUpRatio = 1.03f,
                            elevation = 0.dp,
                            cardBackgroundColor = Color.Transparent,
                            useDefaultBorder = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) { isFocused ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isFocused) Color.White else Color.White.copy(alpha = 0.07f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = if (isFocused) Color.Black else TvTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_title),
                                    color = if (isFocused) Color.Black else TvTextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Right Column: Saved Playlists List (1-Click OK to Activate!)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF10192E))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_iptv_saved_header).format(playlists.size),
                                color = TvTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            if (playlists.isNotEmpty()) {
                                TvFocusableCard(
                                    onClick = onDeleteAllPlaylists,
                                    scaleUpRatio = 1.05f,
                                    elevation = 0.dp,
                                    cardBackgroundColor = Color.Transparent,
                                    useDefaultBorder = false,
                                    modifier = Modifier.height(34.dp)
                                ) { isFocused ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isFocused) TvAccentRed else TvAccentRed.copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.settings_iptv_delete_all),
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (playlists.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_iptv_no_saved),
                                    color = TvTextSecondary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(playlists, key = { it.id }) { pl ->
                                    val isActive = (pl.id == activePlaylist?.id)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Main Playlist Card — Pressing OK activates or syncs immediately!
                                        TvFocusableCard(
                                            onClick = {
                                                if (isActive) onRefreshActive() else onSwitchPlaylist(pl.id)
                                            },
                                            scaleUpRatio = 1.02f,
                                            elevation = 0.dp,
                                            cardBackgroundColor = Color.Transparent,
                                            useDefaultBorder = false,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(64.dp)
                                        ) { isFocused ->
                                            val rowShape = RoundedCornerShape(14.dp)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(rowShape)
                                                    .background(
                                                        when {
                                                            isFocused -> TvAccentGold.copy(alpha = 0.25f)
                                                            isActive -> TvAccentGold.copy(alpha = 0.14f)
                                                            else -> Color.White.copy(alpha = 0.05f)
                                                        }
                                                    )
                                                    .border(
                                                        width = if (isFocused) 2.dp else if (isActive) 1.5.dp else 1.dp,
                                                        color = when {
                                                            isFocused -> TvAccentGold
                                                            isActive -> TvAccentGold.copy(alpha = 0.7f)
                                                            else -> Color.White.copy(alpha = 0.08f)
                                                        },
                                                        shape = rowShape
                                                    )
                                                    .padding(horizontal = 14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.LiveTv,
                                                    contentDescription = null,
                                                    tint = if (isActive || isFocused) TvAccentGold else TvTextSecondary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = pl.name,
                                                            color = TvTextPrimary,
                                                            fontSize = 14.5.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        if (isActive) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(TvAccentGold)
                                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = stringResource(R.string.settings_iptv_active_tag),
                                                                    color = Color.Black,
                                                                    fontSize = 9.5.sp,
                                                                    fontWeight = FontWeight.ExtraBold
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "${stringResource(R.string.settings_iptv_channels_count).format(pl.channelCount)} • ${pl.url}",
                                                        color = TvTextSecondary,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.08f)
                                                        )
                                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                                ) {
                                                    Text(
                                                        text = if (isActive) stringResource(R.string.settings_iptv_sync_button)
                                                        else stringResource(R.string.settings_iptv_activate_button),
                                                        color = if (isFocused) Color.Black else TvAccentGold,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }

                                        // Delete Playlist Button
                                        TvFocusableCard(
                                            onClick = { onDeletePlaylist(pl.id) },
                                            scaleUpRatio = 1.06f,
                                            elevation = 0.dp,
                                            cardBackgroundColor = Color.Transparent,
                                            useDefaultBorder = false,
                                            modifier = Modifier.size(64.dp)
                                        ) { isFocused ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        if (isFocused) TvAccentRed else TvAccentRed.copy(alpha = 0.16f)
                                                    )
                                                    .border(
                                                        width = if (isFocused) 2.dp else 1.dp,
                                                        color = if (isFocused) Color.White else TvAccentRed.copy(alpha = 0.4f),
                                                        shape = RoundedCornerShape(14.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.action_delete),
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IptvDialogTopActionPill(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    TvFocusableCard(
        onClick = onClick,
        focusRequester = focusRequester,
        scaleUpRatio = 1.06f,
        elevation = 0.dp,
        cardBackgroundColor = Color.Transparent,
        useDefaultBorder = false,
        modifier = Modifier.height(40.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isFocused) accentColor else accentColor.copy(alpha = 0.18f))
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Color.White else accentColor.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isFocused) Color.Black else accentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = if (isFocused) Color.Black else TvTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
