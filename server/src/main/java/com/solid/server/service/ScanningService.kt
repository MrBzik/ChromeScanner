package com.solid.server.service

import android.app.ActivityManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Process
import androidx.core.app.NotificationCompat
import com.solid.dto.ClientCommands
import com.solid.server.R
import com.solid.server.data.local.database.ScansDB
import com.solid.server.data.remote.ScanServer
import com.solid.server.shell.ChromeFilesScanner
import com.solid.server.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@AndroidEntryPoint
class ScanningService : Service() {


    @Inject
    lateinit var fileScanner : ChromeFilesScanner
    @Inject
    lateinit var scansDB: ScansDB
    @Inject
    lateinit var scanServer: ScanServer

    private var isServiceRunning = false


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action){

            ServiceActions.START.toString() -> start()

            ServiceActions.STOP.toString() -> stopSelf()

            ServiceActions.CONFIGURE.toString() -> {
                val port = intent.extras?.getInt(CONFIG_PORT)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    private fun start(){

        if(isServiceRunning) return

        isServiceRunning = true

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
// adb forward tcp:12345 tcp:23456

        serviceScope.launch {
            scanServer.startServer()
        }

        serviceScope.launch {
            observeClientCommands()
        }

    }



    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        serviceJob.cancel()
    }


    private suspend fun observeClientCommands(){

        scanServer.clientCommands.collect { command ->

            when(command){
                is ClientCommands.RecoverFileSystem -> {
                    Logger.log("ID IS : ${command.fileSystemID}")
                }
                is ClientCommands.StartScan -> {

                }
                is ClientCommands.StopScan -> {

                }
            }
        }



    }



    private fun checkMemoryUsage(){

        CoroutineScope(Dispatchers.IO).launch {

            val runtime = Runtime.getRuntime()
            val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfoArray = activityManager.getProcessMemoryInfo(intArrayOf(Process.myPid()))
            val memoryInfo = memoryInfoArray[0]

            while (true){


                val total = runtime.totalMemory() / 1048576L
                val free = runtime.freeMemory() / 1048576L
                val max = runtime.maxMemory() / 1048576L

                Logger.log("total: $total, free: $free, max: $max")

//                val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
//                Logger.log(usedMem.toString())
//                val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
//                Logger.log(maxHeapSizeInMB.toString())
//                val availHeapSizeInMB = maxHeapSizeInMB - usedMem
//                Logger.log(availHeapSizeInMB.toString())

                Logger.log("***")

                Logger.log("PSS FOR :${Process.myPid()}: ${memoryInfo.totalPss}")

                delay(2000)
            }
        }
    }



}