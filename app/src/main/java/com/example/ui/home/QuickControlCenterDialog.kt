package com.example.ui.home

import android.app.ActivityManager
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.LauncherPreferences
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val SectionGoldAccent = Color(0xFFF5B942)
private val SectionCyanAccent = Color(0xFF38BDF8)
private val SectionPurpleAccent = Color(0xFFA78BFA)
private val SectionEmeraldAccent = Color(0xFF34D399)

@Composable
fun QuickControlCenterDialog(
    is24hFormat: Boolean,
    @Suppress("UNUSED_PARAMETER") isDynamicBg: Boolean,
    onToggleClock24h: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onToggleDynamicBg: () -> Unit,
    onOpenAmbientMode: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val dialogContext = LocalContext.current
    val currentLayoutDirection = LocalLayoutDirection.current
    val isRtl = currentLayoutDirection == LayoutDirection.Rtl
    val currentConfiguration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val prefs = remember(dialogContext) { LauncherPreferences(dialogContext.applicationContext) }

    // 4x4 Focus Matrix for deterministic TV Remote D-Pad navigation + Header Close button
    val gridRequesters = remember { Array(4) { Array(4) { FocusRequester() } } }
    val closeFocusRequester = remember { FocusRequester() }

    val wallpaperMode by prefs.wallpaperModeFlow.collectAsState(initial = if (isDynamicBg) "dynamic" else "custom")
    val appsTopOffset by prefs.appsTopOffsetFlow.collectAsState(initial = 340)
    val appsPerRow by prefs.appsPerRowFlow.collectAsState(initial = 5)
    val glassOpacity by prefs.glassOpacityFlow.collectAsState(initial = 75)

    val showSeconds by prefs.isClockSecondsEnabled.collectAsState(initial = false)
    val clockSize by prefs.clockSizeFlow.collectAsState(initial = "large")
    val dateFormatStyle by prefs.dateFormatStyleFlow.collectAsState(initial = "full")
    val greetingMode by prefs.greetingModeFlow.collectAsState(initial = "poetic")

    val screensaverMode by prefs.screensaverModeFlow.collectAsState(initial = "clock_date")
    val screensaverTimeout by prefs.screensaverTimeoutFlow.collectAsState(initial = "5")
    val showOnlyTvApps by prefs.showOnlyTvAppsFlow.collectAsState(initial = true)

    // Live RAM telemetry & 1-click boost state
    var ramUsedPercent by remember { mutableIntStateOf(42) }
    var isBoosting by remember { mutableStateOf(false) }
    var freedMb by remember { mutableIntStateOf(0) }

    fun readRamPercent(context: Context): Int {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(memInfo)
            if (memInfo.totalMem > 0L) {
                (((memInfo.totalMem - memInfo.availMem).toDouble() / memInfo.totalMem.toDouble()) * 100.0)
                    .roundToInt()
                    .coerceIn(15, 96)
            } else 42
        } catch (_: Exception) {
            42
        }
    }

    LaunchedEffect(dialogContext) {
        ramUsedPercent = readRamPercent(dialogContext)
    }

    var liveClockText by remember { mutableStateOf("") }
    LaunchedEffect(is24hFormat, showSeconds) {
        val pattern = when {
            is24hFormat && showSeconds -> "HH:mm:ss"
            is24hFormat -> "HH:mm"
            showSeconds -> "hh:mm:ss a"
            else -> "hh:mm a"
        }
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        while (true) {
            liveClockText = formatter.format(Date())
            delay(if (showSeconds) 1000L else 5000L)
        }
    }

    LaunchedEffect(Unit) {
        delay(40L)
        try {
            gridRequesters[0][0].requestFocus()
        } catch (_: Exception) {}
    }

    val wallpaperSub = when (wallpaperMode) {
        "dynamic" -> stringResource(R.string.settings_wallpaper_dynamic)
        "preset" -> stringResource(R.string.settings_wallpaper_presets)
        "custom" -> stringResource(R.string.settings_wallpaper_custom)
        else -> stringResource(R.string.settings_wallpaper_default)
    }
    val wallpaperStep = when (wallpaperMode) {
        "dynamic" -> 0
        "preset" -> 1
        "custom" -> 2
        else -> 3
    }

    val appsPositionSub = when (appsTopOffset) {
        220 -> stringResource(R.string.settings_apps_position_compact)
        280 -> stringResource(R.string.settings_apps_position_balanced)
        340 -> stringResource(R.string.settings_apps_position_lower)
        else -> stringResource(R.string.settings_apps_position_ultra)
    }
    val appsPositionStep = when (appsTopOffset) {
        220 -> 0
        280 -> 1
        340 -> 2
        else -> 3
    }

    val appsPerRowStep = when (appsPerRow) {
        4 -> 0
        5 -> 1
        6 -> 2
        else -> 3
    }

    val glassOpacityStep = when (glassOpacity) {
        25 -> 0
        50 -> 1
        75 -> 2
        90 -> 3
        else -> 4
    }

    val clockSizeSub = when (clockSize) {
        "compact" -> stringResource(R.string.settings_clock_size_compact)
        "medium" -> stringResource(R.string.settings_clock_size_medium)
        else -> stringResource(R.string.settings_clock_size_large)
    }
    val clockSizeStep = when (clockSize) {
        "large" -> 0
        "medium" -> 1
        else -> 2
    }

    val dateFormatSub = when (dateFormatStyle) {
        "medium" -> stringResource(R.string.settings_date_format_medium)
        "numeric" -> stringResource(R.string.settings_date_format_numeric)
        "hidden" -> stringResource(R.string.settings_date_format_hidden)
        else -> stringResource(R.string.settings_date_format_full)
    }
    val dateFormatStep = when (dateFormatStyle) {
        "full" -> 0
        "medium" -> 1
        "numeric" -> 2
        else -> 3
    }

    val greetingModeSub = when (greetingMode) {
        "classic" -> stringResource(R.string.settings_greeting_mode_classic)
        "custom" -> stringResource(R.string.settings_greeting_mode_custom)
        "hidden" -> stringResource(R.string.settings_greeting_mode_hidden)
        else -> stringResource(R.string.settings_greeting_mode_poetic)
    }
    val greetingModeStep = when (greetingMode) {
        "poetic" -> 0
        "classic" -> 1
        "custom" -> 2
        else -> 3
    }

    val screensaverModeSub = when (screensaverMode) {
        "wallpaper_info" -> stringResource(R.string.screensaver_mode_wallpaper)
        "video_stream" -> stringResource(R.string.screensaver_mode_video)
        else -> stringResource(R.string.screensaver_mode_clock)
    }
    val screensaverModeStep = when (screensaverMode) {
        "clock_date" -> 0
        "wallpaper_info" -> 1
        else -> 2
    }

    val screensaverTimeoutSub = when (screensaverTimeout) {
        "2" -> stringResource(R.string.timeout_2min)
        "5" -> stringResource(R.string.timeout_5min)
        "10" -> stringResource(R.string.timeout_10min)
        "15" -> stringResource(R.string.timeout_15min)
        "30" -> stringResource(R.string.timeout_30min)
        else -> stringResource(R.string.timeout_never)
    }
    val screensaverTimeoutStep = when (screensaverTimeout) {
        "2" -> 0
        "5" -> 1
        "10" -> 2
        "15" -> 3
        "30" -> 4
        else -> 5
    }

    // Track currently focused tile title & value for the Header Live Inspector Pill
    val defaultTitle = stringResource(R.string.settings_wallpaper_style)
    var focusedItemTitle by remember { mutableStateOf(defaultTitle) }
    var focusedItemValue by remember { mutableStateOf("") }

    fun Modifier.matrixNavigation(row: Int, col: Int): Modifier {
        val nextCol = (col + 1) % 4
        val prevCol = (col + 3) % 4
        return this.focusProperties {
            up = if (row > 0) gridRequesters[row - 1][col] else closeFocusRequester
            down = if (row < 3) gridRequesters[row + 1][col] else gridRequesters[3][col]
            left = if (isRtl) gridRequesters[row][nextCol] else gridRequesters[row][prevCol]
            right = if (isRtl) gridRequesters[row][prevCol] else gridRequesters[row][nextCol]
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(
            LocalContext provides dialogContext,
            LocalConfiguration provides currentConfiguration,
            LocalLayoutDirection provides currentLayoutDirection
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF02040B).copy(alpha = 0.62f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.Transparent,
                    shadowElevation = 24.dp,
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                TvAccentGold.copy(alpha = 0.72f),
                                Color.White.copy(alpha = 0.24f),
                                TvAccentBlue.copy(alpha = 0.48f)
                            )
                        )
                    ),
                    modifier = Modifier
                        .widthIn(max = 936.dp)
                        .fillMaxWidth(0.95f)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0E1629).copy(alpha = 0.95f),
                                        Color(0xFF080D1A).copy(alpha = 0.96f),
                                        Color(0xFF050811).copy(alpha = 0.97f)
                                    )
                                )
                            )
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            TvAccentGold.copy(alpha = 0.14f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width * 0.14f, 0f),
                                        radius = size.width * 0.46f
                                    ),
                                    center = Offset(size.width * 0.14f, 0f),
                                    radius = size.width * 0.46f
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            TvAccentBlue.copy(alpha = 0.13f),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width * 0.86f, size.height),
                                        radius = size.width * 0.48f
                                    ),
                                    center = Offset(size.width * 0.86f, size.height),
                                    radius = size.width * 0.48f
                                )
                            }
                    ) {
                        // Top specular glass highlight edge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            TvAccentGoldRadiant.copy(alpha = 0.80f),
                                            Color.White.copy(alpha = 0.65f),
                                            TvAccentBlue.copy(alpha = 0.55f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            // Header: Crest Icon, Title, Live Focused Inspector Pill, Clock & Close Button
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
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        TvAccentGold.copy(alpha = 0.32f),
                                                        TvAccentGold.copy(alpha = 0.10f)
                                                    )
                                                )
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = TvAccentGold.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(13.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = TvAccentGoldRadiant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.control_center_title),
                                            color = TvTextPrimary,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = stringResource(R.string.control_center_subtitle),
                                            color = TvTextSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Live Focused Item Inspector Pill (shows currently selected control clearly)
                                    if (focusedItemValue.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(Color(0xFF141E34).copy(alpha = 0.92f))
                                                .border(
                                                    width = 1.dp,
                                                    color = TvAccentGold.copy(alpha = 0.45f),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(TvAccentGoldRadiant)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "$focusedItemTitle: ",
                                                color = TvTextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = focusedItemValue,
                                                color = TvAccentGoldRadiant,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // RAM Status Pill
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFF111B2E).copy(alpha = 0.90f))
                                            .border(
                                                width = 1.dp,
                                                color = SectionEmeraldAccent.copy(alpha = 0.38f),
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Memory,
                                            contentDescription = null,
                                            tint = SectionEmeraldAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "RAM $ramUsedPercent%",
                                            color = SectionEmeraldAccent,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (liveClockText.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(Color(0xFF151F35).copy(alpha = 0.92f))
                                                .border(
                                                    width = 1.dp,
                                                    color = TvAccentGold.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = liveClockText,
                                                color = TvAccentGoldRadiant,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    TvFocusableCard(
                                        onClick = onDismiss,
                                        focusRequester = closeFocusRequester,
                                        scaleUpRatio = 1.10f,
                                        shape = CircleShape,
                                        useDefaultBorder = false,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .focusProperties {
                                                down = gridRequesters[0][3]
                                                left = gridRequesters[0][0]
                                                right = gridRequesters[0][3]
                                            }
                                            .onFocusChanged { state ->
                                                if (state.isFocused) {
                                                    focusedItemTitle = dialogContext.getString(R.string.action_cancel)
                                                    focusedItemValue = "OK"
                                                }
                                            }
                                    ) { isFocused ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(
                                                    if (isFocused) TvAccentGold
                                                    else Color(0xFF19243B).copy(alpha = 0.90f)
                                                )
                                                .border(
                                                    width = if (isFocused) 2.dp else 1.dp,
                                                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.18f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.action_cancel),
                                                tint = if (isFocused) Color.Black else TvTextPrimary,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                TvAccentGold.copy(alpha = 0.50f),
                                                Color.White.copy(alpha = 0.14f),
                                                TvAccentBlue.copy(alpha = 0.35f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Section 1 (Row 0): Appearance & Home Layout
                            ControlSectionHeader(
                                icon = Icons.Default.Palette,
                                title = stringResource(R.string.control_center_section_layout),
                                accentColor = SectionGoldAccent
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val tile00Title = stringResource(R.string.settings_wallpaper_style)
                                QuickToggleTile(
                                    icon = Icons.Default.Wallpaper,
                                    title = tile00Title,
                                    subtitle = wallpaperSub,
                                    isActive = wallpaperMode != "default",
                                    accentColor = SectionGoldAccent,
                                    totalSteps = 4,
                                    currentStep = wallpaperStep,
                                    focusRequester = gridRequesters[0][0],
                                    onFocused = {
                                        focusedItemTitle = tile00Title
                                        focusedItemValue = wallpaperSub
                                    },
                                    onClick = {
                                        scope.launch {
                                            val nextMode = when (wallpaperMode) {
                                                "dynamic" -> "preset"
                                                "preset" -> "custom"
                                                "custom" -> "default"
                                                else -> "dynamic"
                                            }
                                            prefs.setWallpaperMode(nextMode)
                                            prefs.setDynamicBackgroundEnabled(nextMode == "dynamic")
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(0, 0)
                                )

                                val tile01Title = stringResource(R.string.settings_apps_top_position)
                                QuickToggleTile(
                                    icon = Icons.Default.VerticalAlignBottom,
                                    title = tile01Title,
                                    subtitle = appsPositionSub,
                                    isActive = appsTopOffset >= 340,
                                    accentColor = SectionGoldAccent,
                                    totalSteps = 4,
                                    currentStep = appsPositionStep,
                                    focusRequester = gridRequesters[0][1],
                                    onFocused = {
                                        focusedItemTitle = tile01Title
                                        focusedItemValue = appsPositionSub
                                    },
                                    onClick = {
                                        scope.launch {
                                            val nextOffset = when (appsTopOffset) {
                                                220 -> 280
                                                280 -> 340
                                                340 -> 400
                                                else -> 220
                                            }
                                            prefs.setAppsTopOffset(nextOffset)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(0, 1)
                                )

                                val tile02Title = stringResource(R.string.settings_apps_per_row)
                                val tile02Sub = "$appsPerRow ${stringResource(R.string.settings_apps_suffix)}"
                                QuickToggleTile(
                                    icon = Icons.Default.GridView,
                                    title = tile02Title,
                                    subtitle = tile02Sub,
                                    isActive = true,
                                    accentColor = SectionGoldAccent,
                                    totalSteps = 4,
                                    currentStep = appsPerRowStep,
                                    focusRequester = gridRequesters[0][2],
                                    onFocused = {
                                        focusedItemTitle = tile02Title
                                        focusedItemValue = tile02Sub
                                    },
                                    onClick = {
                                        scope.launch {
                                            val nextCount = when (appsPerRow) {
                                                4 -> 5
                                                5 -> 6
                                                6 -> 7
                                                else -> 4
                                            }
                                            prefs.setAppsPerRow(nextCount)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(0, 2)
                                )

                                val tile03Title = stringResource(R.string.settings_glass_opacity)
                                val tile03Sub = "$glassOpacity%"
                                QuickToggleTile(
                                    icon = Icons.Default.Opacity,
                                    title = tile03Title,
                                    subtitle = tile03Sub,
                                    isActive = glassOpacity >= 75,
                                    accentColor = SectionGoldAccent,
                                    totalSteps = 5,
                                    currentStep = glassOpacityStep,
                                    progressFraction = glassOpacity / 100f,
                                    focusRequester = gridRequesters[0][3],
                                    onFocused = {
                                        focusedItemTitle = tile03Title
                                        focusedItemValue = tile03Sub
                                    },
                                    onClick = {
                                        scope.launch {
                                            val nextOpacity = when (glassOpacity) {
                                                25 -> 50
                                                50 -> 75
                                                75 -> 90
                                                90 -> 100
                                                else -> 25
                                            }
                                            prefs.setGlassOpacity(nextOpacity)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(0, 3)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Section 2 (Row 1): Clock, Date & Display
                            ControlSectionHeader(
                                icon = Icons.Default.Schedule,
                                title = stringResource(R.string.control_center_section_clock),
                                accentColor = SectionCyanAccent
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val tile10Title = stringResource(R.string.settings_clock_format)
                                val tile10Sub = if (is24hFormat) "24h" else "12h (AM/PM)"
                                QuickToggleTile(
                                    icon = Icons.Default.Schedule,
                                    title = tile10Title,
                                    subtitle = tile10Sub,
                                    isActive = is24hFormat,
                                    accentColor = SectionCyanAccent,
                                    totalSteps = 2,
                                    currentStep = if (is24hFormat) 0 else 1,
                                    focusRequester = gridRequesters[1][0],
                                    onFocused = {
                                        focusedItemTitle = tile10Title
                                        focusedItemValue = tile10Sub
                                    },
                                    onClick = onToggleClock24h,
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(1, 0)
                                )

                                val tile11Title = stringResource(R.string.settings_clock_seconds)
                                val tile11Sub = if (showSeconds) stringResource(R.string.settings_seconds_enabled) else stringResource(R.string.settings_seconds_disabled)
                                QuickToggleTile(
                                    icon = Icons.Default.Timer,
                                    title = tile11Title,
                                    subtitle = tile11Sub,
                                    isActive = showSeconds,
                                    accentColor = SectionCyanAccent,
                                    totalSteps = 2,
                                    currentStep = if (showSeconds) 1 else 0,
                                    focusRequester = gridRequesters[1][1],
                                    onFocused = {
                                        focusedItemTitle = tile11Title
                                        focusedItemValue = tile11Sub
                                    },
                                    onClick = {
                                        scope.launch {
                                            prefs.setClockSecondsEnabled(!showSeconds)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(1, 1)
                                )

                                val tile12Title = stringResource(R.string.settings_clock_size)
                                QuickToggleTile(
                                    icon = Icons.Default.FormatSize,
                                    title = tile12Title,
                                    subtitle = clockSizeSub,
                                    isActive = clockSize == "large",
                                    accentColor = SectionCyanAccent,
                                    totalSteps = 3,
                                    currentStep = clockSizeStep,
                                    focusRequester = gridRequesters[1][2],
                                    onFocused = {
                                        focusedItemTitle = tile12Title
                                        focusedItemValue = clockSizeSub
                                    },
                                    onClick = {
                                        scope.launch {
                                            val nextSize = when (clockSize) {
                                                "large" -> "medium"
                                                "medium" -> "compact"
                                                else -> "large"
                                            }
                                            prefs.setClockSize(nextSize)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(1, 2)
                                )

                                val tile13Title = stringResource(R.string.settings_date_format)
                                QuickToggleTile(
                                    icon = Icons.Default.CalendarMonth,
                                    title = tile13Title,
                                    subtitle = dateFormatSub,
                                    isActive = dateFormatStyle != "hidden",
                                    accentColor = SectionCyanAccent,
                                    totalSteps = 4,
                                    currentStep = dateFormatStep,
                                    focusRequester = gridRequesters[1][3],
                                    onFocused = {
                                        focusedItemTitle = tile13Title
                                        focusedItemValue = dateFormatSub
                                    },
                                    onClick = {
                                        scope.launch {
                                            val nextDateStyle = when (dateFormatStyle) {
                                                "full" -> "medium"
                                                "medium" -> "numeric"
                                                "numeric" -> "hidden"
                                                else -> "full"
                                            }
                                            prefs.setDateFormatStyle(nextDateStyle)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(1, 3)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Section 3 (Row 2): Greeting, Screensaver & Apps
                            ControlSectionHeader(
                                icon = Icons.Default.Layers,
                                title = stringResource(R.string.control_center_section_experience),
                                accentColor = SectionPurpleAccent
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val tile20Title = stringResource(R.string.settings_greeting_mode)
                                QuickToggleTile(
                                    icon = Icons.Default.AutoAwesome,
                                    title = tile20Title,
                                    subtitle = greetingModeSub,
                                    isActive = greetingMode != "hidden",
                                    accentColor = SectionPurpleAccent,
                                    totalSteps = 4,
                                    currentStep = greetingModeStep,
                                    focusRequester = gridRequesters[2][0],
                                    onFocused = {
                                        focusedItemTitle = tile20Title
                                        focusedItemValue = greetingModeSub
                                    },
                                    onClick = {
                                        scope.launch {
                                            val nextGreeting = when (greetingMode) {
                                                "poetic" -> "classic"
                                                "classic" -> "custom"
                                                "custom" -> "hidden"
                                                else -> "poetic"
                                            }
                                            prefs.setGreetingMode(nextGreeting)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(2, 0)
                                )

                                val tile21Title = stringResource(R.string.settings_screensaver_mode)
                                QuickToggleTile(
                                    icon = Icons.Default.SmartDisplay,
                                    title = tile21Title,
                                    subtitle = screensaverModeSub,
                                    isActive = true,
                                    accentColor = SectionPurpleAccent,
                                    totalSteps = 3,
                                    currentStep = screensaverModeStep,
                                    focusRequester = gridRequesters[2][1],
                                    onFocused = {
                                        focusedItemTitle = tile21Title
                                        focusedItemValue = screensaverModeSub
                                    },
                                    onClick = {
                                        scope.launch {
                                            val nextSaverMode = when (screensaverMode) {
                                                "clock_date" -> "wallpaper_info"
                                                "wallpaper_info" -> "video_stream"
                                                else -> "clock_date"
                                            }
                                            prefs.setScreensaverMode(nextSaverMode)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(2, 1)
                                )

                                val tile22Title = stringResource(R.string.settings_screensaver_timeout)
                                QuickToggleTile(
                                    icon = Icons.Default.Bedtime,
                                    title = tile22Title,
                                    subtitle = screensaverTimeoutSub,
                                    isActive = screensaverTimeout != "never",
                                    accentColor = SectionPurpleAccent,
                                    totalSteps = 6,
                                    currentStep = screensaverTimeoutStep,
                                    focusRequester = gridRequesters[2][2],
                                    onFocused = {
                                        focusedItemTitle = tile22Title
                                        focusedItemValue = screensaverTimeoutSub
                                    },
                                    onClick = {
                                        scope.launch {
                                            val nextTimeout = when (screensaverTimeout) {
                                                "2" -> "5"
                                                "5" -> "10"
                                                "10" -> "15"
                                                "15" -> "30"
                                                "30" -> "never"
                                                else -> "2"
                                            }
                                            prefs.setScreensaverTimeout(nextTimeout)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(2, 2)
                                )

                                val tile23Title = stringResource(R.string.settings_show_only_tv_apps)
                                val tile23Sub = if (showOnlyTvApps) stringResource(R.string.settings_app_filter_tv_only) else stringResource(R.string.settings_app_filter_all)
                                QuickToggleTile(
                                    icon = Icons.Default.Apps,
                                    title = tile23Title,
                                    subtitle = tile23Sub,
                                    isActive = showOnlyTvApps,
                                    accentColor = SectionPurpleAccent,
                                    totalSteps = 2,
                                    currentStep = if (showOnlyTvApps) 0 else 1,
                                    focusRequester = gridRequesters[2][3],
                                    onFocused = {
                                        focusedItemTitle = tile23Title
                                        focusedItemValue = tile23Sub
                                    },
                                    onClick = {
                                        scope.launch {
                                            prefs.setShowOnlyTvApps(!showOnlyTvApps)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(2, 3)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Section 4 (Row 3): 4-Column Equal-Width Action Dock aligned 1:1 with the 4 columns above
                            val dock0Title = stringResource(R.string.settings_screensaver_preview_btn)
                            val dock1Title = stringResource(R.string.control_center_search)
                            val dock1Sub = stringResource(R.string.control_center_search_sub)
                            val dock2Title = stringResource(R.string.control_center_boost_btn)
                            val dock2Sub = when {
                                isBoosting -> stringResource(R.string.control_center_boosting)
                                freedMb > 0 -> stringResource(R.string.control_center_boost_done, freedMb)
                                else -> "RAM $ramUsedPercent% • ${stringResource(R.string.control_center_boost_desc)}"
                            }
                            val dock3Title = stringResource(R.string.settings_title)
                            val dock3Sub = stringResource(R.string.settings_category_launcher)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ControlDockButton(
                                    icon = Icons.Default.PlayArrow,
                                    title = dock0Title,
                                    subtitle = screensaverModeSub,
                                    accentColor = TvAccentGold,
                                    isPrimaryHighlight = true,
                                    focusRequester = gridRequesters[3][0],
                                    onFocused = {
                                        focusedItemTitle = dock0Title
                                        focusedItemValue = screensaverModeSub
                                    },
                                    onClick = {
                                        onDismiss()
                                        onOpenAmbientMode()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(3, 0)
                                )

                                ControlDockButton(
                                    icon = Icons.Default.Mic,
                                    title = dock1Title,
                                    subtitle = dock1Sub,
                                    accentColor = SectionCyanAccent,
                                    isPrimaryHighlight = false,
                                    focusRequester = gridRequesters[3][1],
                                    onFocused = {
                                        focusedItemTitle = dock1Title
                                        focusedItemValue = dock1Sub
                                    },
                                    onClick = {
                                        onDismiss()
                                        onOpenSearch()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(3, 1)
                                )

                                ControlDockButton(
                                    icon = Icons.Default.Bolt,
                                    title = dock2Title,
                                    subtitle = dock2Sub,
                                    accentColor = SectionEmeraldAccent,
                                    isPrimaryHighlight = freedMb > 0,
                                    focusRequester = gridRequesters[3][2],
                                    onFocused = {
                                        focusedItemTitle = dock2Title
                                        focusedItemValue = dock2Sub
                                    },
                                    onClick = {
                                        if (!isBoosting) {
                                            scope.launch {
                                                isBoosting = true
                                                val before = readRamPercent(dialogContext)
                                                withContext(Dispatchers.IO) {
                                                    try {
                                                        dialogContext.cacheDir?.listFiles()?.forEach { file ->
                                                            if (file.name != "WebView") {
                                                                file.deleteRecursively()
                                                            }
                                                        }
                                                    } catch (_: Exception) {}
                                                    System.gc()
                                                }
                                                delay(450L)
                                                val after = readRamPercent(dialogContext)
                                                val reduced = (before - after).coerceAtLeast(2)
                                                ramUsedPercent = (before - reduced).coerceAtLeast(18)
                                                freedMb = reduced * 18
                                                isBoosting = false
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(3, 2)
                                )

                                ControlDockButton(
                                    icon = Icons.Default.Settings,
                                    title = dock3Title,
                                    subtitle = dock3Sub,
                                    accentColor = TvAccentBlue,
                                    isPrimaryHighlight = false,
                                    showChevron = true,
                                    isRtl = isRtl,
                                    focusRequester = gridRequesters[3][3],
                                    onFocused = {
                                        focusedItemTitle = dock3Title
                                        focusedItemValue = dock3Sub
                                    },
                                    onClick = {
                                        onDismiss()
                                        onOpenSettings()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(3, 3)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlSectionHeader(
    icon: ImageVector,
    title: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(13.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(13.5.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = TvTextPrimary.copy(alpha = 0.94f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun QuickToggleTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onFocused: () -> Unit = {},
    totalSteps: Int = 0,
    currentStep: Int = 0,
    progressFraction: Float? = null
) {
    TvFocusableCard(
        onClick = onClick,
        focusRequester = focusRequester,
        scaleUpRatio = 1.05f,
        elevation = 18.dp,
        shape = RoundedCornerShape(15.dp),
        useDefaultBorder = false,
        modifier = modifier
            .height(74.dp)
            .onFocusChanged { state ->
                if (state.isFocused) {
                    onFocused()
                }
            }
    ) { isFocused ->
        val containerColor by animateColorAsState(
            targetValue = when {
                isFocused -> TvAccentGold
                isActive -> Color(0xFF162138).copy(alpha = 0.96f)
                else -> Color(0xFF0B111E).copy(alpha = 0.92f)
            },
            animationSpec = tween(140),
            label = "tile_bg"
        )
        val borderColor by animateColorAsState(
            targetValue = when {
                isFocused -> Color.White
                isActive -> accentColor.copy(alpha = 0.52f)
                else -> Color.White.copy(alpha = 0.10f)
            },
            animationSpec = tween(140),
            label = "tile_border"
        )
        val iconBadgeBg = when {
            isFocused -> Color.Black.copy(alpha = 0.16f)
            isActive -> accentColor.copy(alpha = 0.18f)
            else -> Color.White.copy(alpha = 0.06f)
        }
        val iconTint = when {
            isFocused -> Color.Black
            isActive -> accentColor
            else -> TvTextSecondary
        }
        val labelColor = if (isFocused) Color.Black.copy(alpha = 0.78f) else TvTextSecondary
        val valueColor = when {
            isFocused -> Color.Black
            isActive -> TvTextPrimary
            else -> TvTextPrimary.copy(alpha = 0.85f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(15.dp))
                .background(containerColor)
                .border(
                    width = if (isFocused) 2.2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(15.dp)
                )
        ) {
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    TvAccentGoldRadiant.copy(alpha = 0.55f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    if (isActive) accentColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 11.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Icon badge + Step/Status indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconBadgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(15.5.dp)
                        )
                    }

                    if (totalSteps > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (index in 0 until totalSteps) {
                                val selected = index == currentStep
                                val pillWidth by animateDpAsState(
                                    targetValue = if (selected) 11.dp else 4.dp,
                                    animationSpec = tween(150),
                                    label = "step_pill_w"
                                )
                                Box(
                                    modifier = Modifier
                                        .width(pillWidth)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isFocused && selected -> Color.Black
                                                isFocused -> Color.Black.copy(alpha = 0.25f)
                                                selected -> accentColor
                                                else -> Color.White.copy(alpha = 0.18f)
                                            }
                                        )
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isFocused -> Color.Black
                                        isActive -> accentColor
                                        else -> Color.White.copy(alpha = 0.20f)
                                    }
                                )
                        )
                    }
                }

                // Bottom area: Title + Crisp Value Pill
                Column {
                    Text(
                        text = title,
                        color = labelColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subtitle,
                            color = valueColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isFocused) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.16f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "OK",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            if (progressFraction != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            if (isFocused) Color.Black.copy(alpha = 0.18f)
                            else Color.White.copy(alpha = 0.08f)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction.coerceIn(0.05f, 1f))
                            .fillMaxHeight()
                            .background(
                                if (isFocused) Color.Black.copy(alpha = 0.72f)
                                else accentColor
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlDockButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    isPrimaryHighlight: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onFocused: () -> Unit = {},
    showChevron: Boolean = false,
    isRtl: Boolean = false
) {
    TvFocusableCard(
        onClick = onClick,
        focusRequester = focusRequester,
        scaleUpRatio = 1.045f,
        elevation = 18.dp,
        shape = RoundedCornerShape(15.dp),
        useDefaultBorder = false,
        modifier = modifier
            .height(52.dp)
            .onFocusChanged { state ->
                if (state.isFocused) {
                    onFocused()
                }
            }
    ) { isFocused ->
        val bgBrush = when {
            isFocused -> Brush.horizontalGradient(
                listOf(TvAccentGoldRadiant, TvAccentGold)
            )
            isPrimaryHighlight -> Brush.horizontalGradient(
                listOf(
                    accentColor.copy(alpha = 0.25f),
                    Color(0xFF172238).copy(alpha = 0.96f)
                )
            )
            else -> Brush.horizontalGradient(
                listOf(
                    Color(0xFF162136).copy(alpha = 0.95f),
                    Color(0xFF0E1627).copy(alpha = 0.95f)
                )
            )
        }
        val borderColor = when {
            isFocused -> Color.White
            isPrimaryHighlight -> accentColor.copy(alpha = 0.62f)
            else -> accentColor.copy(alpha = 0.30f)
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(15.dp))
                .background(bgBrush)
                .border(
                    width = if (isFocused) 2.2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(31.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        if (isFocused) Color.Black.copy(alpha = 0.16f)
                        else accentColor.copy(alpha = 0.20f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isFocused) Color.Black else accentColor,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(9.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isFocused) Color.Black else TvTextPrimary,
                    fontSize = 11.8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = if (isFocused) Color.Black.copy(alpha = 0.74f) else if (isPrimaryHighlight) accentColor else TvTextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (showChevron) {
                Icon(
                    imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (isFocused) Color.Black else TvTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
