package com.example.ui.games

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.R
import com.example.ui.common.TvFocusableCard
import com.example.ui.downloader.VirtualMousePointer
import com.example.ui.theme.TvAccentBlue
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
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

const val AURA_STORE_URL = "https://auratv.freedev.app/"
const val AURA_STORE_USER_AGENT = "Mozilla/5.0 (Linux; Android 10; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Downloader/1.4.3"

enum class AuraStoreSubView {
    STORE_BROWSER,
    DOWNLOADS_MANAGER
}

sealed class AuraDownloadState {
    object Idle : AuraDownloadState()
    data class Downloading(val fileName: String, val downloadedBytes: Long, val totalBytes: Long, val percent: Int) : AuraDownloadState()
    data class Completed(val file: File, val fileName: String, val fileSize: Long) : AuraDownloadState()
    data class Error(val message: String, val url: String? = null) : AuraDownloadState()
}

data class DownloadedApkItem(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long
)

private fun sanitizeFileName(name: String): String {
    val clean = name.replace(Regex("""[\\/:*?"<>|]"""), "_").trim()
    return if (clean.isEmpty()) "app_${System.currentTimeMillis()}.apk" else clean
}

private fun extractApkFileName(url: String, contentDisposition: String?, mimeType: String?): String {
    if (!contentDisposition.isNullOrBlank()) {
        val cdMatcher = Regex("""filename\*?=(?:UTF-8'')?["']?([^"';]+)["']?""", RegexOption.IGNORE_CASE)
            .find(contentDisposition)
        if (cdMatcher != null) {
            val name = cdMatcher.groupValues[1].trim()
            if (name.isNotEmpty()) {
                val s = sanitizeFileName(name)
                return if (s.endsWith(".apk", ignoreCase = true)) s else "$s.apk"
            }
        }
    }

    val apkMatcher = Regex("""/([a-zA-Z0-9_\-\.]+?)(?:-apk|\.apk)(?:\.[0-9]+)?(?:/|\?|#|$)""", RegexOption.IGNORE_CASE)
        .find(url)
    if (apkMatcher != null) {
        val baseName = apkMatcher.groupValues[1].trim()
        if (baseName.isNotEmpty()) {
            return sanitizeFileName("$baseName.apk")
        }
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
        Toast.makeText(context, context.getString(R.string.aurastore_installer_error).format(e.message), Toast.LENGTH_LONG).show()
    }
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun AuraStoreScreen(
    onBackToHome: () -> Unit,
    appLanguage: String = "en"
) {
    val context = LocalContext.current
    var currentSubView by remember { mutableStateOf(AuraStoreSubView.STORE_BROWSER) }
    var crashCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    
    var downloadState by remember { mutableStateOf<AuraDownloadState>(AuraDownloadState.Idle) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    // Downloaded files list state
    var downloadedFiles by remember { mutableStateOf<List<DownloadedApkItem>>(emptyList()) }

    fun refreshDownloadedFiles() {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        if (downloadDir.exists()) {
            val apks = downloadDir.listFiles { f ->
                f.isFile && (f.name.endsWith(".apk", ignoreCase = true) || f.extension.equals("apk", ignoreCase = true))
            }?.map {
                DownloadedApkItem(
                    file = it,
                    name = it.name,
                    sizeBytes = it.length(),
                    lastModified = it.lastModified()
                )
            }?.sortedByDescending { it.lastModified } ?: emptyList()
            downloadedFiles = apks
        } else {
            downloadedFiles = emptyList()
        }
    }

    LaunchedEffect(Unit) {
        refreshDownloadedFiles()
    }
    
    // Virtual Mouse State for TV Remote Control
    var isMouseModeEnabled by remember { mutableStateOf(false) }

    // Download engine identical to Mini Downloader
    fun startDownload(
        downloadUrl: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        referer: String?
    ) {
        downloadJob?.cancel()
        downloadJob = scope.launch(Dispatchers.IO) {
            var currentUrl = downloadUrl
            var currentReferer = referer ?: AURA_STORE_URL
            val browserUserAgent = if (!userAgent.isNullOrBlank()) {
                userAgent
            } else {
                AURA_STORE_USER_AGENT
            }

            withContext(Dispatchers.Main) {
                downloadState = AuraDownloadState.Downloading(
                    context.getString(R.string.aurastore_connecting),
                    0L,
                    -1L,
                    -1
                )
            }

            try {
                var connection: HttpURLConnection? = null
                var redirects = 0
                val maxRedirects = 12

                while (redirects < maxRedirects) {
                    val urlObj = URL(currentUrl)
                    val conn = (urlObj.openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false
                        connectTimeout = 20000
                        readTimeout = 30000
                        setRequestProperty("User-Agent", browserUserAgent)
                        setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,application/vnd.android.package-archive,*/*;q=0.8")
                        setRequestProperty("Accept-Language", "$appLanguage,en;q=0.9")
                        setRequestProperty("Referer", currentReferer)
                        setRequestProperty("Connection", "keep-alive")

                        val cookies = CookieManager.getInstance().getCookie(currentUrl)
                        if (!cookies.isNullOrBlank()) {
                            setRequestProperty("Cookie", cookies)
                        }
                    }

                    val code = conn.responseCode

                    conn.headerFields["Set-Cookie"]?.forEach { cookieHeader ->
                        CookieManager.getInstance().setCookie(currentUrl, cookieHeader)
                    }

                    if (code in 300..399) {
                        val newLoc = conn.getHeaderField("Location")
                        conn.disconnect()
                        if (newLoc.isNullOrBlank()) break
                        currentReferer = currentUrl
                        currentUrl = if (newLoc.startsWith("http://", ignoreCase = true) || newLoc.startsWith("https://", ignoreCase = true)) {
                            newLoc
                        } else {
                            URL(urlObj, newLoc).toString()
                        }
                        redirects++
                    } else if (code in 200..299) {
                        val contentType = conn.contentType
                        val finalDisposition = conn.getHeaderField("Content-Disposition")
                        val isApkDirect = currentUrl.endsWith(".apk", ignoreCase = true) ||
                                finalDisposition?.contains(".apk", ignoreCase = true) == true ||
                                contentType?.contains("vnd.android.package-archive", ignoreCase = true) == true ||
                                contentType?.contains("octet-stream", ignoreCase = true) == true

                        if (!isApkDirect && contentType?.startsWith("text/html", ignoreCase = true) == true) {
                            val htmlBody = try {
                                conn.inputStream.bufferedReader().use { it.readText() }
                            } catch (_: Exception) { "" }
                            conn.disconnect()

                            val targetLink = Regex("""href=["']([^"']+\.apk[^"']*)["']""", RegexOption.IGNORE_CASE).find(htmlBody)?.groupValues?.get(1)
                                ?: Regex("""id=["']redirect_message["'][^>]*href=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(htmlBody)?.groupValues?.get(1)
                                ?: Regex("""window\.location(?:\.href)?\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(htmlBody)?.groupValues?.get(1)

                            if (!targetLink.isNullOrBlank()) {
                                currentReferer = currentUrl
                                currentUrl = if (targetLink.startsWith("http://", ignoreCase = true) || targetLink.startsWith("https://", ignoreCase = true)) {
                                    targetLink
                                } else {
                                    URL(urlObj, targetLink).toString()
                                }
                                redirects++
                                continue
                            } else {
                                withContext(Dispatchers.Main) {
                                    downloadState = AuraDownloadState.Idle
                                    webViewRef?.loadUrl(currentUrl)
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

                val conn = connection ?: throw Exception(context.getString(R.string.aurastore_download_failed))
                val finalDisposition = conn.getHeaderField("Content-Disposition") ?: contentDisposition
                val contentType = conn.contentType ?: mimeType
                val guessedName = extractApkFileName(currentUrl, finalDisposition, contentType)

                val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
                if (!downloadDir.exists()) downloadDir.mkdirs()
                val outputFile = File(downloadDir, guessedName)

                val contentLength = conn.contentLength.toLong()
                var downloadedBytes = 0L

                withContext(Dispatchers.Main) {
                    downloadState = AuraDownloadState.Downloading(guessedName, 0L, contentLength, 0)
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
                                downloadState = AuraDownloadState.Downloading(guessedName, downloadedBytes, contentLength, percent)
                            }
                        }
                        outputStream.flush()
                    }
                }
                conn.disconnect()

                withContext(Dispatchers.Main) {
                    refreshDownloadedFiles()
                    downloadState = AuraDownloadState.Completed(outputFile, guessedName, outputFile.length())
                    installApk(context, outputFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    downloadState = AuraDownloadState.Error(e.message ?: context.getString(R.string.aurastore_download_error_generic))
                }
            }
        }
    }

    // Virtual Mouse State for TV Remote Control
    var screenWidthPx by remember { mutableFloatStateOf(1920f) }
    var screenHeightPx by remember { mutableFloatStateOf(1080f) }
    var cursorX by remember { mutableFloatStateOf(960f) }
    var cursorY by remember { mutableFloatStateOf(540f) }
    var isCursorClicking by remember { mutableStateOf(false) }

    val cursorStep = 65f
    val scrollStep = 220f.toInt()

    fun dispatchClickAtCursor() {
        val webView = webViewRef ?: return
        isCursorClicking = true
        val downTime = SystemClock.uptimeMillis()
        val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, cursorX, cursorY, 0)
        val upEvent = MotionEvent.obtain(downTime, downTime + 45, MotionEvent.ACTION_UP, cursorX, cursorY, 0)
        webView.dispatchTouchEvent(downEvent)
        webView.dispatchTouchEvent(upEvent)
        downEvent.recycle()
        upEvent.recycle()
        webView.postDelayed({ isCursorClicking = false }, 120)
    }

    // Remote Back handling
    BackHandler {
        if (currentSubView == AuraStoreSubView.DOWNLOADS_MANAGER) {
            currentSubView = AuraStoreSubView.STORE_BROWSER
        } else {
            val wv = webViewRef
            if (wv != null && wv.canGoBack()) {
                wv.goBack()
            } else {
                onBackToHome()
            }
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (currentSubView) {
            AuraStoreSubView.STORE_BROWSER -> {
                val storeFocusRequester = remember { FocusRequester() }
                LaunchedEffect(isMouseModeEnabled) {
                    if (isMouseModeEnabled) {
                        try { storeFocusRequester.requestFocus() } catch (_: Exception) {}
                    } else {
                        try { webViewRef?.requestFocus() } catch (_: Exception) {}
                    }
                }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(storeFocusRequester)
                        .focusable()
                        .onKeyEvent { keyEvent ->
                            val native = keyEvent.nativeKeyEvent
                            if (native.action == KeyEvent.ACTION_DOWN) {
                                if (native.keyCode == KeyEvent.KEYCODE_MENU || native.keyCode == KeyEvent.KEYCODE_SETTINGS) {
                                    isMouseModeEnabled = !isMouseModeEnabled
                                    true
                                } else if (isMouseModeEnabled) {
                                    when (native.keyCode) {
                                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                                            cursorX = (cursorX - cursorStep).coerceAtLeast(10f)
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                            cursorX = (cursorX + cursorStep).coerceAtMost(screenWidthPx - 10f)
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_UP -> {
                                            if (cursorY <= 120f) {
                                                webViewRef?.scrollBy(0, -scrollStep)
                                            } else {
                                                cursorY = (cursorY - cursorStep).coerceAtLeast(10f)
                                            }
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_DOWN -> {
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
                                        else -> false
                                    }
                                } else {
                                    false
                                }
                            } else false
                        }
                ) {
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    screenWidthPx = with(density) { maxWidth.toPx() }
                    screenHeightPx = with(density) { maxHeight.toPx() }

                    // Fullscreen Webview without any URL address bar
                    androidx.compose.runtime.key(crashCount) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    isFocusable = !isMouseModeEnabled
                                    isFocusableInTouchMode = !isMouseModeEnabled
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
                                        userAgentString = AURA_STORE_USER_AGENT
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            safeBrowsingEnabled = false
                                        }
                                    }

                                    CookieManager.getInstance().let { cm ->
                                        cm.setAcceptCookie(true)
                                        cm.setAcceptThirdPartyCookies(this@apply, true)
                                        try {
                                            cm.setCookie("https://auratv.freedev.app/", "lang=$appLanguage; path=/")
                                            cm.setCookie("https://auratv.freedev.app/", "locale=$appLanguage; path=/")
                                        } catch (_: Exception) {}
                                    }

                                    setLayerType(View.LAYER_TYPE_NONE, null)

                                    webViewClient = object : WebViewClient() {
                                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                            (view?.parent as? ViewGroup)?.removeView(view)
                                            try {
                                                view?.destroy()
                                            } catch (_: Exception) {}
                                            postDelayed({
                                                crashCount++
                                            }, 300)
                                            return true
                                        }

                                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                            val targetUrl = request?.url?.toString() ?: return false
                                            val isApk = targetUrl.endsWith(".apk", ignoreCase = true) ||
                                                        targetUrl.contains(".apk?", ignoreCase = true) ||
                                                        targetUrl.contains(".apk&", ignoreCase = true) ||
                                                        targetUrl.contains("-apk.", ignoreCase = true)
                                            if (isApk) {
                                                startDownload(
                                                    targetUrl,
                                                    view?.settings?.userAgentString,
                                                    null,
                                                    "application/vnd.android.package-archive",
                                                    AURA_STORE_URL
                                                )
                                                return true
                                            }
                                            return false
                                        }

                                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                                            super.onReceivedError(view, request, error)
                                            if (request?.isForMainFrame == true) {
                                                val failingUrl = request.url?.toString() ?: ""
                                                val isApk = failingUrl.endsWith(".apk", ignoreCase = true) ||
                                                            failingUrl.contains(".apk?", ignoreCase = true) ||
                                                            failingUrl.contains(".apk&", ignoreCase = true)
                                                if (isApk) {
                                                    startDownload(
                                                        failingUrl,
                                                        view?.settings?.userAgentString,
                                                        null,
                                                        "application/vnd.android.package-archive",
                                                        AURA_STORE_URL
                                                    )
                                                }
                                            }
                                        }

                                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                            super.onPageStarted(view, url, favicon)
                                            isLoading = true
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isLoading = false
                                            if (!isMouseModeEnabled) {
                                                view?.post { view.requestFocus() }
                                            }
                                        }
                                    }

                                    setDownloadListener { dUrl, userAgent, contentDisposition, mimetype, _ ->
                                        val effectiveUa = if (!userAgent.isNullOrBlank()) userAgent else settings.userAgentString
                                        startDownload(dUrl, effectiveUa, contentDisposition, mimetype, AURA_STORE_URL)
                                    }

                                    val langHeaders = mapOf("Accept-Language" to "$appLanguage,en;q=0.9")
                                    loadUrl(AURA_STORE_URL, langHeaders)
                                    webViewRef = this
                                }
                            },
                            update = { webView ->
                                webViewRef = webView
                                webView.isFocusable = !isMouseModeEnabled
                                webView.isFocusableInTouchMode = !isMouseModeEnabled
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Virtual mouse cursor overlay
                    if (isMouseModeEnabled) {
                        VirtualMousePointer(
                            x = cursorX,
                            y = cursorY,
                            isClicking = isCursorClicking
                        )
                    }

                    // Floating TV Control Bar (Top-Right Pill, No URL Address Bar)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF090D1A).copy(alpha = 0.88f))
                            .border(1.dp, TvAccentGold.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Downloads Library Button with Count Badge
                        TvFocusableCard(
                            onClick = {
                                refreshDownloadedFiles()
                                currentSubView = AuraStoreSubView.DOWNLOADS_MANAGER
                            },
                            scaleUpRatio = 1.08f,
                            modifier = Modifier.height(36.dp)
                        ) { isFocused ->
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(
                                        if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = stringResource(R.string.aurastore_downloads_btn),
                                    tint = if (isFocused) Color.Black else TvAccentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${stringResource(R.string.aurastore_downloads_btn)} (${downloadedFiles.size})",
                                    color = if (isFocused) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Toggle Virtual Mouse Mode
                        TvFocusableCard(
                            onClick = { isMouseModeEnabled = !isMouseModeEnabled },
                            modifier = Modifier.size(36.dp)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isFocused) TvAccentGold else if (isMouseModeEnabled) Color(0xFF6366F1) else Color.White.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mouse,
                                    contentDescription = "Mouse Toggle",
                                    tint = if (isFocused) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = TvAccentGold,
                            strokeWidth = 3.dp,
                            modifier = Modifier
                                .size(42.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            AuraStoreSubView.DOWNLOADS_MANAGER -> {
                // Downloaded Files Library View ("صفحة بها الملفات التي تم تنزيلها")
                DownloadsManagerScreen(
                    files = downloadedFiles,
                    onInstallFile = { item -> installApk(context, item.file) },
                    onDeleteFile = { item ->
                        try {
                            if (item.file.exists()) item.file.delete()
                            refreshDownloadedFiles()
                            Toast.makeText(context, context.getString(R.string.aurastore_file_deleted), Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onClearAllFiles = {
                        try {
                            downloadedFiles.forEach { if (it.file.exists()) it.file.delete() }
                            refreshDownloadedFiles()
                            Toast.makeText(context, context.getString(R.string.aurastore_all_cleared), Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onBackToBrowser = { currentSubView = AuraStoreSubView.STORE_BROWSER }
                )
            }
        }
    }

    // 1. Download In-Progress Dialog
    when (val state = downloadState) {
        is AuraDownloadState.Downloading -> {
            Dialog(onDismissRequest = { /* Prevent dismissal during download */ }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 18.dp,
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
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.aurastore_downloading),
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
                                text = "${state.percent}% (${String.format(Locale.US, "%.1f", downloadedMb)} MB / ${String.format(Locale.US, "%.1f", totalMb)} MB)",
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
                                text = "${String.format(Locale.US, "%.1f", downloadedMb)} MB",
                                color = TvTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        TvFocusableCard(
                            onClick = {
                                downloadJob?.cancel()
                                downloadState = AuraDownloadState.Idle
                            },
                            modifier = Modifier.height(42.dp).fillMaxWidth(0.6f)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isFocused) Color.Red.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.aurastore_cancel_download), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Post-Download Dialog ("يعطي اقتراح تثبيت البرنامج و حذفه بعد التنزيل")
        is AuraDownloadState.Completed -> {
            Dialog(onDismissRequest = { downloadState = AuraDownloadState.Idle }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 20.dp,
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
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.aurastore_download_complete),
                            color = TvTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val sizeMb = state.fileSize / (1024f * 1024f)
                        Text(
                            text = "${state.fileName} (${String.format(Locale.US, "%.1f", sizeMb)} MB)",
                            color = TvAccentGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.aurastore_post_download_desc),
                            color = TvTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(22.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Delete file button
                            TvFocusableCard(
                                onClick = {
                                    try {
                                        if (state.file.exists()) state.file.delete()
                                        refreshDownloadedFiles()
                                        Toast.makeText(context, context.getString(R.string.aurastore_file_deleted), Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    downloadState = AuraDownloadState.Idle
                                },
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isFocused) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.35f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.aurastore_delete_file), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }

                            // 2. Keep file button
                            TvFocusableCard(
                                onClick = { downloadState = AuraDownloadState.Idle },
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stringResource(R.string.aurastore_keep_file), color = if (isFocused) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            // 3. Re-install button
                            TvFocusableCard(
                                onClick = { installApk(context, state.file) },
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isFocused) TvAccentGold else Color(0xFF334155),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = if (isFocused) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.aurastore_install_again), color = if (isFocused) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Download Error Dialog
        is AuraDownloadState.Error -> {
            Dialog(onDismissRequest = { downloadState = AuraDownloadState.Idle }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 16.dp,
                    modifier = Modifier.width(420.dp).padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(46.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = stringResource(R.string.aurastore_download_error), color = TvTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.message, color = TvTextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(20.dp))
                        TvFocusableCard(
                            onClick = { downloadState = AuraDownloadState.Idle },
                            modifier = Modifier.height(42.dp).fillMaxWidth(0.5f)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.aurastore_ok), color = if (isFocused) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        else -> {}
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewRef?.stopLoading()
                webViewRef?.destroy()
                webViewRef = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

/**
 * Downloaded Files Manager Screen ("صفحة بها الملفات التي تم تنزيلها")
 */
@Composable
fun DownloadsManagerScreen(
    files: List<DownloadedApkItem>,
    onInstallFile: (DownloadedApkItem) -> Unit,
    onDeleteFile: (DownloadedApkItem) -> Unit,
    onClearAllFiles: () -> Unit,
    onBackToBrowser: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd • HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E1B4B),
                        TvMidnightBackground,
                        Color(0xFF030712)
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
                    onClick = onBackToBrowser,
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
                            contentDescription = stringResource(R.string.aurastore_back_to_store),
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
                        .background(Color(0xFF6366F1)),
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
                        text = stringResource(R.string.aurastore_manager_title),
                        color = TvTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.aurastore_manager_desc),
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
                        Text(stringResource(R.string.aurastore_clear_all), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                        text = stringResource(R.string.aurastore_no_files),
                        color = TvTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.aurastore_no_files_desc),
                        color = TvTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TvFocusableCard(
                        onClick = onBackToBrowser,
                        modifier = Modifier.height(44.dp).width(180.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isFocused) TvAccentGold else Color(0xFF6366F1), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.aurastore_browse_now), color = if (isFocused) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(files, key = { it.file.absolutePath }) { item ->
                    DownloadedFileCard(
                        item = item,
                        formattedDate = dateFormat.format(Date(item.lastModified)),
                        onInstall = { onInstallFile(item) },
                        onDelete = { onDeleteFile(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadedFileCard(
    item: DownloadedApkItem,
    formattedDate: String,
    onInstall: () -> Unit,
    onDelete: () -> Unit
) {
    val sizeMb = item.sizeBytes / (1024f * 1024f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = item.name,
                        color = TvTextPrimary,
                        fontSize = 15.sp,
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
                            text = "${String.format(Locale.US, "%.1f", sizeMb)} MB",
                            color = TvAccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "•",
                            color = TvTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = formattedDate,
                            color = TvTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Action Buttons: Install & Delete
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Install APK Button
                TvFocusableCard(
                    onClick = onInstall,
                    scaleUpRatio = 1.05f,
                    modifier = Modifier.height(40.dp)
                ) { isFocused ->
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(
                                if (isFocused) TvAccentGold else Color(0xFF10B981),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = if (isFocused) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.aurastore_install),
                            color = if (isFocused) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Delete APK Button
                TvFocusableCard(
                    onClick = onDelete,
                    scaleUpRatio = 1.05f,
                    modifier = Modifier.height(40.dp)
                ) { isFocused ->
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(
                                if (isFocused) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.25f),
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.aurastore_delete),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
