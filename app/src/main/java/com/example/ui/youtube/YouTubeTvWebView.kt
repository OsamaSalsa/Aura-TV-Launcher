package com.example.ui.youtube

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
import com.example.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.common.BindWebViewLifecycle
import com.example.ui.common.findActivityContext
import com.example.ui.common.grantSafeWebPermissions
import com.example.ui.common.hardenWebView
import com.example.ui.common.injectWebViewStabilityShim
import com.example.ui.common.isWebViewDestroyed
import com.example.ui.common.postOnMain
import com.example.ui.common.safeDestroyWebView
import com.example.ui.theme.TvAccentGold

/**
 * Modern Android TV User-Agent recognized by YouTube Leanback for full Smart TV interface
 */
const val YOUTUBE_TV_URL = "https://www.youtube.com/tv"
const val YOUTUBE_TV_USER_AGENT = "Mozilla/5.0 (SMART-TV; Linux; Tizen 6.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 TV Safari/537.36"

@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
class TvLeanbackWebView(
    context: Context,
    var onBackAtRoot: (() -> Unit)? = null,
    var onRendererCrashed: ((didCrash: Boolean) -> Unit)? = null
) : WebView(context) {

    var onLoadingStateChanged: ((Boolean) -> Unit)? = null
    var onVideoPlayingChanged: ((Boolean) -> Unit)? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var fullscreenContainer: FrameLayout? = null

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setBackgroundColor(android.graphics.Color.BLACK)
        keepScreenOn = true
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            userAgentString = YOUTUBE_TV_USER_AGENT
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }
        }

        hardenWebView(this)

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@TvLeanbackWebView, true)
        }

        webViewClient = object : WebViewClient() {
            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                safeDestroyWebView(view, rendererGone = true)
                val didCrash = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    detail?.didCrash() == true
                } else false
                postOnMain(250L) {
                    if (onRendererCrashed != null) {
                        onRendererCrashed?.invoke(didCrash)
                    } else {
                        try {
                            android.widget.Toast.makeText(context, context.getString(R.string.youtube_renderer_crash), android.widget.Toast.LENGTH_SHORT).show()
                        } catch (_: Exception) {}
                        onBackAtRoot?.invoke()
                    }
                }
                return true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                return !(url.contains("youtube.com") || url.contains("google.") || url.contains("googlevideo.com"))
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                injectWebViewStabilityShim(view, disableWebGl = true)
                onLoadingStateChanged?.invoke(true)
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                injectWebViewStabilityShim(view, disableWebGl = true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectWebViewStabilityShim(view, disableWebGl = true)
                onLoadingStateChanged?.invoke(false)
                view?.requestFocus()
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
                onVideoPlayingChanged?.invoke(true)
                fullscreenContainer = FrameLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    keepScreenOn = true
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

    fun loadTargetUrl(targetUrl: String) {
        loadUrl(targetUrl)
        requestFocus()
    }
}

@Composable
fun YouTubeTvWebView(
    url: String = YOUTUBE_TV_URL,
    reloadTrigger: Int = 0,
    onBackToCurated: () -> Unit,
    onVideoPlayingChanged: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<TvLeanbackWebView?>(null) }
    var lastLoadedUrl by remember { mutableStateOf<String?>(null) }
    var crashCount by remember { mutableStateOf(0) }
    val ctxForToast = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(url) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (url != lastLoadedUrl) {
            lastLoadedUrl = url
            wv.loadTargetUrl(url)
        }
    }

    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger > 0) {
            lastLoadedUrl = url
            webViewRef?.loadTargetUrl(url)
        }
    }

    LaunchedEffect(webViewRef) {
        val wv = webViewRef ?: return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(2500)
            if (isWebViewDestroyed(wv)) break
            try {
                if (wv.isAttachedToWindow && !isWebViewDestroyed(wv)) {
                    wv.evaluateJavascript(
                        "(function(){var v=document.querySelector('video');return !!(v && !v.paused && !v.ended && v.readyState>2);})();"
                    ) { result ->
                        val playing = result?.trim() == "true"
                        onVideoPlayingChanged?.invoke(playing)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        androidx.compose.runtime.key(crashCount) {
            AndroidView(
                factory = { ctx ->
                    TvLeanbackWebView(
                        ctx.findActivityContext(),
                        onBackAtRoot = { onBackToCurated() },
                        onRendererCrashed = { _ ->
                            try {
                                android.widget.Toast.makeText(
                                    ctxForToast,
                                    ctxForToast.getString(R.string.youtube_renderer_crash),
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } catch (_: Exception) {}
                            onBackToCurated()
                        }
                    ).apply {
                        onLoadingStateChanged = { loading ->
                            isLoading = loading
                        }
                        this.onVideoPlayingChanged = onVideoPlayingChanged
                        if (crashCount > 0) {
                            try { clearCache(false) } catch (_: Exception) {}
                        }
                        lastLoadedUrl = url
                        loadTargetUrl(url)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    webView.onVideoPlayingChanged = onVideoPlayingChanged
                    webViewRef = webView
                },
                onRelease = { webView ->
                    safeDestroyWebView(webView)
                    if (webViewRef === webView) {
                        webViewRef = null
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
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
            onVideoPlayingChanged?.invoke(false)
            safeDestroyWebView(webViewRef)
            webViewRef = null
        }
    }
}
