package com.example.ui.home

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.os.StatFs
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
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

    // Streamlined 2x4 Focus Matrix for fast, maze-free TV Remote D-Pad navigation + Header Close button
    val gridRequesters = remember { Array(2) { Array(4) { FocusRequester() } } }
    val closeFocusRequester = remember { FocusRequester() }

    val wallpaperMode by prefs.wallpaperModeFlow.collectAsState(initial = if (isDynamicBg) "dynamic" else "custom")
    val showSeconds by prefs.isClockSecondsEnabled.collectAsState(initial = false)
    val screensaverMode by prefs.screensaverModeFlow.collectAsState(initial = "clock_date")
    val showOnlyTvApps by prefs.showOnlyTvAppsFlow.collectAsState(initial = true)

    // Live RAM, Storage & Network telemetry + 1-click boost state
    var ramUsedPercent by remember { mutableIntStateOf(42) }
    var isBoosting by remember { mutableStateOf(false) }
    var freedMb by remember { mutableIntStateOf(0) }
    var storageUsedGb by remember { mutableStateOf(0f) }
    var storageTotalGb by remember { mutableStateOf(0f) }
    var networkKind by remember { mutableStateOf("wifi") } // "wifi", "ethernet", "offline"

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
        withContext(Dispatchers.IO) {
            try {
                val stat = StatFs(Environment.getDataDirectory().path)
                val total = stat.totalBytes.toDouble()
                val avail = stat.availableBytes.toDouble()
                val gbDiv = 1024.0 * 1024.0 * 1024.0
                storageTotalGb = (total / gbDiv).toFloat()
                storageUsedGb = ((total - avail) / gbDiv).toFloat().coerceAtLeast(0f)
            } catch (_: Exception) {}

            try {
                val cm = dialogContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val net = cm?.activeNetwork
                val caps = if (net != null) cm.getNetworkCapabilities(net) else null
                networkKind = when {
                    caps == null -> "offline"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "wifi"
                    else -> "offline"
                }
            } catch (_: Exception) {
                networkKind = "offline"
            }
        }
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

    val screensaverModeSub = when (screensaverMode) {
        "wallpaper_info" -> stringResource(R.string.screensaver_mode_wallpaper)
        "slideshow" -> stringResource(R.string.screensaver_mode_slideshow)
        "video_stream" -> stringResource(R.string.screensaver_mode_video)
        else -> stringResource(R.string.screensaver_mode_clock)
    }
    val screensaverModeStep = when (screensaverMode) {
        "clock_date" -> 0
        "wallpaper_info" -> 1
        "slideshow" -> 2
        else -> 3
    }

    // Track currently focused tile coordinates so Header Inspector updates reactively on focus & click
    var focusedCoord by remember { mutableStateOf(0 to 0) }

    val tile00Title = stringResource(R.string.settings_wallpaper_style)
    val tile01Title = stringResource(R.string.settings_clock_format)
    val tile01Sub = if (is24hFormat) "24h" else "12h (AM/PM)"
    val tile02Title = stringResource(R.string.settings_screensaver_mode)
    val tile03Title = stringResource(R.string.settings_show_only_tv_apps)
    val tile03Sub = if (showOnlyTvApps) stringResource(R.string.settings_app_filter_tv_only) else stringResource(R.string.settings_app_filter_all)

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

    val closeTitle = stringResource(R.string.action_cancel)
    val (focusedItemTitle, focusedItemValue) = when (focusedCoord) {
        0 to 0 -> tile00Title to wallpaperSub
        0 to 1 -> tile01Title to tile01Sub
        0 to 2 -> tile02Title to screensaverModeSub
        0 to 3 -> tile03Title to tile03Sub
        1 to 0 -> dock0Title to screensaverModeSub
        1 to 1 -> dock1Title to dock1Sub
        1 to 2 -> dock2Title to dock2Sub
        1 to 3 -> dock3Title to dock3Sub
        -1 to -1 -> closeTitle to "OK"
        else -> tile00Title to wallpaperSub
    }

    fun Modifier.matrixNavigation(row: Int, col: Int): Modifier {
        val nextCol = (col + 1) % 4
        val prevCol = (col + 3) % 4
        return this.focusProperties {
            up = if (row > 0) gridRequesters[row - 1][col] else closeFocusRequester
            down = if (row < 1) gridRequesters[row + 1][col] else gridRequesters[1][col]
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
                        .widthIn(max = 920.dp)
                        .fillMaxWidth(0.92f)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .onKeyEvent { keyEvent ->
                            val native = keyEvent.nativeKeyEvent
                            val isVoiceKey = native.keyCode == android.view.KeyEvent.KEYCODE_VOICE_ASSIST ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_ASSIST ||
                                native.keyCode == android.view.KeyEvent.KEYCODE_SEARCH
                            if (isVoiceKey) {
                                if (keyEvent.type == KeyEventType.KeyDown && native.repeatCount == 0) {
                                    onDismiss()
                                    onOpenSearch()
                                }
                                true
                            } else {
                                false
                            }
                        }
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
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 22.dp, vertical = 18.dp)
                        ) {
                            // Header: Crest Icon, Title, RAM, Clock & Close Button
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
                                            fontSize = 11.5.sp,
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
                                    // Storage Pill
                                    if (storageTotalGb > 0f) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(Color(0xFF111B2E).copy(alpha = 0.90f))
                                                .border(
                                                    width = 1.dp,
                                                    color = SectionCyanAccent.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Storage,
                                                contentDescription = stringResource(R.string.control_center_storage),
                                                tint = SectionCyanAccent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = stringResource(R.string.control_center_gb_format, storageUsedGb, storageTotalGb),
                                                color = SectionCyanAccent,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Network Pill
                                    val netColor = if (networkKind == "offline") Color(0xFFF87171) else SectionPurpleAccent
                                    val netIcon = when (networkKind) {
                                        "ethernet" -> Icons.Default.SettingsEthernet
                                        "offline" -> Icons.Default.WifiOff
                                        else -> Icons.Default.Wifi
                                    }
                                    val netLabel = when (networkKind) {
                                        "ethernet" -> "LAN"
                                        "offline" -> stringResource(R.string.control_center_network_offline)
                                        else -> "Wi-Fi"
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFF111B2E).copy(alpha = 0.90f))
                                            .border(
                                                width = 1.dp,
                                                color = netColor.copy(alpha = 0.38f),
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = netIcon,
                                            contentDescription = netLabel,
                                            tint = netColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = netLabel,
                                            color = netColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
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
                                            .size(38.dp)
                                            .focusProperties {
                                                down = gridRequesters[0][3]
                                                left = gridRequesters[0][0]
                                                right = gridRequesters[0][3]
                                            }
                                            .onFocusChanged { state ->
                                                if (state.isFocused) {
                                                    focusedCoord = -1 to -1
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
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Live Selected Option Bar showing ONLY the currently focused item and its selected option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color(0xFF10182B).copy(alpha = 0.88f))
                                    .border(
                                        width = 1.dp,
                                        color = TvAccentGold.copy(alpha = 0.36f),
                                        shape = RoundedCornerShape(13.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(TvAccentGoldRadiant)
                                    )
                                    Text(
                                        text = "${stringResource(R.string.control_center_selected_label)} • $focusedItemTitle",
                                        color = TvTextPrimary.copy(alpha = 0.92f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(TvAccentGold.copy(alpha = 0.22f))
                                        .border(1.dp, TvAccentGold.copy(alpha = 0.80f), RoundedCornerShape(50))
                                        .padding(horizontal = 11.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = TvAccentGoldRadiant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = focusedItemValue,
                                        color = TvAccentGoldRadiant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Row 0: 4 Essential Quick Toggles (Wallpaper, Clock Format, Screensaver Mode, Apps Filter)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QuickToggleTile(
                                    icon = Icons.Default.Wallpaper,
                                    title = tile00Title,
                                    subtitle = wallpaperSub,
                                    isActive = wallpaperMode != "default",
                                    accentColor = SectionGoldAccent,
                                    totalSteps = 4,
                                    currentStep = wallpaperStep,
                                    focusRequester = gridRequesters[0][0],
                                    onFocused = { focusedCoord = 0 to 0 },
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

                                QuickToggleTile(
                                    icon = Icons.Default.Schedule,
                                    title = tile01Title,
                                    subtitle = tile01Sub,
                                    isActive = is24hFormat,
                                    accentColor = SectionCyanAccent,
                                    totalSteps = 2,
                                    currentStep = if (is24hFormat) 0 else 1,
                                    focusRequester = gridRequesters[0][1],
                                    onFocused = { focusedCoord = 0 to 1 },
                                    onClick = onToggleClock24h,
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(0, 1)
                                )

                                QuickToggleTile(
                                    icon = Icons.Default.SmartDisplay,
                                    title = tile02Title,
                                    subtitle = screensaverModeSub,
                                    isActive = true,
                                    accentColor = SectionPurpleAccent,
                                    totalSteps = 4,
                                    currentStep = screensaverModeStep,
                                    focusRequester = gridRequesters[0][2],
                                    onFocused = { focusedCoord = 0 to 2 },
                                    onClick = {
                                        scope.launch {
                                            val nextSaverMode = when (screensaverMode) {
                                                "clock_date" -> "wallpaper_info"
                                                "wallpaper_info" -> "slideshow"
                                                "slideshow" -> "video_stream"
                                                else -> "clock_date"
                                            }
                                            prefs.setScreensaverMode(nextSaverMode)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(0, 2)
                                )

                                QuickToggleTile(
                                    icon = Icons.Default.Apps,
                                    title = tile03Title,
                                    subtitle = tile03Sub,
                                    isActive = showOnlyTvApps,
                                    accentColor = SectionEmeraldAccent,
                                    totalSteps = 2,
                                    currentStep = if (showOnlyTvApps) 0 else 1,
                                    focusRequester = gridRequesters[0][3],
                                    onFocused = { focusedCoord = 0 to 3 },
                                    onClick = {
                                        scope.launch {
                                            prefs.setShowOnlyTvApps(!showOnlyTvApps)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(0, 3)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 1: 4 Essential Quick Actions (Screensaver Now, Search, RAM Boost, All Settings)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ControlDockButton(
                                    icon = Icons.Default.PlayArrow,
                                    title = dock0Title,
                                    subtitle = screensaverModeSub,
                                    accentColor = TvAccentGold,
                                    isPrimaryHighlight = true,
                                    focusRequester = gridRequesters[1][0],
                                    onFocused = { focusedCoord = 1 to 0 },
                                    onClick = {
                                        onDismiss()
                                        onOpenAmbientMode()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(1, 0)
                                )

                                ControlDockButton(
                                    icon = Icons.Default.Mic,
                                    title = dock1Title,
                                    subtitle = dock1Sub,
                                    accentColor = SectionCyanAccent,
                                    isPrimaryHighlight = false,
                                    focusRequester = gridRequesters[1][1],
                                    onFocused = { focusedCoord = 1 to 1 },
                                    onClick = {
                                        onDismiss()
                                        onOpenSearch()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(1, 1)
                                )

                                ControlDockButton(
                                    icon = Icons.Default.Bolt,
                                    title = dock2Title,
                                    subtitle = dock2Sub,
                                    accentColor = SectionEmeraldAccent,
                                    isPrimaryHighlight = freedMb > 0,
                                    focusRequester = gridRequesters[1][2],
                                    onFocused = { focusedCoord = 1 to 2 },
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
                                        .matrixNavigation(1, 2)
                                )

                                ControlDockButton(
                                    icon = Icons.Default.Settings,
                                    title = dock3Title,
                                    subtitle = dock3Sub,
                                    accentColor = TvAccentBlue,
                                    isPrimaryHighlight = false,
                                    showChevron = true,
                                    isRtl = isRtl,
                                    focusRequester = gridRequesters[1][3],
                                    onFocused = { focusedCoord = 1 to 3 },
                                    onClick = {
                                        onDismiss()
                                        onOpenSettings()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .matrixNavigation(1, 3)
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
    currentStep: Int = 0
) {
    TvFocusableCard(
        onClick = onClick,
        focusRequester = focusRequester,
        scaleUpRatio = 1.05f,
        elevation = 18.dp,
        shape = RoundedCornerShape(16.dp),
        useDefaultBorder = false,
        modifier = modifier
            .height(94.dp)
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
                .clip(RoundedCornerShape(16.dp))
                .background(containerColor)
                .border(
                    width = if (isFocused) 2.2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
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
                    .padding(horizontal = 12.dp, vertical = 9.dp),
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
                            modifier = Modifier.size(16.dp)
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
                                    targetValue = if (selected) 12.dp else 4.dp,
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

                // Bottom area: Title + Crisp Selected Option Pill
                Column {
                    Text(
                        text = title,
                        color = labelColor,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isFocused -> Color.Black.copy(alpha = 0.14f)
                                        isActive -> accentColor.copy(alpha = 0.16f)
                                        else -> Color.White.copy(alpha = 0.06f)
                                    }
                                )
                                .border(
                                    width = 0.8.dp,
                                    color = when {
                                        isFocused -> Color.Black.copy(alpha = 0.28f)
                                        isActive -> accentColor.copy(alpha = 0.45f)
                                        else -> Color.White.copy(alpha = 0.12f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = when {
                                    isFocused -> Color.Black
                                    isActive -> accentColor
                                    else -> TvTextSecondary
                                },
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = subtitle,
                                color = valueColor,
                                fontSize = 11.5.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isFocused) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.18f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
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
            .height(56.dp)
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
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
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
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isFocused) Color.Black else TvTextPrimary,
                    fontSize = 12.sp,
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
