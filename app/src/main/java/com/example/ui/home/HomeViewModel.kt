package com.example.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.IptvRepository
import com.example.data.local.LauncherPreferences
import com.example.data.model.AppItem
import com.example.data.model.IptvChannel
import androidx.compose.ui.graphics.toArgb
import com.example.ui.common.AuroraColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.util.LocaleHelper

enum class EditingMode {
    NONE, MOVE, DELETE, HIDE, OPTIONS
}

data class ReorderState(
    val selectedPackage: String,
    val items: List<AppItem>
)

data class ChannelReorderState(
    val selectedChannelId: Long,
    val items: List<IptvChannel>
)

class HomeViewModel(
    private val appRepository: AppRepository,
    private val iptvRepository: IptvRepository,
    private val preferences: LauncherPreferences
) : ViewModel() {

    // ... (rest of the fields)

    private val _editingMode = MutableStateFlow(EditingMode.NONE)
    val editingMode: StateFlow<EditingMode> = _editingMode.asStateFlow()

    val installedApps: StateFlow<List<AppItem>> = appRepository.installedAppsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, appRepository.getInitialFastApps())

    val favoriteChannels: StateFlow<List<IptvChannel>> = iptvRepository.favoriteChannelsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allChannels: StateFlow<List<IptvChannel>> = iptvRepository.getChannelsByCategory("All")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val appLockPin: StateFlow<String> = preferences.appLockPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1234")

    val isDynamicBgEnabled: StateFlow<Boolean> = preferences.isDynamicBackgroundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val wallpaperMode: StateFlow<String> = preferences.wallpaperModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "custom")

    val customWallpaperUrl: StateFlow<String> = preferences.customWallpaperUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://images.unsplash.com/photo-1543946207-39bd91e70ca7?w=1920&q=80")

    val isClock24h: StateFlow<Boolean> = preferences.isClock24h
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isClockSecondsEnabled: StateFlow<Boolean> = preferences.isClockSecondsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val clockSize: StateFlow<String> = preferences.clockSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "large")

    val dateFormatStyle: StateFlow<String> = preferences.dateFormatStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "full")

    val greetingMode: StateFlow<String> = preferences.greetingModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "poetic")

    val customGreetingText: StateFlow<String> = preferences.customGreetingTextFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val appsPerRow: StateFlow<Int> = preferences.appsPerRowFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val appsTopOffset: StateFlow<Int> = preferences.appsTopOffsetFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 340)

    val glassOpacity: StateFlow<Int> = preferences.glassOpacityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)

    val screensaverTimeout: StateFlow<String> = preferences.screensaverTimeoutFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "5")

    val screensaverMode: StateFlow<String> = preferences.screensaverModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "clock_date")

    val screensaverWallpaper: StateFlow<String> = preferences.screensaverWallpaperFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "current")

    val screensaverVideo: StateFlow<String> = preferences.screensaverVideoFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EG_g7BFaL5s")

    val isRtl: StateFlow<Boolean> = preferences.languageFlow
        .map { lang ->
            val effective = LocaleHelper.getEffectiveLanguage(lang)
            lang == "ar_rtl" || (lang == "system" && effective == "ar")
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _focusedColor = MutableStateFlow<Int?>(null)
    val focusedColor: StateFlow<Int?> = _focusedColor.asStateFlow()

    val defaultAuroraColors = AuroraColors()
    private val _auroraColors = MutableStateFlow(defaultAuroraColors)
    val auroraColors: StateFlow<AuroraColors> = _auroraColors.asStateFlow()

    private val _selectedAppForMenu = MutableStateFlow<AppItem?>(null)
    val selectedAppForMenu: StateFlow<AppItem?> = _selectedAppForMenu.asStateFlow()

    private val _reorderState = MutableStateFlow<ReorderState?>(null)
    val reorderState: StateFlow<ReorderState?> = _reorderState.asStateFlow()

    private val _channelReorderState = MutableStateFlow<ChannelReorderState?>(null)
    val channelReorderState: StateFlow<ChannelReorderState?> = _channelReorderState.asStateFlow()

    private val _selectedChannelForMenu = MutableStateFlow<IptvChannel?>(null)
    val selectedChannelForMenu: StateFlow<IptvChannel?> = _selectedChannelForMenu.asStateFlow()

    fun onFocusColorChanged(color: Int?) {
        if (!isDynamicBgEnabled.value || wallpaperMode.value != "dynamic") return
        if (_focusedColor.value != color) {
            _focusedColor.value = color
        }
    }

    fun onAppFocused(app: AppItem) {
        if (!isDynamicBgEnabled.value || wallpaperMode.value != "dynamic") return
        val colors = app.auroraColors ?: defaultAuroraColors
        if (_auroraColors.value != colors) {
            _auroraColors.value = colors
        }
        val glowArgb = colors.glow.toArgb()
        if (_focusedColor.value != glowArgb) {
            _focusedColor.value = glowArgb
        }
    }

    fun onNonAppFocused() {
        if (!isDynamicBgEnabled.value || wallpaperMode.value != "dynamic") return
        if (_auroraColors.value != defaultAuroraColors) {
            _auroraColors.value = defaultAuroraColors
        }
        if (_focusedColor.value != null) {
            _focusedColor.value = null
        }
    }

    fun setEditingMode(mode: EditingMode) {
        _editingMode.value = mode
    }

    fun launchApp(app: AppItem) {
        when (_editingMode.value) {
            EditingMode.MOVE -> startReorder(app)
            EditingMode.HIDE -> hideApp(app)
            EditingMode.OPTIONS -> showAppMenu(app)
            EditingMode.DELETE, EditingMode.NONE -> {
                if (_reorderState.value != null) {
                    confirmReorder()
                    return
                }
                if (_editingMode.value == EditingMode.DELETE) {
                    // Logic for uninstall will be handled by the UI or calling uninstallApp
                    // But usually we just want to launch it if NONE
                }
                if (_editingMode.value == EditingMode.NONE) {
                    appRepository.launchApp(app)
                }
            }
        }
    }

    fun showAppMenu(app: AppItem) {
        if (_reorderState.value == null) {
            _selectedAppForMenu.value = app
        }
    }

    fun dismissAppMenu() {
        _selectedAppForMenu.value = null
    }

    fun startReorder(app: AppItem) {
        if (_reorderState.value != null) {
            confirmReorder()
            return
        }
        val current = installedApps.value
        val exists = current.any { it.packageName == app.packageName }
        if (exists) {
            _reorderState.value = ReorderState(selectedPackage = app.packageName, items = current)
            _editingMode.value = EditingMode.MOVE
        }
        dismissAppMenu()
    }

    fun startChannelReorder(channel: IptvChannel) {
        if (_channelReorderState.value != null) {
            confirmChannelReorder()
            return
        }
        val current = favoriteChannels.value
        if (current.any { it.id == channel.id }) {
            _channelReorderState.value = ChannelReorderState(selectedChannelId = channel.id, items = current)
            _editingMode.value = EditingMode.MOVE
        }
        dismissChannelMenu()
    }

    fun showChannelMenu(channel: IptvChannel) {
        if (_channelReorderState.value == null) {
            _selectedChannelForMenu.value = channel
        }
    }

    fun dismissChannelMenu() {
        _selectedChannelForMenu.value = null
    }

    fun toggleFavorite(channel: IptvChannel) {
        viewModelScope.launch {
            iptvRepository.toggleFavorite(channel)
            dismissChannelMenu()
        }
    }

    fun moveAppLeft() {
        val state = _reorderState.value ?: return
        val targetPkg = state.selectedPackage
        val currentItem = state.items.find { it.packageName == targetPkg } ?: return

        if (!currentItem.isGame) {
            val nonGames = state.items.filter { !it.isGame }.toMutableList()
            val idx = nonGames.indexOfFirst { it.packageName == targetPkg }
            if (idx > 0) {
                val temp = nonGames[idx]
                nonGames[idx] = nonGames[idx - 1]
                nonGames[idx - 1] = temp
                val newItems = nonGames + state.items.filter { it.isGame }
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = newItems)
            }
        } else {
            val games = state.items.filter { it.isGame }.toMutableList()
            val idx = games.indexOfFirst { it.packageName == targetPkg }
            if (idx > 0) {
                val temp = games[idx]
                games[idx] = games[idx - 1]
                games[idx - 1] = temp
                val newItems = state.items.filter { !it.isGame } + games
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = newItems)
            }
        }
    }

    fun moveAppRight() {
        val state = _reorderState.value ?: return
        val targetPkg = state.selectedPackage
        val currentItem = state.items.find { it.packageName == targetPkg } ?: return

        if (!currentItem.isGame) {
            val nonGames = state.items.filter { !it.isGame }.toMutableList()
            val idx = nonGames.indexOfFirst { it.packageName == targetPkg }
            if (idx != -1 && idx < nonGames.lastIndex) {
                val temp = nonGames[idx]
                nonGames[idx] = nonGames[idx + 1]
                nonGames[idx + 1] = temp
                val newItems = nonGames + state.items.filter { it.isGame }
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = newItems)
            }
        } else {
            val games = state.items.filter { it.isGame }.toMutableList()
            val idx = games.indexOfFirst { it.packageName == targetPkg }
            if (idx != -1 && idx < games.lastIndex) {
                val temp = games[idx]
                games[idx] = games[idx + 1]
                games[idx + 1] = temp
                val newItems = state.items.filter { !it.isGame } + games
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = newItems)
            }
        }
    }

    fun moveAppUp() {
        val state = _reorderState.value ?: return
        val targetPkg = state.selectedPackage
        val currentItem = state.items.find { it.packageName == targetPkg } ?: return
        val step = appsPerRow.value.coerceIn(4, 7)

        if (!currentItem.isGame) {
            val nonGames = state.items.filter { !it.isGame }.toMutableList()
            val idx = nonGames.indexOfFirst { it.packageName == targetPkg }
            if (idx >= step) {
                val item = nonGames.removeAt(idx)
                nonGames.add(idx - step, item)
                val newItems = nonGames + state.items.filter { it.isGame }
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = newItems)
            }
        } else {
            val games = state.items.filter { it.isGame }.toMutableList()
            val idx = games.indexOfFirst { it.packageName == targetPkg }
            if (idx >= step) {
                val item = games.removeAt(idx)
                games.add(idx - step, item)
                val newItems = state.items.filter { !it.isGame } + games
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = newItems)
            } else {
                // Moving up from top row of games into apps section!
                val converted = currentItem.copy(isGame = false)
                val updatedGames = games.filter { it.packageName != targetPkg }
                val nonGames = state.items.filter { !it.isGame }.toMutableList()
                nonGames.add(converted)
                val newItems = nonGames + updatedGames
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = newItems)
            }
        }
    }

    fun moveAppDown() {
        val state = _reorderState.value ?: return
        val targetPkg = state.selectedPackage
        val currentItem = state.items.find { it.packageName == targetPkg } ?: return
        val step = appsPerRow.value.coerceIn(4, 7)

        if (!currentItem.isGame) {
            val nonGames = state.items.filter { !it.isGame }.toMutableList()
            val idx = nonGames.indexOfFirst { it.packageName == targetPkg }
            if (idx != -1 && idx + step < nonGames.size) {
                val item = nonGames.removeAt(idx)
                nonGames.add(idx + step, item)
                val newItems = nonGames + state.items.filter { it.isGame }
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = newItems)
            } else {
                // Moving down from bottom row of apps into games section!
                val converted = currentItem.copy(isGame = true)
                val updatedNonGames = nonGames.filter { it.packageName != targetPkg }
                val games = state.items.filter { it.isGame }.toMutableList()
                games.add(0, converted)
                val newItems = updatedNonGames + games
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = newItems)
            }
        } else {
            val games = state.items.filter { it.isGame }.toMutableList()
            val idx = games.indexOfFirst { it.packageName == targetPkg }
            if (idx != -1 && idx + step < games.size) {
                val item = games.removeAt(idx)
                games.add(idx + step, item)
                val newItems = state.items.filter { !it.isGame } + games
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = newItems)
            }
        }
    }

    fun moveChannelLeft() {
        val state = _channelReorderState.value ?: return
        val currentItems = state.items.toMutableList()
        val idx = currentItems.indexOfFirst { it.id == state.selectedChannelId }
        if (idx > 0) {
            val temp = currentItems[idx]
            currentItems[idx] = currentItems[idx - 1]
            currentItems[idx - 1] = temp
            _channelReorderState.value = state.copy(items = currentItems)
        }
    }

    fun moveChannelRight() {
        val state = _channelReorderState.value ?: return
        val currentItems = state.items.toMutableList()
        val idx = currentItems.indexOfFirst { it.id == state.selectedChannelId }
        if (idx != -1 && idx < currentItems.lastIndex) {
            val temp = currentItems[idx]
            currentItems[idx] = currentItems[idx + 1]
            currentItems[idx + 1] = temp
            _channelReorderState.value = state.copy(items = currentItems)
        }
    }

    fun confirmChannelReorder() {
        val state = _channelReorderState.value ?: return
        viewModelScope.launch {
            iptvRepository.saveFavoriteOrder(state.items)
            _channelReorderState.value = null
            _editingMode.value = EditingMode.NONE
            dismissChannelMenu()
        }
    }

    fun confirmReorder() {
        val state = _reorderState.value ?: return
        viewModelScope.launch {
            // Save complete ordered list of package names
            appRepository.saveNewOrder(state.items)
            
            // Persist the game / non-game category for each app in state.items
            state.items.forEach { item ->
                preferences.setAppIsGame(item.packageName, item.isGame)
            }
            
            appRepository.triggerReload()
            kotlinx.coroutines.delay(100)
            _reorderState.value = null
            _editingMode.value = EditingMode.NONE
            dismissAppMenu()
        }
    }

    fun toggleAppGameCategory(app: AppItem) {
        viewModelScope.launch {
            val newIsGame = !app.isGame
            preferences.setAppIsGame(app.packageName, newIsGame)
            dismissAppMenu()
            appRepository.triggerReload()
        }
    }

    fun toggleAppLock(app: AppItem) {
        viewModelScope.launch {
            preferences.setAppLocked(app.packageName, !app.isLocked)
            dismissAppMenu()
            appRepository.triggerReload()
        }
    }

    fun toggleClock24h() {
        viewModelScope.launch {
            preferences.setClock24h(!isClock24h.value)
        }
    }

    fun toggleDynamicBg() {
        viewModelScope.launch {
            val next = !isDynamicBgEnabled.value
            preferences.setDynamicBackgroundEnabled(next)
            preferences.setWallpaperMode(if (next) "dynamic" else "default")
        }
    }

    fun openPlayStoreGames(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/category/GAME")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=games")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun hideApp(app: AppItem) {
        viewModelScope.launch {
            appRepository.hideApp(app.packageName)
            dismissAppMenu()
        }
    }

    fun openAppInfo(context: Context, app: AppItem) {
        dismissAppMenu()
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${app.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun uninstallApp(context: Context, app: AppItem) {
        dismissAppMenu()
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:${app.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
    private val _showDefaultLauncherPrompt = MutableStateFlow(false)
    val showDefaultLauncherPrompt: StateFlow<Boolean> = _showDefaultLauncherPrompt.asStateFlow()

    init {
        checkDefaultLauncherPrompt()
    }

    private fun checkDefaultLauncherPrompt() {
        viewModelScope.launch {
            preferences.hasPromptedLauncherFlow.collect { hasPrompted ->
                if (!hasPrompted) {
                    _showDefaultLauncherPrompt.value = true
                }
            }
        }
    }

    fun onDefaultLauncherPromptDismissed() {
        _showDefaultLauncherPrompt.value = false
        viewModelScope.launch {
            preferences.setHasPromptedLauncher(true)
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
}
