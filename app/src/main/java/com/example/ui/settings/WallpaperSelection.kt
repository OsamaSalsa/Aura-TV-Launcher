package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.launch

data class WallpaperItemWithCategory(
    val wallpaper: UnsplashWallpaper,
    val categoryId: String,
    val categoryTitleEn: String,
    val categoryTitleAr: String
)

@Composable
fun OnlineWallpaperSelectionGrid(
    selectedWallpaperUrl: String,
    onWallpaperSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allWallpapersWithCategory = remember {
        onlineWallpaperCategories.flatMap { cat ->
            cat.wallpapers.map { wp ->
                WallpaperItemWithCategory(
                    wallpaper = wp,
                    categoryId = cat.id,
                    categoryTitleEn = cat.titleEn,
                    categoryTitleAr = cat.titleAr
                )
            }
        }
    }

    var selectedCategoryId by remember { mutableStateOf(onlineWallpaperCategories.first().id) }
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Online Wallpapers",
            color = TvTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Category Filter Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(onlineWallpaperCategories, key = { it.id }) { cat ->
                val isSelected = (cat.id == selectedCategoryId)
                TvFocusableCard(
                    onClick = {
                        selectedCategoryId = cat.id
                        val targetIndex = allWallpapersWithCategory.indexOfFirst { it.categoryId == cat.id }
                        if (targetIndex >= 0) {
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(targetIndex)
                            }
                        }
                    },
                    scaleUpRatio = 1.04f,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(38.dp)
                ) { isFocused ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                when {
                                    isSelected -> TvAccentGold
                                    isFocused -> TvAccentGold.copy(alpha = 0.6f)
                                    else -> Color(0xFF1E293B)
                                }
                            )
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat.titleEn,
                            color = if (isSelected) Color.Black else TvTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sequential List of All Wallpapers Across All Categories
        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
        ) {
            items(allWallpapersWithCategory, key = { it.wallpaper.id }) { itemWithCat ->
                val isSelected = (itemWithCat.wallpaper.fullUrl == selectedWallpaperUrl)
                UnsplashThumbnailCard(
                    wallpaper = itemWithCat.wallpaper,
                    isSelected = isSelected,
                    onClick = { onWallpaperSelected(itemWithCat.wallpaper.fullUrl) },
                    onFocused = {
                        selectedCategoryId = itemWithCat.categoryId
                    }
                )
            }
        }
    }
}

@Composable
fun UnsplashThumbnailCard(
    wallpaper: UnsplashWallpaper,
    isSelected: Boolean,
    onClick: () -> Unit,
    onFocused: (() -> Unit)? = null
) {
    val context = LocalContext.current

    TvFocusableCard(
        onClick = onClick,
        onFocused = onFocused ?: {},
        scaleUpRatio = 1.06f,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .width(180.dp)
            .height(120.dp)
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TvMidnightBackground)
        ) {
            // Small preview thumbnail image loaded with crossfade
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(wallpaper.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
            )

            // Bottom gradient overlay for title legibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = wallpaper.title,
                    color = if (isFocused || isSelected) TvAccentGold else TvTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Active Wallpaper Selection Badge
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TvAccentGold)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Active",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
