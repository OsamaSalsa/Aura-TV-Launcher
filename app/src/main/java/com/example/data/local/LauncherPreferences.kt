package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

class LauncherPreferences(private val context: Context) {

    companion object {
        const val DEFAULT_IPTV_URL = "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u8"
        val KEY_APP_ORDER = stringPreferencesKey("custom_app_order")
        val KEY_HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val KEY_DYNAMIC_BG = booleanPreferencesKey("dynamic_background_enabled")
        val KEY_CLOCK_24H = booleanPreferencesKey("clock_24h_format")
        val KEY_APPS_PER_ROW = intPreferencesKey("apps_per_row")
        val KEY_IPTV_URL = stringPreferencesKey("iptv_playlist_url")
        val KEY_WALLPAPER_MODE = stringPreferencesKey("wallpaper_mode")
        val KEY_CUSTOM_WALLPAPER_URL = stringPreferencesKey("custom_wallpaper_url")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_HAS_PROMPTED_LAUNCHER = booleanPreferencesKey("has_prompted_launcher")
        val KEY_YOUTUBE_MODE = stringPreferencesKey("youtube_view_mode")
        val KEY_CUSTOM_GAMES = stringSetPreferencesKey("custom_game_packages")
        val KEY_CUSTOM_NON_GAMES = stringSetPreferencesKey("custom_non_game_packages")
        val KEY_ADS_BLOCKER_ENABLED = booleanPreferencesKey("ads_blocker_enabled")
        val KEY_DNS_BLOCKER_MODE = stringPreferencesKey("dns_blocker_mode")
        val KEY_GLASS_OPACITY = intPreferencesKey("glass_opacity_percent")
        val KEY_SHOW_ONLY_TV_APPS = booleanPreferencesKey("show_only_tv_apps")
        val KEY_LOCKED_APPS = stringSetPreferencesKey("locked_apps")
        val KEY_APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
        val KEY_SCREENSAVER_TIMEOUT = stringPreferencesKey("screensaver_timeout")
        val KEY_SCREENSAVER_MODE = stringPreferencesKey("screensaver_mode")
        val KEY_SCREENSAVER_WALLPAPER = stringPreferencesKey("screensaver_wallpaper")
        val KEY_SCREENSAVER_VIDEO = stringPreferencesKey("screensaver_video_id")
        val KEY_APPS_TOP_OFFSET = intPreferencesKey("apps_top_offset_dp")
        val KEY_CLOCK_SHOW_SECONDS = booleanPreferencesKey("clock_show_seconds")
        val KEY_CLOCK_SIZE = stringPreferencesKey("clock_font_size_mode")
        val KEY_DATE_FORMAT_STYLE = stringPreferencesKey("date_format_style")
        val KEY_GREETING_MODE = stringPreferencesKey("greeting_display_mode")
        val KEY_CUSTOM_GREETING_TEXT = stringPreferencesKey("custom_greeting_text")
    }

    val showOnlyTvAppsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_ONLY_TV_APPS] ?: true
    }

    suspend fun setShowOnlyTvApps(tvOnly: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_ONLY_TV_APPS] = tvOnly
        }
    }

    val glassOpacityFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_GLASS_OPACITY] ?: 75
    }

    suspend fun setGlassOpacity(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GLASS_OPACITY] = percent.coerceIn(25, 100)
        }
    }

    val hasPromptedLauncherFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAS_PROMPTED_LAUNCHER] ?: false
    }

    suspend fun setHasPromptedLauncher(hasPrompted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAS_PROMPTED_LAUNCHER] = hasPrompted
        }
    }

    val appOrderFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_APP_ORDER] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",").map { it.trim() }
    }

    val hiddenAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIDDEN_APPS] ?: emptySet()
    }

    val isDynamicBackgroundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DYNAMIC_BG] ?: true
    }

    val wallpaperModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_WALLPAPER_MODE] ?: "dynamic"
    }

    val customWallpaperUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_WALLPAPER_URL] ?: "aurora_gradient"
    }

    val isClock24h: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CLOCK_24H] ?: true
    }

    val appsPerRowFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_APPS_PER_ROW] ?: 5
    }

    val appsTopOffsetFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_APPS_TOP_OFFSET] ?: 340
    }

    suspend fun setAppsTopOffset(offsetDp: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APPS_TOP_OFFSET] = offsetDp
        }
    }

    val isClockSecondsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CLOCK_SHOW_SECONDS] ?: false
    }

    suspend fun setClockSecondsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CLOCK_SHOW_SECONDS] = enabled
        }
    }

    val clockSizeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CLOCK_SIZE] ?: "large"
    }

    suspend fun setClockSize(size: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CLOCK_SIZE] = size
        }
    }

    val dateFormatStyleFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DATE_FORMAT_STYLE] ?: "full"
    }

    suspend fun setDateFormatStyle(style: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DATE_FORMAT_STYLE] = style
        }
    }

    val greetingModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_GREETING_MODE] ?: "poetic"
    }

    suspend fun setGreetingMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GREETING_MODE] = mode
        }
    }

    val customGreetingTextFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_GREETING_TEXT] ?: ""
    }

    suspend fun setCustomGreetingText(text: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_GREETING_TEXT] = text
        }
    }

    val iptvPlaylistUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_IPTV_URL] ?: DEFAULT_IPTV_URL
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "system"
    }

    suspend fun saveAppOrder(orderList: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_ORDER] = orderList.joinToString(",")
        }
    }

    suspend fun hideApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_HIDDEN_APPS] ?: emptySet()).toMutableSet()
            current.add(packageName)
            prefs[KEY_HIDDEN_APPS] = current
        }
    }

    suspend fun unhideApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_HIDDEN_APPS] ?: emptySet()).toMutableSet()
            current.remove(packageName)
            prefs[KEY_HIDDEN_APPS] = current
        }
    }

    suspend fun unhideAllApps() {
        context.dataStore.edit { prefs ->
            prefs[KEY_HIDDEN_APPS] = emptySet()
        }
    }

    suspend fun resetAppOrder() {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_ORDER] = ""
        }
    }

    suspend fun setDynamicBackgroundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DYNAMIC_BG] = enabled
        }
    }

    suspend fun setClock24h(is24h: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CLOCK_24H] = is24h
        }
    }

    suspend fun setAppsPerRow(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APPS_PER_ROW] = count
        }
    }

    suspend fun setIptvPlaylistUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IPTV_URL] = url
        }
    }

    suspend fun setWallpaperMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WALLPAPER_MODE] = mode
        }
    }

    suspend fun setCustomWallpaperUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_WALLPAPER_URL] = url
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = lang
        }
    }

    val youtubeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_YOUTUBE_MODE] ?: "curated"
    }

    suspend fun setYouTubeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_YOUTUBE_MODE] = mode
        }
    }

    val customGamesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_GAMES] ?: emptySet()
    }

    val customNonGamesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_NON_GAMES] ?: emptySet()
    }

    suspend fun setAppIsGame(packageName: String, isGame: Boolean) {
        context.dataStore.edit { prefs ->
            val games = (prefs[KEY_CUSTOM_GAMES] ?: emptySet()).toMutableSet()
            val nonGames = (prefs[KEY_CUSTOM_NON_GAMES] ?: emptySet()).toMutableSet()
            if (isGame) {
                games.add(packageName)
                nonGames.remove(packageName)
            } else {
                nonGames.add(packageName)
                games.remove(packageName)
            }
            prefs[KEY_CUSTOM_GAMES] = games
            prefs[KEY_CUSTOM_NON_GAMES] = nonGames
        }
    }
    val dnsBlockerModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DNS_BLOCKER_MODE] ?: "off"
    }

    suspend fun setDnsBlockerMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DNS_BLOCKER_MODE] = mode
            // Also sync legacy enabled key for safety
            prefs[KEY_ADS_BLOCKER_ENABLED] = (mode != "off")
        }
    }

    val lockedAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOCKED_APPS] ?: emptySet()
    }

    val appLockPinFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_LOCK_PIN] ?: "1234"
    }

    suspend fun setAppLocked(packageName: String, locked: Boolean) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_LOCKED_APPS] ?: emptySet()).toMutableSet()
            if (locked) current.add(packageName) else current.remove(packageName)
            prefs[KEY_LOCKED_APPS] = current
        }
    }

    suspend fun setAppLockPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_LOCK_PIN] = pin
        }
    }

    val screensaverTimeoutFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SCREENSAVER_TIMEOUT] ?: "5"
    }

    suspend fun setScreensaverTimeout(timeout: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCREENSAVER_TIMEOUT] = timeout
        }
    }

    val screensaverModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SCREENSAVER_MODE] ?: "clock_date"
    }

    suspend fun setScreensaverMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCREENSAVER_MODE] = mode
        }
    }

    val screensaverWallpaperFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SCREENSAVER_WALLPAPER] ?: "current"
    }

    suspend fun setScreensaverWallpaper(wallpaper: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCREENSAVER_WALLPAPER] = wallpaper
        }
    }

    val screensaverVideoFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SCREENSAVER_VIDEO] ?: "EG_g7BFaL5s"
    }

    suspend fun setScreensaverVideo(videoId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCREENSAVER_VIDEO] = videoId
        }
    }
}
