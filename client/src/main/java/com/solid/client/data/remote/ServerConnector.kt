package com.solid.client.data.remote

import com.solid.dto.ServerResponses
import kotlinx.coroutines.flow.Flow

interface ServerConnector {

    val serverResponses : Flow<ServerResponses>

    suspend fun establishConnection()

    suspend fun startScanning(intervalsSec: Int)

    suspend fun stopScanning()

    suspend fun recoverFileSystem(id : Long)

}