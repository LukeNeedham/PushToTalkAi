package com.lukeneedham.pushtotalkai.assistant

/** Which of the things the user has to switch on in Android are on. */
data class SetupStatus(
    val isAccessibilityOn: Boolean,
    val isNotificationAccessOn: Boolean,
    val isMicrophoneGranted: Boolean,
    val isBatteryUnrestricted: Boolean,
)
