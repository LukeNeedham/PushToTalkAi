package com.lukeneedham.pushtotalkai.di

import com.lukeneedham.common.DebugKit
import com.lukeneedham.pushtotalkai.assistant.AssistantScope
import com.lukeneedham.pushtotalkai.assistant.AssistantStatusStore
import com.lukeneedham.pushtotalkai.assistant.AssistantToolRunner
import com.lukeneedham.pushtotalkai.assistant.KeyEventLog
import com.lukeneedham.pushtotalkai.gemini.GeminiLiveClient
import com.lukeneedham.pushtotalkai.ui.EventLogViewModel
import com.lukeneedham.pushtotalkai.ui.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** All of the app's Koin modules: `App` starts Koin with [modules]. A feature with many bindings gets an object of its own that this one lists. */
object KoinModules {

    private val app = module {
        viewModel {
            SettingsViewModel(
                isDebugEnabled = DebugKit.enabled,
                settingsStore = get(),
                systemSetup = get(),
                statusStore = get(),
            )
        }
        viewModelOf(::EventLogViewModel)

        singleOf(::KeyEventLog)
        singleOf(::AssistantStatusStore)
        singleOf(::AssistantScope)
        singleOf(::AssistantToolRunner)
        singleOf(::GeminiLiveClient)
    }

    val modules: List<Module> = listOf(app)
}
