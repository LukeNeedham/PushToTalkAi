package com.lukeneedham.pushtotalkai.assistant

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import com.lukeneedham.common.logger.Logger

/** Controls Spotify through its media session (needs notification access), and falls back to media keys. */
class AndroidMusicControls(private val context: Context) : MusicControls {

    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val sessionManager = context.getSystemService(MediaSessionManager::class.java)
    private val listenerComponent = ComponentName(context, MediaSessionAccess::class.java)
    private val spotifyPackage = "com.spotify.music"

    override suspend fun sendCommand(command: MusicCommand): Boolean {
        val controller = findController()
        Logger.i("Music: $command, session=${controller?.packageName}")
        if (controller != null) {
            val controls = controller.transportControls
            when (command) {
                MusicCommand.Play -> controls.play()
                MusicCommand.Pause -> controls.pause()
                MusicCommand.Next -> controls.skipToNext()
                MusicCommand.Previous -> controls.skipToPrevious()
                MusicCommand.PlayPause ->
                    if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) controls.pause() else controls.play()
            }
            return true
        }
        return dispatchMediaKey(command)
    }

    override suspend fun playFromSearch(query: String): Boolean {
        val controller = findController(spotifyOnly = true)
        Logger.i("Music: play \"$query\", Spotify session=${controller?.packageName}")
        if (controller != null) {
            controller.transportControls.playFromSearch(query, Bundle())
            return true
        }
        // Spotify has no media session yet: ask it to start, which Android may refuse from the background
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
            .setPackage(spotifyPackage)
            .putExtra(SearchManager.QUERY, query)
            .putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Logger.w("Could not start Spotify", e)
            false
        }
    }

    override suspend fun describeNowPlaying(): String? {
        val controller = findController() ?: return null
        val metadata = controller.metadata ?: return null
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: return null
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
        val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
        val song = if (artist.isNullOrBlank()) title else "$title by $artist"
        return if (isPlaying) "$song (playing)" else "$song (paused)"
    }

    private fun findController(spotifyOnly: Boolean = false): MediaController? = try {
        // Not this app's own session: it only receives the media buttons
        val sessions = sessionManager.getActiveSessions(listenerComponent).filter { it.packageName != context.packageName }
        sessions.firstOrNull { it.packageName == spotifyPackage } ?: sessions.firstOrNull().takeUnless { spotifyOnly }
    } catch (e: SecurityException) {
        // Notification access is not switched on
        Logger.w("No access to the media sessions", e)
        null
    }

    private fun dispatchMediaKey(command: MusicCommand): Boolean {
        val keyCode = when (command) {
            MusicCommand.Play -> KeyEvent.KEYCODE_MEDIA_PLAY
            MusicCommand.Pause -> KeyEvent.KEYCODE_MEDIA_PAUSE
            MusicCommand.PlayPause -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            MusicCommand.Next -> KeyEvent.KEYCODE_MEDIA_NEXT
            MusicCommand.Previous -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
        }
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        return true
    }
}
