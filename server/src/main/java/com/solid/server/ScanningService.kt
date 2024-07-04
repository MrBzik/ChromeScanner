package com.solid.server

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.solid.server.data.local.database.ScansDB
import com.solid.server.shell.ChromeFilesScanner
import com.solid.server.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import javax.inject.Inject

@AndroidEntryPoint
class ScanningService : Service() {


    @Inject
    lateinit var fileScanner : ChromeFilesScanner

    @Inject
    lateinit var scansDB: ScansDB

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action){

            ServiceActions.START.toString() -> start()

            ServiceActions.STOP.toString() -> stopSelf()

            ServiceActions.CONFIGURE.toString() -> {
                val port = intent.extras?.getInt(CONFIG_PORT)
                Logger.log(port.toString())
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

        embeddedServer(factory = CIO, port = 23456, module = Application::module).start()


//        fileScanner.launchScan()

//        val arch = scansDB.getLastArchive()
//        Logger.log(arch.toString())

    }




}