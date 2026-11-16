package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import com.example.AuraTvApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val app = context.applicationContext as? AuraTvApp ?: return
            val pendingResult = goAsync()
            app.applicationScope.launch(Dispatchers.IO) {
                try {
                    val mode = app.preferences.dnsBlockerModeFlow.first()
                    if (mode != "off") {
                        val vpnIntent = VpnService.prepare(context)
                        if (vpnIntent == null) {
                            val serviceIntent = Intent(context, DnsBlockerService::class.java).apply {
                                putExtra(DnsBlockerService.EXTRA_MODE, mode)
                            }
                            ContextCompat.startForegroundService(context, serviceIntent)
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
