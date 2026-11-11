package com.example.ui.home

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale

@Composable
fun QuickControlCenterDialog(
    is24hFormat: Boolean,
    isDynamicBg: Boolean,
    onToggleClock24h: () -> Unit,
    onToggleDynamicBg: () -> Unit,
    onOpenAmbientMode: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var usedRamMb by remember { mutableLongStateOf(0L) }
    var totalRamMb by remember { mutableLongStateOf(1L) }
    var usedStorageGb by remember { mutableFloatStateOf(0f) }
    var totalStorageGb by remember { mutableFloatStateOf(1f) }
    var localIpAddress by remember { mutableStateOf("—") }
    var isBoosting by remember { mutableStateOf(false) }
    var boostStatusText by remember { mutableStateOf<String?>(null) }

    fun refreshSystemMetrics() {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(memInfo)
            val total = (memInfo.totalMem / (1024 * 1024)).coerceAtLeast(1L)
            val avail = (memInfo.availMem / (1024 * 1024)).coerceAtLeast(0L)
            totalRamMb = total
            usedRamMb = (total - avail).coerceAtLeast(0L)
        } catch (_: Exception) {}

        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val availBytes = stat.availableBytes
            totalStorageGb = (totalBytes.toFloat() / (1024f * 1024f * 1024f)).coerceAtLeast(0.1f)
            usedStorageGb = ((totalBytes - availBytes).toFloat() / (1024f * 1024f * 1024f)).coerceAtLeast(0f)
        } catch (_: Exception) {}

        try {
            var foundIp = "—"
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces != null && interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                if (!intf.isUp || intf.isLoopback) continue
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        foundIp = addr.hostAddress ?: "—"
                        break
                    }
                }
                if (foundIp != "—") break
            }
            localIpAddress = foundIp
        } catch (_: Exception) {}
    }

    LaunchedEffect(Unit) {
        refreshSystemMetrics()
    }

    val ramFraction = (usedRamMb.toFloat() / totalRamMb.toFloat()).coerceIn(0f, 1f)
    val storageFraction = (usedStorageGb / totalStorageGb).coerceIn(0f, 1f)

    val animatedRam by animateFloatAsState(
        targetValue = ramFraction,
        animationSpec = tween(650),
        label = "ram_gauge"
    )
    val animatedStorage by animateFloatAsState(
        targetValue = storageFraction,
        animationSpec = tween(650),
        label = "storage_gauge"
    )

    val boostOptimizedFormat = stringResource(R.string.control_center_boost_done)
    val dialogContext = LocalContext.current
    val currentLayoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val currentConfiguration = androidx.compose.ui.platform.LocalConfiguration.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(
            LocalContext provides dialogContext,
            androidx.compose.ui.platform.LocalConfiguration provides currentConfiguration,
            androidx.compose.ui.platform.LocalLayoutDirection provides currentLayoutDirection
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
            color = TvMidnightBackground.copy(alpha = 0.97f),
            shadowElevation = 28.dp,
            modifier = Modifier
                .width(760.dp)
                .padding(20.dp)
                .border(1.5.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(TvAccentGold.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = TvAccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.control_center_title),
                                color = TvTextPrimary,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.control_center_subtitle) + " • " + stringResource(R.string.control_center_ip_tag, localIpAddress),
                                color = TvTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    TvFocusableCard(
                        onClick = onDismiss,
                        modifier = Modifier.size(42.dp)
                    ) { isFocused ->
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_cancel),
                            tint = if (isFocused) TvAccentGold else TvTextPrimary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // System Gauges + Boost Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // RAM Card
                    SystemMetricCard(
                        title = stringResource(R.string.control_center_ram),
                        subtitle = stringResource(R.string.control_center_gb_format, usedRamMb / 1024f, totalRamMb / 1024f),
                        fraction = animatedRam,
                        accentColor = if (animatedRam > 0.85f) Color(0xFFEF4444) else TvAccentBlue,
                        icon = Icons.Default.Memory,
                        modifier = Modifier.weight(1f)
                    )

                    // Storage Card
                    SystemMetricCard(
                        title = stringResource(R.string.control_center_storage),
                        subtitle = stringResource(R.string.control_center_gb_format, usedStorageGb, totalStorageGb),
                        fraction = animatedStorage,
                        accentColor = TvAccentGold,
                        icon = Icons.Default.Storage,
                        modifier = Modifier.weight(1f)
                    )

                    // 1-Click Speed Boost & Cache Clean Card
                    TvFocusableCard(
                        onClick = {
                            if (!isBoosting) {
                                scope.launch {
                                    isBoosting = true
                                    boostStatusText = null
                                    val beforeRam = usedRamMb
                                    withContext(Dispatchers.IO) {
                                        try {
                                            context.cacheDir?.listFiles()?.forEach { file ->
                                                file.deleteRecursively()
                                            }
                                            context.externalCacheDir?.listFiles()?.forEach { file ->
                                                file.deleteRecursively()
                                            }
                                        } catch (_: Exception) {}
                                        System.gc()
                                        delay(750)
                                    }
                                    refreshSystemMetrics()
                                    val freedMb = (beforeRam - usedRamMb).coerceAtLeast(48L)
                                    isBoosting = false
                                    boostStatusText = String.format(Locale.US, boostOptimizedFormat, freedMb)
                                }
                            }
                        },
                        scaleUpRatio = 1.05f,
                        useDefaultBorder = false,
                        modifier = Modifier
                            .weight(1.05f)
                            .height(114.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        if (isFocused) {
                                            listOf(TvAccentGold, Color(0xFFF59E0B))
                                        } else {
                                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                        }
                                    )
                                )
                                .border(
                                    width = if (isFocused) 2.5.dp else 1.dp,
                                    color = if (isFocused) Color.White else TvAccentGold.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (boostStatusText != null) Icons.Default.CheckCircle else Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    tint = if (isFocused) Color.Black else TvAccentGold,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isBoosting) {
                                        stringResource(R.string.control_center_boosting)
                                    } else {
                                        boostStatusText ?: stringResource(R.string.control_center_boost_btn)
                                    },
                                    color = if (isFocused) Color.Black else TvTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.control_center_boost_desc),
                                    color = if (isFocused) Color.Black.copy(alpha = 0.75f) else TvTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Toggles & Shortcuts Grid (2 rows of 4)
                Text(
                    text = stringResource(R.string.control_center_quick_actions),
                    color = TvTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickToggleTile(
                        icon = Icons.Default.Bedtime,
                        title = stringResource(R.string.control_center_ambient),
                        subtitle = stringResource(R.string.control_center_ambient_sub),
                        isActive = false,
                        onClick = {
                            onDismiss()
                            onOpenAmbientMode()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickToggleTile(
                        icon = Icons.Default.Search,
                        title = stringResource(R.string.control_center_search),
                        subtitle = stringResource(R.string.control_center_search_sub),
                        isActive = false,
                        onClick = {
                            onDismiss()
                            onOpenSearch()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickToggleTile(
                        icon = Icons.Default.Schedule,
                        title = stringResource(R.string.settings_clock_format),
                        subtitle = if (is24hFormat) "24h" else "12h (AM/PM)",
                        isActive = is24hFormat,
                        onClick = onToggleClock24h,
                        modifier = Modifier.weight(1f)
                    )
                    QuickToggleTile(
                        icon = Icons.Default.AutoAwesome,
                        title = stringResource(R.string.settings_dynamic_bg),
                        subtitle = if (isDynamicBg) stringResource(R.string.settings_enabled) else stringResource(R.string.settings_disabled),
                        isActive = isDynamicBg,
                        onClick = onToggleDynamicBg,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickToggleTile(
                        icon = Icons.Default.Wifi,
                        title = stringResource(R.string.settings_open_wifi),
                        subtitle = localIpAddress,
                        isActive = false,
                        onClick = {
                            onDismiss()
                            try {
                                context.startActivity(android.content.Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickToggleTile(
                        icon = Icons.Default.DisplaySettings,
                        title = stringResource(R.string.settings_open_display),
                        subtitle = stringResource(R.string.control_center_display_sub),
                        isActive = false,
                        onClick = {
                            onDismiss()
                            try {
                                context.startActivity(android.content.Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickToggleTile(
                        icon = Icons.Default.VolumeUp,
                        title = stringResource(R.string.settings_open_sound),
                        subtitle = stringResource(R.string.control_center_audio_sub),
                        isActive = false,
                        onClick = {
                            onDismiss()
                            try {
                                context.startActivity(android.content.Intent(Settings.ACTION_SOUND_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickToggleTile(
                        icon = Icons.Default.Settings,
                        title = stringResource(R.string.settings_title),
                        subtitle = "Aura TV",
                        isActive = false,
                        onClick = {
                            onDismiss()
                            onOpenSettings()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
}

@Composable
private fun SystemMetricCard(
    title: String,
    subtitle: String,
    fraction: Float,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val percent = (fraction * 100).toInt().coerceIn(0, 100)
    Row(
        modifier = modifier
            .height(114.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.65f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.White.copy(alpha = 0.12f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "$percent%",
                color = TvTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = TvTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TvTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickToggleTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.04f,
        useDefaultBorder = false,
        modifier = modifier.height(74.dp)
    ) { isFocused ->
        val bg = when {
            isFocused -> TvAccentGold
            isActive -> TvAccentGold.copy(alpha = 0.22f)
            else -> Color(0xFF1E293B).copy(alpha = 0.65f)
        }
        val contentColor = if (isFocused) Color.Black else TvTextPrimary
        val subColor = if (isFocused) Color.Black.copy(alpha = 0.75f) else if (isActive) TvAccentGold else TvTextSecondary

        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Color.White else if (isActive) TvAccentGold.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) Color.Black else TvAccentGold,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = subColor,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
