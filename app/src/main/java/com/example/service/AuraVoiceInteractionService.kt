package com.example.service

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import com.example.MainActivity

class AuraVoiceInteractionService : VoiceInteractionService() {
    override fun onLaunchVoiceAssistFromKeyguard() {
        try {
            if (!MainActivity.triggerVoiceSearchIfForeground()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    showSession(Bundle.EMPTY, VoiceInteractionSession.SHOW_WITH_ASSIST)
                } else {
                    val launchIntent = Intent(this, MainActivity::class.java).apply {
                        action = MainActivity.ACTION_AURA_VOICE_SEARCH
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        )
                    }
                    startActivity(launchIntent)
                }
            }
        } catch (_: Exception) {}
    }
}
