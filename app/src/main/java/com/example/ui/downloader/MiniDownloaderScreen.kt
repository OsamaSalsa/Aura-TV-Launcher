package com.example.ui.downloader

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.R
import com.example.ui.common.BindWebViewLifecycle
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.findActivityContext
import com.example.ui.common.grantSafeWebPermissions
import com.example.ui.common.hardenWebView
import com.example.ui.common.injectWebViewStabilityShim
import com.example.ui.common.isWebViewDestroyed
import com.example.ui.common.postOnMain
import com.example.ui.common.safeDestroyWebView
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
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

private fun sanitizeFileName(name: String): String {
    val clean = name.replace(Regex("""[\\/:*?"<>|]"""), "_").trim()
    return if (clean.isEmpty()) "app_${System.currentTimeMillis()}.apk" else clean
}

private fun extractFileName(
    url: String,
    contentDisposition: String?,
    mimeType: String?,
    discoveredName: String? = null
): String {
    // 1. Content-Disposition header
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

    // 2. Discovered name from HTML intermediate page (e.g. Google Drive virus-scan warning)
    if (!discoveredName.isNullOrBlank()) {
        val cleanDiscovered = sanitizeFileName(Uri.decode(discoveredName.trim()))
        if (cleanDiscovered.isNotEmpty()) {
            return if (cleanDiscovered.endsWith(".apk", ignoreCase = true)) cleanDiscovered else "$cleanDiscovered.apk"
        }
    }

    // 3. Extract from URL (e.g. /lm-fos-re-1-1-9-apk.6272018/ or app-v1.apk)
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

    // 4. Fallback to URLUtil
    val guessed = URLUtil.guessFileName(url, contentDisposition, mimeType)
    val sanitized = sanitizeFileName(guessed)
    return when {
        sanitized.endsWith(".apk", ignoreCase = true) -> sanitized
        sanitized.endsWith(".bin", ignoreCase = true) -> sanitized.replace(".bin", ".apk", ignoreCase = true)
        sanitized.contains(".") -> sanitized.substringBeforeLast(".") + ".apk"
        else -> "$sanitized.apk"
    }
}

@Composable
fun MiniDownloaderScreen(
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    var currentSubScreen by remember { mutableStateOf(DownloaderSubScreen.KEYPAD) }
    var previousSubScreen by remember { mutableStateOf(DownloaderSubScreen.KEYPAD) }
    var inputCode by remember { mutableStateOf("") }
    var activeUrlToLoad by remember { mutableStateOf("http://aftv.news/") }

    val prefs = remember { context.getSharedPreferences("mini_downloader_history", Context.MODE_PRIVATE) }
    var downloadedFiles by remember { mutableStateOf<List<MiniDownloadedItem>>(emptyList()) }

    fun refreshDownloadedFiles() {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        if (downloadDir.exists()) {
            val list = downloadDir.listFiles { f ->
                f.isFile && (f.name.endsWith(".apk", ignoreCase = true) || f.extension.equals("apk", ignoreCase = true))
            }?.map { file ->
                MiniDownloadedItem(
                    file = file,
                    fileName = file.name,
                    fileSize = file.length(),
                    lastModified = file.lastModified(),
                    sourceUrl = prefs.getString("url_${file.name}", null)
                )
            }?.sortedByDescending { it.lastModified } ?: emptyList()
            downloadedFiles = list
        } else {
            downloadedFiles = emptyList()
        }
    }

    LaunchedEffect(Unit) {
        refreshDownloadedFiles()
    }

    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    fun launchDownload(
        downloadUrl: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        referer: String?
    ) {
        downloadJob?.cancel()
        downloadJob = scope.launch(Dispatchers.IO) {
            var currentUrl = com.example.ui.common.DownloadUrlResolver.normalizeInitialDownloadUrl(downloadUrl)
            var currentReferer = referer ?: "http://aftv.news/"
            var discoveredHtmlFileName: String? = null
            val sessionCookies = linkedMapOf<String, String>()
            val baseUserAgent = if (!userAgent.isNullOrBlank()) {
                userAgent
            } else {
                "Mozilla/5.0 (Linux; Android 10; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Downloader/1.4.3"
            }

            withContext(Dispatchers.Main) {
                downloadState = DownloadState.Downloading(
                    context.getString(R.string.mini_downloader_connecting),
                    0L,
                    -1L,
                    -1
                )
            }

            try {
                var connection: HttpURLConnection? = null
                var redirects = 0
                val maxRedirects = 14

                while (redirects < maxRedirects) {
                    val urlObj = URL(currentUrl)
                    val effectiveUa = com.example.ui.common.DownloadUrlResolver.resolveUserAgentForUrl(currentUrl, baseUserAgent)
                    val conn = (urlObj.openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false
                        connectTimeout = 20000
                        readTimeout = 30000
                        setRequestProperty("User-Agent", effectiveUa)
                        setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,application/vnd.android.package-archive,application/octet-stream,*/*;q=0.8")
                        setRequestProperty("Accept-Language", "en-US,en;q=0.9,ar;q=0.8")
                        setRequestProperty("Referer", currentReferer)
                        setRequestProperty("Connection", "keep-alive")

                        val webViewCookies = CookieManager.getInstance().getCookie(currentUrl)
                        val mergedCookies = buildString {
                            if (!webViewCookies.isNullOrBlank()) {
                                append(webViewCookies.trim().trimEnd(';'))
                            }
                            if (sessionCookies.isNotEmpty()) {
                                if (isNotEmpty()) append("; ")
                                append(sessionCookies.entries.joinToString("; ") { "${it.key}=${it.value}" })
                            }
                        }
                        if (mergedCookies.isNotBlank()) {
                            setRequestProperty("Cookie", mergedCookies)
                        }
                    }

                    val code = conn.responseCode

                    conn.headerFields["Set-Cookie"]?.forEach { cookieHeader ->
                        CookieManager.getInstance().setCookie(currentUrl, cookieHeader)
                        val pair = cookieHeader.substringBefore(";").trim()
                        val k = pair.substringBefore("=", "").trim()
                        val v = pair.substringAfter("=", "").trim()
                        if (k.isNotEmpty()) {
                            sessionCookies[k] = v
                        }
                    }

                    if (code in 300..399) {
                        val newLoc = conn.getHeaderField("Location")
                        conn.disconnect()
                        if (newLoc.isNullOrBlank()) {
                            break
                        }
                        currentReferer = currentUrl
                        val resolvedLoc = if (newLoc.startsWith("http://", ignoreCase = true) || newLoc.startsWith("https://", ignoreCase = true)) {
                            newLoc
                        } else {
                            URL(urlObj, newLoc).toString()
                        }
                        currentUrl = com.example.ui.common.DownloadUrlResolver.unwrapIntentUrl(resolvedLoc)
                        redirects++
                    } else if (code in 200..299) {
                        val contentType = conn.contentType
                        val finalDisposition = conn.getHeaderField("Content-Disposition")
                        val isApkDirect = currentUrl.endsWith(".apk", ignoreCase = true) ||
                                finalDisposition?.contains("attachment", ignoreCase = true) == true ||
                                finalDisposition?.contains(".apk", ignoreCase = true) == true ||
                                contentType?.contains("vnd.android.package-archive", ignoreCase = true) == true ||
                                contentType?.contains("octet-stream", ignoreCase = true) == true ||
                                contentType?.contains("application/x-apk", ignoreCase = true) == true

                        if (!isApkDirect && contentType?.startsWith("text/html", ignoreCase = true) == true) {
                            val htmlBody = try {
                                conn.inputStream.bufferedReader().use { it.readText() }
                            } catch (e: Exception) {
                                ""
                            }
                            conn.disconnect()

                            val htmlResult = com.example.ui.common.DownloadUrlResolver.parseHtmlDownloadPage(currentUrl, htmlBody)
                            if (!htmlResult.discoveredFileName.isNullOrBlank()) {
                                discoveredHtmlFileName = htmlResult.discoveredFileName
                            }
                            val targetLink = htmlResult.nextUrl

                            if (!targetLink.isNullOrBlank()) {
                                currentReferer = currentUrl
                                currentUrl = targetLink
                                redirects++
                                continue
                            } else {
                                withContext(Dispatchers.Main) {
                                    activeUrlToLoad = currentUrl
                                    currentSubScreen = DownloaderSubScreen.BROWSER
                                    downloadState = DownloadState.Idle
                                }
                                return@launch
                            }
                        } else {
                            connection = conn
                            break
                        }
                    } else {
                        connection = conn
                        break
                    }
                }

                val conn = connection ?: throw Exception("Failed to establish connection to $downloadUrl")
                val responseCode = conn.responseCode

                if (responseCode !in 200..299) {
                    conn.disconnect()
                    withContext(Dispatchers.Main) {
                        activeUrlToLoad = currentUrl
                        currentSubScreen = DownloaderSubScreen.BROWSER
                        downloadState = DownloadState.Idle
                    }
                    return@launch
                }

                val contentType = conn.contentType ?: mimeType
                val finalDisposition = conn.getHeaderField("Content-Disposition") ?: contentDisposition

                val guessedName = extractFileName(
                    url = currentUrl,
                    contentDisposition = finalDisposition,
                    mimeType = contentType,
                    discoveredName = discoveredHtmlFileName
                )
                val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
                if (!downloadDir.exists()) downloadDir.mkdirs()
                val outputFile = File(downloadDir, guessedName)

                val contentLength = conn.contentLength.toLong()
                var downloadedBytes = 0L

                withContext(Dispatchers.Main) {
                    downloadState = DownloadState.Downloading(guessedName, 0L, contentLength, 0)
                }

                conn.inputStream.use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        val buffer = ByteArray(64 * 1024)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            val percent = if (contentLength > 0) {
                                ((downloadedBytes * 100) / contentLength).toInt().coerceIn(0, 100)
                            } else -1
                            withContext(Dispatchers.Main) {
                                downloadState = DownloadState.Downloading(guessedName, downloadedBytes, contentLength, percent)
                            }
                        }
                        outputStream.flush()
                    }
                }
                conn.disconnect()

                withContext(Dispatchers.Main) {
                    try {
                        prefs.edit().putString("url_${outputFile.name}", currentUrl).apply()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    refreshDownloadedFiles()
                    downloadState = DownloadState.Completed(outputFile, guessedName, outputFile.length())
                    installApk(context, outputFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (currentUrl.isNotBlank()) {
                        activeUrlToLoad = currentUrl
                        currentSubScreen = DownloaderSubScreen.BROWSER
                    }
                    downloadState = DownloadState.Idle
                }
            }
        }
    }

    fun submitCodeAndOpenBrowser(code: String) {
        val trimmed = code.trim()
        val finalUrl = when {
            trimmed.isEmpty() -> "http://aftv.news/"
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            trimmed.contains(".") && !trimmed.all { it.isDigit() } -> "http://$trimmed"
            else -> "http://aftv.news/$trimmed"
        }

        if (com.example.ui.common.DownloadUrlResolver.shouldInterceptAsDownload(finalUrl)) {
            launchDownload(
                com.example.ui.common.DownloadUrlResolver.normalizeInitialDownloadUrl(finalUrl),
                null,
                null,
                "application/vnd.android.package-archive",
                finalUrl
            )
            return
        }

        downloadState = DownloadState.Idle
        activeUrlToLoad = finalUrl
        currentSubScreen = DownloaderSubScreen.BROWSER
    }

    when (currentSubScreen) {
        DownloaderSubScreen.KEYPAD -> {
            BackHandler {
                onBackToHome()
            }

            KeypadScreen(
                inputCode = inputCode,
                onCodeChanged = { inputCode = it },
                onSubmit = { submitCodeAndOpenBrowser(inputCode) },
                onBackToHome = onBackToHome,
                downloadedFilesCount = downloadedFiles.size,
                onOpenDownloads = {
                    refreshDownloadedFiles()
                    previousSubScreen = DownloaderSubScreen.KEYPAD
                    currentSubScreen = DownloaderSubScreen.DOWNLOADS
                }
            )
        }

        DownloaderSubScreen.BROWSER -> {
            BackHandler {
                currentSubScreen = DownloaderSubScreen.KEYPAD
            }

            BrowserScreen(
                url = activeUrlToLoad,
                onGoToKeypad = { currentSubScreen = DownloaderSubScreen.KEYPAD },
                onCloseToHome = onBackToHome,
                onRequestDownload = { dUrl, ua, disp, mime, ref ->
                    launchDownload(dUrl, ua, disp, mime, ref)
                },
                downloadedFilesCount = downloadedFiles.size,
                onOpenDownloads = {
                    refreshDownloadedFiles()
                    previousSubScreen = DownloaderSubScreen.BROWSER
                    currentSubScreen = DownloaderSubScreen.DOWNLOADS
                }
            )
        }

        DownloaderSubScreen.DOWNLOADS -> {
            BackHandler {
                currentSubScreen = previousSubScreen
            }

            MiniDownloadsManagerScreen(
                files = downloadedFiles,
                onInstallFile = { item -> installApk(context, item.file) },
                onRedownloadFile = { item ->
                    val url = item.sourceUrl
                    if (!url.isNullOrBlank()) {
                        Toast.makeText(context, context.getString(R.string.mini_downloader_redownloading), Toast.LENGTH_SHORT).show()
                        launchDownload(url, null, null, null, null)
                    } else {
                        Toast.makeText(context, "الرابط الأصلي غير متوفر لهذا الملف، يمكنك فتح المتصفح لتنزيله", Toast.LENGTH_LONG).show()
                    }
                },
                onDeleteFile = { item ->
                    try {
                        if (item.file.exists()) item.file.delete()
                        refreshDownloadedFiles()
                        Toast.makeText(context, context.getString(R.string.mini_downloader_file_deleted), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onClearAllFiles = {
                    try {
                        downloadedFiles.forEach { if (it.file.exists()) it.file.delete() }
                        refreshDownloadedFiles()
                        Toast.makeText(context, context.getString(R.string.mini_downloader_all_cleared), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onBackToPrevious = {
                    currentSubScreen = previousSubScreen
                }
            )
        }
    }

    // Download Dialog & Delete Confirmation Dialog
    when (val state = downloadState) {
        is DownloadState.Downloading -> {
            Dialog(onDismissRequest = { /* Prevent accidental dismissal during download */ }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 16.dp,
                    modifier = Modifier.width(440.dp).padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.mini_downloader_downloading),
                            color = TvTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.fileName,
                            color = TvAccentGold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        if (state.percent >= 0) {
                            LinearProgressIndicator(
                                progress = { state.percent / 100f },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                color = TvAccentGold,
                                trackColor = Color.White.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val downloadedMb = state.downloadedBytes / (1024f * 1024f)
                            val totalMb = state.totalBytes / (1024f * 1024f)
                            Text(
                                text = stringResource(R.string.mini_downloader_status_format).format(state.percent, downloadedMb, totalMb),
                                color = TvTextSecondary,
                                fontSize = 13.sp
                            )
                        } else {
                            CircularProgressIndicator(
                                color = TvAccentGold,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val downloadedMb = state.downloadedBytes / (1024f * 1024f)
                            Text(
                                text = stringResource(R.string.mini_downloader_downloaded_mb).format(downloadedMb),
                                color = TvTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        TvFocusableCard(
                            onClick = {
                                downloadJob?.cancel()
                                downloadState = DownloadState.Idle
                            },
                            modifier = Modifier.height(42.dp).fillMaxWidth(0.6f)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    if (isFocused) Color.Red.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.action_cancel), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        is DownloadState.Completed -> {
            Dialog(onDismissRequest = { downloadState = DownloadState.Idle }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 18.dp,
                    modifier = Modifier.width(460.dp).padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.mini_downloader_delete_prompt_title),
                            color = TvTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${state.fileName} (${stringResource(R.string.mini_downloader_downloaded_mb).format(state.fileSize / (1024f * 1024f))})",
                            color = TvAccentGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.mini_downloader_delete_prompt_desc),
                            color = TvTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Delete button (Requested by user: يسال عن حذف الملف بعد التنزيل)
                            TvFocusableCard(
                                onClick = {
                                    try {
                                        if (state.file.exists()) state.file.delete()
                                        refreshDownloadedFiles()
                                        Toast.makeText(context, context.getString(R.string.mini_downloader_file_deleted), Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    downloadState = DownloadState.Idle
                                },
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        if (isFocused) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.35f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.mini_downloader_delete_file),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Keep file button
                            TvFocusableCard(
                                onClick = { downloadState = DownloadState.Idle },
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.mini_downloader_keep_file),
                                        color = if (isFocused) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Re-install button
                            TvFocusableCard(
                                onClick = { installApk(context, state.file) },
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        if (isFocused) TvAccentGold else Color(0xFF334155),
                                        RoundedCornerShape(12.dp)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.mini_downloader_install_again),
                                        color = if (isFocused) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // View All Downloads button
                            TvFocusableCard(
                                onClick = {
                                    downloadState = DownloadState.Idle
                                    previousSubScreen = currentSubScreen
                                    currentSubScreen = DownloaderSubScreen.DOWNLOADS
                                    refreshDownloadedFiles()
                                },
                                modifier = Modifier.weight(1.1f).height(46.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        if (isFocused) TvAccentGold else Color(0xFFE11D48).copy(alpha = 0.35f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.mini_downloader_view_downloads),
                                        color = if (isFocused) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        is DownloadState.Error -> {
            Dialog(onDismissRequest = { downloadState = DownloadState.Idle }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 16.dp,
                    modifier = Modifier.width(440.dp).padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = stringResource(R.string.mini_downloader_download_error), color = TvTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.message, color = TvTextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!state.url.isNullOrBlank()) {
                                TvFocusableCard(
                                    onClick = {
                                        activeUrlToLoad = state.url
                                        currentSubScreen = DownloaderSubScreen.BROWSER
                                        downloadState = DownloadState.Idle
                                    },
                                    modifier = Modifier.weight(1f).height(42.dp)
                                ) { isFocused ->
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(
                                            if (isFocused) TvAccentGold else Color(0xFF334155),
                                            RoundedCornerShape(12.dp)
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.mini_downloader_open_in_browser),
                                            color = if (isFocused) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            TvFocusableCard(
                                onClick = { downloadState = DownloadState.Idle },
                                modifier = Modifier.weight(if (!state.url.isNullOrBlank()) 0.6f else 1f).height(42.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        if (isFocused) Color.White else Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "OK", color = if (isFocused) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        else -> {}
    }
}

private fun installApk(context: Context, file: File) {
    try {
        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(installIntent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.mini_downloader_error_installer).format(e.message), Toast.LENGTH_LONG).show()
    }
}

/**
 * Screen 1: Calculator-like numeric entry screen (Fits at least 7 digits)
 * 7 8 9
 * 4 5 6
 * 1 2 3
 *   0 OK / Enter
 */
@Composable
fun KeypadScreen(
    inputCode: String,
    onCodeChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToHome: () -> Unit,
    downloadedFilesCount: Int = 0,
    onOpenDownloads: () -> Unit = {}
) {
    fun appendDigit(d: String) {
        if (inputCode.length < 24) {
            onCodeChanged(inputCode + d)
        }
    }

    fun removeDigit() {
        if (inputCode.isNotEmpty()) {
            onCodeChanged(inputCode.dropLast(1))
        }
    }

    fun clearAll() {
        onCodeChanged("")
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF881337), // Crimson/Rose deep tone matching Downloader
                        TvMidnightBackground,
                        Color(0xFF020617)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == KeyEvent.ACTION_DOWN) {
                    when (native.keyCode) {
                        KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> { appendDigit("0"); true }
                        KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> { appendDigit("1"); true }
                        KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> { appendDigit("2"); true }
                        KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> { appendDigit("3"); true }
                        KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> { appendDigit("4"); true }
                        KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> { appendDigit("5"); true }
                        KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_NUMPAD_6 -> { appendDigit("6"); true }
                        KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> { appendDigit("7"); true }
                        KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> { appendDigit("8"); true }
                        KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> { appendDigit("9"); true }
                        KeyEvent.KEYCODE_DEL -> { removeDigit(); true }
                        KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> { onSubmit(); true }
                        else -> false
                    }
                } else false
            }
    ) {
        val isLandscape = maxWidth >= 580.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TvFocusableCard(
                        onClick = onBackToHome,
                        modifier = Modifier.size(40.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home", tint = if (isFocused) Color.Black else Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE11D48)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.mini_downloader_title),
                            color = TvTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(R.string.mini_downloader_subtitle),
                            color = TvTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Downloads Library button with Count Badge
                    TvFocusableCard(
                        onClick = onOpenDownloads,
                        modifier = Modifier.height(38.dp)
                    ) { isFocused ->
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(
                                    if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Downloads",
                                tint = if (isFocused) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.mini_downloader_downloads_btn),
                                color = if (isFocused) Color.Black else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (downloadedFilesCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isFocused) Color.Black else Color(0xFFE11D48))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$downloadedFilesCount",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    // Instruction badge (Clean, neutral)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.mini_downloader_quick_title),
                            color = TvAccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isLandscape) {
                // Landscape Side-by-Side: Display + GO on Left, Keypad on Right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Display Box + Prominent GO Button + Hint
                    Column(
                        modifier = Modifier.width(340.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Display Box: ONLY Numbers!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF030712))
                                .border(2.dp, Color(0xFFE11D48), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (inputCode.isEmpty()) "123456" else inputCode,
                                color = if (inputCode.isEmpty()) Color.White.copy(alpha = 0.25f) else Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // GO Button (Width matches Display Box)
                        TvFocusableCard(
                            onClick = onSubmit,
                            scaleUpRatio = 1.05f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isFocused) Brush.horizontalGradient(listOf(Color(0xFFE11D48), TvAccentGold))
                                        else Brush.horizontalGradient(listOf(Color(0xFFBE123C), Color(0xFF9F1239))),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        width = if (isFocused) 2.5.dp else 0.dp,
                                        color = if (isFocused) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(R.string.mini_downloader_go),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Helpful Hint pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.mini_downloader_hint_code),
                                color = TvTextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(36.dp))

                    // Right Column: Keypad
                    val buttonWidth = 76.dp
                    val buttonHeight = 44.dp

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Row 1: 7 8 9
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeypadNumberButton("7", buttonWidth, buttonHeight) { appendDigit("7") }
                            KeypadNumberButton("8", buttonWidth, buttonHeight) { appendDigit("8") }
                            KeypadNumberButton("9", buttonWidth, buttonHeight) { appendDigit("9") }
                        }

                        // Row 2: 4 5 6
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeypadNumberButton("4", buttonWidth, buttonHeight) { appendDigit("4") }
                            KeypadNumberButton("5", buttonWidth, buttonHeight) { appendDigit("5") }
                            KeypadNumberButton("6", buttonWidth, buttonHeight) { appendDigit("6") }
                        }

                        // Row 3: 1 2 3
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeypadNumberButton("1", buttonWidth, buttonHeight) { appendDigit("1") }
                            KeypadNumberButton("2", buttonWidth, buttonHeight) { appendDigit("2") }
                            KeypadNumberButton("3", buttonWidth, buttonHeight) { appendDigit("3") }
                        }

                        // Row 4: Clear (C), 0, Backspace (⌫)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeypadActionButton("C", Color(0xFFEF4444), buttonWidth, buttonHeight) { clearAll() }
                            KeypadNumberButton("0", buttonWidth, buttonHeight) { appendDigit("0") }
                            KeypadActionButton("⌫", Color(0xFFF59E0B), buttonWidth, buttonHeight) { removeDigit() }
                        }
                    }
                }
            } else {
                // Portrait Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Display Box: ONLY Numbers
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF030712))
                            .border(2.dp, Color(0xFFE11D48), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (inputCode.isEmpty()) "123456" else inputCode,
                            color = if (inputCode.isEmpty()) Color.White.copy(alpha = 0.25f) else Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val pBtnWidth = 84.dp
                    val pBtnHeight = 46.dp

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeypadNumberButton("7", pBtnWidth, pBtnHeight) { appendDigit("7") }
                            KeypadNumberButton("8", pBtnWidth, pBtnHeight) { appendDigit("8") }
                            KeypadNumberButton("9", pBtnWidth, pBtnHeight) { appendDigit("9") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeypadNumberButton("4", pBtnWidth, pBtnHeight) { appendDigit("4") }
                            KeypadNumberButton("5", pBtnWidth, pBtnHeight) { appendDigit("5") }
                            KeypadNumberButton("6", pBtnWidth, pBtnHeight) { appendDigit("6") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeypadNumberButton("1", pBtnWidth, pBtnHeight) { appendDigit("1") }
                            KeypadNumberButton("2", pBtnWidth, pBtnHeight) { appendDigit("2") }
                            KeypadNumberButton("3", pBtnWidth, pBtnHeight) { appendDigit("3") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeypadActionButton("C", Color(0xFFEF4444), pBtnWidth, pBtnHeight) { clearAll() }
                            KeypadNumberButton("0", pBtnWidth, pBtnHeight) { appendDigit("0") }
                            KeypadActionButton("⌫", Color(0xFFF59E0B), pBtnWidth, pBtnHeight) { removeDigit() }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TvFocusableCard(
                            onClick = onSubmit,
                            scaleUpRatio = 1.05f,
                            modifier = Modifier
                                .width(268.dp)
                                .height(50.dp)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isFocused) Brush.horizontalGradient(listOf(Color(0xFFE11D48), TvAccentGold))
                                        else Brush.horizontalGradient(listOf(Color(0xFFBE123C), Color(0xFF9F1239))),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        width = if (isFocused) 2.5.dp else 0.dp,
                                        color = if (isFocused) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.mini_downloader_go),
                                        color = Color.White,
                                        fontSize = 17.sp,
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
}

@Composable
fun KeypadNumberButton(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.1f,
        modifier = Modifier.size(width, height)
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isFocused) TvAccentGold else Color(0xFF1E293B),
                    RoundedCornerShape(14.dp)
                )
                .border(
                    width = if (isFocused) 2.5.dp else 1.dp,
                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isFocused) Color.Black else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun KeypadActionButton(
    text: String,
    accentColor: Color,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    TvFocusableCard(
        onClick = onClick,
        scaleUpRatio = 1.1f,
        modifier = Modifier.size(width, height)
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isFocused) accentColor else accentColor.copy(alpha = 0.25f),
                    RoundedCornerShape(14.dp)
                )
                .border(
                    width = if (isFocused) 2.5.dp else 1.dp,
                    color = if (isFocused) Color.White else accentColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isFocused) Color.Black else Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Screen 2: Full-screen Web Browser with Virtual Mouse Cursor & Top Corner '#' Button
 */
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun BrowserScreen(
    url: String,
    onGoToKeypad: () -> Unit,
    onCloseToHome: () -> Unit,
    onRequestDownload: (url: String, userAgent: String?, contentDisposition: String?, mimeType: String?, referer: String?) -> Unit,
    downloadedFilesCount: Int = 0,
    onOpenDownloads: () -> Unit = {}
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var crashCount by remember { mutableStateOf(0) }
    var currentDisplayUrl by remember { mutableStateOf(url) }
    var editableUrlText by remember(url) { mutableStateOf(url) }
    var isLoading by remember { mutableStateOf(true) }
    var lastLoadedUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentDisplayUrl) {
        if (currentDisplayUrl.isNotBlank()) {
            editableUrlText = currentDisplayUrl
        }
    }

    fun navigateToEditableUrl() {
        val trimmed = editableUrlText.trim()
        if (trimmed.isNotBlank()) {
            val target = if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
                "https://$trimmed"
            } else {
                trimmed
            }
            currentDisplayUrl = target
            lastLoadedUrl = target
            webViewRef?.loadUrl(target)
        }
    }

    // Screen dimensions and Virtual Mouse position
    var screenWidthPx by remember { mutableFloatStateOf(1920f) }
    var screenHeightPx by remember { mutableFloatStateOf(1080f) }
    var cursorX by remember { mutableFloatStateOf(960f) }
    var cursorY by remember { mutableFloatStateOf(540f) }
    var isCursorClicking by remember { mutableStateOf(false) }

    val cursorStep = 65f
    val scrollStep = 220f.toInt()

    fun dispatchClickAtCursor() {
        val webView = webViewRef ?: return
        if (isWebViewDestroyed(webView)) return
        isCursorClicking = true
        val downTime = SystemClock.uptimeMillis()
        val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, cursorX, cursorY, 0)
        val upEvent = MotionEvent.obtain(downTime, downTime + 45, MotionEvent.ACTION_UP, cursorX, cursorY, 0)
        webView.dispatchTouchEvent(downEvent)
        webView.dispatchTouchEvent(upEvent)
        downEvent.recycle()
        upEvent.recycle()
        postOnMain(120L) { isCursorClicking = false }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == KeyEvent.ACTION_DOWN) {
                    when (native.keyCode) {
                        KeyEvent.KEYCODE_POUND -> {
                            onGoToKeypad()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            cursorX = (cursorX - cursorStep).coerceAtLeast(10f)
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            cursorX = (cursorX + cursorStep).coerceAtMost(screenWidthPx - 10f)
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            // "عند الذهاب للاعلى يبدأ بالتصفح للأعلى"
                            if (cursorY <= 120f) {
                                webViewRef?.scrollBy(0, -scrollStep)
                            } else {
                                cursorY = (cursorY - cursorStep).coerceAtLeast(10f)
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            // "و عند الذهاب للاسفل و تحريك الفار للاسفل اكثر يبداء بالتصفح بالاسفل"
                            if (cursorY >= screenHeightPx - 140f) {
                                webViewRef?.scrollBy(0, scrollStep)
                            } else {
                                cursorY = (cursorY + cursorStep).coerceAtMost(screenHeightPx - 10f)
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            dispatchClickAtCursor()
                            true
                        }
                        KeyEvent.KEYCODE_BACK -> {
                            val wv = webViewRef
                            if (wv != null && wv.canGoBack()) {
                                wv.goBack()
                                true
                            } else {
                                onGoToKeypad()
                                true
                            }
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        screenWidthPx = with(density) { maxWidth.toPx() }
        screenHeightPx = with(density) { maxHeight.toPx() }

        // WebView
        androidx.compose.runtime.key(crashCount) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx.findActivityContext()).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        isFocusable = true
                        isFocusableInTouchMode = true
                        isClickable = true

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            allowFileAccess = true
                            allowContentAccess = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = "Mozilla/5.0 (Linux; Android 10; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Downloader/1.4.3"
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                safeBrowsingEnabled = false
                            }
                        }

                        CookieManager.getInstance().let { cm ->
                            cm.setAcceptCookie(true)
                            cm.setAcceptThirdPartyCookies(this, true)
                        }

                        hardenWebView(this)

                        webChromeClient = object : WebChromeClient() {
                            override fun onPermissionRequest(request: PermissionRequest?) {
                                grantSafeWebPermissions(request)
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                safeDestroyWebView(view, rendererGone = true)
                                if (webViewRef === view) webViewRef = null
                                postOnMain(300L) {
                                    try {
                                        Toast.makeText(ctx, ctx.getString(R.string.mini_downloader_browser_crash), Toast.LENGTH_SHORT).show()
                                    } catch (_: Exception) {}
                                    onGoToKeypad()
                                }
                                return true
                            }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val rawUrl = request?.url?.toString() ?: return false
                            if (com.example.ui.common.DownloadUrlResolver.shouldInterceptAsDownload(rawUrl)) {
                                onRequestDownload(
                                    com.example.ui.common.DownloadUrlResolver.normalizeInitialDownloadUrl(rawUrl),
                                    view?.settings?.userAgentString,
                                    null,
                                    "application/vnd.android.package-archive",
                                    currentDisplayUrl
                                )
                                return true
                            }
                            if (rawUrl.startsWith("intent://", ignoreCase = true)) {
                                val unwrapped = com.example.ui.common.DownloadUrlResolver.unwrapIntentUrl(rawUrl)
                                if (unwrapped != rawUrl && (unwrapped.startsWith("http://", ignoreCase = true) || unwrapped.startsWith("https://", ignoreCase = true))) {
                                    view?.loadUrl(unwrapped)
                                    return true
                                }
                                return true
                            }
                            return false
                        }

                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                val failingUrl = request.url?.toString() ?: ""
                                if (com.example.ui.common.DownloadUrlResolver.shouldInterceptAsDownload(failingUrl)) {
                                    onRequestDownload(
                                        com.example.ui.common.DownloadUrlResolver.normalizeInitialDownloadUrl(failingUrl),
                                        view?.settings?.userAgentString,
                                        null,
                                        "application/vnd.android.package-archive",
                                        currentDisplayUrl
                                    )
                                } else if (failingUrl.startsWith("https://", ignoreCase = true)) {
                                    val fallbackHttp = failingUrl.replaceFirst("https://", "http://", ignoreCase = true)
                                    view?.loadUrl(fallbackHttp)
                                } else if (failingUrl.startsWith("http://", ignoreCase = true)) {
                                    val fallbackHttps = failingUrl.replaceFirst("http://", "https://", ignoreCase = true)
                                    view?.loadUrl(fallbackHttps)
                                }
                            }
                        }

                        override fun onPageStarted(view: WebView?, startedUrl: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, startedUrl, favicon)
                            injectWebViewStabilityShim(view, disableWebGl = true)
                            com.example.ui.common.DownloadUrlResolver.injectPopupAndBlankTargetShim(view)
                            isLoading = true
                            startedUrl?.let { currentDisplayUrl = it }
                        }

                        override fun onPageFinished(view: WebView?, finishedUrl: String?) {
                            super.onPageFinished(view, finishedUrl)
                            injectWebViewStabilityShim(view, disableWebGl = true)
                            com.example.ui.common.DownloadUrlResolver.injectPopupAndBlankTargetShim(view)
                            isLoading = false
                            finishedUrl?.let { currentDisplayUrl = it }

                            // Auto-fast-forward aftvnews countdown and prevent pausing
                            if (finishedUrl?.contains("aftvnews.com", ignoreCase = true) == true ||
                                finishedUrl?.contains("aftv.news", ignoreCase = true) == true
                            ) {
                                view?.evaluateJavascript(
                                    """
                                    (function() {
                                        try {
                                            if (typeof seconds !== 'undefined') {
                                                seconds = 0;
                                            }
                                            if (typeof paused !== 'undefined') {
                                                paused = false;
                                            }
                                            var link = document.querySelector('#redirect_message a') || document.querySelector('#redirect_noscript a');
                                            if (link && link.href) {
                                                window.location.href = link.href;
                                            }
                                        } catch(e) {}
                                    })();
                                    """.trimIndent(),
                                    null
                                )
                            }
                        }
                    }

                    setDownloadListener { dUrl, userAgent, contentDisposition, mimetype, _ ->
                        val effectiveUa = if (!userAgent.isNullOrBlank()) userAgent else settings.userAgentString
                        onRequestDownload(dUrl, effectiveUa, contentDisposition, mimetype, currentDisplayUrl)
                    }

                    val targetUrl = currentDisplayUrl.ifBlank { url }
                    loadUrl(targetUrl)
                    lastLoadedUrl = targetUrl
                    webViewRef = this
                }
            },
            update = { webView ->
                webViewRef = webView
            },
            onRelease = { webView ->
                if (webViewRef === webView) webViewRef = null
                safeDestroyWebView(webView)
            },
            modifier = Modifier.fillMaxSize()
        )
        }

        LaunchedEffect(url) {
            if (url.isNotBlank() && url != lastLoadedUrl) {
                lastLoadedUrl = url
                webViewRef?.loadUrl(url)
            }
        }

        // Top Overlay Bar: Small # Badge, Large Editable URL Bar, Prominent "انتقال" (GO) Button, Refresh, Exit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small "#" Badge button to return to keypad (اشارة # صغيرة)
            TvFocusableCard(
                onClick = onGoToKeypad,
                modifier = Modifier.size(38.dp)
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isFocused) TvAccentGold else Color(0xFFE11D48),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#",
                        color = if (isFocused) Color.Black else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Large Editable URL Input Container (العنوان URL كبير و قابل للتعديل)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = TvAccentGold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "URL",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    BasicTextField(
                        value = editableUrlText,
                        onValueChange = { editableUrlText = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(TvAccentGold),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { navigateToEditableUrl() })
                    )

                    if (editableUrlText.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { editableUrlText = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Prominent "انتقال" (GO) Button to load page (زر انتقال لفتح الصفحة)
            TvFocusableCard(
                onClick = { navigateToEditableUrl() },
                scaleUpRatio = 1.05f,
                modifier = Modifier.height(48.dp)
            ) { isFocused ->
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(
                            if (isFocused) TvAccentGold else Color(0xFF2563EB),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.mini_downloader_go),
                        tint = if (isFocused) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.mini_downloader_go),
                        color = if (isFocused) Color.Black else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Refresh button
            TvFocusableCard(
                onClick = { webViewRef?.reload() },
                modifier = Modifier.size(38.dp)
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isFocused) TvAccentGold else Color.Black.copy(alpha = 0.75f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = if (isFocused) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Downloads Library button in Browser
            TvFocusableCard(
                onClick = onOpenDownloads,
                modifier = Modifier.height(38.dp)
            ) { isFocused ->
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(
                            if (isFocused) TvAccentGold else Color.Black.copy(alpha = 0.75f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Downloads",
                        tint = if (isFocused) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    if (downloadedFilesCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isFocused) Color.Black else Color(0xFFE11D48))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$downloadedFilesCount",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Close to Launcher button
            TvFocusableCard(
                onClick = onCloseToHome,
                modifier = Modifier.size(38.dp)
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isFocused) Color(0xFFEF4444) else Color.Black.copy(alpha = 0.75f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Virtual Mouse Cursor (مؤشر الماوس)
        VirtualMousePointer(
            x = cursorX,
            y = cursorY,
            isClicking = isCursorClicking
        )
    }

    BindWebViewLifecycle(webViewRef)

    DisposableEffect(Unit) {
        onDispose {
            safeDestroyWebView(webViewRef)
            webViewRef = null
        }
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

    // Shadow
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

    // Inner outline/stroke
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
                        Color(0xFF881337), // Crimson/Rose deep tone matching Downloader
                        TvMidnightBackground,
                        Color(0xFF020617)
                    )
                )
            )
            .padding(horizontal = 48.dp, vertical = 24.dp)
    ) {
        // Top Navigation Bar
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
                                text = "العودة للوحة الأرقام",
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
                            // File Info Row
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
                                                val uri = Uri.parse(item.sourceUrl)
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

                            // Actions Row: Install / Re-download / Delete
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Install
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

                                // 2. Re-download
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

                                // 3. Delete
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
