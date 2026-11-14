package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import com.example.MainActivity

class AuraVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return AuraVoiceInteractionSession(this)
    }
}

private class AuraVoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {
    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        try {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_AURA_VOICE_SEARCH
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
            }
            context.startActivity(launchIntent)
        } catch (_: Exception) {
        } finally {
            try {
                hide()
                finish()
            } catch (_: Exception) {}
        }
    }
}
