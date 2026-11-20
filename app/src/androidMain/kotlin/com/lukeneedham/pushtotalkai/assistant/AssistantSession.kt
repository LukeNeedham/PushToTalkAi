package com.lukeneedham.pushtotalkai.assistant

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.service.voice.VoiceInteractionSession
import com.lukeneedham.common.logger.Logger

/** The prototype of the assist gesture: logs the state of the phone, beeps, and closes at once. */
class AssistantSession(context: Context) : VoiceInteractionSession(context) {

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        val isInteractive = context.getSystemService(PowerManager::class.java).isInteractive
        val isLocked = context.getSystemService(KeyguardManager::class.java).isKeyguardLocked
        Logger.i("VoiceInteraction: session shown, flags=0x${showFlags.toString(16)}, screenOn=$isInteractive, locked=$isLocked, args=$args")
        AudioCues().listening()
        hide()
    }

    override fun onHide() {
        Logger.i("VoiceInteraction: session hidden")
        super.onHide()
    }
}
