package com.example.service

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

class AuraRecognitionService : RecognitionService() {
    private var delegateRecognizer: SpeechRecognizer? = null

    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        if (listener == null) return
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                listener.error(SpeechRecognizer.ERROR_CLIENT)
                return
            }
            val recognizer = delegateRecognizer ?: SpeechRecognizer.createSpeechRecognizer(this).also {
                delegateRecognizer = it
            }
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    try { listener.readyForSpeech(params ?: Bundle.EMPTY) } catch (_: Exception) {}
                }

                override fun onBeginningOfSpeech() {
                    try { listener.beginningOfSpeech() } catch (_: Exception) {}
                }

                override fun onRmsChanged(rmsdB: Float) {
                    try { listener.rmsChanged(rmsdB) } catch (_: Exception) {}
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    if (buffer != null) {
                        try { listener.bufferReceived(buffer) } catch (_: Exception) {}
                    }
                }

                override fun onEndOfSpeech() {
                    try { listener.endOfSpeech() } catch (_: Exception) {}
                }

                override fun onError(error: Int) {
                    try { listener.error(error) } catch (_: Exception) {}
                }

                override fun onResults(results: Bundle?) {
                    try { listener.results(results ?: Bundle.EMPTY) } catch (_: Exception) {}
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    try { listener.partialResults(partialResults ?: Bundle.EMPTY) } catch (_: Exception) {}
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            recognizer.startListening(recognizerIntent ?: Intent())
        } catch (_: Exception) {
            try { listener.error(SpeechRecognizer.ERROR_CLIENT) } catch (_: Exception) {}
        }
    }

    override fun onCancel(listener: Callback?) {
        try {
            delegateRecognizer?.cancel()
        } catch (_: Exception) {}
    }

    override fun onStopListening(listener: Callback?) {
        try {
            delegateRecognizer?.stopListening()
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        try {
            delegateRecognizer?.destroy()
            delegateRecognizer = null
        } catch (_: Exception) {}
        super.onDestroy()
    }
}
