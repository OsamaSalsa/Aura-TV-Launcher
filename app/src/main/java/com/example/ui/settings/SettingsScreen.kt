package com.example.ui.settings

import android.app.role.RoleManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.common.AuroraColors
import com.example.ui.common.BookmarkTileImage
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.TvTextField
import com.example.ui.home.AmbientScreensaverOverlay
import com.example.ui.home.extractYouTubeVideoId
import com.example.ui.home.screensaverPresetVideos
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.util.FilePermissionHelper
import kotlinx.coroutines.delay

enum class SettingsSection {
    SYSTEM, LAUNCHER, SCREENSAVER, IPTV, REMOTE_CONTROLS
}

private data class SystemShortcutItem(
    val icon: ImageVector,
    val title: String,
    val desc: String,
    val onClick: () -> Unit
)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    initialSection: SettingsSection = SettingsSection.SYSTEM,
    onUpdateAvailable: (com.example.data.update.AppUpdateInfo) -> Unit = {},
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    var currentSection by remember(initialSection) { mutableStateOf(initialSection) }
    val initialSidebarFocus = remember { androidx.compose.ui.focus.FocusRequester() }

    val isDynamicBg by viewModel.isDynamicBgEnabled.collectAsState()
    val wallpaperMode by viewModel.wallpaperMode.collectAsState()
    val customWallpaperUrl by viewModel.customWallpaperUrl.collectAsState()
    val headerControlMode by viewModel.headerControlMode.collectAsState()
    val isClock24h by viewModel.isClock24h.collectAsState()
    val isClockSecondsEnabled by viewModel.isClockSecondsEnabled.collectAsState()
    val clockSize by viewModel.clockSize.collectAsState()
    val dateFormatStyle by viewModel.dateFormatStyle.collectAsState()
    val greetingMode by viewModel.greetingMode.collectAsState()
    val customGreetingText by viewModel.customGreetingText.collectAsState()
    var customGreetingInput by remember(customGreetingText) { mutableStateOf(customGreetingText) }
    val appsPerRow by viewModel.appsPerRow.collectAsState()
    val appsTopOffset by viewModel.appsTopOffset.collectAsState()
    @Suppress("UNUSED_VARIABLE")
    val iptvUrl by viewModel.iptvUrl.collectAsState()
    val iptvPlaylists by viewModel.iptvPlaylists.collectAsState()
    val activeIptvPlaylist by viewModel.activeIptvPlaylist.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val dnsBlockerMode by viewModel.dnsBlockerMode.collectAsState()
    val appLockPin by viewModel.appLockPin.collectAsState()
    var pinInput by remember(appLockPin) { mutableStateOf(appLockPin) }
    val autoCheckUpdates by viewModel.autoCheckUpdates.collectAsState()
    val isCheckingUpdates by viewModel.isCheckingUpdates.collectAsState()
    val glassOpacity by viewModel.glassOpacity.collectAsState()
    val showOnlyTvApps by viewModel.showOnlyTvApps.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()
    val showRecentApps by viewModel.showRecentApps.collectAsState()
    val lowRamMode by viewModel.lowRamMode.collectAsState()
    val nightComfortMode by viewModel.nightComfortMode.collectAsState()
    val customWebBookmarks by viewModel.customWebBookmarks.collectAsState()
    val autoResumeLastChannel by viewModel.autoResumeLastChannel.collectAsState()
    val epgUrlOverride by viewModel.epgUrlOverride.collectAsState()
    val appLanguage by viewModel.language.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val screensaverTimeout by viewModel.screensaverTimeout.collectAsState()
    val screensaverMode by viewModel.screensaverMode.collectAsState()
    val screensaverWallpaper by viewModel.screensaverWallpaper.collectAsState()
    val screensaverVideo by viewModel.screensaverVideo.collectAsState()
    val customScreensaverVideos by viewModel.customScreensaverVideos.collectAsState()
    val deletedScreensaverPresets by viewModel.deletedScreensaverPresets.collectAsState()
    val slideshowSource by viewModel.slideshowSource.collectAsState()
    val slideshowFolderUri by viewModel.slideshowFolderUri.collectAsState()
    val slideshowCustomPhotos by viewModel.slideshowCustomPhotos.collectAsState()
    val slideshowIntervalSec by viewModel.slideshowIntervalSec.collectAsState()
    val slideshowMotion by viewModel.slideshowMotion.collectAsState()
    val slideshowTransition by viewModel.slideshowTransition.collectAsState()
    val slideshowEffect by viewModel.slideshowEffect.collectAsState()
    val slideshowClockMode by viewModel.slideshowClockMode.collectAsState()
    val slideshowMusicMode by viewModel.slideshowMusicMode.collectAsState()
    val slideshowCustomMusicUri by viewModel.slideshowCustomMusicUri.collectAsState()
    val slideshowMusicVolume by viewModel.slideshowMusicVolume.collectAsState()
    val slideshowShuffle by viewModel.slideshowShuffle.collectAsState()
    val slideshowDimLevel by viewModel.slideshowDimLevel.collectAsState()
    var showPreviewScreensaver by remember { mutableStateOf(false) }
    var showStorageManagerDialog by remember { mutableStateOf(false) }
    var showTvFolderPickerDialog by remember { mutableStateOf(false) }

    var newBookmarkName by remember { mutableStateOf("") }
    var newBookmarkUrl by remember { mutableStateOf("") }
    @Suppress("UNUSED_VARIABLE")
    var customWallpaperInput by remember(customWallpaperUrl) { mutableStateOf(customWallpaperUrl) }

    var isDefaultLauncher by remember { mutableStateOf(viewModel.isDefaultLauncher(context)) }
    var isDefaultAssistant by remember { mutableStateOf(viewModel.isDefaultAssistant(context)) }
    var hasFilePermission by remember { mutableStateOf(FilePermissionHelper.hasStoragePermission(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefaultLauncher = viewModel.isDefaultLauncher(context)
                isDefaultAssistant = viewModel.isDefaultAssistant(context)
                hasFilePermission = FilePermissionHelper.hasStoragePermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        try {
            initialSidebarFocus.requestFocus()
        } catch (_: Exception) {}
    }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        isDefaultLauncher = viewModel.isDefaultLauncher(context)
    }

    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.setDnsBlockerMode("ads", context)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.setWallpaperMode("custom")
            viewModel.setCustomWallpaperUrl(uri.toString())
        }
    }

    val folderTreePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.setSlideshowFolderUri(uri.toString())
            showTvFolderPickerDialog = false
        }
    }

    val slideshowMultiPhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}
            }
            viewModel.addSlideshowCustomPhotos(uris.map { it.toString() })
        }
    }

    val slideshowMusicPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.setSlideshowCustomMusicUri(uri.toString())
        }
    }

    var assistantRoleRequestStartMs by remember { mutableStateOf(0L) }

    val assistantRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val nowDefault = viewModel.isDefaultAssistant(context)
        isDefaultAssistant = nowDefault
        val elapsed = System.currentTimeMillis() - assistantRoleRequestStartMs
        if (nowDefault) {
            viewModel.showAlreadyDefaultAssistantStatus()
        } else if (result.resultCode != android.app.Activity.RESULT_OK && elapsed < 1200L) {
            viewModel.openAssistantSettingsFallback(context)
        }
    }

    fun requestDefaultLauncher() {
        if (Build.VERSION.SDK_INT >= 29) {
            val rm = context.getSystemService(RoleManager::class.java)
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        viewModel.promptSetDefaultLauncher(context)
    }

    fun requestDefaultAssistant() {
        if (viewModel.isDefaultAssistant(context)) {
            isDefaultAssistant = true
            viewModel.showAlreadyDefaultAssistantStatus()
            return
        }
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                val rm = context.getSystemService(RoleManager::class.java)
                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_ASSISTANT) && !rm.isRoleHeld(RoleManager.ROLE_ASSISTANT)) {
                    assistantRoleRequestStartMs = System.currentTimeMillis()
                    assistantRoleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT))
                    return
                }
            } catch (_: Exception) {}
        }
        viewModel.openAssistantSettingsFallback(context)
    }

    BackHandler {
        onBackToHome()
    }

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(3500)
            viewModel.clearStatusMessage()
        }
    }

    val navItems = listOf(
        Triple(SettingsSection.SYSTEM, stringResource(R.string.settings_category_system), Icons.Default.Settings),
        Triple(SettingsSection.LAUNCHER, stringResource(R.string.settings_category_launcher), Icons.Default.ColorLens),
        Triple(SettingsSection.SCREENSAVER, stringResource(R.string.settings_category_screensaver), Icons.Default.Bedtime),
        Triple(SettingsSection.IPTV, stringResource(R.string.settings_category_iptv), Icons.Default.LiveTv),
        Triple(SettingsSection.REMOTE_CONTROLS, stringResource(R.string.settings_category_remote), Icons.Default.SettingsRemote)
    )

    val activeNav = navItems.first { it.first == currentSection }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060A14),
                        TvMidnightBackground,
                        Color(0xFF04070E)
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(TvAccentGold.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.14f, size.height * 0.14f),
                        radius = size.width * 0.42f
                    ),
                    center = Offset(size.width * 0.14f, size.height * 0.14f),
                    radius = size.width * 0.42f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(TvAccentBlue.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.85f),
                        radius = size.width * 0.48f
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.85f),
                    radius = size.width * 0.48f
                )
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Category Navigation Drawer (Dark Glass Sidebar)
            Column(
                modifier = Modifier
                    .width(292.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0A0F1E).copy(alpha = 0.90f))
                    .padding(vertical = 24.dp)
            ) {
                // Header with Sleek Back Pill & Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvFocusableCard(
                        onClick = onBackToHome,
                        scaleUpRatio = 1.08f,
                        shape = RoundedCornerShape(12.dp),
                        useDefaultBorder = false,
                        modifier = Modifier.size(48.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isFocused) TvAccentGold
                                    else Color(0xFF151F35)
                                )
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isFocused) Color.Black else TvTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = stringResource(R.string.settings_title),
                        color = TvTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Subtle separator below sidebar header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(TvAccentGold.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation Items
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    navItems.forEachIndexed { index, (section, title, icon) ->
                        val isSelected = (section == currentSection)

                        TvFocusableCard(
                            onClick = { currentSection = section },
                            onFocused = { currentSection = section },
                            focusRequester = if (section == initialSection) initialSidebarFocus else remember { androidx.compose.ui.focus.FocusRequester() },
                            scaleUpRatio = 1.03f,
                            shape = RoundedCornerShape(14.dp),
                            useDefaultBorder = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) { isFocused ->
                            val rowBg by animateColorAsState(
                                targetValue = when {
                                    isFocused -> TvAccentGold
                                    isSelected -> TvAccentGold.copy(alpha = 0.16f)
                                    else -> Color(0xFF121B2F).copy(alpha = 0.55f)
                                },
                                animationSpec = tween(150),
                                label = "sidebar_bg"
                            )
                            val rowBorder by animateColorAsState(
                                targetValue = when {
                                    isFocused -> Color.White.copy(alpha = 0.95f)
                                    isSelected -> TvAccentGold.copy(alpha = 0.48f)
                                    else -> Color.White.copy(alpha = 0.06f)
                                },
                                animationSpec = tween(150),
                                label = "sidebar_border"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(rowBg)
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = rowBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Active left pill indicator
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                isFocused -> Color.Black
                                                isSelected -> TvAccentGoldRadiant
                                                else -> Color.Transparent
                                            }
                                        )
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isFocused -> Color.Black.copy(alpha = 0.14f)
                                                isSelected -> TvAccentGold.copy(alpha = 0.20f)
                                                else -> Color.White.copy(alpha = 0.05f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = when {
                                            isFocused -> Color.Black
                                            isSelected -> TvAccentGoldRadiant
                                            else -> TvTextSecondary
                                        },
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = title,
                                    color = when {
                                        isFocused -> Color.Black
                                        isSelected -> TvTextPrimary
                                        else -> TvTextSecondary
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "© Osama Salsa",
                    color = TvAccentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // Sleek Vertical Glass Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.12f),
                                TvAccentGold.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Right Panel: Section Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(horizontal = 36.dp, vertical = 24.dp)
            ) {
                // Section Title & Animated Status Toast
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TvAccentGold.copy(alpha = 0.15f))
                                .border(
                                    width = 1.dp,
                                    color = TvAccentGold.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = activeNav.third,
                                contentDescription = null,
                                tint = TvAccentGoldRadiant,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = activeNav.second,
                            color = TvTextPrimary,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    AnimatedVisibility(
                        visible = statusMessage != null,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(250))
                    ) {
                        statusMessage?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(TvAccentGold.copy(alpha = 0.16f))
                                    .border(
                                        width = 1.dp,
                                        color = TvAccentGold.copy(alpha = 0.45f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = TvAccentGoldRadiant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = msg.asString(context),
                                        color = TvAccentGoldRadiant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    TvAccentGold.copy(alpha = 0.40f),
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Detail View
                when (currentSection) {
                    SettingsSection.SYSTEM -> {
                        val systemShortcuts = listOf(
                            SystemShortcutItem(
                                icon = Icons.Default.Wifi,
                                title = stringResource(R.string.settings_open_wifi),
                                desc = stringResource(R.string.settings_category_network),
                                onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_WIFI_SETTINGS) }
                            ),
                            SystemShortcutItem(
                                icon = Icons.Default.Settings,
                                title = stringResource(R.string.settings_open_ethernet),
                                desc = stringResource(R.string.settings_category_network),
                                onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_WIRELESS_SETTINGS) }
                            ),
                            SystemShortcutItem(
                                icon = Icons.Default.DisplaySettings,
                                title = stringResource(R.string.settings_open_display),
                                desc = stringResource(R.string.settings_open_display),
                                onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_DISPLAY_SETTINGS) }
                            ),
                            SystemShortcutItem(
                                icon = Icons.Default.VolumeUp,
                                title = stringResource(R.string.settings_open_sound),
                                desc = stringResource(R.string.settings_open_sound),
                                onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_SOUND_SETTINGS) }
                            ),
                            SystemShortcutItem(
                                icon = Icons.Default.Schedule,
                                title = stringResource(R.string.settings_open_datetime),
                                desc = stringResource(R.string.settings_open_datetime),
                                onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_DATE_SETTINGS) }
                            ),
                            SystemShortcutItem(
                                icon = Icons.Default.Settings,
                                title = stringResource(R.string.settings_open_all_settings),
                                desc = stringResource(R.string.settings_open_all_settings),
                                onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_SETTINGS) }
                            ),
                            SystemShortcutItem(
                                icon = Icons.Default.Info,
                                title = stringResource(R.string.settings_open_app_info),
                                desc = stringResource(R.string.settings_open_app_info_desc),
                                onClick = { viewModel.openAppInfo(context) }
                            ),
                            SystemShortcutItem(
                                icon = Icons.Default.Storage,
                                title = stringResource(R.string.settings_storage_manager_title),
                                desc = stringResource(R.string.settings_storage_manager_desc),
                                onClick = { showStorageManagerDialog = true }
                            )
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // Language Selection Card
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Language,
                                        title = stringResource(R.string.settings_language),
                                        subtitle = "Select application interface language / اختر لغة التطبيق"
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_language),
                                        options = listOf(
                                            "system" to stringResource(R.string.settings_language_system),
                                            "ar" to "العربية",
                                            "ar_rtl" to "العربية (RTL)",
                                            "en" to "English",
                                            "de" to "Deutsch",
                                            "fr" to "Français",
                                            "it" to "Italiano",
                                            "es" to "Español",
                                            "pt" to "Português",
                                            "ru" to "Русский",
                                            "tr" to "Türkçe",
                                            "pl" to "Polski",
                                            "nl" to "Nederlands",
                                            "el" to "Ελληνικά",
                                            "id" to "Bahasa Indonesia",
                                            "vi" to "Tiếng Việt",
                                            "th" to "ไทย",
                                            "zh" to "中文",
                                            "ja" to "日本語",
                                            "ko" to "한국어",
                                            "hi" to "हिन्दी",
                                            "ur" to "اردو (RTL)",
                                            "fa" to "فارسی (RTL)"
                                        ),
                                        selectedOption = appLanguage,
                                        onOptionSelected = { viewModel.setLanguage(it) }
                                    )
                                }
                            }

                            // Top Bar Settings & Control Center Icon Mode Card
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Settings,
                                        title = stringResource(R.string.settings_header_control_title),
                                        subtitle = stringResource(R.string.settings_header_control_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_header_control_label),
                                        options = listOf(
                                            "control_center" to stringResource(R.string.settings_header_mode_control_center),
                                            "direct_settings" to stringResource(R.string.settings_header_mode_direct_settings),
                                            "both" to stringResource(R.string.settings_header_mode_both)
                                        ),
                                        selectedOption = headerControlMode,
                                        onOptionSelected = { viewModel.setHeaderControlMode(it) }
                                    )
                                }
                            }

                            // System Shortcuts in compact 2-column grid for quick TV D-Pad navigation
                            items(systemShortcuts.chunked(2)) { rowShortcuts ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowShortcuts.forEach { shortcut ->
                                        SettingActionTile(
                                            icon = shortcut.icon,
                                            title = shortcut.title,
                                            desc = shortcut.desc,
                                            onClick = shortcut.onClick,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowShortcuts.size == 1) {
                                        // Balance last odd item nicely without empty void or awkward alignment
                                        // Since weight(1f) isn't needed if it spans or we balance cleanly:
                                        // Actually using a Spacer(weight(1f)) keeps consistent 2-column grid alignment!
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            if (!isDefaultLauncher) {
                                item {
                                    DefaultLauncherManagementCard(
                                        isDefault = isDefaultLauncher,
                                        onEnable = { requestDefaultLauncher() }
                                    )
                                }
                            }

                            item {
                                VoiceAssistantManagementCard(
                                    isDefaultAssistant = isDefaultAssistant,
                                    onEnable = { requestDefaultAssistant() }
                                )
                            }

                            if (!hasFilePermission) {
                                item {
                                    FilePermissionManagementCard(
                                        hasPermission = hasFilePermission,
                                        onGrant = { FilePermissionHelper.requestStoragePermission(context) }
                                    )
                                }
                            }

                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Security,
                                        title = stringResource(R.string.settings_privacy_title),
                                        subtitle = stringResource(R.string.settings_privacy_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_filter_mode),
                                        options = listOf(
                                            "off" to stringResource(R.string.settings_filter_off),
                                            "ads" to stringResource(R.string.settings_filter_ads),
                                            "family" to stringResource(R.string.settings_filter_family)
                                        ),
                                        selectedOption = dnsBlockerMode,
                                        onOptionSelected = { mode ->
                                            if (mode == "off") {
                                                viewModel.setDnsBlockerMode("off", context)
                                            } else {
                                                val vpnIntent = VpnService.prepare(context)
                                                if (vpnIntent != null) {
                                                    vpnLauncher.launch(vpnIntent)
                                                } else {
                                                    viewModel.setDnsBlockerMode(mode, context)
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            // App Lock PIN Customization Card
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Lock,
                                        title = stringResource(R.string.settings_app_lock_pin_title),
                                        subtitle = stringResource(R.string.settings_app_lock_pin_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TvTextField(
                                            value = pinInput,
                                            onValueChange = { next ->
                                                val digitsOnly = next.filter { it.isDigit() }.take(4)
                                                pinInput = digitsOnly
                                            },
                                            placeholder = stringResource(R.string.settings_app_lock_pin_placeholder),
                                            leadingIcon = Icons.Default.Lock,
                                            modifier = Modifier.weight(1f)
                                        )

                                        TvActionButton(
                                            text = stringResource(R.string.action_save),
                                            icon = Icons.Default.Check,
                                            onClick = { viewModel.setAppLockPin(pinInput) },
                                            baseColor = TvAccentGold.copy(alpha = 0.88f),
                                            focusedColor = TvAccentGold,
                                            fillWidth = false,
                                            modifier = Modifier.height(52.dp)
                                        )
                                    }
                                }
                            }

                            // App Updates (GitHub Auto-Update) Card
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.SystemUpdate,
                                        title = stringResource(R.string.settings_updates_title),
                                        subtitle = stringResource(
                                            R.string.settings_updates_desc,
                                            com.example.BuildConfig.VERSION_NAME,
                                            com.example.BuildConfig.VERSION_CODE
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_updates_auto_check),
                                        options = listOf(
                                            true to stringResource(R.string.settings_seconds_enabled),
                                            false to stringResource(R.string.settings_seconds_disabled)
                                        ),
                                        selectedOption = autoCheckUpdates,
                                        onOptionSelected = { viewModel.setAutoCheckUpdates(it) }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TvActionButton(
                                        text = if (isCheckingUpdates) {
                                            stringResource(R.string.settings_updates_checking)
                                        } else {
                                            stringResource(R.string.settings_updates_check_now)
                                        },
                                        icon = Icons.Default.Refresh,
                                        onClick = { viewModel.checkForUpdates(onUpdateAvailable) },
                                        baseColor = TvAccentGold.copy(alpha = 0.88f),
                                        focusedColor = TvAccentGold,
                                        fillWidth = true
                                    )
                                }
                            }
                        }
                    }

                    SettingsSection.LAUNCHER -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // Top Bar Settings & Control Center Icon Mode Card
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Settings,
                                        title = stringResource(R.string.settings_header_control_title),
                                        subtitle = stringResource(R.string.settings_header_control_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_header_control_label),
                                        options = listOf(
                                            "control_center" to stringResource(R.string.settings_header_mode_control_center),
                                            "direct_settings" to stringResource(R.string.settings_header_mode_direct_settings),
                                            "both" to stringResource(R.string.settings_header_mode_both)
                                        ),
                                        selectedOption = headerControlMode,
                                        onOptionSelected = { viewModel.setHeaderControlMode(it) }
                                    )
                                }
                            }

                            // Wallpaper Style & Background Selector Card
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Wallpaper,
                                        title = stringResource(R.string.settings_wallpaper_style),
                                        subtitle = stringResource(R.string.settings_wallpaper_style_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_wallpaper_style),
                                        options = listOf(
                                            "dynamic" to stringResource(R.string.settings_wallpaper_dynamic),
                                            "preset" to stringResource(R.string.settings_wallpaper_presets),
                                            "custom" to stringResource(R.string.settings_wallpaper_pick_device),
                                            "default" to stringResource(R.string.settings_wallpaper_default)
                                        ),
                                        selectedOption = when (wallpaperMode) {
                                            "custom" -> "custom"
                                            "preset" -> "preset"
                                            "dynamic" -> "dynamic"
                                            else -> "default"
                                        },
                                        onOptionSelected = { mode ->
                                            if (mode == "dynamic") {
                                                viewModel.setWallpaperMode("dynamic")
                                                viewModel.setDynamicBgEnabled(true)
                                            } else if (mode == "custom") {
                                                imagePickerLauncher.launch("image/*")
                                            } else {
                                                viewModel.setWallpaperMode(mode)
                                            }
                                        }
                                    )

                                    if (wallpaperMode == "custom") {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = stringResource(R.string.settings_wallpaper_custom),
                                                color = TvTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TvActionButton(
                                                text = stringResource(R.string.settings_wallpaper_pick_device),
                                                icon = Icons.Default.Image,
                                                onClick = { imagePickerLauncher.launch("image/*") },
                                                baseColor = Color(0xFF23304A),
                                                focusedColor = TvAccentGold
                                            )
                                        }
                                    } else if (wallpaperMode == "preset") {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        OnlineWallpaperSelectionGrid(
                                            selectedWallpaperUrl = customWallpaperUrl,
                                            onWallpaperSelected = { fullUrl ->
                                                viewModel.setWallpaperMode("preset")
                                                viewModel.setCustomWallpaperUrl(fullUrl)
                                            }
                                        )
                                    }
                                }
                            }

                            // Clock Display Settings
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Schedule,
                                        title = stringResource(R.string.settings_clock_settings),
                                        subtitle = stringResource(R.string.settings_clock_settings_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_clock_format),
                                        options = listOf(
                                            false to "12h (AM/PM)",
                                            true to "24h"
                                        ),
                                        selectedOption = isClock24h,
                                        onOptionSelected = { viewModel.setClock24h(it) }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_clock_seconds),
                                        options = listOf(
                                            false to stringResource(R.string.settings_seconds_disabled),
                                            true to stringResource(R.string.settings_seconds_enabled)
                                        ),
                                        selectedOption = isClockSecondsEnabled,
                                        onOptionSelected = { viewModel.setClockSecondsEnabled(it) }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_clock_size),
                                        options = listOf(
                                            "large" to stringResource(R.string.settings_clock_size_large),
                                            "medium" to stringResource(R.string.settings_clock_size_medium),
                                            "compact" to stringResource(R.string.settings_clock_size_compact)
                                        ),
                                        selectedOption = clockSize,
                                        onOptionSelected = { viewModel.setClockSize(it) }
                                    )
                                }
                            }

                            // Date Display Settings
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.CalendarMonth,
                                        title = stringResource(R.string.settings_date_settings),
                                        subtitle = stringResource(R.string.settings_date_settings_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    val sampleNow = remember(appLanguage) { java.util.Date() }
                                    fun withDatePreview(styleKey: String, label: String): Pair<String, String> {
                                        if (styleKey == "hidden") return styleKey to label
                                        val preview = com.example.util.LocaleHelper.formatDate(sampleNow, styleKey, appLanguage)
                                        return styleKey to "$label ($preview)"
                                    }
                                    TvSelectField(
                                        label = stringResource(R.string.settings_date_format),
                                        options = listOf(
                                            withDatePreview("full", stringResource(R.string.settings_date_format_full)),
                                            withDatePreview("full_year", stringResource(R.string.settings_date_format_full_year)),
                                            withDatePreview("us_full", stringResource(R.string.settings_date_format_us_full)),
                                            withDatePreview("us_written", stringResource(R.string.settings_date_format_us_written)),
                                            withDatePreview("us_short", stringResource(R.string.settings_date_format_us_short)),
                                            withDatePreview("medium", stringResource(R.string.settings_date_format_medium)),
                                            withDatePreview("numeric_us", stringResource(R.string.settings_date_format_numeric_us)),
                                            withDatePreview("numeric", stringResource(R.string.settings_date_format_numeric)),
                                            withDatePreview("numeric_iso", stringResource(R.string.settings_date_format_numeric_iso)),
                                            withDatePreview("numeric_dots", stringResource(R.string.settings_date_format_numeric_dots)),
                                            "hidden" to stringResource(R.string.settings_date_format_hidden)
                                        ),
                                        selectedOption = dateFormatStyle,
                                        onOptionSelected = { viewModel.setDateFormatStyle(it) }
                                    )
                                }
                            }

                            // Greeting Sentence Settings
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.AutoAwesome,
                                        title = stringResource(R.string.settings_greeting_settings),
                                        subtitle = stringResource(R.string.settings_greeting_settings_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_greeting_mode),
                                        options = listOf(
                                            "poetic" to stringResource(R.string.settings_greeting_mode_poetic),
                                            "classic" to stringResource(R.string.settings_greeting_mode_classic),
                                            "custom" to stringResource(R.string.settings_greeting_mode_custom),
                                            "hidden" to stringResource(R.string.settings_greeting_mode_hidden)
                                        ),
                                        selectedOption = greetingMode,
                                        onOptionSelected = { viewModel.setGreetingMode(it) }
                                    )

                                    if (greetingMode == "custom") {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = stringResource(R.string.settings_custom_greeting_title),
                                            color = TvAccentGoldRadiant,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = stringResource(R.string.settings_custom_greeting_desc),
                                            color = TvTextSecondary,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TvTextField(
                                                value = customGreetingInput,
                                                onValueChange = { customGreetingInput = it },
                                                placeholder = stringResource(R.string.settings_custom_greeting_hint),
                                                modifier = Modifier.weight(1f)
                                            )

                                            TvActionButton(
                                                text = stringResource(R.string.settings_custom_greeting_save),
                                                icon = Icons.Default.Check,
                                                onClick = {
                                                    viewModel.setCustomGreetingText(customGreetingInput.trim())
                                                    viewModel.setGreetingMode("custom")
                                                },
                                                baseColor = TvAccentGold.copy(alpha = 0.88f),
                                                focusedColor = TvAccentGold,
                                                modifier = Modifier.height(52.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Apps per Row Selector
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.GridView,
                                        title = stringResource(R.string.settings_apps_per_row),
                                        subtitle = stringResource(R.string.settings_apps_per_row_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_items_count),
                                        options = listOf(4, 5, 6, 7).map { it to "$it " + stringResource(R.string.settings_apps_suffix) },
                                        selectedOption = appsPerRow,
                                        onOptionSelected = { viewModel.setAppsPerRow(it) }
                                    )
                                }
                            }

                            // Channels Glass Opacity Selector
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Opacity,
                                        title = stringResource(R.string.settings_glass_opacity),
                                        subtitle = stringResource(R.string.settings_glass_opacity_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_opacity_level),
                                        options = listOf(25, 50, 75, 90, 100).map { it to "$it%" },
                                        selectedOption = glassOpacity,
                                        onOptionSelected = { viewModel.setGlassOpacity(it) }
                                    )
                                }
                            }

                            // Apps Start Position / Wallpaper View Selector
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.VerticalAlignBottom,
                                        title = stringResource(R.string.settings_apps_top_position),
                                        subtitle = stringResource(R.string.settings_apps_top_position_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_apps_top_position),
                                        options = listOf(
                                            250 to stringResource(R.string.settings_apps_position_high),
                                            300 to stringResource(R.string.settings_apps_position_compact),
                                            350 to stringResource(R.string.settings_apps_position_balanced),
                                            400 to stringResource(R.string.settings_apps_position_medium_low),
                                            450 to stringResource(R.string.settings_apps_position_lower),
                                            500 to stringResource(R.string.settings_apps_position_ultra)
                                        ),
                                        selectedOption = appsTopOffset,
                                        onOptionSelected = { viewModel.setAppsTopOffset(it) }
                                    )
                                }
                            }

                            // Displayed Apps Filter (All vs TV Only)
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Apps,
                                        title = stringResource(R.string.settings_show_only_tv_apps),
                                        subtitle = stringResource(R.string.settings_show_only_tv_apps_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_display_preference),
                                        options = listOf(
                                            false to stringResource(R.string.settings_app_filter_all),
                                            true to stringResource(R.string.settings_app_filter_tv_only)
                                        ),
                                        selectedOption = showOnlyTvApps,
                                        onOptionSelected = { viewModel.setShowOnlyTvApps(it) }
                                    )
                                }
                            }

                            // Recent Apps Horizontal Strip Toggle
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.History,
                                        title = stringResource(R.string.settings_recent_apps_title),
                                        subtitle = stringResource(R.string.settings_recent_apps_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_recent_apps_title),
                                        options = listOf(
                                            false to stringResource(R.string.settings_seconds_disabled),
                                            true to stringResource(R.string.settings_seconds_enabled)
                                        ),
                                        selectedOption = showRecentApps,
                                        onOptionSelected = { viewModel.setShowRecentApps(it) }
                                    )
                                }
                            }

                            // Material 3 Dynamic Color Toggle (Android 12+)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                item {
                                    SettingsGlassCard {
                                        SettingsCardHeader(
                                            icon = Icons.Default.ColorLens,
                                            title = stringResource(R.string.settings_dynamic_color_title),
                                            subtitle = stringResource(R.string.settings_dynamic_color_desc)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        TvSelectField(
                                            label = stringResource(R.string.settings_dynamic_color_title),
                                            options = listOf(
                                                false to stringResource(R.string.settings_seconds_disabled),
                                                true to stringResource(R.string.settings_seconds_enabled)
                                            ),
                                            selectedOption = useDynamicColor,
                                            onOptionSelected = { viewModel.setUseDynamicColor(it) }
                                        )
                                    }
                                }
                            }

                            // Custom Web Bookmarks / Portals
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Public,
                                        title = stringResource(R.string.settings_web_bookmarks_title),
                                        subtitle = stringResource(R.string.settings_web_bookmarks_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TvTextField(
                                            value = newBookmarkName,
                                            onValueChange = { newBookmarkName = it },
                                            placeholder = stringResource(R.string.settings_web_bookmark_name_hint),
                                            modifier = Modifier.weight(0.9f)
                                        )
                                        TvTextField(
                                            value = newBookmarkUrl,
                                            onValueChange = { newBookmarkUrl = it },
                                            placeholder = stringResource(R.string.settings_web_bookmark_url_hint),
                                            modifier = Modifier.weight(1.4f)
                                        )
                                        TvActionButton(
                                            text = stringResource(R.string.settings_web_bookmark_add),
                                            icon = Icons.Default.Add,
                                            onClick = {
                                                if (newBookmarkName.isNotBlank() && newBookmarkUrl.isNotBlank()) {
                                                    viewModel.addCustomWebBookmark(newBookmarkName, newBookmarkUrl)
                                                    newBookmarkName = ""
                                                    newBookmarkUrl = ""
                                                }
                                            },
                                            baseColor = TvAccentGold.copy(alpha = 0.88f),
                                            focusedColor = TvAccentGold,
                                            modifier = Modifier.height(52.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    if (customWebBookmarks.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.settings_web_bookmark_empty),
                                            color = TvTextSecondary,
                                            fontSize = 12.5.sp
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            customWebBookmarks.forEach { bm ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0xFF0A101F).copy(alpha = 0.7f))
                                                        .border(
                                                            width = 1.dp,
                                                            color = Color.White.copy(alpha = 0.06f),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 52.dp, height = 34.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFF162238)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (bm.iconUrl.isNotBlank()) {
                                                            BookmarkTileImage(
                                                                iconUrl = bm.iconUrl,
                                                                contentDescription = bm.title,
                                                                modifier = Modifier.fillMaxSize(),
                                                                iconVerticalPadding = 2.dp,
                                                                iconHorizontalPadding = 3.dp
                                                            )
                                                        } else {
                                                            Icon(
                                                                imageVector = Icons.Default.Public,
                                                                contentDescription = null,
                                                                tint = TvAccentBlue,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = bm.title,
                                                            color = TvTextPrimary,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = bm.url,
                                                            color = TvTextSecondary,
                                                            fontSize = 11.5.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    TvActionButton(
                                                        text = stringResource(R.string.action_delete),
                                                        icon = Icons.Default.Delete,
                                                        onClick = { viewModel.removeCustomWebBookmark(bm.id) },
                                                        baseColor = TvAccentRed.copy(alpha = 0.25f),
                                                        focusedColor = TvAccentRed,
                                                        modifier = Modifier.height(38.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Night Comfort Dimming, Low-RAM Mode & Full Backup/Restore
                            item {
                                BackupAndPerformanceSettingsCards(
                                    context = context,
                                    appLanguage = com.example.util.LocaleHelper.getEffectiveLanguage(appLanguage),
                                    lowRamMode = lowRamMode,
                                    nightComfortMode = nightComfortMode,
                                    onSetLowRamMode = { viewModel.setLowRamMode(it) },
                                    onSetNightComfortMode = { viewModel.setNightComfortMode(it) },
                                    onExportBackup = { viewModel.exportBackup(context) },
                                    onImportBackup = { viewModel.importBackup(context) }
                                )
                            }

                            // Reset App Order
                            item {
                                SettingActionTile(
                                    icon = Icons.Default.Restore,
                                    title = stringResource(R.string.settings_reset_order),
                                    desc = stringResource(R.string.settings_reset_order_desc),
                                    onClick = { viewModel.resetAppOrder() }
                                )
                            }

                            // Hidden Apps Section
                            item {
                                val hiddenList = allApps.filter { it.isHidden }
                                SettingsGlassCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            SettingsCardHeader(
                                                icon = Icons.Default.VisibilityOff,
                                                title = stringResource(R.string.settings_hidden_apps),
                                                subtitle = if (hiddenList.isEmpty()) stringResource(R.string.settings_no_hidden_apps) else null
                                            )
                                        }

                                        if (hiddenList.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(12.dp))
                                            TvActionButton(
                                                text = stringResource(R.string.action_unhide),
                                                icon = Icons.Default.Visibility,
                                                onClick = { viewModel.unhideAllApps() },
                                                baseColor = TvAccentGold.copy(alpha = 0.88f),
                                                focusedColor = TvAccentGold
                                            )
                                        }
                                    }

                                    if (hiddenList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            hiddenList.forEach { app ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0xFF0A101F).copy(alpha = 0.7f))
                                                        .border(
                                                            width = 1.dp,
                                                            color = Color.White.copy(alpha = 0.06f),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = app.label,
                                                        color = TvTextPrimary,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.weight(1f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    TvActionButton(
                                                        text = stringResource(R.string.action_unhide),
                                                        icon = Icons.Default.Visibility,
                                                        onClick = { viewModel.unhideApp(app.packageName) },
                                                        baseColor = Color(0xFF23304A),
                                                        focusedColor = TvAccentGold,
                                                        modifier = Modifier.height(38.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsSection.SCREENSAVER -> {
                        ScreensaverSettingsSection(
                            context = context,
                            viewModel = viewModel,
                            screensaverTimeout = screensaverTimeout,
                            screensaverMode = screensaverMode,
                            screensaverVideo = screensaverVideo,
                            customScreensaverVideos = customScreensaverVideos,
                            deletedScreensaverPresets = deletedScreensaverPresets,
                            slideshowSource = slideshowSource,
                            slideshowFolderUri = slideshowFolderUri,
                            slideshowCustomPhotos = slideshowCustomPhotos,
                            slideshowIntervalSec = slideshowIntervalSec,
                            slideshowMotion = slideshowMotion,
                            slideshowTransition = slideshowTransition,
                            slideshowEffect = slideshowEffect,
                            slideshowClockMode = slideshowClockMode,
                            slideshowMusicMode = slideshowMusicMode,
                            slideshowCustomMusicUri = slideshowCustomMusicUri,
                            slideshowMusicVolume = slideshowMusicVolume,
                            slideshowShuffle = slideshowShuffle,
                            slideshowDimLevel = slideshowDimLevel,
                            onShowTvFolderPicker = { showTvFolderPickerDialog = true },
                            onLaunchMultiPhotosPicker = {
                                try {
                                    slideshowMultiPhotosLauncher.launch("image/*")
                                } catch (_: Exception) {}
                            },
                            onLaunchMusicPicker = {
                                try {
                                    slideshowMusicPickerLauncher.launch("audio/*")
                                } catch (_: Exception) {}
                            },
                            onPreviewScreensaver = { showPreviewScreensaver = true }
                        )
                    }

                    SettingsSection.IPTV -> {
                        IptvSettingsSection(
                            viewModel = viewModel,
                            iptvPlaylists = iptvPlaylists,
                            activeIptvPlaylist = activeIptvPlaylist,
                            autoResumeLastChannel = autoResumeLastChannel,
                            epgUrlOverride = epgUrlOverride
                        )
                    }

                    SettingsSection.REMOTE_CONTROLS -> {
                        RemoteGuideSection()
                    }
                }
            }
        }

        if (showPreviewScreensaver) {
            AmbientScreensaverOverlay(
                is24hFormat = isClock24h,
                auroraColors = AuroraColors(),
                screensaverMode = screensaverMode,
                screensaverVideoId = screensaverVideo,
                wallpaperMode = if (screensaverWallpaper == "current") wallpaperMode else "custom",
                customWallpaperUrl = if (screensaverWallpaper == "current") customWallpaperUrl else screensaverWallpaper,
                onDismiss = { showPreviewScreensaver = false }
            )
        }

        if (showStorageManagerDialog) {
            AppStorageManagerDialog(
                onDismiss = { showStorageManagerDialog = false }
            )
        }

        if (showTvFolderPickerDialog) {
            com.example.ui.home.TvFolderPickerDialog(
                currentFolderUri = slideshowFolderUri,
                onFolderSelected = { pathOrUri ->
                    viewModel.setSlideshowFolderUri(pathOrUri)
                },
                onLaunchSystemPicker = {
                    try {
                        folderTreePickerLauncher.launch(null)
                    } catch (_: Exception) {}
                },
                onDismiss = { showTvFolderPickerDialog = false }
            )
        }
    }
}
