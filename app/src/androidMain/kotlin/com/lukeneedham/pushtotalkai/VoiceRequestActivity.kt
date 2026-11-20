package com.lukeneedham.pushtotalkai

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.speech.RecognizerIntent
import android.view.WindowManager
import com.lukeneedham.common.logger.Logger
import com.lukeneedham.pushtotalkai.assistant.AudioCues

/**
 * The window Android opens when the headset button is held: the media service starts a voice search, which is web search with the screen
 * on and hands-free voice search with the screen off or the phone locked. It shows nothing and is not a screen of the app.
 * This is a prototype: it logs the request (tag `VoiceAction`), beeps, and closes.
 */
class VoiceRequestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverTheLockScreen()
        log(intent)
        AudioCues().listening()
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        log(intent)
        finish()
    }

    private fun showOverTheLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
    }

    private fun log(intent: Intent?) {
        val isScreenOn = getSystemService(PowerManager::class.java).isInteractive
        val isLocked = getSystemService(KeyguardManager::class.java).isKeyguardLocked
        Logger.i(
            "VoiceAction: ${intent?.action}, secure=${intent?.getBooleanExtra(RecognizerIntent.EXTRA_SECURE, false)}, " +
                "screenOn=$isScreenOn, locked=$isLocked, extras=${intent?.extras?.keySet()}, flags=0x${(intent?.flags ?: 0).toString(16)}",
        )
    }
}
