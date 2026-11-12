package com.example.data.model

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a real installed application on the device.
 */
@Immutable
data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable? = null,
    val banner: Drawable? = null,
    val iconBitmap: ImageBitmap? = null,
    val bannerBitmap: ImageBitmap? = null,
    val dominantColor: Int? = null,
    val isHidden: Boolean = false,
    val displayOrder: Int = 0,
    val auroraColors: com.example.ui.common.AuroraColors? = null,
    val isGame: Boolean = false,
    val isLocked: Boolean = false
)

@Entity(tableName = "iptv_channels")
data class IptvChannel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val logoUrl: String = "",
    val groupTitle: String = "General",
    val streamUrl: String,
    val isFavorite: Boolean = false,
    val orderIndex: Int = 0,
    val playlistId: Long = 0
)

/**
 * Represents a saved IPTV playlist M3U list.
 */
@Entity(tableName = "iptv_playlists")
data class IptvPlaylist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val isActive: Boolean = false,
    val channelCount: Int = 0
)
