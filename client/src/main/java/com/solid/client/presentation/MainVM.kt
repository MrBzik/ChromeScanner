package com.solid.client.presentation

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solid.client.data.remote.ServerConnector
import com.solid.client.fileutils.printTree
import com.solid.client.utils.CONFIG_HOST
import com.solid.client.utils.CONFIG_PORT
import com.solid.client.utils.Logger
import com.solid.dto.FileTreeScan
import com.solid.dto.ServerResponses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    var currentPort = "12345"
        private set
    var currentHost = "10.0.2.2"
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
                serverConnector.startScanning(4)
            }
        }
    }

    fun restoreScan(id: Long) {
        viewModelScope.launch {
            serverConnector.recoverFileSystem(id)
        }
    }

    private fun getCurrentConf(){
        currentHost = configPrefs.getString(CONFIG_HOST, "10.0.0.2") ?: "10.0.0.2"
        currentPort = configPrefs.getString(CONFIG_PORT, "12345") ?: "12345"
    }

    fun updateConfiguration(port: String, host: String){
        configPrefs.edit().apply{
            putString(CONFIG_PORT, port)
            putString(CONFIG_HOST, host)
            apply()
            currentHost = host
            currentPort = port
        }
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

                        _currentTree.update {
                            response.scan
                        }

                    }
                    is ServerResponses.ScanRecoveryResults -> {


                    }
                    is ServerResponses.ScansList -> {
                        _scansList.update { response.scansList }
                    }
                }
            }
        }
    }


}