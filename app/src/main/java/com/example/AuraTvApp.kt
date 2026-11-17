package com.example

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.AppRepository
import com.example.data.IptvRepository
import com.example.data.local.AppDatabase
import com.example.data.local.LauncherPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AuraTvApp : Application(), ImageLoaderFactory {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var database: AppDatabase
        private set

    lateinit var preferences: LauncherPreferences
        private set

    lateinit var appRepository: AppRepository
        private set

    lateinit var iptvRepository: IptvRepository
        private set

    override fun onCreate() {
        super.onCreate()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val processName = getProcessName()
            if (packageName != processName) {
                android.webkit.WebView.setDataDirectorySuffix(processName)
            }
        }

        database = AppDatabase.getDatabase(this)
        preferences = LauncherPreferences(this)
        appRepository = AppRepository(this, preferences)
        iptvRepository = IptvRepository(database.iptvDao(), preferences)

        // Initialize WebView cache dirs & default IPTV data off the main thread
        applicationScope.launch(Dispatchers.IO) {
            com.example.ui.common.ensureWebViewCacheDirs(this@AuraTvApp)
            iptvRepository.initializeDefaultsIfNeeded()
        }
    }

    fun isLowRamTvDevice(): Boolean {
        return try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            am?.isLowRamDevice == true
        } catch (_: Exception) {
            false
        }
    }

    override fun newImageLoader(): ImageLoader {
        val lowRam = isLowRamTvDevice()
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(if (lowRam) 0.10 else 0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("aura_image_cache"))
                    .maxSizeBytes(80L * 1024L * 1024L) // 80 MB bounded disk cache for IPTV logos & 4K wallpapers
                    .build()
            }
            .allowRgb565(true)
            .bitmapConfig(if (lowRam) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888)
            .crossfade(!lowRam)
            .respectCacheHeaders(false)
            .build()
    }

    internal var webViewKeepAliveAnchor: android.webkit.WebView? = null

    fun ensureWebViewKeepAliveAnchor() {
        if (webViewKeepAliveAnchor != null) return
        if (isLowRamTvDevice()) return
        try {
            webViewKeepAliveAnchor = android.webkit.WebView(this).apply {
                webViewClient = com.example.ui.common.createSafeParkedWebViewClient()
                loadUrl("about:blank")
                onPause()
            }
        } catch (_: Exception) {
            webViewKeepAliveAnchor = null
        }
    }

    fun releaseWebViewKeepAliveAnchor() {
        val anchor = webViewKeepAliveAnchor ?: return
        webViewKeepAliveAnchor = null
        com.example.ui.common.destroyWebViewImmediately(anchor)
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_RUNNING_LOW) {
            try {
                coil.Coil.imageLoader(this).memoryCache?.clear()
            } catch (_: Exception) {}
        }
        if (level >= TRIM_MEMORY_UI_HIDDEN || level >= TRIM_MEMORY_RUNNING_CRITICAL) {
            releaseWebViewKeepAliveAnchor()
        }
    }
}
