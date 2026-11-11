package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.example.AuraTvApp
import com.example.data.local.LauncherPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            val app = context.applicationContext as? AuraTvApp ?: return
            runBlocking {
                val mode = app.preferences.dnsBlockerModeFlow.first()
                if (mode != "off") {
                    val vpnIntent = VpnService.prepare(context)
                    if (vpnIntent == null) {
                        try {
                            val serviceIntent = Intent(context, DnsBlockerService::class.java).apply {
                                putExtra(DnsBlockerService.EXTRA_MODE, mode)
                            }
                            context.startService(serviceIntent)
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }
}
