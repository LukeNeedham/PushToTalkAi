package com.lukeneedham.pushtotalkai.assistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.lukeneedham.common.logger.Logger
import com.lukeneedham.pushtotalkai.MainActivity
import com.lukeneedham.pushtotalkai.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Keeps the app alive in the pocket. It is started while the app is on screen, which is what lets Android give it the microphone
 * later, with the screen off. The service itself does nothing: the headset service drives [AssistantController].
 */
class AssistantService : Service(), KoinComponent {

    private val status: AssistantStatusStore by inject()
    private val controller: AssistantController by inject()
    private val mediaButtonSession: MediaButtonSession by inject()

    private val channelId = "assistant"
    private val notificationId = 1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            createChannel()
            ServiceCompat.startForeground(this, notificationId, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
            mediaButtonSession.start()
            status.set(AssistantState.Idle)
            Logger.i("Assistant: service started")
        } catch (e: Exception) {
            // Android refuses a microphone service that starts without the permission or from the background
            Logger.e("The assistant service could not start", e)
            status.set(AssistantState.Error("Could not start: open the app and try again"))
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Logger.i("Assistant: service stopped")
        mediaButtonSession.stop()
        controller.shutdown()
        if (status.state.value !is AssistantState.Error) status.set(AssistantState.Stopped)
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(channelId, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification() = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(android.R.drawable.ic_btn_speak_now)
        .setContentTitle(getString(R.string.notification_title))
        .setContentText(getString(R.string.notification_text))
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE),
        )
        .build()
}
