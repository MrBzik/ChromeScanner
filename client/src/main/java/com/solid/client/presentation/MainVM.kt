package com.solid.client.presentation

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solid.client.data.remote.ServerConnector
import com.solid.client.domain.ArchiveRecoveryStatus
import com.solid.client.domain.ConfigInputRes
import com.solid.client.fileutils.printTree
import com.solid.client.utils.CONFIG_HOST
import com.solid.client.utils.CONFIG_PORT
import com.solid.client.utils.CONFIG_SCAN_INTERVAL
import com.solid.client.utils.Logger
import com.solid.dto.FileTreeScan
import com.solid.dto.ServerResponses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainVM @Inject constructor(
    private val serverConnector: ServerConnector,
    private val configPrefs: SharedPreferences
) : ViewModel() {


    private val _currentTree : MutableStateFlow<FileTreeScan?> = MutableStateFlow(null)
    val currentTree = _currentTree.asStateFlow()

    private val _memoryStatus : MutableStateFlow<ServerResponses.MemoryStatus?> = MutableStateFlow(null)
    val memoryStatus = _memoryStatus.asStateFlow()

    private val _scansList : MutableStateFlow<List<FileTreeScan>> = MutableStateFlow(emptyList())
    val scansList = _scansList.asStateFlow()

    val isConnectedToServer = serverConnector.isConnected
    val isScanningInProgress = serverConnector.isScanning


    private val serverRecoveryEventsChannel = Channel<ArchiveRecoveryStatus>()
    val serverArchiveRecoveryFlow = serverRecoveryEventsChannel.receiveAsFlow()


    var currentPort = 12345
        private set
    var currentHost = "10.0.2.2"
        private set
    var scanInterval = 10
        private set

    init {
        getCurrentConf()
        observeServerResponses()
        viewModelScope.launch {
            connectToServer()
        }

    }


    fun toggleScanning(){
        viewModelScope.launch {
            if(isScanningInProgress.value){
                serverConnector.stopScanning()
            }
            else {
                serverConnector.startScanning(scanInterval)
            }
        }
    }

    fun restoreScan(id: Long) {
        viewModelScope.launch {
            val isInWork = serverConnector.recoverFileSystem(id)
            if(isInWork){
                serverRecoveryEventsChannel.send(ArchiveRecoveryStatus(true, "На сервере идёт процесс восстановления скана"))
            }
        }
    }

    private fun getCurrentConf(){
        configPrefs.apply {
            currentHost = getString(CONFIG_HOST, "10.0.0.2") ?: "10.0.0.2"
            currentPort = getInt(CONFIG_PORT, 12345)
            scanInterval = getInt(CONFIG_SCAN_INTERVAL, 10)
        }

    }


    fun updateConfiguration(port: String, host: String, interval : String) : ConfigInputRes {
        val newPort = port.toIntOrNull()
        val newInterval = interval.toIntOrNull()
        val isValidIp = checkHostIsValid(host)
        if(newPort != null && newInterval != null && isValidIp){
            configPrefs.edit().apply{
                putInt(CONFIG_PORT, newPort)
                putInt(CONFIG_SCAN_INTERVAL, newInterval)
                putString(CONFIG_HOST, host)
                currentHost = host
                currentPort = newPort
                scanInterval = newInterval
                apply()
            }
        }

        return ConfigInputRes(port = newPort != null, host = isValidIp, interval = newInterval != null)
    }

    private fun checkHostIsValid(string: String): Boolean {
        val list = string.split(".")
        if(list.size != 4) return false
        list.forEach {
            if(it.toIntOrNull() == null)
                return false
        }
        return true
    }


    private suspend fun connectToServer(){
        while (true){
            serverConnector.establishConnection(port= currentPort, host = currentHost)
            delay(1000)
        }
    }


    private fun observeServerResponses(){

        viewModelScope.launch {

            serverConnector.serverResponses.collect { response ->

                when(response){
                    is ServerResponses.MemoryStatus -> {
                        _memoryStatus.update {
                            response
                        }
                    }
                    is ServerResponses.NewScan -> {

                        _currentTree.update { scan ->
                            scan?.let {
                                _scansList.update { scamList ->
                                    scamList + scan
                                }
                            }
                            response.scan
                        }
                    }
                    is ServerResponses.ScanRecoveryResults -> {
                        serverRecoveryEventsChannel.send(
                            ArchiveRecoveryStatus(false, response.message)
                        )
                    }
                    is ServerResponses.ScansList -> {
                        _scansList.update { response.scansList }
                    }
                }
            }
        }
    }


}