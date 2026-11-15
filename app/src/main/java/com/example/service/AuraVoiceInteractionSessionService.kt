package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import com.example.MainActivity

class AuraVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return AuraVoiceInteractionSession(this)
    }
}

private class AuraVoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val closeSessionRunnable = Runnable {
        try {
            hide()
            finish()
        } catch (_: Exception) {}
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        mainHandler.removeCallbacks(closeSessionRunnable)
        try {
            // If Aura TV Launcher is already visible in the foreground, trigger Voice Search directly
            // without incurring an unnecessary Activity transition or BAL delay.
            if (!MainActivity.triggerVoiceSearchIfForeground()) {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    action = MainActivity.ACTION_AURA_VOICE_SEARCH
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    )
                }
                var launched = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        // Uses the system Assist token, exempt from Android 10+ Background Activity Launch limits.
                        startAssistantActivity(launchIntent)
                        launched = true
                    } catch (_: Exception) {}
                }
                if (!launched) {
                    context.startActivity(launchIntent)
                }
            }
        } catch (_: Exception) {
        } finally {
            // Delay tearing down the session slightly so startAssistantActivity completes cleanly.
            mainHandler.postDelayed(closeSessionRunnable, 220L)
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(closeSessionRunnable)
        super.onDestroy()
    }
}
