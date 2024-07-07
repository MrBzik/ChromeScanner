package com.solid.server.service

import android.app.ActivityManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Debug
import android.os.IBinder
import android.os.Process
import androidx.core.app.NotificationCompat
import com.solid.dto.ClientCommands
import com.solid.dto.ServerResponses
import com.solid.server.R
import com.solid.server.data.local.database.ScansDB
import com.solid.server.data.remote.ScanServer
import com.solid.server.filesarchiver.ChromeFilesArchiver
import com.solid.server.filescanner.ChromeFilesScanner
import com.solid.server.repositories.ScansRepo
import com.solid.server.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class ScanningService : Service() {


    @Inject
    lateinit var fileScanner : ChromeFilesScanner
    @Inject
    lateinit var scanServer: ScanServer
    @Inject
    lateinit var fileArchiver: ChromeFilesArchiver
    @Inject
    lateinit var repo : ScansRepo

    private var isServiceRunning = false
    private var isClientConnected = false
    private var isToRunScanning = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {

        private val _logs = MutableStateFlow("")
        val serverLogs = _logs.asStateFlow()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action){

            ServiceActions.START.toString() -> start()

            ServiceActions.STOP.toString() -> {
                serviceScope.launch {
                    scanServer.stopServer()
                    delay(1000)
                    stopSelf()
                }
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
            .setContentTitle("Server is running")
            .addAction(0, "Stop server", stopIntent)
            .build()
        startForeground(1, notification)

    }


    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            scanServer.startServer()
        }

        observeClientCommands()

        observeIsClientConnected()

    }


    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        serviceJob.cancel()
    }


    private fun observeClientCommands(){

        serviceScope.launch {

            scanServer.clientCommands.collect { command ->

                when(command){
                    is ClientCommands.RecoverFileSystem -> {
                        fileArchiver.restoreFileSystemFromArchive(command.fileSystemID)?.let { res ->
                            fileScanner.notifyFileSystemChanged(command.fileSystemID)
                            launch {
                                val responseObj = ServerResponses.ScanRecoveryResults(true, "Scan ${command.fileSystemID} is recovered successfully")
                                val responseJson = Json.encodeToString(ServerResponses.serializer(), responseObj)
                                scanServer.sendJsonResponseToClient(responseJson)
                            }
                            val date = Date(res.timeStamp)
                            val format = SimpleDateFormat.getDateTimeInstance()
                            val dateStr = format.format(date)
                            _logs.update {
                                "Скан ${command.fileSystemID} восстановлен, $dateStr, потрачено ${res.durationMls} МЛС"
                            }
                        }
                    }
                    is ClientCommands.StartScan -> {
                        isToRunScanning = true
                        launch {
                            startScanning(command.intervalSec)
                        }

                    }
                    is ClientCommands.StopScan -> {
                        isToRunScanning = false
                    }
                }
            }
        }
    }


    private suspend fun startScanning(intervalSec : Int) {

        val delay = intervalSec * 1000L

        while (isToRunScanning && isClientConnected){
            delay(delay)

            fileScanner.launchScan()?.let {  scanRes ->

                serviceScope.launch {
                    fileArchiver.archiveFileSystem(scanRes)
                }
                serviceScope.launch {
                    val responseObj = ServerResponses.NewScan(scanRes.fileTreeScan)
                    val responseJson = Json.encodeToString(ServerResponses.serializer(), responseObj)
                    Logger.log(responseJson)
                    scanServer.sendJsonResponseToClient(responseJson)
                }
            }
        }
    }



    private fun observeIsClientConnected(){

        serviceScope.launch {
            scanServer.isClientConnected.collectLatest { isConnected ->

                isClientConnected = isConnected

                if(isConnected){
                    launch {
                        sendAllAvailableScans()
                    }

                    launch {
                        sendMemoryUsageStatus()
                    }
                }
                else {
                    // Don't resume scanning if client reconnects
                    isToRunScanning = false
                }
            }
        }
    }



    private suspend fun sendAllAvailableScans(){
        repo.getAllScansListJson()?.let {
            scanServer.sendJsonResponseToClient(it)
        }
    }


    private suspend fun sendMemoryUsageStatus(){

        val runtime = Runtime.getRuntime()

       while (isClientConnected){
           delay(100)

           val max = runtime.maxMemory() / 1024L
           val totalPSS = Debug.getPss()
           val memoryStatus = ServerResponses.MemoryStatus(
               memoryUsageKb = totalPSS.toInt(),
               availableRamKb = max.toInt()
           )

           val jsonResponse = Json.encodeToString(ServerResponses.serializer(), memoryStatus)
           scanServer.sendJsonResponseToClient(jsonResponse)
       }
    }



}