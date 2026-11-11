package com.example.ui.settings

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Bedtime
import com.example.ui.home.AmbientScreensaverOverlay
import com.example.ui.home.screensaverPresetVideos
import com.example.ui.home.extractYouTubeVideoId
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.ui.common.AuroraColors
import com.example.util.FilePermissionHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import androidx.compose.foundation.BorderStroke
import com.example.R
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay

enum class SettingsSection {
    SYSTEM, LAUNCHER, SCREENSAVER, IPTV
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    var currentSection by remember { mutableStateOf(SettingsSection.SYSTEM) }

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
    val iptvUrl by viewModel.iptvUrl.collectAsState()
    val iptvPlaylists by viewModel.iptvPlaylists.collectAsState()
    val activeIptvPlaylist by viewModel.activeIptvPlaylist.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val dnsBlockerMode by viewModel.dnsBlockerMode.collectAsState()
    val glassOpacity by viewModel.glassOpacity.collectAsState()
    val showOnlyTvApps by viewModel.showOnlyTvApps.collectAsState()
    val appLanguage by viewModel.language.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val screensaverTimeout by viewModel.screensaverTimeout.collectAsState()
    val screensaverMode by viewModel.screensaverMode.collectAsState()
    val screensaverWallpaper by viewModel.screensaverWallpaper.collectAsState()
    val screensaverVideo by viewModel.screensaverVideo.collectAsState()
    var showPreviewScreensaver by remember { mutableStateOf(false) }

    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistUrl by remember { mutableStateOf("") }
    var customWallpaperInput by remember(customWallpaperUrl) { mutableStateOf(customWallpaperUrl) }
    var customVideoInput by remember(screensaverVideo) { mutableStateOf(screensaverVideo) }

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

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        isDefaultLauncher = viewModel.isDefaultLauncher(context)
    }

    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Try enabling again after permission request
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
        // Fallback to settings
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvMidnightBackground)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Category Navigation Drawer
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxSize()
                    .background(TvMidnightBackground.copy(alpha = 0.95f))
                    .padding(vertical = 24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvFocusableCard(
                        onClick = onBackToHome,
                        modifier = Modifier.size(40.dp)
                    ) { isFocused ->
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isFocused) TvAccentGold else TvTextPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = stringResource(R.string.settings_title),
                        color = TvTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Navigation Items
                val navItems = listOf(
                    SettingsSection.SYSTEM to (stringResource(R.string.settings_category_system) to Icons.Default.Settings),
                    SettingsSection.LAUNCHER to (stringResource(R.string.settings_category_launcher) to Icons.Default.ColorLens),
                    SettingsSection.SCREENSAVER to (stringResource(R.string.settings_category_screensaver) to Icons.Default.Bedtime),
                    SettingsSection.IPTV to (stringResource(R.string.settings_category_iptv) to Icons.Default.LiveTv)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    navItems.forEach { (section, pair) ->
                        val (title, icon) = pair
                        val isSelected = (section == currentSection)

                        TvFocusableCard(
                            onClick = { currentSection = section },
                            onFocused = { currentSection = section },
                            scaleUpRatio = 1.03f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) { isFocused ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isSelected) TvAccentGold.copy(alpha = 0.35f) else Color.Transparent
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected || isFocused) TvAccentGold else TvTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = title,
                                    color = if (isSelected || isFocused) TvTextPrimary else TvTextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "© Osama Salsa",
                    color = TvAccentGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            // Right Panel: Section Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(horizontal = 36.dp, vertical = 24.dp)
            ) {
                // Section Title & Status Toast
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentSection) {
                            SettingsSection.SYSTEM -> stringResource(R.string.settings_category_system)
                            SettingsSection.LAUNCHER -> stringResource(R.string.settings_category_launcher)
                            SettingsSection.SCREENSAVER -> stringResource(R.string.settings_category_screensaver)
                            SettingsSection.IPTV -> stringResource(R.string.settings_category_iptv)
                        },
                        color = TvTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    statusMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(TvAccentGold.copy(alpha = 0.2f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = msg.asString(context),
                                color = TvAccentGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Detail View
                when (currentSection) {
                    SettingsSection.SYSTEM -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // Language Selection Card
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_language),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Select application interface language / اختر لغة التطبيق",
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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

                            item {
                                    SettingActionTile(
                                    icon = Icons.Default.Wifi,
                                    title = stringResource(R.string.settings_open_wifi),
                                    desc = stringResource(R.string.settings_category_network),
                                    onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_WIFI_SETTINGS) }
                                )
                            }
                            item {
                                    SettingActionTile(
                                    icon = Icons.Default.Settings,
                                    title = stringResource(R.string.settings_open_ethernet),
                                    desc = stringResource(R.string.settings_category_network),
                                    onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_WIRELESS_SETTINGS) }
                                )
                            }
                            item {
                                    SettingActionTile(
                                    icon = Icons.Default.DisplaySettings,
                                    title = stringResource(R.string.settings_open_display),
                                    desc = stringResource(R.string.settings_open_display),
                                    onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_DISPLAY_SETTINGS) }
                                )
                            }
                            item {
                                    SettingActionTile(
                                    icon = Icons.Default.VolumeUp,
                                    title = stringResource(R.string.settings_open_sound),
                                    desc = stringResource(R.string.settings_open_sound),
                                    onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_SOUND_SETTINGS) }
                                )
                            }
                            item {
                                    SettingActionTile(
                                    icon = Icons.Default.Schedule,
                                    title = stringResource(R.string.settings_open_datetime),
                                    desc = stringResource(R.string.settings_open_datetime),
                                    onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_DATE_SETTINGS) }
                                )
                            }
                            item {
                                    SettingActionTile(
                                    icon = Icons.Default.Settings,
                                    title = stringResource(R.string.settings_open_all_settings),
                                    desc = stringResource(R.string.settings_open_all_settings),
                                    onClick = { viewModel.launchSystemSetting(context, Settings.ACTION_SETTINGS) }
                                )
                            }
                            item {
                                SettingActionTile(
                                    icon = Icons.Default.Info,
                                    title = stringResource(R.string.settings_open_app_info),
                                    desc = stringResource(R.string.settings_open_app_info_desc),
                                    onClick = { viewModel.openAppInfo(context) }
                                )
                            }
                            item {
                                DefaultLauncherManagementCard(
                                    isDefault = isDefaultLauncher,
                                    onEnable = { requestDefaultLauncher() },
                                    onReset = { viewModel.resetAndPromptDefaultLauncher(context) }
                                )
                            }
                            item {
                                FilePermissionManagementCard(
                                    hasPermission = hasFilePermission,
                                    onGrant = { FilePermissionHelper.requestStoragePermission(context) }
                                )
                            }
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_privacy_title),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_privacy_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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
                                                val vpnIntent = android.net.VpnService.prepare(context)
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
                        }
                    }

                    SettingsSection.LAUNCHER -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // Set as Default TV Launcher
                            item {
                                DefaultLauncherManagementCard(
                                    isDefault = isDefaultLauncher,
                                    onEnable = { requestDefaultLauncher() },
                                    onReset = { viewModel.resetAndPromptDefaultLauncher(context) }
                                )
                            }
                            item {
                                FilePermissionManagementCard(
                                    hasPermission = hasFilePermission,
                                    onGrant = { FilePermissionHelper.requestStoragePermission(context) }
                                )
                            }

                            // Wallpaper Style & Background Selector Card
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_wallpaper_style),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_wallpaper_style_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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
                                        Spacer(modifier = Modifier.height(16.dp))
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

                                            TvFocusableCard(
                                                onClick = { imagePickerLauncher.launch("image/*") },
                                                scaleUpRatio = 1.04f,
                                                shape = RoundedCornerShape(10.dp)
                                            ) { isFocused ->
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (isFocused) TvAccentGold else Color(0xFF334155), RoundedCornerShape(10.dp))
                                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Image,
                                                            contentDescription = null,
                                                            tint = if (isFocused) Color.Black else Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = stringResource(R.string.settings_wallpaper_pick_device),
                                                            color = if (isFocused) Color.Black else Color.White,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else if (wallpaperMode == "preset") {
                                        Spacer(modifier = Modifier.height(18.dp))
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_clock_settings),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_clock_settings_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TvSelectField(
                                        label = stringResource(R.string.settings_clock_seconds),
                                        options = listOf(
                                            false to stringResource(R.string.settings_seconds_disabled),
                                            true to stringResource(R.string.settings_seconds_enabled)
                                        ),
                                        selectedOption = isClockSecondsEnabled,
                                        onOptionSelected = { viewModel.setClockSecondsEnabled(it) }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_date_settings),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_date_settings_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_greeting_settings),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_greeting_settings_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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
                                            color = TvAccentGold,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.settings_custom_greeting_desc),
                                            color = TvTextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = customGreetingInput,
                                                onValueChange = { customGreetingInput = it },
                                                placeholder = {
                                                    Text(
                                                        stringResource(R.string.settings_custom_greeting_hint),
                                                        color = TvTextSecondary,
                                                        fontSize = 12.sp
                                                    )
                                                },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = TvAccentGold,
                                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                                    focusedTextColor = TvTextPrimary,
                                                    unfocusedTextColor = TvTextPrimary
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            )

                                            Button(
                                                onClick = {
                                                    viewModel.setCustomGreetingText(customGreetingInput.trim())
                                                    viewModel.setGreetingMode("custom")
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = TvAccentGold,
                                                    contentColor = Color.Black
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.height(52.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.settings_custom_greeting_save),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Apps per Row Selector
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_apps_per_row),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_apps_per_row_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_glass_opacity),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_glass_opacity_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_apps_top_position),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_apps_top_position_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_show_only_tv_apps),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_show_only_tv_apps_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.settings_hidden_apps),
                                            color = TvTextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        val hiddenApps = allApps.filter { it.isHidden }
                                        if (hiddenApps.isNotEmpty()) {
                                            TvActionButton(
                                                text = stringResource(R.string.action_unhide),
                                                onClick = { viewModel.unhideAllApps() },
                                                baseColor = TvAccentGold.copy(alpha = 0.85f),
                                                focusedColor = TvAccentGold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val hiddenList = allApps.filter { it.isHidden }
                                    if (hiddenList.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.settings_no_hidden_apps),
                                            color = TvTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    } else {
                                        hiddenList.forEach { app ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(app.label, color = TvTextPrimary, fontSize = 14.sp)
                                                TvFocusableCard(
                                                    onClick = { viewModel.unhideApp(app.packageName) },
                                                    modifier = Modifier
                                                        .height(34.dp)
                                                        .padding(horizontal = 4.dp)
                                                ) { isFocused ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Visibility, contentDescription = null, tint = TvAccentGold, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(stringResource(R.string.action_unhide), color = TvTextPrimary, fontSize = 12.sp)
                                                    }
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_screensaver_timeout),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_screensaver_timeout_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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

                            // Screensaver Mode Selector Card (3 Options)
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_screensaver_mode),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.settings_screensaver_mode_desc),
                                        color = TvTextSecondary,
                                        fontSize = 13.sp
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

                                    if (screensaverMode == "video_stream") {
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Text(
                                            text = stringResource(R.string.screensaver_video_choice),
                                            color = TvAccentGold,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.screensaver_video_choice_desc),
                                            color = TvTextSecondary,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Curated Preset Relaxing Videos
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            screensaverPresetVideos.forEach { presetVideo ->
                                                val isSelected = extractYouTubeVideoId(screensaverVideo) == presetVideo.id
                                                TvFocusableCard(
                                                    onClick = {
                                                        viewModel.setScreensaverVideo(presetVideo.id)
                                                        customVideoInput = presetVideo.id
                                                    },
                                                    scaleUpRatio = 1.02f,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(72.dp)
                                                ) { isFocused ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                if (isSelected) TvAccentGold.copy(alpha = 0.2f)
                                                                else if (isFocused) Color(0xFF334155)
                                                                else Color(0xFF0F172A)
                                                            )
                                                            .border(
                                                                width = if (isSelected) 1.5.dp else if (isFocused) 1.dp else 0.5.dp,
                                                                color = if (isSelected) TvAccentGold else if (isFocused) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                                                                shape = RoundedCornerShape(12.dp)
                                                            )
                                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Video Thumbnail
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
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Custom YouTube URL/ID Input
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF0F172A))
                                                .padding(14.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.screensaver_video_custom_title),
                                                color = TvTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = stringResource(R.string.screensaver_video_custom_desc),
                                                color = TvTextSecondary,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = customVideoInput,
                                                    onValueChange = { customVideoInput = it },
                                                    placeholder = {
                                                        Text(
                                                            stringResource(R.string.screensaver_video_custom_hint),
                                                            color = TvTextSecondary,
                                                            fontSize = 12.sp
                                                        )
                                                    },
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = TvAccentGold,
                                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                                        focusedTextColor = TvTextPrimary,
                                                        unfocusedTextColor = TvTextPrimary
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                Button(
                                                    onClick = {
                                                        if (customVideoInput.isNotBlank()) {
                                                            viewModel.setScreensaverVideo(customVideoInput.trim())
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = TvAccentGold,
                                                        contentColor = Color.Black
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.height(52.dp)
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.screensaver_video_custom_save),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_iptv_add_new),
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newPlaylistName,
                                            onValueChange = { newPlaylistName = it },
                                            placeholder = { Text(stringResource(R.string.settings_iptv_name_placeholder), color = TvTextSecondary) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = TvAccentGold,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                                focusedTextColor = TvTextPrimary,
                                                unfocusedTextColor = TvTextPrimary
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = newPlaylistUrl,
                                            onValueChange = { newPlaylistUrl = it },
                                            placeholder = { Text(stringResource(R.string.settings_iptv_url_placeholder), color = TvTextSecondary) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = TvAccentGold,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                                focusedTextColor = TvTextPrimary,
                                                unfocusedTextColor = TvTextPrimary
                                            ),
                                            shape = RoundedCornerShape(12.dp),
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
                                            baseColor = TvAccentGold.copy(alpha = 0.85f),
                                            focusedColor = TvAccentGold
                                        )

                                        TvActionButton(
                                            text = stringResource(R.string.settings_iptv_load_presets),
                                            onClick = { viewModel.loadPresetPlaylists() },
                                            baseColor = TvAccentGold,
                                            focusedColor = TvAccentGold
                                        )
                                    }
                                }
                            }

                            // Saved IPTV Playlists Header
                            item {
                                Text(
                                    text = stringResource(R.string.settings_iptv_saved_header).format(iptvPlaylists.size),
                                    color = TvTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (iptvPlaylists.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1E293B).copy(alpha = 0.4f))
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.settings_iptv_no_saved),
                                            color = TvTextSecondary,
                                            fontSize = 14.sp
                                        )
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
                                                TvFocusableCard(
                                                    onClick = {
                                                        if (!isActive) viewModel.switchIptvPlaylist(playlist.id)
                                                    },
                                                    scaleUpRatio = 1.02f,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) { isFocused ->
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(
                                                                when {
                                                                    isFocused -> Color(0xFF1E293B)
                                                                    isActive -> TvAccentGold.copy(alpha = 0.25f)
                                                                    else -> Color(0xFF1E293B).copy(alpha = 0.5f)
                                                                }
                                                            )
                                                            .border(
                                                                width = if (isFocused) 2.dp else if (isActive) 1.dp else 0.dp,
                                                                color = if (isFocused) TvAccentGold else if (isActive) TvAccentGold else Color.Transparent,
                                                                shape = RoundedCornerShape(14.dp)
                                                            )
                                                            .padding(16.dp)
                                                    ) {
                                                        // Top Header with Icon, Name, and Active Tag
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(34.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isActive) TvAccentGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.LiveTv,
                                                                    contentDescription = null,
                                                                    tint = if (isActive) TvAccentGold else TvTextPrimary,
                                                                    modifier = Modifier.size(18.dp)
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
                                                                    color = if (isActive) TvAccentGold else TvTextSecondary,
                                                                    fontSize = 12.sp
                                                                )
                                                            }
                                                            if (isActive) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(6.dp))
                                                                        .background(TvAccentGold)
                                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text(
                                                                        text = stringResource(R.string.settings_iptv_active_tag),
                                                                        color = Color.Black,
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(10.dp))

                                                        Text(
                                                            text = playlist.url,
                                                            color = TvTextSecondary.copy(alpha = 0.7f),
                                                            fontSize = 11.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )

                                                        Spacer(modifier = Modifier.height(14.dp))

                                                        // Action Row
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            if (!isActive) {
                                                                TvActionButton(
                                                                    text = stringResource(R.string.settings_iptv_activate_button),
                                                                    onClick = { viewModel.switchIptvPlaylist(playlist.id) },
                                                                    baseColor = TvAccentGold,
                                                                    focusedColor = TvAccentGold
                                                                )
                                                            } else {
                                                                TvActionButton(
                                                                    text = stringResource(R.string.settings_iptv_sync_button),
                                                                    icon = Icons.Default.Refresh,
                                                                    onClick = { viewModel.refreshIptvPlaylist() },
                                                                    baseColor = TvAccentGold.copy(alpha = 0.8f),
                                                                    focusedColor = TvAccentGold
                                                                )
                                                            }

                                                            TvFocusableCard(
                                                                onClick = { viewModel.deleteIptvPlaylist(playlist.id) },
                                                                modifier = Modifier.size(34.dp)
                                                            ) { isDelFocused ->
                                                                Icon(
                                                                    imageVector = Icons.Default.Delete,
                                                                    contentDescription = "Delete Playlist",
                                                                    tint = if (isDelFocused) TvAccentRed else TvTextSecondary,
                                                                    modifier = Modifier.align(Alignment.Center)
                                                                )
                                                            }
                                                        }
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

@Composable
private fun SettingActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.02f,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) TvAccentGold else TvAccentGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(
                    text = title,
                    color = if (isFocused) TvTextPrimary else TvTextPrimary.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    color = TvTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SettingToggleTile(
    title: String,
    desc: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    TvFocusableCard(
        onClick = onToggle,
        scaleUpRatio = 1.02f,
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isFocused) TvTextPrimary else TvTextPrimary.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    color = TvTextSecondary,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = TvAccentGold,
                    uncheckedThumbColor = TvTextSecondary,
                    uncheckedTrackColor = Color(0xFF1E293B)
                )
            )
        }
    }
}

@Composable
fun DefaultLauncherManagementCard(
    isDefault: Boolean,
    onEnable: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = if (isDefault) TvAccentGold.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        // Top Header with Title and Live Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDefault) TvAccentGold.copy(alpha = 0.15f) else Color(0xFF334155).copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = if (isDefault) TvAccentGold else TvTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_default_launcher_card_title),
                        color = TvTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.settings_default_launcher_status_desc),
                        color = TvTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isDefault) Color(0xFF065F46).copy(alpha = 0.75f)
                        else Color(0xFF475569).copy(alpha = 0.5f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDefault) Color(0xFF34D399) else Color(0xFF94A3B8).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isDefault) Color(0xFF34D399) else Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDefault) stringResource(R.string.settings_default_launcher_active)
                        else stringResource(R.string.settings_default_launcher_not_active),
                        color = if (isDefault) Color(0xFFD1FAE5) else TvTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Dual Actions: Set as Default TV Launcher & Reset Default Launcher (Launcher Manager)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TvActionButton(
                    text = stringResource(R.string.settings_set_default_launcher),
                    onClick = onEnable,
                    icon = Icons.Default.CheckCircle,
                    baseColor = if (isDefault) Color(0xFF0F766E).copy(alpha = 0.6f) else TvAccentGold,
                    focusedColor = TvAccentGold,
                    modifier = Modifier.weight(1f)
                )

                TvActionButton(
                    text = stringResource(R.string.settings_reset_default_launcher),
                    onClick = onReset,
                    icon = Icons.Default.Refresh,
                    baseColor = Color(0xFF334155),
                    focusedColor = TvAccentGold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_set_default_launcher_desc),
                color = TvTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun FilePermissionManagementCard(
    hasPermission: Boolean,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = if (hasPermission) Color(0xFF10B981).copy(alpha = 0.35f) else TvAccentGold.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        // Top Header with Title and Live Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (hasPermission) Color(0xFF10B981).copy(alpha = 0.15f)
                            else TvAccentGold.copy(alpha = 0.18f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (hasPermission) Color(0xFF10B981) else TvAccentGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_file_access_title),
                        color = TvTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.settings_file_access_desc),
                        color = TvTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (hasPermission) Color(0xFF10B981).copy(alpha = 0.2f)
                        else TvAccentGold.copy(alpha = 0.2f)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (hasPermission) stringResource(R.string.settings_file_access_granted)
                           else stringResource(R.string.settings_file_access_not_granted),
                    color = if (hasPermission) Color(0xFF10B981) else TvAccentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!hasPermission) {
            Spacer(modifier = Modifier.height(16.dp))
            TvActionButton(
                text = stringResource(R.string.settings_file_access_btn),
                onClick = onGrant,
                icon = Icons.Default.Folder,
                modifier = Modifier.fillMaxWidth(),
                focusedColor = TvAccentGold
            )
        }
    }
}

@Composable
fun <T> TvSelectField(
    label: String,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val currentLabel = options.find { it.first == selectedOption }?.second ?: ""

    TvFocusableCard(
        onClick = { showDialog = true },
        scaleUpRatio = 1.03f,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isFocused) Color.White else Color(0xFF1E293B).copy(alpha = 0.4f))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = if (isFocused) Color.Black else TvTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentLabel,
                    color = if (isFocused) Color(0xFF0369A1) else TvAccentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isFocused) Color.Black else TvTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TvMidnightBackground,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier
                    .width(320.dp)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = label,
                        color = TvTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(options) { (value, label) ->
                            val isSelected = (value == selectedOption)
                            TvFocusableCard(
                                onClick = {
                                    onOptionSelected(value)
                                    showDialog = false
                                },
                                scaleUpRatio = 1.05f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) { itemFocused ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            when {
                                                itemFocused -> TvAccentGold
                                                isSelected -> TvAccentGold.copy(alpha = 0.15f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        color = if (itemFocused) Color.Black else if (isSelected) TvAccentGold else TvTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (itemFocused) Color.Black else TvAccentGold,
                                            modifier = Modifier.size(18.dp)
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


@Composable
fun TvModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.06f,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(48.dp)
    ) { isFocused ->
        val bgColor = when {
            isFocused && isSelected -> TvAccentGold
            isFocused -> TvAccentGold
            isSelected -> TvAccentGold.copy(alpha = 0.85f)
            else -> Color(0xFF334155)
        }
        val textColor = when {
            isFocused && isSelected -> Color.Black
            isFocused -> Color.White
            isSelected -> Color.Black
            else -> TvTextPrimary
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TvActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    baseColor: Color = Color(0xFF334155),
    focusedColor: Color = TvAccentGold
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.05f,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(44.dp)
    ) { isFocused ->
        val bgColor = if (isFocused) focusedColor else baseColor
        val contentColor = if (isFocused && focusedColor == TvAccentGold) Color.Black else Color.White
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}



