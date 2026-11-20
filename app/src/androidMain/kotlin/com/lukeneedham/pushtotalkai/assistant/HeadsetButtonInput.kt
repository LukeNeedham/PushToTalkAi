package com.lukeneedham.pushtotalkai.assistant

import com.lukeneedham.common.logger.Logger
import kotlinx.coroutines.launch

/**
 * Where the centre button arrives, whichever way Android delivers it (the accessibility service, or the media session): the down and up
 * go through [HeadsetButtonGestures], and the gestures to the [AssistantController]. A button seen by both ways is no problem: a second
 * down or up in a row is ignored.
 */
class HeadsetButtonInput(
    private val scope: AssistantScope,
    private val controller: AssistantController,
) {
    private val gestures = HeadsetButtonGestures(scope = scope, onGesture = ::onGesture)

    // Always on the one thread of the scope, because the gestures keep state
    fun onDown() {
        scope.launch { gestures.onDown() }
    }

    fun onUp() {
        scope.launch { gestures.onUp() }
    }

    private fun onGesture(gesture: HeadsetGesture) {
        Logger.i("HeadsetKey: gesture $gesture")
        when (gesture) {
            HeadsetGesture.HoldStart -> controller.onHoldStart()
            HeadsetGesture.HoldEnd -> controller.onHoldEnd()
            is HeadsetGesture.Clicks -> controller.onClicks(gesture.count)
        }
    }
}
