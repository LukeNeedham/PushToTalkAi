package com.lukeneedham.pushtotalkai.di

import com.lukeneedham.pushtotalkai.assistant.ActiveMediaSessions
import com.lukeneedham.pushtotalkai.assistant.AndroidMusicControls
import com.lukeneedham.pushtotalkai.assistant.AndroidSystemSetup
import com.lukeneedham.pushtotalkai.assistant.AssistantController
import com.lukeneedham.pushtotalkai.assistant.AudioCues
import com.lukeneedham.pushtotalkai.assistant.AudioFocusHolder
import com.lukeneedham.pushtotalkai.assistant.DataStoreSettingsStore
import com.lukeneedham.pushtotalkai.assistant.HeadsetButtonInput
import com.lukeneedham.pushtotalkai.assistant.MediaButtonSession
import com.lukeneedham.pushtotalkai.assistant.MicRecorder
import com.lukeneedham.pushtotalkai.assistant.MusicControls
import com.lukeneedham.pushtotalkai.assistant.SettingsDataStoreFactory
import com.lukeneedham.pushtotalkai.assistant.SettingsStore
import com.lukeneedham.pushtotalkai.assistant.SpeechPlayer
import com.lukeneedham.pushtotalkai.assistant.SystemSetup
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** What needs Android: audio, the services' helpers and the stores. */
object AssistantModule {
    val bindings = module {
        single { SettingsDataStoreFactory.create(androidContext()) }
        single<SettingsStore> { DataStoreSettingsStore(get()) }
        single { ActiveMediaSessions(androidContext()) }
        single<MusicControls> { AndroidMusicControls(androidContext(), get()) }
        single<SystemSetup> { AndroidSystemSetup(androidContext()) }
        single { AudioFocusHolder(androidContext()) }
        single { AudioCues() }
        singleOf(::MicRecorder)
        singleOf(::SpeechPlayer)
        singleOf(::AssistantController)
        singleOf(::HeadsetButtonInput)
        single { MediaButtonSession(androidContext(), get(), get()) }
    }
}
