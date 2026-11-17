package com.example.ui.settings

import android.content.Context
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.IptvPlaylist
import com.example.ui.common.TvActionButton
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.TvTextField
import com.example.ui.home.extractYouTubeVideoId
import com.example.ui.home.screensaverPresetVideos
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
internal fun BackupAndPerformanceSettingsCards(
    context: Context,
    appLanguage: String,
    lowRamMode: Boolean,
    nightComfortMode: String,
    onSetLowRamMode: (Boolean) -> Unit,
    onSetNightComfortMode: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    val isArabic = appLanguage.startsWith("ar") || appLanguage == "fa" || appLanguage == "ur"

    // 1. Night Comfort Dimming Schedule Card
    SettingsGlassCard {
        SettingsCardHeader(
            icon = Icons.Default.Bedtime,
            title = if (isArabic) "فلتر راحة العين الليلي (Night Comfort)" else "${stringResource(R.string.slideshow_dim_label)} (Night Comfort)",
            subtitle = if (isArabic) "تعتيم دافئ للشاشة تلقائياً في المساء (22:00 - 06:00) أو بشكل دائم لراحة العين" else "Warm eye-comfort screen dimming automatically at night (22:00–06:00) or always on"
        )
        Spacer(modifier = Modifier.height(14.dp))
        TvSelectField(
            label = stringResource(R.string.slideshow_dim_label),
            options = listOf(
                "off" to stringResource(R.string.settings_seconds_disabled),
                "auto" to if (isArabic) "تلقائي ليلاً (22:00 - 06:00)" else "Auto Night (22:00–06:00)",
                "always" to stringResource(R.string.settings_seconds_enabled)
            ),
            selectedOption = nightComfortMode,
            onOptionSelected = onSetNightComfortMode
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Lite / Low-RAM Performance Mode Card
    SettingsGlassCard {
        SettingsCardHeader(
            icon = Icons.Default.Memory,
            title = if (isArabic) "وضع الأداء الخفيف للأجهزة الضعيفة (Low-RAM Mode)" else "Lite / Low-RAM TV Performance Mode",
            subtitle = if (isArabic) "تقليل استهلاك الذاكرة العشوائية وتحرير ذاكرة الصور والويب فوراً" else "Reduce memory footprint and release image/WebView caches aggressively"
        )
        Spacer(modifier = Modifier.height(14.dp))
        TvSelectField(
            label = if (isArabic) "وضع توفير الذاكرة" else "Low-RAM Optimization",
            options = listOf(
                false to stringResource(R.string.settings_seconds_disabled),
                true to stringResource(R.string.settings_seconds_enabled)
            ),
            selectedOption = lowRamMode,
            onOptionSelected = onSetLowRamMode
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Full Backup & Restore (JSON) Card
    SettingsGlassCard {
        SettingsCardHeader(
            icon = Icons.Default.Save,
            title = if (isArabic) "النسخ الاحتياطي واستعادة الإعدادات (Backup & Restore)" else "Backup & Restore Settings & IPTV (JSON)",
            subtitle = if (isArabic) "حفظ واسترجاع جميع إعدادات اللانشر وقوائم القنوات والاختصارات في مجلد التنزيلات (aura_tv_backup.json)" else "Export or restore all launcher settings, bookmarks, and IPTV playlists (Downloads/aura_tv_backup.json)"
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TvActionButton(
                text = if (isArabic) "حفظ نسخة احتياطية" else "Export Backup",
                icon = Icons.Default.Save,
                onClick = onExportBackup,
                baseColor = TvAccentGold.copy(alpha = 0.88f),
                focusedColor = TvAccentGold,
                fillWidth = true,
                modifier = Modifier.weight(1f)
            )
            TvActionButton(
                text = if (isArabic) "استعادة النسخة الاحتياطية" else "Restore Backup",
                icon = Icons.Default.Restore,
                onClick = onImportBackup,
                baseColor = TvAccentBlue.copy(alpha = 0.85f),
                focusedColor = TvAccentGold,
                fillWidth = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun ScreensaverSettingsSection(
    context: Context,
    viewModel: SettingsViewModel,
    screensaverTimeout: String,
    screensaverMode: String,
    screensaverVideo: String,
    customScreensaverVideos: Set<String>,
    deletedScreensaverPresets: Set<String>,
    slideshowSource: String,
    slideshowFolderUri: String,
    slideshowCustomPhotos: Set<String>,
    slideshowIntervalSec: Int,
    slideshowMotion: String,
    slideshowTransition: String,
    slideshowEffect: String,
    slideshowClockMode: String,
    slideshowMusicMode: String,
    slideshowCustomMusicUri: String,
    slideshowMusicVolume: Int,
    slideshowShuffle: Boolean,
    slideshowDimLevel: Int,
    onShowTvFolderPicker: () -> Unit,
    onLaunchMultiPhotosPicker: () -> Unit,
    onLaunchMusicPicker: () -> Unit,
    onPreviewScreensaver: () -> Unit
) {
    var customVideoInput by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
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
                        "slideshow" to stringResource(R.string.screensaver_mode_slideshow),
                        "video_stream" to stringResource(R.string.screensaver_mode_video)
                    ),
                    selectedOption = screensaverMode,
                    onOptionSelected = { viewModel.setScreensaverMode(it) }
                )
            }
        }

        if (screensaverMode == "wallpaper_info") {
            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.AutoAwesome,
                        title = stringResource(R.string.slideshow_effects_title),
                        subtitle = stringResource(R.string.slideshow_effects_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_effect_label),
                        options = listOf(
                            "snow" to stringResource(R.string.slideshow_effect_snow),
                            "fireflies" to stringResource(R.string.slideshow_effect_fireflies),
                            "rain" to stringResource(R.string.slideshow_effect_rain),
                            "autumn" to stringResource(R.string.slideshow_effect_autumn),
                            "sakura" to stringResource(R.string.slideshow_effect_sakura),
                            "stars" to stringResource(R.string.slideshow_effect_stars),
                            "bokeh" to stringResource(R.string.slideshow_effect_bokeh),
                            "none" to stringResource(R.string.slideshow_effect_none)
                        ),
                        selectedOption = slideshowEffect,
                        onOptionSelected = { viewModel.setSlideshowEffect(it) }
                    )
                }
            }
        }

        if (screensaverMode == "slideshow") {
            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.PhotoLibrary,
                        title = stringResource(R.string.slideshow_source_title),
                        subtitle = stringResource(R.string.slideshow_source_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_source_title),
                        options = listOf(
                            "folder" to stringResource(R.string.slideshow_source_folder),
                            "custom_photos" to stringResource(R.string.slideshow_source_custom_photos),
                            "online_all" to stringResource(R.string.slideshow_source_online_all),
                            "online_nature" to stringResource(R.string.slideshow_source_online_nature),
                            "online_space" to stringResource(R.string.slideshow_source_online_space),
                            "online_cities" to stringResource(R.string.slideshow_source_online_cities)
                        ),
                        selectedOption = slideshowSource,
                        onOptionSelected = { viewModel.setSlideshowSource(it) }
                    )

                    if (slideshowSource == "folder") {
                        Spacer(modifier = Modifier.height(12.dp))
                        val folderDisplay = remember(slideshowFolderUri) {
                            com.example.ui.home.formatSlideshowFolderDisplay(slideshowFolderUri)
                        }
                        val detectedPhotoCount = remember(slideshowFolderUri) {
                            com.example.ui.home.loadSlideshowFolderImages(context, slideshowFolderUri).size
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (folderDisplay.isNotBlank()) {
                                        "$folderDisplay ($detectedPhotoCount)"
                                    } else {
                                        stringResource(R.string.slideshow_no_folder_selected)
                                    },
                                    color = if (folderDisplay.isNotBlank()) TvAccentGoldRadiant else TvTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            TvActionButton(
                                text = stringResource(R.string.slideshow_select_folder_btn),
                                icon = Icons.Default.FolderOpen,
                                onClick = onShowTvFolderPicker,
                                baseColor = TvAccentGold.copy(alpha = 0.88f),
                                focusedColor = TvAccentGold
                            )
                        }
                    }

                    if (slideshowSource == "custom_photos") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${slideshowCustomPhotos.size} ${stringResource(R.string.slideshow_source_custom_photos)}",
                                color = TvAccentGoldRadiant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TvActionButton(
                                    text = stringResource(R.string.slideshow_pick_photos_btn),
                                    icon = Icons.Default.Add,
                                    onClick = onLaunchMultiPhotosPicker,
                                    baseColor = TvAccentGold.copy(alpha = 0.88f),
                                    focusedColor = TvAccentGold
                                )
                                if (slideshowCustomPhotos.isNotEmpty()) {
                                    TvActionButton(
                                        text = stringResource(R.string.action_delete),
                                        icon = Icons.Default.Delete,
                                        onClick = { viewModel.clearSlideshowCustomPhotos() },
                                        baseColor = TvAccentRed.copy(alpha = 0.3f),
                                        focusedColor = TvAccentRed
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_order_label),
                        options = listOf(
                            true to stringResource(R.string.slideshow_order_shuffle),
                            false to stringResource(R.string.slideshow_order_sequential)
                        ),
                        selectedOption = slideshowShuffle,
                        onOptionSelected = { viewModel.setSlideshowShuffle(it) }
                    )
                }
            }

            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.Timer,
                        title = stringResource(R.string.slideshow_display_title),
                        subtitle = stringResource(R.string.slideshow_display_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_interval_label),
                        options = listOf(
                            5 to stringResource(R.string.slideshow_interval_5s),
                            10 to stringResource(R.string.slideshow_interval_10s),
                            15 to stringResource(R.string.slideshow_interval_15s),
                            30 to stringResource(R.string.slideshow_interval_30s),
                            60 to stringResource(R.string.slideshow_interval_1m),
                            120 to stringResource(R.string.slideshow_interval_2m),
                            300 to stringResource(R.string.slideshow_interval_5m)
                        ),
                        selectedOption = slideshowIntervalSec,
                        onOptionSelected = { viewModel.setSlideshowIntervalSec(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_motion_label),
                        options = listOf(
                            "ken_burns" to stringResource(R.string.slideshow_motion_ken_burns),
                            "gentle_zoom" to stringResource(R.string.slideshow_motion_gentle_zoom),
                            "static" to stringResource(R.string.slideshow_motion_static)
                        ),
                        selectedOption = slideshowMotion,
                        onOptionSelected = { viewModel.setSlideshowMotion(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_transition_label),
                        options = listOf(
                            "crossfade" to stringResource(R.string.slideshow_transition_crossfade),
                            "slide" to stringResource(R.string.slideshow_transition_slide),
                            "zoom_fade" to stringResource(R.string.slideshow_transition_zoom_fade),
                            "blur_reveal" to stringResource(R.string.slideshow_transition_blur_reveal),
                            "flip_3d" to stringResource(R.string.slideshow_transition_flip_3d),
                            "random" to stringResource(R.string.slideshow_transition_random)
                        ),
                        selectedOption = slideshowTransition,
                        onOptionSelected = { viewModel.setSlideshowTransition(it) }
                    )
                }
            }

            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.AutoAwesome,
                        title = stringResource(R.string.slideshow_effects_title),
                        subtitle = stringResource(R.string.slideshow_effects_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_effect_label),
                        options = listOf(
                            "snow" to stringResource(R.string.slideshow_effect_snow),
                            "fireflies" to stringResource(R.string.slideshow_effect_fireflies),
                            "rain" to stringResource(R.string.slideshow_effect_rain),
                            "autumn" to stringResource(R.string.slideshow_effect_autumn),
                            "sakura" to stringResource(R.string.slideshow_effect_sakura),
                            "stars" to stringResource(R.string.slideshow_effect_stars),
                            "bokeh" to stringResource(R.string.slideshow_effect_bokeh),
                            "none" to stringResource(R.string.slideshow_effect_none)
                        ),
                        selectedOption = slideshowEffect,
                        onOptionSelected = { viewModel.setSlideshowEffect(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_clock_label),
                        options = listOf(
                            "corner_full" to stringResource(R.string.slideshow_clock_corner_full),
                            "minimal" to stringResource(R.string.slideshow_clock_minimal),
                            "hidden" to stringResource(R.string.slideshow_clock_hidden)
                        ),
                        selectedOption = slideshowClockMode,
                        onOptionSelected = { viewModel.setSlideshowClockMode(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_dim_label),
                        options = listOf(
                            0 to "0%",
                            20 to "20%",
                            35 to "35%",
                            50 to "50%"
                        ),
                        selectedOption = slideshowDimLevel,
                        onOptionSelected = { viewModel.setSlideshowDimLevel(it) }
                    )
                }
            }

            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.MusicNote,
                        title = stringResource(R.string.slideshow_music_title),
                        subtitle = stringResource(R.string.slideshow_music_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_music_label),
                        options = listOf(
                            "off" to stringResource(R.string.slideshow_music_off),
                            "lofi_chill" to stringResource(R.string.slideshow_music_lofi),
                            "nature_rain" to stringResource(R.string.slideshow_music_nature),
                            "ambient_space" to stringResource(R.string.slideshow_music_space),
                            "oriental_cafe" to stringResource(R.string.slideshow_music_oriental),
                            "custom_file" to stringResource(R.string.slideshow_music_custom)
                        ),
                        selectedOption = slideshowMusicMode,
                        onOptionSelected = { viewModel.setSlideshowMusicMode(it) }
                    )

                    if (slideshowMusicMode == "custom_file") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = slideshowCustomMusicUri.ifBlank {
                                    stringResource(R.string.slideshow_music_custom)
                                },
                                color = if (slideshowCustomMusicUri.isNotBlank()) TvAccentGoldRadiant else TvTextSecondary,
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            TvActionButton(
                                text = stringResource(R.string.slideshow_music_pick_btn),
                                icon = Icons.Default.MusicNote,
                                onClick = onLaunchMusicPicker,
                                baseColor = TvAccentGold.copy(alpha = 0.88f),
                                focusedColor = TvAccentGold
                            )
                        }
                    }

                    if (slideshowMusicMode != "off") {
                        Spacer(modifier = Modifier.height(10.dp))
                        TvSelectField(
                            label = stringResource(R.string.slideshow_music_volume_label),
                            options = listOf(
                                25 to "25%",
                                50 to "50%",
                                75 to "75%",
                                100 to "100%"
                            ),
                            selectedOption = slideshowMusicVolume,
                            onOptionSelected = { viewModel.setSlideshowMusicVolume(it) }
                        )
                    }
                }
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
                            ScreensaverVideoRowItem(
                                videoId = presetVideo.id,
                                title = stringResource(presetVideo.titleRes),
                                subtitle = stringResource(presetVideo.subtitleRes),
                                isSelected = isSelected,
                                onSelect = { viewModel.setScreensaverVideo(presetVideo.id) },
                                onDelete = { viewModel.deleteScreensaverVideo(presetVideo.id) }
                            )
                        }

                        customVideoEntries.forEach { (customId, customTitle) ->
                            val isSelected = activeVideoId == customId
                            val displayTitle = if (customTitle.isNotBlank()) {
                                customTitle
                            } else {
                                stringResource(R.string.screensaver_video_custom_item_title).format(customId)
                            }
                            val displaySub = stringResource(R.string.screensaver_video_custom_item_sub).format(customId)

                            ScreensaverVideoRowItem(
                                videoId = customId,
                                title = displayTitle,
                                subtitle = displaySub,
                                isSelected = isSelected,
                                onSelect = { viewModel.setScreensaverVideo(customId) },
                                onDelete = { viewModel.deleteScreensaverVideo(customId) }
                            )
                        }

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

        item {
            SettingActionTile(
                icon = Icons.Default.PlayArrow,
                title = stringResource(R.string.settings_screensaver_preview_btn),
                desc = stringResource(R.string.settings_screensaver_preview_desc),
                onClick = onPreviewScreensaver
            )
        }
    }
}

@Composable
private fun ScreensaverVideoRowItem(
    videoId: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TvFocusableCard(
            onClick = onSelect,
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
                        model = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
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
                        text = title,
                        color = if (isSelected || isFocused) TvTextPrimary else TvTextPrimary.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
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
            onClick = onDelete,
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

@Composable
internal fun IptvSettingsSection(
    viewModel: SettingsViewModel,
    iptvPlaylists: List<IptvPlaylist>,
    activeIptvPlaylist: IptvPlaylist?,
    autoResumeLastChannel: Boolean,
    epgUrlOverride: String
) {
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistUrl by remember { mutableStateOf("") }
    var epgUrlInput by remember(epgUrlOverride) { mutableStateOf(epgUrlOverride) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SettingsGlassCard(
                highlightColor = TvAccentGold,
                highlightActive = activeIptvPlaylist != null
            ) {
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
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(TvAccentGold.copy(alpha = 0.18f))
                                .border(1.dp, TvAccentGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = TvAccentGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = activeIptvPlaylist?.name ?: stringResource(R.string.iptv_title),
                                    color = TvTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (activeIptvPlaylist != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TvAccentGold)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
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
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeIptvPlaylist?.let {
                                    "${pluralStringResource(R.plurals.settings_iptv_channels_count, it.channelCount, it.channelCount)} • ${it.url}"
                                } ?: stringResource(R.string.iptv_no_playlist),
                                color = TvTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TvActionButton(
                            text = stringResource(R.string.settings_iptv_sync_button),
                            icon = Icons.Default.Refresh,
                            onClick = { viewModel.refreshIptvPlaylist() },
                            baseColor = TvAccentGold.copy(alpha = 0.85f),
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
        }

        item {
            SettingsGlassCard {
                SettingsCardHeader(
                    icon = Icons.Default.Add,
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("https://", "http://", ".m3u", ".m3u8").forEach { token ->
                            TvFocusableCard(
                                onClick = {
                                    newPlaylistUrl = if (token.startsWith("http")) {
                                        if (newPlaylistUrl.startsWith("http")) newPlaylistUrl else token + newPlaylistUrl
                                    } else {
                                        if (newPlaylistUrl.endsWith(token)) newPlaylistUrl else newPlaylistUrl + token
                                    }
                                },
                                scaleUpRatio = 1.06f,
                                elevation = 0.dp,
                                cardBackgroundColor = Color.Transparent,
                                useDefaultBorder = false,
                                modifier = Modifier.height(36.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isFocused) TvAccentGold else Color(0xFF1A2540)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isFocused) Color.White else TvAccentGold.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = token,
                                        color = if (isFocused) Color.Black else TvAccentGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            baseColor = TvAccentGold.copy(alpha = 0.90f),
                            focusedColor = TvAccentGold
                        )
                    }
                }
            }
        }

        item {
            SettingsGlassCard {
                SettingsCardHeader(
                    icon = Icons.Default.PlayArrow,
                    title = stringResource(R.string.settings_iptv_auto_resume_title),
                    subtitle = stringResource(R.string.settings_iptv_auto_resume_desc)
                )
                Spacer(modifier = Modifier.height(14.dp))
                TvSelectField(
                    label = stringResource(R.string.settings_iptv_auto_resume_title),
                    options = listOf(
                        false to stringResource(R.string.settings_seconds_disabled),
                        true to stringResource(R.string.settings_seconds_enabled)
                    ),
                    selectedOption = autoResumeLastChannel,
                    onOptionSelected = { viewModel.setAutoResumeLastChannel(it) }
                )
            }
        }

        item {
            val detectedEpgUrl = activeIptvPlaylist?.epgUrl.orEmpty()
            SettingsGlassCard {
                SettingsCardHeader(
                    icon = Icons.Default.CalendarMonth,
                    title = stringResource(R.string.settings_iptv_epg_title),
                    subtitle = if (detectedEpgUrl.isNotBlank()) {
                        "${stringResource(R.string.settings_iptv_epg_desc)} ($detectedEpgUrl)"
                    } else {
                        stringResource(R.string.settings_iptv_epg_desc)
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvTextField(
                        value = epgUrlInput,
                        onValueChange = { epgUrlInput = it },
                        placeholder = stringResource(R.string.settings_iptv_epg_hint),
                        modifier = Modifier.weight(1f)
                    )
                    TvActionButton(
                        text = stringResource(R.string.settings_iptv_epg_save),
                        icon = Icons.Default.Check,
                        onClick = { viewModel.setEpgUrlOverride(epgUrlInput) },
                        baseColor = TvAccentGold.copy(alpha = 0.88f),
                        focusedColor = TvAccentGold,
                        modifier = Modifier.height(52.dp)
                    )
                    TvActionButton(
                        text = stringResource(R.string.settings_iptv_epg_reload),
                        icon = Icons.Default.Refresh,
                        onClick = { viewModel.refreshEpg() },
                        baseColor = Color(0xFF23304A),
                        focusedColor = TvAccentGold,
                        modifier = Modifier.height(52.dp)
                    )
                }
            }
        }

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
                                TvFocusableCard(
                                    onClick = {
                                        if (isActive) {
                                            viewModel.refreshIptvPlaylist()
                                        } else {
                                            viewModel.switchIptvPlaylist(playlist.id)
                                        }
                                    },
                                    scaleUpRatio = 1.02f,
                                    elevation = 0.dp,
                                    cardBackgroundColor = Color.Transparent,
                                    useDefaultBorder = false,
                                    modifier = Modifier.fillMaxWidth()
                                ) { cardFocused ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (cardFocused) TvAccentGold.copy(alpha = 0.20f)
                                                else Color.White.copy(alpha = 0.04f)
                                            )
                                            .border(
                                                width = if (cardFocused) 2.dp else 1.dp,
                                                color = if (cardFocused) TvAccentGold else Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isActive || cardFocused) TvAccentGold.copy(alpha = 0.22f)
                                                    else Color.White.copy(alpha = 0.08f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.LiveTv,
                                                contentDescription = null,
                                                tint = if (isActive || cardFocused) TvAccentGoldRadiant else TvTextPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = playlist.name,
                                                color = TvTextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = pluralStringResource(R.plurals.settings_iptv_channels_count, playlist.channelCount, playlist.channelCount),
                                                color = if (isActive || cardFocused) TvAccentGoldRadiant else TvTextSecondary,
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
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = playlist.url,
                                    color = TvTextSecondary.copy(alpha = 0.72f),
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(12.dp))

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
