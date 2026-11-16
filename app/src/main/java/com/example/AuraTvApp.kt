package com.example

import android.app.Application
import com.example.data.AppRepository
import com.example.data.IptvRepository
import com.example.data.local.AppDatabase
import com.example.data.local.LauncherPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AuraTvApp : Application() {

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

        // Validate WebView cache paths once synchronously before any WebView is constructed
        com.example.ui.common.ensureWebViewCacheDirs(this)

        database = AppDatabase.getDatabase(this)
        preferences = LauncherPreferences(this)
        appRepository = AppRepository(this, preferences)
        iptvRepository = IptvRepository(database.iptvDao(), preferences)

        // Initialize defaults for IPTV
        applicationScope.launch(Dispatchers.IO) {
            iptvRepository.initializeDefaultsIfNeeded()
        }
    }

    internal var webViewKeepAliveAnchor: android.webkit.WebView? = null

    fun ensureWebViewKeepAliveAnchor() {
        if (webViewKeepAliveAnchor != null) return
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
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            releaseWebViewKeepAliveAnchor()
        }
    }
}
