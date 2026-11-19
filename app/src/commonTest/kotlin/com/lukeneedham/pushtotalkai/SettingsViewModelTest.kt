package com.lukeneedham.pushtotalkai

import com.lukeneedham.pushtotalkai.assistant.AssistantSettings
import com.lukeneedham.pushtotalkai.assistant.AssistantState
import com.lukeneedham.pushtotalkai.assistant.AssistantStatusStore
import com.lukeneedham.pushtotalkai.assistant.SettingsStore
import com.lukeneedham.pushtotalkai.assistant.SetupStatus
import com.lukeneedham.pushtotalkai.assistant.SystemSetup
import com.lukeneedham.pushtotalkai.ui.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Names without spaces: a test of commonTest also runs on the targets that do not allow them
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private class FakeSettingsStore(initial: AssistantSettings) : SettingsStore {
        private val state = MutableStateFlow(initial)
        override val settings: Flow<AssistantSettings> = state
        override suspend fun save(settings: AssistantSettings) {
            state.value = settings
        }
    }

    private class FakeSystemSetup : SystemSetup {
        var started = 0
        var stopped = 0
        override fun readStatus() = SetupStatus(
            isAccessibilityOn = false,
            isNotificationAccessOn = false,
            isMicrophoneGranted = true,
            isBatteryUnrestricted = false,
            isDefaultAssistant = false,
        )
        override fun openAccessibilitySettings() = Unit
        override fun openNotificationAccessSettings() = Unit
        override fun openBatterySettings() = Unit
        override fun openAssistantSettings() = Unit
        override fun startAssistant() {
            started++
        }
        override fun stopAssistant() {
            stopped++
        }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        store: SettingsStore = FakeSettingsStore(AssistantSettings()),
        setup: SystemSetup = FakeSystemSetup(),
        status: AssistantStatusStore = AssistantStatusStore(),
        isDebugEnabled: Boolean = true,
    ) = SettingsViewModel(isDebugEnabled = isDebugEnabled, settingsStore = store, systemSetup = setup, statusStore = status)

    @Test
    fun theStateSaysWhetherDebuggingIsOn() {
        assertEquals(true, viewModel(isDebugEnabled = true).uiState.value.isDebugEnabled)
        assertEquals(false, viewModel(isDebugEnabled = false).uiState.value.isDebugEnabled)
    }

    @Test
    fun theSavedSettingsAreShown() {
        val store = FakeSettingsStore(AssistantSettings(apiKey = "key", model = "model"))
        val state = viewModel(store = store).uiState.value
        assertEquals("key", state.apiKey)
        assertEquals("model", state.model)
    }

    @Test
    fun typingTheKeySavesIt() {
        val store = FakeSettingsStore(AssistantSettings())
        val viewModel = viewModel(store = store)
        viewModel.onApiKeyChange(" abc ")
        assertEquals("abc", viewModel.uiState.value.apiKey)
    }

    @Test
    fun theButtonStartsAStoppedAssistantAndStopsARunningOne() {
        val setup = FakeSystemSetup()
        val status = AssistantStatusStore()
        val viewModel = viewModel(setup = setup, status = status)
        viewModel.onToggleAssistant()
        assertEquals(1, setup.started)
        status.set(AssistantState.Idle)
        viewModel.onToggleAssistant()
        assertEquals(1, setup.stopped)
    }
}
