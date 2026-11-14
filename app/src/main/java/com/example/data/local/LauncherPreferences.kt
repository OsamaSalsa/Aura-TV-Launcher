package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.WebBookmark
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
        val KEY_IPTV_PRESETS_INITIALIZED = booleanPreferencesKey("iptv_presets_initialized")
        val KEY_CUSTOM_SCREENSAVER_VIDEOS = stringSetPreferencesKey("custom_screensaver_videos")
        val KEY_DELETED_SCREENSAVER_PRESETS = stringSetPreferencesKey("deleted_screensaver_presets")
        val KEY_AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        val KEY_LAST_PLAYED_CHANNEL_ID = longPreferencesKey("last_played_channel_id")
        val KEY_AUTO_RESUME_LAST_CHANNEL = booleanPreferencesKey("auto_resume_last_channel")
        val KEY_LOCKED_IPTV_CATEGORIES = stringSetPreferencesKey("locked_iptv_categories")
        val KEY_EPG_URL_OVERRIDE = stringPreferencesKey("epg_url_override")
        val KEY_SHOW_RECENT_APPS = booleanPreferencesKey("show_recent_apps_row")
        val KEY_RECENT_APPS = stringPreferencesKey("recent_app_packages")
        val KEY_CUSTOM_WEB_BOOKMARKS = stringSetPreferencesKey("custom_web_bookmarks")
        val KEY_USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_material_color")
        val KEY_SLIDESHOW_SOURCE = stringPreferencesKey("slideshow_source")
        val KEY_SLIDESHOW_FOLDER_URI = stringPreferencesKey("slideshow_folder_uri")
        val KEY_SLIDESHOW_CUSTOM_PHOTOS = stringSetPreferencesKey("slideshow_custom_photos")
        val KEY_SLIDESHOW_INTERVAL_SEC = intPreferencesKey("slideshow_interval_sec")
        val KEY_SLIDESHOW_MOTION = stringPreferencesKey("slideshow_motion")
        val KEY_SLIDESHOW_TRANSITION = stringPreferencesKey("slideshow_transition")
        val KEY_SLIDESHOW_EFFECT = stringPreferencesKey("slideshow_visual_effect")
        val KEY_SLIDESHOW_CLOCK_MODE = stringPreferencesKey("slideshow_clock_mode")
        val KEY_SLIDESHOW_MUSIC_MODE = stringPreferencesKey("slideshow_music_mode")
        val KEY_SLIDESHOW_CUSTOM_MUSIC_URI = stringPreferencesKey("slideshow_custom_music_uri")
        val KEY_SLIDESHOW_MUSIC_VOLUME = intPreferencesKey("slideshow_music_volume")
        val KEY_SLIDESHOW_SHUFFLE = booleanPreferencesKey("slideshow_shuffle")
        val KEY_SLIDESHOW_DIM_LEVEL = intPreferencesKey("slideshow_dim_level")
        val KEY_HEADER_CONTROL_MODE = stringPreferencesKey("header_control_mode")
        val DEFAULT_PRESET_VIDEO_IDS = listOf("EG_g7BFaL5s", "TtLeec_DR-Y", "8xXHeRnLwNQ")
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
        val url = prefs[KEY_CUSTOM_WALLPAPER_URL] ?: ""
        if (url == "aurora_gradient" || url.contains("1543946207")) "" else url
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

    private val quickPrefs by lazy {
        context.applicationContext.getSharedPreferences("aura_quick_prefs", Context.MODE_PRIVATE)
    }

    fun getCachedLanguage(): String {
        return quickPrefs.getString("app_language", null) ?: "system"
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val lang = prefs[KEY_LANGUAGE] ?: "system"
        if (quickPrefs.getString("app_language", null) != lang) {
            quickPrefs.edit().putString("app_language", lang).apply()
        }
        lang
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
        quickPrefs.edit().putString("app_language", lang).apply()
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

    val iptvPresetsInitializedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IPTV_PRESETS_INITIALIZED] ?: false
    }

    suspend fun setIptvPresetsInitialized(initialized: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IPTV_PRESETS_INITIALIZED] = initialized
        }
    }

    val customScreensaverVideosFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_SCREENSAVER_VIDEOS] ?: emptySet()
    }

    val deletedScreensaverPresetsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_DELETED_SCREENSAVER_PRESETS] ?: emptySet()
    }

    suspend fun addCustomScreensaverVideo(videoId: String, customTitle: String = "") {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) return
        context.dataStore.edit { prefs ->
            // If it was a deleted preset, restore it
            val deletedPresets = (prefs[KEY_DELETED_SCREENSAVER_PRESETS] ?: emptySet()).toMutableSet()
            if (deletedPresets.remove(cleanId)) {
                prefs[KEY_DELETED_SCREENSAVER_PRESETS] = deletedPresets
            }
            // If not one of the built-in presets, add to custom videos set
            if (!DEFAULT_PRESET_VIDEO_IDS.contains(cleanId)) {
                val currentCustom = (prefs[KEY_CUSTOM_SCREENSAVER_VIDEOS] ?: emptySet()).toMutableSet()
                currentCustom.removeAll { it.substringBefore("|") == cleanId }
                val entry = if (customTitle.isNotBlank()) "$cleanId|${customTitle.trim()}" else cleanId
                currentCustom.add(entry)
                prefs[KEY_CUSTOM_SCREENSAVER_VIDEOS] = currentCustom
            }
            prefs[KEY_SCREENSAVER_VIDEO] = cleanId
        }
    }

    suspend fun deleteScreensaverVideo(videoId: String) {
        val cleanId = videoId.trim()
        context.dataStore.edit { prefs ->
            val currentCustom = (prefs[KEY_CUSTOM_SCREENSAVER_VIDEOS] ?: emptySet()).toMutableSet()
            currentCustom.removeAll { it.substringBefore("|") == cleanId }
            prefs[KEY_CUSTOM_SCREENSAVER_VIDEOS] = currentCustom

            val deletedPresets = (prefs[KEY_DELETED_SCREENSAVER_PRESETS] ?: emptySet()).toMutableSet()
            if (DEFAULT_PRESET_VIDEO_IDS.contains(cleanId)) {
                deletedPresets.add(cleanId)
                prefs[KEY_DELETED_SCREENSAVER_PRESETS] = deletedPresets
            }

            val currentSelected = prefs[KEY_SCREENSAVER_VIDEO] ?: "EG_g7BFaL5s"
            if (currentSelected == cleanId) {
                val remainingPresets = DEFAULT_PRESET_VIDEO_IDS.filterNot { deletedPresets.contains(it) }
                val remainingCustom = currentCustom.map { it.substringBefore("|") }
                val nextVideo = (remainingCustom + remainingPresets).firstOrNull() ?: ""
                prefs[KEY_SCREENSAVER_VIDEO] = nextVideo
            }
        }
    }

    suspend fun restoreDefaultScreensaverVideos() {
        context.dataStore.edit { prefs ->
            prefs[KEY_DELETED_SCREENSAVER_PRESETS] = emptySet()
            val currentSelected = prefs[KEY_SCREENSAVER_VIDEO] ?: ""
            if (currentSelected.isBlank()) {
                prefs[KEY_SCREENSAVER_VIDEO] = "EG_g7BFaL5s"
            }
        }
    }

    val autoCheckUpdatesFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_CHECK_UPDATES] ?: true
    }

    suspend fun setAutoCheckUpdates(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_CHECK_UPDATES] = enabled
        }
    }

    val lastPlayedChannelIdFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_PLAYED_CHANNEL_ID] ?: -1L
    }

    suspend fun setLastPlayedChannelId(channelId: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_PLAYED_CHANNEL_ID] = channelId
        }
    }

    val autoResumeLastChannelFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_RESUME_LAST_CHANNEL] ?: false
    }

    suspend fun setAutoResumeLastChannel(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_RESUME_LAST_CHANNEL] = enabled
        }
    }

    val lockedIptvCategoriesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOCKED_IPTV_CATEGORIES] ?: emptySet()
    }

    suspend fun setIptvCategoryLocked(category: String, locked: Boolean) {
        val clean = category.trim()
        if (clean.isBlank() || clean == "Favorites" || clean == "All") return
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_LOCKED_IPTV_CATEGORIES] ?: emptySet()).toMutableSet()
            if (locked) current.add(clean) else current.remove(clean)
            prefs[KEY_LOCKED_IPTV_CATEGORIES] = current
        }
    }

    val epgUrlOverrideFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_EPG_URL_OVERRIDE] ?: ""
    }

    suspend fun setEpgUrlOverride(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EPG_URL_OVERRIDE] = url.trim()
        }
    }

    val showRecentAppsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_RECENT_APPS] ?: false
    }

    suspend fun setShowRecentApps(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_RECENT_APPS] = enabled
        }
    }

    val recentAppsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_RECENT_APPS] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    suspend fun recordRecentApp(packageName: String) {
        val clean = packageName.trim()
        if (clean.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_RECENT_APPS] ?: "")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && it != clean }
            val updated = (listOf(clean) + current).take(6)
            prefs[KEY_RECENT_APPS] = updated.joinToString(",")
        }
    }

    val customWebBookmarksFlow: Flow<List<WebBookmark>> = context.dataStore.data.map { prefs ->
        val rawSet = prefs[KEY_CUSTOM_WEB_BOOKMARKS] ?: emptySet()
        rawSet.mapNotNull { entry ->
            val parts = entry.split("|", limit = 3)
            if (parts.size == 3 && parts[0].isNotBlank() && parts[1].isNotBlank() && parts[2].isNotBlank()) {
                WebBookmark(id = parts[0], title = parts[1], url = parts[2])
            } else null
        }.sortedBy { it.title.lowercase() }
    }

    suspend fun addCustomWebBookmark(title: String, url: String) {
        val cleanTitle = title.trim().replace("|", " ")
        var cleanUrl = url.trim()
        if (cleanTitle.isBlank() || cleanUrl.isBlank()) return
        if (!cleanUrl.startsWith("http://", ignoreCase = true) &&
            !cleanUrl.startsWith("https://", ignoreCase = true)
        ) {
            cleanUrl = "https://$cleanUrl"
        }
        val id = System.currentTimeMillis().toString(36)
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_CUSTOM_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            current.add("$id|$cleanTitle|$cleanUrl")
            prefs[KEY_CUSTOM_WEB_BOOKMARKS] = current
        }
    }

    suspend fun removeCustomWebBookmark(id: String) {
        val cleanId = id.trim()
        if (cleanId.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_CUSTOM_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            current.removeAll { it.substringBefore("|") == cleanId }
            prefs[KEY_CUSTOM_WEB_BOOKMARKS] = current
        }
    }

    val useDynamicColorFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_USE_DYNAMIC_COLOR] ?: false
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USE_DYNAMIC_COLOR] = enabled
        }
    }

    // --- Photo Slideshow Screensaver Preferences ---

    val slideshowSourceFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_SOURCE] ?: "online_all"
    }

    suspend fun setSlideshowSource(source: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_SOURCE] = source
        }
    }

    val slideshowFolderUriFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_FOLDER_URI] ?: ""
    }

    suspend fun setSlideshowFolderUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_FOLDER_URI] = uri.trim()
        }
    }

    val slideshowCustomPhotosFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_CUSTOM_PHOTOS] ?: emptySet()
    }

    suspend fun addSlideshowCustomPhotos(uris: Collection<String>) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_SLIDESHOW_CUSTOM_PHOTOS] ?: emptySet()).toMutableSet()
            uris.map { it.trim() }.filter { it.isNotBlank() }.forEach { current.add(it) }
            prefs[KEY_SLIDESHOW_CUSTOM_PHOTOS] = current
        }
    }

    suspend fun clearSlideshowCustomPhotos() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_CUSTOM_PHOTOS] = emptySet()
        }
    }

    val slideshowIntervalSecFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_INTERVAL_SEC] ?: 10
    }

    suspend fun setSlideshowIntervalSec(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_INTERVAL_SEC] = seconds.coerceIn(3, 600)
        }
    }

    val slideshowMotionFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_MOTION] ?: "ken_burns"
    }

    suspend fun setSlideshowMotion(motion: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_MOTION] = motion
        }
    }

    val slideshowTransitionFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_TRANSITION] ?: "crossfade"
    }

    suspend fun setSlideshowTransition(transition: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_TRANSITION] = transition
        }
    }

    val slideshowEffectFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_EFFECT] ?: "snow"
    }

    suspend fun setSlideshowEffect(effect: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_EFFECT] = effect
        }
    }

    val slideshowClockModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_CLOCK_MODE] ?: "corner_full"
    }

    suspend fun setSlideshowClockMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_CLOCK_MODE] = mode
        }
    }

    val slideshowMusicModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_MUSIC_MODE] ?: "off"
    }

    suspend fun setSlideshowMusicMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_MUSIC_MODE] = mode
        }
    }

    val slideshowCustomMusicUriFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_CUSTOM_MUSIC_URI] ?: ""
    }

    suspend fun setSlideshowCustomMusicUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_CUSTOM_MUSIC_URI] = uri.trim()
        }
    }

    val slideshowMusicVolumeFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_MUSIC_VOLUME] ?: 50
    }

    suspend fun setSlideshowMusicVolume(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_MUSIC_VOLUME] = percent.coerceIn(10, 100)
        }
    }

    val slideshowShuffleFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_SHUFFLE] ?: true
    }

    suspend fun setSlideshowShuffle(shuffle: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_SHUFFLE] = shuffle
        }
    }

    val slideshowDimLevelFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_SLIDESHOW_DIM_LEVEL] ?: 20
    }

    suspend fun setSlideshowDimLevel(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_DIM_LEVEL] = percent.coerceIn(0, 70)
        }
    }

    val headerControlModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_HEADER_CONTROL_MODE] ?: "control_center"
    }

    suspend fun setHeaderControlMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HEADER_CONTROL_MODE] = mode
        }
    }
}

