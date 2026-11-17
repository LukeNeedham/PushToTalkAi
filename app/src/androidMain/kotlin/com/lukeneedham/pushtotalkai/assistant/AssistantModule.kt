package com.lukeneedham.pushtotalkai.assistant

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** What needs Android: audio, the services' helpers and the stores. */
object AssistantModule {
    val bindings = module {
        single { SettingsDataStoreFactory.create(androidContext()) }
        single<SettingsStore> { DataStoreSettingsStore(get()) }
        single<MusicControls> { AndroidMusicControls(androidContext()) }
        single<SystemSetup> { AndroidSystemSetup(androidContext()) }
        single { AudioFocusHolder(androidContext()) }
        single { AudioCues() }
        singleOf(::MicRecorder)
        singleOf(::SpeechPlayer)
        singleOf(::AssistantController)
    }
}
