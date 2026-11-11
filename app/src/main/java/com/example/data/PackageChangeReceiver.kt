package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_PACKAGE_ADDED ||
            action == Intent.ACTION_PACKAGE_REMOVED ||
            action == Intent.ACTION_PACKAGE_REPLACED
        ) {
            val app = context.applicationContext as? com.example.AuraTvApp
            app?.appRepository?.triggerReload()
        }
    }
}
