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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
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
    SYSTEM, LAUNCHER, SCREENSAVER, IPTV
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
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    var currentSection by remember { mutableStateOf(SettingsSection.SYSTEM) }
    val initialSidebarFocus = remember { androidx.compose.ui.focus.FocusRequester() }

    val isDynamicBg by viewModel.isDynamicBgEnabled.collectAsState()
    val wallpaperMode by viewModel.wallpaperMode.collectAsState()
    val customWallpaperUrl by viewModel.customWallpaperUrl.collectAsState()
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
    val glassOpacity by viewModel.glassOpacity.collectAsState()
    val showOnlyTvApps by viewModel.showOnlyTvApps.collectAsState()
    val appLanguage by viewModel.language.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val screensaverTimeout by viewModel.screensaverTimeout.collectAsState()
    val screensaverMode by viewModel.screensaverMode.collectAsState()
    val screensaverWallpaper by viewModel.screensaverWallpaper.collectAsState()
    val screensaverVideo by viewModel.screensaverVideo.collectAsState()
    val customScreensaverVideos by viewModel.customScreensaverVideos.collectAsState()
    val deletedScreensaverPresets by viewModel.deletedScreensaverPresets.collectAsState()
    var showPreviewScreensaver by remember { mutableStateOf(false) }

    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistUrl by remember { mutableStateOf("") }
    @Suppress("UNUSED_VARIABLE")
    var customWallpaperInput by remember(customWallpaperUrl) { mutableStateOf(customWallpaperUrl) }
    var customVideoInput by remember { mutableStateOf("") }

    var isDefaultLauncher by remember { mutableStateOf(viewModel.isDefaultLauncher(context)) }
    var hasFilePermission by remember { mutableStateOf(FilePermissionHelper.hasStoragePermission(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefaultLauncher = viewModel.isDefaultLauncher(context)
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
        Triple(SettingsSection.IPTV, stringResource(R.string.settings_category_iptv), Icons.Default.LiveTv)
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
                        modifier = Modifier.size(44.dp)
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
                            focusRequester = if (index == 0) initialSidebarFocus else remember { androidx.compose.ui.focus.FocusRequester() },
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
                                            "fr" to "Français",
                                            "it" to "Italiano",
                                            "ru" to "Русский",
                                            "es" to "Español"
                                        ),
                                        selectedOption = appLanguage,
                                        onOptionSelected = { viewModel.setLanguage(it) }
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
                        }
                    }

                    SettingsSection.LAUNCHER -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {

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
                                    TvSelectField(
                                        label = stringResource(R.string.settings_date_format),
                                        options = listOf(
                                            "full" to stringResource(R.string.settings_date_format_full),
                                            "medium" to stringResource(R.string.settings_date_format_medium),
                                            "numeric" to stringResource(R.string.settings_date_format_numeric),
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
                                            220 to stringResource(R.string.settings_apps_position_compact),
                                            280 to stringResource(R.string.settings_apps_position_balanced),
                                            340 to stringResource(R.string.settings_apps_position_lower),
                                            400 to stringResource(R.string.settings_apps_position_ultra)
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
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // Inactivity Sleep Timer Card
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Timer,
                                        title = stringResource(R.string.settings_screensaver_timeout),
                                        subtitle = stringResource(R.string.settings_screensaver_timeout_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_screensaver_timeout),
                                        options = listOf(
                                            "2" to stringResource(R.string.timeout_2min),
                                            "5" to stringResource(R.string.timeout_5min),
                                            "10" to stringResource(R.string.timeout_10min),
                                            "15" to stringResource(R.string.timeout_15min),
                                            "30" to stringResource(R.string.timeout_30min),
                                            "never" to stringResource(R.string.timeout_never)
                                        ),
                                        selectedOption = screensaverTimeout,
                                        onOptionSelected = { viewModel.setScreensaverTimeout(it) }
                                    )
                                }
                            }

                            // Screensaver Mode Selector Card
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.Bedtime,
                                        title = stringResource(R.string.settings_screensaver_mode),
                                        subtitle = stringResource(R.string.settings_screensaver_mode_desc)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_screensaver_mode),
                                        options = listOf(
                                            "clock_date" to stringResource(R.string.screensaver_mode_clock),
                                            "wallpaper_info" to stringResource(R.string.screensaver_mode_wallpaper),
                                            "video_stream" to stringResource(R.string.screensaver_mode_video)
                                        ),
                                        selectedOption = screensaverMode,
                                        onOptionSelected = { viewModel.setScreensaverMode(it) }
                                    )
                                }
                            }

                            if (screensaverMode == "video_stream") {
                                val activeVideoId = extractYouTubeVideoId(screensaverVideo)
                                val visiblePresets = screensaverPresetVideos.filterNot {
                                    deletedScreensaverPresets.contains(it.id)
                                }
                                val presetIds = screensaverPresetVideos.map { it.id }.toSet()
                                val customVideoEntries = buildList {
                                    customScreensaverVideos.forEach { raw ->
                                        val id = raw.substringBefore("|").trim()
                                        val customTitle = raw.substringAfter("|", "").trim()
                                        if (id.isNotBlank() && !presetIds.contains(id)) {
                                            add(id to customTitle)
                                        }
                                    }
                                    if (
                                        activeVideoId.isNotBlank() &&
                                        !presetIds.contains(activeVideoId) &&
                                        !deletedScreensaverPresets.contains(activeVideoId) &&
                                        none { it.first == activeVideoId }
                                    ) {
                                        add(activeVideoId to "")
                                    }
                                }

                                // Ambient Video Selection Card (compact merged custom video input preserved)
                                item {
                                    SettingsGlassCard {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                SettingsCardHeader(
                                                    icon = Icons.Default.SmartDisplay,
                                                    title = stringResource(R.string.screensaver_video_choice),
                                                    subtitle = stringResource(R.string.screensaver_video_choice_desc)
                                                )
                                            }
                                            if (deletedScreensaverPresets.isNotEmpty()) {
                                                Spacer(modifier = Modifier.width(12.dp))
                                                TvActionButton(
                                                    text = stringResource(R.string.screensaver_video_restore_defaults),
                                                    icon = Icons.Default.Refresh,
                                                    onClick = { viewModel.restoreDefaultScreensaverVideos() },
                                                    baseColor = Color(0xFF23304A),
                                                    focusedColor = TvAccentGold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            if (visiblePresets.isEmpty() && customVideoEntries.isEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0xFF0A101F))
                                                        .padding(18.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.screensaver_no_videos),
                                                        color = TvTextSecondary,
                                                        fontSize = 13.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }

                                            visiblePresets.forEach { presetVideo ->
                                                val isSelected = activeVideoId == presetVideo.id
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TvFocusableCard(
                                                        onClick = { viewModel.setScreensaverVideo(presetVideo.id) },
                                                        scaleUpRatio = 1.02f,
                                                        shape = RoundedCornerShape(12.dp),
                                                        useDefaultBorder = false,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(72.dp)
                                                    ) { isFocused ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(
                                                                    when {
                                                                        isFocused -> TvAccentGold.copy(alpha = 0.22f)
                                                                        isSelected -> TvAccentGold.copy(alpha = 0.16f)
                                                                        else -> Color(0xFF0A101F).copy(alpha = 0.85f)
                                                                    }
                                                                )
                                                                .border(
                                                                    width = if (isFocused) 2.dp else if (isSelected) 1.5.dp else 1.dp,
                                                                    color = when {
                                                                        isFocused -> TvAccentGoldRadiant
                                                                        isSelected -> TvAccentGold.copy(alpha = 0.7f)
                                                                        else -> Color.White.copy(alpha = 0.08f)
                                                                    },
                                                                    shape = RoundedCornerShape(12.dp)
                                                                )
                                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .width(88.dp)
                                                                    .height(54.dp)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color.Black)
                                                            ) {
                                                                AsyncImage(
                                                                    model = "https://img.youtube.com/vi/${presetVideo.id}/hqdefault.jpg",
                                                                    contentDescription = null,
                                                                    contentScale = ContentScale.Crop,
                                                                    modifier = Modifier.fillMaxSize()
                                                                )
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(24.dp)
                                                                        .align(Alignment.Center)
                                                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.PlayArrow,
                                                                        contentDescription = null,
                                                                        tint = Color.White,
                                                                        modifier = Modifier
                                                                            .size(16.dp)
                                                                            .align(Alignment.Center)
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.width(16.dp))

                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = stringResource(presetVideo.titleRes),
                                                                    color = if (isSelected || isFocused) TvTextPrimary else TvTextPrimary.copy(alpha = 0.9f),
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Text(
                                                                    text = stringResource(presetVideo.subtitleRes),
                                                                    color = TvTextSecondary,
                                                                    fontSize = 11.sp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.width(10.dp))

                                                            if (isSelected) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(28.dp)
                                                                        .background(TvAccentGold, CircleShape),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Check,
                                                                        contentDescription = "Selected",
                                                                        tint = Color.Black,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    TvFocusableCard(
                                                        onClick = { viewModel.deleteScreensaverVideo(presetVideo.id) },
                                                        scaleUpRatio = 1.06f,
                                                        shape = RoundedCornerShape(12.dp),
                                                        useDefaultBorder = false,
                                                        modifier = Modifier
                                                            .width(56.dp)
                                                            .height(72.dp)
                                                    ) { isDelFocused ->
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(
                                                                    if (isDelFocused) TvAccentRed
                                                                    else TvAccentRed.copy(alpha = 0.16f)
                                                                )
                                                                .border(
                                                                    width = if (isDelFocused) 2.dp else 1.dp,
                                                                    color = if (isDelFocused) Color.White else TvAccentRed.copy(alpha = 0.4f),
                                                                    shape = RoundedCornerShape(12.dp)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = stringResource(R.string.action_delete),
                                                                tint = if (isDelFocused) Color.White else TvAccentRed,
                                                                modifier = Modifier.size(22.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            customVideoEntries.forEach { (customId, customTitle) ->
                                                val isSelected = activeVideoId == customId
                                                val displayTitle = if (customTitle.isNotBlank()) {
                                                    customTitle
                                                } else {
                                                    stringResource(R.string.screensaver_video_custom_item_title).format(customId)
                                                }
                                                val displaySub = stringResource(R.string.screensaver_video_custom_item_sub).format(customId)

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TvFocusableCard(
                                                        onClick = { viewModel.setScreensaverVideo(customId) },
                                                        scaleUpRatio = 1.02f,
                                                        shape = RoundedCornerShape(12.dp),
                                                        useDefaultBorder = false,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(72.dp)
                                                    ) { isFocused ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(
                                                                    when {
                                                                        isFocused -> TvAccentGold.copy(alpha = 0.22f)
                                                                        isSelected -> TvAccentGold.copy(alpha = 0.16f)
                                                                        else -> Color(0xFF0A101F).copy(alpha = 0.85f)
                                                                    }
                                                                )
                                                                .border(
                                                                    width = if (isFocused) 2.dp else if (isSelected) 1.5.dp else 1.dp,
                                                                    color = when {
                                                                        isFocused -> TvAccentGoldRadiant
                                                                        isSelected -> TvAccentGold.copy(alpha = 0.7f)
                                                                        else -> Color.White.copy(alpha = 0.08f)
                                                                    },
                                                                    shape = RoundedCornerShape(12.dp)
                                                                )
                                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .width(88.dp)
                                                                    .height(54.dp)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color.Black)
                                                            ) {
                                                                AsyncImage(
                                                                    model = "https://img.youtube.com/vi/$customId/hqdefault.jpg",
                                                                    contentDescription = null,
                                                                    contentScale = ContentScale.Crop,
                                                                    modifier = Modifier.fillMaxSize()
                                                                )
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(24.dp)
                                                                        .align(Alignment.Center)
                                                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.PlayArrow,
                                                                        contentDescription = null,
                                                                        tint = Color.White,
                                                                        modifier = Modifier
                                                                            .size(16.dp)
                                                                            .align(Alignment.Center)
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.width(16.dp))

                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = displayTitle,
                                                                    color = if (isSelected || isFocused) TvTextPrimary else TvTextPrimary.copy(alpha = 0.9f),
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Text(
                                                                    text = displaySub,
                                                                    color = TvTextSecondary,
                                                                    fontSize = 11.sp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.width(10.dp))

                                                            if (isSelected) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(28.dp)
                                                                        .background(TvAccentGold, CircleShape),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Check,
                                                                        contentDescription = "Selected",
                                                                        tint = Color.Black,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    TvFocusableCard(
                                                        onClick = { viewModel.deleteScreensaverVideo(customId) },
                                                        scaleUpRatio = 1.06f,
                                                        shape = RoundedCornerShape(12.dp),
                                                        useDefaultBorder = false,
                                                        modifier = Modifier
                                                            .width(56.dp)
                                                            .height(72.dp)
                                                    ) { isDelFocused ->
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(
                                                                    if (isDelFocused) TvAccentRed
                                                                    else TvAccentRed.copy(alpha = 0.16f)
                                                                )
                                                                .border(
                                                                    width = if (isDelFocused) 2.dp else 1.dp,
                                                                    color = if (isDelFocused) Color.White else TvAccentRed.copy(alpha = 0.4f),
                                                                    shape = RoundedCornerShape(12.dp)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = stringResource(R.string.action_delete),
                                                                tint = if (isDelFocused) Color.White else TvAccentRed,
                                                                modifier = Modifier.size(22.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Compact integrated custom video input section
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(Color.White.copy(alpha = 0.08f))
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = stringResource(R.string.screensaver_video_custom_title),
                                                color = TvTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = stringResource(R.string.screensaver_video_custom_desc),
                                                color = TvTextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TvTextField(
                                                    value = customVideoInput,
                                                    onValueChange = { customVideoInput = it },
                                                    placeholder = stringResource(R.string.screensaver_video_custom_hint),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                TvActionButton(
                                                    text = stringResource(R.string.screensaver_video_custom_save),
                                                    icon = Icons.Default.Add,
                                                    onClick = {
                                                        if (customVideoInput.isNotBlank()) {
                                                            viewModel.addCustomScreensaverVideo(customVideoInput.trim())
                                                            customVideoInput = ""
                                                        }
                                                    },
                                                    baseColor = TvAccentGold.copy(alpha = 0.88f),
                                                    focusedColor = TvAccentGold,
                                                    modifier = Modifier.height(52.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Preview Screensaver Action Tile
                            item {
                                SettingActionTile(
                                    icon = Icons.Default.PlayArrow,
                                    title = stringResource(R.string.settings_screensaver_preview_btn),
                                    desc = stringResource(R.string.settings_screensaver_preview_desc),
                                    onClick = { showPreviewScreensaver = true }
                                )
                            }
                        }
                    }

                    SettingsSection.IPTV -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // Add New IPTV Playlist Card
                            item {
                                SettingsGlassCard {
                                    SettingsCardHeader(
                                        icon = Icons.Default.LiveTv,
                                        title = stringResource(R.string.settings_iptv_add_new)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        TvTextField(
                                            value = newPlaylistName,
                                            onValueChange = { newPlaylistName = it },
                                            placeholder = stringResource(R.string.settings_iptv_name_placeholder),
                                            modifier = Modifier.weight(1f)
                                        )

                                        TvTextField(
                                            value = newPlaylistUrl,
                                            onValueChange = { newPlaylistUrl = it },
                                            placeholder = stringResource(R.string.settings_iptv_url_placeholder),
                                            modifier = Modifier.weight(1.8f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        TvActionButton(
                                            text = stringResource(R.string.settings_iptv_add_button),
                                            icon = Icons.Default.Add,
                                            onClick = {
                                                if (newPlaylistUrl.isNotBlank()) {
                                                    viewModel.addIptvPlaylist(newPlaylistName, newPlaylistUrl)
                                                    newPlaylistName = ""
                                                    newPlaylistUrl = ""
                                                }
                                            },
                                            baseColor = TvAccentGold.copy(alpha = 0.88f),
                                            focusedColor = TvAccentGold
                                        )

                                        TvActionButton(
                                            text = stringResource(R.string.settings_iptv_load_presets),
                                            onClick = { viewModel.loadPresetPlaylists() },
                                            baseColor = Color(0xFF23304A),
                                            focusedColor = TvAccentGold
                                        )
                                    }
                                }
                            }

                            // Saved IPTV Playlists Header with Delete All Option
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_iptv_saved_header).format(iptvPlaylists.size),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (iptvPlaylists.isNotEmpty()) {
                                        TvActionButton(
                                            text = stringResource(R.string.settings_iptv_delete_all),
                                            icon = Icons.Default.Delete,
                                            onClick = { viewModel.deleteAllIptvPlaylists() },
                                            baseColor = TvAccentRed.copy(alpha = 0.25f),
                                            focusedColor = TvAccentRed
                                        )
                                    }
                                }
                            }

                            if (iptvPlaylists.isEmpty()) {
                                item {
                                    SettingsGlassCard {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(R.string.settings_iptv_no_saved),
                                                color = TvTextSecondary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                val playlistChunks = iptvPlaylists.chunked(2)
                                items(playlistChunks, key = { chunk -> chunk.first().id }) { chunk ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        chunk.forEach { playlist ->
                                            val isActive = (playlist.id == activeIptvPlaylist?.id)
                                            Box(modifier = Modifier.weight(1f)) {
                                                SettingsGlassCard(
                                                    highlightColor = TvAccentGold,
                                                    highlightActive = isActive
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(
                                                                    if (isActive) TvAccentGold.copy(alpha = 0.18f)
                                                                    else Color.White.copy(alpha = 0.08f)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.LiveTv,
                                                                contentDescription = null,
                                                                tint = if (isActive) TvAccentGoldRadiant else TvTextPrimary,
                                                                modifier = Modifier.size(19.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = playlist.name,
                                                                color = TvTextPrimary,
                                                                fontSize = 15.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                text = stringResource(R.string.settings_iptv_channels_count).format(playlist.channelCount),
                                                                color = if (isActive) TvAccentGoldRadiant else TvTextSecondary,
                                                                fontSize = 12.sp
                                                            )
                                                        }
                                                        if (isActive) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(TvAccentGold)
                                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                                            ) {
                                                                Text(
                                                                    text = stringResource(R.string.settings_iptv_active_tag),
                                                                    color = Color.Black,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.ExtraBold
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(10.dp))

                                                    Text(
                                                        text = playlist.url,
                                                        color = TvTextSecondary.copy(alpha = 0.72f),
                                                        fontSize = 11.5.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    Spacer(modifier = Modifier.height(14.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        if (!isActive) {
                                                            TvActionButton(
                                                                text = stringResource(R.string.settings_iptv_activate_button),
                                                                onClick = { viewModel.switchIptvPlaylist(playlist.id) },
                                                                baseColor = TvAccentGold,
                                                                focusedColor = TvAccentGold,
                                                                fillWidth = true,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        } else {
                                                            TvActionButton(
                                                                text = stringResource(R.string.settings_iptv_sync_button),
                                                                icon = Icons.Default.Refresh,
                                                                onClick = { viewModel.refreshIptvPlaylist() },
                                                                baseColor = TvAccentGold.copy(alpha = 0.85f),
                                                                focusedColor = TvAccentGold,
                                                                fillWidth = true,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }

                                                        TvActionButton(
                                                            text = stringResource(R.string.action_delete),
                                                            icon = Icons.Default.Delete,
                                                            onClick = { viewModel.deleteIptvPlaylist(playlist.id) },
                                                            baseColor = TvAccentRed.copy(alpha = 0.25f),
                                                            focusedColor = TvAccentRed
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (chunk.size < 2) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
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
    }
}
