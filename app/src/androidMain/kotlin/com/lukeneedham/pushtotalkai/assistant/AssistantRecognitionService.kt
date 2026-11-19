package com.lukeneedham.pushtotalkai.assistant

import android.content.Intent
import android.speech.RecognitionService
import android.speech.SpeechRecognizer

/** Does nothing: some Android versions refuse an assistant that declares no recognition service. */
class AssistantRecognitionService : RecognitionService() {

    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        listener?.error(SpeechRecognizer.ERROR_CLIENT)
    }

    override fun onCancel(listener: Callback?) = Unit

    override fun onStopListening(listener: Callback?) = Unit
}
