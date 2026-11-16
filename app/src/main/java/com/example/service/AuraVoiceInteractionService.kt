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
                showSession(Bundle.EMPTY, VoiceInteractionSession.SHOW_WITH_ASSIST)
            }
        } catch (_: Exception) {}
    }
}
