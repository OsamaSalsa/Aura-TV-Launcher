package com.example.ui.games

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.os.SystemClock
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
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

const val EASY_GAMES_URL = "https://easy-games.io/"
const val EASY_GAMES_USER_AGENT = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
class EasyGamesWebView(
    context: Context,
    var onBackAtRoot: (() -> Unit)? = null,
    var onRendererCrashed: ((didCrash: Boolean) -> Unit)? = null
) : WebView(context) {

    var onLoadingStateChanged: ((Boolean) -> Unit)? = null
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
            userAgentString = EASY_GAMES_USER_AGENT
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true
            javaScriptCanOpenWindowsAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }
        }

        hardenWebView(this)

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@EasyGamesWebView, true)
        }

        webViewClient = object : WebViewClient() {
            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                safeDestroyWebView(view, rendererGone = true)
                val didCrash = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    detail?.didCrash() == true
                } else false
                postOnMain(300L) {
                    onRendererCrashed?.invoke(didCrash)
                }
                return true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false // Handle all games navigation internally inside WebView
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                injectWebViewStabilityShim(view, disableWebGl = false)
                onLoadingStateChanged?.invoke(true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectWebViewStabilityShim(view, disableWebGl = false)
                onLoadingStateChanged?.invoke(false)
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
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_BACK -> {
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
fun EasyGamesScreen(
    onBackToHome: () -> Unit
) {
    var crashCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<EasyGamesWebView?>(null) }

    // Virtual Mouse State for TV Remote Control
    var isMouseModeEnabled by remember { mutableStateOf(false) }
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

    BackHandler {
        val webView = webViewRef
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            onBackToHome()
        }
    }

    if (crashCount > 1) {
        LaunchedEffect(Unit) {
            onBackToHome()
        }
        return
    }

    val gameFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isMouseModeEnabled) {
        if (isMouseModeEnabled) {
            try { gameFocusRequester.requestFocus() } catch (_: Exception) {}
        } else {
            try { webViewRef?.requestFocus() } catch (_: Exception) {}
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(gameFocusRequester)
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

        androidx.compose.runtime.key(crashCount) {
            AndroidView(
                factory = { ctx ->
                    EasyGamesWebView(
                        ctx.findActivityContext(),
                        onBackAtRoot = { onBackToHome() },
                        onRendererCrashed = { _ ->
                            onBackToHome()
                        }
                    ).apply {
                        onLoadingStateChanged = { loading ->
                            isLoading = loading
                        }
                        loadUrl(EASY_GAMES_URL)
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

        // Virtual mouse cursor overlay
        if (isMouseModeEnabled) {
            VirtualMousePointer(
                x = cursorX,
                y = cursorY,
                isClicking = isCursorClicking
            )
        }

        // Floating TV Control Bar (Top-Right Pill)
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

    BindWebViewLifecycle(webViewRef)

    DisposableEffect(Unit) {
        onDispose {
            safeDestroyWebView(webViewRef)
            webViewRef = null
        }
    }
}
