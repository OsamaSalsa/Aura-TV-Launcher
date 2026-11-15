package com.example.ui.games

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.example.ui.downloader.VirtualMousePointer
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

private const val BOOKMARK_DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
class CustomBookmarkWebView(
    context: Context,
    var onBackAtRoot: (() -> Unit)? = null,
    var onRendererCrashed: (() -> Unit)? = null
) : WebView(context) {

    var onLoadingStateChanged: ((Boolean) -> Unit)? = null
    var onErrorChanged: ((String?) -> Unit)? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var fullscreenContainer: FrameLayout? = null

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            userAgentString = BOOKMARK_DESKTOP_USER_AGENT
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }
        }

        hardenWebView(this)

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@CustomBookmarkWebView, true)
        }

        webViewClient = object : WebViewClient() {
            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                safeDestroyWebView(view, rendererGone = true)
                postOnMain(250L) { onRendererCrashed?.invoke() }
                return true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                injectWebViewStabilityShim(view, disableWebGl = false)
                onErrorChanged?.invoke(null)
                onLoadingStateChanged?.invoke(true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectWebViewStabilityShim(view, disableWebGl = false)
                onLoadingStateChanged?.invoke(false)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    onLoadingStateChanged?.invoke(false)
                    onErrorChanged?.invoke(error?.description?.toString() ?: "Page load error")
                }
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                grantSafeWebPermissions(request)
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                fullscreenContainer = FrameLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    addView(view)
                }
                (parent as? ViewGroup)?.addView(fullscreenContainer)
                visibility = View.INVISIBLE
            }

            override fun onHideCustomView() {
                if (customView == null) return
                fullscreenContainer?.let { container ->
                    container.removeAllViews()
                    (container.parent as? ViewGroup)?.removeView(container)
                }
                fullscreenContainer = null
                customView = null
                customViewCallback?.onCustomViewHidden()
                visibility = View.VISIBLE
                requestFocus()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (customView != null) {
                webChromeClient?.onHideCustomView()
                return true
            }
            if (canGoBack()) {
                goBack()
                return true
            } else {
                onBackAtRoot?.invoke()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDetachedFromWindow() {
        fullscreenContainer?.let { container ->
            container.removeAllViews()
            (container.parent as? ViewGroup)?.removeView(container)
        }
        fullscreenContainer = null
        customView = null
        customViewCallback = null
        super.onDetachedFromWindow()
    }
}

@Composable
fun CustomWebPortalScreen(
    title: String,
    url: String,
    onBackToHome: () -> Unit
) {
    val normalizedUrl = remember(url) {
        val trimmed = url.trim()
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    var crashCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var webViewRef by remember { mutableStateOf<CustomBookmarkWebView?>(null) }

    var isMouseModeEnabled by remember { mutableStateOf(true) }
    var screenWidthPx by remember { mutableFloatStateOf(1920f) }
    var screenHeightPx by remember { mutableFloatStateOf(1080f) }
    var cursorX by remember { mutableFloatStateOf(960f) }
    var cursorY by remember { mutableFloatStateOf(540f) }
    var isCursorClicking by remember { mutableStateOf(false) }

    val cursorStep = 65f
    val scrollStep = 220

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

    BackHandler {
        val webView = webViewRef
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            onBackToHome()
        }
    }

    if (crashCount > 1) {
        LaunchedEffect(Unit) { onBackToHome() }
        return
    }

    val portalFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isMouseModeEnabled) {
        if (isMouseModeEnabled) {
            try { portalFocusRequester.requestFocus() } catch (_: Exception) {}
        } else {
            try { webViewRef?.requestFocus() } catch (_: Exception) {}
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(portalFocusRequester)
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
                                if (cursorY <= 120f) webViewRef?.scrollBy(0, -scrollStep)
                                else cursorY = (cursorY - cursorStep).coerceAtLeast(10f)
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                if (cursorY >= screenHeightPx - 140f) webViewRef?.scrollBy(0, scrollStep)
                                else cursorY = (cursorY + cursorStep).coerceAtMost(screenHeightPx - 10f)
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                dispatchClickAtCursor()
                                true
                            }
                            else -> false
                        }
                    } else false
                } else false
            }
    ) {
        val density = LocalDensity.current
        screenWidthPx = with(density) { maxWidth.toPx() }
        screenHeightPx = with(density) { maxHeight.toPx() }

        key(crashCount, normalizedUrl) {
            AndroidView(
                factory = { ctx ->
                    CustomBookmarkWebView(
                        ctx.findActivityContext(),
                        onBackAtRoot = { onBackToHome() },
                        onRendererCrashed = { crashCount++ }
                    ).apply {
                        onLoadingStateChanged = { isLoading = it }
                        onErrorChanged = { loadError = it }
                        loadUrl(normalizedUrl)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    webViewRef = webView
                    webView.isFocusable = !isMouseModeEnabled
                    webView.isFocusableInTouchMode = !isMouseModeEnabled
                },
                onRelease = { webView ->
                    if (webViewRef === webView) webViewRef = null
                    safeDestroyWebView(webView)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isMouseModeEnabled) {
            VirtualMousePointer(
                x = cursorX,
                y = cursorY,
                isClicking = isCursorClicking
            )
        }

        // Top Floating Control Pill
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF090D1A).copy(alpha = 0.88f))
                .border(1.dp, TvAccentGold.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = TvAccentGold,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                color = TvTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            TvFocusableCard(
                onClick = { isMouseModeEnabled = !isMouseModeEnabled },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                scaleUpRatio = 1.06f
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .background(
                            if (isFocused) TvAccentGold
                            else if (isMouseModeEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            else Color.White.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mouse,
                        contentDescription = stringResource(R.string.bookmark_toggle_mouse),
                        tint = if (isFocused) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            TvFocusableCard(
                onClick = { webViewRef?.reload() },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                scaleUpRatio = 1.06f
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .background(
                            if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.action_retry),
                        tint = if (isFocused) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            TvFocusableCard(
                onClick = onBackToHome,
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                scaleUpRatio = 1.06f
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .background(
                            if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_cancel),
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
        } else if (loadError != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0B1120).copy(alpha = 0.92f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = loadError ?: "",
                    color = TvTextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                TvFocusableCard(
                    onClick = { webViewRef?.loadUrl(normalizedUrl) },
                    shape = RoundedCornerShape(12.dp),
                    cardBackgroundColor = TvAccentGold.copy(alpha = 0.18f),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_retry),
                        color = TvTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }

    BindWebViewLifecycle(webViewRef)

    DisposableEffect(Unit) {
        onDispose {
            safeDestroyWebView(webViewRef)
            webViewRef = null
        }
    }
}
