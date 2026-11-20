package com.lukeneedham.pushtotalkai.assistant

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lukeneedham.common.logger.Logger

class AndroidSystemSetup(private val context: Context) : SystemSetup {

    override fun readStatus() = SetupStatus(
        isAccessibilityOn = isAccessibilityServiceOn(),
        isNotificationAccessOn = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName),
        isMicrophoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
        isBatteryUnrestricted = context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName),
        isDefaultAssistant = isDefaultAssistant(),
    )

    override fun openAccessibilitySettings() {
        open(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    override fun openNotificationAccessSettings() {
        open(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    override fun openBatterySettings() {
        val request = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
        if (!open(request)) open(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }

    override fun openAssistantSettings() {
        if (!open(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))) open(Intent(Settings.ACTION_SETTINGS))
    }

    override fun openAppInfo() {
        open(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
    }

    override fun startAssistant() {
        try {
            ContextCompat.startForegroundService(context, Intent(context, AssistantService::class.java))
        } catch (e: Exception) {
            Logger.e("Could not start the assistant", e)
        }
    }

    override fun stopAssistant() {
        context.stopService(Intent(context, AssistantService::class.java))
    }

    /** The setting names the selected assistant as `package/class`. */
    private fun isDefaultAssistant(): Boolean = listOf("voice_interaction_service", "assistant").any { key ->
        val value = try {
            Settings.Secure.getString(context.contentResolver, key)
        } catch (e: Exception) {
            null
        }
        value != null && ComponentName.unflattenFromString(value)?.packageName == context.packageName
    }

    private fun isAccessibilityServiceOn(): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        val ours = ComponentName(context, HeadsetKeyService::class.java).flattenToString()
        return enabled.split(':').any { it.equals(ours, ignoreCase = true) }
    }

    private fun open(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: Exception) {
        Logger.w("Could not open ${intent.action}", e)
        false
    }
}
