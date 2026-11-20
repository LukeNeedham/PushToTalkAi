package com.lukeneedham.pushtotalkai

import com.lukeneedham.pushtotalkai.assistant.AssistantToolRunner
import com.lukeneedham.pushtotalkai.assistant.MusicCommand
import com.lukeneedham.pushtotalkai.assistant.MusicControls
import com.lukeneedham.pushtotalkai.assistant.ToolCall
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssistantToolRunnerTest {

    private class FakeMusic : MusicControls {
        var lastCommand: MusicCommand? = null
        var lastQuery: String? = null
        override suspend fun sendCommand(command: MusicCommand): Boolean {
            lastCommand = command
            return true
        }
        override suspend fun playFromSearch(query: String): Boolean {
            lastQuery = query
            return true
        }
        override suspend fun describeNowPlaying(): String? = "Song by Artist (playing)"
    }

    private fun call(name: String, vararg args: Pair<String, String>) =
        ToolCall(id = "1", name = name, args = JsonObject(args.associate { it.first to JsonPrimitive(it.second) }))

    @Test
    fun musicControlSendsTheCommand() = runTest {
        val music = FakeMusic()
        val result = AssistantToolRunner(music).run(call("music_control", "command" to "next"))
        assertEquals(MusicCommand.Next, music.lastCommand)
        assertTrue("result" in result)
    }

    @Test
    fun playMusicSearchesForTheQuery() = runTest {
        val music = FakeMusic()
        AssistantToolRunner(music).run(call("play_music", "query" to "Daft Punk"))
        assertEquals("Daft Punk", music.lastQuery)
    }

    @Test
    fun anUnknownCommandIsAnError() = runTest {
        val result = AssistantToolRunner(FakeMusic()).run(call("music_control", "command" to "dance"))
        assertTrue("error" in result)
    }

    @Test
    fun anUnknownFunctionIsAnError() = runTest {
        val result = AssistantToolRunner(FakeMusic()).run(call("fly"))
        assertTrue("error" in result)
    }
}
