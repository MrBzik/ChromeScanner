package com.solid.server

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer

class ScanningService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action){

            ServiceActions.START.toString() -> start()

            ServiceActions.STOP.toString() -> stopSelf()


            ServiceActions.CONFIGURE.toString() -> {

            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    private fun start(){

        val stopIntent = PendingIntent.getService(this, 1, Intent(this, ScanningService::class.java).also {
            it.action = ServiceActions.STOP.toString()
        },
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Scanning in progress")
            .addAction(0, "Stop", stopIntent)
            .build()
        startForeground(1, notification)
    }


    override fun onCreate() {
        super.onCreate()

        embeddedServer(factory = CIO, port = 8080, module = Application::module).start()

    }


}