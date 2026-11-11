package com.example

import android.app.Application
import com.example.data.AppRepository
import com.example.data.IptvRepository
import com.example.data.YouTubeRepository
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

    lateinit var youtubeRepository: YouTubeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

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
        youtubeRepository = YouTubeRepository(database.youtubeDao())

        // Initialize defaults for IPTV and YouTube
        applicationScope.launch(Dispatchers.IO) {
            iptvRepository.initializeDefaultsIfNeeded()
            youtubeRepository.initializeDefaultsIfNeeded()
        }
    }

    companion object {
        lateinit var instance: AuraTvApp
            private set
    }
}
