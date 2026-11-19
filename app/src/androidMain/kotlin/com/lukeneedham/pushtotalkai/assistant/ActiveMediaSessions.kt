package com.lukeneedham.pushtotalkai.assistant

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import com.lukeneedham.common.logger.Logger

/** Finds the media session of the music player (Spotify). Needs notification access, and never returns the session of this app. */
class ActiveMediaSessions(private val context: Context) {

    private val sessionManager = context.getSystemService(MediaSessionManager::class.java)
    private val listenerComponent = ComponentName(context, MediaSessionAccess::class.java)

    val spotifyPackage = "com.spotify.music"

    /**
     * Calls [onChange] whenever another player starts, stops or changes state. Returns the function that stops watching. Needs notification
     * access: without it nothing is watched.
     */
    fun observe(onChange: () -> Unit): () -> Unit {
        val callback = object : MediaController.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackState?) = onChange()
        }
        var watched = emptyList<MediaController>()
        fun watch(controllers: List<MediaController>?) {
            watched.forEach { it.unregisterCallback(callback) }
            watched = controllers.orEmpty().filter { it.packageName != context.packageName }
            watched.forEach { it.registerCallback(callback) }
        }
        val listener = MediaSessionManager.OnActiveSessionsChangedListener {
            watch(it)
            onChange()
        }
        try {
            sessionManager.addOnActiveSessionsChangedListener(listener, listenerComponent)
            watch(sessionManager.getActiveSessions(listenerComponent))
        } catch (e: SecurityException) {
            Logger.w("No access to watch the media sessions", e)
        }
        return {
            sessionManager.removeOnActiveSessionsChangedListener(listener)
            watch(emptyList())
        }
    }

    /** Spotify's session, else (unless [spotifyOnly]) the first other one. */
    fun find(spotifyOnly: Boolean = false): MediaController? = try {
        val sessions = sessionManager.getActiveSessions(listenerComponent).filter { it.packageName != context.packageName }
        sessions.firstOrNull { it.packageName == spotifyPackage } ?: sessions.firstOrNull().takeUnless { spotifyOnly }
    } catch (e: SecurityException) {
        // Notification access is not switched on
        Logger.w("No access to the media sessions", e)
        null
    }
}
