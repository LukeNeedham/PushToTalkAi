package com.lukeneedham.pushtotalkai.assistant

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Turns the down and up of the headset button into [HeadsetGesture]s: a hold (push to talk) or a count of short presses.
 * The app consumes the button, so it has to rebuild what Android would have done with a click.
 */
class HeadsetButtonGestures(
    private val scope: CoroutineScope,
    private val onGesture: (HeadsetGesture) -> Unit,
    private val holdMillis: Long = 400,
    private val multiClickMillis: Long = 350,
) {
    private var isDown = false
    private var isHold = false
    private var clickCount = 0
    private var holdJob: Job? = null
    private var clickJob: Job? = null

    fun onDown() {
        if (isDown) return
        isDown = true
        isHold = false
        clickJob?.cancel()
        holdJob = scope.launch {
            delay(holdMillis)
            isHold = true
            clickCount = 0
            onGesture(HeadsetGesture.HoldStart)
        }
    }

    fun onUp() {
        if (!isDown) return
        isDown = false
        holdJob?.cancel()
        if (isHold) {
            isHold = false
            onGesture(HeadsetGesture.HoldEnd)
            return
        }
        clickCount++
        clickJob = scope.launch {
            delay(multiClickMillis)
            val count = clickCount
            clickCount = 0
            onGesture(HeadsetGesture.Clicks(count))
        }
    }
}
