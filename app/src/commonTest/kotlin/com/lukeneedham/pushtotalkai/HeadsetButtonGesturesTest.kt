package com.lukeneedham.pushtotalkai

import com.lukeneedham.pushtotalkai.assistant.HeadsetButtonGestures
import com.lukeneedham.pushtotalkai.assistant.HeadsetGesture
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HeadsetButtonGesturesTest {

    private fun TestScope.detector(gestures: MutableList<HeadsetGesture>) =
        HeadsetButtonGestures(scope = backgroundScope, onGesture = { gestures.add(it) })

    @Test
    fun holdingTheButtonStartsAndEndsAHold() = runTest {
        val gestures = mutableListOf<HeadsetGesture>()
        val detector = detector(gestures)
        detector.onDown()
        advanceTimeBy(1_000)
        assertEquals(listOf<HeadsetGesture>(HeadsetGesture.HoldStart), gestures)
        detector.onUp()
        assertEquals(listOf(HeadsetGesture.HoldStart, HeadsetGesture.HoldEnd), gestures)
    }

    @Test
    fun aShortPressIsOneClick() = runTest {
        val gestures = mutableListOf<HeadsetGesture>()
        val detector = detector(gestures)
        detector.onDown()
        advanceTimeBy(100)
        detector.onUp()
        advanceTimeBy(1_000)
        assertEquals(listOf<HeadsetGesture>(HeadsetGesture.Clicks(1)), gestures)
    }

    @Test
    fun twoQuickPressesAreOneDoubleClick() = runTest {
        val gestures = mutableListOf<HeadsetGesture>()
        val detector = detector(gestures)
        detector.onDown()
        advanceTimeBy(100)
        detector.onUp()
        advanceTimeBy(100)
        detector.onDown()
        advanceTimeBy(100)
        detector.onUp()
        advanceTimeBy(1_000)
        assertEquals(listOf<HeadsetGesture>(HeadsetGesture.Clicks(2)), gestures)
    }
}
