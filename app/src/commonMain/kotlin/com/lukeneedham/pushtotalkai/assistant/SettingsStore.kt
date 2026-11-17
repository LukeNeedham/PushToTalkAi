package com.lukeneedham.pushtotalkai.assistant

import kotlinx.coroutines.flow.Flow

/** Keeps the [AssistantSettings] on the device. */
interface SettingsStore {
    val settings: Flow<AssistantSettings>

    suspend fun save(settings: AssistantSettings)
}
