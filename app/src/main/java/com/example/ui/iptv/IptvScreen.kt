package com.example.ui.iptv

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.common.TvTextField
import com.example.data.model.IptvChannel
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.GlassSheenOverlay
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

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
    val currentPlayingChannel by viewModel.currentPlayingChannel.collectAsState()
    val playbackError by viewModel.playbackError.collectAsState()

    BackHandler {
        onBackToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvMidnightBackground)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Column: Categories List
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxSize()
                    .background(TvMidnightBackground.copy(alpha = 0.95f))
                    .padding(vertical = 24.dp)
            ) {
                // Header with Back button and IPTV Settings shortcut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvFocusableCard(
                        onClick = onBackToHome,
                        modifier = Modifier.size(40.dp)
                    ) { isFocused ->
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = if (isFocused) TvAccentGold else TvTextPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = stringResource(R.string.iptv_title),
                        color = TvTextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    TvFocusableCard(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(40.dp)
                    ) { isFocused ->
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_open_iptv_settings),
                            tint = if (isFocused) TvAccentGold else TvTextSecondary,
                            modifier = Modifier.align(Alignment.Center).size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Playlist Switcher Row (if playlists exist)
                if (playlists.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                    ) {
                        items(playlists, key = { it.id }) { pl ->
                            val isActive = (pl.id == activePlaylist?.id)
                            TvFocusableCard(
                                onClick = { viewModel.switchPlaylist(pl.id) },
                                scaleUpRatio = 1.04f,
                                modifier = Modifier.height(38.dp)
                            ) { isFocused ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            when {
                                                isActive -> TvAccentGold
                                                isFocused -> TvAccentGold.copy(alpha = 0.4f)
                                                else -> Color(0xFF1E293B)
                                            }
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = if (isActive) Color.Black else if (isFocused) Color.White else TvAccentGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = pl.name,
                                        color = if (isActive) Color.Black else TvTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isActive || isFocused) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (pl.channelCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${pl.channelCount})",
                                            color = if (isActive) Color.Black.copy(alpha = 0.75f) else TvTextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Categories list with Favorites at the very top
                val allCategories = listOf("Favorites", "All") + categories.filter { it.isNotBlank() && it != "Favorites" && it != "All" }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allCategories) { category ->
                        val isSelected = (category == selectedCategory) && searchQuery.isBlank()
                        TvFocusableCard(
                            onClick = { viewModel.selectCategory(category) },
                            scaleUpRatio = 1.03f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) { isFocused ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isSelected) TvAccentGold.copy(alpha = 0.3f) else Color.Transparent
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (category == "Favorites") {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = TvAccentGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = when (category) {
                                        "Favorites" -> stringResource(R.string.iptv_favorites)
                                        "All" -> stringResource(R.string.iptv_all_categories)
                                        else -> category
                                    },
                                    color = if (isSelected || isFocused) TvTextPrimary else TvTextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Right Main Area: Search Bar + Channels Grid
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                // Top Search Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TvTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = stringResource(R.string.iptv_search_hint),
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier.width(360.dp)
                    )

                    Text(
                        text = stringResource(R.string.iptv_channels_plural).format(channels.size),
                        color = TvTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Channels Grid
                if (channels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = TvTextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    stringResource(R.string.iptv_empty)
                                } else {
                                    stringResource(R.string.iptv_no_playlist)
                                },
                                color = TvTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (searchQuery.isBlank()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                TvFocusableCard(
                                    onClick = onOpenSettings,
                                    modifier = Modifier
                                        .height(48.dp)
                                        .width(220.dp)
                                ) { isFocused ->
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.settings_open_iptv_settings),
                                            color = TvTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(channels, key = { it.id }) { channel ->
                            ChannelGridCard(
                                channel = channel,
                                onClick = { viewModel.playChannel(channel) },
                                onToggleFavorite = { viewModel.toggleFavorite(channel) }
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen ExoPlayer Overlay when channel is active
        currentPlayingChannel?.let { channel ->
            IptvPlayerOverlay(
                channel = channel,
                channels = channels,
                playbackError = playbackError,
                onNextChannel = { viewModel.nextChannel() },
                onPrevChannel = { viewModel.prevChannel() },
                onSelectChannel = { viewModel.playChannel(it) },
                onToggleFavorite = { viewModel.toggleFavorite(channel) },
                onRetry = { viewModel.retryPlayback() },
                onError = { err -> viewModel.setPlaybackError(err) },
                onClose = { viewModel.closePlayer() }
            )
        }
    }
}

@Composable
private fun ChannelGridCard(
    channel: IptvChannel,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        onLongClick = onToggleFavorite,
        scaleUpRatio = 1.06f,
        elevation = 0.dp,
        cardBackgroundColor = Color.Transparent,
        useDefaultBorder = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
    ) { isFocused ->
        val cardShape = RoundedCornerShape(16.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(cardShape)
                .background(Color.White.copy(alpha = 0.08f)) // Base glass layer
                .border(
                    width = if (isFocused) 2.5.dp else 0.dp,
                    color = if (isFocused) TvAccentGold else Color.Transparent,
                    shape = cardShape
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (channel.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                // Removed placeholder icon to satisfy user request for a cleaner glass look without 'inner squares'
                Spacer(modifier = Modifier.width(if (channel.logoUrl.isNotBlank()) 14.dp else 4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.name,
                        color = TvTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = channel.groupTitle,
                        color = TvTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite Star
                TvFocusableCard(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp),
                    cardBackgroundColor = Color.Transparent,
                    useDefaultBorder = false,
                    elevation = 0.dp
                ) { starFocused ->
                    Icon(
                        imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = stringResource(R.string.iptv_toggle_favorite),
                        tint = if (channel.isFavorite) TvAccentGold else if (starFocused) TvAccentGold else TvTextSecondary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Glass Sheen Overlay for the entire channel card
            GlassSheenOverlay(isFocused = isFocused, shape = cardShape)
        }
    }
}
