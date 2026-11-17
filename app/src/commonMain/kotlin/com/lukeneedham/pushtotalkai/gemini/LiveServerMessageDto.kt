package com.lukeneedham.pushtotalkai.gemini

import com.lukeneedham.pushtotalkai.assistant.ToolCall
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
internal data class LiveServerMessageDto(
    val setupComplete: JsonObject? = null,
    val serverContent: ServerContentDto? = null,
    val toolCall: ToolCallDto? = null,
)

@Serializable
internal data class ServerContentDto(
    val modelTurn: ModelTurnDto? = null,
    val turnComplete: Boolean? = null,
    val interrupted: Boolean? = null,
)

@Serializable
internal data class ModelTurnDto(val parts: List<PartDto>? = null)

@Serializable
internal data class PartDto(val inlineData: InlineDataDto? = null)

@Serializable
internal data class InlineDataDto(
    val mimeType: String? = null,
    val data: String? = null,
)

@Serializable
internal data class ToolCallDto(val functionCalls: List<FunctionCallDto>? = null)

@Serializable
internal data class FunctionCallDto(
    val id: String? = null,
    val name: String? = null,
    val args: JsonObject? = null,
)

/** Never throws: what cannot be read is dropped. */
@OptIn(ExperimentalEncodingApi::class)
internal fun LiveServerMessageDto.toEvents(): List<LiveEvent> {
    val events = mutableListOf<LiveEvent>()
    if (setupComplete != null) events += LiveEvent.SetupComplete
    val content = serverContent
    if (content?.interrupted == true) events += LiveEvent.Interrupted
    content?.modelTurn?.parts.orEmpty().forEach { part ->
        val data = part.inlineData?.data ?: return@forEach
        val pcm = try {
            Base64.Default.decode(data)
        } catch (e: IllegalArgumentException) {
            return@forEach
        }
        events += LiveEvent.Audio(pcm)
    }
    val calls = toolCall?.functionCalls.orEmpty().mapNotNull { call ->
        val name = call.name ?: return@mapNotNull null
        ToolCall(id = call.id.orEmpty(), name = name, args = call.args ?: JsonObject(emptyMap()))
    }
    if (calls.isNotEmpty()) events += LiveEvent.ToolCalls(calls)
    if (content?.turnComplete == true) events += LiveEvent.TurnComplete
    return events
}
