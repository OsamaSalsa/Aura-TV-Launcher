package com.example.ui.home

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.LauncherPreferences
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.ui.common.TvFocusableCard
import com.example.ui.settings.TvActionButton
import com.example.ui.settings.onlineWallpaperCategories
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.util.GreetingHelper
import com.example.util.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "bmp", "heic", "avif")

fun loadSlideshowFolderImages(context: Context, folderUriOrPath: String): List<String> {
    val clean = folderUriOrPath.trim()
    if (clean.isBlank()) return emptyList()

    // Case 1: SAF Document Tree URI (content://...)
    if (clean.startsWith("content://")) {
        return try {
            val treeUri = Uri.parse(clean)
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
            val result = mutableListOf<String>()
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            )
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val mimeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val childId = if (idIdx >= 0) cursor.getString(idIdx) else continue
                    val mime = if (mimeIdx >= 0) cursor.getString(mimeIdx) ?: "" else ""
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx)?.lowercase() ?: "" else ""
                    val ext = name.substringAfterLast('.', "")
                    if (mime.startsWith("image/") || ext in IMAGE_EXTENSIONS) {
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId)
                        result.add(docUri.toString())
                    }
                }
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Case 2: Local filesystem directory path (/storage/... or file://...)
    return try {
        val path = if (clean.startsWith("file://")) Uri.parse(clean).path ?: clean else clean
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        dir.listFiles()
            ?.filter { file ->
                file.isFile && file.extension.lowercase() in IMAGE_EXTENSIONS
            }
            ?.sortedBy { it.name.lowercase() }
            ?.map { Uri.fromFile(it).toString() }
            ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

fun formatSlideshowFolderDisplay(folderUriOrPath: String): String {
    val clean = folderUriOrPath.trim()
    if (clean.isBlank()) return ""
    return try {
        if (clean.startsWith("content://")) {
            val decoded = URLDecoder.decode(clean, "UTF-8")
            decoded.substringAfterLast("tree/").substringAfterLast(":").ifBlank { decoded.takeLast(32) }
        } else {
            File(clean.removePrefix("file://")).name.ifBlank { clean }
        }
    } catch (_: Exception) {
        clean.takeLast(28)
    }
}

data class CandidateImageFolder(
    val name: String,
    val path: String,
    val imageCount: Int
)

fun scanCommonDeviceImageFolders(): List<CandidateImageFolder> {
    val roots = mutableListOf<File>()
    try {
        val extRoot = Environment.getExternalStorageDirectory()
        if (extRoot != null && extRoot.exists()) {
            roots.add(File(extRoot, "Pictures"))
            roots.add(File(extRoot, "DCIM"))
            roots.add(File(extRoot, "DCIM/Camera"))
            roots.add(File(extRoot, "Download"))
            roots.add(File(extRoot, "Movies"))
            roots.add(File(extRoot, "Wallpapers"))
        }
        val storageDir = File("/storage")
        storageDir.listFiles()?.forEach { vol ->
            if (vol.isDirectory && vol.name != "self" && vol.name != "emulated") {
                roots.add(vol)
                roots.add(File(vol, "Pictures"))
                roots.add(File(vol, "DCIM"))
                roots.add(File(vol, "Photos"))
            }
        }
    } catch (_: Exception) {}

    return roots.distinctBy { it.absolutePath }.mapNotNull { dir ->
        try {
            if (dir.exists() && dir.isDirectory) {
                val count = dir.listFiles()?.count { f ->
                    f.isFile && f.extension.lowercase() in IMAGE_EXTENSIONS
                } ?: 0
                CandidateImageFolder(
                    name = dir.name.ifBlank { dir.absolutePath },
                    path = dir.absolutePath,
                    imageCount = count
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}

private val AMBIENT_MUSIC_STREAMS = mapOf(
    "lofi_chill" to "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3",
    "nature_rain" to "https://cdn.pixabay.com/download/audio/2022/03/10/audio_5b3650f688.mp3",
    "ambient_space" to "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3",
    "oriental_cafe" to "https://cdn.pixabay.com/download/audio/2022/10/25/audio_946bc6eb4c.mp3"
)

@OptIn(UnstableApi::class)
@Composable
fun SlideshowScreensaver(
    is24hFormat: Boolean,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { LauncherPreferences(context) }

    val lang by prefs.languageFlow.collectAsState(initial = "system")
    val greetingMode by prefs.greetingModeFlow.collectAsState(initial = "poetic")
    val customGreetingText by prefs.customGreetingTextFlow.collectAsState(initial = "")
    val dateFormatStyle by prefs.dateFormatStyleFlow.collectAsState(initial = "full")

    val slideshowSource by prefs.slideshowSourceFlow.collectAsState(initial = "online_all")
    val slideshowFolderUri by prefs.slideshowFolderUriFlow.collectAsState(initial = "")
    val slideshowCustomPhotos by prefs.slideshowCustomPhotosFlow.collectAsState(initial = emptySet())
    val slideshowIntervalSec by prefs.slideshowIntervalSecFlow.collectAsState(initial = 10)
    val slideshowMotion by prefs.slideshowMotionFlow.collectAsState(initial = "ken_burns")
    val slideshowTransition by prefs.slideshowTransitionFlow.collectAsState(initial = "crossfade")
    val slideshowEffect by prefs.slideshowEffectFlow.collectAsState(initial = "snow")
    val slideshowClockMode by prefs.slideshowClockModeFlow.collectAsState(initial = "corner_full")
    val slideshowMusicMode by prefs.slideshowMusicModeFlow.collectAsState(initial = "off")
    val slideshowCustomMusicUri by prefs.slideshowCustomMusicUriFlow.collectAsState(initial = "")
    val slideshowMusicVolume by prefs.slideshowMusicVolumeFlow.collectAsState(initial = 50)
    val slideshowShuffle by prefs.slideshowShuffleFlow.collectAsState(initial = true)
    val slideshowDimLevel by prefs.slideshowDimLevelFlow.collectAsState(initial = 20)

    var photoList by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }

    // Load images based on selected source
    LaunchedEffect(slideshowSource, slideshowFolderUri, slideshowCustomPhotos, slideshowShuffle) {
        val loaded = withContext(Dispatchers.IO) {
            val rawList = when (slideshowSource) {
                "folder" -> loadSlideshowFolderImages(context, slideshowFolderUri)
                "custom_photos" -> slideshowCustomPhotos.toList()
                "online_nature" -> onlineWallpaperCategories
                    .filter { it.id == "nature" || it.id == "ocean" || it.id == "animals" }
                    .flatMap { it.wallpapers }.map { it.fullUrl }
                "online_space" -> onlineWallpaperCategories
                    .filter { it.id == "space" || it.id == "oled" }
                    .flatMap { it.wallpapers }.map { it.fullUrl }
                "online_cities" -> onlineWallpaperCategories
                    .filter { it.id == "cities" || it.id == "travel" || it.id == "architecture" }
                    .flatMap { it.wallpapers }.map { it.fullUrl }
                else -> onlineWallpaperCategories.flatMap { it.wallpapers }.map { it.fullUrl }
            }
            val nonEmpty = rawList.ifEmpty {
                onlineWallpaperCategories.flatMap { it.wallpapers }.map { it.fullUrl }
            }
            if (slideshowShuffle) nonEmpty.shuffled() else nonEmpty
        }
        photoList = loaded
        currentIndex = 0
    }

    // Advance slide automatically
    LaunchedEffect(photoList, slideshowIntervalSec, currentIndex) {
        if (photoList.size > 1) {
            delay(slideshowIntervalSec.coerceAtLeast(3) * 1000L)
            currentIndex = (currentIndex + 1) % photoList.size
        }
    }

    // Background Music Player (ExoPlayer)
    val resolvedMusicUrl = remember(slideshowMusicMode, slideshowCustomMusicUri) {
        when (slideshowMusicMode) {
            "off" -> ""
            "custom_file" -> slideshowCustomMusicUri.trim()
            else -> AMBIENT_MUSIC_STREAMS[slideshowMusicMode] ?: ""
        }
    }

    val musicPlayer = remember(context) {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                false
            )
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
            }
    }

    LaunchedEffect(resolvedMusicUrl) {
        try {
            if (resolvedMusicUrl.isNotBlank()) {
                musicPlayer.setMediaItem(MediaItem.fromUri(resolvedMusicUrl))
                musicPlayer.prepare()
                musicPlayer.playWhenReady = true
            } else {
                musicPlayer.stop()
                musicPlayer.clearMediaItems()
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(slideshowMusicVolume) {
        try {
            musicPlayer.volume = (slideshowMusicVolume.coerceIn(0, 100) / 100f)
        } catch (_: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                musicPlayer.stop()
                musicPlayer.release()
            } catch (_: Exception) {}
        }
    }

    // Clock & Date updater
    var timeStr by remember { mutableStateOf("") }
    var secondsStr by remember { mutableStateOf("") }
    var amPmStr by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("") }
    var greetingStr by remember { mutableStateOf("") }

    val effectiveLang = LocaleHelper.getEffectiveLanguage(lang)
    val isArabic = effectiveLang == "ar" || effectiveLang == "ur" || effectiveLang == "fa"
    val dateFont = if (isArabic) CairoFontFamily else FontFamily.Default

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
            delay(1000L)
        }
    }

    // Continuous Ken Burns / Gentle Zoom motion animation
    val motionTransition = rememberInfiniteTransition(label = "slideshow_motion")
    val motionProgress by motionTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (slideshowIntervalSec.coerceIn(5, 60) * 1000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ken_burns_progress"
    )

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val directTextShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.85f),
            blurRadius = 12f,
            offset = Offset(0f, 3f)
        )
    }

    val currentImageUrl = photoList.getOrNull(currentIndex) ?: ""

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionRight -> {
                            if (photoList.size > 1) {
                                currentIndex = (currentIndex + 1) % photoList.size
                            }
                            true
                        }
                        Key.DirectionLeft -> {
                            if (photoList.size > 1) {
                                currentIndex = (currentIndex - 1 + photoList.size) % photoList.size
                            }
                            true
                        }
                        else -> {
                            onDismiss()
                            true
                        }
                    }
                } else false
            }
    ) {
        // 1. Animated Photo Layer with Entry Transitions + Ken Burns Motion
        if (currentImageUrl.isNotBlank()) {
            AnimatedContent(
                targetState = currentIndex to currentImageUrl,
                transitionSpec = {
                    buildSlideTransition(slideshowTransition, targetState.first)
                },
                label = "slideshow_photo_transition",
                modifier = Modifier.fillMaxSize()
            ) { (slideIdx, imageUrl) ->
                val directionSign = if (slideIdx % 2 == 0) 1f else -1f
                val scale = when (slideshowMotion) {
                    "ken_burns" -> 1.03f + (motionProgress * 0.11f)
                    "gentle_zoom" -> 1.0f + (motionProgress * 0.09f)
                    else -> 1.0f
                }
                val transX = when (slideshowMotion) {
                    "ken_burns" -> directionSign * (motionProgress - 0.5f) * 38f
                    else -> 0f
                }
                val transY = when (slideshowMotion) {
                    "ken_burns" -> -directionSign * (motionProgress - 0.5f) * 22f
                    else -> 0f
                }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = transX
                            translationY = transY
                        }
                )
            }
        }

        // 2. Subtle Dimming Layer
        if (slideshowDimLevel > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = (slideshowDimLevel / 100f).coerceIn(0f, 0.75f)))
            )
        }

        // 3. Atmospheric Particle Effects Layer (Snow, Fireflies, Rain, Autumn, Sakura, Stars, Bokeh)
        if (slideshowEffect != "none") {
            AtmosphericParticleOverlay(
                effect = slideshowEffect,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 4. Corner Shadow & Clock/Greeting Overlay (matching WallpaperInfoScreensaver)
        if (slideshowClockMode != "hidden") {
            Canvas(modifier = Modifier.fillMaxSize()) {
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
                            0.00f to Color.Black.copy(alpha = 0.90f),
                            0.28f to Color.Black.copy(alpha = 0.68f),
                            0.55f to Color.Black.copy(alpha = 0.34f),
                            0.82f to Color.Black.copy(alpha = 0.08f),
                            1.00f to Color.Transparent
                        ),
                        start = startCorner,
                        end = end45Deg
                    )
                )

                val radialRadius = maxOf(size.width * 0.54f, 500.dp.toPx())
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(alpha = 0.94f),
                            0.28f to Color.Black.copy(alpha = 0.80f),
                            0.54f to Color.Black.copy(alpha = 0.48f),
                            0.78f to Color.Black.copy(alpha = 0.16f),
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
                Column(horizontalAlignment = Alignment.End) {
                    if (slideshowClockMode == "corner_full" && greetingStr.isNotBlank()) {
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
                                fontSize = if (slideshowClockMode == "minimal") 48.sp else 62.sp,
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
                                fontSize = if (slideshowClockMode == "minimal") 17.sp else 21.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    fontFeatureSettings = "tnum",
                                    shadow = directTextShadow
                                ),
                                modifier = Modifier.padding(bottom = if (slideshowClockMode == "minimal") 7.dp else 10.dp)
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
                            if (dateStr.isNotBlank()) {
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
                        }
                        if (dateStr.isNotBlank()) {
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
            }
        }

        // 5. Subtle Bottom-Start Info Badge (Slide counter + Music indicator + Exit hint)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 36.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (photoList.size > 1 || resolvedMusicUrl.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = TvAccentGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${currentIndex + 1} / ${photoList.size}",
                        color = TvTextPrimary.copy(alpha = 0.9f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (resolvedMusicUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.ambient_press_any_key),
                color = TvTextPrimary.copy(alpha = 0.78f),
                fontSize = 12.sp,
                style = TextStyle(shadow = directTextShadow)
            )
        }
    }
}

private fun buildSlideTransition(
    mode: String,
    slideIndex: Int
): ContentTransform {
    val effectiveMode = if (mode == "random") {
        val modes = listOf("crossfade", "slide", "zoom_fade", "blur_reveal", "flip_3d")
        modes[slideIndex % modes.size]
    } else mode

    return when (effectiveMode) {
        "slide" -> {
            (slideInHorizontally(
                animationSpec = tween(950, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> fullWidth / 3 }
            ) + fadeIn(tween(950))) togetherWith
                (slideOutHorizontally(
                    animationSpec = tween(950, easing = FastOutSlowInEasing),
                    targetOffsetX = { fullWidth -> -fullWidth / 3 }
                ) + fadeOut(tween(950)))
        }
        "zoom_fade" -> {
            (scaleIn(
                animationSpec = tween(1050, easing = FastOutSlowInEasing),
                initialScale = 0.86f
            ) + fadeIn(tween(1050))) togetherWith
                (scaleOut(
                    animationSpec = tween(1050, easing = FastOutSlowInEasing),
                    targetScale = 1.14f
                ) + fadeOut(tween(1050)))
        }
        "blur_reveal" -> {
            (scaleIn(
                animationSpec = tween(1150, easing = FastOutSlowInEasing),
                initialScale = 1.20f
            ) + fadeIn(tween(1150))) togetherWith
                (scaleOut(
                    animationSpec = tween(950, easing = FastOutSlowInEasing),
                    targetScale = 0.94f
                ) + fadeOut(tween(950)))
        }
        "flip_3d" -> {
            (scaleIn(
                animationSpec = tween(950, easing = FastOutSlowInEasing),
                initialScale = 0.78f
            ) + slideInHorizontally(
                animationSpec = tween(950, easing = FastOutSlowInEasing),
                initialOffsetX = { it / 5 }
            ) + fadeIn(tween(950))) togetherWith
                (scaleOut(
                    animationSpec = tween(950, easing = FastOutSlowInEasing),
                    targetScale = 0.78f
                ) + fadeOut(tween(850)))
        }
        else -> {
            fadeIn(tween(1200, easing = LinearEasing)) togetherWith
                fadeOut(tween(1200, easing = LinearEasing))
        }
    }
}

private data class AtmosphericParticle(
    val xSeed: Float,
    val ySeed: Float,
    val size: Float,
    val speed: Float,
    val swayAmp: Float,
    val swayFreq: Float,
    val alpha: Float,
    val colorIndex: Int
)

@Composable
fun AtmosphericParticleOverlay(
    effect: String,
    modifier: Modifier = Modifier
) {
    val particleCount = when (effect) {
        "rain" -> 95
        "snow" -> 80
        "stars" -> 85
        "fireflies" -> 50
        "sakura" -> 50
        "autumn" -> 42
        "bokeh" -> 28
        else -> 0
    }
    if (particleCount == 0) return

    val particles = remember(effect) {
        val rng = Random(effect.hashCode())
        List(particleCount) {
            AtmosphericParticle(
                xSeed = rng.nextFloat(),
                ySeed = rng.nextFloat(),
                size = rng.nextFloat(),
                speed = 0.45f + rng.nextFloat() * 0.95f,
                swayAmp = 0.01f + rng.nextFloat() * 0.045f,
                swayFreq = 1.0f + rng.nextFloat() * 3.0f,
                alpha = 0.35f + rng.nextFloat() * 0.60f,
                colorIndex = rng.nextInt(5)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particle_engine")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_tick"
    )

    val autumnColors = remember {
        listOf(
            Color(0xFFE67E22),
            Color(0xFFD35400),
            Color(0xFFF1C40F),
            Color(0xFFC0392B),
            Color(0xFFE59866)
        )
    }

    val sakuraColors = remember {
        listOf(
            Color(0xFFFFB7C5),
            Color(0xFFFFC0CB),
            Color(0xFFFFD1DC),
            Color(0xFFF8BBD0),
            Color(0xFFFFE4E8)
        )
    }

    val bokehColors = remember {
        listOf(
            Color(0xFFFFD54F),
            Color(0xFF4DD0E1),
            Color(0xFFBA68C8),
            Color(0xFFFF8A65),
            Color(0xFF81C784)
        )
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        particles.forEachIndexed { idx, p ->
            when (effect) {
                "snow" -> {
                    val yProgress = (p.ySeed + tick * p.speed * 0.14f) % 1f
                    val sway = sin((tick * p.swayFreq + idx) * 0.5f) * p.swayAmp
                    val xProgress = ((p.xSeed + sway) % 1f + 1f) % 1f
                    val cx = xProgress * w
                    val cy = yProgress * h
                    val radius = (2.2f + p.size * 5.8f).dp.toPx()

                    // Soft outer glow for larger foreground snowflakes
                    if (p.size > 0.55f) {
                        drawCircle(
                            color = Color.White.copy(alpha = p.alpha * 0.24f),
                            radius = radius * 2.1f,
                            center = Offset(cx, cy)
                        )
                    }
                    drawCircle(
                        color = Color.White.copy(alpha = p.alpha * 0.9f),
                        radius = radius,
                        center = Offset(cx, cy)
                    )
                }

                "rain" -> {
                    val yProgress = (p.ySeed + tick * p.speed * 0.65f) % 1f
                    val xProgress = ((p.xSeed + yProgress * 0.12f) % 1f + 1f) % 1f
                    val startX = xProgress * w
                    val startY = yProgress * h
                    val streakLen = (16f + p.size * 24f).dp.toPx()
                    drawLine(
                        color = Color(0xFFB3E5FC).copy(alpha = p.alpha * 0.48f),
                        start = Offset(startX, startY),
                        end = Offset(startX + streakLen * 0.18f, startY + streakLen),
                        strokeWidth = (1.1f + p.size * 1.1f).dp.toPx()
                    )
                }

                "fireflies" -> {
                    val yProgress = ((p.ySeed - tick * p.speed * 0.06f) % 1f + 1f) % 1f
                    val sway = sin((tick * p.swayFreq + idx) * 0.4f) * (p.swayAmp * 1.8f)
                    val xProgress = ((p.xSeed + sway) % 1f + 1f) % 1f
                    val pulse = (0.35f + 0.65f * ((sin(tick * 2.4f + idx) + 1f) * 0.5f))
                    val cx = xProgress * w
                    val cy = yProgress * h
                    val radius = (3f + p.size * 5f).dp.toPx()
                    val glowColor = if (p.colorIndex % 2 == 0) Color(0xFFFFD54F) else Color(0xFFFFF176)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = p.alpha * pulse * 0.85f),
                                glowColor.copy(alpha = p.alpha * pulse * 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = radius * 3.4f
                        ),
                        radius = radius * 3.4f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = p.alpha * pulse),
                        radius = radius * 0.55f,
                        center = Offset(cx, cy)
                    )
                }

                "autumn", "sakura" -> {
                    val yProgress = (p.ySeed + tick * p.speed * 0.12f) % 1f
                    val sway = sin((tick * p.swayFreq + idx) * 0.5f) * (p.swayAmp * 1.6f)
                    val xProgress = ((p.xSeed + sway) % 1f + 1f) % 1f
                    val cx = xProgress * w
                    val cy = yProgress * h
                    val leafW = (6f + p.size * 9f).dp.toPx()
                    val leafH = leafW * 0.58f
                    val angle = (tick * 45f * p.speed + idx * 37f) % 360f
                    val color = if (effect == "autumn") {
                        autumnColors[p.colorIndex % autumnColors.size]
                    } else {
                        sakuraColors[p.colorIndex % sakuraColors.size]
                    }

                    rotate(degrees = angle, pivot = Offset(cx, cy)) {
                        drawOval(
                            color = color.copy(alpha = p.alpha * 0.88f),
                            topLeft = Offset(cx - leafW / 2f, cy - leafH / 2f),
                            size = Size(leafW, leafH)
                        )
                    }
                }

                "stars" -> {
                    val twinkle = 0.25f + 0.75f * ((cos(tick * 2.2f * p.speed + idx) + 1f) * 0.5f)
                    val cx = p.xSeed * w
                    val cy = p.ySeed * h * 0.85f
                    val radius = (1.2f + p.size * 2.6f).dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = p.alpha * twinkle),
                        radius = radius,
                        center = Offset(cx, cy)
                    )
                }

                "bokeh" -> {
                    val yProgress = ((p.ySeed - tick * p.speed * 0.035f) % 1f + 1f) % 1f
                    val sway = sin((tick * 0.6f + idx) * 0.35f) * p.swayAmp
                    val xProgress = ((p.xSeed + sway) % 1f + 1f) % 1f
                    val cx = xProgress * w
                    val cy = yProgress * h
                    val radius = (22f + p.size * 58f).dp.toPx()
                    val color = bokehColors[p.colorIndex % bokehColors.size]

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = p.alpha * 0.10f),
                                color.copy(alpha = p.alpha * 0.24f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}

/**
 * TV Remote-friendly dialog allowing the user to select a detected device/USB image folder,
 * launch the system folder picker, or enter a custom folder path.
 */
@Composable
fun TvFolderPickerDialog(
    currentFolderUri: String,
    onFolderSelected: (String) -> Unit,
    onLaunchSystemPicker: () -> Unit,
    onDismiss: () -> Unit
) {
    val detectedFolders = remember { scanCommonDeviceImageFolders() }
    var customPathInput by remember(currentFolderUri) {
        mutableStateOf(if (currentFolderUri.startsWith("content://")) "" else currentFolderUri)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(640.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0E1629))
                    .border(1.5.dp, TvAccentGold.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.slideshow_select_folder_btn),
                            color = TvTextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TvActionButton(
                        text = stringResource(R.string.action_cancel),
                        icon = Icons.Default.Close,
                        onClick = onDismiss,
                        baseColor = Color(0xFF1D2942),
                        focusedColor = TvAccentGold
                    )
                }

                // System Folder Picker Button
                TvActionButton(
                    text = stringResource(R.string.slideshow_system_folder_picker),
                    icon = Icons.Default.Folder,
                    onClick = onLaunchSystemPicker,
                    baseColor = TvAccentGold.copy(alpha = 0.88f),
                    focusedColor = TvAccentGold,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )

                // Detected device/USB folders
                if (detectedFolders.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(detectedFolders, key = { it.path }) { folder ->
                            val isSelected = currentFolderUri == folder.path
                            TvFocusableCard(
                                onClick = {
                                    onFolderSelected(folder.path)
                                    onDismiss()
                                },
                                scaleUpRatio = 1.02f,
                                shape = RoundedCornerShape(12.dp),
                                useDefaultBorder = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) { isFocused ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isFocused -> TvAccentGold.copy(alpha = 0.24f)
                                                isSelected -> TvAccentGold.copy(alpha = 0.14f)
                                                else -> Color(0xFF090F1D)
                                            }
                                        )
                                        .border(
                                            width = if (isFocused) 2.dp else 1.dp,
                                            color = if (isFocused) TvAccentGoldRadiant else Color.White.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = TvAccentGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = folder.name,
                                                color = TvTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${folder.path} • ${folder.imageCount} photos",
                                                color = TvTextSecondary,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(TvAccentGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom folder path input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customPathInput,
                        onValueChange = { customPathInput = it },
                        placeholder = {
                            Text(
                                text = "/storage/emulated/0/Pictures",
                                color = TvTextSecondary.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TvAccentGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
                            focusedTextColor = TvTextPrimary,
                            unfocusedTextColor = TvTextPrimary,
                            cursorColor = TvAccentGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    TvActionButton(
                        text = stringResource(R.string.action_save),
                        icon = Icons.Default.Check,
                        onClick = {
                            if (customPathInput.isNotBlank()) {
                                onFolderSelected(customPathInput.trim())
                                onDismiss()
                            }
                        },
                        baseColor = TvAccentGold.copy(alpha = 0.88f),
                        focusedColor = TvAccentGold,
                        modifier = Modifier.height(50.dp)
                    )
                }
            }
        }
    }
}
