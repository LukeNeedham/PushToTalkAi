package com.lukeneedham.pushtotalkai.assistant

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.lukeneedham.common.logger.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Sees every hardware key before any app does (`flagRequestFilterKeyEvents`). The centre button of the headset is consumed while the
 * assistant runs, so Spotify and Google Assistant never see it. Every key is logged (tag `HeadsetKey`), which shows whether the
 * button arrives with the screen off, and what it looks like.
 */
class HeadsetKeyService : AccessibilityService(), KoinComponent {

    private val input: HeadsetButtonInput by inject()
    private val status: AssistantStatusStore by inject()

    // Whether the press that is going on now is ours: its up must be consumed too
    private var isConsumingPress = false

    override fun onCreate() {
        super.onCreate()
        Logger.i("HeadsetKey: service created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 0x20 is FLAG_REQUEST_FILTER_KEY_EVENTS: without it Android never shows this service the keys
        Logger.i("HeadsetKey: service connected, flags=0x${(serviceInfo?.flags ?: 0).toString(16)}, assistant=${status.state.value}")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean = try {
        handleKeyEvent(event)
    } catch (e: Exception) {
        // A crash here would switch the service off, and every key after it would go unseen
        Logger.e("HeadsetKey: failed to handle ${event.keyCode}", e)
        false
    }

    private fun handleKeyEvent(event: KeyEvent): Boolean {
        val isCentre = HeadsetKeyCodes.isCentreButton(event)
        val consumed = isCentre && handleCentreButton(event)
        Logger.i(
            "HeadsetKey: ${actionName(event)} ${KeyEvent.keyCodeToString(event.keyCode)} (code ${event.keyCode}, scan ${event.scanCode}) " +
                "repeat=${event.repeatCount} flags=0x${event.flags.toString(16)} source=0x${event.source.toString(16)} " +
                "device=${event.device?.name} centre=$isCentre consumed=$consumed assistant=${status.state.value}",
        )
        return consumed
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() {
        Logger.w("HeadsetKey: interrupted")
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        Logger.w("HeadsetKey: service unbound (switched off in Android?)")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Logger.i("HeadsetKey: service destroyed")
        super.onDestroy()
    }

    private fun handleCentreButton(event: KeyEvent): Boolean {
        val isNewPress = event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0
        if (isNewPress) isConsumingPress = status.state.value !is AssistantState.Stopped
        if (!isConsumingPress) return false
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (isNewPress) input.onDown()
            KeyEvent.ACTION_UP -> {
                isConsumingPress = false
                input.onUp()
            }
        }
        return true
    }

    private fun actionName(event: KeyEvent) = when (event.action) {
        KeyEvent.ACTION_DOWN -> "DOWN"
        KeyEvent.ACTION_UP -> "UP"
        else -> "ACTION_${event.action}"
    }
}
