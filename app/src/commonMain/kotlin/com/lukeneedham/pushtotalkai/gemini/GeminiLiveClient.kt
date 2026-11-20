package com.lukeneedham.pushtotalkai.gemini

import com.lukeneedham.pushtotalkai.assistant.AssistantSettings
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** Opens sessions with the Gemini Live API (a WebSocket, with the API key of the user). */
class GeminiLiveClient(private val httpClient: HttpClient) {

    private val endpoint = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"

    private val systemPrompt = "You are a voice assistant that the user talks to through a headset, usually while cycling with the phone in a pocket. " +
        "Answer in one or two short spoken sentences, in the language the user speaks. " +
        "You control the music (Spotify) with your functions: use them when the user asks to play, pause, skip or go back, to play a song, or asks what is playing, " +
        "then say briefly what you did. If a function returns an error, say so."

    /** Connects and sends the setup. Returns once the server accepted it; throws when it cannot. */
    suspend fun connect(settings: AssistantSettings, toolDeclarations: JsonArray, scope: CoroutineScope): GeminiLiveSession {
        val socket = httpClient.webSocketSession(urlString = "$endpoint?key=${settings.apiKey}")
        val session = GeminiLiveSession(socket, scope)
        try {
            session.send(setupMessage(settings.model, toolDeclarations))
            withTimeout(setupTimeoutMillis) { session.awaitSetup() }
        } catch (e: Throwable) {
            session.close()
            throw e
        }
        return session
    }

    private fun setupMessage(model: String, toolDeclarations: JsonArray) = buildJsonObject {
        putJsonObject("setup") {
            put("model", "models/${model.trim().removePrefix("models/")}")
            putJsonObject("generationConfig") {
                putJsonArray("responseModalities") { add("AUDIO") }
            }
            putJsonObject("systemInstruction") {
                putJsonArray("parts") { addJsonObject { put("text", systemPrompt) } }
            }
            putJsonArray("tools") { addJsonObject { put("functionDeclarations", toolDeclarations) } }
            // Push to talk: the app marks the start and the end of the speech itself
            putJsonObject("realtimeInputConfig") {
                putJsonObject("automaticActivityDetection") { put("disabled", true) }
            }
        }
    }

    private val setupTimeoutMillis = 10_000L
}
