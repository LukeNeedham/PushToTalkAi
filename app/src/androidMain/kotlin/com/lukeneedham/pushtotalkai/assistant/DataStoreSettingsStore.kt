package com.lukeneedham.pushtotalkai.assistant

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSettingsStore(private val dataStore: DataStore<Preferences>) : SettingsStore {
    private val apiKeyKey = stringPreferencesKey("api_key")
    private val modelKey = stringPreferencesKey("model")

    override val settings: Flow<AssistantSettings> = dataStore.data.map { preferences ->
        AssistantSettings(
            apiKey = preferences[apiKeyKey].orEmpty(),
            model = preferences[modelKey]?.takeIf { it.isNotBlank() } ?: AssistantSettings.defaultModel,
        )
    }

    override suspend fun save(settings: AssistantSettings) {
        dataStore.edit { preferences ->
            preferences[apiKeyKey] = settings.apiKey
            preferences[modelKey] = settings.model
        }
    }
}
