package com.lukeneedham.pushtotalkai.assistant

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sees every hardware key before any app does (`flagRequestFilterKeyEvents`). The centre button of the headset is consumed while the
 * assistant runs, so Spotify never sees it. Every key is logged, which shows whether the button arrives with the screen off.
 */
class HeadsetKeyService : AccessibilityService(), KoinComponent {

    private val controller: AssistantController by inject()
    private val keyEventLog: KeyEventLog by inject()
    private val status: AssistantStatusStore by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val gestures = HeadsetButtonGestures(scope = scope, onGesture = ::onGesture)

    // Whether the press that is going on now is ours: its up must be consumed too
    private var isConsumingPress = false

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val consumed = event.keyCode in HeadsetKeyCodes.centreButton && handleCentreButton(event)
        log(event, consumed)
        return consumed
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun handleCentreButton(event: KeyEvent): Boolean {
        val isNewPress = event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0
        if (isNewPress) isConsumingPress = status.state.value !is AssistantState.Stopped
        if (!isConsumingPress) return false
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (isNewPress) gestures.onDown()
            KeyEvent.ACTION_UP -> {
                isConsumingPress = false
                gestures.onUp()
            }
        }
        return true
    }

    private fun onGesture(gesture: HeadsetGesture) {
        when (gesture) {
            HeadsetGesture.HoldStart -> controller.onHoldStart()
            HeadsetGesture.HoldEnd -> controller.onHoldEnd()
            is HeadsetGesture.Clicks -> controller.onClicks(gesture.count)
        }
    }

    private fun log(event: KeyEvent, consumed: Boolean) {
        val action = when (event.action) {
            KeyEvent.ACTION_DOWN -> "DOWN"
            KeyEvent.ACTION_UP -> "UP"
            else -> event.action.toString()
        }
        keyEventLog.add(
            KeyLogEntry(
                time = timeFormat.format(Date()),
                key = KeyEvent.keyCodeToString(event.keyCode),
                action = action,
                detail = "dev=${event.device?.name} repeat=${event.repeatCount}${if (consumed) " consumed" else ""}",
            ),
        )
    }
}
