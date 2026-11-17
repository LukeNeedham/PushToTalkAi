package com.lukeneedham.pushtotalkai.assistant

/** What the assistant can do to the music player (Spotify). Each call says whether it could be done. */
interface MusicControls {
    suspend fun sendCommand(command: MusicCommand): Boolean

    suspend fun playFromSearch(query: String): Boolean

    /** A line like "Title by Artist (playing)", or null when nothing is known. */
    suspend fun describeNowPlaying(): String?
}
