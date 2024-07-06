package com.solid.client.data.remote

import com.solid.dto.ServerResponses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ServerConnector {

    val serverResponses : Flow<ServerResponses>
    val isConnected : StateFlow<Boolean>
    val isScanning : StateFlow<Boolean>

    suspend fun establishConnection(port: Int, host: String)

    suspend fun startScanning(intervalsSec: Int)

    suspend fun stopScanning()

    suspend fun recoverFileSystem(id : Long) : Boolean

}