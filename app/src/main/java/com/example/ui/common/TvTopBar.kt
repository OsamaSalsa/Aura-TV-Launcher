package com.example.ui.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NetworkType {
    WIFI, WIFI_NO_INTERNET, ETHERNET, DISCONNECTED
}

@Composable
fun TvTopBar(
    is24hFormat: Boolean,
    showSeconds: Boolean = false,
    clockSize: String = "large",
    dateFormatStyle: String = "full",
    greetingMode: String = "poetic",
    customGreetingText: String = "",
    onOpenSettings: () -> Unit,
    onOpenIptv: () -> Unit,
    onOpenYouTube: () -> Unit,
    onOpenSearch: (() -> Unit)? = null,
    onOpenControlCenter: (() -> Unit)? = null,
    isScrolled: Boolean = false,
    isEditingMode: Boolean = false,
    onToggleEditMode: (() -> Unit)? = null,
    onNonAppFocused: (() -> Unit)? = null,
    onRequestFocusDown: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    glassOpacity: Int = 75,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTimeString by remember { mutableStateOf("") }
    var currentAmPmString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }
    var currentGreeting by remember { mutableStateOf("") }
    var networkType by remember { mutableStateOf(NetworkType.DISCONNECTED) }
    var networkName by remember { mutableStateOf("") }
    
    val prefs = remember { com.example.data.local.LauncherPreferences(context) }
    val lang by prefs.languageFlow.collectAsState(initial = "system")

    // Live network callback
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        
        fun updateNetworkStatus() {
            val active = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(active)
            
            val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                             caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

            networkType = when {
                caps == null -> NetworkType.DISCONNECTED
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    if (hasInternet) NetworkType.WIFI else NetworkType.WIFI_NO_INTERNET
                }
                else -> NetworkType.DISCONNECTED
            }

            // Try to get SSID or connection type name
            networkName = when (networkType) {
                NetworkType.WIFI, NetworkType.WIFI_NO_INTERNET -> {
                    "Wi-Fi"
                }
                NetworkType.ETHERNET -> "Ethernet"
                NetworkType.DISCONNECTED -> "Offline"
            }
        }

        updateNetworkStatus()
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(n: android.net.Network) { updateNetworkStatus() }
            override fun onLost(n: android.net.Network) { updateNetworkStatus() }
            override fun onCapabilitiesChanged(n: android.net.Network, nc: NetworkCapabilities) { updateNetworkStatus() }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()
            
        try { cm?.registerNetworkCallback(request, callback) } catch (_: Exception) {}
        onDispose { try { cm?.unregisterNetworkCallback(callback) } catch (_: Exception) {} }
    }
    
    // Live clock and greeting update
    LaunchedEffect(is24hFormat, showSeconds, dateFormatStyle, greetingMode, customGreetingText, lang) {
        val timePattern = when {
            is24hFormat && showSeconds -> "HH:mm:ss"
            is24hFormat -> "HH:mm"
            showSeconds -> "hh:mm:ss"
            else -> "hh:mm"
        }
        val timeFormat = SimpleDateFormat(timePattern, Locale.US)
        
        val isArabicLang = lang == "ar" || lang == "ar_rtl" || (lang == "system" && Locale.getDefault().language == "ar")
        val dateLocale = if (isArabicLang) Locale("ar") else Locale.getDefault()
        val dateFormat = when (dateFormatStyle) {
            "medium" -> SimpleDateFormat("d MMMM", dateLocale)
            "numeric" -> SimpleDateFormat("dd/MM/yyyy", dateLocale)
            "hidden" -> null
            else -> SimpleDateFormat("EEEE, d MMMM", dateLocale)
        }
        
        val effectiveLang = if (lang == "system") Locale.getDefault().language else lang

        while (true) {
            val now = java.util.Calendar.getInstance()
            val rawTime = timeFormat.format(now.time)
            // Explicitly force English digits (0-9) to prevent any system-level override
            currentTimeString = rawTime.map { ch ->
                val digit = Character.getNumericValue(ch)
                if (digit in 0..9 && ch !in '0'..'9' && ch !in 'a'..'z' && ch !in 'A'..'Z') {
                    digit.toString()[0]
                } else ch
            }.joinToString("")

            val hourOfDay = now.get(java.util.Calendar.HOUR_OF_DAY)
            currentAmPmString = if (!is24hFormat) {
                com.example.util.LocaleHelper.getAmPmString(hourOfDay, lang)
            } else {
                ""
            }
            
            currentDateString = dateFormat?.format(now.time) ?: ""
            currentGreeting = com.example.util.GreetingHelper.getGreeting(hourOfDay, effectiveLang, greetingMode, customGreetingText)
            
            delay(if (showSeconds) 500L else 1000L)
        }
    }

    val topBarBgAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isScrolled) 0.95f else 0.0f,
        animationSpec = androidx.compose.animation.core.tween(250),
        label = "top_bar_bg_alpha"
    )

    val baseTimeSize = when (clockSize) {
        "compact" -> 36f
        "medium" -> 48f
        else -> 60f
    }

    val timeFontSize by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isScrolled) 22f else baseTimeSize,
        animationSpec = androidx.compose.animation.core.tween(250),
        label = "time_font_size"
    )

    val dateFontSize by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isScrolled) 13f else 18f,
        animationSpec = androidx.compose.animation.core.tween(250),
        label = "date_font_size"
    )

    val baselinePadding by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isScrolled) 2.dp else 8.dp,
        animationSpec = androidx.compose.animation.core.tween(250),
        label = "baseline_padding"
    )

    val topTextShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.70f),
            blurRadius = 8f,
            offset = Offset(0f, 2f)
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090D1A).copy(alpha = if (isScrolled) 0.98f else 0.70f),
                        Color(0xFF090D1A).copy(alpha = if (isScrolled) topBarBgAlpha * 0.90f else 0.35f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 48.dp)
            .padding(top = if (isScrolled) 12.dp else 24.dp, bottom = if (isScrolled) 16.dp else 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Greeting at top in normal state; Time, Date, and Greeting beside Date when scrolled
        val isArabic = lang == "ar" || lang == "ar_rtl"
        val arabicFont = if (isArabic) CairoFontFamily else FontFamily.Default

        val hasDate = dateFormatStyle != "hidden" && currentDateString.isNotBlank()
        val hasGreeting = greetingMode != "hidden" && currentGreeting.isNotBlank()
        val hasAmPm = !is24hFormat && currentAmPmString.isNotBlank()

        Column {
            // Normal state: Greeting is at its original position above the Time and Date
            if (!isScrolled && hasGreeting) {
                Text(
                    text = currentGreeting,
                    color = TvAccentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = arabicFont,
                    letterSpacing = if (isArabic) 0.sp else 1.2.sp,
                    style = TextStyle(
                        fontFamily = arabicFont,
                        shadow = topTextShadow
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(top = if (isScrolled) 0.dp else 4.dp)
            ) {
                // Time: Force English (ASCII) Digits and Original System Font
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = currentTimeString,
                        color = TvTextPrimary,
                        fontSize = timeFontSize.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Default,
                        letterSpacing = (-1.0).sp,
                        maxLines = 1,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            shadow = topTextShadow
                        )
                    )
                }
                
                if (hasDate || hasAmPm) {
                    Spacer(modifier = Modifier.width(if (isScrolled) 12.dp else 18.dp))
                    
                    // Date and AM/PM: aligned on the same baseline, at date font size
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(bottom = baselinePadding)
                    ) {
                        if (hasAmPm) {
                            Text(
                                text = currentAmPmString,
                                color = TvAccentGold,
                                fontSize = dateFontSize.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = arabicFont,
                                maxLines = 1,
                                style = TextStyle(
                                    fontFamily = arabicFont,
                                    shadow = topTextShadow
                                )
                            )
                        }

                        if (hasAmPm && hasDate) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•",
                                color = TvTextSecondary.copy(alpha = 0.6f),
                                fontSize = dateFontSize.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = arabicFont,
                                style = TextStyle(
                                    fontFamily = arabicFont,
                                    shadow = topTextShadow
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        if (hasDate) {
                            // Date: Cairo Font in Arabic, shrinks dynamically on scroll
                            Text(
                                text = currentDateString,
                                color = TvTextPrimary.copy(alpha = 0.85f),
                                fontSize = dateFontSize.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = arabicFont,
                                maxLines = 1,
                                style = TextStyle(
                                    fontFamily = arabicFont,
                                    shadow = topTextShadow
                                )
                            )
                        }
                    }
                }

                // Scrolled state (وهو صغار): Greeting appears beside/after the date in the same line
                if (isScrolled && hasGreeting) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = currentGreeting,
                        color = TvAccentGold,
                        fontSize = dateFontSize.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = arabicFont,
                        maxLines = 1,
                        style = TextStyle(
                            fontFamily = arabicFont,
                            shadow = topTextShadow
                        ),
                        modifier = Modifier.padding(bottom = baselinePadding)
                    )
                }
            }
        }

        // Right: Action Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (onOpenSearch != null) {
                TvActionButton(
                    icon = Icons.Default.Search,
                    text = "",
                    onClick = onOpenSearch,
                    onFocused = onNonAppFocused,
                    onRequestFocusDown = onRequestFocusDown,
                    glassOpacity = glassOpacity,
                    showText = false
                )
            }

            if (onOpenControlCenter != null) {
                TvActionButton(
                    icon = Icons.Default.RocketLaunch,
                    text = "",
                    onClick = onOpenControlCenter,
                    onFocused = onNonAppFocused,
                    onRequestFocusDown = onRequestFocusDown,
                    glassOpacity = glassOpacity,
                    showText = false
                )
            }

            val netInfo = when (networkType) {
                NetworkType.WIFI -> Icons.Default.Wifi to stringResource(R.string.network_connected_wifi)
                NetworkType.WIFI_NO_INTERNET -> Icons.Default.WifiOff to stringResource(R.string.network_disconnected)
                NetworkType.ETHERNET -> Icons.Default.SettingsEthernet to stringResource(R.string.network_connected_ethernet)
                NetworkType.DISCONNECTED -> Icons.Default.WifiOff to stringResource(R.string.network_disconnected)
            }
            
            TvActionButton(
                icon = netInfo.first, 
                text = "", 
                onClick = {
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                }, 
                onFocused = onNonAppFocused,
                onRequestFocusDown = onRequestFocusDown,
                glassOpacity = glassOpacity,
                showText = false
            )
            
            TvActionButton(
                icon = Icons.Default.LiveTv, 
                text = "", 
                onClick = onOpenIptv, 
                onLongClick = onOpenSettings, // Long click on TV shows settings/menu
                onFocused = onNonAppFocused,
                onRequestFocusDown = onRequestFocusDown,
                glassOpacity = glassOpacity,
                showText = false
            )
            
            // Edit Button in Top Bar
            if (onToggleEditMode != null) {
                TvActionButton(
                    icon = if (isEditingMode) Icons.Default.Check else Icons.Default.Edit,
                    text = "",
                    onClick = onToggleEditMode,
                    onFocused = onNonAppFocused,
                    onRequestFocusDown = onRequestFocusDown,
                    glassOpacity = glassOpacity,
                    showText = false
                )
            }

            // Settings Button: ONLY Icon (no text label "Settings")
            TvActionButton(
                icon = Icons.Default.Settings, 
                text = "", 
                onClick = onOpenSettings, 
                onFocused = onNonAppFocused,
                onRequestFocusDown = onRequestFocusDown,
                focusRequester = focusRequester,
                glassOpacity = glassOpacity,
                showText = false
            )
        }
    }
}

@Composable
fun TvActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onFocused: (() -> Unit)? = null,
    onRequestFocusDown: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    glassOpacity: Int = 75,
    showText: Boolean = true
) {
    TvFocusableCard(
        onClick = onClick,
        onLongClick = onLongClick ?: {},
        onFocused = onFocused ?: {},
        focusRequester = focusRequester ?: remember { FocusRequester() },
        scaleUpRatio = 1.05f,
        useDefaultBorder = false,
        elevation = 0.dp,
        modifier = Modifier
            .height(44.dp)
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                    if (keyEvent.key == androidx.compose.ui.input.key.Key.DirectionDown || 
                        native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN
                    ) {
                        if (onRequestFocusDown != null) {
                            onRequestFocusDown()
                            true
                        } else false
                    } else false
                } else false
            }
    ) { isFocused ->
        val pillBg = if (isFocused) {
            Color.White
        } else {
            TvMidnightBackground.copy(alpha = 0.65f)
        }
        val contentColor = if (isFocused) {
            Color(0xFF090D1A)
        } else {
            Color.White
        }
        val borderColor = if (isFocused) {
            TvAccentGold
        } else {
            Color.White.copy(alpha = 0.22f)
        }

        Row(
            modifier = Modifier
                .background(pillBg, RoundedCornerShape(22.dp))
                .border(if (isFocused) 2.dp else 1.dp, borderColor, RoundedCornerShape(22.dp))
                .padding(horizontal = if (showText && text.isNotBlank()) 16.dp else 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            if (showText && text.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = contentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
