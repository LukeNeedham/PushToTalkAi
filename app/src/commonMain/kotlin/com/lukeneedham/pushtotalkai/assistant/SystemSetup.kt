package com.lukeneedham.pushtotalkai.assistant

/** The parts of Android the settings page needs: what is switched on, the system pages to switch it on, and the service. */
interface SystemSetup {
    fun readStatus(): SetupStatus

    fun openAccessibilitySettings()

    fun openNotificationAccessSettings()

    fun openBatterySettings()

    fun openAssistantSettings()

    fun startAssistant()

    fun stopAssistant()
}
