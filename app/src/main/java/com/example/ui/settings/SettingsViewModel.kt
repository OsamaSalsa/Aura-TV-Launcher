package com.example.ui.settings

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.util.Log
import android.provider.Settings
import com.example.FakeHomeActivity
import com.example.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.IptvRepository
import com.example.data.local.LauncherPreferences
import com.example.data.model.AppItem
import com.example.data.model.IptvPlaylist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class StatusUiText {
    data class Resource(val resId: Int, val formatArgs: List<Any> = emptyList()) : StatusUiText()
    data class DynamicString(val value: String) : StatusUiText()
    
    fun asString(context: Context): String {
        return when (this) {
            is Resource -> context.getString(resId, *formatArgs.toTypedArray())
            is DynamicString -> value
        }
    }
}

class SettingsViewModel(
    private val appRepository: AppRepository,
    private val iptvRepository: IptvRepository,
    private val preferences: LauncherPreferences
) : ViewModel() {

    val isDynamicBgEnabled: StateFlow<Boolean> = preferences.isDynamicBackgroundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isClock24h: StateFlow<Boolean> = preferences.isClock24h
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isClockSecondsEnabled: StateFlow<Boolean> = preferences.isClockSecondsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setClockSecondsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setClockSecondsEnabled(enabled)
        }
    }

    val clockSize: StateFlow<String> = preferences.clockSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "large")

    fun setClockSize(size: String) {
        viewModelScope.launch {
            preferences.setClockSize(size)
        }
    }

    val dateFormatStyle: StateFlow<String> = preferences.dateFormatStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "full")

    fun setDateFormatStyle(style: String) {
        viewModelScope.launch {
            preferences.setDateFormatStyle(style)
        }
    }

    val greetingMode: StateFlow<String> = preferences.greetingModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "poetic")

    fun setGreetingMode(mode: String) {
        viewModelScope.launch {
            preferences.setGreetingMode(mode)
        }
    }

    val customGreetingText: StateFlow<String> = preferences.customGreetingTextFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setCustomGreetingText(text: String) {
        viewModelScope.launch {
            preferences.setCustomGreetingText(text)
        }
    }

    val appsPerRow: StateFlow<Int> = preferences.appsPerRowFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val appsTopOffset: StateFlow<Int> = preferences.appsTopOffsetFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 340)

    fun setAppsTopOffset(offsetDp: Int) {
        viewModelScope.launch {
            preferences.setAppsTopOffset(offsetDp)
        }
    }

    val wallpaperMode: StateFlow<String> = preferences.wallpaperModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "custom")

    val customWallpaperUrl: StateFlow<String> = preferences.customWallpaperUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://images.unsplash.com/photo-1543946207-39bd91e70ca7?w=1920&q=80")

    val iptvUrl: StateFlow<String> = preferences.iptvPlaylistUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val language: StateFlow<String> = preferences.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val dnsBlockerMode: StateFlow<String> = preferences.dnsBlockerModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "off")

    val glassOpacity: StateFlow<Int> = preferences.glassOpacityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)

    fun setGlassOpacity(percent: Int) {
        viewModelScope.launch {
            preferences.setGlassOpacity(percent)
        }
    }

    val screensaverTimeout: StateFlow<String> = preferences.screensaverTimeoutFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "5")

    fun setScreensaverTimeout(timeout: String) {
        viewModelScope.launch {
            preferences.setScreensaverTimeout(timeout)
        }
    }

    val screensaverMode: StateFlow<String> = preferences.screensaverModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "clock_date")

    fun setScreensaverMode(mode: String) {
        viewModelScope.launch {
            preferences.setScreensaverMode(mode)
        }
    }

    val screensaverWallpaper: StateFlow<String> = preferences.screensaverWallpaperFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "current")

    fun setScreensaverWallpaper(wallpaper: String) {
        viewModelScope.launch {
            preferences.setScreensaverWallpaper(wallpaper)
        }
    }

    val screensaverVideo: StateFlow<String> = preferences.screensaverVideoFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EG_g7BFaL5s")

    val customScreensaverVideos: StateFlow<Set<String>> = preferences.customScreensaverVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val deletedScreensaverPresets: StateFlow<Set<String>> = preferences.deletedScreensaverPresetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun setScreensaverVideo(videoId: String) {
        viewModelScope.launch {
            preferences.setScreensaverVideo(videoId.trim())
        }
    }

    fun addCustomScreensaverVideo(videoInput: String, customTitle: String = "") {
        val extractedId = extractYouTubeId(videoInput)
        if (extractedId.isBlank()) return
        viewModelScope.launch {
            preferences.addCustomScreensaverVideo(extractedId, customTitle)
            _statusMessage.value = StatusUiText.Resource(R.string.status_screensaver_video_added)
        }
    }

    fun deleteScreensaverVideo(videoId: String) {
        viewModelScope.launch {
            preferences.deleteScreensaverVideo(videoId)
            _statusMessage.value = StatusUiText.Resource(R.string.status_screensaver_video_removed)
        }
    }

    fun restoreDefaultScreensaverVideos() {
        viewModelScope.launch {
            preferences.restoreDefaultScreensaverVideos()
            _statusMessage.value = StatusUiText.Resource(R.string.status_screensaver_videos_restored)
        }
    }

    private fun extractYouTubeId(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        if (trimmed.length == 11 && !trimmed.contains("/") && !trimmed.contains(".")) {
            return trimmed
        }
        val patterns = listOf(
            "(?<=watch\\?v=)[^&#?\\s]+",
            "(?<=youtu\\.be/)[^&#?\\s]+",
            "(?<=embed/)[^&#?\\s]+",
            "(?<=shorts/)[^&#?\\s]+",
            "(?<=live/)[^&#?\\s]+"
        )
        for (pattern in patterns) {
            val match = Regex(pattern).find(trimmed)
            if (match != null && match.value.isNotBlank()) {
                return match.value
            }
        }
        return trimmed
    }



    val showOnlyTvApps: StateFlow<Boolean> = preferences.showOnlyTvAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setShowOnlyTvApps(tvOnly: Boolean) {
        viewModelScope.launch {
            preferences.setShowOnlyTvApps(tvOnly)
        }
    }

    val allApps: StateFlow<List<AppItem>> = appRepository.allAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _statusMessage = MutableStateFlow<StatusUiText?>(null)
    val statusMessage: StateFlow<StatusUiText?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun setAppsPerRow(count: Int) {
        viewModelScope.launch {
            preferences.setAppsPerRow(count)
            _statusMessage.value = StatusUiText.Resource(R.string.status_apps_per_row_updated, listOf(count))
        }
    }

    fun setDynamicBgEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setDynamicBackgroundEnabled(enabled)
        }
    }

    fun setClock24h(is24h: Boolean) {
        viewModelScope.launch {
            preferences.setClock24h(is24h)
        }
    }

    fun setWallpaperMode(mode: String) {
        viewModelScope.launch {
            preferences.setWallpaperMode(mode)
            _statusMessage.value = StatusUiText.Resource(R.string.status_wallpaper_style_updated)
        }
    }

    fun setCustomWallpaperUrl(url: String) {
        viewModelScope.launch {
            preferences.setCustomWallpaperUrl(url)
            preferences.setWallpaperMode("custom")
            _statusMessage.value = StatusUiText.Resource(R.string.status_custom_wallpaper_applied)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferences.setLanguage(lang)
            _statusMessage.value = StatusUiText.Resource(R.string.status_language_saved)
        }
    }

    fun setDnsBlockerMode(mode: String, context: Context) {
        viewModelScope.launch {
            if (mode != "off") {
                val vpnIntent = VpnService.prepare(context)
                if (vpnIntent != null) {
                    _statusMessage.value = StatusUiText.Resource(R.string.status_vpn_permission_required)
                } else {
                    preferences.setDnsBlockerMode(mode)
                    startAdsBlockerService(context, mode)
                    _statusMessage.value = StatusUiText.Resource(
                        if (mode == "family") R.string.status_family_protection_enabled else R.string.status_ads_blocker_enabled
                    )
                }
            } else {
                preferences.setDnsBlockerMode("off")
                stopAdsBlockerService(context)
                _statusMessage.value = StatusUiText.Resource(R.string.status_protection_disabled)
            }
        }
    }

    private fun startAdsBlockerService(context: Context, mode: String) {
        try {
            val intent = Intent(context, com.example.service.DnsBlockerService::class.java).apply {
                putExtra(com.example.service.DnsBlockerService.EXTRA_MODE, mode)
            }
            context.startService(intent)
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Failed to start DNS Blocker service", e)
        }
    }

    private fun stopAdsBlockerService(context: Context) {
        try {
            val intent = Intent(context, com.example.service.DnsBlockerService::class.java)
            context.stopService(intent)
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Failed to stop DNS Blocker service", e)
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            appRepository.unhideApp(packageName)
            _statusMessage.value = StatusUiText.Resource(R.string.status_app_unhidden)
        }
    }

    fun unhideAllApps() {
        viewModelScope.launch {
            appRepository.unhideAll()
            _statusMessage.value = StatusUiText.Resource(R.string.status_all_apps_restored)
        }
    }

    fun resetAppOrder() {
        viewModelScope.launch {
            appRepository.resetOrder()
            _statusMessage.value = StatusUiText.Resource(R.string.status_app_order_reset)
        }
    }

    val iptvPlaylists: StateFlow<List<IptvPlaylist>> = iptvRepository.playlistsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeIptvPlaylist: StateFlow<IptvPlaylist?> = iptvRepository.activePlaylistFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addIptvPlaylist(name: String, url: String) {
        viewModelScope.launch {
            val result = iptvRepository.addPlaylist(name, url, activateImmediately = true)
            result.onSuccess { count ->
                _statusMessage.value = StatusUiText.Resource(R.string.status_playlist_added, listOf(count))
            }.onFailure { err ->
                _statusMessage.value = StatusUiText.DynamicString("Failed: ${err.message}")
            }
        }
    }

    fun switchIptvPlaylist(playlistId: Long) {
        viewModelScope.launch {
            val result = iptvRepository.switchActivePlaylist(playlistId)
            result.onSuccess { count ->
                _statusMessage.value = StatusUiText.Resource(R.string.status_playlist_switch_success, listOf(count))
            }.onFailure { err ->
                _statusMessage.value = StatusUiText.DynamicString("Error: ${err.message}")
            }
        }
    }

    fun refreshIptvPlaylist() {
        viewModelScope.launch {
            val result = iptvRepository.refreshActivePlaylist()
            result.onSuccess { count ->
                _statusMessage.value = StatusUiText.Resource(R.string.status_playlist_synced, listOf(count))
            }.onFailure { err ->
                _statusMessage.value = StatusUiText.DynamicString("Sync error: ${err.message}")
            }
        }
    }

    fun deleteIptvPlaylist(playlistId: Long) {
        viewModelScope.launch {
            iptvRepository.deletePlaylist(playlistId)
            _statusMessage.value = StatusUiText.Resource(R.string.status_playlist_removed)
        }
    }

    fun deleteAllIptvPlaylists() {
        viewModelScope.launch {
            iptvRepository.deleteAllPlaylists()
            _statusMessage.value = StatusUiText.Resource(R.string.status_all_playlists_removed)
        }
    }

    fun loadPresetPlaylists() {
        viewModelScope.launch {
            val result = iptvRepository.loadPresetPlaylists()
            result.onSuccess { count ->
                if (count > 0) {
                    _statusMessage.value = StatusUiText.Resource(R.string.status_presets_loaded, listOf(count))
                } else {
                    _statusMessage.value = StatusUiText.Resource(R.string.status_presets_already_loaded)
                }
            }.onFailure { err ->
                _statusMessage.value = StatusUiText.DynamicString("Error: ${err.message}")
            }
        }
    }

    fun loadDefaultIptvPlaylist() {
        loadPresetPlaylists()
    }

    fun launchSystemSetting(context: Context, action: String) {
        try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to general settings
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (_: Exception) {}
        }
    }

    fun isDefaultLauncher(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == context.packageName
        } catch (_: Exception) {
            false
        }
    }

    fun promptSetDefaultLauncher(context: Context) {
        // Strategy 1: Toggle FakeHomeActivity component to trigger Android OS default launcher reset
        try {
            val componentName = ComponentName(context, FakeHomeActivity::class.java)
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (_: Exception) {}

        // Strategy 2: Android 10+ (API 29+) RoleManager system prompt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                        val roleIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                        roleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(roleIntent)
                        _statusMessage.value = StatusUiText.Resource(R.string.status_select_default_launcher)
                        return
                    } else {
                        _statusMessage.value = StatusUiText.Resource(R.string.status_already_default_launcher)
                    }
                }
            } catch (_: Exception) {}
        }

        // Strategy 3: Open Home Settings
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            _statusMessage.value = StatusUiText.Resource(R.string.status_opening_home_settings)
            return
        } catch (_: Exception) {}

        // Strategy 4: Manage Default Apps
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            _statusMessage.value = StatusUiText.Resource(R.string.status_opening_default_apps)
            return
        } catch (_: Exception) {}

        // Strategy 5: Application Details Settings (where user can set as default app or clear defaults)
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            _statusMessage.value = StatusUiText.Resource(R.string.status_opening_app_details)
            return
        } catch (_: Exception) {}

        // Strategy 6: System Settings
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            _statusMessage.value = StatusUiText.Resource(R.string.status_opening_system_settings)
            return
        } catch (_: Exception) {}

        // Strategy 7: Home Intent Chooser
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(homeIntent, "Select Home Launcher").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            _statusMessage.value = StatusUiText.Resource(R.string.status_select_from_list)
        } catch (_: Exception) {
            _statusMessage.value = StatusUiText.Resource(R.string.status_manual_default_launcher_hint)
        }
    }

    fun resetAndPromptDefaultLauncher(context: Context) {
        try {
            val componentName = ComponentName(context, FakeHomeActivity::class.java)
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            _statusMessage.value = StatusUiText.Resource(R.string.status_cleared_defaults)
        } catch (_: Exception) {}

        promptSetDefaultLauncher(context)
    }

    fun openAppInfo(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            launchSystemSetting(context, Settings.ACTION_APPLICATION_SETTINGS)
        }
    }
}
