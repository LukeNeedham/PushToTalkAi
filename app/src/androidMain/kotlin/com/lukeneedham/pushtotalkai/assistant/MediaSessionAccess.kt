package com.lukeneedham.pushtotalkai.assistant

import android.service.notification.NotificationListenerService

/** Does nothing itself. Android lets an app list the media sessions of other apps (Spotify's) only when it has notification access. */
class MediaSessionAccess : NotificationListenerService()
