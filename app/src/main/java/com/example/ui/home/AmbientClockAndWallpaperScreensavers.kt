package com.example.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.LauncherPreferences
import com.example.ui.common.AuroraBackground
import com.example.ui.common.AuroraColors
import com.example.ui.settings.onlineWallpaperCategories
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.util.GreetingHelper
import com.example.util.LocaleHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Mode 2: Random 4K Wallpaper with sleek clock and info placed directly on the image
 * at the bottom-right corner (no black background card).
 * When 12-hour format is chosen, localized AM/PM appears beside the date at the date's font size and level.
 */
@Composable
internal fun WallpaperInfoScreensaver(
    is24hFormat: Boolean,
    auroraColors: AuroraColors,
    wallpaperMode: String,
    customWallpaperUrl: String,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { LauncherPreferences(context) }
    val lang by prefs.languageFlow.collectAsState(initial = "system")

    val allPresetWallpapers = remember {
        onlineWallpaperCategories.flatMap { it.wallpapers }
    }

    var randomWallpaperUrl by remember {
        mutableStateOf(
            if (allPresetWallpapers.isNotEmpty()) {
                allPresetWallpapers.random().fullUrl
            } else {
                "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&w=1920&q=85"
            }
        )
    }

    // Automatically rotate to a new random wallpaper every 1 hour (3,600,000 ms) while in screensaver
    LaunchedEffect(Unit) {
        while (true) {
            delay(3_600_000L)
            if (allPresetWallpapers.size > 1) {
                var next = allPresetWallpapers.random().fullUrl
                if (next == randomWallpaperUrl) {
                    next = allPresetWallpapers.random().fullUrl
                }
                randomWallpaperUrl = next
            }
        }
    }

    var timeStr by remember { mutableStateOf("") }
    var secondsStr by remember { mutableStateOf("") }
    var amPmStr by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("") }
    var greetingStr by remember { mutableStateOf("") }

    val effectiveLang = LocaleHelper.getEffectiveLanguage(lang)
    val isArabic = LocaleHelper.isArabicOrRtlLanguage(lang) ||
        LocaleHelper.containsArabicScript(dateStr) ||
        LocaleHelper.containsArabicScript(greetingStr)
    val dateFont = if (isArabic) CairoFontFamily else FontFamily.Default

    val directTextShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.85f),
            blurRadius = 12f,
            offset = Offset(0f, 3f)
        )
    }

    val greetingMode by prefs.greetingModeFlow.collectAsState(initial = "poetic")
    val customGreetingText by prefs.customGreetingTextFlow.collectAsState(initial = "")
    val dateFormatStyle by prefs.dateFormatStyleFlow.collectAsState(initial = "full")

    LaunchedEffect(is24hFormat, lang, greetingMode, customGreetingText, dateFormatStyle) {
        val pattern = if (is24hFormat) "HH:mm" else "hh:mm"
        val timeFmt = SimpleDateFormat(pattern, Locale.US)
        val secFmt = SimpleDateFormat("ss", Locale.US)
        val effectiveDateStyle = if (dateFormatStyle == "full") "full_year" else dateFormatStyle

        while (true) {
            val cal = Calendar.getInstance()
            timeStr = timeFmt.format(cal.time)
            secondsStr = secFmt.format(cal.time)
            val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
            amPmStr = if (!is24hFormat) LocaleHelper.getAmPmString(hourOfDay, lang) else ""
            dateStr = LocaleHelper.formatDate(cal.time, effectiveDateStyle, lang)
            greetingStr = GreetingHelper.getGreeting(hourOfDay, effectiveLang, greetingMode, customGreetingText)
            delay(1000)
        }
    }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        // Random 4K Wallpaper Layer
        AsyncImage(
            model = randomWallpaperUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        val wallpaperEffect by prefs.slideshowEffectFlow.collectAsState(initial = "none")
        if (wallpaperEffect != "none") {
            AtmosphericParticleOverlay(
                effect = wallpaperEffect,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Deep black corner shadow anchored flush at the active corner
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val cornerX = if (isRtl) 0f else size.width
            val cornerY = size.height
            val startCorner = Offset(x = cornerX, y = cornerY)
            val diagReach = 440.dp.toPx()

            val end45Deg = Offset(
                x = if (isRtl) cornerX + diagReach else cornerX - diagReach,
                y = cornerY - diagReach
            )

            drawRect(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Black.copy(alpha = 0.92f),
                        0.28f to Color.Black.copy(alpha = 0.72f),
                        0.55f to Color.Black.copy(alpha = 0.38f),
                        0.82f to Color.Black.copy(alpha = 0.10f),
                        1.00f to Color.Transparent
                    ),
                    start = startCorner,
                    end = end45Deg
                )
            )

            val radialRadius = maxOf(size.width * 0.56f, 520.dp.toPx())
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Black.copy(alpha = 0.96f),
                        0.28f to Color.Black.copy(alpha = 0.84f),
                        0.54f to Color.Black.copy(alpha = 0.54f),
                        0.78f to Color.Black.copy(alpha = 0.20f),
                        1.00f to Color.Transparent
                    ),
                    center = startCorner,
                    radius = radialRadius
                ),
                center = startCorner,
                radius = radialRadius
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 44.dp, bottom = 38.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (greetingStr.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = greetingStr,
                            color = TvAccentGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = dateFont,
                            style = TextStyle(
                                fontFamily = dateFont,
                                shadow = directTextShadow
                            )
                        )
                    }
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeStr,
                            color = TvTextPrimary,
                            fontSize = 62.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Default,
                            letterSpacing = (-1).sp,
                            style = TextStyle(
                                fontFeatureSettings = "tnum",
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.9f),
                                    blurRadius = 18f,
                                    offset = Offset(0f, 4f)
                                )
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = secondsStr,
                            color = TvAccentGold,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(
                                fontFeatureSettings = "tnum",
                                shadow = directTextShadow
                            ),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!is24hFormat && amPmStr.isNotBlank()) {
                        Text(
                            text = amPmStr,
                            color = TvAccentGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = dateFont,
                            style = TextStyle(
                                fontFamily = dateFont,
                                shadow = directTextShadow
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•",
                            color = TvTextPrimary.copy(alpha = 0.85f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = dateFont,
                            style = TextStyle(
                                fontFamily = dateFont,
                                shadow = directTextShadow
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = dateStr,
                        color = TvTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = dateFont,
                        style = TextStyle(
                            fontFamily = dateFont,
                            shadow = directTextShadow
                        )
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.ambient_press_any_key),
            color = TvTextPrimary.copy(alpha = 0.8f),
            fontSize = 12.sp,
            style = TextStyle(shadow = directTextShadow),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 36.dp, bottom = 36.dp)
        )
    }
}

/**
 * Mode 1 (Default): Centered large Clock, Date, Greetings & Ambient floating luminous orbs.
 * When 12-hour format is chosen, localized AM/PM appears beside the date at the date's font size and level.
 */
@Composable
internal fun ClockDateScreensaver(
    is24hFormat: Boolean,
    auroraColors: AuroraColors,
    focusRequester: FocusRequester? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { LauncherPreferences(context) }
    val lang by prefs.languageFlow.collectAsState(initial = "system")

    var timeStr by remember { mutableStateOf("") }
    var secondsStr by remember { mutableStateOf("") }
    var amPmStr by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("") }
    var greetingStr by remember { mutableStateOf("") }

    val effectiveLang = LocaleHelper.getEffectiveLanguage(lang)
    val isArabic = LocaleHelper.isArabicOrRtlLanguage(lang) ||
        LocaleHelper.containsArabicScript(dateStr) ||
        LocaleHelper.containsArabicScript(greetingStr)
    val dateFont = if (isArabic) CairoFontFamily else FontFamily.Default

    val greetingMode by prefs.greetingModeFlow.collectAsState(initial = "poetic")
    val customGreetingText by prefs.customGreetingTextFlow.collectAsState(initial = "")
    val dateFormatStyle by prefs.dateFormatStyleFlow.collectAsState(initial = "full")

    LaunchedEffect(is24hFormat, lang, greetingMode, customGreetingText, dateFormatStyle) {
        val pattern = if (is24hFormat) "HH:mm" else "hh:mm"
        val timeFmt = SimpleDateFormat(pattern, Locale.US)
        val secFmt = SimpleDateFormat("ss", Locale.US)
        val effectiveDateStyle = if (dateFormatStyle == "full") "full_year" else dateFormatStyle

        while (true) {
            val cal = Calendar.getInstance()
            timeStr = timeFmt.format(cal.time)
            secondsStr = secFmt.format(cal.time)
            val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
            amPmStr = if (!is24hFormat) LocaleHelper.getAmPmString(hourOfDay, lang) else ""
            dateStr = LocaleHelper.formatDate(cal.time, effectiveDateStyle, lang)
            greetingStr = GreetingHelper.getGreeting(hourOfDay, effectiveLang, greetingMode, customGreetingText)
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_motion")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_phase"
    )

    val boxModifier = if (focusRequester != null) {
        Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    onDismiss()
                    true
                } else false
            }
    } else {
        Modifier.fillMaxSize()
    }

    Box(
        modifier = boxModifier
    ) {
        AuroraBackground(colors = auroraColors)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx1 = w * (0.3f + 0.15f * sin(phase))
            val cy1 = h * (0.35f + 0.12f * cos(phase))
            val cx2 = w * (0.7f + 0.14f * cos(phase * 0.8f))
            val cy2 = h * (0.65f + 0.12f * sin(phase * 0.8f))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auroraColors.glow.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(cx1, cy1),
                    radius = w * 0.32f
                ),
                center = Offset(cx1, cy1),
                radius = w * 0.32f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TvAccentGold.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(cx2, cy2),
                    radius = w * 0.28f
                ),
                center = Offset(cx2, cy2),
                radius = w * 0.28f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.68f)
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            if (greetingStr.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, TvAccentGold.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = TvAccentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = greetingStr,
                        color = TvAccentGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = dateFont
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeStr,
                        color = TvTextPrimary,
                        fontSize = 112.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Default,
                        letterSpacing = (-2).sp,
                        style = TextStyle(
                            fontFeatureSettings = "tnum",
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.75f),
                                blurRadius = 24f,
                                offset = Offset(0f, 6f)
                            )
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = secondsStr,
                        color = TvAccentGold,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        modifier = Modifier.padding(bottom = 18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (!is24hFormat && amPmStr.isNotBlank()) {
                    Text(
                        text = amPmStr,
                        color = TvAccentGold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = dateFont
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = TvTextSecondary.copy(alpha = 0.7f),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = dateFont
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = dateStr,
                    color = TvTextPrimary.copy(alpha = 0.88f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = dateFont
                )
            }
        }

        Text(
            text = stringResource(R.string.ambient_press_any_key),
            color = TvTextSecondary.copy(alpha = 0.7f),
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
