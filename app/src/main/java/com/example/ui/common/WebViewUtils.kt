package com.example.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.util.Collections
import java.util.WeakHashMap

private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
private val destroyedWebViews: MutableSet<WebView> =
    Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap()))

fun postOnMain(delayMs: Long = 0L, action: () -> Unit) {
    if (delayMs <= 0L) {
        mainHandler.post(action)
    } else {
        mainHandler.postDelayed(action, delayMs)
    }
}

/**
 * Unwraps localized/configuration ContextWrappers back to the hosting Activity context
 * so WebView receives a valid window-backed context.
 */
fun Context.findActivityContext(): Context {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return this
}

/**
 * Grants safe web permissions while blocking RESOURCE_PROTECTED_MEDIA_ID probes that trigger
 * repeated MediaDrm / DrmHalHidl failures on devices and emulators without hardware DRM.
 */
fun grantSafeWebPermissions(request: PermissionRequest?) {
    if (request == null) return
    val allowed = request.resources
        .filter { it != PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID }
        .toTypedArray()
    try {
        if (allowed.isNotEmpty()) {
            request.grant(allowed)
        } else {
            request.deny()
        }
    } catch (_: Exception) {}
}

private val webViewCacheLock = Any()
@Volatile
private var webViewCachePrepared = false

/**
 * Validates cacheDir and cleans up any broken non-directory entries along Chromium's cache path
 * strictly before the first WebView initializes so SimpleBackendImpl never races or hits EACCES.
 */
fun ensureWebViewCacheDirs(context: Context) {
    if (webViewCachePrepared) return
    synchronized(webViewCacheLock) {
        if (webViewCachePrepared) return
        try {
            val cacheRoot = context.cacheDir ?: return
            if (!cacheRoot.exists()) {
                cacheRoot.mkdirs()
            }
            cacheRoot.setReadable(true, true)
            cacheRoot.setWritable(true, true)
            cacheRoot.setExecutable(true, true)

            val webViewRoot = File(cacheRoot, "WebView")
            if (webViewRoot.exists() && !webViewRoot.isDirectory) {
                webViewRoot.delete()
            }
            // If an interrupted run left an unreadable/unwritable Code Cache leaf, reset it cleanly before Chromium starts
            val codeCacheJs = File(cacheRoot, "WebView/Default/HTTP Cache/Code Cache/js")
            if (codeCacheJs.exists() && (!codeCacheJs.isDirectory || !codeCacheJs.canWrite() || !codeCacheJs.canRead())) {
                codeCacheJs.deleteRecursively()
            }
            val codeCacheWasm = File(cacheRoot, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (codeCacheWasm.exists() && (!codeCacheWasm.isDirectory || !codeCacheWasm.canWrite() || !codeCacheWasm.canRead())) {
                codeCacheWasm.deleteRecursively()
            }
            webViewCachePrepared = true
        } catch (_: Exception) {}
    }
}

fun isWebViewDestroyed(webView: WebView?): Boolean =
    webView == null || destroyedWebViews.contains(webView)

/**
 * Protects active WebView renderers from OS LMK kills (code -1) and GPU rendernode crashes
 * on emulators/TV boxes without /dev/dri/renderD128 or hardware MediaDrm factories.
 */
fun hardenWebView(webView: WebView) {
    try {
        // LAYER_TYPE_NONE allows HTML5 <video> hardware surfaces to render video frames
        // while avoiding allocating an extra offscreen hardware layer buffer.
        webView.setLayerType(View.LAYER_TYPE_NONE, null)
    } catch (_: Exception) {}
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
        } catch (_: Exception) {}
        try {
            webView.settings.safeBrowsingEnabled = false
        } catch (_: Exception) {}
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            webView.webViewRenderProcessClient = object : android.webkit.WebViewRenderProcessClient() {
                override fun onRenderProcessUnresponsive(
                    view: WebView,
                    renderer: android.webkit.WebViewRenderProcess?
                ) {
                    // Allow renderer a grace window to finish heavy JS/video work instead of failing
                }

                override fun onRenderProcessResponsive(
                    view: WebView,
                    renderer: android.webkit.WebViewRenderProcess?
                ) {
                    // Renderer recovered normally
                }
            }
        } catch (_: Exception) {}
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            webView.settings.offscreenPreRaster = false
        } catch (_: Exception) {}
    }
    try {
        webView.settings.setGeolocationEnabled(false)
    } catch (_: Exception) {}
}

/**
 * Injects a defensive JS shim into WebView pages to:
 * 1. Stub navigator.requestMediaKeySystemAccess so Chromium never invokes native MediaDrm / DrmHalHidl.
 * 2. Stub navigator.serviceWorker.register so sites do not spawn background ServiceWorker processes.
 * 3. Filter out unsupported AV1 / HEVC / 10-bit HDR video codecs in MediaSource & mediaCapabilities
 *    so YouTube TV always selects standard H.264 (avc1) / 8-bit VP9 with full video + audio.
 * 4. Force low-power WebGL attributes without failing on performance caveats.
 */
fun injectWebViewStabilityShim(webView: WebView?, disableWebGl: Boolean = false) {
    if (webView == null || isWebViewDestroyed(webView)) return
    val webGlPatch = if (disableWebGl) {
        """
        if (typeof HTMLCanvasElement !== 'undefined') {
            var origGetCtx = HTMLCanvasElement.prototype.getContext;
            HTMLCanvasElement.prototype.getContext = function(type, attrs) {
                if (type === 'webgl' || type === 'experimental-webgl' || type === 'webgl2' || type === 'webgpu') {
                    return null;
                }
                return origGetCtx.call(this, type, attrs);
            };
        }
        """.trimIndent()
    } else {
        """
        if (typeof HTMLCanvasElement !== 'undefined') {
            var origGetCtx = HTMLCanvasElement.prototype.getContext;
            HTMLCanvasElement.prototype.getContext = function(type, attrs) {
                if (type === 'webgl' || type === 'experimental-webgl' || type === 'webgl2') {
                    attrs = attrs || {};
                    attrs.failIfMajorPerformanceCaveat = false;
                    attrs.antialias = false;
                    attrs.powerPreference = 'low-power';
                }
                return origGetCtx.call(this, type, attrs);
            };
        }
        """.trimIndent()
    }

    val script = """
        (function() {
            if (window.__auraStabilityShimApplied) return;
            window.__auraStabilityShimApplied = true;
            function isUnsupportedVideoCodec(typeStr) {
                if (!typeStr || typeof typeStr !== 'string') return false;
                var lower = typeStr.toLowerCase();
                if (lower.indexOf('av01') !== -1 ||
                    lower.indexOf('vp9') !== -1 ||
                    lower.indexOf('vp09') !== -1 ||
                    lower.indexOf('hev1') !== -1 ||
                    lower.indexOf('hvc1') !== -1 ||
                    lower.indexOf('dvh1') !== -1 ||
                    lower.indexOf('dvhe') !== -1 ||
                    lower.indexOf('video/webm') !== -1) {
                    return true;
                }
                var wMatch = lower.match(/width=(\d+)/);
                if (wMatch && parseInt(wMatch[1], 10) > 1280) return true;
                var hMatch = lower.match(/height=(\d+)/);
                if (hMatch && parseInt(hMatch[1], 10) > 720) return true;
                var fpsMatch = lower.match(/framerate=(\d+)/);
                if (fpsMatch && parseInt(fpsMatch[1], 10) > 35) return true;
                return false;
            }
            try {
                if (typeof HTMLMediaElement !== 'undefined' && HTMLMediaElement.prototype && typeof HTMLMediaElement.prototype.canPlayType === 'function') {
                    var origCanPlayType = HTMLMediaElement.prototype.canPlayType;
                    HTMLMediaElement.prototype.canPlayType = function(type) {
                        if (isUnsupportedVideoCodec(type)) return '';
                        return origCanPlayType.call(this, type);
                    };
                }
            } catch (e) {}
            try {
                if (typeof window.MediaSource !== 'undefined' && typeof window.MediaSource.isTypeSupported === 'function') {
                    var origIsTypeSupported = window.MediaSource.isTypeSupported.bind(window.MediaSource);
                    window.MediaSource.isTypeSupported = function(mimeType) {
                        if (isUnsupportedVideoCodec(mimeType)) return false;
                        return origIsTypeSupported(mimeType);
                    };
                }
            } catch (e) {}
            try {
                if (typeof navigator !== 'undefined') {
                    try {
                        Object.defineProperty(navigator, 'gpu', {
                            get: function() { return undefined; },
                            configurable: true
                        });
                    } catch (e) {}
                    navigator.requestMediaKeySystemAccess = function() {
                        return Promise.reject(new DOMException('EME disabled on this device', 'NotSupportedError'));
                    };
                    if (navigator.mediaCapabilities) {
                        navigator.mediaCapabilities.decodingInfo = function(config) {
                            try {
                                var vCfg = config && config.video ? config.video : null;
                                if (vCfg) {
                                    var vType = vCfg.contentType || '';
                                    var vW = vCfg.width || 0;
                                    var vH = vCfg.height || 0;
                                    var vFps = vCfg.framerate || 0;
                                    if (isUnsupportedVideoCodec(vType) || vW > 1280 || vH > 720 || vFps > 35) {
                                        return Promise.resolve({
                                            supported: false,
                                            smooth: false,
                                            powerEfficient: false
                                        });
                                    }
                                }
                            } catch (e) {}
                            return Promise.resolve({
                                supported: true,
                                smooth: true,
                                powerEfficient: true
                            });
                        };
                    }
                    if (navigator.serviceWorker) {
                        navigator.serviceWorker.register = function() {
                            return Promise.reject(new DOMException('ServiceWorker disabled', 'SecurityError'));
                        };
                    }
                }
            } catch (e) {}
            try {
                $webGlPatch
            } catch (e) {}
        })();
    """.trimIndent()

    try {
        webView.evaluateJavascript(script, null)
    } catch (_: Exception) {}
}

/**
 * Syncs WebView pause/resume state with the hosting Lifecycle so backgrounded renderers
 * suspend timers and active media codecs cleanly instead of being killed by LMK.
 */
@Composable
fun BindWebViewLifecycle(webView: WebView?) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, webView) {
        if (webView == null) return@DisposableEffect onDispose {}
        val observer = LifecycleEventObserver { _, event ->
            if (isWebViewDestroyed(webView)) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    try { webView.onPause() } catch (_: Exception) {}
                    try { webView.pauseTimers() } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_RESUME -> {
                    try { webView.resumeTimers() } catch (_: Exception) {}
                    try { webView.onResume() } catch (_: Exception) {}
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Volatile
private var parkedKeepAliveWebView: WebView? = null

/**
 * Safely and idempotently tears down a WebView instance.
 * When [rendererGone] is false (normal navigation/dismissal), unloads active media via about:blank
 * and parks a single idle instance so Chromium's RenderProcessHost refCount never drops to 0
 * (preventing AwBrowserTerminator from unbinding/SIGKILLing SandboxedProcessService0 with code -1).
 */
fun safeDestroyWebView(webView: WebView?, rendererGone: Boolean = false) {
    if (webView == null) return
    if (!destroyedWebViews.add(webView)) return

    try {
        (webView.parent as? ViewGroup)?.removeView(webView)
    } catch (_: Exception) {}

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            webView.webViewRenderProcessClient = null
        } catch (_: Exception) {}
    }

    if (rendererGone) {
        if (parkedKeepAliveWebView === webView) {
            parkedKeepAliveWebView = null
        }
        try {
            webView.removeAllViews()
        } catch (_: Exception) {}
        try {
            webView.webChromeClient = null
            webView.webViewClient = WebViewClient()
        } catch (_: Exception) {}
        try {
            webView.destroy()
        } catch (_: Exception) {}
        return
    }

    try {
        webView.stopLoading()
    } catch (_: Exception) {}
    try {
        webView.loadUrl("about:blank")
    } catch (_: Exception) {}
    try {
        webView.onPause()
    } catch (_: Exception) {}
    try {
        webView.clearHistory()
    } catch (_: Exception) {}
    try {
        webView.webChromeClient = null
        webView.webViewClient = WebViewClient()
    } catch (_: Exception) {}

    val previousParked = parkedKeepAliveWebView
    parkedKeepAliveWebView = webView

    if (previousParked != null && previousParked !== webView) {
        postOnMain(1500L) {
            try {
                previousParked.removeAllViews()
            } catch (_: Exception) {}
            try {
                previousParked.destroy()
            } catch (_: Exception) {}
        }
    }
}
