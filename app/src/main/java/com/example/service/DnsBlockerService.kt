package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class DnsBlockerService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    companion object {
        private const val TAG = "DnsBlockerService"
        private const val CHANNEL_ID = "dns_blocker_channel"
        private const val NOTIFICATION_ID = 1001
        
        const val EXTRA_MODE = "blocker_mode"
        const val MODE_ADS = "ads"
        const val MODE_FAMILY = "family"

        // AdGuard DNS Default (Ads & Trackers blocking)
        val ADS_DNS_SERVERS = listOf(
            "94.140.14.14",
            "94.140.15.15",
            "2a10:50c0::ad1:ff",
            "2a10:50c0::ad2:ff"
        )

        // AdGuard DNS Family Protection (Ads + Adult content + Safe Search)
        val FAMILY_DNS_SERVERS = listOf(
            "94.140.14.15",
            "94.140.15.16",
            "2a10:50c0::bad1:ff",
            "2a10:50c0::bad2:ff"
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mode = intent?.getStringExtra(EXTRA_MODE) ?: MODE_ADS
        startVpn(mode)
        return START_STICKY
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun startVpn(mode: String) {
        if (vpnInterface != null) {
            // If already running, we might need to restart if mode changed
            // For simplicity, we'll just stop and restart if mode differs
            // but here we check if vpnInterface is not null
            stopVpn()
        }

        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        try {
            val builder = Builder()
                .setSession(if (mode == MODE_FAMILY) "Aura TV Family Protection" else "Aura TV Ads Blocker")
                .setMtu(1500)
                .setBlocking(false)

            val servers = if (mode == MODE_FAMILY) FAMILY_DNS_SERVERS else ADS_DNS_SERVERS
            // Add AdGuard DNS servers
            for (dns in servers) {
                builder.addDnsServer(dns)
            }

            // In some Android versions, to force DNS we might need to add a dummy route 
            // or at least one address. For DNS-only VPN, we don't want to route all traffic.
            // However, on many systems, addDnsServer is only effective if we establish the interface.
            // We use a private IP for the VPN interface itself.
            builder.addAddress("10.0.0.2", 32)
            
            // Note: If we don't add routes, only DNS traffic *might* be affected depending on OS.
            // If we want to be sure, we could add 0.0.0.0/0 but that would break connectivity 
            // without a packet forwarder.
            // For DNS blocking apps, they usually use a local DNS proxy.
            
            vpnInterface = builder.establish()
            Log.i(TAG, "VPN Interface established with mode: $mode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish VPN", e)
            stopSelf()
        }
    }

    private fun stopVpn() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            vpnInterface?.close()
            vpnInterface = null
            Log.i(TAG, "VPN Interface closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ads Blocker Service"
            val descriptionText = "Ensures DNS blocking is active"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ads Blocker Active")
            .setContentText("Aura TV is protecting your connection")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
