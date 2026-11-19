package com.lukeneedham.pushtotalkai.assistant

import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.view.KeyEvent
import com.lukeneedham.common.logger.Logger

/**
 * The second way to get the centre button. The USB-C EarPods button does not go through the accessibility key filter (volume does), so
 * Android sends it to the media session that is "playing" most recently. While the assistant runs, this app holds such a session, which
 * does no audio: it only receives the media buttons, and takes them away from Spotify and Google Assistant.
 */
class MediaButtonSession(
    private val context: Context,
    private val input: HeadsetButtonInput,
) {
    private var session: MediaSession? = null

    fun start() {
        if (session != null) return
        val mediaSession = MediaSession(context, "PushToTalkAi")
        mediaSession.setCallback(
            object : MediaSession.Callback() {
                override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean = handle(mediaButtonIntent)
            },
        )
        // The system gives the buttons to the session that most recently said that it plays
        mediaSession.setPlaybackState(
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY_PAUSE)
                .setState(PlaybackState.STATE_PLAYING, 0, 1f)
                .build(),
        )
        mediaSession.isActive = true
        session = mediaSession
        Logger.i("MediaButton: session active")
    }

    fun stop() {
        session?.let {
            it.isActive = false
            it.release()
        }
        session = null
        Logger.i("MediaButton: session released")
    }

    private fun handle(intent: Intent): Boolean {
        val event = keyEventOf(intent)
        if (event == null) {
            Logger.w("MediaButton: intent without a key event: $intent")
            return false
        }
        val isCentre = HeadsetKeyCodes.isCentreButton(event)
        Logger.i(
            "MediaButton: ${if (event.action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"} ${KeyEvent.keyCodeToString(event.keyCode)} " +
                "(code ${event.keyCode}, scan ${event.scanCode}) repeat=${event.repeatCount} flags=0x${event.flags.toString(16)} centre=$isCentre",
        )
        if (!isCentre) return false
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (event.repeatCount == 0) input.onDown()
            KeyEvent.ACTION_UP -> input.onUp()
        }
        return true
    }

    @Suppress("DEPRECATION")
    private fun keyEventOf(intent: Intent): KeyEvent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        } else {
            intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
        }
}
