package com.lukeneedham.pushtotalkai.gemini

import com.lukeneedham.pushtotalkai.assistant.ToolCall

/** What Gemini Live sent back. */
sealed interface LiveEvent {
    /** Speech of Gemini: 16-bit mono PCM at 24 kHz. */
    class Audio(val pcm: ByteArray) : LiveEvent

    /** The user spoke over Gemini: whatever is still queued to play must be dropped. */
    data object Interrupted : LiveEvent

    data object TurnComplete : LiveEvent

    data class ToolCalls(val calls: List<ToolCall>) : LiveEvent

    /** The connection ended, with the reason the server gave (a wrong API key ends it at once). */
    data class Closed(val reason: String?) : LiveEvent

    /** Only inside the session: it completes the connection. */
    data object SetupComplete : LiveEvent
}
