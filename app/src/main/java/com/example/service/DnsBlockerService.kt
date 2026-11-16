package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
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

    override fun onRevoke() {
        stopVpn()
        stopSelf()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun startVpn(mode: String) {
        if (vpnInterface != null) {
            stopVpn()
        }

        createNotificationChannel()
        val notification = createNotification()
        val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, foregroundType)

        try {
            if (prepare(this) != null) {
                Log.w(TAG, "VPN permission is not granted; stopping service")
                stopVpn()
                stopSelf()
                return
            }

            val builder = Builder()
                .setSession(if (mode == MODE_FAMILY) "Aura TV Family Protection" else "Aura TV Ads Blocker")
                .setMtu(1500)
                .setBlocking(false)

            val servers = if (mode == MODE_FAMILY) FAMILY_DNS_SERVERS else ADS_DNS_SERVERS
            for (dns in servers) {
                builder.addDnsServer(dns)
            }

            builder.addAddress("10.0.0.2", 32)

            val established = builder.establish()
            if (established == null) {
                Log.w(TAG, "VPN interface returned null; stopping service")
                stopVpn()
                stopSelf()
                return
            }
            vpnInterface = established
            Log.i(TAG, "VPN Interface established with mode: $mode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish VPN", e)
            stopVpn()
            stopSelf()
        }
    }

    private fun stopVpn() {
        try {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            vpnInterface?.close()
            vpnInterface = null
            Log.i(TAG, "VPN Interface closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
    }

    private fun createNotificationChannel() {
        val name = "Ads Blocker Service"
        val descriptionText = "Ensures DNS blocking is active"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
