package com.solid.server

import android.app.ActivityManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Debug
import android.os.Process
import com.solid.dto.FileTreeScan
import com.solid.dto.ServerResponses
import com.solid.server.service.NOTIFICATION_CHANNEL_ID
import com.solid.server.utils.Logger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.random.Random

@HiltAndroidApp
class ServerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, "Controls Notification", NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(notificationChannel)

    }
}