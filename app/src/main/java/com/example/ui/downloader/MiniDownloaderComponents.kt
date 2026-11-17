package com.example.ui.downloader

import android.net.Uri
import android.webkit.URLUtil
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.R
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

enum class DownloaderSubScreen {
    KEYPAD, BROWSER, DOWNLOADS
}

data class MiniDownloadedItem(
    val file: File,
    val fileName: String,
    val fileSize: Long,
    val lastModified: Long,
    val sourceUrl: String?
)

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val fileName: String, val downloadedBytes: Long, val totalBytes: Long, val percent: Int) : DownloadState()
    data class Completed(val file: File, val fileName: String, val fileSize: Long) : DownloadState()
    data class Error(val message: String, val url: String? = null) : DownloadState()
}

internal fun sanitizeFileName(name: String): String {
    val clean = name.replace(Regex("""[\\/:*?"<>|]"""), "_").trim()
    return if (clean.isEmpty()) "app_${System.currentTimeMillis()}.apk" else clean
}

internal fun extractFileName(
    url: String,
    contentDisposition: String?,
    mimeType: String?,
    discoveredName: String? = null
): String {
    if (!contentDisposition.isNullOrBlank()) {
        val cdMatcher = Regex("""filename\*?=(?:UTF-8'')?["']?([^"';]+)["']?""", RegexOption.IGNORE_CASE)
            .find(contentDisposition)
        if (cdMatcher != null) {
            val rawName = Uri.decode(cdMatcher.groupValues[1].trim())
            if (rawName.isNotEmpty()) {
                val s = sanitizeFileName(rawName)
                return if (s.endsWith(".apk", ignoreCase = true)) s else "$s.apk"
            }
        }
    }

    if (!discoveredName.isNullOrBlank()) {
        val cleanDiscovered = sanitizeFileName(Uri.decode(discoveredName.trim()))
        if (cleanDiscovered.isNotEmpty()) {
            return if (cleanDiscovered.endsWith(".apk", ignoreCase = true)) cleanDiscovered else "$cleanDiscovered.apk"
        }
    }

    val apkMatcher = Regex("""/([a-zA-Z0-9_\-\.]+?)(?:-apk|\.apk)(?:\.[0-9]+)?(?:/|\?|#|$)""", RegexOption.IGNORE_CASE)
        .find(url)
    if (apkMatcher != null) {
        val baseName = Uri.decode(apkMatcher.groupValues[1].trim())
        if (baseName.isNotEmpty()) {
            return sanitizeFileName("$baseName.apk")
        }
    }

    val driveId = com.example.ui.common.DownloadUrlResolver.extractGoogleDriveFileId(url)
    if (!driveId.isNullOrBlank()) {
        return sanitizeFileName("gdrive_${driveId.take(10)}.apk")
    }

    val guessed = URLUtil.guessFileName(url, contentDisposition, mimeType)
    val sanitized = sanitizeFileName(guessed)
    return when {
        sanitized.endsWith(".apk", ignoreCase = true) -> sanitized
        sanitized.endsWith(".bin", ignoreCase = true) -> sanitized.replace(".bin", ".apk", ignoreCase = true)
        sanitized.contains(".") -> sanitized.substringBeforeLast(".") + ".apk"
        else -> "$sanitized.apk"
    }
}

/**
 * Virtual Mouse Pointer overlay component:
 * High-visibility pointer arrow with subtle shadow and click animation.
 */
@Composable
fun VirtualMousePointer(
    x: Float,
    y: Float,
    isClicking: Boolean
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
            .size(32.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMousePointer(isClicking)
        }
    }
}

private fun DrawScope.drawMousePointer(isClicking: Boolean) {
    val scale = if (isClicking) 0.88f else 1.0f
    val path = Path().apply {
        moveTo(0f * scale, 0f * scale)
        lineTo(0f * scale, 24f * scale)
        lineTo(6f * scale, 19f * scale)
        lineTo(11f * scale, 29f * scale)
        lineTo(15f * scale, 27f * scale)
        lineTo(10f * scale, 17f * scale)
        lineTo(18f * scale, 17f * scale)
        close()
    }

    val shadowPath = Path().apply {
        moveTo(1.5f * scale, 1.5f * scale)
        lineTo(1.5f * scale, 25.5f * scale)
        lineTo(7.5f * scale, 20.5f * scale)
        lineTo(12.5f * scale, 30.5f * scale)
        lineTo(16.5f * scale, 28.5f * scale)
        lineTo(11.5f * scale, 18.5f * scale)
        lineTo(19.5f * scale, 18.5f * scale)
        close()
    }

    drawPath(shadowPath, color = Color.Black.copy(alpha = 0.6f))
    drawPath(path, color = if (isClicking) Color(0xFFE11D48) else Color.White)

    drawPath(
        path,
        color = if (isClicking) Color.White else Color.Black,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8f)
    )
}

/**
 * Screen 3: Downloaded Files Manager Screen for Mini Downloader
 * Allows reviewing, installing, re-downloading, and deleting files.
 */
@Composable
fun MiniDownloadsManagerScreen(
    files: List<MiniDownloadedItem>,
    onInstallFile: (MiniDownloadedItem) -> Unit,
    onRedownloadFile: (MiniDownloadedItem) -> Unit,
    onDeleteFile: (MiniDownloadedItem) -> Unit,
    onClearAllFiles: () -> Unit,
    onBackToPrevious: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd • HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF881337),
                        TvMidnightBackground,
                        Color(0xFF020617)
                    )
                )
            )
            .padding(horizontal = 48.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TvFocusableCard(
                    onClick = onBackToPrevious,
                    modifier = Modifier.size(42.dp)
                ) { isFocused ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isFocused) Color.Black else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE11D48)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = stringResource(R.string.mini_downloader_downloads_title),
                        color = TvTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.mini_downloader_downloads_subtitle),
                        color = TvTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            if (files.isNotEmpty()) {
                TvFocusableCard(
                    onClick = onClearAllFiles,
                    modifier = Modifier.height(40.dp)
                ) { isFocused ->
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(
                                if (isFocused) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.25f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text(
                            text = stringResource(R.string.mini_downloader_clear_all),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.mini_downloader_no_files),
                        color = TvTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.mini_downloader_no_files_desc),
                        color = TvTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TvFocusableCard(
                        onClick = onBackToPrevious,
                        modifier = Modifier.height(44.dp).width(190.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isFocused) TvAccentGold else Color(0xFFE11D48), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.mini_downloader_title),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(files, key = { it.file.absolutePath }) { item ->
                    val sizeMb = item.fileSize / (1024f * 1024f)
                    val dateFormatted = try {
                        dateFormat.format(item.lastModified)
                    } catch (e: Exception) {
                        ""
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f).padding(end = 16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE11D48).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFFE11D48).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Android,
                                        contentDescription = null,
                                        tint = Color(0xFFFB7185),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = item.fileName,
                                        color = TvTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "%.1f MB".format(sizeMb),
                                            color = TvAccentGold,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        if (dateFormatted.isNotEmpty()) {
                                            Text(
                                                text = "•",
                                                color = TvTextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = dateFormatted,
                                                color = TvTextSecondary,
                                                fontSize = 13.sp
                                            )
                                        }

                                        if (!item.sourceUrl.isNullOrBlank()) {
                                            Text(
                                                text = "•",
                                                color = TvTextSecondary,
                                                fontSize = 12.sp
                                            )
                                            val shortUrl = try {
                                                val uri = item.sourceUrl.toUri()
                                                uri.host ?: item.sourceUrl
                                            } catch (e: Exception) {
                                                item.sourceUrl
                                            }
                                            Text(
                                                text = shortUrl,
                                                color = Color(0xFF60A5FA),
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TvFocusableCard(
                                    onClick = { onInstallFile(item) },
                                    modifier = Modifier.height(40.dp)
                                ) { isFocused ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .background(
                                                if (isFocused) TvAccentGold else Color(0xFF10B981),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = if (isFocused) Color.Black else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.mini_downloader_install_now),
                                            color = if (isFocused) Color.Black else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                TvFocusableCard(
                                    onClick = { onRedownloadFile(item) },
                                    modifier = Modifier.height(40.dp)
                                ) { isFocused ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .background(
                                                if (isFocused) TvAccentGold else Color(0xFF2563EB),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = if (isFocused) Color.Black else Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.mini_downloader_redownload),
                                            color = if (isFocused) Color.Black else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                TvFocusableCard(
                                    onClick = { onDeleteFile(item) },
                                    modifier = Modifier.size(40.dp)
                                ) { isFocused ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                if (isFocused) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.25f),
                                            RoundedCornerShape(10.dp)
                                            )
                                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
