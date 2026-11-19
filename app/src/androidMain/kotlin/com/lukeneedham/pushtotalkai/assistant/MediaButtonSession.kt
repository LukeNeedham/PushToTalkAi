package com.lukeneedham.pushtotalkai.assistant

import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.lukeneedham.common.logger.Logger

/**
 * The second way to get the centre button. The USB-C EarPods button does not go through the accessibility key filter (volume does), so
 * Android sends it to the media session that is "playing" most recently. While the assistant runs, this app holds such a session, which
 * does no audio: it receives the media buttons, keeps the centre button, and passes all the others on to Spotify.
 */
class MediaButtonSession(
    private val context: Context,
    private val input: HeadsetButtonInput,
    private val sessions: ActiveMediaSessions,
) {
    private var session: MediaSession? = null
    private var stopWatching: (() -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())
    private val retake = Runnable { takeButtonsBack() }
    private val retakeDelayMillis = 300L

    fun start() {
        if (session != null) return
        val mediaSession = MediaSession(context, "PushToTalkAi")
        mediaSession.setCallback(
            object : MediaSession.Callback() {
                override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean = handle(mediaButtonIntent)
            },
        )
        mediaSession.isActive = true
        session = mediaSession
        takeButtonsBack()
        // The buttons go to the player that most recently started playing, so when Spotify does, they are taken back
        stopWatching = sessions.observe { scheduleRetake() }
        Logger.i("MediaButton: session active")
    }

    fun stop() {
        handler.removeCallbacks(retake)
        stopWatching?.invoke()
        stopWatching = null
        session?.let {
            it.isActive = false
            it.release()
        }
        session = null
        Logger.i("MediaButton: session released")
    }

    // Waits for the other player to settle: its last state change would otherwise be newer than ours
    private fun scheduleRetake() {
        handler.removeCallbacks(retake)
        handler.postDelayed(retake, retakeDelayMillis)
    }

    /** The system gives the buttons to the session that most recently went to "playing": going through "paused" makes it this one. */
    private fun takeButtonsBack() {
        val mediaSession = session ?: return
        mediaSession.setPlaybackState(playbackState(PlaybackState.STATE_PAUSED))
        mediaSession.setPlaybackState(playbackState(PlaybackState.STATE_PLAYING))
        Logger.d("MediaButton: holding the media buttons")
    }

    private fun playbackState(state: Int) = PlaybackState.Builder()
        .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY_PAUSE)
        .setState(state, 0, 1f)
        .build()

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
        if (!isCentre) return forwardToSpotify(event)
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (event.repeatCount == 0) input.onDown()
            KeyEvent.ACTION_UP -> input.onUp()
        }
        return true
    }

    /** Every other button (next, previous, stop...) still does its normal job: this session only keeps the centre button for itself. */
    private fun forwardToSpotify(event: KeyEvent): Boolean {
        val controller = sessions.find(spotifyOnly = true) ?: return false
        return controller.dispatchMediaButtonEvent(event)
    }

    @Suppress("DEPRECATION")
    private fun keyEventOf(intent: Intent): KeyEvent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        } else {
            intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
        }
}
