package com.lukeneedham.pushtotalkai.assistant

import android.os.Bundle
import android.service.voice.VoiceInteractionService
import com.lukeneedham.common.logger.Logger

/**
 * Makes the app selectable as the default digital assistant. Android then hands it the assist gesture, which includes holding the
 * headset button. This is a prototype: it only logs what Android does, to see whether the gesture arrives with the screen off.
 */
class AssistantVoiceService : VoiceInteractionService() {

    override fun onReady() {
        super.onReady()
        Logger.i("VoiceInteraction: service ready, this app is the default assistant")
    }

    override fun onShutdown() {
        Logger.i("VoiceInteraction: service shut down")
        super.onShutdown()
    }

    /** The assist gesture while the phone is locked (`supportsLaunchVoiceAssistFromKeyguard`). */
    override fun onLaunchVoiceAssistFromKeyguard() {
        Logger.i("VoiceInteraction: assist gesture on the lock screen")
        showSession(Bundle(), 0)
    }
}
