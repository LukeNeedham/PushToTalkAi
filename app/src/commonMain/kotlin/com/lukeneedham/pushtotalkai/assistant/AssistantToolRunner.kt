package com.lukeneedham.pushtotalkai.assistant

import com.lukeneedham.common.logger.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** The functions Gemini may call, and what they do to the music player. A failure becomes an `error` Gemini can tell the user about. */
class AssistantToolRunner(private val music: MusicControls) {
    private val musicControlName = "music_control"
    private val playMusicName = "play_music"
    private val nowPlayingName = "now_playing"

    /** Sent to Gemini in the setup message. */
    val declarations: JsonArray = buildJsonArray {
        addJsonObject {
            put("name", musicControlName)
            put("description", "Controls the music that is playing on the phone (Spotify): play, pause, skip to the next song or go back to the previous one.")
            putJsonObject("parameters") {
                put("type", "OBJECT")
                putJsonObject("properties") {
                    putJsonObject("command") {
                        put("type", "STRING")
                        putJsonArray("enum") { MusicCommand.entries.forEach { add(it.argument) } }
                    }
                }
                putJsonArray("required") { add("command") }
            }
        }
        addJsonObject {
            put("name", playMusicName)
            put("description", "Searches Spotify for a song, artist, album or playlist and plays it.")
            putJsonObject("parameters") {
                put("type", "OBJECT")
                putJsonObject("properties") {
                    putJsonObject("query") {
                        put("type", "STRING")
                        put("description", "What to play, for example a song title and artist.")
                    }
                }
                putJsonArray("required") { add("query") }
            }
        }
        addJsonObject {
            put("name", nowPlayingName)
            put("description", "Tells which song is playing right now.")
        }
    }

    suspend fun run(call: ToolCall): JsonObject = try {
        when (call.name) {
            musicControlName -> runMusicControl(call)
            playMusicName -> runPlayMusic(call)
            nowPlayingName -> result(music.describeNowPlaying() ?: "Nothing is playing")
            else -> failure("Unknown function ${call.name}")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Logger.e("Tool ${call.name} failed", e)
        failure(e.message ?: "Failed")
    }

    private suspend fun runMusicControl(call: ToolCall): JsonObject {
        val command = MusicCommand.fromArgument(call.args["command"]?.jsonPrimitive?.contentOrNull)
            ?: return failure("Unknown command")
        return if (music.sendCommand(command)) result("done") else failure("Could not control the music player")
    }

    private suspend fun runPlayMusic(call: ToolCall): JsonObject {
        val query = call.args["query"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: return failure("No search text")
        return if (music.playFromSearch(query)) result("playing $query") else failure("Spotify is not running or did not accept the search")
    }

    private fun result(text: String) = buildJsonObject { put("result", text) }

    private fun failure(text: String) = buildJsonObject { put("error", text) }
}
