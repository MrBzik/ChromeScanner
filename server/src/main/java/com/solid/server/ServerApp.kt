package com.solid.server

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

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